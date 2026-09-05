package sibarum.cott.engine.operation.unary;

import sibarum.cott.engine.base.expr.IExpr;

import java.util.Optional;

public record NegationOperationExpr(IExpr operand) implements IUnaryOperationExpr {

    /**
     * Negation is an involution, so negating a negation uncovers the operand rather than stacking a
     * second node on top of it.
     */
    @Override
    public IExpr negated() {
        return operand;
    }

    @Override
    public IExpr simplify() {
        return operand.simplify().negated();
    }

    @Override
    public Optional<Double> evaluate() {
        IExpr simplified = simplify();
        if (!this.equals(simplified)) {
            return simplified.evaluate();
        }
        return operand.evaluate().map(value -> -value);
    }
}
