package sibarum.cott.algebra;

import sibarum.cott.projection.Rational;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A value's {@link Simplifier series} in {@code 0} written in the Chebyshev ring {@code ℚ[s][g] / (g² − s·g + 1)},
 * where {@code g = 0^(1/2)} and {@code s = g + g⁻¹ = 0^(1/2) + ω^(1/2)}. Every element is {@code a + b·g}, with
 * {@code a} and {@code b} polynomials in {@code s}, and {@code g² = s·g − 1} is the only rule.
 *
 * <p>A power of {@code 0} and the same power of {@code ω} add up to a Chebyshev polynomial in {@code s}:
 * {@code 0 + ω = s^2 − 2}. So a series that is symmetric in them has no {@code g} at all, and
 * {@code (2 + 0 + 1/0)^2} is {@code s^4}: its series {@code ω^2 + 4·ω + 6 + 4·0 + 0^2} is the binomial row of
 * {@code (g + g⁻¹)^4}. A series that is not symmetric keeps a {@code g}: {@code 0} is {@code −1 + s·g}.
 *
 * <p>It is the ring of cott/solver's {@code chebyshev_ring.py}, on its rational band with {@code g = 0^(1/2)}. It
 * rewrites the series exactly, approximating nothing, and reads back to the same series ({@link #series}), so it is
 * on the series' own {@link Rung#QUOTIENT quotient} rung. A value with no series has no decomposition, and neither
 * has a plain number, which is its own. Past {@link #MAX_ORDER} orders of {@code 0} it is left out, since its
 * polynomials grow with the order and would bury the series they rewrite.
 */
public final class Chebyshev {

    /** The highest power of {@code 0} or {@code ω} a decomposed series may have. */
    public static final int MAX_ORDER = 16;

    private Chebyshev() {
    }

    private static final Rational ONE = Rational.of(1, 1);

    /** A polynomial in {@code s} with rational coefficients, lowest power first, with no trailing {@code 0}. */
    public record Poly(List<Rational> coefficients) {

        static final Poly ZERO = new Poly(List.of());
        static final Poly ONE = new Poly(List.of(Chebyshev.ONE));
        static final Poly S = new Poly(List.of(Rational.ZERO, Chebyshev.ONE));

        public Poly {
            List<Rational> c = new ArrayList<>(coefficients);
            while (!c.isEmpty() && c.getLast().num().signum() == 0) c.removeLast();
            coefficients = List.copyOf(c);
        }

        public boolean isZero() {
            return coefficients.isEmpty();
        }

        Rational at(int i) {
            return i < coefficients.size() ? coefficients.get(i) : Rational.ZERO;
        }

        Poly plus(Poly y) {
            List<Rational> out = new ArrayList<>();
            for (int i = 0; i < Math.max(coefficients.size(), y.coefficients.size()); i++) out.add(at(i).plus(y.at(i)));
            return new Poly(out);
        }

        Poly neg() {
            return new Poly(coefficients.stream().map(Rational::neg).toList());
        }

        Poly times(Rational c) {
            return new Poly(coefficients.stream().map(x -> x.times(c)).toList());
        }

        Poly times(Poly y) {
            if (isZero() || y.isZero()) return ZERO;
            List<Rational> out = new ArrayList<>();
            for (int i = 0; i < coefficients.size() + y.coefficients.size() - 1; i++) out.add(Rational.ZERO);
            for (int i = 0; i < coefficients.size(); i++)
                for (int j = 0; j < y.coefficients.size(); j++)
                    out.set(i + j, out.get(i + j).plus(coefficients.get(i).times(y.coefficients.get(j))));
            return new Poly(out);
        }

        /** {@code s^4}, {@code s^3 - 2·s}, {@code (1/2)·s + 1}: from the highest power down. */
        @Override
        public String toString() {
            if (isZero()) return "0";
            StringBuilder out = new StringBuilder();
            for (int i = coefficients.size() - 1; i >= 0; i--) {
                Rational c = coefficients.get(i);
                if (c.num().signum() == 0) continue;
                boolean negative = c.num().signum() < 0;
                if (out.isEmpty()) {
                    if (negative) out.append('-');
                } else {
                    out.append(negative ? " - " : " + ");
                }
                Rational size = negative ? c.neg() : c;
                if (i == 0) {
                    out.append(size);
                } else {
                    String power = i == 1 ? "s" : "s^" + i;
                    if (!size.equals(Chebyshev.ONE))
                        out.append(size.den().equals(BigInteger.ONE) ? size : "(" + size + ")").append('·');
                    out.append(power);
                }
            }
            return out.toString();
        }

        /** One term only, so it needs no brackets before {@code ·g}. */
        boolean monomial() {
            return coefficients.stream().filter(c -> c.num().signum() != 0).count() == 1;
        }
    }

    /** An element {@code a + b·g} of the ring. */
    public record Element(Poly a, Poly b) {

        static final Element ZERO = new Element(Poly.ZERO, Poly.ZERO);

        Element plus(Element y) {
            return new Element(a.plus(y.a), b.plus(y.b));
        }

        Element times(Rational c) {
            return new Element(a.times(c), b.times(c));
        }

        /** {@code (a + b·g)·g = −b + (a + b·s)·g}. */
        Element timesG() {
            return new Element(b.neg(), a.plus(b.times(Poly.S)));
        }

        /** {@code (a + b·g)·g⁻¹ = (a + b·g)·(s − g) = (a·s + b) − a·g}. */
        Element timesGInverse() {
            return new Element(a.times(Poly.S).plus(b), a.neg());
        }

        /** No {@code g}: a polynomial in {@code s} alone, as a series symmetric in {@code 0} and {@code ω} is. */
        public boolean scalar() {
            return b.isZero();
        }

        /** {@code s^4}, {@code -1 + s·g}, {@code -s^2 + 1 + (s^3 - 2·s)·g}. */
        @Override
        public String toString() {
            if (b.isZero()) return a.toString();
            String g = b.equals(Poly.ONE) ? "g" : b.equals(Poly.ONE.neg()) ? "-g"
                    : b.monomial() ? b + "·g" : "(" + b + ")·g";
            if (a.isZero()) return g;
            return a + (g.startsWith("-") ? " - " + g.substring(1) : " + " + g);
        }
    }

    /**
     * The decomposition of {@code value}'s series, like terms combined as {@code cancellation} says. Empty where it
     * has no series, where the series is a plain number, and past {@link #MAX_ORDER}.
     */
    public static Optional<Element> of(Object value, Cancellation cancellation) {
        return Simplifier.series(value, cancellation).flatMap(Chebyshev::of);
    }

    /** {@code Σ cₙ·0ⁿ} as {@code Σ cₙ·g^(2n)}, or empty as {@link #of(Object, Cancellation)} says. */
    static Optional<Element> of(TreeMap<Integer, Rational> series) {
        if (series.isEmpty() || series.firstKey() < -MAX_ORDER || series.lastKey() > MAX_ORDER) return Optional.empty();
        if (series.firstKey() == 0 && series.lastKey() == 0) return Optional.empty();
        Element out = Element.ZERO;
        // g^k and g^-k, built a step at a time: each step is one multiplication by g or by g⁻¹ = s − g.
        Element up = new Element(Poly.ONE, Poly.ZERO);
        Element down = up;
        for (int k = 0; k <= 2 * Math.max(series.lastKey(), -series.firstKey()); k++) {
            if (k % 2 == 0) {
                Rational above = series.get(k / 2);
                if (above != null) out = out.plus(up.times(above));
                Rational below = k == 0 ? null : series.get(-k / 2);
                if (below != null) out = out.plus(down.times(below));
            }
            up = up.timesG();
            down = down.timesGInverse();
        }
        return Optional.of(out);
    }

    /**
     * The element read back as a series in {@code 0}, with {@code s = g + g⁻¹} multiplied out: each order with its
     * coefficient, none of them {@code 0}. Empty where an odd power of {@code g} is left, a half order of {@code 0}.
     */
    public static Optional<TreeMap<Integer, Rational>> series(Element e) {
        TreeMap<Integer, Rational> powers = new TreeMap<>();
        laurent(e.a(), 0, powers);
        laurent(e.b(), 1, powers);
        powers.values().removeIf(c -> c.num().signum() == 0);
        TreeMap<Integer, Rational> out = new TreeMap<>();
        for (Map.Entry<Integer, Rational> p : powers.entrySet()) {
            if (p.getKey() % 2 != 0) return Optional.empty();
            out.put(p.getKey() / 2, p.getValue());
        }
        return Optional.of(out);
    }

    /** {@code poly(g + g⁻¹)·g^shift} added into {@code powers} of {@code g}, by the binomial theorem. */
    private static void laurent(Poly poly, int shift, TreeMap<Integer, Rational> powers) {
        for (int k = 0; k < poly.coefficients().size(); k++) {
            Rational c = poly.coefficients().get(k);
            BigInteger binomial = BigInteger.ONE;
            for (int j = 0; j <= k; j++) {
                powers.merge(k - 2 * j + shift, c.times(new Rational(binomial, BigInteger.ONE)), Rational::plus);
                binomial = binomial.multiply(BigInteger.valueOf(k - j)).divide(BigInteger.valueOf(j + 1));
            }
        }
    }

    /** The decomposition as a reading writes it, saying what {@code s} and {@code g} are. */
    public static String written(Element e) {
        return e + (e.scalar() ? " with s = 0^(1/2) + ω^(1/2)" : " with g = 0^(1/2), s = g + 1/g");
    }
}
