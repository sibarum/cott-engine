package sibarum.cott;

import sibarum.cott.Term.AWind;
import sibarum.cott.Term.Approx;
import sibarum.cott.Term.Atom;
import sibarum.cott.Term.Call;
import sibarum.cott.Term.Div;
import sibarum.cott.Term.Exp;
import sibarum.cott.Term.Inv;
import sibarum.cott.Term.Lg;
import sibarum.cott.Term.Logb;
import sibarum.cott.Term.Neg;
import sibarum.cott.Term.Plus;
import sibarum.cott.Term.Pow;
import sibarum.cott.Term.Pt;
import sibarum.cott.Term.Times;
import sibarum.cott.Term.Val;
import sibarum.cott.Term.Wind;
import sibarum.cott.Term.Xp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What a session has named: {@code k = 3}, {@code f(x) = x^2+1}.
 *
 * <h2>Expansion, not environment</h2>
 * A binding is expanded into the term <b>before</b> {@link Cott} sees it, and the evaluator therefore has no
 * notion of a scope at all — it stays a function of its argument, which is what makes every rule in it provable
 * on its own. The whole of the mechanism is {@link #expand}: an {@link Atom} bound to a value becomes that
 * value, a {@link Call} to a bound function becomes its body with the arguments put in, and everything else is
 * carried through unchanged.
 *
 * <h2>Definitions are read in the vocabulary that precedes them, and looked up when they are used</h2>
 * A name is only a name if the notation knows it is one. {@code xy} is {@code x·y} and has to stay so — that is
 * the whole of {@link Notation}'s adjacency rule — and the only thing that can tell {@code theta} from five
 * juxtaposed variables is a list of the names in play. So this object is passed to
 * {@link Notation#normalize(String, Bindings)} and {@link Parser#parse(String, Bindings)}, and a definition's own
 * body is read in <em>this</em> vocabulary plus its parameters: a definition may use what was defined before it,
 * and its parameters shadow whatever they are spelled like.
 *
 * <p>That is the only thing settled when a definition is written. What its names <em>mean</em> is settled when it
 * is used, because {@link #expand} walks the body against whatever is defined then — so correcting {@code k}
 * corrects everything that mentions it, which is the reason for writing {@code k} rather than {@code 3} in the
 * first place. It also means the window can list a definition as it was typed, since that is what is stored.
 *
 * <h2>Functors: a parameter that is a function</h2>
 * A parameter written with empty brackets — {@code iter(f(), n) = f(f(n))} — is a <b>function</b> parameter,
 * and is given the bare name of one: {@code iter(g, 3)}, {@code iter(sin, x)}. The brackets are there because
 * of the adjacency rule and not as decoration: {@code f(n)} in a body is a call only if the notation already
 * knows {@code f} names a function, and without the declaration it would be the product {@code f·(n)} — a
 * perfectly good reading of the same characters, which is exactly why it cannot be guessed at.
 *
 * <p><b>What is not declared is the arity.</b> {@code f()} says "a function" and stops there; how many arguments
 * it takes is settled when it is applied, against the function that was actually passed — the same bargain
 * every other name here makes, and the reason {@code iter} works for a defined {@code g} and for {@code sin}
 * alike. A functor is otherwise nothing new in the evaluator: {@link #expand} substitutes a name for a name and
 * then applies it, so what reaches {@link Cott} is still a term with no notion of a function in it at all.
 *
 * <h2>Immutable</h2>
 * {@link #define} and {@link #without} return a new object, so the window that edits a session's definitions and
 * the worker thread that evaluates with them are never looking at a half-written map.
 */
public final class Bindings {

    /**
     * How many times {@link #expand} will unfold before giving up. A definition that mentions itself — directly
     * or through a ring of others — has no expansion, and the honest thing is to say so rather than to run out
     * of stack.
     */
    private static final int LIMIT = 64;

    /** The names the theory owns, which a session may not take. π and e are atoms of it; ω and i are points. */
    private static final Set<String> RESERVED = Set.of("π", "e", "i", "ω", "log");

    /**
     * One parameter. {@code n} stands for a value; {@code f()} stands for a <em>function</em>, and the body may
     * apply it.
     *
     * <p>The brackets are empty because the only thing that has to be settled while the body is being read is
     * <em>which kind of name this is</em> — the notation needs that and nothing else, since it is what tells
     * {@code f(n)} from {@code f·(n)}. How many arguments it takes is a promise the caller keeps, and it is
     * checked where the function that was passed says what it takes.
     */
    public record Param(String name, boolean function) {
        /** As it is written in the head: {@code n}, or {@code f()}. */
        public String head() {
            return function ? name + "()" : name;
        }
    }

    /**
     * One definition. A value has no parameters and a function has at least one; there is no third kind.
     *
     * @param name   what it is called
     * @param params the parameters, in order, or empty for a value
     * @param body   the right-hand side, already parsed in the vocabulary in scope when it was written
     */
    public record Definition(String name, List<Param> params, Term body) {
        public Definition {
            params = List.copyOf(params);
        }

        public boolean isFunction() {
            return !params.isEmpty();
        }

        /** The left-hand side as it was written — {@code k}, {@code f(x, y)}, or {@code iter(f(), n)}. */
        public String head() {
            if (!isFunction()) {
                return name;
            }
            StringBuilder b = new StringBuilder(name).append('(');
            for (int i = 0; i < params.size(); i++) {
                b.append(i == 0 ? "" : ", ").append(params.get(i).head());
            }
            return b.append(')').toString();
        }

        /** The whole definition as it would be typed, which is how the window lists it. */
        public String source() {
            return head() + " = " + Render.show(body);
        }
    }

    /** Nothing defined — what {@link Parser} and {@link Notation} use when nobody has said otherwise. */
    public static final Bindings EMPTY = new Bindings(Map.of());

    /** Insertion-ordered: the window lists definitions in the order they were made, which is the order typed. */
    private final Map<String, Definition> defs;
    /**
     * The parameters a body is being read in, and empty in every {@link Bindings} a session actually holds.
     *
     * <p>This is the transient scope {@link #read} builds: a parameter is a name the notation must know about
     * for the length of one body and that defines nothing, so it lives beside the definitions rather than being
     * pushed in among them as a definition of itself. Kept as a list because it also carries each parameter's
     * <em>kind</em>, which is what {@link #isFunction} needs to answer while the body is being read.
     */
    private final List<Param> params;
    /** These names and the built-in words as one list, longest first — computed once, scanned per token. */
    private final List<String> vocabulary;

    private Bindings(Map<String, Definition> defs) {
        this(defs, List.of());
    }

    private Bindings(Map<String, Definition> defs, List<Param> params) {
        this.defs = defs;
        this.params = params;
        // A LinkedHashSet because a parameter may be spelled like a definition it shadows, and one word twice
        // in a scan list is a wasted comparison on every token.
        Set<String> words = new java.util.LinkedHashSet<>();
        for (Param p : params) {
            words.add(p.name());
        }
        words.addAll(defs.keySet());
        words.addAll(Notation.builtins());
        this.vocabulary = Notation.vocabularyOf(new ArrayList<>(words));
    }

    /** The scan order {@link Notation} and {@link Parser} both read words with. */
    List<String> vocabulary() {
        return vocabulary;
    }

    /** Every definition, in the order they were made. */
    public List<Definition> all() {
        return List.copyOf(defs.values());
    }

    public boolean isEmpty() {
        return defs.isEmpty();
    }

    /** Every defined name — the vocabulary {@link Notation} scans with. */
    public Set<String> names() {
        return defs.keySet();
    }

    public Definition get(String name) {
        return defs.get(name);
    }

    /**
     * Whether {@code name} is a <em>function</em> here: what makes {@code f(2)} a call and not a product.
     *
     * <p>A parameter is consulted first and answers for itself either way, because a parameter <b>shadows</b> —
     * {@code g(k) = k+1} written in a session where {@code k} is a function must read {@code k} as the value it
     * was handed, and a functor parameter spelled like a defined value must read as a function.
     */
    public boolean isFunction(String name) {
        for (Param p : params) {
            if (p.name().equals(name)) {
                return p.function();
            }
        }
        Definition d = defs.get(name);
        return d != null && d.isFunction();
    }

    /**
     * Read and add {@code line}, which is {@code name = expression}, {@code f(x, y) = expression}, or a
     * functor — {@code iter(f(), n) = f(f(n))}, whose {@code f} is given the name of a function when it is
     * called rather than a value.
     *
     * <p>Redefining a name replaces it in place, keeping its position in the list: correcting {@code k} should
     * not move it to the bottom of the window.
     *
     * @throws SyntaxException on anything that is not a definition, on a reserved name, and on a body that does
     *                         not parse
     */
    public Bindings define(String line) {
        int eq = line.indexOf('=');
        if (eq < 0) {
            throw new SyntaxException("a definition is name = expression");
        }
        String head = line.substring(0, eq).trim();
        String body = line.substring(eq + 1).trim();
        if (body.isEmpty()) {
            throw new SyntaxException("a definition needs a right-hand side");
        }
        List<Param> params = paramsOf(head);
        Definition d = new Definition(nameOf(head), params, read(body, params));
        Map<String, Definition> next = new LinkedHashMap<>(defs);
        next.put(d.name(), d);
        return new Bindings(unmodifiable(next));
    }

    /** Forget {@code name}. Anything that mentioned it goes back to standing unexpanded, which is visible. */
    public Bindings without(String name) {
        if (!defs.containsKey(name)) {
            return this;
        }
        Map<String, Definition> next = new LinkedHashMap<>(defs);
        next.remove(name);
        return new Bindings(unmodifiable(next));
    }

    // ---------------------------------------------------------------- reading a definition

    /** The body, read in this vocabulary plus {@code params} — a parameter shadows whatever it is spelled like. */
    private Term read(String body, List<Param> params) {
        Bindings scope = params.isEmpty() ? this : new Bindings(defs, params);
        return Parser.parse(Notation.normalize(body, scope), scope);
    }

    private static String nameOf(String head) {
        int open = head.indexOf('(');
        return checked((open < 0 ? head : head.substring(0, open)).trim());
    }

    private static List<Param> paramsOf(String head) {
        int open = head.indexOf('(');
        if (open < 0) {
            return List.of();
        }
        if (!head.endsWith(")")) {
            throw new SyntaxException("a parameter list needs its closing bracket");
        }
        String inside = head.substring(open + 1, head.length() - 1).trim();
        if (inside.isEmpty()) {
            throw new SyntaxException("a function with no parameters is a value: write it without the brackets");
        }
        List<Param> params = new ArrayList<>();
        for (String piece : slots(inside)) {
            Param p = param(piece.trim());
            for (Param already : params) {
                if (already.name().equals(p.name())) {
                    throw new SyntaxException("the parameter " + p.name() + " is named twice");
                }
            }
            params.add(p);
        }
        return params;
    }

    /**
     * The comma-separated slots of a parameter list, splitting on the <em>top-level</em> commas only.
     *
     * <p>A functor's own brackets are empty and so hold no comma, which means a plain split would still work
     * today. It is written this way so that a list that does hold one — someone writing {@code iter(f(x), n)},
     * which is the obvious guess — arrives at {@link #param} whole and gets told what is actually wrong with it,
     * instead of being torn in half first and then refused for a missing bracket.
     */
    private static List<String> slots(String inside) {
        List<String> out = new ArrayList<>();
        int depth = 0;
        int from = 0;
        for (int i = 0; i < inside.length(); i++) {
            char c = inside.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                out.add(inside.substring(from, i));
                from = i + 1;
            }
        }
        out.add(inside.substring(from));
        return out;
    }

    /** One slot of a parameter list: {@code n} is a value, {@code f()} is a function. */
    private static Param param(String slot) {
        int open = slot.indexOf('(');
        if (open < 0) {
            return new Param(checked(slot), false);
        }
        String name = slot.substring(0, open).trim();
        if (!slot.endsWith(")")) {
            throw new SyntaxException("the parameter " + name + " is missing its closing bracket");
        }
        if (!slot.substring(open + 1, slot.length() - 1).isBlank()) {
            throw new SyntaxException("a function parameter is written " + name + "(), with nothing between the "
                    + "brackets -- how many arguments it takes is settled by the function that is passed to it");
        }
        return new Param(checked(name), true);
    }

    /** A name has to be a name: a letter, then letters or digits, and not one the theory or the catalogue owns. */
    private static String checked(String name) {
        if (name.isEmpty()) {
            throw new SyntaxException("a definition needs a name");
        }
        if (!Character.isLetter(name.charAt(0))) {
            throw new SyntaxException("a name starts with a letter, so " + name + " is not one");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i > 0 && !Character.isLetterOrDigit(c)) {
                throw new SyntaxException("'" + c + "' cannot be part of a name");
            }
            // A typed w IS ω -- that is how a keyboard with no ω key reaches the point (Notation.normalize).
            // So a name with a w in it could be defined and then never typed again, which is worse than being
            // refused: the refusal is one message now, the other is a name that silently does not exist.
            if (c == 'w') {
                throw new SyntaxException("a w is read as ω here, so a name cannot contain one");
            }
        }
        if (RESERVED.contains(name) || Real.of(name) != null) {
            throw new SyntaxException(name + " already means something here");
        }
        return name;
    }

    private static Map<String, Definition> unmodifiable(Map<String, Definition> m) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(m));
    }

    // ---------------------------------------------------------------- expansion

    /**
     * Put every definition into {@code t}. What comes out mentions no defined name, so what reaches {@link Cott}
     * is the expression the definitions <em>stand for</em> — and so is the term the plotter reads its variables
     * out of, which is why plotting {@code f(x)} draws f's body rather than an opaque symbol.
     *
     * @throws SyntaxException where a definition expands into itself, or a function is given the wrong count
     */
    public Term expand(Term t) {
        if (defs.isEmpty()) {
            return t;
        }
        return t instanceof Val v ? expand(v, Map.of(), 0) : expand((Exp) t, Map.of(), 0);
    }

    private Val expand(Val v, Map<String, Val> args, int depth) {
        if (depth > LIMIT) {
            throw new SyntaxException("a definition here expands into itself");
        }
        return switch (v) {
            case Pt p -> p;
            case Atom a -> {
                Val given = args.get(a.name());
                if (given != null) {
                    yield given;                    // a parameter, already expanded by the caller
                }
                Definition d = defs.get(a.name());
                // A FUNCTION named without brackets is not a value, so it stands: f alone is the name f, and
                // only f(...) is its body. Expanding it here would put a loose parameter into the answer.
                yield d == null || d.isFunction() || !(d.body() instanceof Val body)
                        ? a
                        : expand(body, Map.of(), depth + 1);
            }
            case Neg n -> new Neg(expand(n.of(), args, depth));
            case Inv i -> new Inv(expand(i.of(), args, depth));
            case Div d -> new Div(expand(d.of(), args, depth), expand(d.by(), args, depth));
            case Pow p -> new Pow(expand(p.base(), args, depth), expand(p.exponent(), args, depth));
            case Wind w -> new Wind(expand(w.of(), args, depth));
            case AWind w -> new AWind(expand(w.of(), args, depth));
            case Approx a -> new Approx(expand(a.of(), args, depth));
            case Plus p -> new Plus(expandAll(p.args(), args, depth));
            case Times t -> new Times(expandAll(t.args(), args, depth));
            case Call c -> call(c, args, depth);
        };
    }

    private Val call(Call c, Map<String, Val> args, int depth) {
        List<Val> given = expandAll(c.args(), args, depth);
        Val passed = args.get(c.name());
        if (passed != null) {
            // A PARAMETER standing in call position, which is the whole of the functor mechanism: what the
            // caller bound it to names the function to apply here.
            return applyPassed(c.name(), passed, given, depth);
        }
        Definition d = defs.get(c.name());
        if (d == null || !d.isFunction() || !(d.body() instanceof Val body)) {
            return new Call(c.name(), given);   // a built-in, or a name nothing defines: it stands
        }
        return apply(d, body, given, depth);
    }

    /**
     * Apply what a functor parameter was bound to. That it names a function at all was settled in
     * {@link #checkKind} when it was bound, so the only thing left is which kind of function it names — one
     * this session defined, or one of {@link Real}'s.
     */
    private Val applyPassed(String param, Val passed, List<Val> given, int depth) {
        String named = functionNamed(passed);
        if (named == null) {
            // Unreachable through define/expand, since the kind is checked at the binding. Kept because a
            // silently wrong answer here would be a function quietly turning into a product.
            throw new SyntaxException(param + " is applied to arguments here, so it has to be given a function");
        }
        Definition d = defs.get(named);
        if (d != null) {
            return d.body() instanceof Val body ? apply(d, body, given, depth) : new Call(named, given);
        }
        Real fn = Real.of(named);
        if (given.size() != fn.arity()) {
            throw new SyntaxException(fn.label() + " takes " + fn.arity()
                    + (fn.arity() == 1 ? " argument" : " arguments") + ", not " + given.size());
        }
        return new Call(fn.label(), given);
    }

    /** {@code d}'s body with {@code given} put in. The count and the kinds are checked here, at the binding. */
    private Val apply(Definition d, Val body, List<Val> given, int depth) {
        if (given.size() != d.params().size()) {
            throw new SyntaxException(d.name() + " takes " + d.params().size()
                    + (d.params().size() == 1 ? " argument" : " arguments") + ", not " + given.size());
        }
        Map<String, Val> bound = new LinkedHashMap<>();
        for (int i = 0; i < given.size(); i++) {
            Param p = d.params().get(i);
            checkKind(d, p, given.get(i));
            bound.put(p.name(), given.get(i));
        }
        return expand(body, bound, depth + 1);
    }

    /**
     * A parameter's <em>kind</em>, checked where it is bound rather than where it is used.
     *
     * <p>Here, because this is the one place both halves of the promise are in hand — the definition says which
     * kind it wanted and the argument is sitting there expanded. Checked in both directions, because both are
     * mistakes somebody will make: a functor given an expression cannot be applied, and a value parameter given
     * a bare function name would carry that name into the answer as though it were a variable, and then get
     * plotted as one.
     */
    private void checkKind(Definition d, Param p, Val given) {
        String named = functionNamed(given);
        if (p.function() && named == null) {
            throw new SyntaxException(d.name() + "'s " + p.name() + " is a function, so it is given the NAME of "
                    + "one -- a defined function, or one of the built-in ones like sin");
        }
        if (!p.function() && named != null) {
            throw new SyntaxException(named + " is a function and needs its arguments in brackets, since "
                    + d.name() + "'s " + p.name() + " is a value and not a function");
        }
    }

    /**
     * The function a value <em>names</em>, if it names one, and null otherwise.
     *
     * <p>A bare name is an {@link Atom} and always has been — {@link Parser} takes one in an argument slot and
     * {@link #expand} leaves a function's name standing — so passing a function costs the term language nothing
     * at all. What decides whether such an atom is a function is this lookup, made against whatever is defined
     * at the moment of the call, exactly as every other name here is.
     */
    private String functionNamed(Val v) {
        if (!(v instanceof Atom a)) {
            return null;
        }
        Definition d = defs.get(a.name());
        if (d != null) {
            return d.isFunction() ? a.name() : null;
        }
        return Real.of(a.name()) != null ? a.name() : null;
    }

    private List<Val> expandAll(List<Val> vs, Map<String, Val> args, int depth) {
        List<Val> out = new ArrayList<>(vs.size());
        for (Val v : vs) {
            out.add(expand(v, args, depth));
        }
        return out;
    }

    private Exp expand(Exp e, Map<String, Val> args, int depth) {
        return switch (e) {
            case Xp x -> x;
            case Lg l -> new Lg(expand(l.of(), args, depth));
            case Logb l -> new Logb(expand(l.base(), args, depth), expand(l.of(), args, depth));
        };
    }
}
