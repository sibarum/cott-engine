package sibarum.cott.engine.traction.expr;

import sibarum.cott.engine.base.expr.IExpr;

/**
 * A traction pair: a real part, and the exponent of a traction part.
 * <p>
 * Two nodes implement this and they differ in one thing only -- how the two coordinates are joined.
 * {@link TractionLiteral} is {@code n·0^t} and {@link AdditiveTractionLiteral} is {@code n + 0^t}. Which of
 * them is the carrier is open; see Traction-Theory.md, Carrier. Each closes under one operation and its
 * inverse and spills four cross-terms under the other, so it is a swap rather than a ranking.
 *
 * <h2>Both exist because a term may hold one in the other's coordinate</h2>
 * The smallest spelling of {@code i} needs both: {@code ω/2} is the multiplicative pair
 * {@code m(1/2, -1)}, and {@code i} is {@code a(∅, m(1/2, -1))}. A carrier with only one of the two joins
 * has nowhere to put the {@code 1/2} -- an additive node's second coordinate sits under a {@code 0^} and so
 * holds an exponent, never a coefficient.
 *
 * <h2>The absence marker is the same in both</h2>
 * Zero, in either coordinate, and skipped rather than computed with. That survives the change of join
 * precisely because it is skipped: {@code 1} is {@code 1 + ∅} as readily as {@code 1 · ∅}. What does not
 * survive is the inverse -- see {@code TractionRules.negation}.
 */
public sealed interface ITractionPair extends IExpr permits TractionLiteral, AdditiveTractionLiteral {

    /** The real part n, zero meaning absent. */
    IExpr real();

    /** The traction part's exponent t, zero meaning absent. */
    IExpr exponent();

    /** Whether the real part is absent, so that this is a bare power of zero. */
    boolean isBare();
}
