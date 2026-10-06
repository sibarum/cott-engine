package sibarum.cott.calculator;

import sibarum.cott.notation.Expr;
import sibarum.cott.notation.Printer;
import sibarum.cott.projection.Display;
import sibarum.cott.traction.Lean;
import sibarum.cott.traction.T;
import sibarum.cott.traction.T2;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link Arithmetic#TRACTION_RATIO}: evaluation at Level 2, in {@link T2}, and opaque. A literal enters by
 * {@link T2#of}, and the result is {@link T2#flatten flattened}. That flat pair, with no quotient, is the
 * answer; every other reading of it is a projection the {@link Result.RatioValue} takes on demand.
 */
final class TractionRatio {

    private TractionRatio() {}

    static Result.RatioValue evaluate(Expr closed, Limits limits) {
        Map<String, String> certificates = new LinkedHashMap<>();
        T2 level2 = level2(closed, limits, certificates);
        T flat = level2.flatten();
        return new Result.RatioValue(level2, flat, Display.of(flat), certificates);
    }

    /**
     * The notation's operations at Level 2. {@code a - b} is {@code a + (-b)} and {@code a / b} is
     * {@code a · reciprocal(b)}, the reading {@code flatten} itself gives division.
     */
    @Lean({"T2.of", "T2.ω", "T2.plus", "T2.times", "T2.neg_def", "T2.reciprocal", "T2.power", "T2.flatten"})
    static T2 level2(Expr e, Limits limits, Map<String, String> certificates) {
        return switch (e) {
            case Expr.Num n -> T2.of(new T(n.value(), BigInteger.ONE));
            case Expr.Omega o -> T2.OMEGA;
            case Expr.Neg n -> level2(n.operand(), limits, certificates).neg();
            case Expr.Add a -> level2(a.left(), limits, certificates).plus(level2(a.right(), limits, certificates));
            case Expr.Sub s -> level2(s.left(), limits, certificates).plus(level2(s.right(), limits, certificates).neg());
            case Expr.Mul m -> level2(m.left(), limits, certificates).times(level2(m.right(), limits, certificates));
            case Expr.Div d -> level2(d.left(), limits, certificates).times(level2(d.right(), limits, certificates).reciprocal());
            case Expr.Pow p -> level2(p.base(), limits, certificates).power(naturalExponent(p.exponent()));
            case Expr.Decimal d -> throw new CalculatorException(
                    "a decimal is not a T(T,T) Compound Ratio value yet: whether " + Printer.print(d)
                            + " is a pair over a power of ten or its lowest terms is not chosen");
            case Expr.Var v -> throw new IllegalStateException("free variable " + v.name());
            case Expr.Call c when Trig.isBuiltIn(c.name()) ->
                    T2.of(Trig.evaluate(c, level2(c.args().getFirst(), limits, certificates).flatten(), limits, certificates));
            case Expr.Call c -> throw new IllegalStateException("unexpanded call " + c.name());
        };
    }

    /** The proven power takes a natural number, so for now an exponent is written as one. */
    static int naturalExponent(Expr e) {
        if (!(e instanceof Expr.Num n))
            throw new CalculatorException("an exponent must be a whole number for now, not " + Printer.print(e));
        try {
            return n.value().intValueExact();
        } catch (ArithmeticException tooBig) {
            throw new CalculatorException("exponent " + n.value() + " is too large");
        }
    }
}
