package sibarum.cott.algebra;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * {@link NumberType#IEEE}: IEEE 754 binary64, the {@code double}. Each operation is the IEEE operation itself,
 * rounded to nearest, ties to even: {@code a − b} is subtraction and {@code a / b} is division, not
 * {@code a + (−b)} and {@code a · (1/b)}, which round differently. So {@code 1/0} is {@code ∞}, {@code 0/0} is
 * {@code NaN}, and {@code −0} is kept apart from {@code 0}. {@code ^} is {@link StrictMath#pow}, the same on every
 * machine.
 */
public final class IeeeLevel implements Level<Double> {

    @Override
    public Double zero() {
        return 0.0;
    }

    @Override
    public Double one() {
        return 1.0;
    }

    /** Rounded once, to the nearest double. */
    @Override
    public Double ofInteger(BigInteger n) {
        return n.doubleValue();
    }

    @Override
    public Optional<Double> ofDecimal(BigDecimal d) {
        return Optional.of(d.doubleValue());
    }

    @Override
    public Double add(Double a, Double b) {
        return a + b;
    }

    @Override
    public Double sub(Double a, Double b) {
        return a - b;
    }

    @Override
    public Double mul(Double a, Double b) {
        return a * b;
    }

    @Override
    public Double neg(Double a) {
        return -a;
    }

    @Override
    public Optional<Double> divide(Double a, Double b) {
        return Optional.of(a / b);
    }

    @Override
    public Optional<Double> pow(Double base, Double exponent) {
        return Optional.of(StrictMath.pow(base, exponent));
    }

    @Override
    public Optional<Double> power(Double base, BigInteger n) {
        return Optional.of(StrictMath.pow(base, n.doubleValue()));
    }

    @Override
    public String write(Double a) {
        return display(a);
    }

    /**
     * A double the way a calculator writes it: the fewest digits that read back as the same double, in
     * positional notation from {@code 10^-6} up to {@code 10^21} and as {@code 1.5·10^-7} outside that, which reads back.
     * {@code ∞}, {@code -∞}, {@code NaN} and {@code -0} are written as themselves.
     */
    public static String display(double x) {
        if (Double.isNaN(x)) return "NaN";
        if (Double.isInfinite(x)) return x > 0 ? "∞" : "-∞";
        if (x == 0) return Double.doubleToRawLongBits(x) < 0 ? "-0" : "0";
        BigDecimal shortest = new BigDecimal(Double.toString(x)).stripTrailingZeros();
        // Double.toString gives two digits where one reads back but two are nearer: 4.9e-324 for 5e-324
        BigDecimal oneDigit = shortest.round(new MathContext(1, RoundingMode.HALF_EVEN));
        if (oneDigit.doubleValue() == x) shortest = oneDigit.stripTrailingZeros();
        double magnitude = Math.abs(x);
        if (magnitude >= 1e-6 && magnitude < 1e21) return shortest.toPlainString();
        String digits = shortest.unscaledValue().abs().toString();
        int exponent = digits.length() - 1 - shortest.scale();
        String mantissa = digits.length() == 1 ? digits : digits.charAt(0) + "." + digits.substring(1);
        return (x < 0 ? "-" : "") + mantissa + "·10^" + exponent;
    }

    /** The exact value the double holds, which a decimal literal is usually only near. */
    public static String exact(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x) || x == 0) return display(x);
        BigDecimal exact = new BigDecimal(x);
        double magnitude = Math.abs(x);
        return magnitude >= 1e-6 && magnitude < 1e21 ? exact.toPlainString() : exact.toString().replace("E+", "·10^").replace("E", "·10^");
    }

    /**
     * The 64 bits: sign, 11 of exponent, 52 of fraction. A NaN is written as the one canonical NaN, since
     * which NaN an operation leaves differs from one processor to another.
     */
    public static String bits(double x) {
        return String.format("0x%016X", Double.doubleToLongBits(x));
    }
}
