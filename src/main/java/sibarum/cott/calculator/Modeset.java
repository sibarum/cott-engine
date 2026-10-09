package sibarum.cott.calculator;

import sibarum.cott.algebra.Form;
import sibarum.cott.algebra.NumberType;
import sibarum.cott.algebra.SizeLimit;

import java.util.List;
import java.util.Optional;

/**
 * A family of settings, of which a {@link Calculator} has exactly one {@link Mode} at a time. A front end
 * can offer every modeset without knowing any of them: one control per modeset, listing its
 * {@link #modes()}, and {@link Calculator#set(Mode)} when one is picked.
 */
public enum Modeset {

    /** The numbers at the bottom of every value. */
    NUMBER_TYPE("Number type"),
    /** How large those numbers may be. */
    SIZE_LIMIT("Size limit"),
    /** How a sum and a product nest when they meet. */
    FORM("Form"),
    /** How far a recursion may go before it is stopped. */
    LIMITS("Recursion limits");

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
            case NUMBER_TYPE -> List.of(NumberType.values());
            case SIZE_LIMIT -> List.of(SizeLimit.values());
            case FORM -> List.of(Form.values());
            case LIMITS -> List.of(Limits.values());
        };
    }

    /** The mode a new calculator starts in. */
    public Mode initial() {
        return switch (this) {
            case NUMBER_TYPE -> NumberType.INTEGER;
            case SIZE_LIMIT -> SizeLimit.MEDIUM;
            case FORM -> Form.SUM_OF_PRODUCTS;
            case LIMITS -> Limits.STANDARD;
        };
    }

    public Optional<Mode> mode(String key) {
        return modes().stream().filter(m -> m.key().equals(key)).findFirst();
    }
}
