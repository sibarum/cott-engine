package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TractionPointTest {

    private static Result.PointValue value(String line) {
        return assertInstanceOf(Result.PointValue.class, new Calculator(Arithmetic.TRACTION_POINT).enter(line), line);
    }

    private static String text(String line) {
        return value(line).text();
    }

    private static String reads(String line) {
        return value(line).readings().get("value");
    }

    @Test
    void integersAreRealPointsAndOmegaIsI() {
        assertEquals("3 + 0·i", text("3"));
        assertEquals("0 + 1·i", text("ω"));
        assertEquals("i", reads("ω"));
        assertEquals("-1 + 0·i", text("ω·ω"));
        assertEquals("-1", reads("ω^2"));
        assertEquals("1", reads("ω^4"));
    }

    @Test
    void divisionIsTheExactComplexInverse() {
        assertEquals("-i", reads("1/ω"));
        assertEquals("1/10 + (7/10)i", reads("(1 + 2ω)/(3 - ω)"));
        assertEquals("1/2", reads("1/2"));
        assertEquals("1", reads("(2 + 3ω)/(2 + 3ω)"));
        assertEquals("13", value("2 + 3ω").readings().get("length²"));
    }

    @Test
    void theGroundTruthIsUnreduced() {
        assertEquals("8/16 + (0/16)·i", text("1/2"));
        assertEquals("169/169 + (0/169)·i", text("(2 + 3ω)/(2 + 3ω)"));
        assertEquals("0 + 0·i", text("ω - ω"));
    }

    @Test
    void atZeroTheInverseCollapses() {
        assertEquals("0ω + 0ω·i", text("1/0"));
        assertEquals("undefined", reads("1/0"));
        assertEquals("0ω + 0ω·i", text("0/0"));
    }

    @Test
    void subtractionIsAddingMinusOneTimes() {
        assertEquals("0", reads("ω - ω"));
        assertEquals("-3", reads("-3"));
        assertEquals("2 - i", reads("2 - ω"));
    }

    @Test
    void thereIsNoZerothPowerOrDecimal() {
        assertThrows(CalculatorException.class, () -> value("ω^0"));
        assertThrows(CalculatorException.class, () -> value("0.5"));
    }
}
