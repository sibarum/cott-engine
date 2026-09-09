package sibarum.cott.engine.traction.expr;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;

import java.util.Optional;

/**
 * The traction pair: {@code (n, t) = n·0^t}, a real part and a traction part.
 * <p>
 * The base is always zero, which is what makes this a pair rather than a general power. {@code 2^3} is not a
 * traction and does not become one -- reading 2 as a power of zero is the general involution, which is
 * theory-problems.md #4 and is not assumed here.
 *
 * <h2>Both coordinates are expressions</h2>
 * Not coordinate pairs. The exponent has to nest ({@code 0^(0^2)}), it has to hold values that are not
 * rational at all ({@code 0^w}, and {@code 0^(1+w)} which stands), and the real part has to hold whatever
 * arrives there -- an atom, a call, a product that has not settled. A carrier that can only hold a coordinate
 * in either slot cannot write those terms down at all: not unreduced, unrepresentable. That is the defect
 * that ended the engine before this one, and a closed two-coordinate carrier reintroduces it -- it has
 * nowhere to put a term whose value the theory has not resolved, and {@code -1·0} is one of those.
 *
 * <h2>The real part is never zero</h2>
 * A zero there would be an annihilator, and this theory does not have one: {@code 2·0} and {@code 0·0} have
 * to stay apart, or dividing by zero would prove 2 = 1. So a real part that arrives as the rational zero
 * rolls into the exponent instead -- {@code (0, t)} is {@code (1, t+1)}, since the rational zero IS
 * {@code 0^1} by E4. That is the roll-in note in Traction-Theory.md, and reversibility is why it is there.
 *
 * @param real     the real part n, never the rational zero
 * @param exponent the traction part's exponent t
 */
public record TractionLiteral(IExpr real, IExpr exponent) implements IExpr {

    /** {@code 0^-1}, which is {@code 1÷0} by E3 and E9. The canonical spelling of omega. */
    public static final TractionLiteral OMEGA = new TractionLiteral(RationalLiteral.ONE, RationalLiteral.NEG_ONE);

    /** {@code 0^t}, with nothing in front of it. */
    public static TractionLiteral of(IExpr exponent) {
        return new TractionLiteral(RationalLiteral.ONE, exponent);
    }

    /** Whether the real part is absent, so that this is a bare power of zero. */
    public boolean isBare() {
        return RationalLiteral.ONE.equals(real);
    }

    @Override
    public Optional<Double> evaluate() {
        return Optional.empty();
    }
}
