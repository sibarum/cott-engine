package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;

import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.NEG_ONE;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.OMEGA;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.ONE;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.ZERO;

class ProjectiveRationalLiteralTest {

    private static ProjectiveRationalLiteral at(int numerator, int denominator) {
        return ProjectiveRationalLiteral.of(numerator, denominator);
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

    @Test
    void negationCarriesTheSignOnTheNumerator() {
        assertEquals(NEG_ONE, ONE.negated());
        assertEquals(ONE, NEG_ONE.negated());
        assertEquals(ZERO, ZERO.negated());
        assertEquals(at(-1, 0), OMEGA.negated());
    }

    @Test
    void zeroAndOmegaAreEachOthersReciprocal() {
        assertEquals(OMEGA, ZERO.reciprocal());
        assertEquals(ZERO, OMEGA.reciprocal());
    }

    @Test
    void reciprocalTradesTheCoordinates() {
        assertEquals(ONE, ONE.reciprocal());
        assertEquals(at(3, 2), at(2, 3).reciprocal());
        assertEquals(at(1, -1), NEG_ONE.reciprocal());
    }

    @Test
    void multiplicationMultipliesBothCoordinates() {
        assertEquals(at(10, 21), at(2, 3).times(at(5, 7)));
    }

    @Test
    void zeroOverZeroIsOne() {
        assertEquals(ONE, at(0, 0));
    }

    @Test
    void zeroTimesOmegaIsOne() {
        assertEquals(ONE, ZERO.times(OMEGA));
    }

    @Test
    void dividingZeroByItselfIsOne() {
        assertEquals(ONE, ZERO.dividedBy(ZERO));
        assertEquals(ONE, ONE.dividedBy(ONE));
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
    void omegaSharesZerosShadow() {
        assertEquals(ZERO.evaluate(), OMEGA.evaluate());
        assertEquals(Optional.of(0.0), OMEGA.evaluate());
        assertEquals(Optional.of(0.0), at(-1, 0).evaluate());
    }

    @Test
    void aLiteralIsAlreadySimplified() {
        assertSame(ONE, ONE.simplify());
    }

    @Test
    void anOperationWithSomethingElseStandsAsANode() {
        TractionLiteral traction = new TractionLiteral(ZERO, ONE);
        assertInstanceOf(AdditionOperationExpr.class, ONE.plus(traction));
    }

    @Test
    void coordinatesOutgrowAFixedWidthWithoutWrapping() {
        BigInteger huge = BigInteger.valueOf(Long.MAX_VALUE);
        ProjectiveRationalLiteral value = new ProjectiveRationalLiteral(huge, huge);
        IExpr squared = value.times(value);

        assertEquals(new ProjectiveRationalLiteral(huge.multiply(huge), huge.multiply(huge)), squared);
        assertEquals(Optional.of(1.0), squared.evaluate());
    }

    @Test
    void denominatorsOnlyGrowAndTheValueStaysExact() {
        IExpr sum = ZERO;
        for (int denominator = 2; denominator < 40; denominator++) {
            sum = sum.plus(at(0, denominator));
        }

        assertEquals(Optional.of(0.0), sum.evaluate());
        assertInstanceOf(ProjectiveRationalLiteral.class, sum);
        assertEquals(0, ((ProjectiveRationalLiteral) sum).numerator().signum());
    }

    @Test
    void aShadowIsReadOffTheCoordinatesAndNotOffTheirDoubles() {
        BigInteger tenToThe400 = BigInteger.TEN.pow(400);
        assertEquals(Optional.of(1.0), new ProjectiveRationalLiteral(tenToThe400, tenToThe400).evaluate());
        assertEquals(Optional.of(0.5), new ProjectiveRationalLiteral(tenToThe400, tenToThe400.multiply(BigInteger.TWO)).evaluate());
    }
}
