package sibarum.cott;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sibarum.cott.engine.base.expr.IExpr;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Printing and parsing are inverse, not merely similar.
 *
 * <p>This is the property the whole syntax layer exists to keep, and it is a property and not a habit: the
 * result of one evaluation is the entry for the next, so anything the display can show it has to be able to
 * read. Both halves derive from {@link Notation} — the printer drops a multiplication sign exactly where the
 * adjacency pass will put it back — and this is what checks that they still agree.
 *
 * <p>Checked at the string, going twice. A term that prints as {@code s} must parse back to something that
 * prints as {@code s} again; that is the fixed point, and it is what a display needs. Equality of the two
 * TERMS is a stronger claim and is deliberately not made here: {@code 1÷2} parses to a product with a
 * reciprocal in it and only becomes the pair (1, 2) when something simplifies it, which is the engine's
 * business rather than the printer's.
 */
class RoundTripTest {

    private static String show(String entry) {
        return Render.show(Parser.parse(Notation.normalize(entry)));
    }

    private static String reshow(String printed) {
        return Render.show(Parser.parse(Notation.normalize(printed)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1+1", "2-1", "1÷2", "2.5", "0", "w", "-1", "2w", "x", "π", "e", "i",
            "x+3x", "x·y", "xy", "2(x+1)", "(1+x)÷y", "1÷(x+1)", "x^2+1", "0^1", "0^0", "0^-2",
            "0^(1+w)", "2^3^2", "-x", "1÷2w", "sin(x)", "sin(x)cos(y)", "atan2(x, y)", "log(x, 0)",
            "1-1", "0w", "x-x", "x÷x", "2·sin(x)+1",
    })
    void printingIsInverseToParsing(String entry) {
        String printed = show(entry);
        assertEquals(printed, reshow(printed), entry + " printed as " + printed);
    }

    /**
     * And it survives simplification, which is the case that actually happens: what the engine hands back is
     * what gets shown, and that has to be re-enterable too.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "1+1", "2-1", "1÷2", "5÷10", "2.5", "0w", "1-1", "x-x", "2·3", "1÷2w", "0^(1+w)", "sin(0)",
            "cos(π÷3)", "sin(2)", "x+3x", "2^3",
    })
    void printingIsInverseToParsingAfterSimplification(String entry) {
        IExpr answered = Cott.reduce(Parser.parse(Notation.normalize(entry)));
        String printed = Render.show(answered);
        assertEquals(printed, reshow(printed), entry + " answered as " + printed);
    }

    /**
     * The printer applies no rule. {@code 0^0} is 1 by E5 and {@code 0^1} is 0 by E4, and neither is the
     * printer's to say — the previous one named such points itself, which it could do only because the carrier
     * it printed was already a normal form.
     */
    @Test
    void theDisplayNamesNothingTheEngineHasNotAnswered() {
        assertEquals("0^0", show("0^0"));
        assertEquals("0^1", show("0^1"));
        assertEquals("0^ω", show("0^w"));
    }

    /** Coordinates do not reduce, and two spellings of one value stay two literals. */
    @Test
    void aSpellingKeepsTheCoordinatesItWasWrittenAt() {
        assertEquals("1÷2", Cott.evaluate("1÷2"));
        assertEquals("0.5", Cott.evaluate("5÷10"));
        assertEquals("2.5", Cott.evaluate("2.5"));
        // The decimal spelling is used only where the denominator is already a power of ten, so what it prints
        // reads back as the same pair. 1÷3 has no decimal at all and keeps its quotient.
        assertEquals("1÷3", Cott.evaluate("1÷3"));
    }
}
