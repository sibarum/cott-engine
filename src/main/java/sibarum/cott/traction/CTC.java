package sibarum.cott.traction;

import java.util.Objects;

/**
 * {@code C(T(C, C), T(C, C))}: the point {@code B + A·j} whose coordinates are ratios of Gaussian integers,
 * {@link TC}. These are {@code Point}'s formulas with {@code T(C, C)}'s {@code +}, {@code ·} and {@code −} in
 * place of {@code T}'s, and they give {@link CC} the inverse it lacks, {@code (B − A·j) / (A² + B²)}, wherever
 * the value has one: off the light lines {@code B = ±i·A}.
 */
@Lean("CTC")
public record CTC(@Lean("CTC.p") TC p, @Lean("CTC.q") TC q) {

    public CTC {
        Objects.requireNonNull(p);
        Objects.requireNonNull(q);
    }

    /** A bicomplex integer {@code C(A, B)}, each coordinate over {@code 1}. */
    @Lean("CTC.ofCC")
    public static CTC ofCC(CC z) {
        T one = TC.gaussianInt(java.math.BigInteger.ONE);
        return new CTC(new TC(z.p(), one), new TC(z.q(), one));
    }

    /** The sum of the points: each coordinate added as a ratio. */
    @Lean("CTC.oplus")
    public CTC oplus(CTC y) {
        return new CTC(p.plus(y.p), q.plus(y.q));
    }

    /** {@code C(A₁·B₂ + A₂·B₁, B₁·B₂ − A₁·A₂)}: the product of the points, with {@code j² = −1}. */
    @Lean("CTC.otimes")
    public CTC otimes(CTC y) {
        return new CTC(p.times(y.q).plus(y.p.times(q)), q.times(y.q).plus(p.times(y.p).neg()));
    }

    /** {@code B + A·j ↦ B − A·j}. */
    @Lean("CTC.conj")
    public CTC conj() {
        return new CTC(p.neg(), q);
    }

    /** {@code A·A + B·B}, the norm of the point, as a ratio of Gaussian integers. */
    @Lean("CTC.nrm")
    public TC nrm() {
        return p.times(p).plus(q.times(q));
    }

    /** {@code C(−A / N, B / N)} with {@code N} the norm: the inverse of the point, {@code (B − A·j) / N}. */
    @Lean("CTC.inv")
    public CTC inv() {
        TC overN = nrm().reciprocal();
        return new CTC(p.neg().times(overN), q.times(overN));
    }

    /**
     * {@code D = u²t² + v²s²}, a Gaussian integer, for {@code A = T(u, v)} and {@code B = T(s, t)}: the norm with
     * the denominators cleared. It is zero exactly on the light lines.
     */
    @Lean({"CTC.den", "CTC.den_eq_zero_iff", "T.toGaussian_oplus", "T.toGaussian_otimes"})
    public T den() {
        T u = p.p(), v = p.q(), s = q.p(), t = q.q();
        return u.otimes(u).otimes(t).otimes(t).oplus(v.otimes(v).otimes(s).otimes(s));
    }

    @Override
    public String toString() {
        return "CTC(" + p + "," + q + ")";
    }
}
