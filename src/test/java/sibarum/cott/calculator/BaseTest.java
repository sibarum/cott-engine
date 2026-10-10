package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;
import sibarum.cott.algebra.Base;
import sibarum.cott.algebra.NumberType;
import sibarum.cott.notation.SyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@code e = b}: the base of {@code e^x}, and the unit {@code cos} and {@code sin} count in. */
class BaseTest {

    private static Result.Value value(Calculator c, String line) {
        return (Result.Value) c.enter(line);
    }

    @Test
    void theBaseIsTheFullTurnOneUnlessInIeee() {
        assertEquals(Base.ONE, new Calculator().base());
        assertEquals(Base.ONE, new Calculator(NumberType.DECIMAL).base());
        assertEquals(Base.E, new Calculator(NumberType.IEEE).base());
    }

    @Test
    void eIsSetToABaseByName() {
        Calculator c = new Calculator();
        assertEquals("e = -1: e^x is -1^x, counted in half turns", c.enter("e = -1").text());
        assertEquals(Base.NEG_ONE, c.base());
        c.enter("e = i");
        assertEquals(Base.I, c.base());
        c.enter("e = ω");
        assertEquals(Base.OMEGA, c.base());
        c.enter("e = e");
        assertEquals(Base.E, c.base());
        assertThrows(CalculatorException.class, () -> c.enter("e = 2"));
        assertThrows(SyntaxException.class, () -> c.enter("e(x) = x"));
        assertThrows(SyntaxException.class, () -> c.enter("f(e) = e"));
    }

    @Test
    void cosAndSinCountInTheBasesUnit() {
        Calculator turns = new Calculator();
        String sixth = turns.enter("cos(1/6)").text();
        Calculator halves = new Calculator();
        halves.enter("e = -1");
        assertEquals(sixth, halves.enter("cos(1/3)").text());
        Calculator quarters = new Calculator();
        quarters.enter("e = i");
        assertEquals(sixth, quarters.enter("cos(2/3)").text());
    }

    @Test
    void eToTheXIsTheRotationByXUnits() {
        Calculator c = new Calculator();
        Result.Value quarter = value(c, "e^(1/4)");
        assertEquals("C(Q(2, 2), Q(0, 2))", quarter.text());
        assertEquals("i", quarter.readings().get("value"));
        assertTrue(quarter.readings().get("e^(1/4)").startsWith("depth "), quarter.readings().toString());
        assertTrue(quarter.readings().get("rung").startsWith("up to error"));
        assertEquals("-1", value(c, "e^(1/2)").readings().get("value"));
        c.enter("e = -1");
        assertEquals("-1", value(c, "e^1").readings().get("value"), "the spinor: a whole unit is a half turn");
        c.enter("f(x) = e^x");
        c.enter("e = i");
        assertEquals("i", value(c, "f(1)").readings().get("value"), "the base is the one in force where it is used");
    }

    @Test
    void eIsTheBasesValue() {
        Calculator c = new Calculator();
        assertEquals("1", c.enter("e").text());
        c.enter("e = i");
        assertEquals("C(1, 0)", c.enter("e").text());
        c.enter("e = ω");
        assertEquals("Q(1, 0)", c.enter("e").text());
    }

    @Test
    void whatABaseWithoutATurnOrTheClassicalEOverTheIntegersRefuses() {
        Calculator c = new Calculator();
        c.enter("e = 0");
        CalculatorException noTurn = assertThrows(CalculatorException.class, () -> c.enter("e^1"));
        assertTrue(noTurn.getMessage().contains("0 : 0"), noTurn.getMessage());
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/6)"));
        c.enter("e = e");
        assertThrows(CalculatorException.class, () -> c.enter("cos(1/6)"));
        assertThrows(CalculatorException.class, () -> c.enter("e"));
        assertThrows(CalculatorException.class, () -> new Calculator(NumberType.DECIMAL).enter("e^(1/4)"));
    }

    @Test
    void inIeeeTheClassicalEIsRadians() {
        Calculator c = new Calculator(NumberType.IEEE);
        assertEquals("2.718281828459045", c.enter("e").text());
        assertEquals("2.718281828459045", c.enter("e^1").text());
        assertEquals("1", c.enter("cos(0)").text());
        assertEquals("-1", c.enter("cos(3.141592653589793)").text());
        c.enter("e = 1");
        assertEquals("-1", c.enter("cos(0.5)").text());
        assertEquals("C(1, 6.123233995736766·10^-17)", c.enter("e^0.25").text());
    }
}
