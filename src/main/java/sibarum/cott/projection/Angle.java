package sibarum.cott.projection;

import sibarum.cott.traction.Lean;
import sibarum.cott.traction.T;

import java.math.BigInteger;

/**
 * {@code θ = arg(q + p·i)}, in {@code (−π, π]}, with {@code tan θ = p/q} for every pair. {@code θ(0ω)} is
 * {@code 0}, as {@code arg 0} is. Two pairs other than {@code 0ω} have the same θ exactly when they are
 * on the same ray.
 *
 * <p>θ is a real number and this is its nearest {@code double}: the one reading here that is not exact.
 */
public final class Angle implements Projection<Double> {

    @Override
    public String name() {
        return "angle";
    }

    /** θ in radians. */
    @Override
    @Lean({"T.theta", "T.toC"})
    public Double apply(T x) {
        // Coordinates too large for a double are shifted down together first; the ray, and so θ, is kept.
        int excess = Math.max(x.p().bitLength(), x.q().bitLength()) - 900;
        BigInteger p = excess > 0 ? x.p().shiftRight(excess) : x.p();
        BigInteger q = excess > 0 ? x.q().shiftRight(excess) : x.q();
        return Math.atan2(p.doubleValue(), q.doubleValue());
    }

    /** In degrees, to at most ten decimal places. */
    @Override
    public String display(Double radians) {
        double degrees = Math.toDegrees(radians);
        String s = new java.math.BigDecimal(degrees).setScale(10, java.math.RoundingMode.HALF_EVEN)
                .stripTrailingZeros().toPlainString();
        return (s.equals("-0") ? "0" : s) + "°";
    }
}
