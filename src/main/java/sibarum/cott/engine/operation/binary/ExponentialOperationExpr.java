package sibarum.cott.engine.operation.binary;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.traction.rule.TractionRules;

import java.util.Optional;

public record ExponentialOperationExpr(IExpr base, IExpr exponent) implements IBinaryOperationExpr {

    @Override
    public IExpr simplify() {
        IExpr b = base.simplify();
        IExpr e = exponent.simplify();
        return TractionRules.power(b, e).map(sibarum.cott.engine.base.rule.Rewrite::result)
                .orElseGet(() -> b.equals(base) && e.equals(exponent) ? this : new ExponentialOperationExpr(b, e));
    }

    @Override
    public Optional<Double> evaluate() {
        return Optional.empty();
    }

    @Override
    public IExpr left() {
        return base;
    }

    @Override
    public IExpr right() {
        return exponent;
    }
}
