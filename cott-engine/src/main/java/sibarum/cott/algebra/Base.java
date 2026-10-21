package sibarum.cott.algebra;

import sibarum.cott.projection.Rational;
import sibarum.cott.traction.Lean;

import java.util.Optional;

/**
 * What {@code e} is: the base of {@code e^x}, and of the turn {@code cos} and {@code sin} count in. {@code e = b} makes
 * {@code e^x} be {@code b^x}, so {@code e = 1} counts in whole turns and {@code e = −1}, the spinor, in half turns.
 *
 * <p>A base is held by its units, as cott-lean holds it ({@code T/Bases.lean}), not by its value: the full-turn
 * {@code 1} and the identity {@code 1} are the same number with different units. {@code e = 1} is the full turn. The
 * classical {@code e} counts in radians, which take π, so it is a double's base only.
 */
public enum Base {

    @Lean("T.Unquotiented.Base.one")
    ONE("1", Rational.of(1, 1)),
    @Lean("T.Unquotiented.Base.negOne")
    NEG_ONE("-1", Rational.of(1, 2)),
    @Lean("T.Unquotiented.Base.i")
    I("i", Rational.of(1, 4)),
    @Lean("T.Unquotiented.Base.zero")
    ZERO("0", null),
    @Lean("T.Unquotiented.Base.omega")
    OMEGA("ω", null),
    /** The classical base, with a turn of {@code 2π} radians. */
    E("e", null);

    private final String name;
    private final Rational turnUnit;

    Base(String name, Rational turnUnit) {
        this.name = name;
        this.turnUnit = turnUnit;
    }

    /** How the base is written after {@code e =}. */
    public String written() {
        return name;
    }

    /**
     * The fraction of a whole turn that one unit of this base is: {@code 1} for the full-turn {@code 1}, {@code 1/2}
     * for {@code −1}, {@code 1/4} for {@code i}. Empty for {@code 0} and {@code ω}, whose turn unit is {@code 0 : 0},
     * and for the classical {@code e}, whose unit is a radian.
     */
    public Optional<Rational> turnUnit() {
        return Optional.ofNullable(turnUnit);
    }

    /** What one unit is, in words, for {@code e = b}'s answer. */
    public String unit() {
        return switch (this) {
            case ONE -> "turns";
            case NEG_ONE -> "half turns";
            case I -> "quarter turns";
            case ZERO, OMEGA -> "no turn: its turn unit is 0 : 0";
            case E -> "radians, in IEEE 64-bit";
        };
    }

    /** The base for {@code e}'s name, as written after {@code e =}. */
    public static Optional<Base> named(String written) {
        for (Base b : values()) if (b.name.equals(written)) return Optional.of(b);
        return Optional.empty();
    }
}
