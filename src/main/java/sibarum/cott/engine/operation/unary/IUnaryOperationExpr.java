package sibarum.cott.engine.operation.unary;

import sibarum.cott.engine.base.expr.IExpr;

public interface IUnaryOperationExpr extends IExpr {

    IExpr operand();

}
