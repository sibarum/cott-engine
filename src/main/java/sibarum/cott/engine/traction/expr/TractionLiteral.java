package sibarum.cott.engine.traction.expr;

import sibarum.cott.engine.base.expr.IExpr;

import java.util.Optional;

/**
 * This expression is of the form base^exp.
 * <p>
 * Both parts are expressions, not coordinates. The exponent has to nest: 1^0 routes through
 * 0^(0^2), and 0*x is 0^(1+u), which puts an arbitrary traction value in an exponent. A carrier
 * that can only hold a coordinate there cannot write those terms down at all -- not unreduced,
 * unrepresentable -- which is the defect that ended the previous engine.
 * <p>
 * Traction arithmetic is not defined yet. Until it is, a traction carries no operations of its own:
 * it inherits the node-building defaults from {@link IExpr}, so an operation on one stands as an
 * unevaluated term instead of claiming an answer.
 *
 * @param base
 * @param exp
 */
public record TractionLiteral(IExpr base, IExpr exp) implements IExpr {

    @Override
    public Optional<Double> evaluate() {
        return Optional.empty();
    }
}
