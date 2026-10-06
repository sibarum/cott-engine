package sibarum.cott.calculator;

import sibarum.cott.notation.Expr;

import java.util.function.BiFunction;

/**
 * The {@link Modeset#ARITHMETIC} modes: what a value is, and how {@code +}, {@code -}, {@code ·},
 * {@code /} and {@code ^} act on it. The notation, the definitions and the substitution are the same in
 * every arithmetic; only the evaluation of a closed expression differs. So a definition made in one
 * arithmetic is evaluated afresh in another.
 */
public enum Arithmetic implements Mode {

    IEEE_FLOATING_POINT("ieee", "IEEE Floating Point", false, true, (e, l) -> IeeeFloatingPoint.evaluate(e)),
    /** {@code T(T, T)}: a ratio of two ratios, flattened. */
    TRACTION_RATIO("compound", "T(T,T) Compound Ratio", true, false, TractionRatio::evaluate),
    /** {@code C(T, T)}: a point with ratio coordinates. */
    TRACTION_POINT("point", "C(T,T) Point", true, false, (e, l) -> TractionPoint.evaluate(e)),
    /** {@code T(C, C)}: a ratio of two Gaussian integers. */
    COMPLEX_RATIO("complex", "T(C,C) Complex Ratio", true, false, (e, l) -> ComplexRatio.evaluate(e)),
    /** {@code C(C, C)}: a point with Gaussian-integer coordinates, the bicomplex integers. */
    BICOMPLEX("bicomplex", "C(C,C) Bicomplex", false, false, (e, l) -> Bicomplex.evaluate(e)),
    /** {@code C(T(C, C), T(C, C))}: a point whose coordinates are ratios of Gaussian integers. */
    BICOMPLEX_RATIO("bicomplex-ratio", "C(T(C,C),T(C,C)) Bicomplex Ratio", true, false, (e, l) -> BicomplexRatio.evaluate(e));

    private final String key;
    private final String label;
    private final boolean omega;
    private final boolean decimals;
    private final BiFunction<Expr, Limits, Result.Value> evaluate;

    Arithmetic(String key, String label, boolean omega, boolean decimals, BiFunction<Expr, Limits, Result.Value> evaluate) {
        this.key = key;
        this.label = label;
        this.omega = omega;
        this.decimals = decimals;
        this.evaluate = evaluate;
    }

    @Override
    public Modeset modeset() {
        return Modeset.ARITHMETIC;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String key() {
        return key;
    }

    /** Whether {@code ω} is a value here. Where it is not, entering it is a {@link CalculatorException}. */
    public boolean hasOmega() {
        return omega;
    }

    /** Whether a decimal literal is a value here. Where it is not, entering one is a {@link CalculatorException}. */
    public boolean hasDecimals() {
        return decimals;
    }

    /** The value of an expression with no variables or calls left in it. */
    public Result.Value evaluate(Expr closed) {
        return evaluate(closed, (Limits) Modeset.LIMITS.initial());
    }

    /** The value of an expression with no variables or calls left in it, within these recursion limits. */
    public Result.Value evaluate(Expr closed, Limits limits) {
        return evaluate.apply(closed, limits);
    }
}
