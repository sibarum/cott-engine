package sibarum.cott.projection;

import java.math.BigInteger;

/** A rational number in lowest terms with a positive denominator: an element of ℚ, not a traction. */
public record Rational(BigInteger num, BigInteger den) {

    public static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);

    public Rational {
        if (den.signum() == 0) throw new ArithmeticException("a rational has a non-zero denominator");
        BigInteger g = num.gcd(den);
        if (den.signum() < 0) g = g.negate();
        num = num.divide(g);
        den = den.divide(g);
    }

    public static Rational of(long num, long den) {
        return new Rational(BigInteger.valueOf(num), BigInteger.valueOf(den));
    }

    public Rational plus(Rational y) {
        return new Rational(num.multiply(y.den).add(y.num.multiply(den)), den.multiply(y.den));
    }

    public Rational times(Rational y) {
        return new Rational(num.multiply(y.num), den.multiply(y.den));
    }

    public Rational neg() {
        return new Rational(num.negate(), den);
    }

    /** {@code 1/r}, for {@code r ≠ 0}. */
    public Rational inverse() {
        return new Rational(den, num);
    }

    @Override
    public String toString() {
        return den.equals(BigInteger.ONE) ? num.toString() : num + "/" + den;
    }
}
