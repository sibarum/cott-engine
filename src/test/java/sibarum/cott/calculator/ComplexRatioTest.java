package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComplexRatioTest {

    private static Result.ComplexRatioValue value(String line) {
        return assertInstanceOf(Result.ComplexRatioValue.class, new Calculator(Arithmetic.COMPLEX_RATIO).enter(line), line);
    }

    private static String text(String line) {
        return value(line).text();
    }

    private static String reads(String line, String reading) {
        return value(line).readings().get(reading);
    }

    @Test
    void anIntegerIsOverOneAndOmegaIsOneOverZero() {
        assertEquals("3/1", text("3"));
        assertEquals("-3/1", text("-3"));
        assertEquals("ω", text("ω"));
        assertEquals("ω", text("1/0"));
    }

    @Test
    void theGroundTruthIsUnreduced() {
        assertEquals("5/6", text("1/2 + 1/3"));
        assertEquals("2/-4", text("2/(-4)"));
        assertEquals("-1/2", reads("2/(-4)", "value"));
        assertEquals("6/6", text("(2/3)·(3/2)"));
        assertEquals("1", reads("(2/3)·(3/2)", "value"));
    }

    @Test
    void aPointAtInfinityKeepsItsNumerator() {
        assertEquals("3ω", text("3/0"));
        assertEquals("3ω", text("3ω"));
        assertEquals("-3ω", text("-3/0"));
        assertEquals("undefined", reads("3/0", "value"));
        assertEquals("0ω + 0ω·i", reads("3/0", "point"));
    }

    @Test
    void multiplyingByZeroLosesTheDirection() {
        assertEquals("0ω", text("(3/0)·0"));
        assertEquals("0ω", text("(1/0)·0"));
    }

    @Test
    void thePointReadingIsTheRationalizedPair() {
        assertEquals("30/36 + (0/36)·i", reads("1/2 + 1/3", "point"));
        assertEquals("5/6", reads("1/2 + 1/3", "value"));
    }

    @Test
    void thereIsNoZerothPowerOrDecimal() {
        assertEquals("8/1", text("2^3"));
        assertThrows(CalculatorException.class, () -> value("2^0"));
        assertThrows(CalculatorException.class, () -> value("0.5"));
    }
}
