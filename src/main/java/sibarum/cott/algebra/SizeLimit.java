package sibarum.cott.algebra;

/**
 * How large a number of a {@link NumberType} may be. An {@link NumberType#INTEGER Integer} over its limit cannot be
 * rounded without changing the pair it is in, so it is refused. A {@link NumberType#DECIMAL Decimal} is rounded to
 * its limit, and the level says that it was. {@link NumberType#IEEE IEEE 64-bit} is a {@code double}, whatever the
 * limit.
 */
public enum SizeLimit {

    SMALL("small", "Small: 64 bits, 16 digits", 64, 16),
    STANDARD("standard", "Standard: 4096 bits, 34 digits", 4096, 34),
    LARGE("large", "Large: 1 MiB, 1000 digits", 8L * 1024 * 1024, 1000);

    private final String key;
    private final String label;
    private final long integerBits;
    private final int decimalDigits;

    SizeLimit(String key, String label, long integerBits, int decimalDigits) {
        this.key = key;
        this.label = label;
        this.integerBits = integerBits;
        this.decimalDigits = decimalDigits;
    }

    public String key() {
        return key;
    }

    public String label() {
        return label;
    }

    /** The most bits an Integer may have, sign apart. */
    public long integerBits() {
        return integerBits;
    }

    /** The significant digits a Decimal is rounded to. */
    public int decimalDigits() {
        return decimalDigits;
    }
}
