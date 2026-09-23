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
import sibarum.cott.engine.traction.expr.AdditiveTractionLiteral;
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
 * the traction pair (1, −1) and prints as {@code ω}, which is a spelling and not a rule.
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
    // · and / take factors, + takes terms.
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
        // keeps / and − in the display without putting a second pair of nodes in the engine.
        if (e instanceof MultiplicationOperationExpr(IExpr left, IExpr right)
                && right instanceof ReciprocalOperationExpr(IExpr by)) {
            return arg(left, MUL) + "/" + arg(by, POW);
        }
        if (e instanceof AdditionOperationExpr(IExpr left, IExpr right)
                && right instanceof NegationOperationExpr(IExpr taken)) {
            return arg(left, ADD) + "−" + arg(taken, MUL);
        }
        return switch (e) {
            case RationalLiteral p -> coordinates(p);
            case TractionLiteral t -> traction(t);
            // The additive pair prints as the sum it is, through the same path a written sum takes, so that
            // 1+0 and 1+ω read back as themselves. A bare one is just its power -- the fold says so too, but
            // a printer is handed terms that have not reduced and may not assume they have.
            case AdditiveTractionLiteral t -> t.isBare()
                    ? powerName(t.exponent())
                    : sum(new AdditionOperationExpr(t.real(), TractionLiteral.of(t.exponent())));
            case AtomExpr a -> a.name();
            // A call is written the way it is typed. Its arguments are whole expressions and the call's own
            // brackets already separate them, so nothing inside needs brackets of its own.
            case CallExpr c -> c.name() + "(" + String.join(", ", c.args().stream().map(Render::show).toList()) + ")";
            // log returns an EXPONENT, so it renders as one: log of -1 to base 0 is ω.
            case LogarithmOperationExpr l -> "log(" + show(l.operand()) + ", " + show(l.base()) + ")";
            case NegationOperationExpr n -> "−" + arg(n.operand(), ATOM);
            case ReciprocalOperationExpr i -> "1/" + arg(i.operand(), POW);
            case ExponentialOperationExpr p -> arg(p.base(), ATOM) + "^" + exponentAtom(p.exponent());
            case MultiplicationOperationExpr x -> juxtapose(arg(x.left(), MUL), arg(x.right(), MUL));
            case AdditionOperationExpr x -> sum(x);
            default -> e.toString();
        };
    }

    /**
     * A rational coordinate as the display writes it: a unit denominator is the plain numeral, and everything
     * else is a quotient, or a decimal where that is both shorter and faithful.
     *
     * <p>Omega is not here any more, and neither is {@code -0}. Both were spellings this pair had to carry
     * when a zero denominator lived in it; omega is now {@code 0^-1}, which the traction pair spells, and
     * {@code -0} is {@code -1·0}, which is the pair {@code (-1, 1)} and prints as the product it is.
     */
    private static String coordinates(RationalLiteral p) {
        BigInteger n = p.numerator();
        BigInteger d = p.denominator();
        if (d.equals(BigInteger.ONE)) {
            return signed(n.toString());
        }
        return rational(n, d);
    }

    /**
     * A traction pair, {@code n·0^t}.
     *
     * <p>The two multiplicative units get their names: {@code 0^1} is {@code 0} and {@code 0^-1} is
     * {@code ω}. That is a spelling and not a rule — the carrier holds those pairs, the way it used to hold
     * {@code (1, 0)} and print it as omega — and it is what keeps {@code 2ω} out of the display as
     * {@code 2·0^-1}. Everything else prints as the power it is.
     *
     * <p>A real part prints in front, joined the way a product is: {@code 2ω}, and {@code 2·0} with the sign
     * shown, since {@code 20} would read back as twenty. {@code -1·0} prints that way too, which is exactly
     * what it is — there is no {@code -0} among the units, and a name for it would suggest otherwise.
     */
    private static String traction(TractionLiteral t) {
        String power = powerName(t.exponent());
        if (t.isBare()) {
            return power;
        }
        // A real part of exactly -1 is a sign and prints as one, the way -1 does: -ω rather than -1ω, and -0
        // rather than -1·0. Both read back as the negation they are -- the printer is not claiming -0 is a
        // fifth unit, any more than it claims -2 is one.
        //
        // Only where the traction part has a NAME, though. A leading minus binds looser than ^, so -0^2 reads
        // back as (-0)^2, which is 0^2 -- a different value. That one keeps the coefficient: -1·0^2.
        if (RationalLiteral.NEG_ONE.equals(t.real()) && !power.startsWith("0^")) {
            return "−" + power;
        }
        return juxtapose(arg(t.real(), MUL), power);
    }

    private static String powerName(IExpr exponent) {
        if (RationalLiteral.ONE.equals(exponent)) {
            return "0";
        }
        if (RationalLiteral.NEG_ONE.equals(exponent)) {
            return "ω";
        }
        return "0^" + exponentAtom(exponent);
    }

    /** A leading coefficient. */
    private static String coefficient(BigInteger n) {
        return signed(n.toString());
    }

    /**
     * One minus sign, and it is the display's own.
     *
     * <p>{@link BigInteger} and {@link BigDecimal} write an ASCII hyphen, and {@link Notation#normalize} turns
     * that into U+2212 on the way back in — so a negative literal and a negation node were printing one value
     * two different ways. Both re-parsed correctly, which is exactly why it went unnoticed: the defect was at
     * the string, and the string is where a display lives.
     */
    private static String signed(String s) {
        return s.startsWith("-") ? "−" + s.substring(1) : s;
    }

    /**
     * A pair in the display's own language: {@code (5, 2)} prints as {@code 5/2}, which is what the keypad
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
        String fraction = signed(n.toString()) + "/" + signed(d.toString());
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
        // A zero numerator has no decimal spelling that keeps its denominator: 0/10 as a decimal is 0, which
        // reads back as (0,1) and is a different literal. So (0,10) keeps the quotient — it is zero at those
        // coordinates, written 0/10, and none of it is the point zero.
        if (n.signum() == 0) {
            return null;
        }
        BigDecimal value = new BigDecimal(n)
                .divide(new BigDecimal(d), SHOWN, RoundingMode.HALF_EVEN)
                .stripTrailingZeros();
        return value.signum() == 0 && n.signum() != 0 ? null : signed(value.toPlainString());
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
     * {@code 0^(1+ω)} and {@code 0^(1/2)} would lose everything after the first token.
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
                && !wordSpansTheJoin(left, right)
                ? left + right
                : left + Notation.TIMES + right;
    }

    /**
     * Whether dropping the sign would let a word of the vocabulary swallow the join.
     *
     * <p>Now that every letter is a variable, two factors can concatenate into a <em>name</em>:
     * {@code c·o·s} is a product of three, and printing it as {@code cos} hands back something that reads as
     * the cosine and then fails for want of brackets. The vocabulary is matched before single characters, so
     * once the boundary is inside a word it is gone — which is exactly the failure this class exists to
     * prevent, arriving by a route that did not exist while the variables were {@code x}, {@code y} and
     * {@code z} and no word was spelled out of those.
     *
     * <p>A word that <em>begins</em> at the join is not this: {@code 2·sin(x)} prints as {@code 2sin(x)} and
     * the scan finds {@code sin} as its own token, which is where the sign goes back. Only a word crossing
     * the boundary destroys it.
     *
     * <p>Asked of the builtin vocabulary, since the printer is not handed a session's. A session that defines
     * a name spelled out of its own variables could still collide, and that is the older question of what a
     * definition may shadow.
     */
    private static boolean wordSpansTheJoin(String left, String right) {
        String joined = left + right;
        for (int i = 0; i < left.length(); i++) {
            String word = Notation.wordAt(joined, i, Bindings.EMPTY);
            if (word != null && i + word.length() > left.length()) {
                return true;
            }
        }
        return false;
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
            return MUL;   // a quotient: a/b·c would move which pair the residue belongs to
        }
        return switch (e) {
            case AdditionOperationExpr a -> ADD;
            case MultiplicationOperationExpr m -> MUL;
            case RationalLiteral p -> coordinatePrecedence(p);
            case TractionLiteral t -> tractionPrecedence(t);
            case AtomExpr a -> ATOM;
            case CallExpr c -> ATOM;          // a call is parenthesised, so it binds as tightly as a name
            case LogarithmOperationExpr l -> ATOM;
            default -> POW;                   // negation, reciprocal, exponential
        };
    }

    /** A pair binds as its spelling does: a numeral is a primary, a quotient is not, a signed one leads with -. */
    private static int coordinatePrecedence(RationalLiteral p) {
        if (p.numerator().signum() < 0) {
            return POW;   // the leading minus has to be kept off a juxtaposition
        }
        if (p.denominator().equals(BigInteger.ONE)) {
            return ATOM;
        }
        return coordinates(p).indexOf('/') < 0 ? ATOM : MUL;
    }

    /**
     * A traction binds as its spelling does, and the spelling is read off the pair rather than off the
     * string: {@code 0} and {@code ω} are names and bind as tightly as one, a bare power is a power, and a
     * pair with a real part in front is the product it prints as.
     */
    private static int tractionPrecedence(TractionLiteral t) {
        if (!t.isBare()) {
            boolean leadingMinus = t.real() instanceof NegationOperationExpr
                    || t.real() instanceof RationalLiteral r && r.numerator().signum() < 0;
            return leadingMinus ? POW : MUL;
        }
        return RationalLiteral.ONE.equals(t.exponent()) || RationalLiteral.NEG_ONE.equals(t.exponent())
                ? ATOM
                : POW;
    }
}
