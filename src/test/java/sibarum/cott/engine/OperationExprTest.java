package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.OMEGA;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.ONE;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.ZERO;

class OperationExprTest {

    /**
     * Stands in for anything the projective layer cannot combine with.
     *
     * <p>0^2 and not 0^1: the latter is the point zero by E4, and now that the fold is a rewrite of its own
     * rather than something a rule did on its way past, it is no longer opaque to anything.
     */
    private static final TractionLiteral OPAQUE = new TractionLiteral(ZERO, ProjectiveRationalLiteral.of(2, 1));

    private static ProjectiveRationalLiteral at(int numerator, int denominator) {
        return ProjectiveRationalLiteral.of(numerator, denominator);
    }

    @Test
    void additionCombinesRationalOperands() {
        assertEquals(at(2, 1), new AdditionOperationExpr(ONE, ONE).simplify());
    }

    @Test
    void multiplicationCombinesRationalOperands() {
        assertEquals(at(10, 21), new MultiplicationOperationExpr(at(2, 3), at(5, 7)).simplify());
    }

    @Test
    void operandsAreSimplifiedBeforeTheOperationIsTried() {
        IExpr nested = new AdditionOperationExpr(new AdditionOperationExpr(ONE, ONE), ONE);
        assertEquals(at(3, 1), nested.simplify());
    }

    @Test
    void aTermWithNoDefiniteAnswerStands() {
        IExpr sum = new AdditionOperationExpr(ONE, OPAQUE);
        assertEquals(sum, sum.simplify());
        assertEquals(sum.simplify(), sum.simplify().simplify());
    }

    @Test
    void negationIsAnInvolution() {
        assertSame(OPAQUE, new NegationOperationExpr(OPAQUE).negated());
        assertSame(OPAQUE, new NegationOperationExpr(new NegationOperationExpr(OPAQUE)).simplify());
    }

    @Test
    void reciprocalIsAnInvolution() {
        assertSame(OPAQUE, new ReciprocalOperationExpr(OPAQUE).reciprocal());
        assertSame(OPAQUE, new ReciprocalOperationExpr(new ReciprocalOperationExpr(OPAQUE)).simplify());
    }

    @Test
    void theReciprocalOfZeroSimplifiesToOmega() {
        assertEquals(OMEGA, new ReciprocalOperationExpr(ZERO).simplify());
    }

    @Test
    void evaluateCombinesTheOperandValues() {
        assertEquals(5.0 / 6.0, new AdditionOperationExpr(at(1, 2), at(1, 3)).evaluate().orElseThrow(), 1e-12);
        assertEquals(Optional.of(-1.0), new NegationOperationExpr(ONE).evaluate());
        assertEquals(Optional.of(2.0), new ReciprocalOperationExpr(at(1, 2)).evaluate());
        assertEquals(1.0 / 6.0, new MultiplicationOperationExpr(at(1, 2), at(1, 3)).evaluate().orElseThrow(), 1e-12);
    }

    @Test
    void evaluateIsEmptyOnlyWhereThereIsNoProjectionAtAll() {
        assertEquals(Optional.empty(), new AdditionOperationExpr(ONE, OPAQUE).evaluate());
        assertEquals(Optional.of(0.0), new AdditionOperationExpr(ONE, OMEGA).evaluate());
        assertEquals(Optional.of(0.0), new ReciprocalOperationExpr(ZERO).evaluate());
    }
}
