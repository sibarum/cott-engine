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
 * <h2>A zero coordinate means the axis contributes nothing</h2>
 * In either slot, and that is why the marker is zero rather than one.
 * <pre>
 *  1 = ( 1, 0)      0 = ( 0, 1)      w = ( 0,-1)     -1 = (-1, 0)
 * </pre>
 * A zero exponent is an absent traction part, since {@code 0^0} is 1. A zero real part is an absent real
 * part, and zero is the only number that can say so: one is a coefficient a term could actually have, so a
 * marker of one could not be told from a real part that happens to be one -- and a marker that is a value
 * takes part in arithmetic and gets absorbed, the way {@code 1·1^1} can be argued into {@code 1^2}. Zero
 * cannot be a coefficient here, because a coefficient of zero would annihilate and this theory has no
 * annihilator.
 * <p>
 * So at most one coordinate is ever zero: {@code (0, 0)} is erasure, which is not a member of the type. It
 * arises transiently and discharges at once, to {@code (1, 0) = 1} multiplicatively or {@code (0, 1) = 0}
 * additively. {@code 0·w} is the first of those.
 *
 * <h2>An absent coordinate is not read as a value</h2>
 * The operations skip it rather than computing with it. In a product the real parts combine as
 * {@code a·c} with an absent one skipped, so {@code 2·0} is {@code (2, 1)} and keeps its 2 -- computing
 * {@code 2·0} as coordinates would annihilate, and then dividing by zero would prove 2 = 1. Negation has
 * nothing to turn in an absent real part, so it materialises the -1 it is multiplying by: {@code -0} is
 * {@code (-1, 1)}, which is {@code -1·0}.
 *
 * <h2>Both coordinates are expressions</h2>
 * Not coordinate pairs. The exponent has to nest ({@code 0^(0^2)}), it has to hold values that are not
 * rational at all ({@code 0^w}, and {@code 0^(1+w)} which stands), and the real part has to hold whatever
 * arrives there -- an atom, a call, a product that has not settled. A carrier that can only hold a coordinate
 * in either slot cannot write those terms down at all: not unreduced, unrepresentable. That is the defect
 * that ended the engine before this one, and a closed two-coordinate carrier reintroduces it -- it has
 * nowhere to put a term whose value the theory has not resolved, and {@code -1·0} is one of those.
 *
 * @param real     the real part n, zero meaning absent
 * @param exponent the traction part's exponent t, zero meaning absent
 */
public record TractionLiteral(IExpr real, IExpr exponent) implements IExpr {

    /** The absent coordinate, in either slot. */
    public static final RationalLiteral ABSENT = RationalLiteral.ZERO;

    /** {@code 0^-1}, which is {@code 1÷0} by E3 and E9. The canonical spelling of omega. */
    public static final TractionLiteral OMEGA = new TractionLiteral(ABSENT, RationalLiteral.NEG_ONE);

    /** {@code 0^t}, with no real part. */
    public static TractionLiteral of(IExpr exponent) {
        return new TractionLiteral(ABSENT, exponent);
    }

    /** Whether the real part is absent, so that this is a bare power of zero. */
    public boolean isBare() {
        return ABSENT.equals(real);
    }

    @Override
    public Optional<Double> evaluate() {
        return Optional.empty();
    }
}
