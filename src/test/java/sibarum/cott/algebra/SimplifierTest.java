package sibarum.cott.algebra;

import org.junit.jupiter.api.Test;
import sibarum.cott.notation.Parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A value as a series in {@code 0}: flattened, reduced, with like terms combined. */
class SimplifierTest {

    private static Object value(NumberType type, Cancellation cancellation, String line) {
        return Evaluator.evaluate(new Parser().expression(line), type, SizeLimit.MEDIUM, Form.SUM_OF_PRODUCTS,
                cancellation).value();
    }

    private static String written(String line) {
        return Simplifier.written(value(NumberType.INTEGER, Cancellation.RESIDUAL, line), Cancellation.RESIDUAL)
                .orElse("none");
    }

    private static String built(NumberType type, String line) {
        Object v = Simplifier.simplify(value(type, Cancellation.RESIDUAL, line), Cancellation.RESIDUAL).orElseThrow();
        return new Evaluator(type, SizeLimit.MEDIUM, Form.SUM_OF_PRODUCTS, Cancellation.RESIDUAL, null).write(v);
    }

    @Test
    void likeTermsCombine() {
        assertEquals("ω^2 + 4·ω + 6 + 4·0 + 0^2", written("(2 + 0 + 1/0)^2"));
        assertEquals("ω^2 + 6·ω + 2 + 6·0 + 0^2", written("(3 + 0 + 1/0)^2 - 9"));
    }

    @Test
    void theSeriesIsAValueFromTheHighestPowerOfOmegaDown() {
        assertEquals("S(Z(1, -2), S(Z(4, -1), S(6, S(Z(4, 1), Z(1, 2)))))", built(NumberType.INTEGER, "(2 + 0 + 1/0)^2"));
    }

    @Test
    void quotientsAreReduced() {
        assertEquals("2", written("6/3"));
        assertEquals("ω", written("1/0"));
        assertEquals("-(1/2)·ω", written("(-1/2)/0"));
        assertEquals("(3/2)·ω^2", written("(3/2)/0^2"));
    }

    @Test
    void aDenominatorOfSeveralOrdersHasNoSeries() {
        assertEquals("none", written("1/(1 + 0)"));
    }

    @Test
    void cancellingTermsFollowTheMode() {
        Object v = new Pair<>(TractionAlgebra.S, new Pair<>(TractionAlgebra.Z, java.math.BigInteger.valueOf(3),
                java.math.BigInteger.ONE), new Pair<>(TractionAlgebra.S, java.math.BigInteger.ONE,
                new Pair<>(TractionAlgebra.Z, java.math.BigInteger.valueOf(-3), java.math.BigInteger.ONE)));
        assertEquals("1 + 3·0^2", Simplifier.written(v, Cancellation.RESIDUAL).orElseThrow(), "3·0 − 3·0 = 3·0^2");
        assertEquals("1", Simplifier.written(v, Cancellation.ABSORB).orElseThrow());
        Object plain = new Pair<>(TractionAlgebra.S, java.math.BigInteger.valueOf(3), java.math.BigInteger.valueOf(-3));
        assertEquals("0", Simplifier.written(plain, Cancellation.RESIDUAL).orElseThrow(), "1 − 1 is still 0");
    }

    @Test
    void whatItDoesNotReadHasNoSeries() {
        assertEquals("none", written("4 + 3i"));
        assertEquals("none", written("1/(3 - 3)"), "a zero denominator");
        assertEquals("none", written("E(2, 1)"));
    }

    @Test
    void decimalsStayDecimals() {
        assertEquals("S(0.25, S(Z(1, 1), Z(1, 2)))", built(NumberType.DECIMAL, "(0.5 + 0)^2"));
    }

    @Test
    void aReadingNamesTheSeriesWhereItDiffers() {
        assertEquals("ω^2 + 4·ω + 6 + 4·0 + 0^2", Readings.of(value(NumberType.INTEGER, Cancellation.RESIDUAL,
                "(2 + 0 + 1/0)^2")).stream().filter(r -> r.name().equals("simplified")).findFirst().orElseThrow().text());
        assertEquals(0, Readings.of(value(NumberType.INTEGER, Cancellation.RESIDUAL, "Q(1, 3)")).stream()
                .filter(r -> r.name().equals("simplified")).count(), "Q(1, 3) is already its series");
    }
}
