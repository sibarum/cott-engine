package sibarum.cott.algebra;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * The numbers of a {@link NumberType}, at the bottom of every value, and how {@code +}, {@code −}, {@code ·},
 * {@code /} and {@code ^} act on them.
 *
 * <p>An operation a level does not have is not an error. {@link #divide} and the powers answer empty, and the value
 * meets the traction algebra whose reading is that operation, as {@code a / b} over the integers is {@code Q(a, b)}.
 *
 * @param <V> the values of this level
 */
public interface Level<V> {

    V zero();

    V one();

    V ofInteger(BigInteger n);

    /** A decimal literal, or empty where this level does not take one. */
    Optional<V> ofDecimal(BigDecimal d);

    V add(V a, V b);

    /** {@code a − b}. It is {@code a + (−b)} unless the level's own subtraction differs. */
    default V sub(V a, V b) {
        return add(a, neg(b));
    }

    V mul(V a, V b);

    V neg(V a);

    /** {@code a / b}, or empty where this level cannot divide {@code a} by {@code b}. */
    Optional<V> divide(V a, V b);

    /** {@code base ^ n} for a whole {@code n}, or empty where this level has no such power. */
    Optional<V> power(V base, BigInteger n);

    /** {@code base ^ exponent} for an exponent of this level, or empty where this level has no such power. */
    default Optional<V> pow(V base, V exponent) {
        return Optional.empty();
    }

    /** The value as the calculator writes it. */
    String write(V a);
}
