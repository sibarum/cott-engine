package sibarum.cott.engine.traction.expr;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.expr.IPromotionRule;
import sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral;

public class ProjRationalToTractionPromotionRule implements IPromotionRule {
    @Override
    public boolean isApplicableFor(IExpr expr) {
        return expr instanceof ProjectiveRationalLiteral;
    }

    @Override
    public IExpr apply(IExpr expr) {
        ProjectiveRationalLiteral projExpr = (ProjectiveRationalLiteral) expr;
        return new TractionLiteral(projExpr, ProjectiveRationalLiteral.ONE);
    }
}
