package sibarum.cott.calculator;

import sibarum.cott.notation.Expr;
import sibarum.cott.notation.Printer;
import sibarum.cott.projection.Display;
import sibarum.cott.projection.Rational;
import sibarum.cott.traction.Lean;
import sibarum.cott.traction.T;
import sibarum.cott.traction.T2;

import java.math.BigInteger;

/**
 * {@link Arithmetic#TRACTION_POINT}: the point reading at Level 2. A value is {@code T2(A, B)}, the complex
 * number {@code B + A·i} whose coordinates are ratios, and it is the answer as it stands, with no quotient
 * and nothing flattened.
 *
 * <p>An integer {@code n} enters as the point {@code n + 0·i}, and {@code ω}, the flat {@code T(1,0)}, as
 * the point it already is, {@code 0 + 1·i}. {@code +} is the sum of the points and {@code ·} their product.
 * {@code a / b} is {@code a · (1/b)} with the point's own inverse, {@code (B − A·i)/(A² + B²)}, which is exact
 * because each coordinate keeps its own denominator. {@code -a} is {@code (-1) · a}, and {@code a - b} is
 * {@code a + (-1)·b}. {@code a^n} is {@code a} multiplied by itself, for {@code n ≥ 1}.
 */
final class TractionPoint {

    private TractionPoint() {}

    private static final T2 MINUS_ONE = T2.ofPoint(T.of(0, -1));

    static Result.PointValue evaluate(Expr closed) {
        return new Result.PointValue(point(closed));
    }

    @Lean({"T2.ofPoint", "T2.oplus", "T2.otimes", "T2.pointInv"})
    static T2 point(Expr e) {
        return switch (e) {
            case Expr.Num n -> T2.ofPoint(new T(BigInteger.ZERO, n.value()));
            case Expr.Omega o -> T2.ofPoint(T.OMEGA);
            case Expr.Neg n -> MINUS_ONE.otimes(point(n.operand()));
            case Expr.Add a -> point(a.left()).oplus(point(a.right()));
            case Expr.Sub s -> point(s.left()).oplus(MINUS_ONE.otimes(point(s.right())));
            case Expr.Mul m -> point(m.left()).otimes(point(m.right()));
            case Expr.Div d -> point(d.left()).otimes(point(d.right()).pointInv());
            case Expr.Pow p -> power(point(p.base()), TractionRatio.naturalExponent(p.exponent()));
            case Expr.Decimal d -> throw new CalculatorException(
                    "a decimal is not a C(T,T) Point value yet: whether " + Printer.print(d)
                            + " is a pair over a power of ten or its lowest terms is not chosen");
            case Expr.Var v -> throw new IllegalStateException("free variable " + v.name());
            case Expr.Call c -> throw new IllegalStateException("unexpanded call " + c.name());
        };
    }

    /** Repeated {@code ·}. cott-lean defines no power on the point reading, so {@code x^0} is not given one. */
    private static T2 power(T2 x, int n) {
        if (n == 0) throw new CalculatorException("x^0 is not defined in C(T,T) Point: cott-lean has no point power yet");
        T2 out = x;
        for (int i = 1; i < n; i++) out = out.otimes(x);
        return out;
    }

    /** The ground truth, {@code B + A·i}, each coordinate written as a pair, unreduced. */
    static String write(T2 x) {
        String a = Display.of(x.p());
        if (a.contains("/") || a.startsWith("-")) a = "(" + a + ")";
        return Display.of(x.q()) + " + " + a + "·i";
    }

    /**
     * The complex number the point is, in lowest terms: {@code B + A·i} with each coordinate read as its
     * ratio. {@code undefined} where a coordinate has a zero denominator, outside {@code Finite}, where the
     * theorems that make it the complex value do not apply.
     */
    @Lean({"T2.val", "T2.rv", "T2.Finite", "T2.val_oplus", "T2.val_otimes", "T2.val_pointInv"})
    static String value(T2 x) {
        if (x.p().q().signum() == 0 || x.q().q().signum() == 0) return "undefined";
        Rational re = new Rational(x.q().p(), x.q().q());
        Rational im = new Rational(x.p().p(), x.p().q());
        if (im.num().signum() == 0) return re.toString();
        String imaginary = imaginary(im.num().signum() < 0 ? im.neg() : im);
        if (re.num().signum() == 0) return (im.num().signum() < 0 ? "-" : "") + imaginary;
        return re + (im.num().signum() < 0 ? " - " : " + ") + imaginary;
    }

    private static String imaginary(Rational positive) {
        if (positive.equals(Rational.of(1, 1))) return "i";
        if (positive.den().equals(BigInteger.ONE)) return positive + "i";
        return "(" + positive + ")i";
    }

    /** {@code A·A + B·B}, unreduced: what {@code 1/x} divides by. */
    @Lean("T2.normSq")
    static String lengthSquared(T2 x) {
        return Display.of(x.normSq());
    }
}
