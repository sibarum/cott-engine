package sibarum.cott.engine.traction.rule;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.rule.Rewrite;
import sibarum.cott.engine.base.rule.Rule;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.ExponentialOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;

import java.math.BigInteger;
import java.util.Optional;

/**
 * The rules, one method per row of the table in {@code docs/rule-combinations.md}.
 * <p>
 * Every rule here is Proven from the primitives. What is Maybe, what is Chosen and in conflict, and what is
 * open is NOT wired into simplification: those live at the bottom of this class as
 * {@link #provisionalSum} and {@link #provisionalNegation}, exercised by tests and reachable by hand, so that
 * the engine's default answers are the ones the theory has actually settled.
 *
 * <h2>Matching is on terms, never on values</h2>
 * A degenerate cell cannot be recognised by what it is worth. {@code 1·1} is the value 1 and {@code 1·(1÷1)}
 * is an erasure; they are the same class and different terms. So the erasure guards below compare the
 * exponents as written -- {@code b} is {@code -a} when it is the negation of that term, not when the two
 * happen to add to something that projects onto zero -- and nothing is canonicalised on the way in.
 *
 * <h2>Reading a value as a power of zero</h2>
 * Every traction value is {@code 0^a} for exactly one a (E6), but only four of them can be read that way from
 * the coordinates alone: {@code 0 = 0^1} (E4), {@code 1 = 0^0} (E5), {@code w = 0^-1} (Proven), and
 * {@code -1 = 0^w} (Chosen -- this one inherits the leap). Anything else would need the general involution,
 * which is theory-problems.md #4 and is not assumed here. That is why {@code 2^3} stands: reading 2 as a power
 * of zero is precisely the unpaid assumption.
 */
public final class TractionRules {

    private static final ProjectiveRationalLiteral ZERO = ProjectiveRationalLiteral.ZERO;
    private static final ProjectiveRationalLiteral ONE = ProjectiveRationalLiteral.ONE;
    private static final ProjectiveRationalLiteral NEG_ONE = ProjectiveRationalLiteral.NEG_ONE;
    private static final ProjectiveRationalLiteral OMEGA = ProjectiveRationalLiteral.OMEGA;

    /** The table in rule-combinations.md, as the things a derivation cites. */
    public static final Rule PRODUCT =
            new Rule("0^a · 0^b = 0^(a+b)", "E1", Rule.Status.PROVEN);
    public static final Rule QUOTIENT =
            new Rule("0^a ÷ 0^b = 0^(a-b)", "E1 + E3", Rule.Status.PROVEN);
    public static final Rule DIFFERENCE =
            new Rule("0^a - 0^b = 0^(a÷b)", "E10, total", Rule.Status.PROVEN);
    public static final Rule INTEGER_POWER =
            new Rule("(0^a)^n = 0^(a·n), integer n", "E1, repeated multiplication", Rule.Status.PROVEN);
    public static final Rule NEGATIVE_POWER =
            new Rule("(0^a)^-n = 0^(-a·n)", "E3", Rule.Status.PROVEN);
    public static final Rule BASE_ZERO =
            new Rule("0^E is a traction; 0^1 = 0 and 0^0 = 1", "E4, E5", Rule.Status.PROVEN);

    /** Not a rewrite at all: the cell is open, and saying so is what keeps the layer below from answering. */
    public static final Rule STANDS =
            new Rule("the term stands", "theory-problems.md #1", Rule.Status.OPEN);

    public static final Rule ADDITION_LAW =
            new Rule("0^a + 0^b = 0^(a·b)", "the mirror of E10", Rule.Status.MAYBE);
    public static final Rule NEGATION =
            new Rule("-(0^a) = 0^(a+w)", "E1 and -1 = 0^w", Rule.Status.CHOSEN);
    public static final Rule MINUS_ONE =
            new Rule("-1 = 0^w", "E6 and E7, the leap", Rule.Status.CHOSEN);

    private TractionRules() {
    }

    // ---------------------------------------------------------------- reading and building

    /** The exponent {@code a} for which this expression is {@code 0^a}, where that can be read off. */
    public static Optional<IExpr> exponentOfZero(IExpr e) {
        if (e instanceof TractionLiteral t && ZERO.equals(t.base())) {
            return Optional.of(t.exp());
        }
        if (e instanceof ExponentialOperationExpr p && ZERO.equals(p.base())) {
            return Optional.of(p.exponent());
        }
        if (ZERO.equals(e)) {
            return Optional.of(ONE);        // E4
        }
        if (ONE.equals(e)) {
            return Optional.of(ZERO);       // E5
        }
        if (OMEGA.equals(e)) {
            return Optional.of(NEG_ONE);    // Proven: w = 0^-1 = 1/0
        }
        // -1 = 0^w is NOT read here, though it is Chosen. See theLeapIsNotWired below and
        // {@link #provisionalMinusOne}: taking it makes (-1)·(-1) = 0^(w+w), and the coordinates put w+w at
        // 1, so the engine would answer 0 for the square of minus one. Three of the four points read; the
        // fourth is the leap, and it is the one in conflict.
        return Optional.empty();
    }

    /**
     * {@code 0^exponent}, as the point it names where it names one and as a traction otherwise.
     * <p>
     * The four foldings are the same four readings run backwards, so this and {@link #exponentOfZero} agree by
     * construction and a value cannot come out spelled two ways depending on which was used.
     */
    public static IExpr traction(IExpr exponent) {
        if (ONE.equals(exponent)) {
            return ZERO;
        }
        if (ZERO.equals(exponent)) {
            return ONE;
        }
        if (NEG_ONE.equals(exponent)) {
            return OMEGA;
        }
        // 0^w stays 0^w, for the same reason -1 is not read as it: folding one direction without the other
        // would leave the two unable to meet under E1, and folding both answers 0 for (-1)·(-1).
        return new TractionLiteral(ZERO, exponent);
    }

    /**
     * {@code -1 = 0^w}, the Chosen leap, kept here and out of the rules above.
     * <p>
     * It is forced by E6 and E7 given the other three points, so it is not lightly dropped. But wiring it as a
     * reading makes {@code (-1)·(-1)} into {@code 0^(w+w)}, and the projective coordinates put {@code w+w} at
     * 1 — since {@code (1,0) + (1,0)} is {@code (0,0)}, which is one — so the engine would answer that the
     * square of minus one is zero. The disagreement is between the layers and not inside either: the
     * coordinates are a model of the rationals with omega, and this identity is a claim about tractions.
     * <p>
     * Until one of the two gives, minus one and {@code 0^w} are kept from meeting: three points read, the
     * fourth does not, and {@code 0^w} prints as itself.
     */
    public static Optional<IExpr> provisionalMinusOne(IExpr e) {
        if (NEG_ONE.equals(e)) {
            return Optional.of(OMEGA);
        }
        if (OMEGA.equals(e)) {
            return Optional.of(NEG_ONE);
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------- the settled rows

    /**
     * {@code 0^a · 0^b -> 0^(a+b)} (E1), and {@code 0^a ÷ 0^b -> 0^(a-b)} (E1 + E3), division arriving as a
     * product with a reciprocal in it.
     * <p>
     * Empty where the exponent operation is an erasure and the term therefore stands: {@code b = -a} for the
     * product, {@code a = b} for the quotient. Both are the additive erasure in the exponent, which is
     * Problem 1 and is not this class's to answer. {@code 0·w} is the product case at {@code a = 1, b = -1}.
     */
    public static Optional<Rewrite> product(IExpr left, IExpr right) {
        if (right instanceof ReciprocalOperationExpr(IExpr by)) {
            return pair(left, by).map(ab -> ab.a().equals(ab.b())
                    ? new Rewrite(stands(left, right), STANDS)
                    : new Rewrite(traction(minus(ab.a(), ab.b())), QUOTIENT));
        }
        return pair(left, right).map(ab -> negates(ab.a(), ab.b())
                ? new Rewrite(stands(left, right), STANDS)
                : new Rewrite(traction(plus(ab.a(), ab.b())), PRODUCT));
    }

    /**
     * The term, unchanged, for a cell the theory has not settled.
     * <p>
     * Returned rather than declining, and the difference matters: an empty answer means "no traction reading
     * here, let the projective layer have it", and the projective layer would then answer anyway. {@code 0·w}
     * is a product of two coordinate pairs as well as an erasure between two exponents, and the coordinates
     * are perfectly willing to multiply them. Standing has to be said, not merely not-said.
     */
    private static IExpr stands(IExpr left, IExpr right) {
        return new MultiplicationOperationExpr(left, right);
    }

    /**
     * {@code 0^a - 0^b -> 0^(a÷b)} (E10), subtraction arriving as a sum with a negation in it.
     * <p>
     * Total, including at {@code a = b}: the multiplicative erasure in the exponent materialises as 1, so
     * {@code y - y} is {@code 0^1}, which is the point zero. That totality is the branch this engine is on,
     * and it is what settles the row rather than leaving it to a convention.
     *
     * <p>Addition itself is not here. {@code 0^a + 0^b -> 0^(a·b)} is Maybe and is
     * {@link #provisionalSum}.
     */
    public static Optional<Rewrite> sum(IExpr left, IExpr right) {
        if (!(right instanceof NegationOperationExpr(IExpr taken))) {
            return Optional.empty();
        }
        // At a = b the exponent is the multiplicative erasure, and what it materialises as is the
        // multiplicative IDENTITY -- not whatever the coordinates happen to compute. a÷a at a = (2,1) is the
        // pair (2,2), which is the value one at coordinates that are not one, and 0^(2,2) is not the point
        // zero. Reading the erasure off the term is what makes subtraction total: y - y is 0 for every y.
        return pair(left, taken).map(ab -> new Rewrite(ab.a().equals(ab.b())
                ? traction(ONE)
                : traction(over(ab.a(), ab.b())), DIFFERENCE));
    }

    /**
     * {@code (0^a)^n -> 0^(a·n)} for a nonzero integer n, which is E1 and not E2: an integer power is
     * repeated multiplication, so exponent addition gives it directly.
     * <p>
     * Two exclusions, both load-bearing. {@code n = 0} is not repeated multiplication of anything -- zero
     * copies is the empty product, and calling that 1 is {@code x^0 = 1}, which this theory does not have. And
     * n has to be an integer <b>as a term</b>: the pair (6, 3) is a coordinate pair and not the integer 2, so
     * asking what it projects to would let E2 back in at exactly the points E2 was withdrawn from.
     *
     * <p>A base-zero power is not a rule at all and is handled first: {@code 0^E} <em>is</em> a traction for
     * any E, so it is recognised rather than derived. That is what turns a typed {@code 0^2} into one, and
     * what makes {@code 0^0} the value 1 by E5 and {@code 0^1} the point zero by E4.
     */
    public static Optional<Rewrite> power(IExpr base, IExpr exponent) {
        if (ZERO.equals(base)) {
            return Optional.of(new Rewrite(traction(exponent), BASE_ZERO));
        }
        if (!isNonZeroInteger(exponent)) {
            return Optional.empty();
        }
        BigInteger n = ((ProjectiveRationalLiteral) exponent).numerator();
        if (n.signum() > 0 && combines(base)) {
            return Optional.of(new Rewrite(repeated(base, n), INTEGER_POWER));
        }
        // A NEGATIVE power is 1/y^n, and E3 grants that at base zero only. For a general base it is a
        // convention rather than a consequence, so 2^-1 stands -- as it did in the engine before this one.
        return exponentOfZero(base).map(a -> new Rewrite(traction(times(a, exponent)), NEGATIVE_POWER));
    }

    /**
     * {@code base} multiplied by itself n times, which is what a positive integer power IS.
     * <p>
     * No traction reading is needed for this and none is taken: it is the meaning of the notation, and it is
     * also the whole proof of the integer power rule. Where the base is {@code 0^a} the product rule above
     * turns the n copies into {@code 0^(na)} on its own, so the two are one mechanism rather than two —
     * {@code (0^2)^3} lands on {@code 0^6} by the same path that takes {@code 2^3} to 8.
     */
    /**
     * Whether writing the copies out will actually join them back up.
     * <p>
     * {@code x^2} is not improved by becoming {@code x·x} — the power is the better spelling of the same term,
     * and {@code (x^2)^2} would unfold to four x's for nothing. So the copies are only written where something
     * multiplies them: a coordinate pair, or a value that reads as a power of zero.
     */
    private static boolean combines(IExpr base) {
        return base instanceof ProjectiveRationalLiteral || exponentOfZero(base).isPresent();
    }

    private static IExpr repeated(IExpr base, BigInteger n) {
        IExpr out = base;
        for (BigInteger i = BigInteger.ONE; i.compareTo(n) < 0; i = i.add(BigInteger.ONE)) {
            out = new MultiplicationOperationExpr(out, base).simplify();
        }
        return out;
    }

    // ---------------------------------------------------------------- provisional, and deliberately unwired

    /**
     * {@code 0^a + 0^b -> 0^(a·b)}, the Maybe addition law -- the mirror of E10, never stated independently.
     * <p>
     * Not wired into simplification, and it cannot simply be switched on: it makes the value 0 an additive
     * identity, since {@code x + 0 = 0^u + 0^1 = 0^(u·1) = 0^u}, and that contradicts
     * {@link #provisionalNegation}, which gives {@code -0 = 0^(1+w) != 0}. Whichever of the two is wired, the
     * other must not be. See theory-problems.md, problem 2.
     * <p>
     * Empty at {@code b = 1÷a}, where the exponent product is the multiplicative erasure. That cell has no
     * recognisable form at the value level, which is one more reason the law is only Maybe.
     */
    public static Optional<Rewrite> provisionalSum(IExpr left, IExpr right) {
        return pair(left, right).flatMap(ab -> reciprocates(ab.a(), ab.b())
                ? Optional.<Rewrite>empty()
                : Optional.of(new Rewrite(traction(times(ab.a(), ab.b())), ADDITION_LAW)));
    }

    /**
     * {@code -(0^a) -> 0^(a+w)}, negation as multiplication by -1, which is Chosen.
     * <p>
     * Not wired, for the conflict above and for a second reason worth seeing before it is: the coordinates
     * make {@code 1 + w} equal to {@code w}, since {@code (1,1) + (1,0) = (1,0)}, so this rule sends
     * {@code -0} to {@code 0^w}, which is {@code -1}. The carrier's own negation says {@code -0 = 0}, because
     * a zero numerator negates to itself. Three answers for one term, and the disagreement is between the
     * layers rather than inside either.
     */
    public static Optional<Rewrite> provisionalNegation(IExpr operand) {
        return exponentOfZero(operand).map(a -> new Rewrite(traction(plus(a, OMEGA)), NEGATION));
    }

    // ---------------------------------------------------------------- the exponent arithmetic

    private record Pair(IExpr a, IExpr b) {
    }

    /** Both operands read as powers of zero, or empty if either cannot be. */
    private static Optional<Pair> pair(IExpr left, IExpr right) {
        return exponentOfZero(left).flatMap(a -> exponentOfZero(right).map(b -> new Pair(a, b)));
    }

    private static IExpr plus(IExpr a, IExpr b) {
        return new AdditionOperationExpr(a, b).simplify();
    }

    private static IExpr minus(IExpr a, IExpr b) {
        return new AdditionOperationExpr(a, new NegationOperationExpr(b)).simplify();
    }

    private static IExpr times(IExpr a, IExpr b) {
        return new MultiplicationOperationExpr(a, b).simplify();
    }

    private static IExpr over(IExpr a, IExpr b) {
        return new MultiplicationOperationExpr(a, new ReciprocalOperationExpr(b)).simplify();
    }

    /** Whether {@code b} is {@code -a} as a TERM: the negation of it, or its coordinates with the sign turned. */
    private static boolean negates(IExpr a, IExpr b) {
        if (b instanceof NegationOperationExpr(IExpr of) && of.equals(a)) {
            return true;
        }
        if (a instanceof NegationOperationExpr(IExpr of) && of.equals(b)) {
            return true;
        }
        return a instanceof ProjectiveRationalLiteral(BigInteger an, BigInteger ad)
                && b instanceof ProjectiveRationalLiteral(BigInteger bn, BigInteger bd)
                // A zero numerator is its own negation, and 1·1 is not an erasure.
                && an.signum() != 0
                && ad.equals(bd)
                && an.negate().equals(bn);
    }

    /** Whether {@code b} is {@code 1÷a} as a term, which is the erasure cell of the addition law. */
    private static boolean reciprocates(IExpr a, IExpr b) {
        if (b instanceof ReciprocalOperationExpr(IExpr of) && of.equals(a)) {
            return true;
        }
        if (a instanceof ReciprocalOperationExpr(IExpr of) && of.equals(b)) {
            return true;
        }
        return a instanceof ProjectiveRationalLiteral(BigInteger an, BigInteger ad)
                && b instanceof ProjectiveRationalLiteral(BigInteger bn, BigInteger bd)
                && an.equals(bd) && ad.equals(bn)
                && !an.equals(ad);   // a = 1÷a at (1,1) and (-1,-1), where the pair is not an erasure
    }

    private static boolean isNonZeroInteger(IExpr e) {
        return e instanceof ProjectiveRationalLiteral(BigInteger n, BigInteger d)
                && d.equals(BigInteger.ONE)
                && n.signum() != 0;
    }
}
