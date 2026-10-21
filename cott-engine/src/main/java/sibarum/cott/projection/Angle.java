package sibarum.cott.projection;

import sibarum.cott.traction.Lean;
import sibarum.cott.traction.T;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@code θ = arg(q + p·i)}, in {@code (−π, π]}, with {@code tan θ = p/q} for every pair. {@code θ(0ω)} is
 * {@code 0}, as {@code arg 0} is. Two pairs other than {@code 0ω} have the same θ exactly when they are
 * on the same ray.
 *
 * <p>θ is a real number and this is a {@code double} within a few ulps of it, by {@link Math#atan2}: the
 * one reading here that is not exact.
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
        // Coordinates too large for a double are divided together by one power of two first, exactly, so the
        // ray, and so θ, is kept; only the conversion to double rounds.
        int excess = Math.max(x.p().bitLength(), x.q().bitLength()) - 900;
        if (excess <= 0) return Math.atan2(x.p().doubleValue(), x.q().doubleValue());
        BigDecimal scale = new BigDecimal(BigInteger.ONE.shiftLeft(excess));
        double p = new BigDecimal(x.p()).divide(scale).doubleValue();
        double q = new BigDecimal(x.q()).divide(scale).doubleValue();
        return Math.atan2(p, q);
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
