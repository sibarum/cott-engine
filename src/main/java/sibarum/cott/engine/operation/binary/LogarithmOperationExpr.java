package sibarum.cott.engine.operation.binary;

import sibarum.cott.engine.base.expr.IExpr;

import java.util.Optional;

public record LogarithmOperationExpr(IExpr base, IExpr operand) implements IBinaryOperationExpr {

    @Override
    public IExpr left() {
        return base;
    }

    @Override
    public IExpr right() {
        return operand;
    }

    @Override
    public IExpr simplify() {
        return this;
    }

    @Override
    public Optional<Double> evaluate() {
        return Optional.empty();
    }
}
