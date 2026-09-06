package sibarum.cott.engine.base.expr;

import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;

import java.util.Optional;

public interface IExpr {

    default IExpr plus(IExpr expr) {
        return new AdditionOperationExpr(this, expr);
    }
    default IExpr negated() {
        return new NegationOperationExpr(this);
    }
    default IExpr minus(IExpr expr) {
        return this.plus(expr.negated());
    }
    default IExpr times(IExpr expr) {
        return new MultiplicationOperationExpr(this, expr);
    }
    default IExpr reciprocal() {
        return new ReciprocalOperationExpr(this);
    }
    default IExpr dividedBy(IExpr expr) {
        return this.times(expr.reciprocal());
    }

    /**
     * The furthest this expression reduces on its own terms. Operands are simplified first, and
     * where the operation has no definite answer for them the term stands: the result is then equal
     * to this one rather than reduced. Simplification is therefore idempotent.
     *
     * <p>This is the derivation with the reasons dropped, and it is not a second implementation of it. There
     * is no fast path: an evaluator that answers by one route and explains itself by another can be made to
     * disagree with its own explanation, and an explanation that does not describe what happened is worse than
     * none. So the audit and the answer are one walk, and there is nothing left to keep in step.
     */
    default IExpr simplify() {
        return sibarum.cott.engine.derivation.Deriver.derive(this).to();
    }

    /**
     * The real value of this expression, or empty where it has none — because a part of it is not a
     * number, or because the point it names is not a finite real.
     */
    Optional<Double> evaluate();

}
