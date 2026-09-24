package sibarum.cott.traction;

import java.math.BigInteger;
import java.util.Objects;

/**
 * {@code T2(p, q)}: a traction whose two coordinates are themselves tractions, under the fraction
 * arithmetic of {@link T}. The flat pairs sit inside it by {@link #of}, and {@link #flatten} reads a pair
 * of pairs back as the flat pair {@code p / q}.
 */
@Lean({"T2", "T2.ext"})
public record T2(@Lean("T2.p") T p, @Lean("T2.q") T q) {

    public T2 {
        Objects.requireNonNull(p);
        Objects.requireNonNull(q);
    }

    @Lean("T2.«0»")
    public static final T2 ZERO = new T2(T.ZERO, T.ONE);
    @Lean("T2.«1»")
    public static final T2 ONE = new T2(T.ONE, T.ONE);
    @Lean("T2.ω")
    public static final T2 OMEGA = new T2(T.ONE, T.ZERO);

    /** {@code T(a,b) ↦ T2(T(a,1), T(b,1))}: each integer coordinate becomes its own unit-denominator pair. */
    @Lean("T2.of")
    public static T2 of(T x) {
        return new T2(new T(x.p(), BigInteger.ONE), new T(x.q(), BigInteger.ONE));
    }

    /** {@code T2(p₁,q₁) + T2(p₂,q₂) = T2(p₁·q₂ + q₁·p₂, q₁·q₂)}. */
    @Lean("T2.plus")
    public T2 plus(T2 y) {
        return new T2(p.times(y.q).plus(q.times(y.p)), q.times(y.q));
    }

    /** {@code T2(p₁,q₁) · T2(p₂,q₂) = T2(p₁·p₂, q₁·q₂)}. */
    @Lean("T2.times")
    public T2 times(T2 y) {
        return new T2(p.times(y.p), q.times(y.q));
    }

    /** {@code -T2(p, q) = T2(-p, q)}. */
    @Lean({"T2.instNeg", "T2.neg_def"})
    public T2 neg() {
        return new T2(p.neg(), q);
    }

    /** {@code T2(q, p)}. */
    @Lean("T2.reciprocal")
    public T2 reciprocal() {
        return new T2(q, p);
    }

    /** {@code T2(pⁿ, qⁿ)}, each coordinate raised by {@link T#power}: repeated {@code ·}. */
    @Lean("T2.power")
    public T2 power(int n) {
        return new T2(p.power(n), q.power(n));
    }

    /** {@code T2(A, B) ↦ A / B}, so {@code T2(T(a,b), T(c,d)) ↦ T(ad, bc)}. */
    @Lean({"T2.flatten", "T2.flatten_def"})
    public T flatten() {
        return p.times(q.reciprocal());
    }

    @Override
    public String toString() {
        return "T2(" + p + "," + q + ")";
    }
}
