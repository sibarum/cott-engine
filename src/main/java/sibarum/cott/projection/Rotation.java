package sibarum.cott.projection;

import sibarum.cott.traction.Lean;
import sibarum.cott.traction.Spin;
import sibarum.cott.traction.T;

import java.util.Optional;

/**
 * The rotation the pair squares to, {@code ((q² − p²)/N, 2pq/N)}: exact, rational, and with no π. It is the
 * rotation by {@code 2θ}, twice the {@link Angle}, so {@code 1} reads as a quarter turn and {@code ω} as a
 * half turn. Two pairs read the same exactly when they lie on one line through the origin. {@code 0ω} has
 * none.
 */
public final class Rotation implements Projection<Optional<Rotation.Turned>> {

    /** The rotation as the point it takes {@code 1} to: {@code cos + sin·i}. */
    public record Turned(Rational cos, Rational sin) {}

    @Override
    public String name() {
        return "rotation";
    }

    @Override
    @Lean({"T.rot", "T.rot_mem_specialOrthogonalGroup", "T.rotCos_eq_cos", "T.rotSin_eq_sin"})
    public Optional<Turned> apply(T x) {
        if (x.equals(T.ZERO_OMEGA)) return Optional.empty();
        T c = Spin.rotCos(x);
        T s = Spin.rotSin(x);
        return Optional.of(new Turned(new Rational(c.p(), c.q()), new Rational(s.p(), s.q())));
    }

    /** {@code cos + sin·i}, with a fractional {@code sin} in parentheses, or {@code none}. */
    @Override
    public String display(Optional<Turned> reading) {
        return reading.map(r -> {
            Rational s = r.sin();
            if (s.num().signum() == 0) return r.cos().toString();
            Rational abs = s.num().signum() < 0 ? s.neg() : s;
            String im = abs.equals(Rational.of(1, 1)) ? "i"
                    : abs.den().equals(java.math.BigInteger.ONE) ? abs + "·i" : "(" + abs + ")·i";
            if (r.cos().num().signum() == 0) return (s.num().signum() < 0 ? "-" : "") + im;
            return r.cos() + (s.num().signum() < 0 ? " - " : " + ") + im;
        }).orElse("none");
    }
}
