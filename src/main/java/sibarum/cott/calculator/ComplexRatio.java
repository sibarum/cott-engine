package sibarum.cott.calculator;

import sibarum.cott.notation.Expr;
import sibarum.cott.notation.Printer;
import sibarum.cott.traction.Lean;
import sibarum.cott.traction.T;
import sibarum.cott.traction.TC;

import java.math.BigInteger;

/**
 * {@link Arithmetic#COMPLEX_RATIO}: {@code T(C, C)}, a ratio {@code z / w} of two Gaussian integers, as it
 * stands, unreduced.
 *
 * <p>An integer {@code n} enters as {@code n / 1}, and {@code ω} as {@code 1 / 0}, the ratio reading of the
 * flat {@code ω}. {@code +} and {@code ·} are {@code T}'s formulas over the Gaussian integers, {@code -a} is
 * {@code T(-z, w)}, {@code a / b} is {@code a · reciprocal(b)} and {@code a - b} is {@code a + (-b)}.
 * {@code a^n} is {@code a} multiplied by itself, for {@code n ≥ 1}.
 */
final class ComplexRatio {

    private ComplexRatio() {}

    static Result.ComplexRatioValue evaluate(Expr closed) {
        return new Result.ComplexRatioValue(value(closed));
    }

    @Lean({"T.complexInt", "TOver.instAdd", "TOver.instMul", "TOver.instNeg", "TOver.reciprocal"})
    static TC value(Expr e) {
        return switch (e) {
            case Expr.Num n -> TC.of(n.value());
            case Expr.Named _, Expr.Construct _ -> throw Arithmetic.notYet(e);
            case Expr.Omega o -> TC.of(BigInteger.ONE).times(TC.of(BigInteger.ZERO).reciprocal());
            case Expr.Neg n -> value(n.operand()).neg();
            case Expr.Add a -> value(a.left()).plus(value(a.right()));
            case Expr.Sub s -> value(s.left()).plus(value(s.right()).neg());
            case Expr.Mul m -> value(m.left()).times(value(m.right()));
            case Expr.Div d -> value(d.left()).times(value(d.right()).reciprocal());
            case Expr.Pow p -> power(value(p.base()), TractionRatio.naturalExponent(p.exponent()));
            case Expr.Decimal d -> throw new CalculatorException(
                    "a decimal is not a T(C,C) Complex Ratio value yet: whether " + Printer.print(d)
                            + " is a pair over a power of ten or its lowest terms is not chosen");
            case Expr.Var v -> throw new IllegalStateException("free variable " + v.name());
            case Expr.Call c -> throw Trig.notHere(c);
        };
    }

    /** Repeated {@code ·}. cott-lean defines no power on {@code TOver}, so {@code x^0} is not given one. */
    private static TC power(TC x, int n) {
        if (n == 0) throw new CalculatorException("x^0 is not defined in T(C,C) Complex Ratio: cott-lean has no power there yet");
        TC out = x;
        for (int i = 1; i < n; i++) out = out.times(x);
        return out;
    }

    /**
     * The ground truth, {@code z/w}, each Gaussian integer written out, unreduced. Over {@code 0} it is
     * {@code zω}, since {@code z·ω = T(z·1, 1·0) = T(z, 0)} exactly.
     */
    static String write(TC x) {
        if (x.q().p().signum() == 0 && x.q().q().signum() == 0) {
            String z = grouped(x.p());
            return switch (z) {
                case "1" -> "ω";
                case "-1" -> "-ω";
                default -> z + "ω";
            };
        }
        return grouped(x.p()) + "/" + grouped(x.q());
    }

    /**
     * The complex number {@code z / w} in lowest terms ({@code TC.val}), by way of the point it rationalizes
     * to. {@code undefined} where {@code w = 0}, outside the theorems that make it the complex value.
     */
    @Lean({"TC.val", "TC.val_rationalize"})
    static String complexValue(TC x) {
        if (x.q().p().signum() == 0 && x.q().q().signum() == 0) return "undefined";
        return TractionPoint.value(x.rationalize());
    }

    /** The point {@code C(T, T)} it rationalizes to, written as Point writes it. */
    @Lean("TC.rationalize")
    static String point(TC x) {
        return TractionPoint.write(x.rationalize());
    }

    /** The flat point {@code T(p, q)} as the Gaussian integer {@code q + p·i}: {@code 3}, {@code -2i}, {@code 3 - 2i}. */
    @Lean({"T.toGaussian", "T.toGaussian_re", "T.toGaussian_im"})
    static String gaussian(T z) {
        BigInteger re = z.q(), im = z.p();
        if (im.signum() == 0) return re.toString();
        String i = im.abs().equals(BigInteger.ONE) ? "i" : im.abs() + "i";
        if (re.signum() == 0) return (im.signum() < 0 ? "-" : "") + i;
        return re + (im.signum() < 0 ? " - " : " + ") + i;
    }

    /** {@link #gaussian}, in parentheses when it has both a real and an imaginary part. */
    static String grouped(T z) {
        String s = gaussian(z);
        return z.p().signum() != 0 && z.q().signum() != 0 ? "(" + s + ")" : s;
    }
}
