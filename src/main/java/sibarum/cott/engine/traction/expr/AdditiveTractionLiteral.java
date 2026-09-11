package sibarum.cott.engine.traction.expr;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;

import java.util.Optional;

/**
 * The additive traction pair: {@code (n, t) = n + 0^t}.
 * <p>
 * The same two coordinates as {@link TractionLiteral} joined by {@code +} instead of {@code ·}. By the
 * duality that E1 and E2 state -- an operation acts on the real coordinate as itself and on the traction
 * coordinate as its dual -- this node closes under {@code +} and {@code −}, where the multiplicative one
 * closes under {@code ·} and {@code ÷}.
 *
 * <h2>What it holds that the multiplicative node cannot</h2>
 * The 45° positions, as values rather than as standing sums. {@code 1 + 0} is the pair {@code (1, 1)} and
 * {@code 1 + ω} is {@code (1, -1)}; under {@code n·0^t} the first is unreachable, because {@code x · 1 = x}
 * collapses {@code (1,1)} to {@code (0,1)}, so it can only be held unreduced. A standing sum is not
 * something a differential can be taken of, and {@code f(x+0)} needs it to be a value.
 * <p>
 * What it costs is their powers: {@code (1+ω)²} is {@code 1 + 2ω + 0^-2}, three terms, which is the
 * multiplication that does not close.
 *
 * <h2>Addition between two of these is not wired</h2>
 * It would need {@code 0^b + 0^d = 0^(b·d)}, the mirror law, which is the addition law Traction-Theory.md
 * does not adopt. So the node, its folds and its inverse are here, and the sum is provisional -- see
 * {@code TractionRules.provisionalAdditiveSum}.
 *
 * @param real     the real part n, zero meaning absent
 * @param exponent the traction part's exponent t, zero meaning absent
 */
public record AdditiveTractionLiteral(IExpr real, IExpr exponent) implements ITractionPair {

    /** The absent coordinate, in either slot. The same marker as the multiplicative node's. */
    public static final RationalLiteral ABSENT = TractionLiteral.ABSENT;

    /** {@code ∅ + 0^t}, with no real part -- which is {@code 0^t}, and folds to the multiplicative node. */
    public static AdditiveTractionLiteral of(IExpr exponent) {
        return new AdditiveTractionLiteral(ABSENT, exponent);
    }

    @Override
    public boolean isBare() {
        return ABSENT.equals(real);
    }

    /** Whether the traction part is absent, so that this is just its real part. */
    public boolean isReal() {
        return ABSENT.equals(exponent);
    }

    /**
     * No real reading of its own, as with the multiplicative pair. Omega shares zero's shadow, so projecting
     * the coordinates and joining those is not the same as joining exactly and projecting the result.
     */
    @Override
    public Optional<Double> evaluate() {
        return Optional.empty();
    }
}
