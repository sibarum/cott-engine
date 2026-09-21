package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.ratio.T;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The structural facts about {@link T}, as counts rather than as prose.
 *
 * <p>These are the measurements docs/T-Design.md quotes. They are here, and not in a throwaway
 * program, because a number in a document that cannot be re-run is worse than no number: nobody can
 * contradict it, and it goes stale silently. Every figure in that document's law tables is asserted
 * below, so changing the arithmetic changes a test rather than only a claim.
 *
 * <h2>These are characterisation tests over fixed samples</h2>
 * The counts are properties of {@link #SPREAD}, {@link #WIDE} and {@link #RING} as written, not
 * universal theorems -- 126 of 196 means 126 of the 196 pairs drawn from a 14-element sample.
 * Editing a sample is expected to change its counts. What the counts are for is the RULE each one
 * lands on exactly: the recovery figures agree with "loses exactly where a coordinate is zero" to
 * the last case, which is the evidence that the rule is right.
 */
class TLawsTest {

    /** The nine the model names: the eight directions, then the centre. */
    private static final List<T> RING = pairs(
            0, 1, 1, 1, 1, 0, 1, -1, 0, -1, -1, -1, -1, 0, -1, 1, 0, 0);

    /** The nine, plus ordinary fractions. Used for the recovery counts. */
    private static final List<T> SPREAD = pairs(
            0, 1, 1, 1, 1, 0, 1, -1, 0, -1, -1, -1, -1, 0, -1, 1, 0, 0,
            2, 1, 3, 4, -2, 5, 7, 3, 1, 2);

    /** Wider, for the associativity sweep. */
    private static final List<T> WIDE = pairs(
            0, 1, 1, 1, 1, 0, 1, -1, 0, -1, -1, -1, -1, 0, -1, 1, 0, 0,
            1, 2, 1, 3, 1, 4, 2, 3, 3, 2, 5, 7, -2, 5, 7, -3, 4, 4);

    /** SPREAD without the 7/3, for the distributivity sweep. */
    private static final List<T> THIRTEEN = pairs(
            0, 1, 1, 1, 1, 0, 1, -1, 0, -1, -1, -1, -1, 0, -1, 1, 0, 0,
            2, 1, 3, 4, -2, 5, 1, 2);

    private static List<T> pairs(long... coordinates) {
        List<T> out = new ArrayList<>();
        for (int i = 0; i < coordinates.length; i += 2) {
            out.add(T.of(coordinates[i], coordinates[i + 1]));
        }
        return List.copyOf(out);
    }

    private static final BinaryOperator<T> PLUS = T::plus;
    private static final BinaryOperator<T> OPLUS = T::oplus;
    private static final BinaryOperator<T> TIMES = T::times;
    private static final BinaryOperator<T> OTIMES = T::otimes;

    // ---- commutativity and associativity

    @Test
    void allFourCommute() {
        for (BinaryOperator<T> op : List.of(PLUS, OPLUS, TIMES, OTIMES)) {
            for (T a : SPREAD) {
                for (T b : SPREAD) {
                    assertEquals(op.apply(a, b), op.apply(b, a));
                }
            }
        }
    }

    /**
     * Regrouping does not move the coordinates, for any of the four. Exactly, not up to reduction:
     * for {@code +} both groupings are identically {@code (adf+bcf+bde, bdf)}.
     */
    @Test
    void allFourAssociateOnTheCoordinates() {
        int triples = WIDE.size() * WIDE.size() * WIDE.size();
        assertEquals(5832, triples);
        for (BinaryOperator<T> op : List.of(PLUS, OPLUS, TIMES, OTIMES)) {
            assertEquals(0, regroupings(op, WIDE), "regroupings that moved");
        }
    }

    private static int regroupings(BinaryOperator<T> op, List<T> values) {
        int moved = 0;
        for (T a : values) {
            for (T b : values) {
                for (T c : values) {
                    if (!op.apply(op.apply(a, b), c).equals(op.apply(a, op.apply(b, c)))) {
                        moved++;
                    }
                }
            }
        }
        return moved;
    }

    // ---- the two positions differ on distributivity

    /**
     * The exponent-position pair is the Gaussian integers, so this holds for formal reasons: the
     * same four products appear on each side.
     */
    @Test
    void theExponentPositionDistributesExactly() {
        int identical = 0;
        for (T a : THIRTEEN) {
            for (T b : THIRTEEN) {
                for (T c : THIRTEEN) {
                    if (a.otimes(b.oplus(c)).equals(a.otimes(b).oplus(a.otimes(c)))) {
                        identical++;
                    }
                }
            }
        }
        assertEquals(2197, identical, "every triple, on coordinates");
    }

    /**
     * The value-position pair does not, and the failures are where a zero erases: {@code w·(0+1)}
     * is {@code w} where {@code w·0 + w·1} is the origin. The middle band is the non-reduction
     * showing -- one ratio reached at two coordinates depending on the grouping.
     */
    @Test
    void theValuePositionDistributesOnlySometimes() {
        int identical = 0;
        int sameValue = 0;
        int different = 0;
        for (T a : THIRTEEN) {
            for (T b : THIRTEEN) {
                for (T c : THIRTEEN) {
                    T left = a.times(b.plus(c));
                    T right = a.times(b).plus(a.times(c));
                    if (left.equals(right)) {
                        identical++;
                    } else if (sameProjection(left, right)) {
                        sameValue++;
                    } else {
                        different++;
                    }
                }
            }
        }
        assertEquals(1141, identical);
        assertEquals(720, sameValue);
        assertEquals(336, different);
        // the named example from the document
        assertEquals(T.OMEGA, T.OMEGA.times(T.ZERO.plus(T.ONE)));
        assertEquals(T.ZERO_OMEGA, T.OMEGA.times(T.ZERO).plus(T.OMEGA.times(T.ONE)));
    }

    private static boolean sameProjection(T a, T b) {
        return a.projection() == b.projection()
                || (Double.isNaN(a.projection()) && Double.isNaN(b.projection()));
    }

    // ---- identities

    @Test
    void theIdentitiesAreExactAndTotal() {
        for (T x : SPREAD) {
            assertEquals(x, x.plus(T.ZERO), "0 is the additive identity at " + x);
            assertEquals(x, x.times(T.ONE), "1 is the multiplicative identity at " + x);
            assertEquals(x, x.oplus(T.ZERO_OMEGA), "0w is the mediant's identity at " + x);
            assertEquals(x, x.otimes(T.OTIMES_UNIT), "0 is the exponent product's identity at " + x);
        }
    }

    /**
     * A sum with one operand on the omega axis is {@code T(a,0) + T(c,d) = T(ad, 0)}: the result
     * stays on that axis, keeps {@code a} scaled by the OTHER operand's denominator, and loses the
     * other operand's numerator entirely.
     */
    @Test
    void omegaAbsorbsAdditively() {
        for (long a : new long[]{1, 2, -1, 3}) {
            for (T other : SPREAD) {
                assertEquals(new T(BigInteger.valueOf(a).multiply(other.q()), BigInteger.ZERO),
                        T.of(a, 0).plus(other),
                        "T(" + a + ",0) + " + other);
            }
        }
        assertEquals(T.of(4, 0), T.of(3, 4).plus(T.OMEGA), "the 3 is gone, the 4 survives");
        assertEquals(T.OMEGA, T.OMEGA.plus(T.ZERO), "0 is still the identity");
        assertEquals(T.ZERO_OMEGA, T.OMEGA.plus(T.OMEGA), "both lost when d is zero too");
    }

    /**
     * The projection's four landmarks are not reserved: overflow and underflow land ordinary ratios
     * on them, so {@code +inf} does not mean omega and {@code -0} does not mean the negative zero.
     */
    @Test
    void theProjectionsLandmarksAreNotReserved() {
        BigInteger big = BigInteger.TEN.pow(400);
        assertEquals(0.0, new T(BigInteger.ONE, big).projection());
        assertEquals(T.ZERO.projection(), new T(BigInteger.ONE, big).projection());
        assertEquals(-0.0, new T(BigInteger.ONE, big.negate()).projection());
        assertEquals(Double.POSITIVE_INFINITY, new T(big, BigInteger.ONE).projection());
        assertEquals(T.OMEGA.projection(), new T(big, BigInteger.ONE).projection());
    }

    /** The two readings disagree at a signed zero, because an exact zero has no sign. */
    @Test
    void evaluateAndProjectionPartAtASignedZero() {
        assertEquals(java.util.Optional.of(0.0), T.of(0, -1).evaluate());
        assertEquals(-0.0, T.of(0, -1).projection());
        assertEquals(1, Math.copySign(1, T.of(0, -1).evaluate().orElseThrow()));
        assertEquals(-1, Math.copySign(1, T.of(0, -1).projection()));
    }

    // ---- closure over the nine

    @Test
    void onlyTheValueProductIsClosedOnTheNine() {
        assertEquals(81, onTheRing(TIMES), "* is closed: the coordinates quantise to {-1,0,1}");
        assertEquals(9, distinct(TIMES));
        assertEquals(73, onTheRing(PLUS));
        assertEquals(13, distinct(PLUS));
        assertEquals(65, onTheRing(OTIMES));
        assertEquals(13, distinct(OTIMES));
        assertEquals(49, onTheRing(OPLUS));
        assertEquals(25, distinct(OPLUS));
    }

    private static int onTheRing(BinaryOperator<T> op) {
        int on = 0;
        for (T a : RING) {
            for (T b : RING) {
                if (RING.contains(op.apply(a, b))) {
                    on++;
                }
            }
        }
        return on;
    }

    private static int distinct(BinaryOperator<T> op) {
        Set<T> seen = new LinkedHashSet<>();
        for (T a : RING) {
            for (T b : RING) {
                seen.add(op.apply(a, b));
            }
        }
        return seen.size();
    }

    // ---- where information is lost

    /**
     * Given a result and one operand, is the other recoverable exactly? This is the measurement the
     * whole question of a graded carrier rests on, so it is pinned per operation.
     *
     * <p>The counts land on the rule rather than near it: 5 of the 14 values have a zero coordinate
     * and 5 x 14 = 70 = 196 - 126; 3 have a zero denominator and 3 x 14 = 42 = 196 - 154.
     */
    @Test
    void recoveryLosesOnlyWhereACoordinateIsZero() {
        assertEquals(196, SPREAD.size() * SPREAD.size());
        assertEquals(196, recovered(OPLUS, TLawsTest::fromOplus), "the mediant never loses");
        assertEquals(182, recovered(OTIMES, TLawsTest::fromOtimes), "only against the origin");
        assertEquals(154, recovered(PLUS, TLawsTest::fromPlus), "loses at a zero denominator");
        assertEquals(126, recovered(TIMES, TLawsTest::fromTimes), "loses at either zero coordinate");
    }

    /**
     * Every recovery failure above is the operation being ambiguous, not the inverse formula giving
     * up. For each failing case a second operand is CONSTRUCTED that produces the same result.
     *
     * <p>The witness is built rather than searched for, because searching a sample is not evidence:
     * {@code (2,1)·(1,0) = (2,0)} has no partner among the fourteen values, and is ambiguous all the
     * same — every {@code (2,k)} lands there. A search would have reported 16 of the 70 failures as
     * unexplained purely because the sample is small.
     */
    @Test
    void everyRecoveryFailureIsGenuineAmbiguity() {
        assertAmbiguous(TIMES, TLawsTest::fromTimes);
        assertAmbiguous(PLUS, TLawsTest::fromPlus);
        assertAmbiguous(OTIMES, TLawsTest::fromOtimes);
        assertAmbiguous(OPLUS, TLawsTest::fromOplus);
    }

    private static void assertAmbiguous(BinaryOperator<T> op, Inverse inverse) {
        for (T a : SPREAD) {
            for (T b : SPREAD) {
                T result = op.apply(a, b);
                if (a.equals(inverse.from(result, b))) {
                    continue;
                }
                T witness = witness(op, a, b);
                assertTrue(witness != null,
                        "recovery failed at " + a + " o " + b + " but no second operand gives "
                                + result + " -- the inverse formula is too weak, not the operation");
                assertEquals(result, op.apply(witness, b));
            }
        }
    }

    /** A different operand giving the same result, if the operation cannot tell them apart. */
    private static T witness(BinaryOperator<T> op, T a, T b) {
        List<T> candidates = List.of(
                new T(a.p().add(BigInteger.ONE), a.q()),
                new T(a.p(), a.q().add(BigInteger.ONE)),
                new T(a.p().add(BigInteger.ONE), a.q().add(BigInteger.ONE)));
        for (T candidate : candidates) {
            if (!candidate.equals(a) && op.apply(candidate, b).equals(op.apply(a, b))) {
                return candidate;
            }
        }
        return null;
    }

    private interface Inverse {
        T from(T result, T known);
    }

    private static int recovered(BinaryOperator<T> op, Inverse inverse) {
        int ok = 0;
        for (T a : SPREAD) {
            for (T b : SPREAD) {
                if (a.equals(inverse.from(op.apply(a, b), b))) {
                    ok++;
                }
            }
        }
        return ok;
    }

    private static T fromOplus(T r, T k) {
        return new T(r.p().subtract(k.p()), r.q().subtract(k.q()));
    }

    private static T fromTimes(T r, T k) {
        if (k.p().signum() == 0 || k.q().signum() == 0) {
            return null;
        }
        BigInteger[] p = r.p().divideAndRemainder(k.p());
        BigInteger[] q = r.q().divideAndRemainder(k.q());
        return p[1].signum() == 0 && q[1].signum() == 0 ? new T(p[0], q[0]) : null;
    }

    private static T fromPlus(T r, T k) {
        if (k.q().signum() == 0) {
            return null;
        }
        BigInteger[] b = r.q().divideAndRemainder(k.q());
        if (b[1].signum() != 0) {
            return null;
        }
        BigInteger[] a = r.p().subtract(b[0].multiply(k.p())).divideAndRemainder(k.q());
        return a[1].signum() == 0 ? new T(a[0], b[0]) : null;
    }

    /** Gaussian division: the result times the conjugate, over the norm. */
    private static T fromOtimes(T r, T k) {
        BigInteger norm = norm(k);
        if (norm.signum() == 0) {
            return null;
        }
        T scaled = r.otimes(k.otimesInverse());
        BigInteger[] p = scaled.p().divideAndRemainder(norm);
        BigInteger[] q = scaled.q().divideAndRemainder(norm);
        return p[1].signum() == 0 && q[1].signum() == 0 ? new T(p[0], q[0]) : null;
    }

    private static BigInteger norm(T t) {
        return t.p().multiply(t.p()).add(t.q().multiply(t.q()));
    }

    /** The exponent-position inverse is conjugation, and a pair against its own conjugate is the norm. */
    @Test
    void theExponentInverseIsConjugation() {
        for (T x : SPREAD) {
            assertEquals(new T(BigInteger.ZERO, norm(x)), x.otimes(x.otimesInverse()));
        }
        assertEquals(T.of(0, 25), T.of(3, 4).otimes(T.of(3, 4).otimesInverse()));
    }

    /** The Gaussian units are the four points on the axes. */
    @Test
    void theInvertibleElementsAreTheAxisPoints() {
        List<T> invertible = new ArrayList<>();
        for (T x : RING) {
            for (T y : RING) {
                if (x.otimes(y).equals(T.OTIMES_UNIT) && !invertible.contains(x)) {
                    invertible.add(x);
                }
            }
        }
        assertEquals(List.of(T.of(0, 1), T.of(1, 0), T.of(0, -1), T.of(-1, 0)), invertible);
    }

    /**
     * A coordinate over one behaves exactly as the integer does, under every operation the four
     * formulas perform on coordinates: add, multiply, negate.
     *
     * <p>This is why {@code T(n,1) -> n} is a pruning that can be switched off without changing an
     * answer, and it is the only part of the graded design in docs/T-Design.md §5 that is a fact
     * about the type as it stands rather than about a prototype.
     */
    @Test
    void theEmbeddingOfAnIntegerPreservesTheCoordinateOperations() {
        for (long a = -4; a <= 4; a++) {
            for (long c = -4; c <= 4; c++) {
                assertEquals(T.of(a * c, 1), T.of(a, 1).times(T.of(c, 1)));
                assertEquals(T.of(a + c, 1), T.of(a, 1).plus(T.of(c, 1)));
            }
            assertEquals(T.of(-a, 1), T.of(a, 1).otimesInverse());
        }
    }

    // ---- the two exponent laws, at integer exponents

    /**
     * {@code (T^m)^n = T^(m·n)} on the coordinates, at every pair in the sample and both signs.
     *
     * <p>Exact because both sides are the same list of factors under ⊗ in a different bracketing, and ⊗
     * associates exactly here. Nothing has to cancel for this one to land.
     */
    @Test
    void theIteratedAnglePowerIsExact() {
        for (T x : SPREAD) {
            for (int m = -3; m <= 3; m++) {
                for (int n = -3; n <= 3; n++) {
                    assertEquals(x.otimesPower(m * n), x.otimesPower(m).otimesPower(n),
                            x + " raised by " + m + " then " + n);
                }
            }
        }
    }

    /**
     * {@code T^(m+n) = T^m ⊗ T^n} is exact on the coordinates until the exponents cancel, and off by a
     * power of the norm where they do.
     *
     * <p>The factor is {@code (a²+b²)^min(|m|,|n|)} exactly -- one norm per turn that went out and came
     * back -- so the law holds as written wherever the exponents point the same way, and holds up to that
     * factor everywhere else. This is {@link #theExponentInverseIsConjugation} counted over exponents: the
     * same norm that stands at {@code T(0, a²+b²)} instead of at the unit.
     *
     * <p>1234 of the 1694 cases are exact on the coordinates with no factor at all. That is more than the
     * same-sign cases, because a cancelling pass whose norm is 1 leaves nothing behind -- the four axis
     * points are the ⊗ units, and turning by one of those is free.
     */
    @Test
    void theAnglePowerSumIsExactUntilTheExponentsCancel() {
        int exact = 0;
        int scaledByTheNorm = 0;
        for (T x : SPREAD) {
            for (int m = -5; m <= 5; m++) {
                for (int n = -5; n <= 5; n++) {
                    T sides = x.otimesPower(m).otimes(x.otimesPower(n));
                    T sum = x.otimesPower(m + n);
                    int turns = (m < 0) != (n < 0) ? Math.min(Math.abs(m), Math.abs(n)) : 0;
                    BigInteger factor = norm(x).pow(turns);
                    assertEquals(new T(sum.p().multiply(factor), sum.q().multiply(factor)), sides,
                            x + " raised by " + m + " against " + n);
                    if (sides.equals(sum)) {
                        exact++;
                    } else {
                        scaledByTheNorm++;
                    }
                }
            }
        }
        assertEquals(1694, exact + scaledByTheNorm);
        assertEquals(1234, exact);
        assertEquals(460, scaledByTheNorm);
    }

    // ---- two facts about zero that the ratio cannot see

    /** A zero numerator has no sign to turn, so the sign of a zero arrives by the denominator. */
    @Test
    void theSignOfAZeroComesFromTheDenominator() {
        assertEquals(T.ZERO, T.ZERO.times(T.NEG_ONE), "0 * -1 is 0, not -0");
        assertEquals(T.of(0, -1), T.ZERO.times(T.of(1, -1)), "0 * _1 is -0");
        assertEquals(T.of(0, -1), T.ZERO.times(T.of(-1, -1)));
    }

    /** Only the multiplicative unit pins its operand; the other two units do not. */
    @Test
    void theUnitsDifferOnUniqueness() {
        assertEquals(5, solutions(T.ZERO, T.ZERO), "0x = 0 has any T(n,1)");
        assertEquals(5, solutions(T.OMEGA, T.OMEGA), "wx = w has any T(1,n)");
        assertEquals(1, solutions(T.ONE, T.ONE), "1x = 1 is unique");
        assertEquals(T.OMEGA, T.OMEGA.times(T.OMEGA), "w is one of w's own solutions");
    }

    private static int solutions(T a, T target) {
        int found = 0;
        for (int p = -2; p <= 2; p++) {
            for (int q = -2; q <= 2; q++) {
                if (a.times(T.of(p, q)).equals(target)) {
                    found++;
                }
            }
        }
        return found;
    }
}
