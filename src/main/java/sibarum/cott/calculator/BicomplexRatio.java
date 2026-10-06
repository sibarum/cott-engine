package sibarum.cott.calculator;

import sibarum.cott.notation.Expr;
import sibarum.cott.notation.Printer;
import sibarum.cott.traction.CC;
import sibarum.cott.traction.CTC;
import sibarum.cott.traction.Lean;
import sibarum.cott.traction.T;

import java.math.BigInteger;

/**
 * {@link Arithmetic#BICOMPLEX_RATIO}: {@code C(T(C, C), T(C, C))}, the point {@code B + A·j} whose coordinates
 * are ratios of Gaussian integers, as it stands, unreduced.
 *
 * <p>An integer {@code n} enters as the bicomplex integer {@code n + 0·j}, each coordinate over {@code 1}, and
 * {@code ω} as {@code 1/0}, evaluated here. {@code +} is the sum of the points and {@code ·} their product,
 * {@code a / b} is {@code a · inv(b)}, {@code -a} is {@code (-1)·a} and {@code a - b} is {@code a + (-1)·b}.
 * {@code a^n} is {@code a} multiplied by itself, for {@code n ≥ 1}.
 */
final class BicomplexRatio {

    private BicomplexRatio() {}

    private static final CTC MINUS_ONE = integer(BigInteger.ONE.negate());

    static Result.BicomplexRatioValue evaluate(Expr closed) {
        return new Result.BicomplexRatioValue(value(closed));
    }

    @Lean({"CTC.ofCC", "CC.ofOuter", "CTC.oplus", "CTC.otimes", "CTC.inv"})
    static CTC value(Expr e) {
        return switch (e) {
            case Expr.Num n -> integer(n.value());
            case Expr.Omega o -> integer(BigInteger.ONE).otimes(integer(BigInteger.ZERO).inv());
            case Expr.Neg n -> MINUS_ONE.otimes(value(n.operand()));
            case Expr.Add a -> value(a.left()).oplus(value(a.right()));
            case Expr.Sub s -> value(s.left()).oplus(MINUS_ONE.otimes(value(s.right())));
            case Expr.Mul m -> value(m.left()).otimes(value(m.right()));
            case Expr.Div d -> value(d.left()).otimes(value(d.right()).inv());
            case Expr.Pow p -> power(value(p.base()), TractionRatio.naturalExponent(p.exponent()));
            case Expr.Decimal d -> throw new CalculatorException(
                    "a decimal is not a C(T(C,C),T(C,C)) Bicomplex Ratio value yet: whether " + Printer.print(d)
                            + " is a pair over a power of ten or its lowest terms is not chosen");
            case Expr.Var v -> throw new IllegalStateException("free variable " + v.name());
            case Expr.Call c -> throw Trig.notHere(c);
        };
    }

    /** The integer {@code n} as the bicomplex integer {@code n + 0·j}, over {@code 1}. */
    private static CTC integer(BigInteger n) {
        return CTC.ofCC(CC.ofOuter(new T(BigInteger.ZERO, n)));
    }

    /** Repeated {@code ·}. cott-lean defines no power on {@code CTC}, so {@code x^0} is not given one. */
    private static CTC power(CTC x, int n) {
        if (n == 0) throw new CalculatorException(
                "x^0 is not defined in C(T(C,C),T(C,C)) Bicomplex Ratio: cott-lean has no power there yet");
        CTC out = x;
        for (int i = 1; i < n; i++) out = out.otimes(x);
        return out;
    }

    /** The ground truth, {@code B + A·j}, each coordinate a ratio of Gaussian integers written as Complex Ratio writes it. */
    static String write(CTC x) {
        String a = ComplexRatio.write(x.p());
        if (a.contains("/") || a.contains(" ") || a.startsWith("-")) a = "(" + a + ")";
        return ComplexRatio.write(x.q()) + " + " + a + "·j";
    }

    /**
     * The bicomplex number {@code B + A·j}, each coordinate the complex number its ratio is, in lowest terms.
     * {@code undefined} where a coordinate has a zero denominator, outside {@code Finite}.
     */
    @Lean({"CTC.val", "CTC.Finite", "TC.val"})
    static String bicomplexValue(CTC x) {
        if (zero(x.p().q()) || zero(x.q().q())) return "undefined";
        String b = ComplexRatio.complexValue(x.q()), a = ComplexRatio.complexValue(x.p());
        if (a.equals("0")) return b;
        String aj = (a.contains(" ") || a.contains("/") ? "(" + a + ")" : a) + "j";
        if (b.equals("0")) return aj;
        return b + " + " + aj;
    }

    /**
     * Whether the point has an inverse: a finite point is a unit exactly when {@code D ≠ 0}, off the light
     * lines {@code B = ±i·A}.
     */
    @Lean({"CTC.isUnit_val_iff", "CTC.den_eq_zero_iff"})
    static String unit(CTC x) {
        if (zero(x.p().q()) || zero(x.q().q())) return "undefined";
        return zero(x.den()) ? "no: on a light line" : "yes";
    }

    /** The norm {@code A·A + B·B}, a ratio of Gaussian integers, unreduced. */
    @Lean("CTC.nrm")
    static String norm(CTC x) {
        return ComplexRatio.write(x.nrm());
    }

    /** The Gaussian integer {@code 0} is the flat {@code 0ω}. */
    private static boolean zero(T gaussian) {
        return gaussian.p().signum() == 0 && gaussian.q().signum() == 0;
    }
}
