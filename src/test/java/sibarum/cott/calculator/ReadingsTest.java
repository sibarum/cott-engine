package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;
import sibarum.cott.algebra.Form;
import sibarum.cott.algebra.NumberType;
import sibarum.cott.algebra.Rung;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The readings of every algebra's values, each on its rung. */
class ReadingsTest {

    private static Result.Value value(Calculator c, String line) {
        return (Result.Value) c.enter(line);
    }

    private static Result.Value value(String line) {
        return value(new Calculator(), line);
    }

    @Test
    void aNestedValueIsReadAsTheNumberItStandsFor() {
        Result.Value v = value("1/2 + (4 + 3i)");
        assertEquals("9/2 + 3i", v.readings().get("value"));
        assertEquals(Rung.QUOTIENT, v.rungs().get("value"), "a Q inside gives the same value for many pairs");
        assertEquals("117/4", v.readings().get("norm"));
        assertEquals(Rung.UP_TO_INVARIANT, v.rungs().get("norm"));
        assertTrue(v.readings().get("turn").startsWith("0.09358"), v.readings().get("turn"));
        assertEquals("Nested/Point: C of Q", v.readings().get("cott-lean"));
        assertEquals("exact", v.readings().get("rung"));
    }

    @Test
    void aPointOfNumbersLosesNothingByItsValue() {
        Result.Value v = value("4 + 3i");
        assertEquals("4 + 3i", v.readings().get("value"));
        assertEquals(Rung.EXACT, v.rungs().get("value"));
        assertEquals("25", v.readings().get("norm"));
        assertTrue(v.readings().get("turn").startsWith("0.10241"), v.readings().get("turn"));
        assertEquals("T/Unquotiented and T/PairAlgebras: C of two numbers", v.readings().get("cott-lean"));
    }

    @Test
    void eachAlgebraIsReadAsItsMeaning() {
        assertEquals("1", value("D(2, 3)").readings().get("value"));
        assertEquals("5", value("S(2, 3)").readings().get("value"));
        assertEquals("6", value("P(2, 3)").readings().get("value"));
        assertEquals("11", value("P(2, 3) + P(1, 5)").readings().get("value"));
        assertFalse(value("P(2, 3)").readings().containsKey("norm"), "a real value has no turn to keep apart");
        assertEquals("9/2 + 3i", value(new Calculator(Form.PRODUCT_OF_SUMS), "1/2 + (4 + 3i)").readings().get("value"));
    }

    @Test
    void aZeroDenominatorLeavesNoValue() {
        Result.Value v = value("C(1, 2) + ω");
        assertEquals("undefined", v.readings().get("value"));
        assertEquals(Rung.ERROR, v.rungs().get("value"));
    }

    @Test
    void lowestTermsWhereSomeQIsNotInThem() {
        assertEquals("C(Q(1, 2), Q(2, 1))", value("C(Q(2, 4), Q(6, 3))").readings().get("lowest terms"));
        assertFalse(value("C(Q(1, 2), 3)").readings().containsKey("lowest terms"));
    }

    @Test
    void whatCottLeanCovers() {
        assertEquals("Nested/RatioPoint: Q of C, TC", value(new Calculator(Form.PRODUCT_OF_SUMS), "1/2 + (4 + 3i)").readings().get("cott-lean"));
        assertEquals("Nested/BicomplexRatio: C of Q of C", value("C(Q(C(1, 2), 3), 1)").readings().get("cott-lean"));
        assertTrue(value("P(2, 3) + P(1, 5)").readings().get("cott-lean").startsWith("no file covers"));
        assertTrue(value(new Calculator(NumberType.IEEE), "Q(1, 2)").readings().get("cott-lean").startsWith("no file covers"));
    }

    @Test
    void theAnswersRung() {
        assertEquals("exact", value("6/3").readings().get("rung"));
        Calculator ieee = new Calculator(NumberType.IEEE);
        assertEquals("up to error: IEEE 64-bit rounding", value(ieee, "0.5 + i").readings().get("rung"));
        assertEquals("0.5 + i", value(ieee, "0.5 + i").readings().get("value"));
        assertEquals("error: NaN", value(ieee, "0/0").readings().get("rung"));
        assertEquals(Rung.ERROR, value(ieee, "0/0").rungs().get("rung"));
        assertEquals("up to error: cos and sin are dialed to a bracket", value("cos(1/6)").readings().get("rung"));
    }

    @Test
    void thePromptShowsEachRungThatLosesSomething() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Repl.run(new ByteArrayInputStream("1/2 + (4 + 3i)\n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));
        String out = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(out.contains("    value      9/2 + 3i   (quotient)"), out);
        assertTrue(out.contains("    norm       117/4   (up to invariant)"), out);
        assertTrue(out.contains("    rung       exact" + System.lineSeparator()), out);
    }
}
