package sibarum.cott.calculator;

import java.math.BigInteger;

/**
 * The {@link Modeset#LIMITS} modes: how far a recursion may go before the calculator stops it. Each bound is a
 * count, not a time, so a line gives the same answer on every machine.
 *
 * <p>For {@code cos} and {@code sin}, the mediant descent stops at the first of:
 * <ul>
 *   <li>{@code precision}: the bracket's {@code N(L)·N(R)} reaches {@code 2^precision}, so the angle between
 *       its ends has a squared sine of at most {@code 2^-precision};</li>
 *   <li>{@code maxSteps}: that many steps;</li>
 *   <li>{@code maxPowerBits}: the next comparison would walk a power with more bits than this. Its size is
 *       about {@code 8b} times the mediant's bits, and it is what the comparison's cost grows with.</li>
 * </ul>
 * A turn's denominator is at most {@code maxDenominator}. {@code cos(t, n)} takes {@code n} up to
 * {@code maxSteps}, and is refused rather than cut short if the power size would stop it first.
 */
public enum Limits implements Mode {

    SHALLOW("shallow", "Shallow: 2^-32, 256 steps, b ≤ 360", 32, 256, 360, 1L << 16),
    STANDARD("standard", "Standard: 2^-64, 1024 steps, b ≤ 1000", 64, 1024, 1000, 1L << 19),
    DEEP("deep", "Deep: 2^-128, 16384 steps, b ≤ 10000; can take hours", 128, 16384, 10_000, 1L << 24);

    private final String key;
    private final String label;
    private final int precision;
    private final int maxSteps;
    private final long maxDenominator;
    private final long maxPowerBits;

    Limits(String key, String label, int precision, int maxSteps, long maxDenominator, long maxPowerBits) {
        this.key = key;
        this.label = label;
        this.precision = precision;
        this.maxSteps = maxSteps;
        this.maxDenominator = maxDenominator;
        this.maxPowerBits = maxPowerBits;
    }

    @Override
    public Modeset modeset() {
        return Modeset.LIMITS;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String key() {
        return key;
    }

    /** {@code 2^precision}: the descent stops once {@code N(L)·N(R)} reaches it. */
    public BigInteger width() {
        return BigInteger.ONE.shiftLeft(precision);
    }

    public int precision() {
        return precision;
    }

    public int maxSteps() {
        return maxSteps;
    }

    public long maxDenominator() {
        return maxDenominator;
    }

    public long maxPowerBits() {
        return maxPowerBits;
    }
}
