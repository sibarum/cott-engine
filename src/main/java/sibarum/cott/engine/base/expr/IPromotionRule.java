package sibarum.cott.engine.base.expr;

public interface IPromotionRule {

    boolean isApplicableFor(IExpr expr);
    IExpr apply(IExpr expr);

}
