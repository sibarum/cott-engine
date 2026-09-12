package sibarum.cott;

import sibarum.cott.engine.base.expr.AtomExpr;
import sibarum.cott.engine.base.expr.CallExpr;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.ExponentialOperationExpr;
import sibarum.cott.engine.operation.binary.LogarithmOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;

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
 * <h2>Nodes are built, never combined</h2>
 * Every node here is constructed directly rather than through {@link IExpr#plus} and its neighbours, and that
 * is load-bearing. Those defaults combine where they can: {@code ONE.plus(ONE)} is the literal 2, so a parser
 * that used them would fold {@code 1+1} on the way in and hand simplification a term that had already lost its
 * shape. A degenerate cell is recognised by the term and not by the value — {@code 1·1} is the value 1 while
 * {@code 1·(1÷1)} is an erasure — so canonicalising here would destroy exactly what the rules need to see.
 *
 * <p>The grammar has no sort of its own for exponents, but {@code log} still returns one, and an exponent is
 * not an operand of the arithmetic. {@link #val} is where that is enforced — with a message saying so, rather
 * than the bare "Error" an ill-sorted term used to earn from the rewriting engine. {@link Real}'s functions are
 * the other kind of call and have no such restriction: they are values, and {@code 2+sin(x)} is an ordinary sum.
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
    public static IExpr parse(String normalized) {
        return parse(normalized, Bindings.EMPTY);
    }

    /** As {@link #parse(String)}, reading {@code session}'s names as names rather than as juxtaposition. */
    public static IExpr parse(String normalized, Bindings session) {
        Parser parser = new Parser(normalized, session);
        IExpr t = parser.expr();
        if (parser.p < normalized.length()) {
            throw new SyntaxException("Error");
        }
        return t;
    }

    private IExpr expr() {
        IExpr a = term();
        while (p < s.length() && (peek() == '+' || peek() == '−')) {
            char op = next();
            IExpr b = val(term());
            // Subtraction is addition of a negation, and the neg is KEPT: plus(A, neg(A)) is the
            // additive residue form, so folding it away would destroy the form before it is seen.
            a = new AdditionOperationExpr(val(a), op == '+' ? b : new NegationOperationExpr(b));
        }
        return a;
    }

    private IExpr term() {
        IExpr a = factor();
        while (p < s.length() && (peek() == Notation.TIMES || peek() == '÷')) {
            char op = next();
            IExpr b = val(factor());
            // Division is a product with a reciprocal, which is how the carrier spells it, and the reciprocal
            // is KEPT for the same reason the negation above is: y·(1÷y) is the multiplicative residue, and
            // which operand it came from is the whole of what makes it reversible.
            a = new MultiplicationOperationExpr(val(a), op == Notation.TIMES ? b : new ReciprocalOperationExpr(b));
        }
        return a;
    }

    private IExpr factor() {
        IExpr a = primary();
        if (p < s.length() && peek() == '^') {
            next();
            a = new ExponentialOperationExpr(val(a), val(factor()));   // right-associative: 2^3^2 is 2^(3^2)
        }
        return a;
    }

    private IExpr primary() {
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
                return new CallExpr(fn.label(), arguments(word, fn.arity()));
            }
            if (word.equals(Notation.LOG)) {
                return logCall();
            }
            if (session.isFunction(word)) {
                return new CallExpr(word, arguments(word, -1));
            }
            return new AtomExpr(word);
        }
        char c = peek();
        if (c == '−') {
            next();
            return new NegationOperationExpr(val(primary()));
        }
        if (c == '(') {
            next();
            IExpr e = expr();
            if (p >= s.length() || next() != ')') {
                throw new SyntaxException("Error");
            }
            return e;
        }
        IExpr named = switch (c) {
            case 'ω' -> TractionLiteral.OMEGA;
            // i is 0^(ω÷2), which this carrier CAN hold apart -- ω÷2 is the pair ((1,2), -1) and omega is (1, -1).
            // What it cannot do is close the square: ω÷2 + ω÷2 is ((2,4), -1), and (2,4) is one at coordinates that
            // are not one, so 0^(ω÷2)² lands beside 0^ω rather than on it. It needs uniqueness of roots, so i stands.
            case 'i' -> new AtomExpr("i");
            // π and e have no base-0 exponential form and are not derivable here, so they are atoms
            case 'π' -> new AtomExpr("π");
            case 'e' -> new AtomExpr("e");
            default -> null;
        };
        if (named != null) {
            next();
            return named;
        }
        if (Notation.numeral(c)) {
            return number();
        }
        // Any other letter is a variable. Not a list of three, because nothing below this layer ever cared
        // which letter it was -- an atom is a leaf, no rule matches one, and the engine has always carried
        // whatever name it was given. The four above are reserved and are reached first, so a variable can
        // never be one of them.
        //
        // Single letters only, and that is the same rule as before rather than a limitation of it: xy is
        // x·y, so a run of letters cannot be read as one name by default. What breaks that tie is a
        // vocabulary, which is what Bindings is for -- see Notation.
        if (Notation.variable(c)) {
            next();
            return new AtomExpr(String.valueOf(c));
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
    private List<IExpr> arguments(String name, int arity) {
        if (p >= s.length() || next() != '(') {
            throw new SyntaxException(name + " needs its argument in brackets");
        }
        boolean functors = arity < 0;
        List<IExpr> args = new ArrayList<>();
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
     * to g it looks like. A name passed this way is an {@link AtomExpr}, which is what a bare name has always
     * parsed to — no new kind of term, and {@link Bindings#expand} settles what it means when it is applied.
     */
    private IExpr argument(boolean functors) {
        if (functors) {
            String word = Notation.wordAt(s, p, session);
            // log is deliberately absent: it returns an exponent rather than a value, so there is no call it
            // could be put in the place of.
            if (word != null && (session.isFunction(word) || Real.of(word) != null)) {
                int after = p + word.length();
                if (after >= s.length() || s.charAt(after) == ',' || s.charAt(after) == ')') {
                    p = after;
                    return new AtomExpr(word);
                }
            }
        }
        return val(expr());
    }

    /** {@code log(x, b)} — the log of x to base b, which is an exponent. The word is already behind us. */
    private IExpr logCall() {
        if (p >= s.length() || next() != '(') {
            throw new SyntaxException("log needs its argument in brackets");
        }
        IExpr of = val(expr());
        if (p >= s.length() || next() != ',') {
            throw new SyntaxException("log needs a base: log(x, b) is the log OF x, TO base b");
        }
        IExpr base = val(expr());
        if (p >= s.length() || next() != ')') {
            throw new SyntaxException("log is missing its closing bracket");
        }
        return new LogarithmOperationExpr(base, of);
    }

    /**
     * An operand position: a value goes through, an exponent is a sort error and says so.
     *
     * <p>The only thing that produces an exponent is {@code log}, and the message names it rather than the
     * position, because "an exponent may not be added" is only useful to somebody who already knew that
     * COTT's {@code log} is not the logarithm.
     */
    private static IExpr val(IExpr t) {
        if (!(t instanceof LogarithmOperationExpr)) {
            return t;
        }
        throw new SyntaxException("log here returns an exponent, not a value -- it cannot be added to or "
                + "multiplied by anything, only stand alone");
    }

    /**
     * A numeral, decimals included, as exact coordinates — {@code 2.5} is the pair (25, 10), not a float.
     *
     * <p>The coordinates are the ones the numeral was written at, since nothing here reduces: {@code 2.5} is
     * (25, 10) and stays there, which is also what makes it print back as {@code 2.5}.
     *
     * <p>{@code 0} is the point zero, the pair (0, 1), and emphatically not "zero copies of something": zero
     * copies is the additive erasure and has nowhere to record the operand it came from. Reading a typed
     * {@code 0} as that is a real bug that has been made before.
     */
    private IExpr number() {
        StringBuilder d = new StringBuilder();
        while (p < s.length() && Notation.numeral(peek())) {
            d.append(next());
        }
        String text = d.toString();
        int dot = text.indexOf('.');
        if (dot < 0) {
            return new RationalLiteral(new BigInteger(text), BigInteger.ONE);
        }
        String digits = text.replace(".", "");
        if (digits.isEmpty() || text.indexOf('.', dot + 1) >= 0) {
            throw new SyntaxException("Error");
        }
        BigInteger scale = BigInteger.TEN.pow(text.length() - dot - 1);
        return new RationalLiteral(new BigInteger(digits), scale);
    }

    private char peek() {
        return s.charAt(p);
    }

    private char next() {
        return s.charAt(p++);
    }
}
