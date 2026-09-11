package sibarum.cott.engine.traction.rule;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.rule.Rewrite;
import sibarum.cott.engine.base.rule.Rule;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.ExponentialOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;
import sibarum.cott.engine.traction.expr.AdditiveTractionLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;

import java.math.BigInteger;
import java.util.Optional;

/**
 * The rules: the operations of Traction-Theory.md, one method each, and the two unit tables.
 *
 * <h2>The operations are the pair arithmetic</h2>
 * {@code (a, b) · (c, d) = (a·c, b+d)} is E1; {@code 1÷(a, b) = (1÷a, -b)} is E3; negation turns the real
 * coordinate. Those three are total and reversible on the pair and need no reading of anything as a power of
 * zero. Addition is not one of them -- see {@link #provisionalAddition}.
 *
 * <h2>Matching is on terms, never on values</h2>
 * A degenerate cell cannot be recognised by what it is worth. {@code 1·1} is the value 1 and {@code 1·(1÷1)}
 * is an erasure; they are the same class and different terms. So the erasure guards compare the exponents as
 * written -- {@code b} is {@code -a} when it is the negation of that term, not when the two happen to add to
 * something that projects onto zero -- and nothing is canonicalised on the way in.
 *
 * <h2>Reading a value as a power of zero</h2>
 * Every traction value is {@code 0^a} for one a (E6), but only the ones on the traction axis can be read that
 * way from the carrier: a bare pair, and the point zero by E4. {@code 1} and {@code -1} are the additive units
 * and are not read as powers of zero -- that is the axis restriction. Anything else would need the general
 * involution, which is theory-problems.md #4 and is not assumed here, and is why {@code 2^3} stands rather
 * than becoming a traction.
 */
public final class TractionRules {

    private static final RationalLiteral ZERO = RationalLiteral.ZERO;
    private static final RationalLiteral ONE = RationalLiteral.ONE;
    private static final RationalLiteral NEG_ONE = RationalLiteral.NEG_ONE;
    private static final TractionLiteral OMEGA = TractionLiteral.OMEGA;
    /** The absence marker, in either coordinate. Numerically the rational zero, and never read as one. */
    private static final RationalLiteral ABSENT = TractionLiteral.ABSENT;

    // ---------------------------------------------------------------- what a derivation cites

    public static final Rule PRODUCT =
            new Rule("a·0^b · c·0^d = (a·c)·0^(b+d)", "E1", Rule.Status.PROVEN);
    public static final Rule QUOTIENT =
            new Rule("a·0^b ÷ c·0^d = (a÷c)·0^(b-d)", "E1 + E3", Rule.Status.PROVEN);
    /**
     * The quotient rule, which Traction-Theory.md lists among the axioms as E2. The older docs derive it
     * instead, number it E10 and file it Chosen, since through the involution it inherits problem 4. It is an
     * axiom here because that is the model being implemented -- but a derivation that cites it names both.
     */
    public static final Rule DIFFERENCE =
            new Rule("0^a - 0^b = 0^(a÷b)", "E2, total (E10 and Chosen in the older docs)", Rule.Status.PROVEN);
    public static final Rule RECIPROCAL =
            new Rule("1÷(a·0^b) = (1÷a)·0^(-b)", "E3", Rule.Status.PROVEN);
    public static final Rule NEGATION =
            new Rule("-(a·0^b) = (-a)·0^b", "negation distributes over a product", Rule.Status.PROVEN);
    public static final Rule OMEGA_DEF =
            new Rule("1÷0 = ω", "E9", Rule.Status.PROVEN);
    public static final Rule LIKE_TERMS =
            new Rule("a·0^b + c·0^b = (a+c)·0^b", "distributivity", Rule.Status.PROVEN);
    public static final Rule INTEGER_POWER =
            new Rule("(0^a)^n = 0^(a·n), integer n", "E1, repeated multiplication", Rule.Status.PROVEN);
    /**
     * A negative outer power, which is NOT E3 however it is cited.
     * <p>
     * E3 relates a negated exponent to the reciprocal, {@code 0^(-a) = 1÷0^a}. Getting from there to
     * {@code (0^a)^-n} needs {@code x^-1 = 1÷x} at {@code x = 0^a} first, and the unit table refutes that
     * identification at the two units on the additive axis. So this is Chosen, and restricted to a base on
     * the traction axis -- see {@link #foldsToAnAdditiveUnit}.
     */
    public static final Rule NEGATIVE_POWER =
            new Rule("(0^a)^-n = 0^(-a·n)", "x^-1 = 1÷x at the base, which E3 does not give", Rule.Status.CHOSEN);
    public static final Rule BASE_ZERO =
            new Rule("0^E is a traction", "the pair (∅, E), and 0^0 the pair (1, ∅)", Rule.Status.PROVEN);
    public static final Rule POINT =
            new Rule("0^1 = 0, a·0^0 = a", "E4 and E5", Rule.Status.PROVEN);
    public static final Rule ROLL_IN =
            new Rule("(0, t) = (1, t+1)", "E4: the rational zero is 0^1", Rule.Status.PROVEN);
    public static final Rule ZERO_COORDINATES =
            new Rule("0÷d = (1÷d)·0", "E4: the rational zero is 0^1", Rule.Status.PROVEN);
    // x + 0 = x was a rule here. It is not one: it is a fact about the projection, and as a rewrite it
    // breaks associativity. See identity().
    public static final Rule TIMES_ONE =
            new Rule("x · 1 = x", "1 is invariant under multiplication", Rule.Status.PROVEN);
    public static final Rule LOG_INVERTS =
            new Rule("log_0(0^a) = a", "E8", Rule.Status.PROVEN);

    /**
     * The erasure, discharged to the identity of its own operation.
     * <p>
     * {@code z÷z = ∅} multiplicatively and {@code z-z = ∅} additively, so standing alone the first is 1 and
     * the second is 0. The kind is fixed by the operation it came FROM, not the one it lands in, which is what
     * settles {@code 0·w}: w is {@code 1÷0} by E9, so {@code 0·w} is {@code y·(1÷y)}, and it discharges to 1
     * even though it reaches the exponent as the additive erasure {@code 1 + -1}. The lift changes the kind.
     */
    public static final Rule ERASURE =
            new Rule("z÷z = 1, z-z = 0", "the erasure discharges to its own operation's identity", Rule.Status.PROVEN);
    /** {@code x + (z-z) = x}: under addition the additive erasure is ∅ and leaves no residue at all. */
    public static final Rule ERASURE_VANISHES =
            new Rule("x + (z-z) = x", "the erasure matches the operation, so nothing is left", Rule.Status.PROVEN);

    /** The leap, folded one way only. See {@link #exponentOfZero}. */
    public static final Rule LEAP =
            new Rule("0^ω = -1", "E6 and E7, the leap", Rule.Status.CHOSEN);
    /**
     * The unit tables. Chosen, but no longer merely asserted.
     * <p>
     * Searching all 4^12 completions of the axiom-forced base-0 row: 216 keep {@code x^1 = x} and a
     * logarithm at every base, four of those have every column a permutation and all four compose, two of
     * those keep {@code 0^x = ω^(-x)}, and one of those keeps the reciprocal law. This one. So the table is
     * forced given its premises, and it is Chosen because the reciprocal law is.
     */
    public static final Rule UNIT_POWER =
            new Rule("the unit exponentiation table",
                    "the only completion keeping x^1 = x, every log, and the reciprocal law", Rule.Status.CHOSEN);
    public static final Rule UNIT_LOG =
            new Rule("the unit logarithm table", "the exponentiation table inverted", Rule.Status.CHOSEN);

    /**
     * The additive node's inverse, which is NOT the multiplicative node's negation.
     * <p>
     * An inverse has to erase both coordinates, each under the operation acting there. In {@code n + 0^t}
     * the traction parts are joined by {@code ·}, so erasing them takes {@code 1/t} and not {@code -t}:
     * {@code inv(n, t) = (-n, 1/t)}, against {@code recip(n, t) = (1/n, -t)}. Each negates its own
     * coordinate and applies the OTHER operation's inverse to the traction part, which is the same duality
     * E1 and E2 state. {@code -} is then not primitive: {@code a - b} is {@code a + inv(b)}.
     */
    public static final Rule ADDITIVE_INVERSE =
            new Rule("inv(n, t) = (-n, 1/t)", "the inverse erases both coordinates", Rule.Status.PROVEN);
    /** {@code n + ∅ = n} and {@code ∅ + 0^t = 0^t}: an absent coordinate is skipped in this node too. */
    public static final Rule ADDITIVE_POINT =
            new Rule("(n, ∅) = n, (∅, t) = 0^t", "an absent coordinate is skipped", Rule.Status.PROVEN);
    /**
     * The additive node's own sum, which needs the mirror law and so is not wired.
     * <p>
     * {@code (a + 0^b) + (c + 0^d)} is {@code (a+c) + (0^b + 0^d)}, and closing the second bracket is
     * {@code 0^b + 0^d = 0^(b·d)} -- the mirror law, which is Traction-Theory.md's addition law on bare
     * powers and is not adopted there. It is the same disagreement: the law makes {@code ω + ω} the point
     * zero where distributivity makes it {@code 2ω}.
     */
    public static final Rule MIRROR_SUM =
            new Rule("(a, b) + (c, d) = (a+c, b·d)", "the mirror law, not adopted", Rule.Status.OPEN);

    public static final Rule ADDITION_LAW =
            new Rule("a·0^b + c·0^d = (a+c)·a^d·c^b·0^(bd)", "Traction-Theory.md: Addition", Rule.Status.OPEN);
    public static final Rule INVOLUTION =
            new Rule("0^(0^n) = n", "E6, off the closure set", Rule.Status.OPEN);
    public static final Rule MINUS_ZERO =
            new Rule("-1·0 = ω", "conjectured, and refuted from E1 + E3 + E6", Rule.Status.OPEN);

    private TractionRules() {
    }

    // ---------------------------------------------------------------- reading and building

    /**
     * The pair {@code (n, t)} this expression is, where the carrier can say.
     * <p>
     * A nonzero rational is {@code (r, 0)}: its real part is itself and its traction part is absent. That is
     * not E5 being used to read 1 as {@code 0^0} -- the real coordinate stays put, and nothing here moves an
     * additive unit onto the traction axis. The point zero is {@code (0, 1)} by E4, its real part absent.
     * <p>
     * A zero numerator away from {@code (0,1)} is not the absence marker but a multiple of the point zero:
     * {@code 0÷d} is {@code (1÷d)·0}, and dropping the d would lose something multiplicative -- a root, an
     * orientation -- that the type is supposed to conserve.
     */
    private static Optional<Pair> pair(IExpr e) {
        if (e instanceof TractionLiteral t) {
            return Optional.of(new Pair(t.real(), t.exponent()));
        }
        if (e instanceof RationalLiteral r) {
            if (!r.isZero()) {
                return Optional.of(new Pair(r, ABSENT));
            }
            return Optional.of(r.denominator().equals(BigInteger.ONE)
                    ? new Pair(ABSENT, ONE)
                    : new Pair(new RationalLiteral(BigInteger.ONE, r.denominator()), ONE));
        }
        return Optional.empty();
    }

    /** Whether a coordinate is the absence marker: this axis contributes nothing. */
    private static boolean absent(IExpr coordinate) {
        return ABSENT.equals(coordinate);
    }

    /**
     * Whether this term is a coordinate, or will be one once its sign settles.
     * <p>
     * The negation has to be seen through, because whether a rule fires must not depend on how far the
     * operand happens to have reduced. {@code -1÷-1} arrives with negation nodes the first time and with the
     * literal {@code -1} the second, and reading only the literal made those two disagree -- the first was
     * the whole-term erasure and answered 1, the second went to the coordinates and answered {@code (-1,-1)}.
     * A printed answer then re-read as something else.
     */
    private static boolean isCoordinate(IExpr e) {
        if (e instanceof NegationOperationExpr(IExpr operand)) {
            return isCoordinate(operand);
        }
        return e instanceof RationalLiteral;
    }

    /**
     * Whether this term is {@code z - z}: the additive erasure, as a term.
     * <p>
     * Either way round, and in either spelling. {@code 1 + (-1)} at the literal -1 is the same erasure as
     * {@code 1 - 1} at a negation node, and reading only the node made the two disagree —
     * {@code (1-1) + 2·0} was {@code 2·0} and {@code (1 + -1) + 2·0} was {@code 3·0}.
     */
    private static boolean isAdditiveErasure(IExpr e) {
        if (!(e instanceof AdditionOperationExpr(IExpr l, IExpr r))) {
            return false;
        }
        if (r instanceof NegationOperationExpr(IExpr taken)) {
            return l.equals(taken);
        }
        if (l instanceof NegationOperationExpr(IExpr taken)) {
            return r.equals(taken);
        }
        return negates(l, r);
    }

    /**
     * The exponents of two tractions added, with an absent one skipped.
     * <p>
     * Skipped for the same reason an absent real part is: {@code 0^0} is 1 and contributes nothing to a
     * product, so its exponent contributes nothing to the exponent sum. Adding it instead handed the
     * exponent slot a term the VALUE rules then read as the point zero -- {@code 0^0 · 0^0} became
     * {@code 0^(0+0)} and then {@code 0^(2·0)}, which is the two sorts being confused where it shows.
     */
    private static IExpr exponentSum(IExpr b, IExpr d) {
        if (absent(b)) {
            return d;
        }
        return absent(d) ? b : plus(b, d);
    }

    /** The same for a quotient: {@code b - d}, with an absent exponent skipped. */
    private static IExpr exponentDifference(IExpr b, IExpr d) {
        if (absent(d)) {
            return b;
        }
        return absent(b) ? new NegationOperationExpr(d) : minus(b, d);
    }

    /**
     * Whether this term is a bare power of zero that folds to an additive unit: {@code 0^0} is 1 and
     * {@code 0^ω} is -1.
     * <p>
     * Both are powers of zero as TERMS, and E1 may read them as such -- {@code 0^0 · 0^2} is {@code 0^2}.
     * The power rule may not, and the reason is the whole of why {@code x^-1} is not {@code 1÷x}. The rule
     * {@code (0^a)^-n = 0^(-a·n)} is cited to E3, but E3 relates a NEGATED EXPONENT to the reciprocal; to get
     * from it to a negative outer power you first need {@code x^-1 = 1÷x} at {@code x = 0^a}, and the unit
     * table refutes that at the two units on the additive axis. So the rule holds where {@code 0^a} is on
     * the traction axis, and at {@code a = 0} and {@code a = ω} the table answers instead: {@code (0^0)^-1}
     * is {@code 1^-1 = -1}, not {@code 0^(-0)}, and {@code (0^ω)^-1} is {@code (-1)^-1 = 1}, not
     * {@code 0^(-ω)}.
     */
    private static boolean foldsToAnAdditiveUnit(IExpr e) {
        IExpr exponent = e instanceof TractionLiteral t && t.isBare() ? t.exponent()
                : e instanceof ExponentialOperationExpr p && ZERO.equals(p.base()) ? p.exponent()
                : null;
        return exponent != null && (absent(exponent) || OMEGA.equals(exponent));
    }

    /**
     * Two real parts multiplied, with an absent one skipped rather than computed with.
     * <p>
     * This is what keeps the marker out of the arithmetic. Multiplying by it would annihilate -- {@code 2·0}
     * would come back as the point zero with the 2 gone, and then dividing by zero proves 2 = 1. Skipping it
     * is what {@code ∅} means multiplicatively: {@code x·(z÷z) = x}.
     */
    private static IExpr realProduct(IExpr a, IExpr c) {
        if (absent(a)) {
            return c;
        }
        return absent(c) ? a : times(a, c);
    }

    /** Two real parts divided, the same way. An absent numerator is 1, so it leaves {@code 1÷c}. */
    private static IExpr realQuotient(IExpr a, IExpr c) {
        if (absent(c)) {
            return a;
        }
        return absent(a) ? over(ONE, c) : over(a, c);
    }

    /**
     * Two real parts added, where an absent one counts as ONE COPY.
     * <p>
     * The real part is a multiplicative slot, so its absence is a multiplicative erasure -- and a
     * multiplicative erasure landing in a sum leaves a residue of one: {@code x + (z÷z) = x + 1}. That is
     * what makes {@code w + w} two omegas and {@code 0 + 0} two zeros.
     * <p>
     * Traction-Theory.md's addition law reads the same cell the other way, dropping the term rather than
     * leaving a residue, and answers {@code 0^(bd)} -- so it makes {@code w + w} the point zero. That
     * disagreement is the one edge of the law that does not dissolve; see {@link #provisionalAddition}.
     */
    private static IExpr realSum(IExpr a, IExpr c) {
        return plus(absent(a) ? ONE : a, absent(c) ? ONE : c);
    }

    /**
     * Whether this term has to be lifted before the pair rules can answer for it.
     * <p>
     * A traction obviously, and the rational zero, because the coordinate layer must never multiply that one:
     * as a rational it annihilates, and {@code 0·0} would come back as {@code 0} rather than {@code 0^2}. Two
     * ordinary rationals are left to the coordinates, which is not deference -- it is that lifting them would
     * put the pair rules in a loop, rebuilding {@code 2÷3} as {@code (2÷3)·0^0} for ever.
     */
    private static boolean liftable(IExpr e, boolean inExponent) {
        // In an exponent the rational zero is the absence marker, not the point zero, and lifting it there
        // is the sort confusion: 0 + 1 as an exponent is 1, and 0·3 is 0. A traction in an exponent is
        // still a value -- 0^(ω+ω) -- so that one lifts wherever it sits.
        return e instanceof TractionLiteral || !inExponent && e instanceof RationalLiteral r && r.isZero();
    }

    /**
     * The exponent {@code a} for which this expression is a BARE {@code 0^a}, where that can be read off.
     * <p>
     * Two readings: a traction with no real part, and the point zero by E4. Omega is the traction
     * {@code (1, -1)}, so it is already one of them.
     * <p>
     * {@code 1} and {@code -1} are not read here, though E5 says the first is {@code 0^0} and the leap says
     * the second is {@code 0^ω}. They are the additive units, and reading one as a power of zero mixes the
     * axes. The leap in particular folds only in the other direction -- {@code 0^ω} becomes {@code -1} --
     * because reading {@code -1} as {@code 0^ω} turns {@code (-1)·(-1)} into {@code 0^(ω+ω)}, which no rule
     * finishes, where the real coordinate answers 1 directly.
     */
    public static Optional<IExpr> exponentOfZero(IExpr e) {
        if (e instanceof TractionLiteral t && t.isBare()) {
            return Optional.of(t.exponent());
        }
        if (e instanceof ExponentialOperationExpr p && ZERO.equals(p.base())) {
            return Optional.of(p.exponent());
        }
        if (ZERO.equals(e)) {
            return Optional.of(ONE);        // E4
        }
        return Optional.empty();
    }

    /** {@code 0^exponent}. */
    public static IExpr traction(IExpr exponent) {
        return TractionLiteral.of(exponent);
    }

    /**
     * The pair {@code (real, exponent)}.
     * <p>
     * Built rather than folded: {@code (a, 0)} is {@code a·0^0} and E5 is the rule that finishes it, which
     * {@link #point} does on the turn after, in view of the derivation.
     */
    private static IExpr pairOf(IExpr real, IExpr exponent) {
        return new TractionLiteral(real, exponent);
    }

    // ---------------------------------------------------------------- the pair's own folds

    /**
     * What a pair folds to, where it folds to something: {@code a·0^0 = a} (E5), {@code 0^1 = 0} (E4), and
     * {@code 0^ω = -1} (the leap).
     * <p>
     * A rewrite of its own rather than something the constructor does on the way past, so that a derivation
     * shows it happening instead of finding it already done.
     * <p>
     * {@code (1, -1)} does not fold: it IS omega, which is how the carrier spells it, and the printer names
     * it. Nor does {@code (-1, 1)}, which is {@code -1·0} -- the theory has not resolved that one, and
     * {@link #provisionalMinusZero} records both the conjecture and what refutes it.
     */
    public static Optional<Rewrite> point(TractionLiteral t) {
        // A real part at a zero numerator away from (0,1) is a multiple of the point zero rather than the
        // marker: 0÷d·0^t is (1÷d)·0^(t+1).
        if (t.real() instanceof RationalLiteral r && r.isZero() && !r.denominator().equals(BigInteger.ONE)) {
            return Optional.of(new Rewrite(
                    pairOf(new RationalLiteral(BigInteger.ONE, r.denominator()), plus(t.exponent(), ONE)),
                    ROLL_IN));
        }
        if (absent(t.exponent())) {
            // Both absent is (0,0), erasure itself, which is not a member. It arrives here from the
            // multiplicative discharge -- 0·w -- so it discharges the way that operation does, to 1.
            return Optional.of(t.isBare()
                    ? new Rewrite(ONE, ERASURE)
                    : new Rewrite(t.real(), POINT));                    // a·0^0 = a·1 = a, E5
        }
        // A real part of exactly one is the multiplicative identity, so it says nothing that the absence
        // marker does not: 1·0^t is 0^t by x·1 = x. Collapsing it is what keeps one spelling per value --
        // 1÷0 arrives here as (1, -1) and omega is (0, -1) -- and it happens as a rule, in view of the
        // derivation, rather than by the marker and the coefficient being the same thing.
        if (ONE.equals(t.real())) {
            return Optional.of(new Rewrite(pairOf(ABSENT, t.exponent()), TIMES_ONE));
        }
        if (t.isBare() && ONE.equals(t.exponent())) {
            return Optional.of(new Rewrite(ZERO, POINT));               // 0^1 = 0, E4
        }
        if (OMEGA.equals(t.exponent())) {
            // The leap folds under a real part too, and it has to. Otherwise a product carries the real part
            // in first -- 0^ω · 0^ω becomes (-1, ω) by E1 -- and the fold can no longer see the term it was
            // waiting for, so the square of minus one stands instead of answering 1.
            return t.isBare()
                    ? Optional.of(new Rewrite(NEG_ONE, LEAP))
                    : Optional.of(new Rewrite(times(t.real(), NEG_ONE), LEAP));
        }
        return Optional.empty();
    }

    /**
     * What an additive pair folds to, where it folds: {@code n + ∅ = n} and {@code ∅ + 0^t = 0^t}.
     * <p>
     * Both are the absent coordinate being skipped, which is the rule that survives the change of join --
     * {@code 1} is {@code 1 + ∅} as readily as {@code 1 · ∅}. The second fold hands the term to the
     * multiplicative node, because a bare power of zero is one and the same value however it was written.
     * <p>
     * Both coordinates absent is {@code (0,0)}, the double erasure, and the first fold returns it as the
     * marker itself -- which standing alone is the point zero, addition's own discharge. Whether it should
     * instead stay as a member of the type is open; see Traction-Theory.md, Carrier.
     */
    public static Optional<Rewrite> additivePoint(AdditiveTractionLiteral t) {
        if (absent(t.exponent())) {
            return Optional.of(new Rewrite(t.real(), ADDITIVE_POINT));       // n + ∅ = n
        }
        if (t.isBare()) {
            return Optional.of(new Rewrite(traction(t.exponent()), ADDITIVE_POINT));   // ∅ + 0^t = 0^t
        }
        return Optional.empty();
    }

    /**
     * A zero numerator away from the coordinate {@code (0, 1)}: {@code 0÷d} is {@code (1÷d)·0}, the pair.
     * <p>
     * The rational zero at {@code (0, 1)} is the point zero and stays as it is. Anything else with a zero
     * numerator is a multiple of it -- {@code (0, -1)} is what {@code 1 + 1÷(-1)} lands on, where the
     * magnitude cancels and the orientation survives -- and the pair is where a multiple of the point zero
     * lives. Without this the same value had two spellings and the display did not round-trip: the coordinate
     * sum produced {@code 0÷-1} and re-reading it produced {@code 1÷-1·0}.
     */
    public static Optional<Rewrite> zeroCoordinates(RationalLiteral r, boolean inExponent) {
        if (inExponent || !r.isZero() || r.denominator().equals(BigInteger.ONE)) {
            return Optional.empty();
        }
        return Optional.of(new Rewrite(
                pairOf(new RationalLiteral(BigInteger.ONE, r.denominator()), ONE), ZERO_COORDINATES));
    }

    // ---------------------------------------------------------------- multiplication and division

    /**
     * {@code (a, b) · (c, d) = (a·c, b+d)}, E1, with division arriving as a product with a reciprocal in it,
     * written either way round.
     * <p>
     * Where the exponent operation is an erasure the term discharges to the multiplicative identity:
     * {@code b = -a} for the product, {@code a = b} for the quotient. Both present as the ADDITIVE erasure in
     * the exponent and both are multiplicative at the value level, so what they discharge to is
     * multiplication's identity. {@code 0·w} is the product case, and it is 1.
     */
    public static Optional<Rewrite> product(IExpr left, IExpr right, boolean inExponent) {
        if (right instanceof ReciprocalOperationExpr(IExpr by)) {
            return quotient(left, by, inExponent);
        }
        // A division written the other way round is still a division: (1÷y)·x is x÷y, the same rule with the
        // roles swapped. Matching one order only made multiplication non-commutative on those terms.
        if (left instanceof ReciprocalOperationExpr(IExpr by)) {
            return quotient(right, by, inExponent);
        }
        if (!liftable(left, inExponent) && !liftable(right, inExponent)) {
            return Optional.empty();
        }
        return pair(left).flatMap(l -> pair(right).map(r -> {
            IExpr real = realProduct(l.n(), r.n());
            if (!negates(l.t(), r.t())) {
                return new Rewrite(pairOf(real, exponentSum(l.t(), r.t())), PRODUCT);
            }
            // The exponent erases and the operation is a product, so it discharges to multiplication's
            // identity. Where there is no real part either, that identity is the whole answer, and it has to
            // be given HERE rather than as the pair (0,0): (0,0) is erasure itself and not a member, and
            // left standing for a turn an enclosing product multiplied it. 0·ω·1 answered 0^(2·0).
            return absent(real)
                    ? new Rewrite(ONE, ERASURE)
                    : new Rewrite(pairOf(real, ABSENT), ERASURE);
        }));
    }

    /**
     * {@code (a, b) ÷ (c, d) = (a÷c, b-d)}, E1 + E3.
     * <p>
     * {@code z÷z} is the whole-term erasure and is 1 before any coordinate is touched -- otherwise
     * {@code 2÷2} would come back as the pair {@code (2,2)}, which is the value one at coordinates that are
     * not one. Reading the erasure off the term is what makes it exact.
     */
    private static Optional<Rewrite> quotient(IExpr of, IExpr by, boolean inExponent) {
        // The whole-term erasure, but not between two coordinates: there the coordinates answer, and what
        // they answer is (2,2) -- one at coordinates that are not one. That is deliberate and it is the same
        // fact the exponent erasure is read off the term to avoid, so overruling it here would contradict
        // the reason this engine does not reduce. Everywhere else -- tractions, atoms, whole terms -- z÷z
        // is 1.
        boolean coordinates = isCoordinate(of) && isCoordinate(by);
        if (!coordinates && of.equals(by)) {
            return Optional.of(new Rewrite(ONE, ERASURE));
        }
        if (!liftable(of, inExponent) && !liftable(by, inExponent)) {
            return Optional.empty();
        }
        if (of.equals(by)) {
            return Optional.of(new Rewrite(ONE, ERASURE));
        }
        return pair(of).flatMap(l -> pair(by).map(r -> {
            IExpr real = realQuotient(l.n(), r.n());
            if (!l.t().equals(r.t())) {
                return new Rewrite(pairOf(real, exponentDifference(l.t(), r.t())), QUOTIENT);
            }
            return absent(real)
                    ? new Rewrite(ONE, ERASURE)
                    : new Rewrite(pairOf(real, ABSENT), QUOTIENT);
        }));
    }

    // ---------------------------------------------------------------- addition and subtraction

    /**
     * What a sum answers: the like-terms case by distributivity, and subtraction by E2.
     * <p>
     * {@code a·0^b + c·0^b = (a+c)·0^b} is distributivity and nothing more -- it is what keeps
     * {@code ω + ω = 2ω} rather than sending it through a law. The general sum at unlike exponents is
     * {@link #provisionalAddition} and is not wired.
     */
    public static Optional<Rewrite> sum(IExpr left, IExpr right, boolean inExponent) {
        // x + (z-z) = x. An additive erasure under addition is ∅ and vanishes -- it does NOT materialise as
        // the point zero and then add. Discharging it first answered 0 + (1-1) as 2·0, where 0 + 1 - 1 is 0.
        // The residue is only left where the operation does not match, which for z-z means a product.
        if (isAdditiveErasure(right)) {
            return Optional.of(new Rewrite(left, ERASURE_VANISHES));
        }
        if (isAdditiveErasure(left)) {
            return Optional.of(new Rewrite(right, ERASURE_VANISHES));
        }
        if (right instanceof NegationOperationExpr(IExpr taken)) {
            return difference(left, taken);
        }
        // A subtraction written the other way round is still a subtraction: -x + y is y - x.
        if (left instanceof NegationOperationExpr(IExpr taken)) {
            return difference(right, taken);
        }
        // A sum of two terms where one is the other with its sign turned is the same erasure with no
        // negation node left to recognise it by -- the coordinates have already absorbed the sign. It has to
        // be caught here as well, because the product rule cannot always catch it first: E1 builds the
        // exponent sum while the exponents are still terms, so 0^(2÷2)·0^(-2÷2) reaches this as
        // 0^((2,2) + (-2,2)) with its own erasure the only one left to find. Letting the coordinates have it
        // instead answered 0^(1÷2·0), which is not 1.
        if (negates(left, right)) {
            return Optional.of(new Rewrite(ZERO, ERASURE));
        }
        return likeTerms(left, right, inExponent);
    }

    /**
     * Two terms alike in their traction part, whose real parts therefore add.
     * <p>
     * This is what makes {@code 0 + 0} come out as {@code 2·0} rather than as {@code 0}: it runs before the
     * identity, so {@code x + 0 = x} applies exactly where x is NOT a multiple of the point zero. Between
     * them they are the dominance condition REVIEW.md's P1-1 asks for -- the identity holds where x dominates,
     * and where the two are at the same order the real parts add instead.
     * <p>
     * Real parts that negate as terms are the additive erasure and discharge to the point zero. Without that,
     * {@code 1·0 + (-1)·0} would land on a real part of zero, which is the absence marker and would say
     * {@code 0^1} for a different reason -- the coordinates arriving at the right answer by accident, where
     * the term said erasure.
     * <p>
     * An absent real part counts as one copy here; see {@link #realSum}.
     */
    private static Optional<Rewrite> likeTerms(IExpr left, IExpr right, boolean inExponent) {
        if (!liftable(left, inExponent) && !liftable(right, inExponent)) {
            return Optional.empty();    // two rationals: the coordinates do this, and say so
        }
        return pair(left).flatMap(l -> pair(right)
                .filter(r -> l.t().equals(r.t()))
                .map(r -> negates(l.n(), r.n())
                        ? new Rewrite(ZERO, ERASURE)
                        : new Rewrite(pairOf(realSum(l.n(), r.n()), l.t()), LIKE_TERMS)));
    }

    /**
     * {@code 0^a - 0^b = 0^(a÷b)}, E2, given the term subtracted FROM and the term taken away.
     * <p>
     * Total, including at {@code a = b}: {@code z-z} is the additive erasure and discharges to the additive
     * operation's own value, the point zero. Reading it off the term is what makes that exact --
     * {@code a÷a} at {@code a = (2,1)} is the pair {@code (2,2)}, one at coordinates that are not one, and
     * {@code 0^(2,2)} is not the point zero.
     * <p>
     * Bare powers of zero only. {@code 2·0^a - 3·0^b} has no rule and stands, and the additive units are not
     * read as powers of zero at all.
     */
    private static Optional<Rewrite> difference(IExpr from, IExpr taken) {
        if (from.equals(taken)) {
            return Optional.of(new Rewrite(ZERO, ERASURE));
        }
        return exponentOfZero(from).flatMap(a -> exponentOfZero(taken)
                .map(b -> new Rewrite(traction(over(a, b)), DIFFERENCE)));
    }

    // ---------------------------------------------------------------- the unary operations

    /**
     * {@code -(a·0^b) = (-a)·0^b}: negation turns the real coordinate and leaves the traction part alone.
     * <p>
     * So {@code -0} is {@code (-1, 1)}, which is {@code -1·0} and does not reduce -- there is no {@code -0}
     * among the four units, and the theory has not resolved what that product is. The alternative reading,
     * {@code -(0^a) = 0^(a+ω)}, is the same claim routed through the leap; it agrees at {@code -1} and is not
     * needed anywhere the real coordinate can answer.
     */
    public static Optional<Rewrite> negation(IExpr operand, boolean inExponent) {
        // The additive node's inverse reciprocates the traction part instead of leaving it alone, because
        // there the traction parts are joined by · and that is what erases them. An absent real part has
        // nothing to turn and stays absent -- additively the marker is already the identity, so there is no
        // -1 to materialise the way there is in a product.
        if (operand instanceof AdditiveTractionLiteral t) {
            return Optional.of(new Rewrite(new AdditiveTractionLiteral(
                    t.isBare() ? ABSENT : t.real().negated(),
                    absent(t.exponent()) ? ABSENT : t.exponent().reciprocal()), ADDITIVE_INVERSE));
        }
        if (operand instanceof TractionLiteral t) {
            // An absent real part has no sign to turn, so the -1 being multiplied by materialises there:
            // negation IS multiplication by -1, and (-1, 0)·(0, b) is (-1, b) by the same skipping rule.
            // A traction in an exponent is still a value -- 0^(ω+ω) -- so this fires wherever it sits.
            return Optional.of(new Rewrite(
                    pairOf(t.isBare() ? NEG_ONE : t.real().negated(), t.exponent()), NEGATION));
        }
        // The point zero is (0, 1), whose real part is absent, so negating it lifts it first: 0 is 0^1 by E4
        // and -0 is (-1)·0^1. Leaving it to the coordinates is what used to answer -0 = 0, which is not what
        // negation is.
        //
        // NOT in an exponent. There the rational zero is the absence marker rather than the point zero, and
        // lifting it materialises a value on the traction axis inside a slot that holds exponents: 0^(2-0)
        // became 0^(2 + (-1)·0) and stood, where 0^(0-2) reached 0^-2 because the negation fell on the 2
        // instead. One operation, two answers, decided by which side the zero was written on.
        if (!inExponent && ZERO.equals(operand)) {
            return Optional.of(new Rewrite(pairOf(NEG_ONE, ONE), NEGATION));
        }
        return Optional.empty();
    }

    /**
     * {@code 1÷(a·0^b) = (1÷a)·0^(-b)}, E3, and {@code 1÷0 = ω}, E9.
     * <p>
     * E9 is where the old carrier's zero denominator went. A rational cannot hold {@code 1÷0}, so this is an
     * axiom being applied, and a derivation shows it being applied.
     */
    public static Optional<Rewrite> reciprocal(IExpr operand) {
        if (ZERO.equals(operand)) {
            return Optional.of(new Rewrite(OMEGA, OMEGA_DEF));
        }
        if (operand instanceof TractionLiteral t) {
            // An absent real part stays absent: 1÷∅ is ∅, since ∅ is one multiplicatively.
            return Optional.of(new Rewrite(
                    pairOf(t.isBare() ? ABSENT : t.real().reciprocal(), t.exponent().negated()), RECIPROCAL));
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------- the identities

    /**
     * The multiplicative identity, {@code x · 1 = x}, and only that one.
     *
     * <h2>{@code x + 0 = x} is not here, and is not a traction rule</h2>
     * It is a fact about the PROJECTION. Adding the point zero does not move a value's shadow, and that is
     * the whole of what it says; in the type the two terms are both still there, and discarding one loses
     * information the type exists to conserve. So {@code x + 0} stands, and its shadow is x's.
     * <p>
     * It was a rule here, with a condition on x, and the condition could not be made to work. Any version of
     * it breaks associativity: {@code (1 + -1) + 2·0} is {@code 2·0} because the erasure vanishes and leaves
     * the multiple of zero, while {@code 1 + (-1 + 2·0)} was 0 because the identity absorbed the {@code 2·0}
     * into the {@code -1} first and then the 1 and the -1 erased. Six pairs in a 2400-pair sweep, all of
     * that shape, and they are gone with the rule.
     * <p>
     * Where an answer still comes out as x, the coordinates did it -- {@code 1 + 0} is 1 because
     * {@code (1,1) + (0,1)} is {@code (1,1)} -- and the coordinates are the model rather than the theory.
     * The traction layer no longer claims it.
     * <p>
     * -1 and ω are invariant under neither operation, which is what separates them from 1: {@code 1 + ω} has
     * no single value and stands as the pair it is.
     */
    /**
     * Whether a sum's operands are coordinates that may simply add.
     * <p>
     * Not where one of them is the point zero. Adding it away is the same absorption the traction rule was
     * removed for, and it happens here too: {@code (1,1) + (0,1)} is {@code (1,1)}, so {@code 1 + 0} came
     * out as 1 with the point zero gone. Whether that is the model being allowed to answer, or the model
     * quietly doing what the theory declined to, is the question the removal leaves -- and until it is
     * settled the conservative reading is the one that matches the rule's removal.
     */
    public static boolean addsAsCoordinates(IExpr left, IExpr right) {
        return !ZERO.equals(left) && !ZERO.equals(right);
    }

    public static Optional<Rewrite> identity(IExpr left, IExpr right, boolean product) {
        if (!product) {
            return Optional.empty();
        }
        if (ONE.equals(right)) {
            return Optional.of(new Rewrite(left, TIMES_ONE));
        }
        return ONE.equals(left) ? Optional.of(new Rewrite(right, TIMES_ONE)) : Optional.empty();
    }

    // ---------------------------------------------------------------- exponentiation

    /**
     * {@code base^exponent}: a traction where the base is zero, the unit table on the four points, and
     * repeated multiplication at an integer exponent.
     * <p>
     * {@code n = 0} is not repeated multiplication of anything -- zero copies is the empty product, and
     * calling that 1 is {@code x^0 = 1}, which this theory does not have; the unit table gives the four
     * points instead, and {@code 2^0} stands. And n has to be an integer <b>as a term</b>: the pair
     * {@code (6, 3)} is a coordinate pair and not the integer 2.
     */
    public static Optional<Rewrite> power(IExpr base, IExpr exponent) {
        if (ZERO.equals(base)) {
            // 0^0 is (1, ∅) and NOT (∅, ∅). The second is the double erasure -- neither coordinate defined,
            // which is not a value at all -- and building it here made 0^0 reach 1 by discharging an erasure
            // rather than by E5. A real part of one with the traction part absent is what 0^0 is, and
            // point() then answers it with E4 and E5, which is the rule the theory actually cites.
            return Optional.of(new Rewrite(
                    absent(exponent) ? pairOf(ONE, ABSENT) : traction(exponent), BASE_ZERO));
        }
        // A base that folds to an additive unit is not read as a power of zero here: 0^0 is 1 and 0^ω is -1,
        // and for those the unit table answers rather than the power rule. Declining lets the base fold
        // first. See foldsToAnAdditiveUnit -- this is the exclusion that keeps x^-1 apart from 1÷x.
        if (foldsToAnAdditiveUnit(base)) {
            return Optional.empty();
        }
        Optional<Rewrite> unit = unitPower(base, exponent);
        if (unit.isPresent()) {
            return unit;
        }
        if (!isNonZeroInteger(exponent)) {
            return Optional.empty();
        }
        BigInteger n = ((RationalLiteral) exponent).numerator();
        if (n.signum() > 0 && combines(base)) {
            return Optional.of(new Rewrite(repeated(base, n), INTEGER_POWER));
        }
        // A NEGATIVE power is 1/y^n, and E3 grants that at base zero only. For a general base it is a
        // convention rather than a consequence, so 2^-1 stands.
        return exponentOfZero(base).map(a -> new Rewrite(traction(times(a, exponent)), NEGATIVE_POWER));
    }

    /**
     * The unit exponentiation table: the twelve cells whose base is not zero.
     * <p>
     * Exponentiation is bijective over the units, and each of the four bases gives a different permutation of
     * them. Three cells are ordinary -- {@code 1^1}, {@code (-1)^1}, {@code ω^1}, one copy of the base -- and
     * the rest are the table's own, including the {@code x^0} 4-cycle {@code 0 -> 1 -> ω -> -1 -> 0}. That
     * cycle is the first map on those four points that is not an involution and it has no fixed point, so
     * not {@code x^0 = 1}.
     * <p>
     * A lookup on sixteen cells is a table, and a table cannot be checked by reading it. What checks this one
     * is that every row is a permutation and that the base-zero row is E5, E4, the leap and E3+E9 -- which is
     * why that row is not read from here but derived, in {@link #power} and {@link #point}.
     */
    private static Optional<Rewrite> unitPower(IExpr base, IExpr exponent) {
        int b = unit(base);
        int x = unit(exponent);
        if (b < 0 || x < 0 || b == UNIT_ZERO) {
            return Optional.empty();
        }
        return Optional.of(new Rewrite(value(TABLE[b][x]), UNIT_POWER));
    }

    /**
     * Ordered {@code 0, 1, ω, -1}, the order the cycle takes them in. Read as {@code TABLE[base][exponent]}.
     * <pre>
     * 0^0=1   0^1=0   0^ω=-1  0^-1=ω
     * 1^0=ω   1^1=1   1^ω=0   1^-1=-1
     * ω^0=-1  ω^1=ω   ω^ω=1   ω^-1=0
     * -1^0=0  -1^1=-1 -1^ω=ω  -1^-1=1
     * </pre>
     */
    private static final int[][] TABLE = {
            {1, 0, 3, 2},
            {2, 1, 0, 3},
            {3, 2, 1, 0},
            {0, 3, 2, 1},
    };

    /**
     * {@code log_0(0^a) = a}, which is E8, and the unit logarithm table at the other three bases.
     * <p>
     * E8 reads the four points where the arithmetic rules will not, and that is not the axis rule being bent:
     * inverting is not arithmetic, no addition law is involved, the result is the exponent itself, and nothing
     * anywhere produces a log for this to feed.
     */
    public static Optional<Rewrite> logarithm(IExpr base, IExpr operand) {
        if (ZERO.equals(base)) {
            Optional<IExpr> exponent = exponentOfZero(operand);
            if (exponent.isPresent()) {
                return exponent.map(a -> new Rewrite(a, LOG_INVERTS));
            }
            // The inverse of point(): 1 is 0^0 by E5, and -1 is 0^ω by the leap. Neither is something
            // exponentOfZero will say, and the second is the leap read backwards, so it cites the table.
            if (ONE.equals(operand)) {
                return Optional.of(new Rewrite(ZERO, LOG_INVERTS));
            }
            return NEG_ONE.equals(operand) ? Optional.of(new Rewrite(OMEGA, UNIT_LOG)) : Optional.empty();
        }
        int b = unit(base);
        int x = unit(operand);
        if (b < 0 || x < 0) {
            return Optional.empty();
        }
        // The inverse of the table: log_b(v) is the exponent whose cell holds v. Every row is a permutation,
        // so there is exactly one.
        for (int exponent = 0; exponent < TABLE[b].length; exponent++) {
            if (TABLE[b][exponent] == x) {
                return Optional.of(new Rewrite(value(exponent), UNIT_LOG));
            }
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------- provisional, and deliberately unwired

    /**
     * The traction addition law, {@code a·0^b + c·0^d = (a+c)·a^d·c^b·0^(bd)}.
     * <p>
     * Not wired, and it cannot simply be switched on. Its derivation needs the general involution off the
     * closure set -- theory-problems.md #4 -- and a power rule at general exponents, which the theory does
     * not have in the ω-direction. But the reason it is not wired is more immediate than either: the formula
     * disagrees with the rest of the theory at three edges.
     * <h2>The erasure rule is what makes it nearly work</h2>
     * A factor mentioning an absent coordinate never got generated -- those factors are cross-terms of the
     * distribution, and a cross-term with an erased part in it was never there to distribute. Dropping them
     * rather than evaluating them is what this method does, and three edges that look fatal dissolve:
     * <pre>
     * 0 + 1      a absent, d absent   (a+c) is c, and only c^b survives     = 1
     * 1 + 1      b absent, d absent   only (a+c) survives                   = 2
     * 0^2 + 0^3  a and c absent       only 0^(bd) survives                  = 0^6
     * </pre>
     * The last of those is the older docs' mirror law, {@code 0^a + 0^b = 0^(a·b)}, so the two agree on bare
     * powers. Evaluating the dropped factors instead is what gave the wrong answers: {@code a^d} at
     * {@code d = 0} would be {@code 1^0}, which the cycle says is ω, and {@code 0^(bd)} at {@code b = d = 0}
     * would be {@code 0^(0·0)}, which is {@code 0^(0^2)} and stands.
     *
     * <h2>The edge that does not dissolve</h2>
     * At {@code b = d} only {@code 0^(bd)} survives, so {@code ω + ω} is {@code 0^((-1)(-1))}, which is
     * {@code 0^1} -- the point zero. Distributivity says {@code 2ω}, and {@link #realSum} says why: an absent
     * real part is a multiplicative erasure, and a multiplicative erasure landing in a sum leaves a residue of
     * one, not nothing. This law drops it; the residue rule keeps it as one copy. The same disagreement makes
     * the law say {@code 0 + 0 = 0}, which is the value zero acting as an additive identity -- exactly the
     * collision the older docs record against the mirror law, and it is a theory decision rather than a defect.
     */
    public static Optional<Rewrite> provisionalAddition(IExpr left, IExpr right) {
        return pair(left).flatMap(l -> pair(right).map(r -> {
            IExpr result = null;
            if (!absent(l.n()) || !absent(r.n())) {
                result = absent(l.n()) ? r.n() : absent(r.n()) ? l.n() : plus(l.n(), r.n());
            }
            if (!absent(l.n()) && !absent(r.t())) {
                result = result == null ? raise(l.n(), r.t()) : times(result, raise(l.n(), r.t()));
            }
            if (!absent(r.n()) && !absent(l.t())) {
                result = result == null ? raise(r.n(), l.t()) : times(result, raise(r.n(), l.t()));
            }
            if (!absent(l.t()) && !absent(r.t())) {
                IExpr traction = traction(times(l.t(), r.t()));
                result = result == null ? traction : times(result, traction);
            }
            return new Rewrite(result == null ? ONE : result, ADDITION_LAW);
        }));
    }

    /**
     * The additive node's sum, {@code (a, b) + (c, d) = (a+c, b·d)}.
     * <p>
     * Not wired, for one reason: the traction coordinate multiplies, and that is
     * {@code 0^b + 0^d = 0^(b·d)} -- the mirror law, which Traction-Theory.md does not adopt because it
     * makes {@code ω + ω} the point zero where distributivity makes it {@code 2ω}. Wiring this would adopt
     * it through the carrier rather than through the law, which is the same decision wearing a different
     * hat.
     * <p>
     * It is here because it is what makes the inverse testable. {@code z + inv(z)} erases in both
     * coordinates at once and lands on {@code (0,0)}: {@code 1 + (-1)} is
     * {@code (1,∅) + (-1,∅) = (0, ∅·∅)}, the double erasure, rather than a value.
     */
    public static Optional<Rewrite> provisionalAdditiveSum(AdditiveTractionLiteral l,
                                                           AdditiveTractionLiteral r) {
        IExpr real = absent(l.real()) ? r.real() : absent(r.real()) ? l.real() : plus(l.real(), r.real());
        IExpr exponent = absent(l.exponent()) ? r.exponent()
                : absent(r.exponent()) ? l.exponent() : times(l.exponent(), r.exponent());
        return Optional.of(new Rewrite(new AdditiveTractionLiteral(real, exponent), MIRROR_SUM));
    }

    /**
     * {@code 0^(0^n) = n} off the closure set: E6 read as a general involution.
     * <p>
     * Not wired. On the four points the tables have it, and there it is forced. Off them it is
     * theory-problems.md #4, the largest unpaid assumption the theory has -- E2 depends on it in the older
     * docs, and so does the addition law by the same conjugation. Wiring it also makes every value a power of
     * zero, and the axis restriction exists because that does not terminate.
     */
    public static Optional<Rewrite> provisionalInvolution(IExpr e) {
        return exponentOfZero(e).flatMap(TractionRules::exponentOfZero)
                .map(n -> new Rewrite(n, INVOLUTION));
    }

    /**
     * {@code -1·0 = ω}, conjectured, and what refutes it.
     * <p>
     * Follow it through the primitives. {@code -1·0} is {@code 0^ω · 0^1}, which is {@code 0^(ω+1)} by E1, and
     * the conjecture makes that {@code 0^-1}, so E6 gives {@code ω + 1 = -1} and hence {@code ω = -2} as an
     * exponent. Then {@code -1 = 0^ω = 0^-2}, whose reciprocal is {@code 0^2} by E3; but {@code -1} is its own
     * reciprocal, so {@code 0^2 = 0^-2}, and E6 gives {@code 2 = -2}.
     * <p>
     * So the pair {@code (-1, 1)} stands. It is not a distinguished element, and there is no rule for it.
     */
    public static Optional<Rewrite> provisionalMinusZero(IExpr e) {
        if (e instanceof TractionLiteral t && NEG_ONE.equals(t.real()) && ONE.equals(t.exponent())) {
            return Optional.of(new Rewrite(OMEGA, MINUS_ZERO));
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------- the four units, as an index

    private static final int UNIT_ZERO = 0;

    /** Which of the four units this term is, in the cycle's order, or -1 for anything else. */
    private static int unit(IExpr e) {
        if (ZERO.equals(e)) {
            return 0;
        }
        if (ONE.equals(e)) {
            return 1;
        }
        if (OMEGA.equals(e)) {
            return 2;
        }
        return NEG_ONE.equals(e) ? 3 : -1;
    }

    private static IExpr value(int unit) {
        return switch (unit) {
            case 0 -> ZERO;
            case 1 -> ONE;
            case 2 -> OMEGA;
            default -> NEG_ONE;
        };
    }

    // ---------------------------------------------------------------- the exponent arithmetic

    private record Pair(IExpr n, IExpr t) {
    }

    // The exponent arithmetic is BUILT and not performed. A rule that reduced its own exponent would be doing
    // work no derivation could show -- 0·0 would arrive at 0^2 with the step from 1+1 to 2 missing -- and
    // since the derivation is the evaluator, work that cannot be shown is work that does not happen. The
    // driver reduces these on the turns that follow.

    private static IExpr plus(IExpr a, IExpr b) {
        return new AdditionOperationExpr(a, b);
    }

    private static IExpr minus(IExpr a, IExpr b) {
        return new AdditionOperationExpr(a, new NegationOperationExpr(b));
    }

    private static IExpr times(IExpr a, IExpr b) {
        return new MultiplicationOperationExpr(a, b);
    }

    private static IExpr over(IExpr a, IExpr b) {
        return new MultiplicationOperationExpr(a, new ReciprocalOperationExpr(b));
    }

    private static IExpr raise(IExpr a, IExpr b) {
        return new ExponentialOperationExpr(a, b);
    }

    /** Whether {@code b} is {@code -a} as a TERM: the negation of it, or its coordinates with the sign turned. */
    private static boolean negates(IExpr a, IExpr b) {
        if (b instanceof NegationOperationExpr(IExpr of) && of.equals(a)) {
            return true;
        }
        if (a instanceof NegationOperationExpr(IExpr of) && of.equals(b)) {
            return true;
        }
        return a instanceof RationalLiteral(BigInteger an, BigInteger ad)
                && b instanceof RationalLiteral(BigInteger bn, BigInteger bd)
                // A zero exponent is its own negation, and 1·1 is not an erasure.
                && an.signum() != 0
                && ad.equals(bd)
                && an.negate().equals(bn);
    }

    /**
     * Whether writing the copies out will actually join them back up.
     * <p>
     * {@code x^2} is not improved by becoming {@code x·x} -- the power is the better spelling of the same
     * term, and {@code (x^2)^2} would unfold to four x's for nothing. So the copies are only written where
     * something multiplies them: a coordinate, a pair, or a value that reads as a power of zero.
     */
    private static boolean combines(IExpr base) {
        return base instanceof RationalLiteral || base instanceof TractionLiteral
                || exponentOfZero(base).isPresent();
    }

    /**
     * {@code base} multiplied by itself n times, which is what a positive integer power IS.
     * <p>
     * No traction reading is needed for this and none is taken: it is the meaning of the notation, and it is
     * also the whole proof of the integer power rule. Where the base is {@code 0^a} the product rule turns
     * the n copies into {@code 0^(na)} on its own, so the two are one mechanism -- {@code (0^2)^3} lands on
     * {@code 0^6} by the same path that takes {@code 2^3} to 8.
     */
    private static IExpr repeated(IExpr base, BigInteger n) {
        IExpr out = base;
        for (BigInteger i = BigInteger.ONE; i.compareTo(n) < 0; i = i.add(BigInteger.ONE)) {
            out = new MultiplicationOperationExpr(out, base);
        }
        return out;
    }

    private static boolean isNonZeroInteger(IExpr e) {
        return e instanceof RationalLiteral r && r.isInteger() && !r.isZero();
    }
}
