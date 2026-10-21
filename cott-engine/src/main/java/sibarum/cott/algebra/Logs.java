package sibarum.cott.algebra;

import sibarum.cott.projection.Rational;
import sibarum.cott.traction.Lean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * The logarithm pair {@link TractionAlgebra#L L}'s exact parts, as cott-lean states them ({@code T/LogPair},
 * {@code T/Bases}): a ratio kept unreduced, traction's division of ratios, the units a base is held by, and
 * {@code log_b} by those units.
 *
 * <pre>
 * log_b z = ( log N_z ÷ scale unit ,  turn z ÷ turn unit )        (a : b) ÷ (c : d) = (a·d : b·c)
 * </pre>
 *
 * <p>{@code ÷} is defined for every divisor, so {@code log_b} is defined for every base, {@code 0} and {@code ω}
 * among them, whose turn unit is {@code 0 : 0}.
 */
public final class Logs {

    private Logs() {
    }

    /** A ratio {@code a : b}, read {@code a/b}, unreduced; {@code b} may be {@code 0}. */
    public record Ratio(Rational a, Rational b) {

        static Ratio of(long a, long b) {
            return new Ratio(Rational.of(a, 1), Rational.of(b, 1));
        }

        /** {@code a/b}, where {@code b} is not {@code 0}. */
        public Optional<Rational> value() {
            return b.num().signum() == 0 ? Optional.empty() : Optional.of(a.times(b.inverse()));
        }

        /** {@code 1 : 2}, {@code -1 : 0}, {@code 1/2 : 1}. */
        @Override
        public String toString() {
            return a + " : " + b;
        }
    }

    /** {@code (a : b) ÷ (c : d) = (a·d : b·c)}: traction's division of ratios, defined for every divisor. */
    @Lean("T.Unquotiented.rdiv")
    public static Ratio rdiv(Ratio x, Ratio y) {
        return new Ratio(x.a().times(y.b()), x.b().times(y.a()));
    }

    /** A base, held by its units: the scale unit, {@code log N} of one unit, and the turn unit. */
    public record Units(Ratio scale, Ratio turn) {}

    /**
     * The units of each base that has them: the full-turn {@code 1}, {@code −1}, {@code i}, {@code 0} and {@code ω}
     * as a ratio. The classical {@code e} counts in radians and has none here.
     */
    @Lean({"T.Unquotiented.Base", "T.Unquotiented.Base.one", "T.Unquotiented.Base.negOne", "T.Unquotiented.Base.i",
            "T.Unquotiented.Base.zero", "T.Unquotiented.Base.omega"})
    public static Optional<Units> units(Base base) {
        return Optional.ofNullable(switch (base) {
            case ONE -> new Units(Ratio.of(0, 1), Ratio.of(1, 1));
            case NEG_ONE -> new Units(Ratio.of(0, 1), new Ratio(Rational.of(1, 2), Rational.of(1, 1)));
            case I -> new Units(Ratio.of(0, 1), new Ratio(Rational.of(1, 4), Rational.of(1, 1)));
            case ZERO -> new Units(Ratio.of(-1, 0), Ratio.of(0, 0));
            case OMEGA -> new Units(Ratio.of(1, 0), Ratio.of(0, 0));
            case E -> null;
        });
    }

    /** {@code log_b} of the value whose {@code L} is {@code (scale, turn)}: each over its unit. */
    @Lean({"T.Unquotiented.logBase", "T.Unquotiented.logBase_one", "T.Unquotiented.logBase_negOne",
            "T.Unquotiented.logBase_i", "T.Unquotiented.logBase_zero", "T.Unquotiented.logBase_omega"})
    public static Units logBase(Units base, Ratio scale, Ratio turn) {
        return new Units(rdiv(scale, base.scale()), rdiv(turn, base.turn()));
    }

    /** What {@code 0^(p/q)} is, by James's rules ({@code zeroPow}): one of the four, or none for {@code 0/0}. */
    public sealed interface ZeroPower {
        /** {@code 1}: {@code L} of the point {@code 1}, {@code (0 : 1, 0 : 1)}. */
        record One() implements ZeroPower {}

        /** {@code −1}: {@code L} of the point {@code −1}, {@code (0 : 1, 1/2 : 1)}. */
        record NegOne() implements ZeroPower {}

        /** A rotation of zero magnitude: scale {@code −1 : 0}, turned {@code p : q}. */
        record Rotation(Rational p, Rational q) implements ZeroPower {}
    }

    /**
     * {@code 0^(p/q)} by the rules cott-lean states: {@code p} and {@code q} both non-zero is a rotation of zero
     * magnitude by {@code p : q} turns; {@code p = 0} is {@code 1} for {@code q > 0} and {@code −1} for {@code q < 0};
     * {@code q = 0} the opposite, {@code −1} for {@code p > 0} and {@code 1} for {@code p < 0}; {@code 0/0} has no
     * rule. So {@code 0^(1/0) = −1} and {@code 0^(0/1) = 1}.
     */
    @Lean({"T.Unquotiented.zeroPow", "T.Unquotiented.zeroPow_one_zero", "T.Unquotiented.zeroPow_zero_one",
            "T.Unquotiented.zeroPow_one_one", "T.Unquotiented.zeroPow_zero_zero", "T.Unquotiented.zeroPow_axis_iff"})
    public static Optional<ZeroPower> zeroPow(Rational p, Rational q) {
        int ps = p.num().signum(), qs = q.num().signum();
        if (ps == 0 && qs == 0) return Optional.empty();
        if (ps == 0) return Optional.of(qs > 0 ? new ZeroPower.One() : new ZeroPower.NegOne());
        if (qs == 0) return Optional.of(ps > 0 ? new ZeroPower.NegOne() : new ZeroPower.One());
        return Optional.of(new ZeroPower.Rotation(p, q));
    }

    /**
     * A coordinate of an {@code L} as a ratio: a number {@code v} is {@code v : 1}, and a {@code Q} whose coordinates
     * are numbers, or {@code Q}s of them, is its two coordinates as they stand. Empty for anything else.
     */
    public static Optional<Ratio> ratio(Object v) {
        if (v instanceof Pair<?>(TractionAlgebra a, Object p, Object q) && a == TractionAlgebra.Q)
            return rational(p).flatMap(x -> rational(q).map(y -> new Ratio(x, y)));
        return rational(v).map(x -> new Ratio(x, Rational.of(1, 1)));
    }

    /** A number, or a {@code Q} of two numbers with a denominator that is not {@code 0}, as a rational. */
    static Optional<Rational> rational(Object v) {
        return switch (v) {
            case BigInteger n -> Optional.of(new Rational(n, BigInteger.ONE));
            case BigDecimal d -> Optional.of(d.scale() <= 0
                    ? new Rational(d.toBigIntegerExact(), BigInteger.ONE)
                    : new Rational(d.unscaledValue(), BigInteger.TEN.pow(d.scale())));
            case Pair<?>(TractionAlgebra a, Object p, Object q) when a == TractionAlgebra.Q ->
                    rational(p).flatMap(x -> rational(q).filter(y -> y.num().signum() != 0).map(y -> x.times(y.inverse())));
            default -> Optional.empty();
        };
    }
}
