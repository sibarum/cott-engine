package sibarum.cott;

import java.math.BigInteger;

/**
 * An exact rational, reduced, with a positive denominator.
 *
 * <p>Arbitrary precision rather than {@code long}: exponents multiply under {@code pow}, so
 * {@code ((2^5)^5)^5} reaches numbers a {@code long} silently wraps. The reduction is deliberate
 * and is exactly why the exponent carries a separate torsion slot — {@code 0/2} reduces to
 * {@code 0} here, so a root of the residue zero cannot be represented as a rational and needs a
 * coordinate of its own. See {@link Exp}.
 */
public final class Rational implements Comparable<Rational> {

    public static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);
    public static final Rational ONE = new Rational(BigInteger.ONE, BigInteger.ONE);
    public static final Rational TWO = new Rational(BigInteger.TWO, BigInteger.ONE);

    private final BigInteger num;
    private final BigInteger den;

    private Rational(BigInteger num, BigInteger den) {
        this.num = num;
        this.den = den;
    }

    public static Rational of(long n) {
        return new Rational(BigInteger.valueOf(n), BigInteger.ONE);
    }

    public static Rational of(long n, long d) {
        return of(BigInteger.valueOf(n), BigInteger.valueOf(d));
    }

    public static Rational of(BigInteger n, BigInteger d) {
        if (d.signum() == 0) {
            // Division by zero is a POINT here (omega), never a rational. Reaching this means a
            // caller skipped the guard the laws all carry, so fail loudly rather than invent ω.
            throw new ArithmeticException("rational with zero denominator");
        }
        BigInteger sn = d.signum() < 0 ? n.negate() : n;
        BigInteger sd = d.abs();
        BigInteger g = sn.gcd(sd);
        return g.equals(BigInteger.ONE) ? new Rational(sn, sd)
                : new Rational(sn.divide(g), sd.divide(g));
    }

    /** Parse the forms this class prints: {@code 3}, {@code -3}, {@code 5/2}. */
    public static Rational parse(String s) {
        int slash = s.indexOf('/');
        return slash < 0
                ? new Rational(new BigInteger(s.trim()), BigInteger.ONE)
                : of(new BigInteger(s.substring(0, slash).trim()),
                        new BigInteger(s.substring(slash + 1).trim()));
    }

    public BigInteger numerator() {
        return num;
    }

    public BigInteger denominator() {
        return den;
    }

    public Rational add(Rational o) {
        return of(num.multiply(o.den).add(o.num.multiply(den)), den.multiply(o.den));
    }

    public Rational subtract(Rational o) {
        return add(o.negate());
    }

    public Rational negate() {
        return new Rational(num.negate(), den);
    }

    public Rational multiply(Rational o) {
        return of(num.multiply(o.num), den.multiply(o.den));
    }

    public Rational divide(Rational o) {
        if (o.isZero()) {
            throw new ArithmeticException("rational division by zero");
        }
        return of(num.multiply(o.den), den.multiply(o.num));
    }

    public Rational reciprocal() {
        return ONE.divide(this);
    }

    /** The greatest integer not above this. Used by the twist and torsion closures. */
    public BigInteger floor() {
        BigInteger[] qr = num.divideAndRemainder(den);
        return qr[1].signum() < 0 ? qr[0].subtract(BigInteger.ONE) : qr[0];
    }

    /** This raised to a non-negative whole power. Only that case is rational. */
    public Rational pow(BigInteger e) {
        if (e.signum() < 0) {
            throw new ArithmeticException("negative exponent");
        }
        int n = e.intValueExact();
        return of(num.pow(n), den.pow(n));
    }

    public boolean isZero() {
        return num.signum() == 0;
    }

    public boolean isOne() {
        return num.equals(BigInteger.ONE) && den.equals(BigInteger.ONE);
    }

    /** Whether this is a whole number — the {@code G1 :: Nat} style guard the laws use. */
    public boolean isInteger() {
        return den.equals(BigInteger.ONE);
    }

    public boolean isNatural() {
        return isInteger() && num.signum() >= 0;
    }

    public int signum() {
        return num.signum();
    }

    @Override
    public int compareTo(Rational o) {
        return num.multiply(o.den).compareTo(o.num.multiply(den));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Rational r && num.equals(r.num) && den.equals(r.den);
    }

    @Override
    public int hashCode() {
        return num.hashCode() * 31 + den.hashCode();
    }

    /** {@code 3}, {@code -3}, {@code 5/2} — the form {@link #parse} reads back. */
    @Override
    public String toString() {
        return den.equals(BigInteger.ONE) ? num.toString() : num + "/" + den;
    }
}
