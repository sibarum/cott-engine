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
import sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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
 *
 * <h2>What this printer does not do</h2>
 * It prints the term it is given and applies no rule to it. {@code 0^0} prints as {@code 0^0} and not as
 * {@code 1}, because {@code 0^0 = 1} is E5 and belongs to simplification. The previous printer named such
 * points itself, which it could do because the carrier it printed was already a normal form; this one prints a
 * term that may not be reduced at all, so naming would be the printer answering a question the engine had not
 * been asked. The four points still print as their names where the CARRIER holds them that way — {@code ω} is
 * the coordinate pair (1, 0) and prints as {@code ω}, which is a spelling and not a rule.
 *
 * <h2>Coordinates do not reduce, so a spelling may not either</h2>
 * {@code (1, 2)} and {@code (5, 10)} are the same value at different coordinates and are different literals.
 * A printer that showed the first as {@code 0.5} would be handing back something that reads as the second, so
 * the decimal spelling is used only where it names the same pair — see {@link #rational}.
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

    /** An exponent the {@code ^} slot can hold whole: one signed integer, or omega. */
    private static final Pattern EXP_ATOM = Pattern.compile("-?(?:\\d+|ω)");

    /**
     * Places the display keeps back from {@link Real#PLACES} — the guard digits.
     *
     * <p>Three of them, and they are what makes {@code sin(θ)²+cos(θ)²} come out as 1. The arithmetic rounds
     * wider than the display shows, so the error that grows through an expression grows in the part nobody
     * reads. Written as a difference rather than as a second constant because that is what it is: change the
     * arithmetic's width and the display follows, and the gap between them stays the thing that was chosen.
     */
    private static final int GUARD = 3;

    /** Decimal places a value is shown to. See {@link #rational}. */
    private static final int SHOWN = Real.PLACES - GUARD;

    private static final BigInteger TEN = BigInteger.TEN;

    public static String show(IExpr e) {
        // A quotient and a difference are spelled with the operator they were typed with, though the carrier
        // holds them as a product with a reciprocal and a sum with a negation. Recognising them here is what
        // keeps ÷ and − in the display without putting a second pair of nodes in the engine.
        if (e instanceof MultiplicationOperationExpr(IExpr left, IExpr right)
                && right instanceof ReciprocalOperationExpr(IExpr by)) {
            return arg(left, MUL) + "÷" + arg(by, POW);
        }
        if (e instanceof AdditionOperationExpr(IExpr left, IExpr right)
                && right instanceof NegationOperationExpr(IExpr taken)) {
            return arg(left, ADD) + "−" + arg(taken, MUL);
        }
        return switch (e) {
            case ProjectiveRationalLiteral p -> coordinates(p);
            case TractionLiteral t -> arg(t.base(), ATOM) + "^" + exponentAtom(t.exp());
            case AtomExpr a -> a.name();
            // A call is written the way it is typed. Its arguments are whole expressions and the call's own
            // brackets already separate them, so nothing inside needs brackets of its own.
            case CallExpr c -> c.name() + "(" + String.join(", ", c.args().stream().map(Render::show).toList()) + ")";
            // log returns an EXPONENT, so it renders as one: log of -1 to base 0 is ω.
            case LogarithmOperationExpr l -> "log(" + show(l.operand()) + ", " + show(l.base()) + ")";
            case NegationOperationExpr n -> "−" + arg(n.operand(), ATOM);
            case ReciprocalOperationExpr i -> "1÷" + arg(i.operand(), POW);
            case ExponentialOperationExpr p -> arg(p.base(), ATOM) + "^" + exponentAtom(p.exponent());
            case MultiplicationOperationExpr x -> juxtapose(arg(x.left(), MUL), arg(x.right(), MUL));
            case AdditionOperationExpr x -> sum(x);
            default -> e.toString();
        };
    }

    /**
     * A coordinate pair as the display writes it.
     *
     * <p>A unit denominator is the plain numeral. A zero denominator is a multiple of omega, since omega is
     * {@code 1÷0} and the pair {@code (n, 0)} is n of them — {@code (1, 0)} is the bare name. Everything else
     * is a quotient, or a decimal where that is both shorter and faithful.
     */
    private static String coordinates(ProjectiveRationalLiteral p) {
        BigInteger n = p.numerator();
        BigInteger d = p.denominator();
        if (d.equals(BigInteger.ONE)) {
            return n.toString();
        }
        if (d.signum() == 0) {
            if (n.equals(BigInteger.ONE)) {
                return "ω";
            }
            if (n.equals(BigInteger.ONE.negate())) {
                return "−ω";
            }
            return juxtapose(coefficient(n), "ω");
        }
        return rational(n, d);
    }

    /** A leading coefficient: bracketed when it is spelled as a quotient, bare otherwise. */
    private static String coefficient(BigInteger n) {
        return n.toString();
    }

    /**
     * A pair in the display's own language: {@code (5, 2)} prints as {@code 5÷2}, which is what the keypad
     * types.
     *
     * <h2>Or as a decimal, when that is shorter AND says the same thing</h2>
     * There are two ways to write a value down and the shorter is the one to show. But coordinates do not
     * reduce, so {@code 0.5} is the pair {@code (5, 10)} and nothing else: showing {@code (1, 2)} that way
     * would print one literal and read back another. The decimal is therefore used only where the denominator
     * is already a power of ten, which is exactly the case it exists for — {@link Real} rounds to
     * {@link Real#PLACES} and hands back a denominator of {@code 10^15}, so {@code sin(2)} is
     * {@code 0.909297426826} rather than a fifteen-digit quotient.
     *
     * <h2>Twelve places, and what that costs</h2>
     * The decimal is rounded to {@link #SHOWN} places, because arithmetic <em>downstream</em> of an
     * approximation is exact arithmetic on an approximation and gets longer as it goes:
     * {@code sin(θ)²+cos(θ)²} is 1.000000000001279794257938, every digit of it correct and none of it useful.
     * So the display is a twelve-place window onto an exact value, and re-entering what it shows re-enters the
     * rounding — which is what a displayed decimal has always meant, and is true of this one too. It is the one
     * place the printer is not an exact inverse, and it is deliberate.
     */
    private static String rational(BigInteger n, BigInteger d) {
        String fraction = n + "÷" + d;
        String decimal = decimal(n, d);
        return decimal != null && decimal.length() < fraction.length() ? decimal : fraction;
    }

    /**
     * {@code n/d} as a decimal of at most {@link #SHOWN} places, or null where that is not a spelling of this
     * pair — a denominator that is not a power of ten, or a rounding that would lose the number entirely.
     */
    private static String decimal(BigInteger n, BigInteger d) {
        if (d.signum() <= 0 || !isPowerOfTen(d)) {
            return null;
        }
        BigDecimal value = new BigDecimal(n)
                .divide(new BigDecimal(d), SHOWN, RoundingMode.HALF_EVEN)
                .stripTrailingZeros();
        return value.signum() == 0 && n.signum() != 0 ? null : value.toPlainString();
    }

    private static boolean isPowerOfTen(BigInteger d) {
        BigInteger rest = d;
        while (rest.compareTo(BigInteger.ONE) > 0 && rest.mod(TEN).signum() == 0) {
            rest = rest.divide(TEN);
        }
        return rest.equals(BigInteger.ONE);
    }

    /**
     * An exponent in the {@code ^} slot, bracketed only where it has to be. {@code ^} takes a single
     * primary on its right, so {@code 0^2} and {@code 0^-2} read back exactly as printed, while
     * {@code 0^(1+ω)} and {@code 0^(1÷2)} would lose everything after the first token.
     */
    private static String exponentAtom(IExpr e) {
        String s = show(e);
        return EXP_ATOM.matcher(s.replace('−', '-')).matches() ? s : "(" + s + ")";
    }

    // ---------------------------------------------------------------- the operators

    /**
     * A sum. A negative right operand is joined with a minus rather than a plus-minus pair, which is both how
     * it would be typed and how it reads back — {@code 2−1} parses to the same sum it came from.
     */
    private static String sum(AdditionOperationExpr x) {
        String right = arg(x.right(), MUL);
        String left = arg(x.left(), ADD);
        return right.startsWith("−") ? left + right : left + "+" + right;
    }

    /** Two rendered factors, with a sign only where juxtaposition would not read back as a product. */
    private static String juxtapose(String left, String right) {
        return Notation.implied(left.charAt(left.length() - 1), right.charAt(0))
                ? left + right
                : left + Notation.TIMES + right;
    }

    /** Render for a position that needs at least {@code min} binding tightness. */
    private static String arg(IExpr e, int min) {
        String s = show(e);
        return precedence(e) < min ? "(" + s + ")" : s;
    }

    /** How tightly a term's rendering binds. Read off the TERM, never off the rendered string. */
    private static int precedence(IExpr e) {
        if (e instanceof MultiplicationOperationExpr(IExpr ignored, IExpr right)
                && right instanceof ReciprocalOperationExpr) {
            return MUL;   // a quotient: a÷b·c would move which pair the residue belongs to
        }
        return switch (e) {
            case AdditionOperationExpr a -> ADD;
            case MultiplicationOperationExpr m -> MUL;
            case ProjectiveRationalLiteral p -> pair(p);
            case AtomExpr a -> ATOM;
            case CallExpr c -> ATOM;          // a call is parenthesised, so it binds as tightly as a name
            case LogarithmOperationExpr l -> ATOM;
            default -> POW;                   // negation, reciprocal, exponential, traction
        };
    }

    /** A pair binds as its spelling does: a numeral is a primary, a quotient is not, a signed one leads with -. */
    private static int pair(ProjectiveRationalLiteral p) {
        if (p.numerator().signum() < 0) {
            return POW;   // the leading minus has to be kept off a juxtaposition
        }
        if (p.denominator().equals(BigInteger.ONE)) {
            return ATOM;
        }
        if (p.denominator().signum() == 0) {
            return p.numerator().equals(BigInteger.ONE) ? ATOM : MUL;
        }
        return coordinates(p).indexOf('÷') < 0 ? ATOM : MUL;
    }
}
