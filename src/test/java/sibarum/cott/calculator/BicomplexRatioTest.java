package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BicomplexRatioTest {

    private static Result.BicomplexRatioValue value(String line) {
        return assertInstanceOf(Result.BicomplexRatioValue.class,
                new Calculator(Arithmetic.BICOMPLEX_RATIO).enter(line), line);
    }

    private static String reads(String line, String reading) {
        return value(line).readings().get(reading);
    }

    @Test
    void anIntegerIsABicomplexIntegerOverOne() {
        assertEquals("3/1 + (0/1)·j", value("3").text());
        assertEquals("3", reads("3", "value"));
        assertEquals("9/1", reads("3", "norm"));
        assertEquals("yes", reads("3", "unit"));
    }

    @Test
    void divisionIsTheExactInverseAndAgreesWithPoint() {
        assertEquals("8/16 + (0/16)·j", value("1/2").text());
        assertEquals(new Calculator(Arithmetic.TRACTION_POINT).enter("1/2").text().replace("·i", "·j"),
                value("1/2").text());
        assertEquals("1/2", reads("1/2", "value"));
        assertEquals("3/2", reads("(1+2)/(3-1)", "value"));
    }

    @Test
    void atZeroTheInverseCollapses() {
        assertEquals("0ω + 0ω·j", value("1/0").text());
        assertEquals("0ω + 0ω·j", value("ω").text());
        assertEquals("undefined", reads("1/0", "value"));
        assertEquals("undefined", reads("1/0", "unit"));
    }

    @Test
    void thereIsNoZerothPowerOrDecimal() {
        assertThrows(CalculatorException.class, () -> value("2^0"));
        assertThrows(CalculatorException.class, () -> value("0.5"));
    }
}
