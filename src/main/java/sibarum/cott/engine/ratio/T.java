package sibarum.cott.engine.ratio;

import sibarum.cott.engine.base.expr.IExpr;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Optional;

/**
 * {@code T(p, q)}, the ratio of two numbers: docs/Traction-Model.md, implemented as written.
 * <pre>
 *  T(p, q) := p÷q = tan(θ)
 *  θ = arg(q + pi)
 * </pre>
 * The pair is the point {@code q + pi} in the plane and the ratio is the tangent of the angle it stands at.
 * Those are two readings of one pair rather than two things: {@code T(1,0)} is {@code 1÷0}, and it is the
 * quarter turn, and it is ω.
 *
 * <h2>A zero denominator is the point of it</h2>
 * That is the whole difference from {@code RationalLiteral}, which refuses one -- there ω is not a ratio at
 * all, it is {@code 0^-1}, and the traction pair spells it. Here it is {@code T(1,0)}, a quarter turn, and
 * {@code T(-1,0)} is the other one. So this type stands beside that carrier rather than replacing it; which
 * of them carries a value is Traction-Theory.md's Carrier question and nothing here answers it.
 *
 * <h2>Two arithmetics on one pair, by position</h2>
 * The model's table gives each position its own {@code +} and its own {@code ·}:
 * <pre>
 *        value position          exponent position
 *  +     T(ad+bc, bd)            T(a+c, b+d)        [0^z1 · 0^z2]
 *  ·     T(ac, bd)               T(ad+bc, bd−ac)    [(0^z1)^z2]
 *  unit  1÷1                     0÷0  and  0÷1
 *  inv   T(b,a)                  T(-a,-b),  T(-a,b)
 * </pre>
 * {@link #plus} and {@link #times} are the value-position pair, {@link #oplus} (⊕) and {@link #otimes} (⊗)
 * the exponent-position pair. The model's {@code X(a,b)} is this same record read in the exponent position,
 * so it is those two methods rather than a second class -- {@code X}'s {@code +} is ⊕ and its {@code ·} is ⊗.
 *
 * <p>⊕ is the mediant, and it is what {@code 0^a · 0^b = 0^(a⊕b)} adds with. ⊗ multiplies the points:
 * {@code (b+ai)(d+ci) = (bd−ac) + (ad+bc)i}, so it adds the angles, which is the tangent addition formula.
 *
 * <h2>Nothing is reduced and nothing is normalised</h2>
 * Coordinates are kept exactly as they arise, so {@code T(1,2)} and {@code T(2,4)} are the same ratio at
 * different coordinates and are not the same value here. Equality is coordinate equality throughout, as it
 * is in the rest of the engine.
 *
 * <p>All four sign placements stay distinct, because the model's table keeps them distinct: {@code T(0,1)}
 * is 0 and {@code T(0,-1)} is -0, {@code T(1,1)} is 1 and {@code T(-1,-1)} is the point at {@code -1-i}. No
 * sign is moved into the numerator, which is what the word oriented is doing in "an oriented projective
 * rational" -- the orientation is which of the four the point stands in, and the angle reads it directly
 * where the ratio cannot.
 *
 * <p>{@code T(0,0)} least of all. It is the unit of ⊕, so a type that turned it into {@code T(1,1)} would
 * leave the exponent-position sum with no identity that can be written down -- and nothing else here turns
 * it either, not in the constructor and not on the way out through a reading. The table's
 * {@code 0÷0 --> (1:1)} is {@code x÷x} applied to a pair, which is a reading and not the pair; a computation
 * that wants it applies it in its own code, where the choice can be seen. What the readings do instead is
 * decline: {@link #evaluate()} answers nothing there and {@link #projection()} and {@link #theta()} answer
 * {@code NaN}, because the origin names no number and stands at no angle.
 *
 * @param p the numerator, and the imaginary part of the point {@code q + pi}
 * @param q the denominator, and the real part of that point; zero here, where a rational's may not be
 */
public record T(BigInteger p, BigInteger q) implements IExpr {

    /** {@code 0 = T(0,1)}: the point {@code 1}, at angle 0. */
    public static final T ZERO = of(0, 1);

    /** {@code 1 = T(1,1)}: the point {@code 1+i}, at half a right angle. The value-position unit. */
    public static final T ONE = of(1, 1);

    /** {@code -1 = T(-1,1)}: the point {@code 1-i}. */
    public static final T NEG_ONE = of(-1, 1);

    /** {@code ω = T(1,0)}: the point {@code i}, at a quarter turn -- where the ratio has no real value. */
    public static final T OMEGA = of(1, 0);

    /**
     * {@code 0ω = T(0,0)}: the unit of ⊕, and the pair with no reading of its own.
     * <p>
     * It stays what it is. The table reads it as a value by {@code x÷x = 1}, and that reading belongs to
     * whoever is calculating -- applied here it would cost the ⊕ unit its spelling, and it would be applied
     * silently, at whatever depth a pair happened to pass through a reading. Classically {@code 0÷0} is
     * indeterminate; declining to place it is not agreeing with that, it is declining to decide it here.
     */
    public static final T ZERO_OMEGA = of(0, 0);

    /** {@code T(0,1)}, the unit of ⊗: the point {@code 1}, the angle that adds nothing. */
    public static final T OTIMES_UNIT = ZERO;

    /** The ordinary way to write one down. */
    public static T of(long p, long q) {
        return new T(BigInteger.valueOf(p), BigInteger.valueOf(q));
    }

    /** {@code T(a,b) + T(c,d) = T(ad+bc, bd)}: the value-position sum. */
    public T plus(T that) {
        return new T(
                this.p.multiply(that.q).add(that.p.multiply(this.q)),
                this.q.multiply(that.q));
    }

    /** {@code T(a,b) · T(c,d) = T(ac, bd)}: the value-position product. */
    public T times(T that) {
        return new T(this.p.multiply(that.p), this.q.multiply(that.q));
    }

    /**
     * {@code T(a,b) ⊕ T(c,d) = T(a+c, b+d)}: the mediant, which is the exponent-position sum.
     * <p>
     * This is the addition of {@code 0^a · 0^b = 0^(a⊕b)}. Its unit is {@link #ZERO_OMEGA} and its inverse
     * is {@link #oplusInverse()}.
     */
    public T oplus(T that) {
        return new T(this.p.add(that.p), this.q.add(that.q));
    }

    /**
     * {@code T(a,b) ⊗ T(c,d) = T(ad+bc, bd−ac)}: the exponent-position product, which is the tangent
     * addition formula.
     * <p>
     * On the points it is ordinary multiplication -- {@code (b+ai)(d+ci) = (bd−ac) + (ad+bc)i} -- so it adds
     * the two angles, and a denominator arriving at zero is a sum that reached the quarter turn rather than
     * anything failing. This is the product of {@code (0^z1)^z2}. Its unit is {@link #OTIMES_UNIT} and its
     * inverse is {@link #otimesInverse()}.
     */
    public T otimes(T that) {
        return new T(
                this.p.multiply(that.q).add(that.p.multiply(this.q)),
                this.q.multiply(that.q).subtract(this.p.multiply(that.p)));
    }

    /**
     * {@code T(a,b)^n = T(a^n, b^n)}, which is {@link #times} iterated -- the value-position power, and not
     * the n-fold angle.
     * <p>
     * A negative n is refused rather than answered by swapping the coordinates. {@code T(a,b)^-1} is
     * {@code T(1÷a, 1÷b)}, and reaching {@code T(b,a)} from there means multiplying both coordinates by
     * {@code ab} -- a reduction, and this type does not reduce. Where the value-position inverse is what is
     * wanted, {@link #reciprocal()} is it and says so.
     *
     * @param n zero or more; {@code T^0} is {@code T(1,1)}, the value-position unit, at every pair including
     *          {@link #ZERO_OMEGA}
     */
    public T power(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(
                    "T(a,b)^" + n + " is T(a^" + n + ", b^" + n + "), which is not a pair of integers: "
                            + "for the value-position inverse T(b,a), use reciprocal()");
        }
        return new T(this.p.pow(n), this.q.pow(n));
    }

    /**
     * {@code T^n}: the angle scaled n times, which is {@link #otimes} iterated -- the model's
     * exponentiation, at the exponents where it lands on a pair of integers.
     * <p>
     * The model writes exponentiation as angle scaling, {@code T(a,b)^T(c,d) = tan((c÷d)·arctan(a÷b))}.
     * Where the exponent is {@code T(n,1)} that is this method, and the coordinates stay integers because
     * the scaling is then a repeated ⊗, which is the product of the two points. Where it is not -- a third
     * of an angle, say -- the tangent named is not the ratio of any two integers, so there is no pair to
     * return and none is invented here.
     *
     * <p>Unlike {@link #power}, this is total over the whole range of {@code n}: the ⊗ inverse is
     * conjugation and costs no reduction, so a negative exponent is the conjugate raised to the positive
     * one. It makes no difference which comes first, conjugating and then raising or raising and then
     * conjugating, because conjugation passes through the product.
     *
     * <h2>The model's two exponent laws, as this type answers them</h2>
     * {@code (T^m)^n = T^(m·n)} holds on the coordinates everywhere, both signs included.
     * {@code T^(m+n) = T^m ⊗ T^n} holds on the coordinates while {@code m} and {@code n} point the same
     * way. Where they cancel the two sides are one ratio at different coordinates, and the difference is
     * exactly a factor of {@code (a²+b²)^min(|m|,|n|)} -- the norm, the same one {@link #otimesInverse}
     * leaves behind when it lands on {@code T(0, a²+b²)} rather than on the unit. Nothing here removes it.
     * Whether a turn that went out and came back should leave that factor standing or be cancelled away is
     * the model's question and not this type's to settle.
     *
     * @param n any integer; {@code T^0} is {@link #OTIMES_UNIT} at every pair, including
     *          {@link #ZERO_OMEGA}, which ⊗ absorbs at every other exponent
     */
    public T otimesPower(int n) {
        T base = n < 0 ? otimesInverse() : this;
        long e = Math.abs((long) n);
        T result = OTIMES_UNIT;
        // Squared rather than iterated, which is not a choice about the answer: ⊗ associates exactly on
        // the coordinates, so the same n factors under any bracketing land on the same pair.
        while (e > 0) {
            if ((e & 1L) == 1L) {
                result = result.otimes(base);
            }
            e >>= 1;
            if (e > 0) {
                base = base.otimes(base);
            }
        }
        return result;
    }

    /**
     * The model's {@code z(T(a,b)) = T(2ab, b²−a²)}: the point squared, so the angle doubled.
     * <p>
     * It is {@code this ⊗ this} on the coordinates and not merely at that ratio, so it is
     * {@link #otimesPower}{@code (2)} under the name the model gives it.
     *
     * <p>The model files this as a tangent half-angle substitution returning a unit vector. The formula it
     * states is the double-angle tangent, and the pair it produces is not a unit vector: the point
     * {@code (b²−a²) + 2ab·i} has norm {@code a²+b²}, and dividing by that to reach the unit circle leaves
     * the integers, which this carrier does not do. The formula is what is implemented.
     */
    public T doubleAngle() {
        return otimes(this);
    }

    /**
     * {@code T(b,a)}: the coordinates trade places, sign and all.
     * <p>
     * The value-position inverse, and it is total -- {@code 0} and {@code ω} are each other's reciprocal,
     * which is what the zero denominator is for. As an angle it reflects about half a right angle, which is
     * where tan and cot trade.
     */
    @Override
    public T reciprocal() {
        return new T(this.q, this.p);
    }

    /** {@code T(-a,-b)}: the inverse under ⊕, whose unit is {@link #ZERO_OMEGA}. */
    public T oplusInverse() {
        return new T(this.p.negate(), this.q.negate());
    }

    /**
     * {@code T(-a,b)}: the inverse under ⊗, whose unit is {@link #OTIMES_UNIT}. The angle turns back, which
     * is the point conjugated.
     * <p>
     * This is not offered as {@code negated()}. Which turn negation is on this pair is not something the
     * model states -- its table puts {@code -1} at {@code T(-1,1)} and {@code -0} at {@code T(0,-1)}, and
     * those are two different motions. So a negated {@code T} stands as a term, and this method is asked for
     * by the name of the law it comes from.
     */
    public T otimesInverse() {
        return new T(this.p.negate(), this.q);
    }

    /**
     * Whether this is {@code T(0,0)}: the unit of ⊕, and the one pair with no reading of its own.
     * <p>
     * Nothing here turns it into anything else. {@code x÷x = 1} would take it to {@code T(1,1)} and that is
     * a reading a caller may want, but it is not one the type applies: the pair that arrives is the pair
     * that is kept, and a computation that wants the value reading asks for it in its own code, where the
     * choice is visible.
     */
    public boolean isZeroOmega() {
        return this.p.signum() == 0 && this.q.signum() == 0;
    }

    /** Whether this is at a quarter turn, where the ratio has no real value: {@code ω} and {@code -ω}. */
    public boolean isQuarterTurn() {
        return this.q.signum() == 0 && this.p.signum() != 0;
    }

    /**
     * {@code θ = arg(q + pi)}, in radians.
     * <p>
     * This is the reading that tells the four sign placements apart where the ratio cannot: {@code T(0,1)}
     * and {@code T(0,-1)} are both a zero tangent, and they are the points {@code 1} and {@code -1}, half a
     * turn apart. {@code T(0,0)} is the origin, which has no argument, and this answers {@code NaN} rather
     * than putting it somewhere -- see {@link #isZeroOmega()}.
     */
    public double theta() {
        T it = this;
        // Taken from the ratio rather than from the two coordinates as doubles, because a pair whose
        // coordinates have BOTH outgrown a double leaves atan2 with infinity over infinity, which it answers
        // at 45 degrees whatever the real angle was: T(10^400, 10^500) is a hair off zero and T(10^500,
        // 10^400) a hair off the quarter turn, and both used to report the same 45. The ratio is exact, and
        // an overflow in it is harmless -- an infinite ratio is a quarter turn and atan says so.
        double ratio = it.projection();
        if (it.q.signum() >= 0) {
            return Math.atan(ratio);
        }
        // The half of the circle atan cannot reach. A zero numerator turns with the positive half, so that
        // T(0,-1) comes back at half a turn rather than at minus one.
        return Math.atan(ratio) + (it.p.signum() < 0 ? -Math.PI : Math.PI);
    }

    /**
     * The model table's projection column: what this pair is as an ordinary signed number.
     * <p>
     * Total, where {@link #evaluate()} is partial, and that is the whole difference between them. A quarter
     * turn has no finite real and {@code evaluate} says so by answering nothing; the projection answers an
     * infinity, because the column's job is to say what a pair becomes when it is pushed onto the number
     * line, including where that loses something.
     *
     * <h2>The sign survives onto the two values that have no magnitude</h2>
     * <pre>
     *  T(0,1)  +0      T(1,0)  +inf
     *  T(0,-1) -0      T(-1,0) -inf
     * </pre>
     * Which is why this returns a double rather than a rational: IEEE has a signed zero and two infinities,
     * and those are exactly the four places the model needs kept apart here. It is still a projection and it
     * still loses things -- {@code T(1,-1)} and {@code T(-1,1)} both come back -1, and they are half a turn
     * apart. {@link #theta()} is the reading that keeps them; a chart wanting both reports this one beside
     * the point rather than instead of it.
     *
     * <h2>The division is exact wherever it can be</h2>
     * The four cases above are answered from the signs, and everything else divides the coordinates rather
     * than their doubles. Dividing the doubles would ask what infinity over infinity is as soon as a
     * coordinate outgrew a double -- {@code (10^400, 10^400)} is 1, and its two shadows are both infinite.
     *
     * <p>{@code T(0,0)} is {@code NaN}, which is IEEE's own word for a division that names no number. It is
     * not taken to 1 on the way past: {@code x÷x} would put it there and that reading is the caller's to
     * apply, not this column's to assume. See {@link #isZeroOmega()}.
     *
     * <p>{@link #evaluate()} agrees with this wherever it answers at all, but for the signed zero, which it
     * cannot carry -- it divides exactly, and an exact zero has no sign.
     */
    public double projection() {
        T it = this;
        if (it.q.signum() == 0) {
            if (it.p.signum() == 0) {
                return Double.NaN;
            }
            return it.p.signum() > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        if (it.p.signum() == 0) {
            return it.q.signum() < 0 ? -0.0 : 0.0;
        }
        return new BigDecimal(it.p)
                .divide(new BigDecimal(it.q), MathContext.DECIMAL64)
                .doubleValue();
    }

    /**
     * The ratio {@code p÷q}, or empty at a quarter turn, where the tangent is not a finite real.
     * <p>
     * The division is done on the coordinates rather than on their doubles, because coordinates outgrow a
     * double long before the ratio they name does.
     * <p>
     * This is the value reading and {@link #projection()} is the table's column. They differ in two places
     * on purpose: at a quarter turn this answers nothing where the projection answers an infinity, and at a
     * negative zero this answers zero where the projection answers {@code -0}. At {@code T(0,0)} both
     * decline, one with nothing and one with {@code NaN}.
     */
    @Override
    public Optional<Double> evaluate() {
        T it = this;
        if (it.q.signum() == 0) {
            return Optional.empty();
        }
        return Optional.of(new BigDecimal(it.p)
                .divide(new BigDecimal(it.q), MathContext.DECIMAL64)
                .doubleValue());
    }

    /** The value-position sum where the other side is one of these, and a standing term where it is not. */
    @Override
    public IExpr plus(IExpr expr) {
        return expr instanceof T that ? plus(that) : IExpr.super.plus(expr);
    }

    /** The value-position product where the other side is one of these, and a standing term where it is not. */
    @Override
    public IExpr times(IExpr expr) {
        return expr instanceof T that ? times(that) : IExpr.super.times(expr);
    }
}
