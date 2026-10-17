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
        Result.Value v = value("C(1, 2) + Q(1, 0)");
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
    void zIsReadAsAPowerOfZeroAndItsLogarithm() {
        Result.Value z = value("Z(3, 2)");
        assertEquals("3·0^2", z.readings().get("value"));
        assertEquals(Rung.EXACT, z.rungs().get("value"), "off a zero coefficient Z is faithful");
        assertEquals("2 - 0·ln 3", z.readings().get("log₀"));
        assertEquals(Rung.EXACT, z.rungs().get("log₀"), "on a positive coefficient E keeps both");
        assertEquals("T/ZeroAlgebra and T/Residual: Z of two numbers", z.readings().get("cott-lean"));
        assertEquals("0", value("Z(1, 1)").readings().get("value"));
        assertEquals("-0", value("-Z(1, 1)").readings().get("value"));
        assertEquals("ω", value("Z(1, -1)").readings().get("value"));
        assertEquals("(1/2)·ω^3", value("Z(1/2, -3)").readings().get("value"));
        assertEquals("1", value("Z(1, 1)").readings().get("log₀"));
        assertEquals("-1", value("Z(1, -1)").readings().get("log₀"));
        Result.Value residual = value("Z(6/2, 0) - Z(6/2, 0)");
        assertEquals("3·0", residual.readings().get("value"));
        assertEquals(Rung.QUOTIENT, residual.rungs().get("value"), "Z(Q(6, 2), 1) and Z(3, 1) are one value");
        assertEquals("T/Residual: Z of Q", residual.readings().get("cott-lean"));
    }

    @Test
    void eIsReadAsALogarithmInBaseZero() {
        Result.Value e = value("E(3, 2)");
        assertEquals("2 - 0·ln 3", e.readings().get("value"));
        assertEquals("3·0^2", e.readings().get("0^E"));
        assertEquals("-0·ln(1/2)", value("E(1/2, 0)").readings().get("value"));
        assertEquals("1 - 0·ln|-3|", value("E(-3, 1)").readings().get("value"), "the real logarithm, of |p|");
        assertEquals("3", value("E(0, 2)").readings().get("value"), "log₀ 0 = 1");
        assertEquals(Rung.QUOTIENT, value("E(0, 2)").rungs().get("value"), "E(0, 2) and E(1, 3) are one value");
        assertEquals("T/ZeroAlgebra: E of two numbers", e.readings().get("cott-lean"));
    }

    @Test
    void bIsReadAsALogarithm() {
        Result.Value b = value("E(8, 0) / E(2, 0)");
        assertEquals("3", b.readings().get("value"));
        assertEquals(Rung.QUOTIENT, b.rungs().get("value"));
        assertEquals("3/2", value("B(4, 8)").readings().get("value"));
        assertEquals("-2", value("B(2, 1/4)").readings().get("value"));
        assertTrue(value("B(2, 3)").readings().get("value").startsWith("1.58496250072"), value("B(2, 3)").readings().get("value"));
        assertEquals(Rung.UP_TO_ERROR, value("B(2, 3)").rungs().get("value"));
        assertFalse(value("B(1, 3)").readings().containsKey("value"), "log_1 has no value");
        assertEquals("T/ZeroAlgebra: B of two numbers", b.readings().get("cott-lean"));
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
