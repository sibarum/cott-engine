package sibarum.cott.algebra;

import sibarum.cott.projection.Rational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A value as a series in {@code 0}: its sums flattened, its quotients reduced, and its like terms combined, so
 * {@code (2 + 0 + 1/0)^2}, which evaluates to {@code S(S(Z(4, 1), Z(Q(4, 1), −1)), S(Z(Q(2, 1), 0), …, 4))}, is
 * {@code ω^2 + 4·ω + 6 + 4·0 + 0^2}. The first stage of the simplifier Traction-Simplifier.md describes.
 *
 * <p>It is a reading, not an evaluation: the value it is read from is kept as computed, and the series gives up which
 * pair was used, so it is on the {@link Rung#QUOTIENT quotient} rung.
 *
 * <p><b>What it reads.</b> Values built from numbers, {@code Q}, {@code Z}, {@code S}, {@code D} and {@code P}, with
 * exact coefficients: a number is of order {@code 0}, {@code Z(p, q)} is {@code p} moved up a whole {@code q} orders,
 * {@code S} and {@code D} are a sum and a difference, {@code P} a product multiplied out, and {@code Q} a quotient by
 * a denominator of one order. A {@code C} is read where its imaginary part is {@code 0}. Anything else has no series:
 * a double, an {@code E} or {@code B}, a zero denominator, a denominator of several orders (a series that never ends).
 *
 * <p><b>Like terms that cancel.</b> Terms of one order add up; where they cancel exactly, the {@link Cancellation}
 * says what is left, as it does for {@code Z}'s {@code +}. In the residual mode a cancellation that a {@code Z} takes
 * part in leaves {@code 0} times the first term, one order up, where it is combined in turn: {@code 3·0 − 3·0} is
 * {@code 3·0^2}. Plain numbers that cancel leave the number {@code 0}, as {@code 1 − 1} does. In the absorb mode a
 * cancelled order is gone.
 */
public final class Simplifier {

    private Simplifier() {
    }

    /** One term: a coefficient at a power of {@code 0}, and whether a {@code Z} made it one. */
    private record Term(int order, Rational coefficient, boolean zero) {

        Term times(Term t) {
            return new Term(Math.addExact(order, t.order), coefficient.times(t.coefficient), zero || t.zero);
        }

        Term negated() {
            return new Term(order, coefficient.neg(), zero);
        }
    }

    /** No series: a value outside what {@link Simplifier} reads. */
    private static final class Unread extends RuntimeException {
        Unread() {
            super(null, null, false, false);
        }
    }

    /** {@code value} as a series in {@code 0}, or empty where it has none. */
    public static Optional<Object> simplify(Object value, Cancellation cancellation) {
        try {
            TreeMap<Integer, Rational> series = combine(terms(value), cancellation);
            return Optional.of(build(series, decimal(value)));
        } catch (Unread | ArithmeticException none) {
            return Optional.empty();
        }
    }

    /** The series written out: {@code ω^2 + 4·ω + 6 + 4·0 + 0^2}, from the highest power of {@code ω} down. */
    public static Optional<String> written(Object value, Cancellation cancellation) {
        try {
            return Optional.of(write(combine(terms(value), cancellation)));
        } catch (Unread | ArithmeticException none) {
            return Optional.empty();
        }
    }

    // ---- reading the terms ----

    private static List<Term> terms(Object v) {
        if (!(v instanceof Pair<?> pair)) return List.of(new Term(0, rational(v), false));
        List<Term> out = new ArrayList<>();
        switch (pair.algebra()) {
            case S -> {
                out.addAll(terms(pair.p()));
                out.addAll(terms(pair.q()));
            }
            case D -> {
                out.addAll(terms(pair.q()));
                for (Term t : terms(pair.p())) out.add(t.negated());
            }
            case P -> {
                for (Term a : terms(pair.p())) for (Term b : terms(pair.q())) out.add(a.times(b));
            }
            case Z -> {
                int order = order(pair.q());
                for (Term t : terms(pair.p())) out.add(new Term(Math.addExact(t.order(), order), t.coefficient(), true));
            }
            case Q -> {
                Term over = monomial(pair.q());
                Rational inverse = over.coefficient().inverse();
                for (Term t : terms(pair.p()))
                    out.add(new Term(Math.subtractExact(t.order(), over.order()), t.coefficient().times(inverse),
                            t.zero() || over.zero()));
            }
            case C -> {
                if (!plainSum(terms(pair.p())).isEmpty()) throw new Unread();
                out.addAll(terms(pair.q()));
            }
            case E, B -> throw new Unread();
        }
        return out;
    }

    /** A denominator: one term of one order, not {@code 0}. */
    private static Term monomial(Object v) {
        List<Term> terms = terms(v);
        TreeMap<Integer, Rational> sum = plainSum(terms);
        if (sum.size() != 1) throw new Unread();
        Map.Entry<Integer, Rational> only = sum.firstEntry();
        return new Term(only.getKey(), only.getValue(), terms.stream().anyMatch(Term::zero));
    }

    /** A {@code Z}'s order: a whole number. */
    private static int order(Object v) {
        TreeMap<Integer, Rational> sum = plainSum(terms(v));
        if (sum.isEmpty()) return 0;
        if (sum.size() != 1 || sum.firstKey() != 0 || !sum.firstEntry().getValue().den().equals(BigInteger.ONE))
            throw new Unread();
        return sum.firstEntry().getValue().num().intValueExact();
    }

    /** Terms added by order with no residual, dropping what comes to {@code 0}: for orders and denominators. */
    private static TreeMap<Integer, Rational> plainSum(List<Term> terms) {
        TreeMap<Integer, Rational> out = new TreeMap<>();
        for (Term t : terms) out.merge(t.order(), t.coefficient(), Rational::plus);
        out.values().removeIf(c -> c.num().signum() == 0);
        return out;
    }

    private static Rational rational(Object v) {
        return switch (v) {
            case BigInteger n -> new Rational(n, BigInteger.ONE);
            case BigDecimal d -> d.scale() <= 0
                    ? new Rational(d.toBigIntegerExact(), BigInteger.ONE)
                    : new Rational(d.unscaledValue(), BigInteger.TEN.pow(d.scale()));
            default -> throw new Unread();
        };
    }

    // ---- combining like terms ----

    /**
     * The terms added by order, lowest first, so that a residual is combined into the order above it: where an
     * order's terms cancel and a {@code Z} took part, the residual mode carries the first of them up an order.
     */
    private static TreeMap<Integer, Rational> combine(List<Term> terms, Cancellation cancellation) {
        TreeMap<Integer, List<Term>> byOrder = new TreeMap<>();
        for (Term t : terms) byOrder.computeIfAbsent(t.order(), k -> new ArrayList<>()).add(t);
        TreeMap<Integer, Rational> out = new TreeMap<>();
        while (!byOrder.isEmpty()) {
            Map.Entry<Integer, List<Term>> next = byOrder.pollFirstEntry();
            List<Term> like = next.getValue().stream().filter(t -> t.coefficient().num().signum() != 0).toList();
            if (like.isEmpty()) continue;
            Rational sum = Rational.ZERO;
            for (Term t : like) sum = sum.plus(t.coefficient());
            if (sum.num().signum() != 0) {
                out.put(next.getKey(), sum);
            } else if (cancellation == Cancellation.RESIDUAL && like.stream().anyMatch(Term::zero)) {
                Term first = like.getFirst();
                byOrder.computeIfAbsent(Math.addExact(next.getKey(), 1), k -> new ArrayList<>())
                        .addFirst(new Term(next.getKey() + 1, first.coefficient(), true));
            }
        }
        return out;
    }

    // ---- the series as a value ----

    /**
     * Right-nested {@code S} of its terms, from the highest power of {@code ω} down: a plain number at order
     * {@code 0} and {@code Z(c, n)} elsewhere, each coefficient a number where it is whole (or, over Decimals, where
     * it terminates) and a {@code Q} in lowest terms where it is not. No terms is the number {@code 0}.
     */
    private static Object build(TreeMap<Integer, Rational> series, boolean decimal) {
        Object out = null;
        for (Map.Entry<Integer, Rational> e : series.descendingMap().entrySet()) {
            Object c = number(e.getValue(), decimal);
            Object term = e.getKey() == 0 ? c
                    : new Pair<>(TractionAlgebra.Z, c, whole(BigInteger.valueOf(e.getKey()), decimal));
            out = out == null ? term : new Pair<>(TractionAlgebra.S, term, out);
        }
        return out == null ? whole(BigInteger.ZERO, decimal) : out;
    }

    private static Object number(Rational r, boolean decimal) {
        if (r.den().equals(BigInteger.ONE)) return whole(r.num(), decimal);
        if (decimal) {
            try {
                return new BigDecimal(r.num()).divide(new BigDecimal(r.den()));
            } catch (ArithmeticException nonTerminating) {
                // falls through to a quotient
            }
        }
        return new Pair<>(TractionAlgebra.Q, whole(r.num(), decimal), whole(r.den(), decimal));
    }

    private static Object whole(BigInteger n, boolean decimal) {
        return decimal ? new BigDecimal(n) : n;
    }

    private static boolean decimal(Object v) {
        if (v instanceof Pair<?> x) return decimal(x.p()) || decimal(x.q());
        return v instanceof BigDecimal;
    }

    // ---- the series as text ----

    private static String write(TreeMap<Integer, Rational> series) {
        if (series.isEmpty()) return "0";
        StringBuilder out = new StringBuilder();
        for (Map.Entry<Integer, Rational> e : series.entrySet()) {
            Rational c = e.getValue();
            boolean negative = c.num().signum() < 0;
            if (out.isEmpty()) {
                if (negative) out.append('-');
            } else {
                out.append(negative ? " - " : " + ");
            }
            out.append(monomial(negative ? c.neg() : c, e.getKey()));
        }
        return out.toString();
    }

    /** {@code 4·0}, {@code ω^2}, {@code (1/2)·ω}, {@code 6}: a coefficient that is not negative, at an order. */
    private static String monomial(Rational c, int order) {
        String coefficient = c.toString();
        if (order == 0) return coefficient;
        String power = order == 1 ? "0" : order == -1 ? "ω" : order > 0 ? "0^" + order : "ω^" + -order;
        if (coefficient.equals("1")) return power;
        return (c.den().equals(BigInteger.ONE) ? coefficient : "(" + coefficient + ")") + "·" + power;
    }
}
