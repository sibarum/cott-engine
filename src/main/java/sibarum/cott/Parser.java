package sibarum.cott;

import sibarum.cott.Term.Atom;
import sibarum.cott.Term.Call;
import sibarum.cott.Term.Div;
import sibarum.cott.Term.Logb;
import sibarum.cott.Term.Neg;
import sibarum.cott.Term.Plus;
import sibarum.cott.Term.Pow;
import sibarum.cott.Term.Times;
import sibarum.cott.Term.Val;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A display expression to a term. Precedence: {@code ^} over {@code · ÷} over {@code + −}, with
 * {@code ^} associating to the right.
 *
 * <p>The input is expected to have been through {@link Notation#normalize}, so it holds the display
 * glyphs and carries an explicit {@link Notation#TIMES} wherever juxtaposition meant one. It must have been
 * normalized in the <em>same</em> {@link Bindings} this reads in, since that is what decided which runs of
 * letters are words and which are juxtaposed variables.
 *
 * <p>The grammar is written over {@link Term} rather than {@link Val} because {@code log} returns an
 * <em>exponent</em>, which is a different sort. An exponent cannot be an operand of the arithmetic,
 * so it is only ever a whole expression, and {@link #val} is where that is enforced — with a message
 * saying so, rather than the bare "Error" an ill-sorted term used to earn from the rewriting engine.
 * {@link Real}'s functions are the other kind of call and have no such restriction: they are values, and
 * {@code 2+sin(x)} is an ordinary sum.
 */
public final class Parser {

    private final String s;
    private final Bindings session;
    private int p;

    private Parser(String s, Bindings session) {
        this.s = s;
        this.session = session;
    }

    /** Parse a normalized display expression. Throws {@link SyntaxException} on anything else. */
    public static Term parse(String normalized) {
        return parse(normalized, Bindings.EMPTY);
    }

    /** As {@link #parse(String)}, reading {@code session}'s names as names rather than as juxtaposition. */
    public static Term parse(String normalized, Bindings session) {
        Parser parser = new Parser(normalized, session);
        Term t = parser.expr();
        if (parser.p < normalized.length()) {
            throw new SyntaxException("Error");
        }
        return t;
    }

    private Term expr() {
        Term a = term();
        while (p < s.length() && (peek() == '+' || peek() == '−')) {
            char op = next();
            Val b = val(term());
            // Subtraction is addition of a negation, and the neg is KEPT: plus(A, neg(A)) is the
            // additive residue form, so folding it away would destroy the form before it is seen.
            a = new Plus(List.of(val(a), op == '+' ? b : new Neg(b)));
        }
        return a;
    }

    private Term term() {
        Term a = factor();
        while (p < s.length() && (peek() == Notation.TIMES || peek() == '÷')) {
            char op = next();
            Val b = val(factor());
            // Division is primitive, not times-by-inverse: on equal arguments it is the
            // multiplicative residue and has to keep which argument it came from.
            a = op == Notation.TIMES ? new Times(List.of(val(a), b)) : new Div(val(a), b);
        }
        return a;
    }

    private Term factor() {
        Term a = primary();
        if (p < s.length() && peek() == '^') {
            next();
            a = new Pow(val(a), val(factor()));   // right-associative: 2^3^2 is 2^(3^2)
        }
        return a;
    }

    private Term primary() {
        if (p >= s.length()) {
            throw new SyntaxException("Error");
        }
        // A WORD first, because the vocabulary is what decides where one begins: the adjacency pass has already
        // put a sign between anything it did not recognise, so a run of letters standing here is a word.
        String word = Notation.wordAt(s, p, session);
        if (word != null) {
            p += word.length();
            Real fn = Real.of(word);
            if (fn != null) {
                return new Call(fn.label(), arguments(word, fn.arity()));
            }
            if (word.equals(Notation.LOG)) {
                return logCall();
            }
            if (session.isFunction(word)) {
                return new Call(word, arguments(word, -1));
            }
            return new Atom(word);
        }
        char c = peek();
        if (c == '−') {
            next();
            return new Neg(val(primary()));
        }
        if (c == '(') {
            next();
            Term e = expr();
            if (p >= s.length() || next() != ')') {
                throw new SyntaxException("Error");
            }
            return e;
        }
        Val named = switch (c) {
            case 'ω' -> Term.OMEGA;
            case 'i' -> Term.IU;
            // π and e have no base-0 exponential form and are not derivable here, so they are atoms
            case 'π' -> new Atom("π");
            case 'e' -> new Atom("e");
            case 'x', 'y', 'z' -> new Atom(String.valueOf(c));
            default -> null;
        };
        if (named != null) {
            next();
            return named;
        }
        if (Notation.numeral(c)) {
            return number();
        }
        throw new SyntaxException("'" + c + "' not in COTT");
    }

    /**
     * The bracketed argument list of a call. {@code arity} of −1 accepts whatever is written, which is what a
     * defined function needs — {@link Bindings} checks the count against the definition, where the number that
     * was promised actually lives.
     *
     * <p>An arity of −1 is also what says this is a <em>defined</em> function, and so the one kind of call whose
     * arguments may be function names rather than values. {@link Real}'s take values and only values, and the
     * distinction is worth keeping: {@code sin(g)} is a mistake and stays one.
     */
    private List<Val> arguments(String name, int arity) {
        if (p >= s.length() || next() != '(') {
            throw new SyntaxException(name + " needs its argument in brackets");
        }
        boolean functors = arity < 0;
        List<Val> args = new ArrayList<>();
        args.add(argument(functors));
        while (p < s.length() && peek() == ',') {
            next();
            args.add(argument(functors));
        }
        if (p >= s.length() || next() != ')') {
            throw new SyntaxException(name + " is missing its closing bracket");
        }
        if (arity >= 0 && args.size() != arity) {
            throw new SyntaxException(name + " takes " + arity + (arity == 1 ? " argument" : " arguments")
                    + ", not " + args.size());
        }
        return args;
    }

    /**
     * One argument. Ordinarily an expression — but a defined function's argument may also be the bare
     * <b>name</b> of another function, which is how a functor is handed the function it works on:
     * {@code iter(g, 3)}, {@code iter(sin, x)}.
     *
     * <p>Taken only where the name <em>fills the slot</em>, ending at the comma or the closing bracket. That is
     * what keeps this from loosening the rule everywhere else: {@code f} written where {@code f(2)} was meant is
     * still the missing-brackets error it has always been, and {@code iter(g(1), 3)} is still the ordinary call
     * to g it looks like. A name passed this way is an {@link Atom}, which is what a bare name has always parsed
     * to — no new kind of term, and {@link Bindings#expand} settles what it means when it is applied.
     */
    private Val argument(boolean functors) {
        if (functors) {
            String word = Notation.wordAt(s, p, session);
            // log is deliberately absent: it returns an exponent rather than a value, so there is no call it
            // could be put in the place of.
            if (word != null && (session.isFunction(word) || Real.of(word) != null)) {
                int after = p + word.length();
                if (after >= s.length() || s.charAt(after) == ',' || s.charAt(after) == ')') {
                    p = after;
                    return new Atom(word);
                }
            }
        }
        return val(expr());
    }

    /** {@code log(x, b)} — the log of x to base b, which is an exponent. The word is already behind us. */
    private Term logCall() {
        if (p >= s.length() || next() != '(') {
            throw new SyntaxException("log needs its argument in brackets");
        }
        Val of = val(expr());
        if (p >= s.length() || next() != ',') {
            throw new SyntaxException("log needs a base: log(x, b) is the log OF x, TO base b");
        }
        Val base = val(expr());
        if (p >= s.length() || next() != ')') {
            throw new SyntaxException("log is missing its closing bracket");
        }
        return new Logb(base, of);
    }

    /**
     * An operand position: a point goes through, an exponent is a sort error and says so.
     *
     * <p>The only thing that produces an exponent is {@code log}, and the message names it rather than the
     * position, because "an exponent may not be added" is only useful to somebody who already knew that
     * COTT's {@code log} is not the logarithm.
     */
    private static Val val(Term t) {
        if (t instanceof Val v) {
            return v;
        }
        throw new SyntaxException("log here returns an exponent, not a value -- it cannot be added to or "
                + "multiplied by anything, only stand alone");
    }

    /**
     * A numeral, decimals included, as an exact multiplicity — {@code 2.5} is 5/2, not a float.
     *
     * <p>{@code 0} is the POINT zero, which is 0^1, and emphatically not a multiplicity of zero:
     * zero copies of 1 is the additive residue and has nowhere to record the operand it came from.
     * Reading a typed {@code 0} as that multiplicity is a real bug that has been made before.
     */
    private Term number() {
        StringBuilder d = new StringBuilder();
        while (p < s.length() && Notation.numeral(peek())) {
            d.append(next());
        }
        String text = d.toString();
        if (text.equals("0")) {
            return Term.ZERO;
        }
        int dot = text.indexOf('.');
        if (dot < 0) {
            return Term.number(Rational.parse(text));
        }
        String digits = text.replace(".", "");
        if (digits.isEmpty() || text.indexOf('.', dot + 1) >= 0) {
            throw new SyntaxException("Error");
        }
        BigInteger scale = BigInteger.TEN.pow(text.length() - dot - 1);
        return Term.number(Rational.of(new BigInteger(digits), scale));
    }

    private char peek() {
        return s.charAt(p);
    }

    private char next() {
        return s.charAt(p++);
    }
}
