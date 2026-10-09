package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;
import sibarum.cott.algebra.Form;
import sibarum.cott.algebra.NumberType;
import sibarum.cott.algebra.SizeLimit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModeTest {

    @Test
    void theModesets() {
        assertEquals(List.of(Modeset.NUMBER_TYPE, Modeset.SIZE_LIMIT, Modeset.FORM, Modeset.LIMITS), List.of(Modeset.values()));
        assertEquals(List.of(NumberType.INTEGER, NumberType.DECIMAL, NumberType.IEEE), Modeset.NUMBER_TYPE.modes());
        assertEquals(List.of(SizeLimit.SMALL, SizeLimit.MEDIUM, SizeLimit.LARGE), Modeset.SIZE_LIMIT.modes());
        assertEquals(List.of(Form.SUM_OF_PRODUCTS, Form.PRODUCT_OF_SUMS), Modeset.FORM.modes());
        assertEquals("IEEE 64-bit", NumberType.IEEE.label());
        assertEquals(Optional.of(NumberType.IEEE), Modeset.NUMBER_TYPE.mode("ieee64"));
        assertEquals(Optional.empty(), Modeset.NUMBER_TYPE.mode("wheel"));
    }

    @Test
    void everyModeBelongsToItsModesetAndKeysAreUnique() {
        Set<String> keys = new HashSet<>();
        for (Modeset set : Modeset.values()) {
            assertTrue(set.modes().contains(set.initial()), set.label());
            for (Mode m : set.modes()) {
                assertEquals(set, m.modeset(), m.label());
                assertTrue(keys.add(m.key()), "key " + m.key() + " is in two modes");
            }
        }
    }

    @Test
    void aCalculatorStartsOverIntegersAndChangesMode() {
        Calculator c = new Calculator();
        assertEquals(NumberType.INTEGER, c.numberType());
        assertEquals(SizeLimit.MEDIUM, c.sizeLimit());
        assertEquals(Form.SUM_OF_PRODUCTS, c.form());
        assertEquals(Limits.STANDARD, c.limits());
        c.set(NumberType.IEEE);
        assertEquals(NumberType.IEEE, c.numberType());
        assertEquals(Form.SUM_OF_PRODUCTS, c.form(), "a mode of one modeset leaves the others");
        assertEquals(NumberType.DECIMAL, new Calculator(NumberType.DECIMAL).numberType());
    }

    @Test
    void aDefinitionIsEvaluatedInTheModesOfTheLineThatUsesIt() {
        Calculator c = new Calculator();
        c.enter("x = 1/3");
        assertEquals("Q(1, 3)", c.enter("x").text());
        c.set(NumberType.IEEE);
        assertEquals("0.3333333333333333", c.enter("x").text());
        c.set(NumberType.DECIMAL);
        assertEquals("0.3333333333333333333333333333333333", c.enter("x").text());
        c.set(SizeLimit.SMALL);
        assertEquals("0.3333333333333333", c.enter("x").text());
    }

    @Test
    void theSizeLimitBoundsIntegers() {
        Calculator c = new Calculator(SizeLimit.SMALL);
        assertEquals("9223372036854775808", c.enter("2^63").text());
        assertThrows(CalculatorException.class, () -> c.enter("2^64"));
        c.set(SizeLimit.MEDIUM);
        assertEquals("18446744073709551616", c.enter("2^64").text());
    }

    @Test
    void integersTakeNoDecimal() {
        assertThrows(CalculatorException.class, () -> new Calculator().enter("0.5"));
        assertEquals("0.5", new Calculator(NumberType.DECIMAL).enter("0.5").text());
    }

    @Test
    void theReplListsAndChangesModes() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Repl.run(new ByteArrayInputStream(":mode\n1/0\n:mode ieee64\n1/0\n:mode\n:mode wheel\n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));
        String out = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(out.contains("  * integer  Integer"), out);
        assertTrue(out.contains("  * sum-of-products  Sum of products"), out);
        assertTrue(out.contains("Number type: IEEE 64-bit"), out);
        assertTrue(out.contains("= Q(1, 0)"), out);
        assertTrue(out.contains("= ∞"), out);
        assertTrue(out.contains("    exact  ∞"), out);
        assertTrue(out.contains("  * ieee64   IEEE 64-bit"), out);
        assertTrue(out.contains("! no mode 'wheel'"), out);
    }

    @Test
    void theRecursionLimitsModeset() {
        assertEquals(List.of(Limits.SHALLOW, Limits.STANDARD, Limits.DEEP), Modeset.LIMITS.modes());
        assertEquals(Limits.STANDARD, Modeset.LIMITS.initial());
        assertEquals(Optional.of(Limits.DEEP), Modeset.LIMITS.mode("deep"));
        Calculator c = new Calculator();
        c.set(Limits.DEEP);
        assertEquals(Limits.DEEP, c.limits());
        assertEquals(NumberType.INTEGER, c.numberType(), "a mode of one modeset leaves the others");
    }
}
