package sibarum.cott.engine.traction.expr;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.expr.IPromotionRule;
import sibarum.cott.engine.rational.expr.RationalLiteral;

/**
 * A rational read as a traction pair: {@code r} becomes {@code (r, 0)}, which is {@code r·0^0}.
 * <p>
 * The exponent is zero and not one. A rational is not on the traction axis at all -- its real part is itself
 * and its traction part is absent -- and E5 is what says the absent part is 1. Promoting to {@code r^1} was
 * the older carrier's reading, from when a traction was a general {@code base^exponent}.
 */
public class RationalToTractionPromotionRule implements IPromotionRule {

    @Override
    public boolean isApplicableFor(IExpr expr) {
        return expr instanceof RationalLiteral;
    }

    @Override
    public IExpr apply(IExpr expr) {
        return new TractionLiteral(expr, RationalLiteral.ZERO);
    }
}
