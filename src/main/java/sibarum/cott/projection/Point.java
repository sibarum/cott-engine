package sibarum.cott.projection;

import sibarum.cott.traction.Lean;
import sibarum.cott.traction.T;

/** {@code T(p, q) ↦ q + p·i}: the pair as a point of the plane. It is {@code 0} only at {@code 0ω}. */
public final class Point implements Projection<T> {

    @Override
    public String name() {
        return "point";
    }

    /** The pair itself: {@code q} is the real part and {@code p} the imaginary part. */
    @Override
    @Lean({"T.toC", "T.toC_re", "T.toC_im"})
    public T apply(T x) {
        return x;
    }

    @Override
    public String display(T x) {
        String re = x.q().toString();
        if (x.p().signum() == 0) return re;
        String im = x.p().abs().equals(java.math.BigInteger.ONE) ? "i" : x.p().abs() + "i";
        if (x.q().signum() == 0) return (x.p().signum() < 0 ? "-" : "") + im;
        return re + (x.p().signum() < 0 ? " - " : " + ") + im;
    }
}
