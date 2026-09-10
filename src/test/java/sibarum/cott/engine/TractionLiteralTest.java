package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;
import sibarum.cott.engine.traction.expr.RationalToTractionPromotionRule;
import sibarum.cott.engine.traction.expr.TractionLiteral;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.engine.rational.expr.RationalLiteral.NEG_ONE;
import static sibarum.cott.engine.rational.expr.RationalLiteral.ONE;
import static sibarum.cott.engine.rational.expr.RationalLiteral.ZERO;

class TractionLiteralTest {

    private static RationalLiteral at(int numerator, int denominator) {
        return RationalLiteral.of(numerator, denominator);
    }

    @Test
    void anExponentCanBeATraction() {
        TractionLiteral zeroSquared = TractionLiteral.of(at(2, 1));
        TractionLiteral nested = TractionLiteral.of(zeroSquared);

        assertEquals(zeroSquared, nested.exponent());
        assertSame(nested, nested.simplify());
    }

    /** An exponent is reduced wherever it stands, and 0^1 is then the point zero by E4. */
    @Test
    void anExponentCanBeAnyExpression() {
        IExpr sum = new AdditionOperationExpr(ONE, ZERO);
        // The exponent 1+0 is 1, since 0 is invariant under addition, and 0^1 is then the point zero by E4.
        assertEquals(ZERO, TractionLiteral.of(sum).simplify());
        assertEquals(TractionLiteral.of(at(2, 1)),
                TractionLiteral.of(new AdditionOperationExpr(ONE, ONE)).simplify());
    }

    @Test
    void thePartsSimplifyButNoRuleFiresBetweenThem() {
        TractionLiteral traction = TractionLiteral.of(TractionLiteral.of(at(2, 1)));

        assertEquals(traction, traction.simplify());
        assertEquals(traction.simplify(), traction.simplify().simplify());
        assertEquals(Optional.empty(), traction.evaluate());
    }

    /**
     * {@code 0^1 · 0^-1} is the multiplicative erasure and discharges to 1.
     *
     * <p>This test has been rewritten three times, which is the interesting part. It was {@code 0^1 · 0^1},
     * chosen in phase 1 because nothing could answer it, until E1 could. Then it was this product, standing,
     * because Problem 1 was open. Then w became 1÷0 and the product became a value times its own reciprocal.
     * It reads the same in this carrier, where w is the pair {@code (1, -1)} rather than a coordinate.
     */
    @Test
    void anOperationTheTheoryHasSinceSettled() {
        IExpr product = TractionLiteral.of(ONE).times(TractionLiteral.OMEGA);
        assertEquals(ONE, product.simplify());
    }

    /**
     * The two multiplicative units, as the pair holds them: {@code (1,1)} folds to the point zero and
     * {@code (1,-1)} IS omega and stays where it is.
     */
    @Test
    void thePointsThePairHolds() {
        assertEquals(ZERO, TractionLiteral.of(ONE).simplify());
        assertSame(TractionLiteral.OMEGA, TractionLiteral.OMEGA.simplify());
        assertEquals(NEG_ONE, TractionLiteral.of(TractionLiteral.OMEGA).simplify());   // 0^ω = -1, the leap
    }

    /**
     * {@code -0} is the pair {@code (-1, 1)}, and it stands.
     *
     * <p>It is {@code -1·0} and the theory has not resolved what that product is, so no rule reduces it and
     * it is not one of the four units. Notably it is not the point zero -- a carrier that answered
     * {@code -0 = 0} would be saying negation does nothing there -- and not omega either, which
     * {@code TractionRules.provisionalMinusZero} records along with what refutes it.
     */
    @Test
    void minusZeroIsAProductThatStands() {
        TractionLiteral minusZero = new TractionLiteral(NEG_ONE, ONE);

        assertSame(minusZero, minusZero.simplify());
        assertEquals(minusZero, new MultiplicationOperationExpr(NEG_ONE, ZERO).simplify());
        // and the negation node lifts it: the literal has no sign to turn, so E4 has to be used first.
        assertEquals(minusZero, new NegationOperationExpr(ZERO).simplify());
    }

    /**
     * A zero real part is the absence marker, so {@code (0, 2)} is {@code 0^2} and is already where it
     * belongs. What rolls into the exponent is a real part at a zero numerator AWAY from {@code (0,1)}: that
     * is a multiple of the point zero rather than an absence, and {@code 0÷2·0^2} is {@code (1÷2)·0^3}.
     */
    @Test
    void anAbsentRealPartIsNotARolledUpOne() {
        TractionLiteral zeroSquared = TractionLiteral.of(at(2, 1));
        assertSame(zeroSquared, zeroSquared.simplify());
        assertEquals(new TractionLiteral(at(1, 2), at(3, 1)),
                new TractionLiteral(at(0, 2), at(2, 1)).simplify());
    }

    /**
     * Multiplying by the point zero keeps the other factor, because the marker is skipped rather than
     * multiplied: {@code 2·0} is {@code (2, 1)}. Were it computed, the 2 would be annihilated and dividing by
     * zero would prove 2 = 1.
     */
    @Test
    void anAbsentCoordinateIsSkippedAndNotComputedWith() {
        assertEquals(TractionLiteral.of(at(2, 1)), new MultiplicationOperationExpr(ZERO, ZERO).simplify());
        assertEquals(new TractionLiteral(at(2, 1), ONE),
                new MultiplicationOperationExpr(at(2, 1), ZERO).simplify());
        assertEquals(TractionLiteral.of(at(3, 1)),
                new MultiplicationOperationExpr(ZERO, TractionLiteral.of(at(2, 1))).simplify());
    }

    /**
     * A real part of exactly one is the multiplicative identity and collapses to the marker, so a value has
     * one spelling: {@code 1·0^-1} is omega and not a second pair beside it.
     */
    @Test
    void aRealPartOfOneCollapsesToTheMarker() {
        assertEquals(TractionLiteral.OMEGA, new TractionLiteral(ONE, NEG_ONE).simplify());
        assertEquals(ZERO, new TractionLiteral(ONE, ONE).simplify());
        assertEquals(ONE, new TractionLiteral(ONE, ZERO).simplify());
    }

    @Test
    void promotionReadsARationalAsATractionAtTheZeroPower() {
        RationalToTractionPromotionRule rule = new RationalToTractionPromotionRule();

        assertTrue(rule.isApplicableFor(at(2, 3)));
        assertFalse(rule.isApplicableFor(TractionLiteral.of(ONE)));
        assertEquals(new TractionLiteral(at(2, 3), ZERO), rule.apply(at(2, 3)));
    }
}
