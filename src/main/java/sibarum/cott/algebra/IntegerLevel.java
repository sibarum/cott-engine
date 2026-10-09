package sibarum.cott.algebra;

import sibarum.cott.calculator.CalculatorException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * {@link NumberType#INTEGER}: {@link BigInteger}, exact. There is no division here, not even where it would come out
 * whole: {@code 6/3} is the pair {@code Q(6, 3)}, and answering {@code 2} would be a quotient. A number with more bits
 * than the limit is refused, since rounding it would change the pair it is in.
 */
public final class IntegerLevel implements Level<BigInteger> {

    private final long maxBits;

    public IntegerLevel(long maxBits) {
        this.maxBits = maxBits;
    }

    @Override
    public BigInteger zero() {
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger one() {
        return BigInteger.ONE;
    }

    @Override
    public BigInteger ofInteger(BigInteger n) {
        return checked(n);
    }

    /** Whether {@code 0.5} is a pair over a power of ten or its lowest terms is not chosen, so none is taken. */
    @Override
    public Optional<BigInteger> ofDecimal(BigDecimal d) {
        return Optional.empty();
    }

    @Override
    public BigInteger add(BigInteger a, BigInteger b) {
        return checked(a.add(b));
    }

    @Override
    public BigInteger mul(BigInteger a, BigInteger b) {
        if ((long) a.bitLength() + b.bitLength() > maxBits + 1) tooLarge("a product");
        return checked(a.multiply(b));
    }

    @Override
    public BigInteger neg(BigInteger a) {
        return checked(a.negate());
    }

    @Override
    public Optional<BigInteger> divide(BigInteger a, BigInteger b) {
        return Optional.empty();
    }

    /** An integer exponent is a whole power. */
    @Override
    public Optional<BigInteger> pow(BigInteger base, BigInteger exponent) {
        return power(base, exponent);
    }

    /** A non-negative power; a negative one is not an integer. */
    @Override
    public Optional<BigInteger> power(BigInteger base, BigInteger exponent) {
        if (exponent.signum() < 0) return Optional.empty();
        if (base.abs().compareTo(BigInteger.ONE) <= 0)
            return Optional.of(exponent.signum() == 0 ? BigInteger.ONE : base.signum() >= 0 || !exponent.testBit(0) ? base.abs() : base);
        // |base| ≥ 2, so the power has more than exponent · (bits of base − 1) bits
        if (exponent.bitLength() > 62 || exponent.longValue() >= maxBits
                || (base.bitLength() - 1) * exponent.longValue() >= maxBits) tooLarge("a power");
        return Optional.of(checked(base.pow(exponent.intValueExact())));
    }

    @Override
    public String write(BigInteger a) {
        return a.toString();
    }

    private BigInteger checked(BigInteger n) {
        if (n.bitLength() > maxBits) tooLarge("an integer");
        return n;
    }

    private void tooLarge(String what) {
        throw new CalculatorException(what + " would have more than " + maxBits + " bits, past the size limit");
    }
}
