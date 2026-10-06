package sibarum.cott.traction;

import java.math.BigInteger;

/**
 * The cosine and sine of the turn {@code a/b}, as two rationals, from integers alone:
 *
 * <ol>
 *   <li>Halve the turn, {@code a/(2b)}, and split it into quarter turns, {@code k/4 + c/(8b)} with {@code c}
 *       in {@code (0, 2b]}.</li>
 *   <li>Dial {@code c/(8b)} for {@code n} steps, and keep the upper end {@code R}.</li>
 *   <li>Turn {@code R} by {@code ω} if {@code k} is odd. Under the square {@code ω} is a half turn, so this
 *       restores the quarter turns the split took off; even ones square away.</li>
 *   <li>Square: the cosine and sine are {@link Spin#rotCos} and {@link Spin#rotSin} of that pair.</li>
 * </ol>
 *
 * <p>For {@code b > 0} the answer is exactly on the unit circle, {@code cos² + sin² = 1}; only its angle is
 * approximate, and the bracket the dial returns says by how much.
 */
public final class RationalTrig {

    private RationalTrig() {}

    /** {@code a/B} as {@code k/4 + c/(4B)}, with {@code c} in {@code (0, B]}. */
    public record Split(@Lean("T.quarterSplit") BigInteger quarters, @Lean("T.quarterSplit") BigInteger rest) {}

    /** {@code k = ⌊(4a − 1)/B⌋} and {@code c = (4a − 1) mod B + 1}, for {@code B > 0}. */
    @Lean({"T.quarterSplit", "T.quarter_split"})
    public static Split quarterSplit(BigInteger a, BigInteger B) {
        if (B.signum() <= 0) throw new IllegalArgumentException("quarterSplit takes a positive denominator: " + B);
        BigInteger m = a.shiftLeft(2).subtract(BigInteger.ONE);
        BigInteger rest = m.mod(B);
        return new Split(m.subtract(rest).divide(B), rest.add(BigInteger.ONE));
    }

    /** The descent at some depth, its bracket, and the pair whose square is the rotation by the turn. */
    public record Dialed(@Lean("T.dial") int depth, @Lean("T.dial") Dial.Bracket bracket,
                         @Lean("T.spinTurn") T spin) {}

    /** The pair whose square is the rotation by {@code a/b} of a turn, to depth {@code n}. */
    @Lean("T.spinTurn")
    public static T spinTurn(BigInteger a, long b, int n) {
        return dialed(a, b, n, null).spin();
    }

    /** {@code cos(2π·a/b)}, as the pair {@code T(q² − p², N)} of {@link #spinTurn}. */
    @Lean({"T.cosTurn", "T.cosTurn_sq_add_sinTurn_sq", "T.cosTurn_err"})
    public static T cosTurn(BigInteger a, long b, int n) {
        return Spin.rotCos(spinTurn(a, b, n));
    }

    /** {@code sin(2π·a/b)}, as the pair {@code T(2pq, N)} of {@link #spinTurn}. */
    @Lean({"T.sinTurn", "T.cosTurn_sq_add_sinTurn_sq", "T.sinTurn_err"})
    public static T sinTurn(BigInteger a, long b, int n) {
        return Spin.rotSin(spinTurn(a, b, n));
    }

    /**
     * {@link #spinTurn} at depth {@code n}, with the bracket it came from. Given {@code widthDenominator}, the
     * descent stops early, at the first depth where {@code N(L)·N(R)} reaches it. Every depth is one the
     * theorems cover, so where it stops needs no proof of its own.
     */
    @Lean({"T.spinTurn", "T.dial", "T.sin_sq_dial"})
    public static Dialed dialed(BigInteger a, long b, int n, BigInteger widthDenominator) {
        if (b <= 0) throw new IllegalArgumentException("a turn's denominator is positive: " + b);
        long bb = Math.multiplyExact(2, b);
        Split s = quarterSplit(a, BigInteger.valueOf(bb));
        long target = Math.multiplyExact(4, bb);
        Dial.Bracket br = Dial.Bracket.START;
        int depth = 0;
        while (depth < n && (widthDenominator == null || br.widthDenominator().compareTo(widthDenominator) < 0)) {
            br = Dial.step(s.rest(), target, br);
            depth++;
        }
        T spin = br.upper().otimes(T.OMEGA.otimesPowNat(s.quarters().mod(BigInteger.TWO).intValueExact()));
        return new Dialed(depth, br, spin);
    }
}
