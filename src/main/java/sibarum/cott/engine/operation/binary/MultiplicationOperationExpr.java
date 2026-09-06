package sibarum.cott.engine.operation.binary;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.traction.rule.TractionRules;

import java.util.Optional;

public record MultiplicationOperationExpr(IExpr left, IExpr right) implements IBinaryOperationExpr {

    /**
     * The traction rules are tried first, and the projective layer answers what they leave. That order is the
     * whole of "the finer reading wins": 0·w read as coordinates is a product of two pairs, and read as
     * tractions it is 0^(1 + -1), an erasure. The second reading is the one with a theory behind it.
     */
    @Override
    public IExpr simplify() {
        IExpr l = left.simplify();
        IExpr r = right.simplify();
        return TractionRules.product(l, r).map(sibarum.cott.engine.base.rule.Rewrite::result).orElseGet(() -> l.times(r));
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
