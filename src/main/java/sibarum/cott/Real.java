package sibarum.cott;

import sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

/**
 * The real-valued functions, and the one place in this engine that approximates.
 *
 * <h2>Why they are a catalogue and not terms</h2>
 * Everything else here is exact: a value is a pair of projective coordinates, an exponent is an expression, and a term
 * with no definite answer stands rather than being rounded into one. Trigonometry has no such reading — there is
 * no base-0 exponential form for {@code sin}, the same reason π and e are atoms — so these are not
 * theory, they are a table of functions the keypad can reach. Keeping them in one enum rather than scattering
 * them through the engine is the same discipline {@link Notation} follows: the parser, the printer, the
 * adjacency pass and the keypad all read the names from here, so a key that types {@code sin(} and a printer
 * that writes {@code sin(} cannot drift apart.
 *
 * <h2>What "numeric when it can" means</h2>
 * A call reduces only when every argument has a real reading — an ordinary numeral, π, e, or anything built out
 * of those. {@code sin(x)} stands, and stands as a term the plotter can still draw. {@code sin(2)} answers.
 * Where the answer is not a finite real — {@code asin(2)}, {@code acosh(0)} — the term <em>stands</em>, which is
 * this engine's standing habit for a question it cannot answer, rather than a NaN leaking into the display.
 *
 * <h2>Angles</h2>
 * The native measure is the radian, and {@link #DEG}/{@link #RAD} are angle <em>constructors</em> rather than a
 * mode: {@code deg(90)} is the angle of ninety degrees and {@code rad(n)} is n radians, so {@code sin(deg(90))}
 * is 1 and nothing in an expression depends on a switch set somewhere else. A calculator with a DEG/RAD mode
 * makes every stored expression ambiguous about which one it was written in; this cannot.
 */
public enum Real {

    SIN("sin", Math::sin),
    COS("cos", Math::cos),
    TAN("tan", Math::tan),

    ASIN("asin", Math::asin),
    ACOS("acos", Math::acos),
    ATAN("atan", Math::atan),

    // The reciprocals, taken from the ratio rather than from 1/tan and friends where that is better behaved:
    // cot as cos/sin is exact at the odd multiples of π/2, where 1/tan divides by a number that is merely large.
    SEC("sec", x -> 1.0 / Math.cos(x)),
    CSC("csc", x -> 1.0 / Math.sin(x)),
    COT("cot", x -> Math.cos(x) / Math.sin(x)),

    ASEC("asec", x -> Math.acos(1.0 / x)),
    ACSC("acsc", x -> Math.asin(1.0 / x)),
    // The CONTINUOUS branch, (0, π), rather than atan(1/x): the other convention tears the function in half at
    // zero, which is exactly where a plot of it would be asked about.
    ACOT("acot", x -> Math.PI / 2 - Math.atan(x)),

    SINH("sinh", Math::sinh),
    COSH("cosh", Math::cosh),
    TANH("tanh", Math::tanh),

    ASINH("asinh", x -> Math.log(x + Math.sqrt(x * x + 1))),
    ACOSH("acosh", x -> Math.log(x + Math.sqrt(x * x - 1))),
    ATANH("atanh", x -> 0.5 * Math.log((1 + x) / (1 - x))),

    /**
     * The angle of the point {@code (x, y)}, argument order as written — which is the reverse of
     * {@code Math.atan2}, whose y comes first. The keypad's label says {@code atan2(x, y)} and a key that lies
     * about its own argument order is worse than no key.
     */
    ATAN2("atan2", (x, y) -> Math.atan2(y, x)),

    /** {@code d} degrees, as an angle in the native measure. */
    DEG("deg", d -> d * Math.PI / 180),
    /** {@code n} radians — the identity, and worth a key so that an expression can say which measure it meant. */
    RAD("rad", n -> n);

    /**
     * Decimal places an answer is rounded to.
     *
     * <p>Rounded at all because of {@code sin(π)}. In binary floating point that is 1.22e-16 rather than zero,
     * and a calculator that says so is reporting the arithmetic's error as if it were the answer. Rounding puts
     * every such crumb at zero — and puts {@code tan(π÷4)} at 1 and {@code cos(π÷3)} at {@code 1÷2}. To a number
     * of decimal <em>places</em> and not of significant figures, deliberately: significant figures would keep
     * {@code 1.22e-16} to twelve of them, which is the case this exists to kill.
     *
     * <p><b>Fifteen, and the display shows twelve.</b> Everything downstream of a call is exact arithmetic on an
     * approximation, so the error grows with the expression: square a value good to n places and the answer is
     * good to rather fewer. Rounding and showing at the same width puts that growth on screen —
     * {@code sin(θ)²+cos(θ)²} came to 1.000000000001, every digit of it honest and the answer plainly 1. The
     * three places between {@link Render}'s window and this are guard digits, and they are the whole of the
     * arrangement: compute wider than you show, and what you show is right for as long as it is worth showing.
     */
    static final int PLACES = 15;

    private final String label;
    private final int arity;
    private final DoubleUnaryOperator unary;
    private final DoubleBinaryOperator binary;

    Real(String label, DoubleUnaryOperator unary) {
        this.label = label;
        this.arity = 1;
        this.unary = unary;
        this.binary = null;
    }

    Real(String label, DoubleBinaryOperator binary) {
        this.label = label;
        this.arity = 2;
        this.unary = null;
        this.binary = binary;
    }

    private static final Map<String, Real> BY_LABEL =
            java.util.Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(Real::label, f -> f));

    /** Every function name, longest first — the order a scanner has to try them in so {@code asin} beats {@code a}. */
    public static final List<String> NAMES = BY_LABEL.keySet().stream()
            .sorted(java.util.Comparator.comparingInt(String::length).reversed().thenComparing(s -> s))
            .toList();

    /** The function of that name, or null where there is none. */
    public static Real of(String name) {
        return BY_LABEL.get(name);
    }

    /** How it is written, typed and printed. */
    public String label() {
        return label;
    }

    /** How many arguments it takes. */
    public int arity() {
        return arity;
    }

    /**
     * Apply it, rounded to {@link #PLACES}, or null where the answer is not a finite real — which is how a
     * function declines to reduce, the same way every other unanswerable question here leaves its term
     * standing.
     */
    ProjectiveRationalLiteral apply(List<Double> args) {
        if (args.size() != arity) {
            return null;
        }
        double answer = arity == 1
                ? unary.applyAsDouble(args.get(0))
                : binary.applyAsDouble(args.get(0), args.get(1));
        if (!Double.isFinite(answer)) {
            return null;
        }
        BigDecimal rounded = BigDecimal.valueOf(answer).setScale(PLACES, RoundingMode.HALF_EVEN)
                .stripTrailingZeros();
        // A BigDecimal is a scaled integer, so unscaling it is exactly the fraction it stands for -- no
        // second rounding, and nothing here has to know how a decimal string is spelled.
        return rounded.scale() <= 0
                ? new ProjectiveRationalLiteral(rounded.toBigIntegerExact(), BigInteger.ONE)
                : new ProjectiveRationalLiteral(rounded.unscaledValue(), BigInteger.TEN.pow(rounded.scale()));
    }
}
