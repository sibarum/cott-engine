package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.ratio.T;
import sibarum.cott.engine.rational.expr.RationalLiteral;

import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.engine.ratio.T.NEG_ONE;
import static sibarum.cott.engine.ratio.T.OMEGA;
import static sibarum.cott.engine.ratio.T.ONE;
import static sibarum.cott.engine.ratio.T.OTIMES_UNIT;
import static sibarum.cott.engine.ratio.T.ZERO;
import static sibarum.cott.engine.ratio.T.ZERO_OMEGA;

/** docs/Traction-Model.md, one test per line of it. */
class TTest {

    private static final double TOLERANCE = 1e-12;
    private static final double QUARTER = Math.PI / 2;
    private static final double EIGHTH = Math.PI / 4;

    private static T at(long p, long q) {
        return T.of(p, q);
    }

    @Test
    void theFourValuesAreTheCoordinatesTheModelGivesThem() {
        assertEquals(at(0, 1), ZERO);
        assertEquals(at(1, 1), ONE);
        assertEquals(at(-1, 1), NEG_ONE);
        assertEquals(at(1, 0), OMEGA);
    }

    /**
     * The table's angle column, which is the {@code q+pi} column read as a position.
     *
     * <p>This is the reading that separates the nine rows. Four of them have a zero tangent or none at all,
     * and it is the angle that says {@code 0} and {@code -0} are half a turn apart rather than the same
     * place written twice.
     */
    @Test
    void theAngleIsTheArgumentOfThePoint() {
        assertEquals(-EIGHTH, NEG_ONE.theta(), TOLERANCE);          // -1  at 1-i
        assertEquals(0.0, ZERO.theta(), TOLERANCE);                 //  0  at 1
        assertEquals(QUARTER, OMEGA.theta(), TOLERANCE);            //  w  at i
        assertEquals(EIGHTH, ONE.theta(), TOLERANCE);               //  1  at 1+i
        assertTrue(Double.isNaN(ZERO_OMEGA.theta()));               // 0w  the origin, at no angle
        assertEquals(-QUARTER, at(-1, 0).theta(), TOLERANCE);       // -w  at -i
        assertEquals(Math.PI, at(0, -1).theta(), TOLERANCE);        // -0  at -1
        assertEquals(3 * EIGHTH, at(1, -1).theta(), TOLERANCE);     // _1  at -1+i
        assertEquals(-3 * EIGHTH, at(-1, -1).theta(), TOLERANCE);   // -_1 at -1-i
    }

    @Test
    void theRatioIsTheTangentOfThatAngle() {
        assertEquals(Optional.of(-1.0), NEG_ONE.evaluate());
        assertEquals(Optional.of(0.0), ZERO.evaluate());
        assertEquals(Optional.of(1.0), ONE.evaluate());
        assertEquals(Math.tan(at(1, 2).theta()), at(1, 2).evaluate().orElseThrow(), TOLERANCE);
    }

    /** A quarter turn has no tangent to report, so there is no real value -- rather than an invented one. */
    @Test
    void theQuarterTurnHasNoRealValue() {
        assertTrue(OMEGA.isQuarterTurn());
        assertTrue(at(-1, 0).isQuarterTurn());
        assertEquals(Optional.empty(), OMEGA.evaluate());
        assertEquals(Optional.empty(), at(-1, 0).evaluate());
        assertFalse(ZERO_OMEGA.isQuarterTurn());
    }

    /**
     * The table's projection column, which is total where the value reading is not.
     *
     * <p>The two values with no magnitude keep their sign through it, which is what the signed zero and the
     * two infinities are for: {@code 0} and {@code -0} are half a turn apart and stay apart here.
     */
    @Test
    void theProjectionIsTheTableColumn() {
        assertEquals(0.0, ZERO.projection());
        assertEquals(1.0, ONE.projection());
        assertEquals(-1.0, NEG_ONE.projection());
        assertEquals(Double.POSITIVE_INFINITY, OMEGA.projection());
        assertEquals(-1.0, at(1, -1).projection());          // _1
        assertEquals(1.0, at(-1, -1).projection());          // -_1
        assertEquals(Double.NEGATIVE_INFINITY, at(-1, 0).projection());
        assertTrue(Double.isNaN(ZERO_OMEGA.projection()));   // no number, and not taken to one
        assertEquals(0.5, at(1, 2).projection());
    }

    /** A signed zero is a distinct projection, and the one the formatter would lose. */
    @Test
    void theZerosProjectApart() {
        assertEquals(1, Math.copySign(1, ZERO.projection()));
        assertEquals(-1, Math.copySign(1, at(0, -1).projection()));
        assertEquals(-1, Math.copySign(1, at(0, -7).projection()));
    }

    /** The exact division, so a coordinate larger than a double does not ask what infinity over infinity is. */
    @Test
    void theProjectionDividesTheCoordinatesAndNotTheirShadows() {
        BigInteger huge = BigInteger.TEN.pow(400);
        assertEquals(1.0, new T(huge, huge).projection());
        assertEquals(2.0, new T(huge.multiply(BigInteger.TWO), huge).projection());
    }

    /**
     * The angle is taken from the ratio, so a pair whose coordinates have both outgrown a double still
     * stands where it stands.
     *
     * <p>Read off the two coordinates as doubles it was infinity over infinity, which atan2 answers at 45
     * degrees whatever was asked -- so these two, a hair off zero and a hair off the quarter turn, both came
     * back at the same eighth turn.
     */
    @Test
    void theAngleSurvivesCoordinatesLargerThanADouble() {
        BigInteger e400 = BigInteger.TEN.pow(400);
        BigInteger e500 = BigInteger.TEN.pow(500);
        assertEquals(0.0, Math.toDegrees(new T(e400, e500).theta()), 1e-9);
        assertEquals(90.0, Math.toDegrees(new T(e500, e400).theta()), 1e-9);
        assertEquals(45.0, Math.toDegrees(new T(e400, e400).theta()), 1e-9);
        assertEquals(-90.0, Math.toDegrees(new T(e500.negate(), e400).theta()), 1e-9);
        assertEquals(180.0, Math.toDegrees(new T(BigInteger.ZERO, e400.negate()).theta()), 1e-9);
    }

    /** Two readings, and they are meant to differ: one says nothing where the other says infinity. */
    @Test
    void theValueReadingAndTheProjectionPartAtTheQuarterTurn() {
        assertEquals(Optional.empty(), OMEGA.evaluate());
        assertEquals(Double.POSITIVE_INFINITY, OMEGA.projection());
        assertEquals(Optional.of(-1.0), NEG_ONE.evaluate());
        assertEquals(-1.0, NEG_ONE.projection());
    }

    @Test
    void coordinatesAreNotAnEquivalenceClass() {
        assertNotEquals(at(1, 2), at(2, 4));
        assertEquals(at(1, 2).evaluate(), at(2, 4).evaluate());
    }

    /**
     * The sign is not moved into the numerator, so all four placements stay distinct -- which is what makes
     * this oriented. {@code T(1,-1)} and {@code T(-1,1)} are one ratio at two positions, half a turn apart.
     */
    @Test
    void theSignStaysWhereItWasWritten() {
        assertNotEquals(at(1, -1), at(-1, 1));
        assertEquals(at(1, -1).evaluate(), at(-1, 1).evaluate());
        assertEquals(Math.PI, at(1, -1).theta() - at(-1, 1).theta(), TOLERANCE);
    }

    @Test
    void valuePositionAdditionCrossMultiplies() {
        assertEquals(at(5, 6), at(1, 2).plus(at(1, 3)));
        assertEquals(at(2, 1), ONE.plus(ONE));
        assertEquals(ONE, ZERO.plus(ONE));
    }

    /**
     * The law as the model writes it, cross-multiplying whether or not there is already a common
     * denominator.
     *
     * <p>{@code RationalLiteral.plus} takes the other option and uses the common denominator when it has
     * one, so {@code 1÷2 + 1÷2} lands on {@code (2,2)} there and on {@code (4,4)} here. Both name the same
     * ratio; the coordinates differ, and since nothing reduces the difference is permanent. This pins which
     * of the two this type does, and is not a claim about which the model wants.
     */
    @Test
    void additionDoesNotLookForACommonDenominator() {
        assertEquals(at(4, 4), at(1, 2).plus(at(1, 2)));
        assertEquals(RationalLiteral.of(2, 2), RationalLiteral.of(1, 2).plus(RationalLiteral.of(1, 2)));
    }

    /**
     * {@code w + w} lands on {@code T(0,0)} -- every cross term picks up the zero denominator -- and the
     * table reads that as 1. Pinned because it is the sum most worth being able to point at, not because
     * anything here decides what it ought to be.
     */
    @Test
    void omegaPlusOmegaIsTheZeroOmegaPair() {
        assertEquals(ZERO_OMEGA, OMEGA.plus(OMEGA));
        assertTrue(OMEGA.plus(OMEGA).isZeroOmega());
    }

    @Test
    void valuePositionMultiplicationTakesTheCoordinatesApart() {
        assertEquals(at(3, 8), at(1, 2).times(at(3, 4)));
        assertEquals(at(1, 2), at(1, 2).times(ONE));
        assertEquals(ZERO_OMEGA, ZERO.times(OMEGA));
    }

    /** The mediant, and the sum that {@code 0^a · 0^b = 0^(a⊕b)} uses. */
    @Test
    void theExponentPositionSumIsTheMediant() {
        assertEquals(at(2, 5), at(1, 2).oplus(at(1, 3)));
        assertEquals(at(1, 1), ZERO.oplus(OMEGA));
    }

    @Test
    void zeroOmegaIsTheUnitOfTheMediantAndIsWritable() {
        assertEquals(at(3, 4), at(3, 4).oplus(ZERO_OMEGA));
        assertEquals(ZERO_OMEGA, at(3, 4).oplus(at(3, 4).oplusInverse()));
        // and it survives being constructed, which is what lets the line above be written at all
        assertTrue(ZERO_OMEGA.isZeroOmega());
        assertEquals(at(0, 0), ZERO_OMEGA);
    }

    /**
     * There is no normalisation at all, {@code x÷x} included.
     *
     * <p>{@code T(0,0)} stays where it was written, and the readings decline rather than placing it: no
     * value, no projection, no angle. The table's {@code 0÷0 --> (1:1)} is a reading, and a caller wanting
     * it applies it where the choice can be seen.
     */
    @Test
    void nothingIsNormalised() {
        assertEquals(at(0, 0), ZERO_OMEGA);
        assertEquals(Optional.empty(), ZERO_OMEGA.evaluate());
        assertTrue(Double.isNaN(ZERO_OMEGA.projection()));
        assertTrue(Double.isNaN(ZERO_OMEGA.theta()));
        // and the pairs that merely LOOK like x÷x were never touched either
        assertEquals(at(-1, -1), new T(at(-1, -1).p(), at(-1, -1).q()));
        assertEquals(Optional.of(1.0), at(2, 2).evaluate());
        assertEquals(1.0, at(-1, -1).projection());
    }

    /** {@code ⊗} multiplies the points, so the angles add. */
    @Test
    void theExponentPositionProductAddsTheAngles() {
        assertEquals(at(2, 0), ONE.otimes(ONE));
        assertEquals(QUARTER, ONE.otimes(ONE).theta(), TOLERANCE);
        assertEquals(EIGHTH + EIGHTH, ONE.otimes(ONE).theta(), TOLERANCE);
        // a quarter turn twice is a half turn, which is where -0 is
        assertEquals(at(0, -1), OMEGA.otimes(OMEGA));
        assertEquals(Math.PI, OMEGA.otimes(OMEGA).theta(), TOLERANCE);
    }

    @Test
    void theExponentPositionProductHasZeroAsItsUnit() {
        assertEquals(at(3, 4), at(3, 4).otimes(OTIMES_UNIT));
        assertEquals(ZERO, OTIMES_UNIT);
    }

    /**
     * The ⊗ inverse turns the angle back to zero. It lands on the unit's angle rather than on the unit's
     * coordinates -- {@code T(0, a²+b²)} -- because nothing reduces.
     */
    @Test
    void theExponentPositionInverseConjugatesThePoint() {
        assertEquals(at(-3, 4), at(3, 4).otimesInverse());
        assertEquals(at(0, 25), at(3, 4).otimes(at(3, 4).otimesInverse()));
        assertEquals(0.0, at(3, 4).otimes(at(3, 4).otimesInverse()).theta(), TOLERANCE);
    }

    @Test
    void thePowerTakesBothCoordinates() {
        assertEquals(at(8, 27), at(2, 3).power(3));
        assertEquals(at(2, 3).times(at(2, 3)), at(2, 3).power(2));
        assertEquals(ONE, at(2, 3).power(0));
        assertEquals(ONE, ZERO_OMEGA.power(0));
    }

    /** A negative power is not a pair of integers, and swapping the coordinates instead would reduce. */
    @Test
    void aNegativePowerIsRefusedRatherThanInverted() {
        assertThrows(IllegalArgumentException.class, () -> at(2, 3).power(-1));
    }

    /**
     * The model's exponentiation, where the exponent is an integer: the angle is scaled, and the
     * coordinates are whatever standing at that angle costs.
     */
    @Test
    void theAnglePowerScalesTheAngle() {
        assertEquals(at(3, 4), at(3, 4).otimesPower(1));
        assertEquals(at(3, 4).otimes(at(3, 4)), at(3, 4).otimesPower(2));
        assertEquals(at(2, 0), ONE.otimesPower(2));             // two eighth turns is a quarter
        assertEquals(QUARTER, ONE.otimesPower(2).theta(), TOLERANCE);
        assertEquals(at(0, -4), ONE.otimesPower(4));            // four of them is a half turn, at -0
        assertEquals(Math.PI, ONE.otimesPower(4).theta(), TOLERANCE);
        assertEquals(at(0, -1), OMEGA.otimesPower(2));          // and two quarter turns is -0 as well
    }

    /** It is total over a negative exponent, which is the whole difference from the coordinate power. */
    @Test
    void theAnglePowerTurnsBackOnANegativeExponent() {
        assertEquals(at(-3, 4), at(3, 4).otimesPower(-1));
        assertEquals(at(3, 4).otimesInverse(), at(3, 4).otimesPower(-1));
        assertEquals(-at(3, 4).theta(), at(3, 4).otimesPower(-1).theta(), TOLERANCE);
        // conjugating and then raising, or raising and then conjugating, reach the same coordinates
        assertEquals(at(3, 4).otimesInverse().otimesPower(3), at(3, 4).otimesPower(-3));
        assertEquals(at(3, 4).otimesPower(3).otimesInverse(), at(3, 4).otimesPower(-3));
    }

    /** The exponent zero is the ⊗ unit at every pair, including the one ⊗ absorbs everywhere else. */
    @Test
    void theAnglePowerIsTheExponentUnitAtZero() {
        assertEquals(OTIMES_UNIT, at(3, 4).otimesPower(0));
        assertEquals(OTIMES_UNIT, ZERO_OMEGA.otimesPower(0));
        assertEquals(ZERO_OMEGA, ZERO_OMEGA.otimesPower(3));
    }

    /**
     * Two different operations, and the model states only the angle one. {@code power} raises the
     * coordinates and stays at the value position; this one turns.
     */
    @Test
    void theAnglePowerIsNotThePowerOfTheCoordinates() {
        assertEquals(at(4, 9), at(2, 3).power(2));
        assertEquals(at(12, 5), at(2, 3).otimesPower(2));
        assertNotEquals(at(2, 3).power(2).evaluate(), at(2, 3).otimesPower(2).evaluate());
        assertEquals(2 * at(2, 3).theta(), at(2, 3).otimesPower(2).theta(), TOLERANCE);
    }

    /**
     * {@code z(T(a,b)) = T(2ab, b²−a²)}: the point squared, so the angle doubled, and on the coordinates
     * rather than only at that ratio.
     */
    @Test
    void theModelsZIsThePointSquared() {
        assertEquals(at(24, 7), at(3, 4).doubleAngle());        // 2ab = 24, b²-a² = 7
        assertEquals(at(3, 4).otimesPower(2), at(3, 4).doubleAngle());
        assertEquals(at(3, 4).otimes(at(3, 4)), at(3, 4).doubleAngle());
        assertEquals(2 * at(3, 4).theta(), at(3, 4).doubleAngle().theta(), TOLERANCE);
        assertEquals(at(0, -1), OMEGA.doubleAngle());
    }

    /** The coordinates trade places, which is what makes zero invertible. */
    @Test
    void theValuePositionInverseSwapsTheCoordinates() {
        assertEquals(OMEGA, ZERO.reciprocal());
        assertEquals(ZERO, OMEGA.reciprocal());
        assertEquals(at(4, 3), at(3, 4).reciprocal());
        assertEquals(at(3, 4), at(3, 4).reciprocal().reciprocal());
    }

    /** As an expression it combines with its own kind and stands against anything else. */
    @Test
    void aSumWithSomethingElseStands() {
        assertInstanceOf(AdditionOperationExpr.class, ONE.plus(RationalLiteral.ONE));
        assertEquals(at(2, 1), ONE.plus((IExpr) ONE));
    }
}
