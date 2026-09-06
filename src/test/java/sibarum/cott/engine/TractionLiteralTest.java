package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
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

    @Test
    void anExponentCanBeAnyExpression() {
        IExpr sum = new AdditionOperationExpr(ONE, ZERO);
        assertEquals(new TractionLiteral(ZERO, at(1, 1)), new TractionLiteral(ZERO, sum).simplify());
    }

    @Test
    void thePartsSimplifyButNoRuleFiresBetweenThem() {
        TractionLiteral traction = new TractionLiteral(ZERO, new TractionLiteral(ZERO, at(2, 1)));

        assertEquals(traction, traction.simplify());
        assertEquals(traction.simplify(), traction.simplify().simplify());
        assertEquals(Optional.empty(), traction.evaluate());
    }

    /**
     * This used to be {@code 0^1 · 0^1}, chosen because nothing could answer it. E1 answers it now — that is
     * phase 2 — so the term that stands here is one the theory has genuinely not settled: an exponent sum that
     * is an erasure, which is Problem 1.
     */
    @Test
    void anOperationTheTheoryHasNotSettledStandsAsATerm() {
        TractionLiteral zero = new TractionLiteral(ZERO, ONE);
        TractionLiteral omega = new TractionLiteral(ZERO, at(-1, 1));
        IExpr product = zero.times(omega);

        assertEquals(product, product.simplify());
        assertEquals(product.simplify(), product.simplify().simplify());
    }

    @Test
    void promotionReadsARationalAsATractionAtTheFirstPower() {
        ProjRationalToTractionPromotionRule rule = new ProjRationalToTractionPromotionRule();

        assertTrue(rule.isApplicableFor(at(2, 3)));
        assertFalse(rule.isApplicableFor(new TractionLiteral(ZERO, ONE)));
        assertEquals(new TractionLiteral(at(2, 3), ONE), rule.apply(at(2, 3)));
    }
}
