package sibarum.cott.traction;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * A traction {@code T(p, q)}: a pair of integers, read as the ratio {@code p/q} and as the point
 * {@code q + p·i}.
 *
 * <p>Nothing is reduced. Two pairs are equal exactly when their coordinates are, so {@code T(1,2)} and
 * {@code T(2,4)} are different values; an invariant under which they agree is a {@link Quotient}. The
 * coordinates are unbounded, as Lean's integers are, so no operation here can overflow into a result the
 * proofs do not describe.
 */
@Lean({"T", "T.ext"})
public record T(@Lean("T.p") BigInteger p, @Lean("T.q") BigInteger q) {

    public T {
        Objects.requireNonNull(p);
        Objects.requireNonNull(q);
    }

    @Lean("T.mk")
    public static T of(long p, long q) {
        return new T(BigInteger.valueOf(p), BigInteger.valueOf(q));
    }

    // The nine named values, spelled the model's way.

    @Lean({"T.«0»", "T.zero_def"})
    public static final T ZERO = of(0, 1);
    @Lean("T.«1»")
    public static final T ONE = of(1, 1);
    @Lean("T.ω")
    public static final T OMEGA = of(1, 0);
    @Lean("T._1")
    public static final T UNDER_ONE = of(1, -1);
    @Lean("T._0")
    public static final T UNDER_ZERO = of(0, -1);
    @Lean("T.«-_1»")
    public static final T NEG_UNDER_ONE = of(-1, -1);
    @Lean("T.«-ω»")
    public static final T NEG_OMEGA = of(-1, 0);
    @Lean("T.«-1»")
    public static final T NEG_ONE = of(-1, 1);
    @Lean("T.«0ω»")
    public static final T ZERO_OMEGA = of(0, 0);

    @Lean("T.nine")
    public static final List<T> NINE = List.of(
            ZERO, ONE, OMEGA, UNDER_ONE, UNDER_ZERO, NEG_UNDER_ONE, NEG_OMEGA, NEG_ONE, ZERO_OMEGA);

    /** {@code T(p,q) + T(r,s) = T(ps + rq, qs)}, fraction addition. */
    @Lean("T.plus")
    public T plus(T y) {
        return new T(p.multiply(y.q).add(y.p.multiply(q)), q.multiply(y.q));
    }

    /** {@code T(p,q) * T(r,s) = T(pr, qs)}, fraction multiplication. */
    @Lean("T.times")
    public T times(T y) {
        return new T(p.multiply(y.p), q.multiply(y.q));
    }

    /** {@code -T(p,q) = T(-p, q)}: the numerator turned. So {@code -0 = 0}. */
    @Lean({"T.otimesInverse", "T.neg_def"})
    public T neg() {
        return new T(p.negate(), q);
    }

    /** {@code T(q, p)}. */
    @Lean("T.reciprocal")
    public T reciprocal() {
        return new T(q, p);
    }

    /** {@code T(pⁿ, qⁿ)}, for a natural {@code n}. */
    @Lean("T.power")
    public T power(int n) {
        if (n < 0) throw new IllegalArgumentException("power takes a natural exponent: " + n);
        return new T(p.pow(n), q.pow(n));
    }

    /** {@code T(kp, kq)}: both coordinates multiplied by {@code k}. */
    @Lean("T.scale")
    public T scale(BigInteger k) {
        return new T(k.multiply(p), k.multiply(q));
    }

    /** {@code T(0, k)}, the one term by which a law as written can differ from its exact result. */
    @Lean("T.residue")
    public static T residue(BigInteger k) {
        return new T(BigInteger.ZERO, k);
    }

    @Override
    public String toString() {
        return "T(" + p + "," + q + ")";
    }
}
