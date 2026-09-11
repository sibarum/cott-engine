package sibarum.cott.engine.rational.expr;

import sibarum.cott.engine.base.expr.IExpr;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Optional;

/**
 * A rational coordinate: the real part of a traction, or an exponent.
 * <p>
 * Coordinates are kept exactly as they arise: nothing is reduced by a common factor, so {@code (2, 4)} and
 * {@code (1, 2)} are the same value at different coordinates and are not the same literal. Equality is
 * coordinate equality throughout.
 *
 * <h2>There is no zero denominator</h2>
 * This is the whole difference from the pair that came before it. Omega used to live here as {@code (1, 0)},
 * because a coordinate pair was the only thing the carrier had that could hold {@code 1÷0}. It does not need
 * to: {@code w} is {@code 0^-1}, and the traction pair spells that directly. So division by zero is not
 * arithmetic here -- it is E9, and it happens as a rule, in view of a derivation.
 * <p>
 * What that buys is the two defects the docs record as the worst the coordinate model had. Cross-multiplying
 * across a zero denominator sent {@code w + w} to 1 and {@code (-1)·(-1)} to 0; with no zero denominator
 * there is nothing to cross-multiply across.
 *
 * <h2>Which coordinate carries the sign is not settled</h2>
 * The class this replaced was called projective, and dropping that name was a side effect rather than a
 * decision. What went with the zero denominator was the reason a ZERO numerator had to put its sign on the
 * denominator -- {@code -0} needed somewhere to live, and it now lives in the traction pair as
 * {@code (-1, 1)}. That removed a requirement. It did not decide that the sign may no longer sit there, and
 * the two are not the same thing.
 * <p>
 * So the sign still moves. {@link #reciprocal()} swaps the coordinates, so {@code 1÷(-1)} is {@code (1, -1)}
 * and {@code (-1)÷1} is {@code (-1, 1)} -- one value at two coordinates, which is the same kind of fact as
 * {@code (2, 4)} and {@code (1, 2)} being one value at two coordinates. Since matching is on terms and never
 * on values, nothing downstream reads them as equal.
 * <p>
 * Whether that difference carries information -- an orientation -- is open, and nothing here should be read
 * as answering it. The older class had an {@code orientation()} that put the sign on whichever coordinate
 * could hold it; it is not restored, because what orientation would MEAN is not settled either. Note that
 * the question no longer stands or falls with {@code -0}: {@code -w} is a distinct value from {@code 0}
 * whichever way this goes.
 *
 * @param numerator
 * @param denominator never zero
 */
public record RationalLiteral(BigInteger numerator, BigInteger denominator) implements IExpr {

    public static final RationalLiteral ZERO = of(0, 1);
    public static final RationalLiteral ONE = of(1, 1);
    public static final RationalLiteral NEG_ONE = of(-1, 1);

    public RationalLiteral {
        if (denominator.signum() == 0) {
            throw new IllegalArgumentException(
                    "a zero denominator is not a rational: 1÷0 is w by E9, which is the traction 0^-1");
        }
    }

    /** The ordinary way to write one down. */
    public static RationalLiteral of(long numerator, long denominator) {
        return new RationalLiteral(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    public boolean isZero() {
        return numerator.signum() == 0;
    }

    /** Whether this is an integer as a TERM, which is what the power rule is allowed to ask. */
    public boolean isInteger() {
        return denominator.equals(BigInteger.ONE);
    }

    @Override
    public IExpr plus(IExpr expr) {
        if (expr instanceof RationalLiteral(BigInteger n, BigInteger d)) {
            // A common denominator is used when there already is one. Cross-multiplication is how you MAKE a
            // common denominator, and applying it regardless invents one you were already holding: it sent
            // 1/2 + 1/2 to (4,4) rather than (2,2), and cubed the denominator over three additions. Since
            // nothing reduces afterwards, that inflation is permanent and visible -- adding a value to itself
            // and doubling it by multiplication landed on different coordinates.
            if (this.denominator.equals(d)) {
                return new RationalLiteral(this.numerator.add(n), this.denominator);
            }
            return new RationalLiteral(
                    this.numerator.multiply(d).add(n.multiply(this.denominator)),
                    this.denominator.multiply(d));
        }
        return IExpr.super.plus(expr);
    }

    /**
     * Negation turns the numerator, and only the numerator.
     * <p>
     * That is what this method does; it is not a claim about where the sign may be. {@link #reciprocal()}
     * puts it on the denominator, and the class header says why that is left standing.
     * <p>
     * A zero numerator therefore negates to itself here. That is right for what this literal is asked to be
     * in the carrier -- an exponent, or a real part -- because the VALUE zero is not this literal at all:
     * {@code 0} the value is {@code 0^1}, the traction pair {@code (0, 1)}, and its negation is
     * {@code (-1, 1)}, an unresolved {@code -1·0}. So {@code -0} does not need a spelling here, which is the
     * requirement the older carrier had and this one does not.
     * <p>
     * It is not evidence that a zero numerator HAS no orientation, only that nothing currently asks it for
     * one.
     */
    @Override
    public IExpr negated() {
        return new RationalLiteral(this.numerator.negate(), this.denominator);
    }

    @Override
    public IExpr times(IExpr expr) {
        if (expr instanceof RationalLiteral(BigInteger n, BigInteger d)) {
            return new RationalLiteral(this.numerator.multiply(n), this.denominator.multiply(d));
        }
        return IExpr.super.times(expr);
    }

    /**
     * The two coordinates trade places, sign and all.
     * <p>
     * So this is where a negative denominator comes from: {@code 1÷(-1)} is {@code (1, -1)}, where
     * {@code (-1)÷1} is {@code (-1, 1)}. One value, two coordinates, and not the same literal -- which is
     * what this pair does everywhere, {@code (2, 4)} against {@code (1, 2)} included. It is left alone
     * rather than normalised; see the class header.
     * <p>
     * Not at a zero numerator: {@code 1÷0} is E9 and the answer is a traction, not a pair. A rule does that,
     * so a derivation shows the axiom being used instead of finding it already applied. Here the term stands.
     */
    @Override
    public IExpr reciprocal() {
        if (isZero()) {
            return IExpr.super.reciprocal();
        }
        return new RationalLiteral(this.denominator, this.numerator);
    }

    /**
     * The rational this names, as a real.
     * <p>
     * The division is done on the coordinates rather than on their doubles, because coordinates outgrow a
     * double long before the value they name does: {@code (10^400, 10^400)} is one, and dividing the two
     * shadows would ask what infinity over infinity is.
     */
    @Override
    public Optional<Double> evaluate() {
        return Optional.of(new BigDecimal(this.numerator)
                .divide(new BigDecimal(this.denominator), MathContext.DECIMAL64)
                .doubleValue());
    }
}
