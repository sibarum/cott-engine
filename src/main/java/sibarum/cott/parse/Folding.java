package sibarum.cott.parse;

import sibarum.cott.engine.ratio.T;

import java.util.Optional;

/**
 * What negating a literal does -- the one operation the model's table does not settle.
 * <p>
 * Every other fold is stated: the sums, the products, the power and the reciprocal {@code T(b,a)} all come
 * straight from docs/Traction-Model.md. Negation does not, because the table puts {@code -1} at
 * {@code T(-1,1)} and {@code -0} at {@code T(0,-1)}, and those are two different motions on the pair -- one
 * turns the numerator, the other turns the denominator. {@link T} therefore has no {@code negated()} at all.
 *
 * <p>So the choice is made here, by whoever is calculating, rather than baked into the arithmetic:
 * <ul>
 * <li>{@link #ORDINARY} turns the numerator, which is the table's {@code T(-a,b)} and what a calculator's
 * user means by a minus sign. Note what it does at the point zero: {@code −T(0,1)} comes back as
 * {@code T(0,1)}, so the table's {@code -0} is not reachable this way.</li>
 * <li>{@link #STANDING} folds no negation at all, leaving {@code −T(p,q)} as a node. For a caller that must
 * not have the sign placed for it.</li>
 * </ul>
 */
@FunctionalInterface
public interface Folding {

    /** The negation of this literal, or empty to leave the negation standing as a node. */
    Optional<T> negate(T value);

    /** The numerator turns: {@code −T(a,b)} is {@code T(-a,b)}. */
    Folding ORDINARY = value -> Optional.of(value.otimesInverse());

    /** Nothing is decided: {@code −T(a,b)} stays a {@link Node.Negation}. */
    Folding STANDING = value -> Optional.empty();
}
