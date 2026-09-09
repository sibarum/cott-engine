package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;

import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sibarum.cott.engine.rational.expr.RationalLiteral.NEG_ONE;
import static sibarum.cott.engine.rational.expr.RationalLiteral.ONE;
import static sibarum.cott.engine.rational.expr.RationalLiteral.ZERO;

class RationalLiteralTest {

    private static RationalLiteral at(int numerator, int denominator) {
        return RationalLiteral.of(numerator, denominator);
    }

    @Test
    void coordinatesAreNotAnEquivalenceClass() {
        assertNotEquals(at(1, 2), at(2, 4));
        assertEquals(at(1, 2).evaluate(), at(2, 4).evaluate());
    }

    @Test
    void additionCrossMultiplies() {
        assertEquals(at(5, 6), at(1, 2).plus(at(1, 3)));
        assertEquals(at(2, 1), ONE.plus(ONE));
    }

    @Test
    void additionDoesNotReduceWhatItProduces() {
        assertEquals(at(2, 2), at(1, 2).plus(at(1, 2)));
        // and it does not INFLATE what it produces either: a common denominator is used where there is one,
        // so adding a value to itself and doubling it by multiplication land on the same coordinates.
        assertEquals(at(1, 2).plus(at(1, 2)), at(2, 1).times(at(1, 2)));
    }

    /**
     * The sign goes on the numerator, and there is nowhere else for it to go.
     *
     * <p>The old pair had to put it on the denominator at a zero numerator, so that {@code -0} was
     * {@code (0,-1)} and not {@code (0,1)}. That job is gone: the rational zero is an exponent or the point
     * zero's spelling, {@code -0} is {@code -1·0} and lives in the traction pair, and the exponent zero has no
     * orientation to carry.
     */
    @Test
    void negationTurnsTheNumerator() {
        assertEquals(NEG_ONE, ONE.negated());
        assertEquals(ONE, NEG_ONE.negated());
        assertEquals(at(-2, 3), at(2, 3).negated());

        assertEquals(ZERO, ZERO.negated());
        assertEquals(ZERO, ZERO.negated().negated());
    }

    @Test
    void reciprocalTradesTheCoordinates() {
        assertEquals(ONE, ONE.reciprocal());
        assertEquals(at(3, 2), at(2, 3).reciprocal());
        assertEquals(at(1, -1), NEG_ONE.reciprocal());
    }

    /**
     * {@code 1÷0} is not arithmetic here. Omega is {@code 0^-1}, which the traction pair spells, so the
     * coordinate layer leaves the term standing and E9 answers it -- in view of the derivation, which is
     * where an axiom being applied belongs.
     */
    @Test
    void theReciprocalOfZeroStandsForE9() {
        assertInstanceOf(ReciprocalOperationExpr.class, ZERO.reciprocal());
        assertEquals(TractionLiteral.OMEGA, ZERO.reciprocal().simplify());
    }

    /** There is no {@code (0,0)}, and no rewrite of it to one: a zero denominator is not a rational. */
    @Test
    void aZeroDenominatorIsNotARational() {
        assertThrows(IllegalArgumentException.class, () -> at(1, 0));
        assertThrows(IllegalArgumentException.class, () -> at(0, 0));
    }

    @Test
    void multiplicationMultipliesBothCoordinates() {
        assertEquals(at(10, 21), at(2, 3).times(at(5, 7)));
    }

    @Test
    void dividingByItselfLandsOnTheValueOneWithoutLandingOnTheLiteralOne() {
        IExpr quotient = at(2, 3).dividedBy(at(2, 3));
        assertEquals(at(6, 6), quotient);
        assertNotEquals(ONE, quotient);
        assertEquals(Optional.of(1.0), quotient.evaluate());
    }

    @Test
    void subtractingSomethingFromItselfLandsOnTheValueZero() {
        IExpr difference = at(2, 3).minus(at(2, 3));
        assertEquals(at(0, 3), difference);
        assertEquals(Optional.of(0.0), difference.evaluate());
    }

    @Test
    void evaluateDividesTheCoordinates() {
        assertEquals(Optional.of(1.0), ONE.evaluate());
        assertEquals(Optional.of(0.5), at(1, 2).evaluate());
        assertEquals(Optional.of(-0.5), at(-1, 2).evaluate());
        assertEquals(Optional.of(0.0), ZERO.evaluate());
    }

    @Test
    void aLiteralIsAlreadySimplified() {
        assertSame(ONE, ONE.simplify());
    }

    @Test
    void anOperationWithSomethingElseStandsAsANode() {
        assertInstanceOf(AdditionOperationExpr.class, ONE.plus(TractionLiteral.of(at(2, 1))));
    }

    @Test
    void coordinatesOutgrowAFixedWidthWithoutWrapping() {
        BigInteger huge = BigInteger.valueOf(Long.MAX_VALUE);
        RationalLiteral value = new RationalLiteral(huge, huge);
        IExpr squared = value.times(value);

        assertEquals(new RationalLiteral(huge.multiply(huge), huge.multiply(huge)), squared);
        assertEquals(Optional.of(1.0), squared.evaluate());
    }

    @Test
    void denominatorsOnlyGrowAndTheValueStaysExact() {
        IExpr sum = ZERO;
        for (int denominator = 2; denominator < 40; denominator++) {
            sum = sum.plus(at(0, denominator));
        }

        assertEquals(Optional.of(0.0), sum.evaluate());
        assertInstanceOf(RationalLiteral.class, sum);
        assertEquals(0, ((RationalLiteral) sum).numerator().signum());
    }

    @Test
    void aShadowIsReadOffTheCoordinatesAndNotOffTheirDoubles() {
        BigInteger tenToThe400 = BigInteger.TEN.pow(400);
        assertEquals(Optional.of(1.0), new RationalLiteral(tenToThe400, tenToThe400).evaluate());
        assertEquals(Optional.of(0.5),
                new RationalLiteral(tenToThe400, tenToThe400.multiply(BigInteger.TWO)).evaluate());
    }
}
