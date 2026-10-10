package sibarum.cott.algebra;

/**
 * How much of a value an answer or a reading still holds, from all of it to none. Each rung keeps less than the one
 * above it.
 */
public enum Rung {

    /** Everything: the pair as computed, or a reading that loses nothing of it. */
    EXACT("exact"),
    /** The value, written as another equation: a different pair, which cannot be turned back into this one. */
    SAME_VALUE("same value"),
    /** The algebra's reading: every pair with the same reading gives the same answer, as {@code Q(6, 2)} and {@code Q(3, 1)}. */
    QUOTIENT("quotient"),
    /** Part of the information only, as a direction without its scale: many values give this answer. */
    UP_TO_INVARIANT("up to invariant"),
    /** Within a bound: information may have been lost, as a rounded Decimal or a double. */
    UP_TO_ERROR("up to error"),
    /** Nothing: total loss, as a double's {@code NaN}. */
    ERROR("error");

    private final String label;

    Rung(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
