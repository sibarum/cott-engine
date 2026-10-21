package sibarum.cott.algebra;

import java.util.Objects;

/**
 * A pair {@code (p, q)} of a {@link TractionAlgebra}, as it stands: nothing is reduced, and two pairs are equal only
 * when the algebra and both coordinates are. {@code p} and {@code q} are values of the level below, which may itself
 * be a pair.
 *
 * @param <V> the values of the level below
 */
public record Pair<V>(TractionAlgebra algebra, V p, V q) {

    public Pair {
        Objects.requireNonNull(algebra);
        Objects.requireNonNull(p);
        Objects.requireNonNull(q);
    }
}
