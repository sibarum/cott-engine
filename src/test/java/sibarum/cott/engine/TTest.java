package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.ratio.T;
import sibarum.cott.engine.rational.expr.RationalLiteral;

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
        assertEquals(EIGHTH, ZERO_OMEGA.theta(), TOLERANCE);        // 0w  at 1+i, once resolved
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
        assertEquals(ONE, OMEGA.plus(OMEGA).resolved());
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

    /** {@code x÷x = 1} is applied where the pair is read as a value, and to this pair only. */
    @Test
    void resolvingIsTheOnlyNormalisation() {
        assertEquals(ONE, ZERO_OMEGA.resolved());
        assertEquals(at(-1, -1), at(-1, -1).resolved());
        assertEquals(at(2, 2), at(2, 2).resolved());
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
