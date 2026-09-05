package sibarum.cott.engine.operation.binary;

import sibarum.cott.engine.base.expr.IExpr;

import java.util.Optional;

public record MultiplicationOperationExpr(IExpr left, IExpr right) implements IBinaryOperationExpr {

    @Override
    public IExpr simplify() {
        return left.simplify().times(right.simplify());
    }

    @Override
    public Optional<Double> evaluate() {
        IExpr simplified = simplify();
        if (!this.equals(simplified)) {
            return simplified.evaluate();
        }
        return left.evaluate().flatMap(l -> right.evaluate().map(r -> l * r));
    }
}
