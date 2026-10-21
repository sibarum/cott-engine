package sibarum.cott.calculator;

/**
 * One setting of a {@link Modeset}: {@link sibarum.cott.algebra.NumberType#INTEGER} is a mode of
 * {@link Modeset#NUMBER_TYPE}. A calculator has exactly one mode of each modeset at a time.
 */
public interface Mode {

    /** The modeset this is one setting of. */
    Modeset modeset();

    /** What a user is shown: {@code Integer}. */
    String label();

    /** A short name to type or store, unique among all modes: {@code integer}. */
    String key();
}
