package sibarum.cott.engine.operation.binary;

import sibarum.cott.engine.base.expr.IExpr;

public interface IBinaryOperationExpr extends IExpr {
    IExpr left();
    IExpr right();
}
