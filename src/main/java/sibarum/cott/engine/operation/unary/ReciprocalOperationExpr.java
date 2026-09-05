package sibarum.cott.engine.operation.unary;

import sibarum.cott.engine.base.expr.IExpr;

import java.util.Optional;

public record ReciprocalOperationExpr(IExpr operand) implements IUnaryOperationExpr {

    /**
     * As with negation, taking the reciprocal twice uncovers the operand.
     */
    @Override
    public IExpr reciprocal() {
        return operand;
    }

    @Override
    public IExpr simplify() {
        return operand.simplify().reciprocal();
    }

    /**
     * A shadow of zero belongs to both zero and omega, and each is the other's reciprocal, so the
     * projection stays at zero either way instead of running off to an infinity.
     */
    @Override
    public Optional<Double> evaluate() {
        IExpr simplified = simplify();
        if (!this.equals(simplified)) {
            return simplified.evaluate();
        }
        return operand.evaluate().map(value -> value == 0.0 ? 0.0 : 1.0 / value);
    }
}
