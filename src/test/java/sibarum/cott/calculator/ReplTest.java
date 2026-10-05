package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;
import sibarum.cott.notation.SyntaxException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplTest {

    private static String session(String input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Repl.run(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void escapesExpandToTheirSymbols() {
        assertEquals("ω(2x) − 1 ÷ 2 · 3 × 4", Escapes.expand("\\o(2x) \\- 1 \\/ 2 \\. 3 \\x 4"));
        assertEquals("1/0", Escapes.expand("1/0"));
    }

    @Test
    void badEscapesAreSyntaxErrors() {
        assertThrows(SyntaxException.class, () -> Escapes.expand("\\q"));
        assertThrows(SyntaxException.class, () -> Escapes.expand("1 + \\"));
    }

    @Test
    void anEscapeIsTypedAndTheSymbolIsPrinted() throws IOException {
        String out = session("1/0\n\\o + 1\nw = \\o\n:quit\n");
        assertTrue(out.contains("= ω"), out);
        assertTrue(out.contains("w = ω"), out);
        assertTrue(!out.contains("\\"), out);
    }

    @Test
    void aTypedSymbolStillWorks() throws IOException {
        assertTrue(session("ω + 1\n").contains("= ω"));
    }

    @Test
    void aBadEscapeIsReportedAndTheSessionGoesOn() throws IOException {
        String out = session("\\q\n1/0\n");
        assertTrue(out.contains("! unknown escape '\\q'"), out);
        assertTrue(out.contains("= ω"), out);
    }

    @Test
    void symbolsListsWhatTheCurrentArithmeticCanUse() throws IOException {
        String out = session(":symbols\n:mode ieee\n:symbols\n");
        int ieee = out.indexOf("Symbols in IEEE Floating Point");
        assertTrue(out.indexOf("Symbols in T(T,T) Compound Ratio") >= 0, out);
        assertTrue(ieee > 0, out);
        assertTrue(out.substring(0, ieee).contains("\\o  ω  omega"), out);
        assertTrue(!out.substring(ieee).contains("ω"), out);
        assertTrue(out.substring(ieee).contains("\\/  ÷  divided by"), out);
        assertTrue(out.substring(ieee).contains("    ^    to the power of"), out);
        assertTrue(out.substring(ieee).contains("    ( )  grouping"), out);
    }

    @Test
    void aSessionOpensWithAWelcomeThatPointsToHelp() throws IOException {
        String out = session("");
        assertTrue(out.startsWith("The traction calculator, in T(T,T) Compound Ratio." + System.lineSeparator()), out);
        assertTrue(out.contains("Type :help"), out);
    }

    @Test
    void helpListsTheEscapes() throws IOException {
        String out = session(":help\n");
        assertTrue(out.contains("\\o  ω"), out);
    }
}
