package sibarum.cott;

import sibarum.cott.Term.AWind;
import sibarum.cott.Term.Approx;
import sibarum.cott.Term.Atom;
import sibarum.cott.Term.Div;
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

import java.util.List;
import java.util.regex.Pattern;

/**
 * Terms back into the notation they were typed in.
 *
 * <p>The requirement is that this is the <em>inverse</em> of {@link Parser}: whatever appears in the
 * display has to be something the display can read again, because the result of one evaluation is
 * the entry for the next. Two things follow. Brackets come from the term's shape and never from
 * scanning the rendered string — with the sign dropped, {@code 2ω} is a product that no longer looks
 * like one, and a scanner would call it an atom and then print {@code (2ω)^ω} as {@code 2ω^ω}. And
 * the sign is dropped exactly where {@link Notation#implied} will put it back.
 */
public final class Render {

    private Render() {
    }

    // How tightly a rendered form binds, mirroring the parser: ^ takes a primary on both sides,
    // · and ÷ take factors, + takes terms.
    private static final int ADD = 1;
    private static final int MUL = 2;
    private static final int POW = 3;
    private static final int ATOM = 4;

    /** An exponent the {@code ^} slot can hold whole: one signed integer, or the twist unit. */
    private static final Pattern EXP_ATOM = Pattern.compile("-?(?:\\d+|ω)");

    public static String show(Term t) {
        return switch (t) {
            case Xp x -> exponent(x);
            // lg and logb return an EXPONENT, so they render as one: log of -1 to base 0 is ω.
            case Lg l -> "log(" + show(l.of()) + ", 0)";
            case Logb l -> "log(" + show(l.of()) + ", " + show(l.base()) + ")";
            case Pt p -> point(p);
            case Atom a -> a.name();
            case Wind w -> "1^" + arg(w.of(), ATOM);
            case AWind w -> "0^" + arg(w.of(), ATOM);
            case Neg n -> "-" + arg(n.of(), ATOM);
            case Inv i -> "1÷" + arg(i.of(), ATOM);
            case Pow p -> arg(p.base(), ATOM) + "^" + arg(p.exponent(), ATOM);
            case Approx a -> "≈" + arg(a.of(), ATOM);
            case Div d -> arg(d.of(), POW) + "÷" + arg(d.by(), POW);
            case Times x -> product(x.args());
            case Plus x -> sum(x.args());
        };
    }

    /**
     * {@code k} copies of 0^E. A unit exponent makes it the plain number k; a multiplicity of one
     * makes it the bare point, named if it has a name.
     */
    private static String point(Pt p) {
        if (p.exp().isUnit()) {
            return rational(p.mult());   // k copies of 1 IS the number k
        }
        String named = name(p.exp());
        String body = named != null ? named : "0^" + exponentAtom(p.exp());
        if (p.mult().isOne()) {
            return body;
        }
        if (p.mult().equals(MINUS_ONE)) {
            return "-" + body;
        }
        // 2ω, but 2·0^2: a digit may not lead. A FRACTIONAL coefficient is bracketed, both because
        // 1÷2ω reads as a quotient of the product, and because a bare exponent — which is what a
        // logarithm returns — renders as 1÷2ω itself, and two objects must not print alike.
        return juxtapose(coefficient(p.mult()), body);
    }

    private static final Rational MINUS_ONE = Rational.of(-1);

    /** A multiplicity in front of a point: bracketed when it is a fraction, bare when it is whole. */
    private static String coefficient(Rational mult) {
        return mult.isInteger() ? mult.toString() : "(" + rational(mult) + ")";
    }

    /** The points that have names; null for everything else. */
    static String name(Xp e) {
        if (!e.torsion().isZero()) {
            return null;   // a root of the residue zero has no classical name
        }
        if (e.twist().isZero()) {
            if (e.grade().isZero()) {
                return "1";
            }
            if (e.grade().isOne()) {
                return "0";
            }
            return e.grade().equals(MINUS_ONE) ? "ω" : null;
        }
        if (e.grade().isZero()) {
            if (e.twist().isOne()) {
                return "-1";
            }
            return e.twist().equals(Rational.of(1, 2)) ? "i" : null;
        }
        return null;
    }

    static String name(Pt p) {
        return name(p.exp());
    }

    /** The exponent g + tω + the torsion, with zero parts and unit coefficients dropped. */
    private static String exponent(Xp e) {
        StringBuilder s = new StringBuilder();
        if (!e.grade().isZero()) {
            s.append(rational(e.grade()));
        }
        if (!e.twist().isZero()) {
            plus(s).append(e.twist().isOne() ? "ω" : rational(e.twist()) + "ω");
        }
        if (!e.torsion().isZero()) {
            // torsion 1/d is the d-th root of the residue zero, written 0/d
            plus(s).append("0÷").append(e.torsion().denominator());
        }
        return s.isEmpty() ? "0" : s.toString();
    }

    /**
     * An exponent in the {@code ^} slot, bracketed only where it has to be. {@code ^} takes a single
     * primary on its right, so {@code 0^2} and {@code 0^-2} read back exactly as printed, while
     * {@code 0^(1+ω)} and {@code 0^(1÷2)} would lose everything after the first token.
     */
    private static String exponentAtom(Xp e) {
        String s = exponent(e);
        return EXP_ATOM.matcher(s).matches() ? s : "(" + s + ")";
    }

    private static StringBuilder plus(StringBuilder s) {
        if (!s.isEmpty()) {
            s.append('+');
        }
        return s;
    }

    /** A rational in the display's own language: 5/2 prints as 5÷2, which is what the keypad types. */
    private static String rational(Rational r) {
        return r.toString().replace("/", "÷");
    }

    // ---------------------------------------------------------------- the operators

    /**
     * A sum. A negative term is joined with a minus rather than a plus-minus pair, which is both how
     * it would be typed and how it reads back — {@code 2-1} parses to the same sum it came from.
     */
    private static String sum(List<Val> args) {
        StringBuilder out = new StringBuilder();
        for (Val a : args) {
            String piece = arg(a, MUL);
            if (out.isEmpty()) {
                out.append(piece);
            } else if (piece.startsWith("-")) {
                out.append(piece);   // the sign is already the operator
            } else {
                out.append('+').append(piece);
            }
        }
        return out.toString();
    }

    /**
     * A product, rendered the way it would be typed: the sign dropped wherever juxtaposition alone
     * already means multiplication. The factors arrive in canonical order from the evaluator, which
     * is what puts the coefficient at the front rather than buried in the middle.
     */
    private static String product(List<Val> args) {
        String out = "";
        for (Val f : args) {
            // A product inside a product needs no brackets — times is associative and juxtaposition
            // binds exactly as · does. A QUOTIENT does: a·x÷y reads as (a·x)÷y, and div is primitive
            // here, so dropping the brackets would move which pair the residue belongs to.
            String piece = arg(f, quotient(f) ? POW : MUL);
            out = out.isEmpty() ? piece : juxtapose(out, piece);
        }
        return out;
    }

    /** Whether a factor renders as a division, and so has to keep its brackets inside a product. */
    private static boolean quotient(Val v) {
        return switch (v) {
            case Div d -> true;
            case Inv i -> true;
            // A fractional multiplicity is a bare quotient only when there is no point behind it:
            // 5/2 alone renders as 5÷2, while 5/2 copies of ω render as (5÷2)ω, bracketing itself.
            case Pt p -> p.exp().isUnit() && !p.mult().isInteger();
            default -> false;
        };
    }

    /** Two rendered factors, with a sign only where juxtaposition would not read back as a product. */
    private static String juxtapose(String left, String right) {
        return Notation.implied(left.charAt(left.length() - 1), right.charAt(0))
                ? left + right
                : left + Notation.TIMES + right;
    }

    /** Render for a position that needs at least {@code min} binding tightness. */
    private static String arg(Term t, int min) {
        String s = show(t);
        return precedence(t) < min ? "(" + s + ")" : s;
    }

    /** How tightly a term's rendering binds. Read off the TERM, never off the rendered string. */
    private static int precedence(Term t) {
        return switch (t) {
            case Plus p -> ADD;
            case Times x -> MUL;
            case Div d -> MUL;
            // A pt renders as a bare number, a name, 0^(...), or a coefficient times one of those.
            case Pt p -> {
                if (p.exp().isUnit()) {
                    // the plain number k: an atom, unless it is a fraction, which is a quotient
                    yield p.mult().isInteger() ? ATOM : MUL;
                }
                boolean bare = p.mult().isOne() || p.mult().equals(MINUS_ONE);
                yield bare ? (name(p.exp()) != null ? ATOM : POW) : MUL;
            }
            case Xp x -> exponent(x).indexOf('+') >= 0 ? ADD : ATOM;
            case Atom a -> ATOM;
            case Lg l -> ATOM;      // a call is parenthesised, so it binds as tightly as a name
            case Logb l -> ATOM;
            default -> POW;         // neg, inv, pow, wind, awind, approx
        };
    }
}
