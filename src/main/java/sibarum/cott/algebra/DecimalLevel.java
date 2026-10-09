package sibarum.cott.algebra;

import sibarum.cott.calculator.CalculatorException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * {@link NumberType#DECIMAL}: {@link BigDecimal}, kept to a number of significant digits. {@code +}, {@code −} and
 * {@code ·} are exact until a result has more digits than the limit, and {@code /} is exact where the quotient
 * ends; past that each result is rounded half to even. The level remembers whether any result of the evaluation
 * was rounded, so an answer that is only near its value can say so.
 */
public final class DecimalLevel implements Level<BigDecimal> {

    private final MathContext context;
    private boolean rounded;

    public DecimalLevel(int digits) {
        this.context = new MathContext(digits, RoundingMode.HALF_EVEN);
    }

    /** Whether any value this level gave was rounded to the limit. */
    public boolean rounded() {
        return rounded;
    }

    @Override
    public BigDecimal zero() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal one() {
        return BigDecimal.ONE;
    }

    @Override
    public BigDecimal ofInteger(BigInteger n) {
        return limited(new BigDecimal(n));
    }

    @Override
    public Optional<BigDecimal> ofDecimal(BigDecimal d) {
        return Optional.of(limited(d));
    }

    @Override
    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return limited(a.add(b));
    }

    @Override
    public BigDecimal sub(BigDecimal a, BigDecimal b) {
        return limited(a.subtract(b));
    }

    @Override
    public BigDecimal mul(BigDecimal a, BigDecimal b) {
        return limited(a.multiply(b));
    }

    @Override
    public BigDecimal neg(BigDecimal a) {
        return a.negate();
    }

    /** Empty when {@code b} is zero: there the value meets {@code Q}, as over the integers. */
    @Override
    public Optional<BigDecimal> divide(BigDecimal a, BigDecimal b) {
        if (b.signum() == 0) return Optional.empty();
        try {
            return Optional.of(limited(a.divide(b)));
        } catch (ArithmeticException noEnd) {
            rounded = true;
            return Optional.of(a.divide(b, context));
        }
    }

    /** A whole exponent; any other is empty. */
    @Override
    public Optional<BigDecimal> pow(BigDecimal base, BigDecimal exponent) {
        try {
            return power(base, exponent.toBigIntegerExact());
        } catch (ArithmeticException notWhole) {
            return Optional.empty();
        }
    }

    /**
     * By squaring, each product kept to the limit. A negative exponent is the reciprocal of the power, and is empty
     * for a zero base.
     */
    @Override
    public Optional<BigDecimal> power(BigDecimal base, BigInteger n) {
        BigDecimal result = BigDecimal.ONE, square = base;
        try {
            for (BigInteger k = n.abs(); k.signum() > 0; k = k.shiftRight(1)) {
                if (k.testBit(0)) result = mul(result, square);
                if (k.bitLength() > 1) square = mul(square, square);
            }
        } catch (ArithmeticException outOfRange) {
            throw new CalculatorException("a power of " + write(base) + " is out of a decimal's range");
        }
        return n.signum() < 0 ? divide(BigDecimal.ONE, result) : Optional.of(result);
    }

    /** Positional from {@code 10^-6} up to {@code 10^21}, and as {@code 1.5e-7} outside that, as a double is written. */
    @Override
    public String write(BigDecimal a) {
        if (a.signum() == 0) return a.scale() <= 0 ? "0" : a.toPlainString();
        int exponent = a.precision() - a.scale() - 1;
        if (exponent >= -6 && exponent < 21) return a.toPlainString();
        String digits = a.unscaledValue().abs().toString();
        String mantissa = digits.length() == 1 ? digits : digits.charAt(0) + "." + digits.substring(1);
        return (a.signum() < 0 ? "-" : "") + mantissa + "e" + exponent;
    }

    private BigDecimal limited(BigDecimal exact) {
        if (exact.precision() <= context.getPrecision()) return exact;
        BigDecimal r = exact.round(context);
        if (r.compareTo(exact) != 0) rounded = true;
        return r;
    }
}
