package sibarum.cott.engine.projective.expr;

import sibarum.cott.engine.base.expr.IExpr;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Optional;

/**
 * "Projective" as in coordinates, not an equivalence class.
 * <p>
 * Coordinates are kept exactly as they arise: nothing is reduced by a common factor and no sign is
 * moved between the two slots, so {@code (2, 4)} and {@code (1, 2)} are the same value at different
 * coordinates and are not the same literal. Equality is coordinate equality throughout.
 * <p>
 * The coordinates are {@link BigInteger} because nothing here reduces. Every sum and every product
 * multiplies denominators, so they only ever grow, and a fixed-width coordinate would decide how
 * long an exact engine stays exact -- silently, at whatever depth the multiplication happened to
 * wrap. Growth is the design, so the coordinate has to be unbounded; {@link #of} keeps the ordinary
 * spellings short.
 *
 * @param numerator
 * @param denominator
 */
public record ProjectiveRationalLiteral(BigInteger numerator, BigInteger denominator) implements IExpr {

    public static final ProjectiveRationalLiteral ZERO = of(0, 1);
    public static final ProjectiveRationalLiteral ONE = of(1, 1);
    public static final ProjectiveRationalLiteral NEG_ONE = of(-1, 1);
    public static final ProjectiveRationalLiteral OMEGA = of(1, 0);

    public ProjectiveRationalLiteral {
        if (numerator.signum() == 0 && denominator.signum() == 0) {
            numerator = BigInteger.ONE;
            denominator = BigInteger.ONE;
        }
    }

    /** The ordinary way to write one down. */
    public static ProjectiveRationalLiteral of(long numerator, long denominator) {
        return new ProjectiveRationalLiteral(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    @Override
    public IExpr plus(IExpr expr) {
        if (expr instanceof ProjectiveRationalLiteral(BigInteger numerator1, BigInteger denominator1)) {
            // A common denominator is used when there already is one. Cross-multiplication is how you MAKE a
            // common denominator, and applying it regardless invents one you were already holding: it sent
            // 1/2 + 1/2 to (4,4) rather than (2,2), and cubed the denominator over three additions. Since
            // nothing reduces afterwards, that inflation is permanent and it is visible -- adding a value to
            // itself and doubling it by multiplication landed on different coordinates.
            //
            // It also breaks at a zero denominator, where every cross term picks up a zero factor: w + w came
            // out as (0,0), which is one, while 2·w is (2,0). The identity (a/b + c/d) = (ad+cb)/(bd) is
            // derived on the assumption that bd is not zero, so omega was never a special case -- it was the
            // place the general defect could not be missed.
            //
            // This does not reduce anything: 1/2 + 1/2 is (2,2), which is still not the literal one.
            if (this.denominator.equals(denominator1)) {
                return new ProjectiveRationalLiteral(this.numerator.add(numerator1), this.denominator);
            }
            return new ProjectiveRationalLiteral(
                    this.numerator.multiply(denominator1).add(numerator1.multiply(this.denominator)),
                    this.denominator.multiply(denominator1));
        } else {
            return IExpr.super.plus(expr);
        }
    }

    /**
     * The sign is carried by the numerator, so that {@code ONE.negated()} is {@code NEG_ONE} rather
     * than a second spelling of it. Zero negates to itself; omega does not.
     */
    @Override
    public IExpr negated() {
        return new ProjectiveRationalLiteral(this.numerator.negate(), this.denominator);
    }

    @Override
    public IExpr times(IExpr expr) {
        if (expr instanceof ProjectiveRationalLiteral(BigInteger numerator1, BigInteger denominator1)) {
            return new ProjectiveRationalLiteral(
                    this.numerator.multiply(numerator1),
                    this.denominator.multiply(denominator1));
        } else {
            return IExpr.super.times(expr);
        }
    }

    /**
     * The two coordinates trade places, which is what makes zero invertible: {@code ZERO} and
     * {@code OMEGA} are each other's reciprocal, and neither is a special case.
     */
    @Override
    public IExpr reciprocal() {
        return new ProjectiveRationalLiteral(this.denominator, this.numerator);
    }

    /**
     * A literal is already as far reduced as it goes -- reducing coordinates is exactly what this
     * type does not do.
     */
    @Override
    public IExpr simplify() {
        return this;
    }

    /**
     * The default projection: the rational shadow this value casts on the real line. Omega has a
     * zero magnitude, so it projects onto the same coordinate as zero rather than onto an infinity
     * the type does not contain.
     * <p>
     * The division is done on the coordinates rather than on their doubles, because coordinates
     * outgrow a double long before the value they name does: {@code (10^400, 10^400)} is one, and
     * dividing the two shadows would ask what infinity over infinity is.
     */
    @Override
    public Optional<Double> evaluate() {
        if (this.denominator.signum() == 0) {
            return Optional.of(0.0);
        }
        return Optional.of(new BigDecimal(this.numerator)
                .divide(new BigDecimal(this.denominator), MathContext.DECIMAL64)
                .doubleValue());
    }
}
