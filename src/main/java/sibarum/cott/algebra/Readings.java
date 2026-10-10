package sibarum.cott.algebra;

import sibarum.cott.projection.Rational;
import sibarum.cott.traction.Lean;
import sibarum.cott.traction.Quotient;
import sibarum.cott.traction.T;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The readings of a value the evaluator gives, each with its {@link Rung}. They are taken from the value and never
 * replace it.
 *
 * <ul>
 *   <li>{@code value}: the number the pair stands for, each algebra read as its meaning all the way down, so
 *       {@code C(3, Q(9, 2))} is {@code 9/2 + 3i}. {@code C} over numbers loses nothing by it; any other algebra
 *       gives the same value for many pairs, a quotient.</li>
 *   <li>{@code norm} and {@code turn}: of a value off the real line, its squared length, the scale without the
 *       direction, and its direction as a fraction of a turn, without the scale.</li>
 *   <li>{@code lowest terms}: each {@code Q} of two Integers in lowest terms, where one is not.</li>
 *   <li>{@code cott-lean}: the file that proves this nesting's operations, or that none does.</li>
 * </ul>
 */
public final class Readings {

    private Readings() {}

    /** One reading: its name, as written, and its rung; a rung of {@code null} is a fact about the value, not a reading of it. */
    public record Reading(String name, String text, Rung rung) {}

    public static List<Reading> of(Object value) {
        List<Reading> out = new ArrayList<>();
        if (!(value instanceof Pair<?> pair)) return out;
        boolean flatRatio = pair.algebra() == TractionAlgebra.Q && pair.p() instanceof BigInteger && pair.q() instanceof BigInteger;
        if (!flatRatio) {
            if (hasDouble(value)) {
                approximately(value).ifPresentOrElse(
                        z -> out.add(new Reading("value", z, Rung.UP_TO_ERROR)),
                        () -> out.add(new Reading("value", "undefined", Rung.ERROR)));
            } else {
                Optional<Gaussian> z = exactly(value);
                out.add(new Reading("value", z.map(Gaussian::toString).orElse("undefined"),
                        z.isEmpty() ? Rung.ERROR : allPoints(value) ? Rung.EXACT : Rung.QUOTIENT));
                z.filter(g -> g.im().num().signum() != 0).ifPresent(g -> {
                    out.add(new Reading("norm", g.norm().toString(), Rung.UP_TO_INVARIANT));
                    out.add(new Reading("turn", turn(g), Rung.UP_TO_ERROR));
                });
            }
        }
        String lowest = lowestTerms(value);
        if (!lowest.equals(write(value))) out.add(new Reading("lowest terms", lowest, Rung.QUOTIENT));
        out.add(new Reading("cott-lean", covered(value).orElse("no file covers this nesting; computed by the engine's rules"), null));
        return out;
    }

    // ---- the value ----

    /** A Gaussian rational {@code re + im·i}. */
    record Gaussian(Rational re, Rational im) {

        static Gaussian real(Rational r) {
            return new Gaussian(r, Rational.ZERO);
        }

        Gaussian plus(Gaussian y) {
            return new Gaussian(re.plus(y.re), im.plus(y.im));
        }

        Gaussian minus(Gaussian y) {
            return plus(new Gaussian(y.re.neg(), y.im.neg()));
        }

        Gaussian times(Gaussian y) {
            return new Gaussian(re.times(y.re).plus(im.times(y.im).neg()), re.times(y.im).plus(im.times(y.re)));
        }

        Rational norm() {
            return re.times(re).plus(im.times(im));
        }

        /** {@code this / y}, or empty where {@code y} is {@code 0}. */
        Optional<Gaussian> over(Gaussian y) {
            Rational n = y.norm();
            if (n.num().signum() == 0) return Optional.empty();
            Gaussian conj = new Gaussian(y.re.times(n.inverse()), y.im.neg().times(n.inverse()));
            return Optional.of(times(conj));
        }

        /** {@code 1/10 + (7/10)i}, {@code -i}, {@code 3}. */
        @Override
        public String toString() {
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
    }

    /**
     * The value exactly, or empty where a {@code Q} or a {@code /} inside it has a zero denominator: {@code C} is
     * {@code q + ip}, {@code D} is {@code q − p}, {@code S} is {@code p + q}, {@code Q} is {@code p/q} and {@code P} is
     * {@code p·q}, each of the coordinates' values.
     */
    @Lean({"T.Unquotiented.C", "T.Unquotiented.D", "T.Unquotiented.S", "T.Unquotiented.Q", "T.Unquotiented.P"})
    static Optional<Gaussian> exactly(Object v) {
        if (v instanceof BigInteger n) return Optional.of(Gaussian.real(new Rational(n, BigInteger.ONE)));
        if (v instanceof BigDecimal d) return Optional.of(Gaussian.real(rational(d)));
        Pair<?> x = (Pair<?>) v;
        Optional<Gaussian> p = exactly(x.p()), q = exactly(x.q());
        if (p.isEmpty() || q.isEmpty()) return Optional.empty();
        Gaussian a = p.get(), b = q.get();
        return switch (x.algebra()) {
            case C -> Optional.of(b.plus(new Gaussian(Rational.ZERO, Rational.of(1, 1)).times(a)));
            case D -> Optional.of(b.minus(a));
            case S -> Optional.of(a.plus(b));
            case Q -> a.over(b);
            case P -> Optional.of(a.times(b));
        };
    }

    private static Rational rational(BigDecimal d) {
        return d.scale() <= 0
                ? new Rational(d.unscaledValue().multiply(BigInteger.TEN.pow(-d.scale())), BigInteger.ONE)
                : new Rational(d.unscaledValue(), BigInteger.TEN.pow(d.scale()));
    }

    /** Whether every pair in the value is a {@code C}: then the value loses nothing of it ({@code C_injective}). */
    @Lean("T.Unquotiented.C_injective")
    private static boolean allPoints(Object v) {
        return !(v instanceof Pair<?> x) || x.algebra() == TractionAlgebra.C && allPoints(x.p()) && allPoints(x.q());
    }

    private static boolean hasDouble(Object v) {
        return v instanceof Double || v instanceof Pair<?> x && (hasDouble(x.p()) || hasDouble(x.q()));
    }

    /** The value of a pair of doubles, by complex arithmetic in doubles; empty where it is not a number. */
    private static Optional<String> approximately(Object v) {
        double[] z = complex(v);
        if (Double.isNaN(z[0]) || Double.isNaN(z[1])) return Optional.empty();
        String re = IeeeLevel.display(z[0]);
        if (z[1] == 0) return Optional.of(re);
        String im = Math.abs(z[1]) == 1 ? "i" : IeeeLevel.display(Math.abs(z[1])) + "i";
        if (z[0] == 0) return Optional.of((z[1] < 0 ? "-" : "") + im);
        return Optional.of(re + (z[1] < 0 ? " - " : " + ") + im);
    }

    private static double[] complex(Object v) {
        if (v instanceof Pair<?> x) {
            double[] a = complex(x.p()), b = complex(x.q());
            return switch (x.algebra()) {
                case C -> new double[]{b[0] - a[1], b[1] + a[0]};
                case D -> new double[]{b[0] - a[0], b[1] - a[1]};
                case S -> new double[]{a[0] + b[0], a[1] + b[1]};
                case P -> new double[]{a[0] * b[0] - a[1] * b[1], a[0] * b[1] + a[1] * b[0]};
                case Q -> {
                    double n = b[0] * b[0] + b[1] * b[1];
                    yield new double[]{(a[0] * b[0] + a[1] * b[1]) / n, (a[1] * b[0] - a[0] * b[1]) / n};
                }
            };
        }
        if (v instanceof Double d) return new double[]{d, 0};
        if (v instanceof BigDecimal d) return new double[]{d.doubleValue(), 0};
        return new double[]{((BigInteger) v).doubleValue(), 0};
    }

    /** The direction as a fraction of a whole turn, in {@code (−1/2, 1/2]}, to ten places. */
    @Lean("T.Unquotiented.turn")
    private static String turn(Gaussian z) {
        double t = Math.atan2(decimal(z.im()), decimal(z.re())) / (2 * Math.PI);
        String s = new BigDecimal(t).setScale(10, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString();
        return s.equals("-0") ? "0" : s;
    }

    private static double decimal(Rational r) {
        return new BigDecimal(r.num()).divide(new BigDecimal(r.den()), MathContext.DECIMAL64).doubleValue();
    }

    // ---- lowest terms ----

    /** The value with each {@code Q} of two Integers as its ratio's representative. */
    @Lean("T.ratioRel_iff")
    private static String lowestTerms(Object v) {
        if (!(v instanceof Pair<?> x)) return write(v);
        if (x.algebra() == TractionAlgebra.Q && x.p() instanceof BigInteger p && x.q() instanceof BigInteger q) {
            T r = Quotient.RATIO.representative(new T(p, q));
            return "Q(" + r.p() + ", " + r.q() + ")";
        }
        return x.algebra() + "(" + lowestTerms(x.p()) + ", " + lowestTerms(x.q()) + ")";
    }

    private static String write(Object v) {
        if (v instanceof Pair<?> x) return x.algebra() + "(" + write(x.p()) + ", " + write(x.q()) + ")";
        if (v instanceof Double d) return IeeeLevel.display(d);
        if (v instanceof BigDecimal d) return d.toPlainString();
        return v.toString();
    }

    // ---- what cott-lean covers ----

    /**
     * The cott-lean file proving the operations of this nesting, where one does. A number beside a pair is that pair's
     * number on its slot ({@code T.ratioInt}, {@code T.complexInt}), so it counts as one. A double is not a real
     * number cott-lean speaks of.
     */
    @Lean({"T.Unquotiented.pairPlus", "T2.plus", "T2.otimes", "TOver.instAdd", "CC.otimes", "CTC.otimes",
            "T.ratioInt", "T.complexInt"})
    static Optional<String> covered(Object v) {
        if (hasDouble(v)) return Optional.empty();
        if (!(v instanceof Pair<?> x)) return Optional.of("a number");
        Object p = x.p(), q = x.q();
        if (!isPair(p) && !isPair(q)) return Optional.of("T/Unquotiented and T/PairAlgebras: " + x.algebra() + " of two numbers");
        if (x.algebra() == TractionAlgebra.Q && both(p, q, TractionAlgebra.Q)) return Optional.of("Nested/Basic: Q of Q, T2");
        if (x.algebra() == TractionAlgebra.C && both(p, q, TractionAlgebra.Q)) return Optional.of("Nested/Point: C of Q");
        if (x.algebra() == TractionAlgebra.Q && both(p, q, TractionAlgebra.C)) return Optional.of("Nested/RatioPoint: Q of C, TC");
        if (x.algebra() == TractionAlgebra.C && both(p, q, TractionAlgebra.C)) return Optional.of("Nested/Bicomplex: C of C");
        if (x.algebra() == TractionAlgebra.C && ratiosOfPoints(p) && ratiosOfPoints(q))
            return Optional.of("Nested/BicomplexRatio: C of Q of C");
        return Optional.empty();
    }

    private static boolean isPair(Object v) {
        return v instanceof Pair<?>;
    }

    /** Each of {@code p} and {@code q} a number or an {@code a} of two numbers. */
    private static boolean both(Object p, Object q, TractionAlgebra a) {
        return flatOrNumber(p, a) && flatOrNumber(q, a);
    }

    private static boolean flatOrNumber(Object v, TractionAlgebra a) {
        return !(v instanceof Pair<?> x) || x.algebra() == a && !isPair(x.p()) && !isPair(x.q());
    }

    private static boolean ratiosOfPoints(Object v) {
        return !(v instanceof Pair<?> x) || x.algebra() == TractionAlgebra.Q && both(x.p(), x.q(), TractionAlgebra.C);
    }
}
