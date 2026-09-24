package sibarum.cott.projection;

import sibarum.cott.traction.Lean;
import sibarum.cott.traction.T;

import java.util.Optional;

/**
 * What a classical calculator says: {@code T(p,q) ↦ p/q} where {@code q ≠ 0}, and the common meadow's
 * error element where {@code q = 0}. It respects {@code +}, {@code *} and {@code -} exactly, and the
 * reciprocal everywhere but at the quarter turns, where T says {@code 1/ω = 0} and the meadow says error.
 */
public final class Classical implements Projection<Optional<Rational>> {

    @Override
    public String name() {
        return "classical";
    }

    /** {@code Optional.empty()} is the meadow's error element {@code a}. */
    @Override
    @Lean({"T.toQa", "T.Qa"})
    public Optional<Rational> apply(T x) {
        return x.q().signum() == 0 ? Optional.empty() : Optional.of(new Rational(x.p(), x.q()));
    }

    @Override
    public String display(Optional<Rational> reading) {
        return reading.map(Rational::toString).orElse("undefined");
    }
}
