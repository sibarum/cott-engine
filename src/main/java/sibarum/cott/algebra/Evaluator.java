package sibarum.cott.algebra;

import sibarum.cott.calculator.CalculatorException;
import sibarum.cott.notation.Expr;
import sibarum.cott.notation.Printer;
import sibarum.cott.projection.Rational;
import sibarum.cott.traction.Lean;

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

    /**
     * What an evaluation answers: the value, how it is written by its constructors, how it is written in the ordinary
     * notation ({@link #writeOrdinary}), and whether a Decimal was rounded.
     */
    public record Result(Object value, String text, String ordinary, boolean rounded) {}

    /** The most factors a power of a pair is multiplied out of, one at a time. */
    static final int MAX_FACTORS = 1 << 16;

    private final Level<Object> numbers;
    private final Form form;
    private final Cancellation cancellation;
    private final Calls calls;

    @SuppressWarnings("unchecked")
    Evaluator(NumberType type, SizeLimit limit, Form form, Cancellation cancellation, Calls calls) {
        this.numbers = (Level<Object>) type.level(limit);
        this.form = form;
        this.cancellation = cancellation;
        this.calls = calls;
    }

    public static Result evaluate(Expr closed, NumberType type, SizeLimit limit, Form form) {
        return evaluate(closed, type, limit, form, Cancellation.RESIDUAL);
    }

    public static Result evaluate(Expr closed, NumberType type, SizeLimit limit, Form form, Cancellation cancellation) {
        return evaluate(closed, type, limit, form, cancellation, (c, args) -> {
            throw new CalculatorException(c.name() + " is not in the traction algebras yet");
        });
    }

    public static Result evaluate(Expr closed, NumberType type, SizeLimit limit, Form form, Cancellation cancellation,
                                  Calls calls) {
        Evaluator ev = new Evaluator(type, limit, form, cancellation, calls);
        Object v = ev.eval(closed);
        boolean rounded = (Object) ev.numbers instanceof DecimalLevel d && d.rounded();
        return new Result(v, ev.write(v), ev.writeOrdinary(v), rounded);
    }

    // ---- the expression ----

    Object eval(Expr e) {
        return switch (e) {
            case Expr.Num n -> written(number(n));
            case Expr.Decimal d -> written(number(d));
            case Expr.Omega o -> residual() ? named(TractionAlgebra.Z, 1, -1) : named(TractionAlgebra.Q, 1, 0);
            case Expr.Named n -> switch (n.name()) {
                case "_0" -> named(TractionAlgebra.Q, 0, -1);
                case "_1" -> named(TractionAlgebra.Q, 1, -1);
                case "i" -> named(TractionAlgebra.C, 1, 0);
                default -> throw new IllegalStateException("no named value " + n.name());
            };
            case Expr.Construct c -> new Pair<>(TractionAlgebra.valueOf(c.algebra()), coordinate(c.p()), coordinate(c.q()));
            case Expr.Neg n -> neg(eval(n.operand()));
            case Expr.Add a -> add(eval(a.left()), eval(a.right()));
            case Expr.Sub s -> sub(eval(s.left()), eval(s.right()));
            case Expr.Mul m -> mul(eval(m.left()), eval(m.right()));
            case Expr.Div d -> div(eval(d.left()), eval(d.right()));
            case Expr.Pow p -> pow(eval(p.base()), coordinate(p.exponent()), p);
            case Expr.Var v -> throw new IllegalStateException("free variable " + v.name());
            case Expr.Call c -> calls.call(c, c.args().stream().map(this::coordinate).toList());
        };
    }

    /** A literal as a number of the number type. */
    private Object number(Expr literal) {
        return switch (literal) {
            case Expr.Num n -> numbers.ofInteger(n.value());
            case Expr.Decimal d -> numbers.ofDecimal(d.value()).orElseThrow(() -> new CalculatorException(
                    "a decimal is not an Integer: whether " + Printer.print(d)
                            + " is a pair over a power of ten or its lowest terms is not chosen"));
            default -> throw new IllegalStateException("not a literal: " + Printer.print(literal));
        };
    }

    /**
     * A number written as a value. A written {@code 0} is the invertible zero, {@code Z(1, 1)}, so it stays as a
     * residual wherever it is used: {@code (0 + 3)^2} is {@code 9 + 6·0 + 0^2}, and {@code 1/0} is {@code Z(1/1, −1)}.
     * A {@code 0} that is computed, as {@code 3 − 3}, is the number.
     */
    private Object written(Object n) {
        return residual() && vanishes(n) ? named(TractionAlgebra.Z, 1, 1) : n;
    }

    /**
     * Whether a written {@code 0} and {@code ω} are {@code Z}'s, {@code Z(1, 1)} and {@code Z(1, −1)}: in the
     * {@code residual} mode over exact numbers. In the {@code absorb} mode, and for a double, they are the number
     * {@code 0} and {@code Q(1, 0)}.
     */
    private boolean residual() {
        return cancellation == Cancellation.RESIDUAL && !((Object) numbers instanceof IeeeLevel);
    }

    /**
     * Where a number is meant rather than a value: a constructor's coordinate, an exponent, a function's argument. A
     * literal there, signed or not, is the number, so {@code Z(1, 0)} is of order {@code 0} and {@code x^0} is a power.
     * Anything else is evaluated as a value.
     */
    private Object coordinate(Expr e) {
        return switch (e) {
            case Expr.Num n -> number(n);
            case Expr.Decimal d -> number(d);
            case Expr.Neg(Expr operand) when operand instanceof Expr.Num || operand instanceof Expr.Decimal ->
                    neg(number(operand));
            default -> eval(e);
        };
    }

    /**
     * {@code ω = Z(1, −1)} ({@code Q(1, 0)} where a written {@code 0} is the number), {@code _0 = Q(0, −1)},
     * {@code _1 = Q(1, −1)}, {@code i = C(1, 0)}: named pairs, exact.
     */
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
     * {@code p} over {@code 1}, for {@code P} {@code q} times {@code 1}, for {@code Z} {@code p} at order {@code 0},
     * and for {@code E} {@code q} plus {@code log₀ 1}. The identity is a typeless number. {@code B} has no slot.
     */
    @Lean({"T.ratioInt", "T.complexInt", "T.ZeroScale.Z.ofInt", "T.ZeroScale.E_add_C"})
    Pair<Object> slot(TractionAlgebra a, Object v) {
        return switch (a) {
            case Q -> new Pair<>(a, v, numbers.one());
            case P, E -> new Pair<>(a, numbers.one(), v);
            case Z -> new Pair<>(a, v, numbers.zero());
            case C, D, S -> new Pair<>(a, numbers.zero(), v);
            case B, L -> throw new IllegalStateException(a + " has no slot");
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

    /**
     * {@code Q}, {@code P}, {@code Z}: the sign on {@code p}, so {@code Z}'s {@code −0 = Z(−1, 1)} is not {@code 0}.
     * {@code C}, {@code D}, {@code S}: both coordinates. {@code E}: {@code Z}'s division, {@code E(1/p, −q)}, since its
     * {@code +} is {@code Z}'s {@code ·}. {@code B}: {@code D(x, 0)}, {@code 0 − x}.
     */
    @Lean({"TOver.instNeg", "T2.neg_def", "T.oplusInverse", "T.ZeroScale.Z.negZero_eq", "T.ZeroScale.Residual.R.neg",
            "T.ZeroScale.E_add"})
    Object neg(Object x) {
        if (!isPair(x)) return numbers.neg(x);
        Pair<Object> v = pair(x);
        return switch (v.algebra()) {
            case Q, P, Z -> new Pair<>(v.algebra(), neg(v.p()), v.q());
            case C, D, S -> new Pair<>(v.algebra(), neg(v.p()), neg(v.q()));
            case E -> new Pair<>(v.algebra(), div(numbers.one(), v.p()), neg(v.q()));
            case B -> new Pair<>(TractionAlgebra.D, x, numbers.zero());
            case L -> new Pair<>(v.algebra(), neg(v.p()), neg(v.q()));
        };
    }

    /**
     * {@code Q}: the sum of fractions, {@code (ps + rq, qs)}. {@code C}, {@code D}, {@code S}: the sum of the points.
     * {@code Z}: the coefficients added at one order, and two orders are {@code S(x, y)}. {@code E}: {@code Z}'s product,
     * {@code (pr, q + s)}. {@code P}, {@code B}: {@code S(x, y)}.
     */
    @Lean({"T.Unquotiented.pairPlus", "TOver.instAdd", "T2.plus", "T2.oplus", "CC.oplus", "CTC.oplus",
            "T.Unquotiented.D_add", "T.Unquotiented.S_add"})
    Object add(Object x, Object y) {
        if (isLog(x) || isLog(y)) return logPairSum(x, y);
        Optional<TractionAlgebra> at = meeting(x, y);
        if (at.isEmpty()) return numbers.add(x, y);
        TractionAlgebra a = at.get();
        if (a == TractionAlgebra.P || a == TractionAlgebra.B) return new Pair<>(TractionAlgebra.S, x, y);
        Pair<Object> u = in(a, x), v = in(a, y);
        return switch (a) {
            case Q -> new Pair<>(a, add(mul(u.p(), v.q()), mul(v.p(), u.q())), mul(u.q(), v.q()));
            case C, D, S -> new Pair<>(a, add(u.p(), v.p()), add(u.q(), v.q()));
            case Z -> zeroSum(u, v, x, y);
            case E -> logSum(u, v);
            case P, B, L -> throw new AssertionError();
        };
    }

    private static boolean isLog(Object x) {
        return x instanceof Pair<?> p && p.algebra() == TractionAlgebra.L;
    }

    /**
     * {@code L(s, t) + L(u, v) = L(s + u, t + v)}, each coordinate a fraction {@code +}: the logarithm of the product.
     * An {@code L} and anything else is {@code S(x, y)}.
     */
    @Lean("T.Unquotiented.Lmul")
    private Object logPairSum(Object x, Object y) {
        if (!isLog(x) || !isLog(y)) return new Pair<>(TractionAlgebra.S, x, y);
        return new Pair<>(TractionAlgebra.L, add(pair(x).p(), pair(y).p()), add(pair(x).q(), pair(y).q()));
    }

    /**
     * {@code Z(a, n) + Z(b, n) = Z(a + b, n)}, at the left one's order as written. Where {@code a + b} cancels exactly,
     * the residual is {@code 0·x}, {@code Z(a, n + 1)}, unless the coefficients absorb. Sums of two orders are new
     * elements, {@code S(x, y)}. The number {@code 0}, a computed one or a typeless identity, is {@code +}'s own and
     * leaves the other as it is.
     */
    @Lean({"T.ZeroScale.toLaurent_add_same", "T.ZeroScale.Residual.R.add", "T.ZeroScale.Residual.R.cancel",
            "T.ZeroScale.Residual.R.add_neg_self"})
    private Object zeroSum(Pair<Object> u, Pair<Object> v, Object x, Object y) {
        if (!isPair(x) && vanishes(x)) return y;
        if (!isPair(y) && vanishes(y)) return x;
        if (!vanishes(sub(u.q(), v.q()))) return new Pair<>(TractionAlgebra.S, x, y);
        Object sum = add(u.p(), v.p());
        if (cancellation == Cancellation.RESIDUAL && vanishes(sum))
            return new Pair<>(TractionAlgebra.Z, u.p(), add(u.q(), numbers.one()));
        return new Pair<>(TractionAlgebra.Z, sum, u.q());
    }

    /**
     * {@code E(a, m) + E(b, n) = E(ab, m + n)}. A coefficient {@code 0} is {@code log₀ 0 = 1}, so against it the sum
     * keeps the other coefficient and gains an order: {@code E(0, m) + E(b, n) = E(b, m + n + 1)}.
     */
    @Lean({"T.ZeroScale.E_add", "T.ZeroScale.E_add_zero"})
    private Object logSum(Pair<Object> u, Pair<Object> v) {
        Object order = add(u.q(), v.q());
        if (vanishes(u.p())) return new Pair<>(TractionAlgebra.E, v.p(), add(order, numbers.one()));
        if (vanishes(v.p())) return new Pair<>(TractionAlgebra.E, u.p(), add(order, numbers.one()));
        return new Pair<>(TractionAlgebra.E, mul(u.p(), v.p()), order);
    }

    /** {@code x + (−y)}, with a number type's own subtraction. {@code P}, {@code B}: {@code D(y, x)}. */
    Object sub(Object x, Object y) {
        if (isLog(x) != isLog(y)) return new Pair<>(TractionAlgebra.D, y, x);
        Optional<TractionAlgebra> at = meeting(x, y);
        if (at.isEmpty()) return numbers.sub(x, y);
        if (at.get() == TractionAlgebra.P || at.get() == TractionAlgebra.B) return new Pair<>(TractionAlgebra.D, y, x);
        return add(x, neg(y));
    }

    /**
     * Whether a value is exactly {@code 0}: a number that is, a {@code Q}, {@code Z} or {@code P} with a vanishing
     * factor, a {@code C} with both coordinates, a {@code D} or {@code S} whose difference or sum is. An {@code E} or a
     * {@code B} is never taken for one.
     */
    private boolean vanishes(Object v) {
        return switch (v) {
            case BigInteger n -> n.signum() == 0;
            case BigDecimal d -> d.signum() == 0;
            case Double d -> d == 0;
            case Pair<?> x -> switch (x.algebra()) {
                case Q, Z -> vanishes(x.p());
                case P -> vanishes(x.p()) || vanishes(x.q());
                case C -> vanishes(x.p()) && vanishes(x.q());
                case D -> vanishes(sub(x.q(), x.p()));
                case S -> vanishes(add(x.p(), x.q()));
                case E, B, L -> false;
            };
            default -> throw new IllegalStateException("not a value: " + v);
        };
    }

    /**
     * {@code Q}, {@code P}: coordinate by coordinate, {@code (pr, qs)}. {@code C}: {@code ⊗}, {@code (ps + rq, qs − pr)}.
     * {@code D}, {@code S}: {@code ⊚}, {@code (ps + rq, qs + pr)}. {@code Z}: {@code (pr, q + s)}. {@code E}: a whole
     * multiple only. {@code B}: composition only.
     */
    @Lean({"T.Unquotiented.pairTimes", "T.Unquotiented.pairOtimes", "T.Unquotiented.pairSplit", "TOver.instMul",
            "T2.times", "T2.otimes", "CC.otimes", "CTC.otimes"})
    Object mul(Object x, Object y) {
        if (isLog(x) || isLog(y)) return logPairMultiple(x, y);
        Optional<TractionAlgebra> at = meeting(x, y);
        if (at.isEmpty()) return numbers.mul(x, y);
        TractionAlgebra a = at.get();
        if (a == TractionAlgebra.E) return logProduct(x, y);
        if (a == TractionAlgebra.B) return composition(x, y);
        Pair<Object> u = in(a, x), v = in(a, y);
        return switch (a) {
            case Q, P -> new Pair<>(a, mul(u.p(), v.p()), mul(u.q(), v.q()));
            case C -> new Pair<>(a, add(mul(u.p(), v.q()), mul(v.p(), u.q())), sub(mul(u.q(), v.q()), mul(u.p(), v.p())));
            case D, S -> new Pair<>(a, add(mul(u.p(), v.q()), mul(v.p(), u.q())), add(mul(u.q(), v.q()), mul(u.p(), v.p())));
            case Z -> absorbs(x) || absorbs(y) ? numbers.zero() : zeroProduct(u, v);
            case E, B, L -> throw new AssertionError();
        };
    }

    /**
     * A rational {@code k} times {@code L(s, t)} is {@code L(k·s, k·t)}, the logarithm of the value's {@code k}-th power:
     * {@code (1/2)·L(0, 1/2) = L(0/2, 1/4)}, {@code (−1)^(1/2) = i}. Each coordinate is multiplied as a fraction, so
     * nothing is reduced. Any other product with an {@code L} is {@code P(x, y)}.
     */
    private Object logPairMultiple(Object x, Object y) {
        Object k = isLog(x) ? y : x, l = isLog(x) ? x : y;
        if (isLog(k) || Logs.ratio(k).isEmpty()) return new Pair<>(TractionAlgebra.P, x, y);
        return new Pair<>(TractionAlgebra.L, mul(k, pair(l).p()), mul(k, pair(l).q()));
    }

    /**
     * Whether {@code x} is the number {@code 0}, a computed one or a typeless identity, which absorbs a product or a
     * quotient with a {@code Z}: it is the classical {@code 0}, not {@code Z(1, 1)}.
     */
    private boolean absorbs(Object x) {
        return !isPair(x) && vanishes(x);
    }

    /** {@code Z(a, m)·Z(b, n) = Z(ab, m + n)}: the exponent law. */
    @Lean({"T.ZeroScale.Z.mul_def", "T.ZeroScale.toLaurent_mul", "T.ZeroScale.Residual.R.mul"})
    private Pair<Object> zeroProduct(Pair<Object> u, Pair<Object> v) {
        return new Pair<>(TractionAlgebra.Z, mul(u.p(), v.p()), add(u.q(), v.q()));
    }

    /**
     * A whole number {@code k} times {@code E(p, q)} is its multiple, {@code E(pᵏ, kq)}, {@code Z}'s power; a negative
     * one is the multiple's {@code −}. A product of two {@code E}s would multiply logarithms, which no operation of
     * theirs does, so any other product is {@code P(x, y)}.
     */
    @Lean({"T.ZeroScale.E_nsmul", "T.ZeroScale.Z.pow_def"})
    private Object logProduct(Object x, Object y) {
        Optional<BigInteger> k = !isPair(x) ? whole(x) : !isPair(y) ? whole(y) : Optional.empty();
        if (k.isEmpty()) return new Pair<>(TractionAlgebra.P, x, y);
        Pair<Object> v = pair(isPair(x) ? x : y);
        Pair<Object> multiple = new Pair<>(TractionAlgebra.E, power(v.p(), k.get().abs()),
                mul(numbers.ofInteger(k.get().abs()), v.q()));
        return k.get().signum() >= 0 ? multiple : neg(multiple);
    }

    /**
     * {@code B(p, q)·B(q, r) = B(p, r)}, where the inner {@code q} is not {@code 0}, {@code 1} or {@code −1}. Any other
     * product is {@code P(x, y)}.
     */
    @Lean("T.ZeroScale.B_mul_B")
    private Object composition(Object x, Object y) {
        if (isPair(x) && isPair(y) && pair(x).algebra() == TractionAlgebra.B && pair(y).algebra() == TractionAlgebra.B) {
            Object q = pair(x).q();
            if (vanishes(sub(q, pair(y).p())) && !vanishes(q) && !vanishes(sub(q, numbers.one()))
                    && !vanishes(add(q, numbers.one())))
                return new Pair<>(TractionAlgebra.B, pair(x).p(), pair(y).q());
        }
        return new Pair<>(TractionAlgebra.P, x, y);
    }

    /**
     * {@code Q}: {@code x} times the swap of {@code y}. {@code C}: {@code x} times {@code (−r, s)/(r² + s²)}.
     * {@code D}, {@code S}: {@code x} times {@code (−r, s)/(s² − r²)}, the {@code ⊚} inverse. {@code P}:
     * {@code (p/r, q/s)}. {@code Z}: {@code (p/r, q − s)}. The divisions inside are this evaluator's, so over the
     * integers they are {@code Q} pairs. In the product-of-sums form a sum divided by a sum is {@code Q(x, y)} instead.
     * Two numbers the number type cannot divide are {@code Q(x, y)}, and so is a quotient of {@code B}s, or of
     * {@code E}s other than two of order {@code 0}, which is a {@code B}. So is a quotient by a {@code C}, {@code D} or
     * {@code S} whose norm is itself a sum, as {@code 1/(1 + 0)}: the inverse would be divided by a sum again, without
     * end, since it is a series in {@code 0}.
     */
    @Lean({"TOver.reciprocal", "T2.reciprocal", "T2.pointInv", "CTC.inv", "T.ZeroScale.Residual.R.inv"})
    Object div(Object x, Object y) {
        if (isLog(x) && !isLog(y) && Logs.ratio(y).isPresent()) return logPairMultiple(div(numbers.one(), y), x);
        if (isLog(x) || isLog(y)) return new Pair<>(TractionAlgebra.Q, x, y);
        Optional<TractionAlgebra> at = meeting(x, y);
        if (at.isEmpty()) return numbers.divide(x, y).orElseGet(() -> new Pair<>(TractionAlgebra.Q, x, y));
        TractionAlgebra a = at.get();
        if (a == TractionAlgebra.E) return logRatio(x, y);
        if (a == TractionAlgebra.B || a.isSum() && form == Form.PRODUCT_OF_SUMS) return new Pair<>(TractionAlgebra.Q, x, y);
        Pair<Object> u = in(a, x), v = in(a, y);
        return switch (a) {
            case Q -> mul(u, new Pair<>(a, v.q(), v.p()));
            case C -> byNorm(x, y, u, v, add(mul(v.p(), v.p()), mul(v.q(), v.q())));
            case D, S -> byNorm(x, y, u, v, sub(mul(v.q(), v.q()), mul(v.p(), v.p())));
            case P -> new Pair<>(a, div(u.p(), v.p()), div(u.q(), v.q()));
            case Z -> absorbs(x) ? numbers.zero() : zeroProduct(u, new Pair<>(a, div(numbers.one(), v.p()), neg(v.q())));
            case E, B, L -> throw new AssertionError();
        };
    }

    /**
     * {@code E(q, 0) / E(p, 0) = B(p, q)}: both are multiples of {@code 0}, and the {@code 0} of the {@code 0/0}
     * cancels. Any other quotient with an {@code E} is {@code Q(x, y)}.
     */
    @Lean({"T.ZeroScale.E_eq_B_mul", "T.ZeroScale.E_eq"})
    private Object logRatio(Object x, Object y) {
        if (isPair(x) && isPair(y) && pair(x).algebra() == TractionAlgebra.E && pair(y).algebra() == TractionAlgebra.E
                && vanishes(pair(x).q()) && vanishes(pair(y).q()))
            return new Pair<>(TractionAlgebra.B, pair(y).p(), pair(x).p());
        return new Pair<>(TractionAlgebra.Q, x, y);
    }

    /** {@code u} times the inverse of {@code v} over its norm, or {@code Q(x, y)} where the norm is a sum. */
    private Object byNorm(Object x, Object y, Pair<Object> u, Pair<Object> v, Object norm) {
        if (isPair(norm) && pair(norm).algebra().isSum()) return new Pair<>(TractionAlgebra.Q, x, y);
        return mul(u, inverse(v.algebra(), v, norm));
    }

    /** {@code (−r/N, s/N)}: the conjugate over the norm. */
    private Pair<Object> inverse(TractionAlgebra a, Pair<Object> v, Object norm) {
        return new Pair<>(a, div(neg(v.p()), norm), div(v.q(), norm));
    }

    /**
     * A power. The exponent is a number. A number raised to a number is the number type's own power where it has
     * one, as a double's {@code 2^0.5}. Otherwise the exponent is whole: {@code Q} and {@code P} raise each
     * coordinate, {@code Z} raises its coefficient and multiplies its order, the others multiply the base by itself,
     * and a negative power is {@code 1} divided by the positive one.
     */
    Object pow(Object base, Object exponent, Expr.Pow written) {
        if (isPair(exponent) || whole(exponent).isEmpty()) {
            Optional<Object> angle = fractionalPower(base, exponent, written == null ? null : written.exponent());
            if (angle.isPresent()) return angle.get();
        }
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

    /**
     * A power that is not whole, in angles: the {@link TractionAlgebra#L L} of the answer, where the base has one
     * exactly. The exponent is the ratio {@code p : q} it is written as, unreduced: {@code Q(p, q)}, a number
     * {@code v : 1}, or {@code c·ω} as {@code c : 0}.
     *
     * <ul>
     *   <li>A written {@code 0} follows {@code zeroPow}: {@code 0^(1/2)} is {@code L(Q(−1, 0), Q(1, 2))}, a zero
     *       magnitude turned half a turn, {@code 0^(1/0) = −1} and {@code 0^(0/1) = 1}. {@code 0^(0/0)} has no rule.</li>
     *   <li>{@code 1}, {@code i}, {@code −1} and {@code −i}, as numbers or points, have {@code L(0, t)} with {@code t}
     *       their turn, and the power is {@code (p/q)·L(0, t)}: {@code (−1)^(1/2) = L(0/2, 1/4)}, which is {@code i}.</li>
     * </ul>
     *
     * Empty for any other base, a double, or an exponent that is no ratio, which keep the refusals they had.
     */
    @Lean({"T.Unquotiented.zeroPow", "T.Unquotiented.Lr", "T.Unquotiented.Lr_zero_one", "T.Unquotiented.Lr_zero_negOne",
            "T.Unquotiented.turn_one_zero", "T.Unquotiented.turn_zero_negOne", "T.Unquotiented.turn_zero_one"})
    private Optional<Object> fractionalPower(Object base, Object exponent, Expr written) {
        if ((Object) numbers instanceof IeeeLevel) return Optional.empty();
        Optional<Logs.Ratio> ratio = writtenRatio(written).or(() -> exponentRatio(exponent));
        if (ratio.isEmpty()) return Optional.empty();
        if (writtenZero(base)) {
            Logs.ZeroPower power = Logs.zeroPow(ratio.get().a(), ratio.get().b()).orElseThrow(() ->
                    new CalculatorException("0^(0/0) has no rule: the exponent's ratio is 0 : 0"));
            return Optional.of(switch (power) {
                case Logs.ZeroPower.One one -> new Pair<>(TractionAlgebra.L, numbers.zero(), numbers.zero());
                case Logs.ZeroPower.NegOne negOne -> new Pair<>(TractionAlgebra.L, numbers.zero(), half(1));
                case Logs.ZeroPower.Rotation r -> new Pair<>(TractionAlgebra.L,
                        new Pair<>(TractionAlgebra.Q, numbers.ofInteger(BigInteger.ONE.negate()), numbers.zero()),
                        new Pair<>(TractionAlgebra.Q, value(r.p()), value(r.q())));
            });
        }
        Logs.Ratio r = ratio.get();
        if (r.b().num().signum() == 0) return Optional.empty();
        Object k = r.b().equals(Rational.of(1, 1)) ? value(r.a()) : new Pair<>(TractionAlgebra.Q, value(r.a()), value(r.b()));
        return unitTurn(base).map(turn -> mul(k, new Pair<>(TractionAlgebra.L, numbers.zero(), turn)));
    }

    /**
     * The ratio an exponent is written as, where it is written with literals: {@code 0/1} is {@code 0 : 1} and
     * {@code ω} is {@code 1 : 0}, the numbers themselves, though a written {@code 0} evaluates to {@code Z(1, 1)}.
     */
    private static Optional<Logs.Ratio> writtenRatio(Expr e) {
        return switch (e) {
            case Expr.Div(Expr p, Expr q) -> literal(p).flatMap(a -> literal(q).map(b -> new Logs.Ratio(a, b)));
            case Expr.Construct(String a, Expr p, Expr q) when a.equals("Q") ->
                    literal(p).flatMap(x -> literal(q).map(y -> new Logs.Ratio(x, y)));
            case Expr.Omega o -> Optional.of(new Logs.Ratio(Rational.of(1, 1), Rational.ZERO));
            case null, default -> Optional.empty();
        };
    }

    private static Optional<Rational> literal(Expr e) {
        return switch (e) {
            case Expr.Num n -> Logs.rational(n.value());
            case Expr.Decimal d -> Logs.rational(d.value());
            case Expr.Neg(Expr operand) -> literal(operand).map(Rational::neg);
            default -> Optional.empty();
        };
    }

    /** The exponent as the ratio it is written as: {@code Q(p, q)} of numbers, {@code v : 1}, or {@code c·ω} as {@code c : 0}. */
    private static Optional<Logs.Ratio> exponentRatio(Object exponent) {
        if (exponent instanceof Pair<?>(TractionAlgebra a, Object c, Object order) && a == TractionAlgebra.Z
                && order instanceof BigInteger n && n.equals(BigInteger.ONE.negate()))
            return Logs.rational(c).map(r -> new Logs.Ratio(r, Rational.ZERO));
        return Logs.ratio(exponent);
    }

    /** A written {@code 0}, {@code Z(1, 1)}, or the number {@code 0} where a written {@code 0} is the number. */
    private boolean writtenZero(Object base) {
        if (!isPair(base)) return vanishes(base);
        Pair<Object> b = pair(base);
        return b.algebra() == TractionAlgebra.Z && !isPair(b.p()) && !isPair(b.q())
                && whole(b.p()).filter(BigInteger.ONE::equals).isPresent()
                && whole(b.q()).filter(BigInteger.ONE::equals).isPresent();
    }

    /**
     * The turn of {@code 1}, {@code i}, {@code −1} or {@code −i}, as a number or as a point {@code C(p, q)} of two
     * numbers: {@code 0}, {@code 1/4}, {@code 1/2} and {@code −1/4}, in {@code (−1/2, 1/2]} as cott-lean's turn is.
     */
    private Optional<Object> unitTurn(Object base) {
        Optional<Rational> re, im;
        if (!isPair(base)) {
            re = Logs.rational(base);
            im = Optional.of(Rational.ZERO);
        } else if (pair(base).algebra() == TractionAlgebra.C && !isPair(pair(base).p()) && !isPair(pair(base).q())) {
            re = Logs.rational(pair(base).q());
            im = Logs.rational(pair(base).p());
        } else {
            return Optional.empty();
        }
        if (re.isEmpty() || im.isEmpty()) return Optional.empty();
        Rational one = Rational.of(1, 1), x = re.get(), y = im.get();
        if (y.equals(Rational.ZERO) && x.equals(one)) return Optional.of(numbers.zero());
        if (y.equals(Rational.ZERO) && x.equals(one.neg())) return Optional.of(half(1));
        if (x.equals(Rational.ZERO) && y.equals(one)) return Optional.of(div(numbers.one(), numbers.ofInteger(BigInteger.valueOf(4))));
        if (x.equals(Rational.ZERO) && y.equals(one.neg()))
            return Optional.of(div(numbers.ofInteger(BigInteger.ONE.negate()), numbers.ofInteger(BigInteger.valueOf(4))));
        return Optional.empty();
    }

    /** {@code n/2}, as the number type divides it. */
    private Object half(long n) {
        return div(numbers.ofInteger(BigInteger.valueOf(n)), numbers.ofInteger(BigInteger.TWO));
    }

    /** A rational as a value of the number type: its numerator over its denominator, where that is not {@code 1}. */
    private Object value(Rational r) {
        Object num = numbers.ofInteger(r.num());
        return r.den().equals(BigInteger.ONE) ? num : div(num, numbers.ofInteger(r.den()));
    }

    @Lean({"T2.power", "T.power", "T.ZeroScale.Z.pow_def"})
    private Object power(Object base, BigInteger n) {
        if (!isPair(base))
            return numbers.power(base, n).orElseThrow(() -> new IllegalStateException("no power " + n + " of a number"));
        Pair<Object> b = pair(base);
        if (b.algebra() == TractionAlgebra.Q || b.algebra() == TractionAlgebra.P)
            return new Pair<>(b.algebra(), power(b.p(), n), power(b.q(), n));
        if (b.algebra() == TractionAlgebra.Z)
            return new Pair<>(b.algebra(), power(b.p(), n), mul(numbers.ofInteger(n), b.q()));
        if (n.compareTo(BigInteger.valueOf(MAX_FACTORS)) > 0)
            throw new CalculatorException("an exponent of more than " + MAX_FACTORS + " is not multiplied out");
        if (n.signum() == 0) return b.algebra() == TractionAlgebra.B ? numbers.one() : slot(b.algebra(), numbers.one());
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

    /**
     * {@code 9/2 + 3i}: the value in the ordinary notation, as a line written without constructors is. A {@code Q} is
     * the quotient {@code p/q}, unreduced, and a {@code C} is {@code q + pi}, leaving out a coordinate that is the
     * number {@code 0}. Each reads back as the same value, though not always as the same pair: {@code 3} is the value
     * of {@code C(0, 3)}, and a {@code Q} of {@code Q}s reads back multiplied out. The other algebras have no such
     * writing and keep their constructors, with their coordinates written this way.
     */
    String writeOrdinary(Object v) {
        return Printer.print(ordinary(v));
    }

    private Expr ordinary(Object v) {
        if (!isPair(v)) return negative(v) ? new Expr.Neg(number(numbers.neg(v))) : number(v);
        Pair<Object> x = pair(v);
        return switch (x.algebra()) {
            case Q -> new Expr.Div(ordinary(x.p()), ordinary(x.q()));
            case C -> complex(x.q(), x.p());
            default -> new Expr.Construct(x.algebra().name(), ordinary(x.p()), ordinary(x.q()));
        };
    }

    /** {@code re + im·i}, as {@code 3 - 2i}, {@code -i} or {@code 1/2·i}. */
    private Expr complex(Object re, Object im) {
        if (isZero(im)) return ordinary(re);
        boolean subtract = !isZero(re) && negative(im);
        Object coefficient = subtract ? neg(im) : im;
        Expr i = new Expr.Named("i");
        Expr imaginary;
        if (isOne(coefficient)) imaginary = i;
        else if (isOne(neg(coefficient))) imaginary = new Expr.Neg(i);
        else imaginary = new Expr.Mul(ordinary(coefficient), i, !isPair(coefficient) && plain(coefficient));
        if (isZero(re)) return imaginary;
        return subtract ? new Expr.Sub(ordinary(re), imaginary) : new Expr.Add(ordinary(re), imaginary);
    }

    /**
     * A number that is not negative, as its number type writes it. A Decimal or a double goes to the printer as an
     * atom of that text, so {@code 1.5·10^-7} and {@code ∞} are written as they always are.
     */
    private Expr number(Object v) {
        return v instanceof BigInteger n ? new Expr.Num(n) : new Expr.Var(numbers.write(v));
    }

    /** Whether a number is written in plain digits, so that {@code 2i} can be written by juxtaposition. */
    private boolean plain(Object v) {
        return numbers.write(v).matches("-?[0-9]+(\\.[0-9]+)?");
    }

    /** A negative number, or a {@code Q}, {@code P} or {@code Z} whose sign, on {@code p}, is. */
    private static boolean negative(Object v) {
        return switch (v) {
            case BigInteger n -> n.signum() < 0;
            case BigDecimal d -> d.signum() < 0;
            case Double d -> d < 0;
            case Pair<?> x -> switch (x.algebra()) {
                case Q, P, Z -> negative(x.p());
                default -> false;
            };
            default -> false;
        };
    }

    /**
     * A term worth leaving out of a sum: the number {@code 0}, or a {@code Q} of it over a number that is not, as
     * {@code Q(0, 2)}. {@code Q(0, 0)} is not {@code 0}, and no other pair is taken for it.
     */
    private static boolean isZero(Object v) {
        if (v instanceof Pair<?>(TractionAlgebra a, Object p, Object q))
            return a == TractionAlgebra.Q && !isPair(p) && !isPair(q) && isZero(p) && !isZero(q);
        return v instanceof BigInteger n ? n.signum() == 0
                : v instanceof BigDecimal d ? d.signum() == 0
                : v instanceof Double x && x == 0;
    }

    private static boolean isOne(Object v) {
        return v instanceof BigInteger n ? n.equals(BigInteger.ONE)
                : v instanceof BigDecimal d ? d.compareTo(BigDecimal.ONE) == 0
                : v instanceof Double x && x == 1;
    }
}
