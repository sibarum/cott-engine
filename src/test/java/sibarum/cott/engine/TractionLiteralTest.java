package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral;
import sibarum.cott.engine.traction.expr.ProjRationalToTractionPromotionRule;
import sibarum.cott.engine.traction.expr.TractionLiteral;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.ONE;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.ZERO;

class TractionLiteralTest {

    private static ProjectiveRationalLiteral at(int numerator, int denominator) {
        return ProjectiveRationalLiteral.of(numerator, denominator);
    }

    @Test
    void anExponentCanBeATraction() {
        TractionLiteral zeroSquared = new TractionLiteral(ZERO, at(2, 1));
        TractionLiteral nested = new TractionLiteral(ZERO, zeroSquared);

        assertEquals(zeroSquared, nested.exp());
        assertSame(nested, nested.simplify());
    }

    /** An exponent is reduced wherever it stands, and 0^1 is then the point zero by E4. */
    @Test
    void anExponentCanBeAnyExpression() {
        IExpr sum = new AdditionOperationExpr(ONE, ZERO);
        // The exponent 1+0 is 1, since 0 is invariant under addition, and 0^1 is then the point zero by E4.
        assertEquals(ZERO, new TractionLiteral(ZERO, sum).simplify());
        assertEquals(new TractionLiteral(ZERO, at(2, 1)),
                new TractionLiteral(ZERO, new AdditionOperationExpr(ONE, ONE)).simplify());
    }

    @Test
    void thePartsSimplifyButNoRuleFiresBetweenThem() {
        TractionLiteral traction = new TractionLiteral(ZERO, new TractionLiteral(ZERO, at(2, 1)));

        assertEquals(traction, traction.simplify());
        assertEquals(traction.simplify(), traction.simplify().simplify());
        assertEquals(Optional.empty(), traction.evaluate());
    }

    /**
     * {@code 0^1 · 0^-1} is the multiplicative erasure and discharges to 1.
     *
     * <p>This test has been rewritten twice, which is the interesting part. It was {@code 0^1 · 0^1}, chosen
     * in phase 1 because nothing could answer it, until E1 could. Then it was this product, standing, because
     * Problem 1 was open. Now w is 1÷0 and the product is a value times its own reciprocal.
     */
    @Test
    void anOperationTheTheoryHasSinceSettled() {
        IExpr product = new TractionLiteral(ZERO, ONE).times(new TractionLiteral(ZERO, at(-1, 1)));
        assertEquals(ONE, product.simplify());
    }

    @Test
    void promotionReadsARationalAsATractionAtTheFirstPower() {
        ProjRationalToTractionPromotionRule rule = new ProjRationalToTractionPromotionRule();

        assertTrue(rule.isApplicableFor(at(2, 3)));
        assertFalse(rule.isApplicableFor(new TractionLiteral(ZERO, ONE)));
        assertEquals(new TractionLiteral(at(2, 3), ONE), rule.apply(at(2, 3)));
    }
}
