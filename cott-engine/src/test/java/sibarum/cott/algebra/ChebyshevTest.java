package sibarum.cott.algebra;

import org.junit.jupiter.api.Test;
import sibarum.cott.notation.Parser;
import sibarum.cott.projection.Rational;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A series in {@code 0} in the Chebyshev ring {@code ℚ[s][g] / (g² − s·g + 1)}, {@code g = 0^(1/2)}. The expected
 * elements are what cott/solver's {@code chebyshev_ring.py} gives for the same series.
 */
class ChebyshevTest {

    private static Object value(String line) {
        return Evaluator.evaluate(new Parser().expression(line), NumberType.INTEGER, SizeLimit.MEDIUM,
                Form.SUM_OF_PRODUCTS, Cancellation.RESIDUAL).value();
    }

    private static String decomposed(String line) {
        return Chebyshev.of(value(line), Cancellation.RESIDUAL).map(Chebyshev.Element::toString).orElse("none");
    }

    private static TreeMap<Integer, Rational> series(Object... orderThenCoefficient) {
        TreeMap<Integer, Rational> out = new TreeMap<>();
        for (int i = 0; i < orderThenCoefficient.length; i += 2)
            out.put((Integer) orderThenCoefficient[i], (Rational) orderThenCoefficient[i + 1]);
        return out;
    }

    private static String decomposed(TreeMap<Integer, Rational> series) {
        return Chebyshev.of(series).map(Chebyshev.Element::toString).orElse("none");
    }

    @Test
    void zeroAndOmegaAreTheGeneratorSquaredAndItsInverse() {
        assertEquals("-1 + s·g", decomposed("0"), "0 = g² = s·g − 1");
        assertEquals("s^2 - 1 - s·g", decomposed("1/0"), "ω = g⁻² = (s − g)²");
        assertEquals("-s^2 + 1 + (s^3 - 2·s)·g", decomposed("0^2"));
    }

    @Test
    void aSeriesSymmetricInZeroAndOmegaIsAPolynomialInS() {
        assertEquals("s^2 - 2", decomposed("0 + 1/0"));
        assertEquals("s^4", decomposed("(2 + 0 + 1/0)^2"), "the binomial row of (g + 1/g)^4");
        assertEquals("s^4 + 2·s^2 - 8", decomposed("(3 + 0 + 1/0)^2 - 9"));
    }

    @Test
    void aLopsidedSeriesKeepsAG() {
        assertEquals("-s^2 + 4 + (s^3 + 4·s)·g", decomposed("(0 + 3)^2"));
        assertEquals("-(1/2)·s^6 + 2·s^4 - (3/2)·s^2 + (s^5 - 4·s^3 + 3·s)·g",
                decomposed(series(3, Rational.of(1, 2), -3, Rational.of(-1, 2))));
    }

    @Test
    void aPlainNumberOrAValueWithNoSeriesHasNone() {
        assertEquals("none", decomposed("6/3"));
        assertEquals("none", decomposed("4 + 3i"));
        assertEquals("none", decomposed("1/(1 + 0)"));
    }

    @Test
    void pastTheHighestOrderItIsLeftOut() {
        Rational one = Rational.of(1, 1);
        assertEquals(17, decomposed(series(Chebyshev.MAX_ORDER, one, -Chebyshev.MAX_ORDER, one)).split(" [+-] ").length,
                "0^16 + ω^16 is a polynomial of 17 terms in s");
        assertEquals("none", decomposed(series(Chebyshev.MAX_ORDER + 1, one)));
        assertEquals("none", decomposed(series(-Chebyshev.MAX_ORDER - 1, one)));
    }

    @Test
    void itReadsBackToTheSameSeries() {
        Random random = new Random(7);
        for (int trial = 0; trial < 200; trial++) {
            TreeMap<Integer, Rational> series = new TreeMap<>();
            for (int i = 0; i < 1 + random.nextInt(5); i++)
                series.put(random.nextInt(-6, 7), Rational.of(random.nextInt(-9, 10), random.nextInt(1, 5)));
            series.values().removeIf(c -> c.num().signum() == 0);
            Optional<Chebyshev.Element> e = Chebyshev.of(series);
            if (e.isEmpty()) {
                assertEquals(true, series.isEmpty() || series.keySet().equals(java.util.Set.of(0)), series::toString);
                continue;
            }
            assertEquals(Optional.of(series), Chebyshev.series(e.get()), series::toString);
        }
    }

    @Test
    void aReadingNamesTheDecompositionAndWhatSAndGAre() {
        Map<String, String> readings = new java.util.HashMap<>();
        Readings.of(value("(2 + 0 + 1/0)^2")).forEach(r -> readings.put(r.name(), r.text()));
        assertEquals("s^4 with s = 0^(1/2) + ω^(1/2)", readings.get("chebyshev"));
        readings.clear();
        Readings.of(value("(0 + 3)^2")).forEach(r -> readings.put(r.name(), r.text()));
        assertEquals("-s^2 + 4 + (s^3 + 4·s)·g with g = 0^(1/2), s = g + 1/g", readings.get("chebyshev"));
    }
}
