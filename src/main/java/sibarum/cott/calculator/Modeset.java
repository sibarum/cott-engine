package sibarum.cott.calculator;

import java.util.List;
import java.util.Optional;

/**
 * A family of settings, of which a {@link Calculator} has exactly one {@link Mode} at a time. A front end
 * can offer every modeset without knowing any of them: one control per modeset, listing its
 * {@link #modes()}, and {@link Calculator#set(Mode)} when one is picked.
 */
public enum Modeset {

    /** What a value is, and how the operations act on it. */
    ARITHMETIC("Arithmetic");

    private final String label;

    Modeset(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Every mode of this modeset, in the order they are offered. */
    public List<Mode> modes() {
        return switch (this) {
            case ARITHMETIC -> List.of(Arithmetic.values());
        };
    }

    /** The mode a new calculator starts in. */
    public Mode initial() {
        return switch (this) {
            case ARITHMETIC -> Arithmetic.TRACTION_RATIO;
        };
    }

    public Optional<Mode> mode(String key) {
        return modes().stream().filter(m -> m.key().equals(key)).findFirst();
    }
}
