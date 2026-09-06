package sibarum.cott.engine.operation.binary;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.traction.rule.TractionRules;

import java.util.Optional;

public record AdditionOperationExpr(IExpr left, IExpr right) implements IBinaryOperationExpr {

    /**
     * Simplified operands are handed back to {@link IExpr#plus}, which combines them where it knows
     * how to and rebuilds this node where it does not.
     */
    @Override
    public IExpr simplify() {
        IExpr l = left.simplify();
        IExpr r = right.simplify();
        return TractionRules.sum(l, r).map(sibarum.cott.engine.base.rule.Rewrite::result).orElseGet(() -> l.plus(r));
    }

    /**
     * Projection does not commute with the operations — omega shares zero's shadow, so projecting
     * the operands and adding those is not the same as adding exactly and projecting the result.
     * The exact arithmetic is done first wherever it can be, and only a term that stands falls back
     * to combining the shadows it has.
     */
    @Override
    public Optional<Double> evaluate() {
        IExpr simplified = simplify();
        if (!this.equals(simplified)) {
            return simplified.evaluate();
        }
        return left.evaluate().flatMap(l -> right.evaluate().map(r -> l + r));
    }
}
