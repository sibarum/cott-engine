package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;
import sibarum.cott.algebra.NumberType;
import sibarum.cott.traction.RationalTrig;
import sibarum.cott.traction.T;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrigTest {

    private static Result.Value value(Calculator c, String line) {
        return (Result.Value) c.enter(line);
    }

    private static T flat(Calculator c, String line) {
        return value(c, line).flat().orElseThrow();
    }

    @Test
    void cosAndSinTakeATurnAndAnswerWithTheUnreducedPair() {
        Calculator c = new Calculator();
        Result.Value cos = value(c, "cos(1/6)");
        T spin = RationalTrig.dialed(BigInteger.ONE, 6, Limits.STANDARD.maxSteps(), Limits.STANDARD.width(),
                Limits.STANDARD.maxPowerBits()).spin();
        assertEquals(new T(spin.q().pow(2).subtract(spin.p().pow(2)), spin.norm()), cos.flat().orElseThrow());
        assertEquals(0.5, cos.flat().orElseThrow().p().doubleValue() / cos.flat().orElseThrow().q().doubleValue(), 1e-9);
        assertTrue(cos.readings().get("cos(1/6)").startsWith("depth "));

        Result.Value sin = value(c, "sin(1/6)");
        assertEquals(Math.sqrt(3) / 2, sin.flat().orElseThrow().p().doubleValue() / sin.flat().orElseThrow().q().doubleValue(), 1e-9);
        assertEquals(cos.flat().orElseThrow().q(), sin.flat().orElseThrow().q());
    }

    @Test
    void theAnswerIsExactlyOnTheCircle() {
        Result.Value one = value(new Calculator(), "cos(1/7)^2 + sin(1/7)^2");
        assertEquals("1", one.readings().get("classical"));
    }

    @Test
    void aMultipleOfTheTurnGivesTheSameAnswer() {
        Calculator c = new Calculator();
        assertEquals(flat(c, "cos(1/6)"), flat(c, "cos(2/12)"));
        assertEquals(flat(c, "sin(5/7)"), flat(c, "sin(10/14)"));
    }

    @Test
    void theQuarterAndHalfTurnsLandExactly() {
        Calculator c = new Calculator();
        assertEquals(T.of(0, 2), flat(c, "cos(1/4)"));
        assertEquals(T.of(2, 2), flat(c, "sin(1/4)"));
        assertEquals(T.of(-1, 1), flat(c, "cos(1/2)"));
        assertEquals(T.of(1, 1), flat(c, "cos(0)"));
    }

    @Test
    void aSecondArgumentIsTheDepth() {
        Calculator c = new Calculator();
        assertEquals(RationalTrig.cosTurn(BigInteger.ONE, 6, 5), flat(c, "cos(1/6, 5)"));
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
        Result.Value r = value(c, "f(t)");
        assertEquals(Math.sqrt(2), r.flat().orElseThrow().p().doubleValue() / r.flat().orElseThrow().q().doubleValue(), 1e-9);
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
        assertThrows(CalculatorException.class, () -> c.enter("cos(C(1, 2))"));
        c.set(NumberType.DECIMAL);
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/6)"));
    }

    @Test
    void aFreeVariableLeavesTheCallAsWritten() {
        assertEquals("cos(y)", new Calculator().enter("cos(y)").text());
    }

    @Test
    void theLimitsModeBoundsTheDescent() {
        Calculator c = new Calculator();
        assertEquals(Limits.STANDARD, c.limits());
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/1500)"));
        c.set(Limits.DEEP);
        assertTrue(value(c, "cos(1/1500)").readings().get("cos(1/1500)").startsWith("depth "));
        c.set(Limits.SHALLOW);
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/500)"));
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/6, 300)"));
    }

    @Test
    void theCertificateSaysWhichLimitStoppedIt() {
        Calculator c = new Calculator();
        assertTrue(value(c, "cos(1/6)").readings().get("cos(1/6)").matches("depth [0-9]+, between .*"));
        assertTrue(value(c, "cos(1/4)").readings().get("cos(1/4)").startsWith("depth 1024 (step limit)"));
        assertTrue(value(c, "cos(250/1000)").readings().get("cos(250/1000)").startsWith("depth 1024 (step limit)"));
    }

    @Test
    void anExplicitDepthIsRefusedRatherThanCutShort() {
        Calculator c = new Calculator();
        CalculatorException e = assertThrows(CalculatorException.class, () -> c.enter("cos(333/999, 1000)"));
        assertTrue(e.getMessage().contains("past standard limits"), e.getMessage());
    }
}
