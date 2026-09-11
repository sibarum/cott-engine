package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.Render;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.rule.Rewrite;
import sibarum.cott.engine.base.rule.Rule;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;
import sibarum.cott.engine.traction.expr.AdditiveTractionLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;
import sibarum.cott.engine.traction.rule.TractionRules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static sibarum.cott.engine.rational.expr.RationalLiteral.NEG_ONE;
import static sibarum.cott.engine.rational.expr.RationalLiteral.ONE;
import static sibarum.cott.engine.rational.expr.RationalLiteral.ZERO;

/**
 * The additive node, {@code n + 0^t}, and the inverse that goes with it.
 *
 * <p>The two nodes are the same two coordinates under a different join, so most of what holds of one holds
 * of the other. The inverse is where they part, and that is what most of this file is about.
 */
class AdditiveTractionLiteralTest {

    private static RationalLiteral at(int numerator, int denominator) {
        return RationalLiteral.of(numerator, denominator);
    }

    /** {@code n + 0^t}. */
    private static AdditiveTractionLiteral a(IExpr real, IExpr exponent) {
        return new AdditiveTractionLiteral(real, exponent);
    }

    // ---------------------------------------------------------------- the folds

    /**
     * An absent coordinate is skipped here too, and that is the whole reason the marker survives the change
     * of join: it is skipped rather than read, so it does not matter that addition's identity is 0 while
     * multiplication's is 1. {@code 1} is {@code 1 + ∅} as readily as {@code 1 · ∅}.
     */
    @Test
    void anAbsentCoordinateIsSkipped() {
        assertEquals(ONE, a(ONE, ZERO).simplify());                          // 1 + ∅ = 1
        assertEquals(ZERO, a(ZERO, ONE).simplify());                         // ∅ + 0^1 = 0^1 = 0
        assertEquals(TractionLiteral.OMEGA, a(ZERO, NEG_ONE).simplify());    // ∅ + 0^-1 = ω
    }

    /**
     * Both absent is {@code (0,0)}, the double erasure, and standing alone it discharges the way addition
     * does -- to the point zero. Whether it should stay a member of the type instead is open.
     */
    @Test
    void bothAbsentIsTheDoubleErasure() {
        assertEquals(ZERO, a(ZERO, ZERO).simplify());
    }

    /**
     * A bare power is one value however it is written, so it folds to the multiplicative node rather than
     * standing as a second spelling beside it.
     */
    @Test
    void aBarePowerFoldsToTheMultiplicativeNode() {
        assertEquals(TractionLiteral.of(at(2, 1)), a(ZERO, at(2, 1)).simplify());
    }

    // ---------------------------------------------------------------- the inverse

    /**
     * {@code inv(n, t) = (-n, 1/t)}, which is NOT the multiplicative node's negation.
     *
     * <p>An inverse has to erase both coordinates, each under the operation acting there. Here the traction
     * parts are joined by {@code ·}, so erasing them takes the reciprocal; in {@code n·0^t} they are joined
     * by {@code +} and it takes the negation. Each node negates its own coordinate and applies the OTHER
     * operation's inverse to the traction part.
     */
    @Test
    void theAdditiveInverseReciprocatesTheTractionPart() {
        Rewrite rewrite = TractionRules.negation(a(at(2, 1), at(3, 1)), false).orElseThrow();
        assertEquals(a(at(-2, 1), at(1, 3)), rewrite.result());
        assertSame(TractionRules.ADDITIVE_INVERSE, rewrite.rule());
    }

    /** And the multiplicative node still leaves its traction part alone, which is the contrast. */
    @Test
    void theMultiplicativeNegationIsUnchanged() {
        Rewrite rewrite = TractionRules
                .negation(new TractionLiteral(at(2, 1), at(3, 1)), false).orElseThrow();
        assertEquals(new TractionLiteral(at(-2, 1), at(3, 1)), rewrite.result());
        assertSame(TractionRules.NEGATION, rewrite.rule());
    }

    /**
     * An absent real part stays absent rather than materialising a -1.
     *
     * <p>That is the other half of the same point. In a product the marker has to become {@code -1} because
     * negation IS multiplication by it and the slot is multiplicative. In a sum the marker is already the
     * identity of the operation acting there, so there is nothing to turn.
     */
    @Test
    void anAbsentRealPartHasNothingToTurn() {
        assertEquals(a(ZERO, at(1, 2)),
                TractionRules.negation(a(ZERO, at(2, 1)), false).orElseThrow().result());
    }

    /** {@code 1} is {@code (1, ∅)} and its inverse is {@code -1}, once the folds have run. */
    @Test
    void theInverseOfOneIsMinusOne() {
        assertEquals(NEG_ONE, new NegationOperationExpr(a(ONE, ZERO)).simplify());
    }

    /**
     * Omega should be its own additive inverse, because {@code 1/(-1)} is {@code -1}. <b>It is not, and
     * this test pins why.</b>
     *
     * <p>By value it is: the inverse comes back as {@code 0 + 0^(1÷-1)}, and {@code 1÷-1} is -1. By TERM it
     * is not, because {@link RationalLiteral#reciprocal()} swaps the two coordinates and so leaves the sign
     * on the DENOMINATOR -- {@code (1,-1)} where omega's own exponent is {@code (-1,1)}. Two spellings of
     * one rational, and since matching here is on terms and never on values, nothing downstream can see
     * that they are the same: the erasure guards compare denominators, so {@code ω + inv(ω)} does not
     * cancel, and {@code 0^(-1÷-1)} is not recognised as {@code 0^1}.
     *
     * <p>That is not this node's defect -- it is the carrier's, and it is older. The same spelling split
     * makes {@code 1 + 1÷(-1)} answer {@code (1÷-1)·0} where {@code 1 + (-1)÷1} answers the point zero.
     * {@link RationalLiteral}'s own documentation says the sign is carried by the numerator and that this
     * is the only place it can be; {@code reciprocal()} does not keep to that.
     *
     * <p>It is left standing rather than normalised away, because where the sign sits is one of the
     * invariants the algebra may want to track rather than erase, and normalising it here would decide
     * that. What this test records is the cost of not deciding.
     */
    @Test
    void omegaIsItsOwnAdditiveInverseByValueButNotByTerm() {
        AdditiveTractionLiteral omega = a(ZERO, NEG_ONE);
        IExpr inverse = TractionRules.negation(omega, false).orElseThrow().result();

        assertEquals(a(ZERO, at(1, -1)), inverse);      // the value is -1; the spelling is not omega's
        assertEquals("0^(1÷−1)", Render.show(inverse.simplify()));
    }

    // ---------------------------------------------------------------- the sum, which is not wired

    /**
     * {@code z + inv(z)} erases in BOTH coordinates at once and lands on {@code (0,0)}.
     *
     * <p>Not on a value. {@code 1 + (-1)} is {@code (1,∅) + (-1,∅)}, whose real parts cancel and whose
     * traction parts are both absent, so it is the double erasure -- which is what it should be, and what
     * decides what it means only when something asks it to.
     */
    @Test
    void aTermPlusItsInverseIsTheDoubleErasure() {
        AdditiveTractionLiteral one = a(ONE, ZERO);
        AdditiveTractionLiteral minusOne =
                (AdditiveTractionLiteral) TractionRules.negation(one, false).orElseThrow().result();

        Rewrite sum = TractionRules.provisionalAdditiveSum(one, minusOne).orElseThrow();
        assertEquals(ZERO, sum.result().simplify());
        assertSame(TractionRules.MIRROR_SUM, sum.rule());
    }

    /**
     * The sum is Open and the driver never reaches it, because it is the mirror law.
     *
     * <p>{@code (a,b) + (c,d) = (a+c, b·d)} closes the traction coordinate with
     * {@code 0^b + 0^d = 0^(b·d)}, which Traction-Theory.md does not adopt. Wiring it here would adopt it
     * through the carrier instead of through the law, which is the same decision in a different place.
     */
    @Test
    void theSumIsOpenAndUnwired() {
        assertEquals(Rule.Status.OPEN, TractionRules.MIRROR_SUM.status());
        assertSame(Rule.Status.PROVEN, TractionRules.ADDITIVE_INVERSE.status());

        // Two additive nodes side by side do not combine: nothing in the driver produces MIRROR_SUM.
        IExpr standing = a(ONE, ONE).plus(a(ONE, NEG_ONE));
        assertEquals(standing, standing.simplify());
    }

    // ---------------------------------------------------------------- what the node is for

    /**
     * The 45° positions, as values rather than as standing sums.
     *
     * <p>This is what the node buys. Under {@code n·0^t} the pair {@code (1,1)} is collapsed to
     * {@code (0,1)} by {@code x · 1 = x}, so {@code 1 + 0} can only be held as an unreduced sum -- and a
     * standing sum is not something a differential can be taken of, where {@code f(x+0)} needs a value.
     */
    @Test
    void the45DegreePositionsAreValues() {
        assertEquals("1+0", Render.show(a(ONE, ONE).simplify()));
        assertEquals("1+ω", Render.show(a(ONE, NEG_ONE).simplify()));

        // and they are literals, not sums: simplification leaves them where they are
        assertEquals(a(ONE, ONE), a(ONE, ONE).simplify());
        assertEquals(a(ONE, NEG_ONE), a(ONE, NEG_ONE).simplify());
    }

    /** Whatever is inside the coordinates still reduces, and the exponent slot is still an exponent. */
    @Test
    void theCoordinatesStillReduce() {
        // the real part: 1+1 becomes 2
        assertEquals(a(at(2, 1), NEG_ONE), a(ONE.plus(ONE), NEG_ONE).simplify());
        // the exponent: the rational zero is the absence marker there, so 1+0 is 1 and not a point zero
        assertEquals(a(at(2, 1), ONE), a(at(2, 1), ONE.plus(ZERO)).simplify());
    }
}
