package sibarum.cott.calculator;

import sibarum.cott.notation.Expr;
import sibarum.cott.notation.Printer;
import sibarum.cott.traction.CC;
import sibarum.cott.traction.Lean;
import sibarum.cott.traction.T;

import java.math.BigInteger;

/**
 * {@link Arithmetic#BICOMPLEX}: {@code C(C, C)}, the point {@code B + A·j} with Gaussian-integer
 * coordinates, as it stands.
 *
 * <p>An integer {@code n} enters on the outer unit as {@code n + 0·j}. {@code +} is the sum of the points
 * and {@code ·} their product, {@code -a} is {@code (-1)·a} and {@code a - b} is {@code a + (-1)·b}.
 * {@code a^n} is {@code a} multiplied by itself, for {@code n ≥ 1}. cott-lean gives {@code C(C, C)} no
 * inverse, so there is no {@code /}, and no {@code ω}, which enters elsewhere as {@code 1/0}.
 */
final class Bicomplex {

    private Bicomplex() {}

    private static final CC MINUS_ONE = CC.ofOuter(T.of(0, -1));

    static Result.BicomplexValue evaluate(Expr closed) {
        return new Result.BicomplexValue(value(closed));
    }

    @Lean({"CC.ofOuter", "CC.oplus", "CC.otimes"})
    static CC value(Expr e) {
        return switch (e) {
            case Expr.Num n -> CC.ofOuter(new T(BigInteger.ZERO, n.value()));
            case Expr.Omega o -> throw new CalculatorException(
                    "ω is not a C(C,C) Bicomplex value: it is 1/0, and cott-lean gives C(C,C) no inverse yet");
            case Expr.Neg n -> MINUS_ONE.otimes(value(n.operand()));
            case Expr.Add a -> value(a.left()).oplus(value(a.right()));
            case Expr.Sub s -> value(s.left()).oplus(MINUS_ONE.otimes(value(s.right())));
            case Expr.Mul m -> value(m.left()).otimes(value(m.right()));
            case Expr.Div d -> throw new CalculatorException(
                    "there is no / in C(C,C) Bicomplex: cott-lean gives it no inverse yet");
            case Expr.Pow p -> power(value(p.base()), TractionRatio.naturalExponent(p.exponent()));
            case Expr.Decimal d -> throw new CalculatorException(
                    "a decimal is not a C(C,C) Bicomplex value: " + Printer.print(d));
            case Expr.Var v -> throw new IllegalStateException("free variable " + v.name());
            case Expr.Call c -> throw new IllegalStateException("unexpanded call " + c.name());
        };
    }

    /** Repeated {@code ·}. cott-lean defines no power on {@code CC}, so {@code x^0} is not given one. */
    private static CC power(CC x, int n) {
        if (n == 0) throw new CalculatorException("x^0 is not defined in C(C,C) Bicomplex: cott-lean has no power there yet");
        CC out = x;
        for (int i = 1; i < n; i++) out = out.otimes(x);
        return out;
    }

    /** The ground truth, {@code B + A·j}, each coordinate a Gaussian integer. */
    static String write(CC x) {
        String a = ComplexRatio.grouped(x.p());
        if (a.startsWith("-")) a = "(" + a + ")";
        return ComplexRatio.grouped(x.q()) + " + " + a + "·j";
    }
}
