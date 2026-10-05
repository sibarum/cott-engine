package sibarum.cott.traction;

import java.math.BigInteger;
import java.util.Objects;

/**
 * {@code T(C, C)}: a ratio {@code z / w} of two Gaussian integers, under {@code T}'s own value-position
 * formulas ({@code TOver ℤ[i]}). Nothing is reduced.
 *
 * <p>Each Gaussian integer is held as the flat point it already is: {@code T(p, q)} is {@code q + p·i}
 * ({@code T.toGaussian}), whose {@code ⊕} is the Gaussian {@code +}, {@code ⊗} the Gaussian {@code ·},
 * {@link T#oplusInverse} the Gaussian negation and {@link T#neg} the conjugate. So the Gaussian {@code 1} is
 * the flat {@code T(0,1)} and the Gaussian {@code 0} is the flat {@code 0ω}.
 */
@Lean({"TC", "TOver"})
public record TC(@Lean("TOver.p") T p, @Lean("TOver.q") T q) {

    public TC {
        Objects.requireNonNull(p);
        Objects.requireNonNull(q);
    }

    /** The integer {@code n} as the Gaussian integer {@code n + 0·i}, over {@code 1}. */
    @Lean({"T.complexInt", "T.toGaussian_complexInt"})
    public static TC of(BigInteger n) {
        return new TC(gaussianInt(n), gaussianInt(BigInteger.ONE));
    }

    /** {@code T.complexInt n = T(0, n)}: the integer {@code n} as a Gaussian integer. */
    @Lean({"T.complexInt", "T.toGaussian_complexInt"})
    public static T gaussianInt(BigInteger n) {
        return new T(BigInteger.ZERO, n);
    }

    /** {@code T(z₁,w₁) + T(z₂,w₂) = T(z₁·w₂ + z₂·w₁, w₁·w₂)}, in Gaussian integers. */
    @Lean({"TOver.instAdd", "TOver.add_p", "TOver.add_q", "T.toGaussian_oplus", "T.toGaussian_otimes"})
    public TC plus(TC y) {
        return new TC(p.otimes(y.q).oplus(y.p.otimes(q)), q.otimes(y.q));
    }

    /** {@code T(z₁,w₁) · T(z₂,w₂) = T(z₁·z₂, w₁·w₂)}. */
    @Lean({"TOver.instMul", "TOver.mul_p", "TOver.mul_q", "T.toGaussian_otimes"})
    public TC times(TC y) {
        return new TC(p.otimes(y.p), q.otimes(y.q));
    }

    /** {@code -T(z, w) = T(-z, w)}, with {@code -z} the Gaussian negation. */
    @Lean({"TOver.instNeg", "TOver.neg_p", "TOver.neg_q", "T.toGaussian_oplusInverse"})
    public TC neg() {
        return new TC(p.oplusInverse(), q);
    }

    /** {@code T(w, z)}: a swap. */
    @Lean("TOver.reciprocal")
    public TC reciprocal() {
        return new TC(q, p);
    }

    /** {@code re² + im²}, the norm of the denominator. */
    @Lean("TC.nrm")
    public BigInteger nrm() {
        return q.q().pow(2).add(q.p().pow(2));
    }

    /**
     * {@code z / w = (z·w̄) / N(w)}: the point {@code C(T, T)} whose real and imaginary parts are those of
     * {@code z·w̄}, each over the norm of {@code w}. Every {@code T(z, 0)} goes to {@code T2(0ω, 0ω)}.
     */
    @Lean({"TC.rationalize", "TC.nrm", "T.toGaussian_neg", "T.toGaussian_re", "T.toGaussian_im"})
    public T2 rationalize() {
        T zw = p.otimes(q.neg());
        BigInteger n = nrm();
        return new T2(new T(zw.p(), n), new T(zw.q(), n));
    }

    @Override
    public String toString() {
        return "TC(" + p + "," + q + ")";
    }
}
