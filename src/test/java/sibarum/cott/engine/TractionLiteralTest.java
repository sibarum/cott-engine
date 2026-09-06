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
     * This used to be {@code 0^1 · 0^1}, chosen because nothing could answer it. E1 answers it now — that is
     * phase 2 — so the term that stands here is one the theory has genuinely not settled: an exponent sum that
     * is an erasure, which is Problem 1.
     */
    @Test
    void anOperationTheTheoryHasNotSettledStandsAsATerm() {
        // 0^1 and 0^-1 are the points zero and omega, which they fold to first; their product is the erasure
        // and stands there.
        IExpr product = new TractionLiteral(ZERO, ONE).times(new TractionLiteral(ZERO, at(-1, 1)));
        IExpr stood = product.simplify();

        assertEquals(new MultiplicationOperationExpr(ZERO, at(1, 0)), stood);
        assertEquals(stood, stood.simplify());
    }

    @Test
    void promotionReadsARationalAsATractionAtTheFirstPower() {
        ProjRationalToTractionPromotionRule rule = new ProjRationalToTractionPromotionRule();

        assertTrue(rule.isApplicableFor(at(2, 3)));
        assertFalse(rule.isApplicableFor(new TractionLiteral(ZERO, ONE)));
        assertEquals(new TractionLiteral(at(2, 3), ONE), rule.apply(at(2, 3)));
    }
}
