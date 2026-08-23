package sibarum.cott;

import sibarum.cott.Term.Atom;
import sibarum.cott.Term.Div;
import sibarum.cott.Term.Logb;
import sibarum.cott.Term.Neg;
import sibarum.cott.Term.Plus;
import sibarum.cott.Term.Pow;
import sibarum.cott.Term.Times;
import sibarum.cott.Term.Val;

import java.math.BigInteger;
import java.util.List;

/**
 * A display expression to a term. Precedence: {@code ^} over {@code × ÷} over {@code + −}, with
 * {@code ^} associating to the right.
 *
 * <p>The input is expected to have been through {@link Notation#normalize}, so it holds the display
 * glyphs and carries an explicit × wherever juxtaposition meant one.
 *
 * <p>The grammar is written over {@link Term} rather than {@link Val} because {@code log} returns an
 * <em>exponent</em>, which is a different sort. An exponent cannot be an operand of the arithmetic,
 * so it is only ever a whole expression, and {@link #val} is where that is enforced — with a message
 * saying so, rather than the bare "Error" an ill-sorted term used to earn from the rewriting engine.
 */
public final class Parser {

    private final String s;
    private int p;

    private Parser(String s) {
        this.s = s;
    }

    /** Parse a normalized display expression. Throws {@link SyntaxException} on anything else. */
    public static Term parse(String normalized) {
        Parser parser = new Parser(normalized);
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
        while (p < s.length() && (peek() == '×' || peek() == '÷')) {
            char op = next();
            Val b = val(factor());
            // Division is primitive, not times-by-inverse: on equal arguments it is the
            // multiplicative residue and has to keep which argument it came from.
            a = op == '×' ? new Times(List.of(val(a), b)) : new Div(val(a), b);
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
        if (c == 'l') {
            return logCall();
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

    /** {@code log(x, b)} — the log of x to base b, which is an exponent. */
    private Term logCall() {
        expect("log");
        if (p >= s.length() || next() != '(') {
            throw new SyntaxException("Error");
        }
        Val of = val(expr());
        if (p >= s.length() || next() != ',') {
            throw new SyntaxException("log needs a base");
        }
        Val base = val(expr());
        if (p >= s.length() || next() != ')') {
            throw new SyntaxException("Error");
        }
        return new Logb(base, of);
    }

    /** An operand position: a point goes through, an exponent is a sort error and says so. */
    private static Val val(Term t) {
        if (t instanceof Val v) {
            return v;
        }
        throw new SyntaxException("log is an exponent, not a value");
    }

    private void expect(String word) {
        if (!s.startsWith(word, p)) {
            throw new SyntaxException("Error");
        }
        p += word.length();
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
