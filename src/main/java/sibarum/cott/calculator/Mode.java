package sibarum.cott.calculator;

/**
 * One setting of a {@link Modeset}: {@link Arithmetic#IEEE_FLOATING_POINT} is a mode of
 * {@link Modeset#ARITHMETIC}. A calculator has exactly one mode of each modeset at a time.
 */
public sealed interface Mode permits Arithmetic {

    /** The modeset this is one setting of. */
    Modeset modeset();

    /** What a user is shown: {@code IEEE Floating Point}. */
    String label();

    /** A short name to type or store, unique within the modeset: {@code ieee}. */
    String key();
}
