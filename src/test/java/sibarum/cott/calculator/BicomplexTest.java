package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BicomplexTest {

    private static Result.BicomplexValue value(String line) {
        return assertInstanceOf(Result.BicomplexValue.class, new Calculator(Arithmetic.BICOMPLEX).enter(line), line);
    }

    @Test
    void anIntegerIsOnTheOuterUnit() {
        assertEquals("3 + 0·j", value("3").text());
        assertEquals("7 + 0·j", value("2·3 + 1").text());
        assertEquals("-2 + 0·j", value("-2").text());
        assertEquals("8 + 0·j", value("2^3").text());
    }

    @Test
    void theReadingsAreTheNormAndTheTwoEvaluations() {
        var r = value("-2").readings();
        assertEquals("4", r.get("norm"));
        assertEquals("-2", r.get("j → i"));
        assertEquals("-2", r.get("j → −i"));
    }

    @Test
    void thereIsNoDivisionAndNoOmega() {
        assertThrows(CalculatorException.class, () -> value("1/2"));
        assertThrows(CalculatorException.class, () -> value("ω"));
        assertThrows(CalculatorException.class, () -> value("2^0"));
    }
}
