package sibarum.cott.calculator;

import sibarum.cott.algebra.IeeeLevel;
import sibarum.cott.notation.Expr;

import java.math.BigDecimal;

/**
 * {@link Arithmetic#IEEE_FLOATING_POINT}: IEEE 754 binary64, the {@code double} of most languages and of
 * most calculators. Each operation is the IEEE operation itself, rounded to nearest, ties to even:
 * {@code a - b} is subtraction and {@code a / b} is division, not {@code a + (-b)} and
 * {@code a · (1/b)}, which round differently. So {@code 1/0} is {@code ∞}, {@code 0/0} is {@code NaN},
 * and {@code -0} is kept apart from {@code 0}.
 *
 * <p>A literal is rounded once, to the nearest double. {@code ^} is {@link StrictMath#pow}, which gives
 * the same answer on every machine. {@code ω} is not a value here.
 */
final class IeeeFloatingPoint {

    private IeeeFloatingPoint() {}

    static Result.IeeeValue evaluate(Expr closed) {
        double value = value(closed);
        return new Result.IeeeValue(value, display(value));
    }

    static double value(Expr e) {
        return switch (e) {
            case Expr.Num n -> n.value().doubleValue();
            case Expr.Decimal d -> d.value().doubleValue();
            case Expr.Neg n -> -value(n.operand());
            case Expr.Add a -> value(a.left()) + value(a.right());
            case Expr.Sub s -> value(s.left()) - value(s.right());
            case Expr.Mul m -> value(m.left()) * value(m.right());
            case Expr.Div d -> value(d.left()) / value(d.right());
            case Expr.Pow p -> StrictMath.pow(value(p.base()), value(p.exponent()));
            case Expr.Named _, Expr.Construct _ -> throw Arithmetic.notYet(e);
            case Expr.Omega o -> throw new CalculatorException("ω is not an IEEE Floating Point value");
            case Expr.Var v -> throw new IllegalStateException("free variable " + v.name());
            case Expr.Call c -> throw Trig.notHere(c);
        };
    }

    /** {@link IeeeLevel#display}. */
    static String display(double x) {
        return IeeeLevel.display(x);
    }

    /** The exact value the double holds, which a decimal literal is usually only near. */
    static String exact(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x) || x == 0) return display(x);
        BigDecimal exact = new BigDecimal(x);
        double magnitude = Math.abs(x);
        return magnitude >= 1e-6 && magnitude < 1e21 ? exact.toPlainString() : exact.toString().replace("E+", "e").replace("E", "e");
    }

    /**
     * The 64 bits: sign, 11 of exponent, 52 of fraction. A NaN is written as the one canonical NaN, since
     * which NaN an operation leaves differs from one processor to another.
     */
    static String bits(double x) {
        return String.format("0x%016X", Double.doubleToLongBits(x));
    }
}
