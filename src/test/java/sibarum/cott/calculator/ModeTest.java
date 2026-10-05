package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModeTest {

    @Test
    void theArithmeticModeset() {
        assertEquals(List.of(Arithmetic.IEEE_FLOATING_POINT, Arithmetic.TRACTION_RATIO, Arithmetic.TRACTION_POINT,
                        Arithmetic.COMPLEX_RATIO, Arithmetic.BICOMPLEX, Arithmetic.BICOMPLEX_RATIO),
                Modeset.ARITHMETIC.modes());
        assertEquals("IEEE Floating Point", Arithmetic.IEEE_FLOATING_POINT.label());
        assertEquals("T(T,T) Compound Ratio", Arithmetic.TRACTION_RATIO.label());
        assertEquals(Optional.of(Arithmetic.IEEE_FLOATING_POINT), Modeset.ARITHMETIC.mode("ieee"));
        assertEquals(Optional.empty(), Modeset.ARITHMETIC.mode("wheel"));
    }

    @Test
    void everyModeBelongsToItsModesetAndKeysAreUnique() {
        for (Modeset set : Modeset.values()) {
            assertTrue(set.modes().contains(set.initial()), set.label());
            for (Mode m : set.modes()) assertEquals(set, m.modeset(), m.label());
            assertEquals(set.modes().size(), new HashSet<>(set.modes().stream().map(Mode::key).toList()).size());
        }
    }

    @Test
    void aCalculatorStartsInTractionRatioAndChangesMode() {
        Calculator c = new Calculator();
        assertEquals(Arithmetic.TRACTION_RATIO, c.arithmetic());
        assertEquals(Arithmetic.TRACTION_RATIO, c.mode(Modeset.ARITHMETIC));
        c.set(Arithmetic.IEEE_FLOATING_POINT);
        assertEquals(Arithmetic.IEEE_FLOATING_POINT, c.arithmetic());
        assertEquals(Arithmetic.TRACTION_RATIO, new Calculator(Arithmetic.TRACTION_RATIO).arithmetic());
    }

    @Test
    void aDefinitionIsEvaluatedInTheArithmeticOfTheLineThatUsesIt() {
        Calculator c = new Calculator();
        c.enter("x = 1/3");
        assertEquals("1/3", c.enter("x").text());
        assertInstanceOf(Result.RatioValue.class, c.enter("x"));
        c.set(Arithmetic.IEEE_FLOATING_POINT);
        assertEquals("0.3333333333333333", c.enter("x").text());
        assertInstanceOf(Result.IeeeValue.class, c.enter("x"));
        c.set(Arithmetic.TRACTION_RATIO);
        assertEquals("ω", c.enter("1/0").text());
    }

    @Test
    void whatEachArithmeticTakesAsAValue() {
        assertTrue(Arithmetic.TRACTION_RATIO.hasOmega());
        assertTrue(!Arithmetic.TRACTION_RATIO.hasDecimals());
        assertTrue(!Arithmetic.IEEE_FLOATING_POINT.hasOmega());
        assertTrue(Arithmetic.IEEE_FLOATING_POINT.hasDecimals());
        assertThrows(CalculatorException.class, () -> new Calculator().enter("0.5"));
        assertThrows(CalculatorException.class, () -> new Calculator(Arithmetic.IEEE_FLOATING_POINT).enter("ω"));
    }

    @Test
    void theReplListsAndChangesModes() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Repl.run(new ByteArrayInputStream(":mode\n1/0\n:mode ieee\n1/0\n:mode\n:mode wheel\n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));
        String out = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(out.contains("  * compound         T(T,T) Compound Ratio"), out);
        assertTrue(out.contains("Arithmetic: IEEE Floating Point"), out);
        assertTrue(out.contains("= ω"), out);
        assertTrue(out.contains("= ∞"), out);
        assertTrue(out.contains("    exact  ∞"), out);
        assertTrue(out.contains("  * ieee             IEEE Floating Point"), out);
        assertTrue(out.contains("! no mode 'wheel'"), out);
    }
}
