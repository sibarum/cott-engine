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

    // The point reading: T2(A, B) is the complex number B + A·i, each coordinate a ratio.

    /** A flat point {@code T(p, q)}, the Gaussian integer {@code q + p·i}: {@code T2(T(p,1), T(q,1))}. */
    @Lean("T2.ofPoint")
    public static T2 ofPoint(T z) {
        return new T2(new T(z.p(), BigInteger.ONE), new T(z.q(), BigInteger.ONE));
    }

    /** A flat ratio {@code x}, as the real point {@code x + 0·i}: {@code T2(0, x)}. */
    @Lean("T2.ofRatio")
    public static T2 ofRatio(T x) {
        return new T2(T.ZERO, x);
    }

    /** {@code T2(A₁,B₁) ⊕ T2(A₂,B₂) = T2(A₁ + A₂, B₁ + B₂)}: the sum of the points. */
    @Lean("T2.oplus")
    public T2 oplus(T2 y) {
        return new T2(p.plus(y.p), q.plus(y.q));
    }

    /** {@code T2(A₁·B₂ + A₂·B₁, B₁·B₂ − A₁·A₂)}: the product of the points, {@code (B₁ + A₁i)(B₂ + A₂i)}. */
    @Lean("T2.otimes")
    public T2 otimes(T2 y) {
        return new T2(p.times(y.q).plus(y.p.times(q)), q.times(y.q).plus(p.times(y.p).neg()));
    }

    /** {@code A·A + B·B}, the squared length of the point, as a ratio. */
    @Lean("T2.normSq")
    public T normSq() {
        return p.times(p).plus(q.times(q));
    }

    /** {@code T2(−A / N, B / N)} with {@code N} the squared length: the inverse of the point, {@code (B − A·i)/N}. */
    @Lean("T2.pointInv")
    public T2 pointInv() {
        T overN = normSq().reciprocal();
        return new T2(p.neg().times(overN), q.times(overN));
    }

    /**
     * Whether the numerator comes back from {@code flatten(T2(A, B))}, whatever it was: exactly when
     * {@code B} is off both axes, where the determinant {@code B.p · B.q} of the Möbius map is non-zero.
     */
    @Lean({"T2.flatten_recoverable_iff", "T2.flatten_eq_act", "T2.det_flatten_mat"})
    public static boolean numeratorRecoverable(T b) {
        return b.p().signum() != 0 && b.q().signum() != 0;
    }

    @Override
    public String toString() {
        return "T2(" + p + "," + q + ")";
    }
}
