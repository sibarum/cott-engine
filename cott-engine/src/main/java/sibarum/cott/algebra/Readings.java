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
 *   <li>{@code lowest terms}: each {@code Q} of two Integers in lowest terms, where one is not, inside a nested value;
 *       a flat {@code Q}'s is its {@code ratio}.</li>
 *   <li>Of a {@code Z}, {@code E} or {@code B}, which stand for no complex number: {@code Z}'s {@code value}
 *       {@code p·0^q} and its {@code log₀}, {@code E}'s {@code value} {@code q + log₀ p} and its {@code 0^E}, and
 *       {@code B}'s {@code value} {@code log_p q}.</li>
 *   <li>{@code cott-lean}: the file that proves this nesting's operations, or that none does.</li>
 * </ul>
 */
public final class Readings {

    private Readings() {}

    /** One reading: its name, as written, and its rung; a rung of {@code null} is a fact about the value, not a reading of it. */
    public record Reading(String name, String text, Rung rung) {}

    public static List<Reading> of(Object value) {
        return of(value, Cancellation.RESIDUAL);
    }

    /**
     * The readings of {@code value}, with its {@link Simplifier series} among them where that differs from it: like
     * terms combined as {@code cancellation} says, and its {@link Chebyshev} decomposition where it has one. A flat {@code Q} of two Integers has none, since reducing it is
     * what its projections already say, ray and ratio and classical apart.
     */
    public static List<Reading> of(Object value, Cancellation cancellation) {
        List<Reading> out = new ArrayList<>();
        if (!(value instanceof Pair<?> pair)) return out;
        boolean flatRatio = pair.algebra() == TractionAlgebra.Q && pair.p() instanceof BigInteger && pair.q() instanceof BigInteger;
        if (!flatRatio) Simplifier.simplify(value, cancellation).filter(s -> !s.equals(value)).ifPresent(s ->
                out.add(new Reading("simplified", Simplifier.written(value, cancellation).orElseThrow(), Rung.QUOTIENT)));
        if (!flatRatio) Chebyshev.of(value, cancellation).ifPresent(e ->
                out.add(new Reading("chebyshev", Chebyshev.written(e), Rung.QUOTIENT)));
        if (pair.algebra() == TractionAlgebra.L) {
            out.addAll(logPairReadings(pair));
        } else if (zeroScale(pair.algebra())) {
            if (!holdsZeroScale(pair.p()) && !holdsZeroScale(pair.q())) out.addAll(zeroScaleReadings(pair));
        } else if (!flatRatio && !holdsZeroScale(value)) {
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
        if (!flatRatio && pair.algebra() != TractionAlgebra.L && !lowest.equals(write(value))) out.add(new Reading("lowest terms", lowest, Rung.QUOTIENT));
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
            case Z, E, B, L -> throw new IllegalStateException(x.algebra() + " has no complex value");
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
        return complex(v).flatMap(Readings::approximately);
    }

    private static Optional<String> approximately(double[] z) {
        if (Double.isNaN(z[0]) || Double.isNaN(z[1])) return Optional.empty();
        String re = IeeeLevel.display(z[0]);
        if (z[1] == 0) return Optional.of(re);
        String im = Math.abs(z[1]) == 1 ? "i" : IeeeLevel.display(Math.abs(z[1])) + "i";
        if (z[0] == 0) return Optional.of((z[1] < 0 ? "-" : "") + im);
        return Optional.of(re + (z[1] < 0 ? " - " : " + ") + im);
    }

    /**
     * The complex number a value stands for, {@code {re, im}} in doubles, up to error: each algebra read as its meaning
     * all the way down, and {@code L(s, t)} as {@code e^(s/2)} turned {@code t}. Empty for a {@code Z}, {@code E} or
     * {@code B}, a zero magnitude turned, or a {@code Q} over {@code 0}, none of which is a complex number, and where
     * the doubles make a NaN. A double's infinity is kept.
     */
    public static Optional<double[]> complex(Object v) {
        double[] z = point(v);
        return z == null || Double.isNaN(z[0]) || Double.isNaN(z[1]) ? Optional.empty() : Optional.of(z);
    }

    /** {@link #complex}, or null where there is none. */
    private static double[] point(Object v) {
        if (v instanceof Pair<?> x) {
            if (x.algebra() == TractionAlgebra.L) return logPairPoint(x);
            if (zeroScale(x.algebra())) return null;
            double[] a = point(x.p()), b = point(x.q());
            if (a == null || b == null) return null;
            return switch (x.algebra()) {
                case C -> new double[]{b[0] - a[1], b[1] + a[0]};
                case D -> new double[]{b[0] - a[0], b[1] - a[1]};
                case S -> new double[]{a[0] + b[0], a[1] + b[1]};
                case P -> new double[]{a[0] * b[0] - a[1] * b[1], a[0] * b[1] + a[1] * b[0]};
                case Q -> {
                    double n = b[0] * b[0] + b[1] * b[1];
                    yield n == 0 ? null : new double[]{(a[0] * b[0] + a[1] * b[1]) / n, (a[1] * b[0] - a[0] * b[1]) / n};
                }
                case Z, E, B, L -> throw new AssertionError();
            };
        }
        if (v instanceof Double d) return new double[]{d, 0};
        if (v instanceof BigDecimal d) return new double[]{d.doubleValue(), 0};
        if (v instanceof BigInteger n) return new double[]{n.doubleValue(), 0};
        return null;
    }

    /** {@code L(s, t)} as {@code e^(s/2)} turned {@code t}, exactly at a whole quarter turn of magnitude {@code 1}. */
    @Lean("T.Unquotiented.L")
    private static double[] logPairPoint(Pair<?> x) {
        Optional<Rational> s = Logs.ratio(x.p()).flatMap(Logs.Ratio::value), t = Logs.ratio(x.q()).flatMap(Logs.Ratio::value);
        if (s.isEmpty() || t.isEmpty()) return null;
        Rational quarters = t.get().times(Rational.of(4, 1));
        if (s.get().num().signum() == 0 && quarters.den().equals(BigInteger.ONE)) {
            int k = quarters.num().mod(BigInteger.valueOf(4)).intValue();
            return new double[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}}[k];
        }
        double r = Math.exp(decimal(s.get()) / 2), a = 2 * Math.PI * decimal(t.get());
        return new double[]{r * Math.cos(a), r * Math.sin(a)};
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

    // ---- L ----

    /**
     * {@code L(s, t)}: the {@code magnitude} {@code √N = e^(s/2)}, its {@code turn}, the {@code value} it is the
     * logarithm of, {@code e^(s/2)} turned {@code t}, and {@code log_b} in each base held by its units. A value at a
     * whole number of quarter turns with magnitude {@code 1} is exact; a zero magnitude, scale {@code −1 : 0}, is
     * {@code 0} turned, which no complex number holds.
     */
    @Lean({"T.Unquotiented.L", "T.Unquotiented.logBase"})
    private static List<Reading> logPairReadings(Pair<?> x) {
        List<Reading> out = new ArrayList<>();
        Optional<Logs.Ratio> scale = Logs.ratio(x.p()), turn = Logs.ratio(x.q());
        if (scale.isEmpty() || turn.isEmpty()) return out;
        Logs.Ratio s = scale.get(), t = turn.get();
        Optional<Rational> sv = s.value(), tv = t.value();
        int infinite = sv.isPresent() ? 0 : s.a().num().signum();
        String magnitude = infinite < 0 ? "0" : infinite > 0 ? "ω" : sv.isEmpty() ? "undefined"
                : sv.get().num().signum() == 0 ? "1" : IeeeLevel.display(Math.exp(decimal(sv.get()) / 2));
        out.add(new Reading("magnitude", magnitude, sv.isEmpty() && infinite == 0 ? Rung.ERROR
                : sv.isPresent() && sv.get().num().signum() != 0 ? Rung.UP_TO_ERROR : Rung.QUOTIENT));
        String turnText = tv.map(Rational::toString).orElse(t.toString());
        out.add(new Reading("turn", turnText, Rung.QUOTIENT));
        if (tv.isEmpty() || sv.isEmpty() && infinite == 0) {
            out.add(new Reading("value", "undefined", Rung.ERROR));
        } else if (infinite != 0) {
            out.add(new Reading("value", magnitude + ", turned " + turnText, Rung.QUOTIENT));
        } else {
            Rational quarters = tv.get().times(Rational.of(4, 1));
            if (sv.get().num().signum() == 0 && quarters.den().equals(BigInteger.ONE)) {
                String[] corners = {"1", "i", "-1", "-i"};
                out.add(new Reading("value", corners[quarters.num().mod(BigInteger.valueOf(4)).intValue()], Rung.QUOTIENT));
            } else {
                double r = Math.exp(decimal(sv.get()) / 2), a = 2 * Math.PI * decimal(tv.get());
                approximately(new double[]{r * Math.cos(a), r * Math.sin(a)})
                        .ifPresent(z -> out.add(new Reading("value", z, Rung.UP_TO_ERROR)));
            }
        }
        for (Base b : List.of(Base.ONE, Base.NEG_ONE, Base.I, Base.ZERO)) {
            Logs.Units in = Logs.logBase(Logs.units(b).orElseThrow(), s, t);
            out.add(new Reading("log_" + b.written(), "scale " + in.scale() + ", turn " + in.turn(), Rung.QUOTIENT));
        }
        return out;
    }

    // ---- Z, E and B ----

    private static boolean zeroScale(TractionAlgebra a) {
        return a == TractionAlgebra.Z || a == TractionAlgebra.E || a == TractionAlgebra.B;
    }

    private static boolean holdsZeroScale(Object v) {
        return v instanceof Pair<?> x && (zeroScale(x.algebra()) || x.algebra() == TractionAlgebra.L
                || holdsZeroScale(x.p()) || holdsZeroScale(x.q()));
    }

    /** A coordinate's value: exact where it holds no double, and a double's otherwise. */
    private record Coordinate(Gaussian exact, double[] approx) {

        static Optional<Coordinate> of(Object v) {
            if (hasDouble(v)) return complex(v).map(z -> new Coordinate(null, z));
            return exactly(v).map(g -> new Coordinate(g, null));
        }

        boolean real() {
            return exact != null ? exact.im().num().signum() == 0 : approx[1] == 0;
        }

        /** The sign of a real value. */
        int sign() {
            return exact != null ? exact.re().num().signum() : (int) Math.signum(approx[0]);
        }

        boolean isOne() {
            return exact != null ? exact.equals(Gaussian.real(Rational.of(1, 1))) : approx[0] == 1 && approx[1] == 0;
        }

        Coordinate plusOne() {
            return exact != null ? new Coordinate(exact.plus(Gaussian.real(Rational.of(1, 1))), null)
                    : new Coordinate(null, new double[]{approx[0] + 1, approx[1]});
        }

        /** The natural logarithm of a real value's magnitude. */
        double lnAbs() {
            if (exact == null) return Math.log(Math.abs(approx[0]));
            return Readings.lnAbs(exact.re().num()) - Readings.lnAbs(exact.re().den());
        }

        @Override
        public String toString() {
            return exact != null ? exact.toString() : approximately(approx).orElseThrow();
        }
    }

    /**
     * {@code Z}: its value {@code p·0^q} in {@code ℤ[0, 0⁻¹]}, and its logarithm in base {@code 0}, {@code E} with the
     * same coordinates. {@code E}: its value {@code q + log₀ p} and its power of zero, {@code Z} with the same
     * coordinates. {@code B}: {@code log_p q}. A coordinate with no value gives no reading.
     */
    private static List<Reading> zeroScaleReadings(Pair<?> x) {
        Optional<Coordinate> p = Coordinate.of(x.p()), q = Coordinate.of(x.q());
        if (p.isEmpty() || q.isEmpty()) return List.of();
        boolean approximate = hasDouble(x);
        Rung monomial = approximate ? Rung.UP_TO_ERROR : faithful(x) ? Rung.EXACT : Rung.QUOTIENT;
        Rung logarithm = approximate ? Rung.UP_TO_ERROR : keptByLog(x) ? Rung.EXACT : Rung.QUOTIENT;
        List<Reading> out = new ArrayList<>();
        switch (x.algebra()) {
            case Z -> {
                out.add(new Reading("value", monomial(p.get(), q.get()), monomial));
                logZero(p.get(), q.get()).ifPresent(e -> out.add(new Reading("log₀", e, logarithm)));
            }
            case E -> {
                logZero(p.get(), q.get()).ifPresent(e -> out.add(new Reading("value", e, logarithm)));
                out.add(new Reading("0^E", monomial(p.get(), q.get()), monomial));
            }
            case B -> logarithm(p.get(), q.get()).ifPresent(out::add);
            default -> throw new IllegalStateException(x.algebra() + " is not read here");
        }
        return out;
    }

    /** {@code Z} of a non-zero number at a whole order is held faithfully by its monomial. */
    @Lean("T.ZeroScale.toLaurent_injective")
    private static boolean faithful(Pair<?> x) {
        return !isPair(x.p()) && !isPair(x.q()) && Coordinate.of(x.p()).filter(c -> c.real() && c.sign() != 0).isPresent();
    }

    /** {@code E} keeps both the coefficient and the order where the coefficient is a positive number. */
    @Lean("T.ZeroScale.E_injective")
    private static boolean keptByLog(Pair<?> x) {
        return !isPair(x.p()) && !isPair(x.q()) && Coordinate.of(x.p()).filter(c -> c.real() && c.sign() > 0).isPresent();
    }

    /** {@code 3·0^2}, {@code 0}, {@code -0}, {@code ω}, {@code (1/2)·ω^3}: {@code 0 = Z(1, 1)} and {@code ω = Z(1, −1)}. */
    @Lean({"T.ZeroScale.toLaurent", "T.ZeroScale.Z.zero_pow", "T.ZeroScale.Z.omega_pow", "T.ZeroScale.Z.negZero"})
    static String monomial(Coordinate coefficient, Coordinate order) {
        String c = coefficient.toString(), n = order.toString();
        if (n.equals("0")) return c;
        String power = n.equals("1") ? "0" : n.equals("-1") ? "ω"
                : order.real() && order.sign() < 0 ? "ω^" + group(n.substring(1)) : "0^" + group(n);
        if (c.equals("1")) return power;
        if (c.equals("-1")) return "-" + power;
        return group(c) + "·" + power;
    }

    /**
     * {@code q + log₀ p} as {@code q - 0·ln p}, since {@code log₀ p = −0·ln p}; {@code log₀ 1 = 0}, and
     * {@code log₀ 0 = 1}. The logarithm is the real one, of {@code |p|}. A coefficient off the real line has none.
     */
    @Lean({"T.ZeroScale.E_eq", "T.ZeroScale.logZero_zero", "T.ZeroScale.E_zero"})
    private static Optional<String> logZero(Coordinate p, Coordinate q) {
        if (!p.real()) return Optional.empty();
        if (p.sign() == 0) return Optional.of(q.plusOne().toString());
        if (p.isOne()) return Optional.of(q.toString());
        String argument = p.sign() < 0 ? "|" + p + "|" : group(p.toString());
        String ln = "0·ln" + (argument.startsWith("(") || argument.startsWith("|") ? argument : " " + argument);
        String n = q.toString();
        return Optional.of(n.equals("0") ? "-" + ln : n + " - " + ln);
    }

    /**
     * {@code log_p q} for real {@code p} and {@code q} above {@code 0}, {@code p ≠ 1}: a rational where {@code q} is a
     * rational power of {@code p}, and a double otherwise. Many pairs give one value, as {@code B(2, 8)} and
     * {@code B(4, 64)} give {@code 3}.
     */
    @Lean({"T.ZeroScale.B", "T.ZeroScale.E_eq_B_mul"})
    private static Optional<Reading> logarithm(Coordinate p, Coordinate q) {
        if (!p.real() || !q.real() || p.sign() <= 0 || q.sign() <= 0 || p.isOne()) return Optional.empty();
        if (p.exact() != null && q.exact() != null) {
            Optional<Rational> r = rationalPower(p.exact().re(), q.exact().re());
            if (r.isPresent()) return Optional.of(new Reading("value", r.get().toString(), Rung.QUOTIENT));
        }
        return Optional.of(new Reading("value", IeeeLevel.display(q.lnAbs() / p.lnAbs()), Rung.UP_TO_ERROR));
    }

    /** The {@code a/b} with {@code p^a = q^b}, for {@code b} up to 64 and powers of moderate size, or empty. */
    private static Optional<Rational> rationalPower(Rational p, Rational q) {
        double estimate = (lnAbs(q.num()) - lnAbs(q.den())) / (lnAbs(p.num()) - lnAbs(p.den()));
        long bits = Math.max(p.num().bitLength(), p.den().bitLength()) + Math.max(q.num().bitLength(), q.den().bitLength());
        for (int b = 1; b <= 64; b++) {
            long a = Math.round(estimate * b);
            if (Math.abs(a) * bits > 1 << 16 || (long) b * bits > 1 << 16) break;
            if (power(p, a).equals(power(q, b))) return Optional.of(Rational.of(a, b));
        }
        return Optional.empty();
    }

    private static Rational power(Rational r, long n) {
        Rational base = n < 0 ? r.inverse() : r;
        int k = (int) Math.abs(n);
        return new Rational(base.num().pow(k), base.den().pow(k));
    }

    /** {@code ln |n|} for an integer of any size. */
    private static double lnAbs(BigInteger n) {
        int shift = Math.max(0, n.abs().bitLength() - 1000);
        return Math.log(n.abs().shiftRight(shift).doubleValue()) + shift * Math.log(2);
    }

    /** A number as it stands, or in parentheses where it is more than one. */
    private static String group(String s) {
        return s.matches("-?[0-9]+(\\.[0-9]+)?") ? s : "(" + s + ")";
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
            "T.ratioInt", "T.complexInt", "T.ZeroScale.Z", "T.ZeroScale.E", "T.ZeroScale.B", "T.ZeroScale.Residual.R"})
    static Optional<String> covered(Object v) {
        if (hasDouble(v)) return Optional.empty();
        if (!(v instanceof Pair<?> x)) return Optional.of("a number");
        Object p = x.p(), q = x.q();
        if (x.algebra() == TractionAlgebra.Z && !isPair(q) && flatOrNumber(p, TractionAlgebra.Q))
            return Optional.of(isPair(p) ? "T/Residual: Z of Q" : "T/ZeroAlgebra and T/Residual: Z of two numbers");
        if ((x.algebra() == TractionAlgebra.E || x.algebra() == TractionAlgebra.B) && !isPair(p) && !isPair(q))
            return Optional.of("T/ZeroAlgebra: " + x.algebra() + " of two numbers");
        if (x.algebra() == TractionAlgebra.L)
            return Logs.ratio(p).isPresent() && Logs.ratio(q).isPresent()
                    ? Optional.of("T/LogPair and T/Bases: L of two ratios") : Optional.empty();
        if (zeroScale(x.algebra())) return Optional.empty();
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
