package sibarum.cott.algebra;

import sibarum.cott.calculator.CalculatorException;
import sibarum.cott.notation.Expr;
import sibarum.cott.notation.Printer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * Evaluates a closed expression in the traction algebras, over one {@link NumberType}.
 *
 * <p>A value is a bare number of the number type, or a {@link Pair} whose coordinates are values, each of its own
 * kind: {@code C(0, Q(1, 2))} holds a number beside a {@code Q} pair. A bare number is typeless. It stays a number
 * until an operation meets it with a pair, and then it enters that pair's algebra on its slot, with a typeless
 * identity on the other coordinate. Nothing is embedded before it is needed, so nothing has to be undone after.
 *
 * <p>Every operation is the algebra's own formula, and the formula's operations on the coordinates are this
 * evaluator's again, so they meet in turn by the same rules, all the way down. Two pairs of different algebras meet
 * in one of them, the other entering it whole on its slot: two sums or two products in the left one, a sum and a
 * product as the {@link Form} says.
 *
 * <p>An operation is never refused. Where an algebra lacks it, the answer is the pair whose reading is that
 * operation: {@code a + b} is {@code S(a, b)}, {@code a − b} is {@code D(b, a)}, {@code a · b} is {@code P(a, b)},
 * and {@code a / b} is {@code Q(a, b)}.
 */
public final class Evaluator {

    /** The built-in functions, given a call and its arguments, evaluated. */
    @FunctionalInterface
    public interface Calls {
        Object call(Expr.Call call, List<Object> args);
    }

    /** What an evaluation answers: the value, how it is written, and whether a Decimal was rounded. */
    public record Result(Object value, String text, boolean rounded) {}

    /** The most factors a power of a pair is multiplied out of, one at a time. */
    static final int MAX_FACTORS = 1 << 16;

    private final Level<Object> numbers;
    private final Form form;
    private final Calls calls;

    @SuppressWarnings("unchecked")
    Evaluator(NumberType type, SizeLimit limit, Form form, Calls calls) {
        this.numbers = (Level<Object>) type.level(limit);
        this.form = form;
        this.calls = calls;
    }

    public static Result evaluate(Expr closed, NumberType type, SizeLimit limit, Form form) {
        return evaluate(closed, type, limit, form, (c, args) -> {
            throw new CalculatorException(c.name() + " is not in the traction algebras yet");
        });
    }

    public static Result evaluate(Expr closed, NumberType type, SizeLimit limit, Form form, Calls calls) {
        Evaluator ev = new Evaluator(type, limit, form, calls);
        Object v = ev.eval(closed);
        boolean rounded = (Object) ev.numbers instanceof DecimalLevel d && d.rounded();
        return new Result(v, ev.write(v), rounded);
    }

    // ---- the expression ----

    Object eval(Expr e) {
        return switch (e) {
            case Expr.Num n -> numbers.ofInteger(n.value());
            case Expr.Decimal d -> numbers.ofDecimal(d.value()).orElseThrow(() -> new CalculatorException(
                    "a decimal is not an Integer: whether " + Printer.print(d)
                            + " is a pair over a power of ten or its lowest terms is not chosen"));
            case Expr.Omega o -> named(TractionAlgebra.Q, 1, 0);
            case Expr.Named n -> switch (n.name()) {
                case "_0" -> named(TractionAlgebra.Q, 0, -1);
                case "_1" -> named(TractionAlgebra.Q, 1, -1);
                case "i" -> named(TractionAlgebra.C, 1, 0);
                default -> throw new IllegalStateException("no named value " + n.name());
            };
            case Expr.Construct c -> new Pair<>(TractionAlgebra.valueOf(c.algebra()), eval(c.p()), eval(c.q()));
            case Expr.Neg n -> neg(eval(n.operand()));
            case Expr.Add a -> add(eval(a.left()), eval(a.right()));
            case Expr.Sub s -> sub(eval(s.left()), eval(s.right()));
            case Expr.Mul m -> mul(eval(m.left()), eval(m.right()));
            case Expr.Div d -> div(eval(d.left()), eval(d.right()));
            case Expr.Pow p -> pow(eval(p.base()), eval(p.exponent()), p);
            case Expr.Var v -> throw new IllegalStateException("free variable " + v.name());
            case Expr.Call c -> calls.call(c, c.args().stream().map(this::eval).toList());
        };
    }

    /** {@code ω = Q(1, 0)}, {@code _0 = Q(0, −1)}, {@code _1 = Q(1, −1)}, {@code i = C(1, 0)}: named pairs, exact. */
    private Pair<Object> named(TractionAlgebra a, long p, long q) {
        return new Pair<>(a, numbers.ofInteger(BigInteger.valueOf(p)), numbers.ofInteger(BigInteger.valueOf(q)));
    }

    // ---- meeting ----

    private static boolean isPair(Object x) {
        return x instanceof Pair<?>;
    }

    @SuppressWarnings("unchecked")
    private static Pair<Object> pair(Object x) {
        return (Pair<Object>) x;
    }

    /**
     * {@code v} entered whole into {@code a} on its slot: {@code q} with {@code 0} on {@code p}, or for {@code Q}
     * {@code p} over {@code 1}, and for {@code P} {@code q} times {@code 1}. The identity is a typeless number.
     */
    Pair<Object> slot(TractionAlgebra a, Object v) {
        return switch (a) {
            case Q -> new Pair<>(a, v, numbers.one());
            case P -> new Pair<>(a, numbers.one(), v);
            case C, D, S -> new Pair<>(a, numbers.zero(), v);
        };
    }

    /**
     * The algebra two values meet in, or empty for two numbers. A number takes the pair's algebra. Two sums or two
     * products meet in the left one's; a sum and a product as the form says.
     */
    private Optional<TractionAlgebra> meeting(Object x, Object y) {
        if (!isPair(x) && !isPair(y)) return Optional.empty();
        if (!isPair(x)) return Optional.of(pair(y).algebra());
        if (!isPair(y)) return Optional.of(pair(x).algebra());
        TractionAlgebra a = pair(x).algebra(), b = pair(y).algebra();
        boolean left = a == b || a.isSum() == b.isSum() || a.isSum() == (form == Form.SUM_OF_PRODUCTS);
        return Optional.of(left ? a : b);
    }

    /** {@code v} as a pair of {@code a}: itself if it is one, and otherwise entered on the slot. */
    private Pair<Object> in(TractionAlgebra a, Object v) {
        return isPair(v) && pair(v).algebra() == a ? pair(v) : slot(a, v);
    }

    // ---- the operations ----

    Object neg(Object x) {
        if (!isPair(x)) return numbers.neg(x);
        Pair<Object> v = pair(x);
        return switch (v.algebra()) {
            case Q, P -> new Pair<>(v.algebra(), neg(v.p()), v.q());
            case C, D, S -> new Pair<>(v.algebra(), neg(v.p()), neg(v.q()));
        };
    }

    /** {@code Q}: the sum of fractions, {@code (ps + rq, qs)}. {@code C}, {@code D}, {@code S}: the sum of the points. {@code P}: {@code S(x, y)}. */
    Object add(Object x, Object y) {
        Optional<TractionAlgebra> at = meeting(x, y);
        if (at.isEmpty()) return numbers.add(x, y);
        TractionAlgebra a = at.get();
        if (a == TractionAlgebra.P) return new Pair<>(TractionAlgebra.S, x, y);
        Pair<Object> u = in(a, x), v = in(a, y);
        return switch (a) {
            case Q -> new Pair<>(a, add(mul(u.p(), v.q()), mul(v.p(), u.q())), mul(u.q(), v.q()));
            case C, D, S -> new Pair<>(a, add(u.p(), v.p()), add(u.q(), v.q()));
            case P -> throw new AssertionError();
        };
    }

    /** {@code x + (−y)}, with a number type's own subtraction. {@code P}: {@code D(y, x)}. */
    Object sub(Object x, Object y) {
        Optional<TractionAlgebra> at = meeting(x, y);
        if (at.isEmpty()) return numbers.sub(x, y);
        if (at.get() == TractionAlgebra.P) return new Pair<>(TractionAlgebra.D, y, x);
        return add(x, neg(y));
    }

    /**
     * {@code Q}, {@code P}: coordinate by coordinate, {@code (pr, qs)}. {@code C}: {@code ⊗}, {@code (ps + rq, qs − pr)}.
     * {@code D}, {@code S}: {@code ⊚}, {@code (ps + rq, qs + pr)}.
     */
    Object mul(Object x, Object y) {
        Optional<TractionAlgebra> at = meeting(x, y);
        if (at.isEmpty()) return numbers.mul(x, y);
        TractionAlgebra a = at.get();
        Pair<Object> u = in(a, x), v = in(a, y);
        return switch (a) {
            case Q, P -> new Pair<>(a, mul(u.p(), v.p()), mul(u.q(), v.q()));
            case C -> new Pair<>(a, add(mul(u.p(), v.q()), mul(v.p(), u.q())), sub(mul(u.q(), v.q()), mul(u.p(), v.p())));
            case D, S -> new Pair<>(a, add(mul(u.p(), v.q()), mul(v.p(), u.q())), add(mul(u.q(), v.q()), mul(u.p(), v.p())));
        };
    }

    /**
     * {@code Q}: {@code x} times the swap of {@code y}. {@code C}: {@code x} times {@code (−r, s)/(r² + s²)}.
     * {@code D}, {@code S}: {@code x} times {@code (−r, s)/(s² − r²)}, the {@code ⊚} inverse. {@code P}:
     * {@code (p/r, q/s)}. The divisions inside are this evaluator's, so over the integers they are {@code Q} pairs.
     * In the product-of-sums form a sum divided by a sum is {@code Q(x, y)} instead. Two numbers the number type
     * cannot divide are {@code Q(x, y)}.
     */
    Object div(Object x, Object y) {
        Optional<TractionAlgebra> at = meeting(x, y);
        if (at.isEmpty()) return numbers.divide(x, y).orElseGet(() -> new Pair<>(TractionAlgebra.Q, x, y));
        TractionAlgebra a = at.get();
        if (a.isSum() && form == Form.PRODUCT_OF_SUMS) return new Pair<>(TractionAlgebra.Q, x, y);
        Pair<Object> u = in(a, x), v = in(a, y);
        return switch (a) {
            case Q -> mul(u, new Pair<>(a, v.q(), v.p()));
            case C -> mul(u, inverse(a, v, add(mul(v.p(), v.p()), mul(v.q(), v.q()))));
            case D, S -> mul(u, inverse(a, v, sub(mul(v.q(), v.q()), mul(v.p(), v.p()))));
            case P -> new Pair<>(a, div(u.p(), v.p()), div(u.q(), v.q()));
        };
    }

    /** {@code (−r/N, s/N)}: the conjugate over the norm. */
    private Pair<Object> inverse(TractionAlgebra a, Pair<Object> v, Object norm) {
        return new Pair<>(a, div(neg(v.p()), norm), div(v.q(), norm));
    }

    /**
     * A power. The exponent is a number. A number raised to a number is the number type's own power where it has
     * one, as a double's {@code 2^0.5}. Otherwise the exponent is whole: {@code Q} and {@code P} raise each
     * coordinate, the others multiply the base by itself, and a negative power is {@code 1} divided by the positive
     * one.
     */
    Object pow(Object base, Object exponent, Expr.Pow written) {
        if (isPair(exponent))
            throw new CalculatorException("an exponent is a number, and " + Printer.print(written.exponent()) + " is a pair");
        if (!isPair(base)) {
            Optional<Object> own = numbers.pow(base, exponent);
            if (own.isPresent()) return own.get();
        }
        BigInteger n = whole(exponent).orElseThrow(() -> new CalculatorException(
                "an exponent must be a whole number here, not " + Printer.print(written.exponent())));
        Object positive = power(base, n.abs());
        return n.signum() >= 0 ? positive : div(numbers.one(), positive);
    }

    private Object power(Object base, BigInteger n) {
        if (!isPair(base))
            return numbers.power(base, n).orElseThrow(() -> new IllegalStateException("no power " + n + " of a number"));
        Pair<Object> b = pair(base);
        if (b.algebra() == TractionAlgebra.Q || b.algebra() == TractionAlgebra.P)
            return new Pair<>(b.algebra(), power(b.p(), n), power(b.q(), n));
        if (n.compareTo(BigInteger.valueOf(MAX_FACTORS)) > 0)
            throw new CalculatorException("an exponent of more than " + MAX_FACTORS + " is not multiplied out");
        if (n.signum() == 0) return slot(b.algebra(), numbers.one());
        Object out = base;
        for (int i = 1; i < n.intValue(); i++) out = mul(out, base);
        return out;
    }

    private static Optional<BigInteger> whole(Object n) {
        try {
            return switch (n) {
                case BigInteger i -> Optional.of(i);
                case BigDecimal d -> Optional.of(d.toBigIntegerExact());
                case Double d when d == Math.rint(d) && !d.isInfinite() -> Optional.of(BigDecimal.valueOf(d).toBigIntegerExact());
                default -> Optional.empty();
            };
        } catch (ArithmeticException notWhole) {
            return Optional.empty();
        }
    }

    // ---- writing ----

    /** {@code C(0, Q(1, 2))}: the algebra, then each coordinate; a number as its number type writes it. */
    String write(Object v) {
        if (!isPair(v)) return numbers.write(v);
        Pair<Object> x = pair(v);
        return x.algebra() + "(" + write(x.p()) + ", " + write(x.q()) + ")";
    }
}
