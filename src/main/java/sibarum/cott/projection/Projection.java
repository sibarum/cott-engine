package sibarum.cott.projection;

import sibarum.cott.traction.T;

/**
 * A reading of a value. The value itself is the pair as computed, with no quotient: that is the ground
 * truth and it is never replaced. A projection is computed from it on demand, and several can be taken
 * of the same value.
 *
 * <p>{@link #apply} is the reading, and cites the Lean map it implements. {@link #display} is only how
 * that reading is written.
 *
 * @param <R> what the reading produces
 */
public interface Projection<R> {

    /** A short name, unique among the projections, such as {@code ratio} or {@code classical}. */
    String name();

    R apply(T x);

    String display(R reading);

    default String read(T x) {
        return display(apply(x));
    }
}
