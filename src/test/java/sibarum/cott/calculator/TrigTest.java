package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;
import sibarum.cott.traction.RationalTrig;
import sibarum.cott.traction.T;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrigTest {

    private static Result.RatioValue value(Calculator c, String line) {
        return (Result.RatioValue) c.enter(line);
    }

    @Test
    void cosAndSinTakeATurnAndAnswerWithTheUnreducedPair() {
        Calculator c = new Calculator();
        Result.RatioValue cos = value(c, "cos(1/6)");
        T spin = RationalTrig.dialed(BigInteger.ONE, 6, Trig.MAX_DEPTH, Trig.WIDTH).spin();
        assertEquals(new T(spin.q().pow(2).subtract(spin.p().pow(2)), spin.norm()), cos.flat());
        assertEquals(0.5, cos.flat().p().doubleValue() / cos.flat().q().doubleValue(), 1e-9);
        assertTrue(cos.readings().get("cos(1/6)").startsWith("depth "));

        Result.RatioValue sin = value(c, "sin(1/6)");
        assertEquals(Math.sqrt(3) / 2, sin.flat().p().doubleValue() / sin.flat().q().doubleValue(), 1e-9);
        assertEquals(cos.flat().q(), sin.flat().q());
    }

    @Test
    void theAnswerIsExactlyOnTheCircle() {
        Result.RatioValue one = value(new Calculator(), "cos(1/7)^2 + sin(1/7)^2");
        assertEquals("1", one.readings().get("classical"));
    }

    @Test
    void aMultipleOfTheTurnGivesTheSameAnswer() {
        Calculator c = new Calculator();
        assertEquals(value(c, "cos(1/6)").flat(), value(c, "cos(2/12)").flat());
        assertEquals(value(c, "sin(5/7)").flat(), value(c, "sin(10/14)").flat());
    }

    @Test
    void theQuarterAndHalfTurnsLandExactly() {
        Calculator c = new Calculator();
        assertEquals(T.of(0, 2), value(c, "cos(1/4)").flat());
        assertEquals(T.of(2, 2), value(c, "sin(1/4)").flat());
        assertEquals(T.of(-1, 1), value(c, "cos(1/2)").flat());
        assertEquals(T.of(1, 1), value(c, "cos(0)").flat());
    }

    @Test
    void aSecondArgumentIsTheDepth() {
        Calculator c = new Calculator();
        assertEquals(RationalTrig.cosTurn(BigInteger.ONE, 6, 5), value(c, "cos(1/6, 5)").flat());
        assertEquals("depth 5, between T(4,7) and T(3,5), sin² of the gap 1/2210",
                value(c, "cos(1/6, 5)").readings().get("cos(1/6, 5)"));
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/6, 1/2)"));
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/6, 5000)"));
    }

    @Test
    void definitionsAndFunctionsCanUseThem() {
        Calculator c = new Calculator();
        c.enter("t = 1/8");
        c.enter("f(x) = 2sin(x)");
        Result.RatioValue r = value(c, "f(t)");
        assertEquals(Math.sqrt(2), r.flat().p().doubleValue() / r.flat().q().doubleValue(), 1e-9);
    }

    @Test
    void whatIsRefused() {
        Calculator c = new Calculator();
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/0)"));
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/(-6))"));
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/3000)"));
        assertThrows(CalculatorException.class, () -> c.enter("cos(1, 2, 3)"));
        assertThrows(CalculatorException.class, () -> c.enter("cos = 2"));
        assertThrows(CalculatorException.class, () -> c.enter("sin(x) = x"));
        c.set(Arithmetic.TRACTION_POINT);
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/6)"));
    }

    @Test
    void aFreeVariableLeavesTheCallAsWritten() {
        assertEquals("cos(y)", new Calculator().enter("cos(y)").text());
    }
}
