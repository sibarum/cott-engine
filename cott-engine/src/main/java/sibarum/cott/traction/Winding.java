package sibarum.cott.traction;

import java.math.BigInteger;

/**
 * Comparing a pair's turn with a rational, in integers. A turn is an angle as a fraction of the whole circle,
 * {@code θ/2π}, so {@code 1} is an eighth of a turn and {@code ω} a quarter.
 *
 * <p>Walk the {@code ⊗} powers {@code x, x², …, x^b} and count the steps that cross the positive real ray,
 * the numerator going from negative to not negative. For a pair in the upper half-plane or on the positive
 * real ray, that count is {@code ⌊b · turn x⌋}, so {@code turn x < a/b} exactly when it is under {@code a}.
 * No π is involved. Elsewhere the definitions still compute, but the Lean says nothing of what they mean.
 */
public final class Winding {

    private Winding() {}

    /** {@code x^b} and the winding count of the walk to it. */
    public record Walk(@Lean("T.powWind") T power, @Lean("T.powWind") long winding) {}

    /** How many of the first {@code b} steps of {@code x, x², …} cross the positive real ray, one step at a time. */
    @Lean("T.winding")
    public static long winding(T x, long b) {
        long count = 0;
        T power = T.ZERO;
        for (long k = 0; k < b; k++) {
            T next = power.otimes(x);
            if (power.p().signum() < 0 && next.p().signum() >= 0) count++;
            power = next;
        }
        return count;
    }

    /**
     * {@code (x^b, winding x b)} by squaring, in {@code O(log b)} products: a doubled power winds once more
     * exactly when the half-way power is in the closed lower half-plane.
     */
    @Lean({"T.powWind", "T.powWindAux", "T.powWind_eq"})
    public static Walk powWind(T x, long b) {
        if (b < 0) throw new IllegalArgumentException("powWind takes a natural count: " + b);
        if (b == 0) return new Walk(T.ZERO, 0);
        Walk half = powWind(x, b / 2);
        T r = half.power();
        T y = r.otimes(r);
        boolean lower = r.p().signum() < 0 || (r.p().signum() == 0 && r.q().signum() < 0);
        long w = Math.addExact(Math.multiplyExact(2, half.winding()), lower ? 1 : 0);
        if (b % 2 == 0) return new Walk(y, w);
        T z = y.otimes(x);
        return new Walk(z, w + (y.p().signum() < 0 && z.p().signum() >= 0 ? 1 : 0));
    }

    /** {@code turn x < a/b}, decided by the winding count, found by squaring. */
    @Lean({"T.turnLt", "T.turnLt_iff"})
    public static boolean turnLt(T x, BigInteger a, long b) {
        return BigInteger.valueOf(powWind(x, b).winding()).compareTo(a) < 0;
    }
}
