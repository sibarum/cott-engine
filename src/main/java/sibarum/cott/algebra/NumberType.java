package sibarum.cott.algebra;

import sibarum.cott.calculator.Mode;
import sibarum.cott.calculator.Modeset;

/**
 * The numbers at the bottom of every value. A pair of Integers under {@code Q} is the rationals, unreduced; a Decimal
 * can divide, so it can bring an expression down to one pair; IEEE 64-bit is the {@code double}.
 */
public enum NumberType implements Mode {

    INTEGER("integer", "Integer"),
    DECIMAL("decimal", "Decimal"),
    IEEE("ieee64", "IEEE 64-bit");

    private final String key;
    private final String label;

    NumberType(String key, String label) {
        this.key = key;
        this.label = label;
    }

    @Override
    public Modeset modeset() {
        return Modeset.NUMBER_TYPE;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String label() {
        return label;
    }

    /**
     * A level of these numbers within this limit, for one evaluation. A Decimal level remembers whether it rounded,
     * so each evaluation takes a new one.
     */
    public Level<?> level(SizeLimit limit) {
        return switch (this) {
            case INTEGER -> new IntegerLevel(limit.integerBits());
            case DECIMAL -> new DecimalLevel(limit.decimalDigits());
            case IEEE -> new IeeeLevel();
        };
    }
}
