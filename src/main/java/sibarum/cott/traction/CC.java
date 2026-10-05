package sibarum.cott.traction;

import java.math.BigInteger;
import java.util.Objects;

/**
 * {@code C(C, C)}: the point {@code B + A·j} whose coordinates are Gaussian integers, with {@code j² = −1}:
 * the bicomplex integers. There are two square roots of {@code −1}, the {@code i} inside each coordinate and
 * the {@code j} of the point, and {@code (i·j)² = 1}, so there are zero divisors.
 *
 * <p>As in {@link TC}, each Gaussian integer is held as the flat point {@code T(p, q) = q + p·i}
 * ({@code T.toGaussian}): {@code ⊕} and {@code ⊗} are its {@code +} and {@code ·}.
 */
@Lean("CC")
public record CC(@Lean("CC.p") T p, @Lean("CC.q") T q) {

    public CC {
        Objects.requireNonNull(p);
        Objects.requireNonNull(q);
    }

    /** A flat point on the outer unit, {@code T(p, q) ↦ C(p, q)}: each integer coordinate a Gaussian integer. */
    @Lean({"CC.ofOuter", "T.complexInt", "T.toGaussian_complexInt"})
    public static CC ofOuter(T z) {
        return new CC(TC.gaussianInt(z.p()), TC.gaussianInt(z.q()));
    }

    /** The sum of the points. */
    @Lean({"CC.oplus", "T.toGaussian_oplus"})
    public CC oplus(CC y) {
        return new CC(p.oplus(y.p), q.oplus(y.q));
    }

    /** {@code C(A₁·B₂ + A₂·B₁, B₁·B₂ − A₁·A₂)}: the product of the points, with {@code j² = −1}. */
    @Lean({"CC.otimes", "T.toGaussian_oplus", "T.toGaussian_otimes", "T.toGaussian_oplusInverse"})
    public CC otimes(CC y) {
        return new CC(p.otimes(y.q).oplus(y.p.otimes(q)), q.otimes(y.q).oplus(p.otimes(y.p).oplusInverse()));
    }

    /** {@code B² + A²}, a Gaussian integer: what {@code z ⊗ conj z} leaves. */
    @Lean({"CC.nrm", "T.toGaussian_oplus", "T.toGaussian_otimes"})
    public T nrm() {
        return q.otimes(q).oplus(p.otimes(p));
    }

    /** {@code j ↦ i}: {@code B + i·A}. The Gaussian {@code i} is the flat {@code ω}. */
    @Lean({"CC.evPlus", "CC.gi", "T.toGaussian_omega", "T.toGaussian_oplus", "T.toGaussian_otimes"})
    public T evPlus() {
        return q.oplus(T.OMEGA.otimes(p));
    }

    /** {@code j ↦ −i}: {@code B − i·A}. */
    @Lean({"CC.evMinus", "CC.gi", "T.toGaussian_omega", "T.toGaussian_oplusInverse"})
    public T evMinus() {
        return q.oplus(T.OMEGA.otimes(p).oplusInverse());
    }

    @Override
    public String toString() {
        return "CC(" + p + "," + q + ")";
    }
}
