package sibarum.cott;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The display layer: what a keypad expression evaluates to, written the way it is typed.
 *
 * <p>The load-bearing test here is {@link #outputReadsBackAsItself}. The result of one evaluation is
 * the entry for the next, so anything the display can write it also has to be able to read — and
 * getting that wrong is subtle, because a rendering can be perfectly legible and still re-parse into
 * a different term. {@code 2ω} is the standing example: with the × dropped it is a product that no
 * longer looks like one.
 */
class DisplayTest {

    private static String ev(String entry) {
        return Cott.evaluate(entry);
    }

    /** Arithmetic on numerals, which are points like everything else. */
    @Test
    void numerals() {
        assertEquals("6", ev("2×3"));
        assertEquals("2", ev("1+1"));
        assertEquals("8", ev("2^3"));
        assertEquals("5÷2", ev("2.5"));
        assertEquals("1", ev("2−1"));
        assertEquals("1÷3", ev("1÷3"));
    }

    /** The wheel points, reached by ordinary division. */
    @Test
    void thePoints() {
        assertEquals("ω", ev("1÷0"));
        assertEquals("2ω", ev("2÷0"));
        assertEquals("(1÷2)ω", ev("1÷2w"));
        assertEquals("0^-2", ev("w×w"));
        assertEquals("1", ev("0^0"));
        assertEquals("-1", ev("0^w"));
        assertEquals("-1", ev("w^w"));
        assertEquals("i", ev("−1^(1÷2)"));
        assertEquals("-1", ev("i^2"));
        assertEquals("1", ev("i^4"));
        // two copies of the point 0 — the multiplicity is the count, not a coefficient to fold in
        assertEquals("2×0", ev("0+0"));
    }

    /**
     * The residue in its two contexts. This is the pair the merged theory exists for: the same
     * subterm, two contexts, two answers.
     */
    @Test
    void theSameResidueInTwoContexts() {
        assertEquals("x", ev("x×(0÷0)"));      // erasure: the operand disappears
        assertEquals("1+x", ev("x+0÷0"));      // residue: it materialises as a one
        assertEquals("1^1", ev("1÷1"));        // the finer reading: not 1
        assertEquals("1^0", ev("0×w"));        // the user law: 0·ω is 0/0
        assertEquals("x", ev("x×(1÷1)"));
        assertEquals("x", ev("x×(w÷w)"));
        // ÷ binds left to right within a product, and div is primitive, so the brackets above are
        // not decoration: x×0÷0 is (x·0)/0, a quotient of a product, and it has no residue in it.
        assertEquals("(0x)÷0", ev("x×0÷0"));
    }

    /** Outside the closure set nothing erases, so the winding survives in both contexts. */
    @Test
    void windingsOutsideTheClosureSet() {
        assertEquals("x×1^y", ev("x×(y÷y)"));
        assertEquals("x+1^y", ev("x+y÷y"));
        assertEquals("x×0^y", ev("x×(y−y)"));
        assertEquals("x+0^y", ev("x+(y−y)"));
    }

    /** What has no definite answer is returned unchanged rather than forced. */
    @Test
    void formalAnswers() {
        assertEquals("xy", ev("xy"));
        assertEquals("x+y", ev("x+y"));
        assertEquals("eπ", ev("πe"));
        assertEquals("x^2", ev("x^2"));
        assertEquals("0^x", ev("0^x"));
        assertEquals("(2ω)^ω", ev("(2w)^w"));
        assertEquals("(-1)^ω", ev("−1^w"));    // twist times twist is the ω² floor
    }

    /** Logarithms return an exponent, which is a different sort and prints as one. */
    @Test
    void logarithms() {
        assertEquals("1", ev("log(0, 0)"));
        assertEquals("-1", ev("log(w, 0)"));
        assertEquals("ω", ev("log(−1, 0)"));
        assertEquals("log(x, 0)", ev("log(x, 0)"));
        // an exponent is not a point, so it cannot be an operand
        assertEquals("log is an exponent, not a value",
                assertThrows(SyntaxException.class, () -> ev("2×log(0, 0)")).getMessage());
    }

    /** A typed expression and a clicked one agree, because juxtaposition is resolved in one place. */
    @Test
    void juxtapositionMultiplies() {
        assertEquals(ev("2×ω"), ev("2w"));
        assertEquals(ev("x×y"), ev("xy"));
        assertEquals(ev("3×(x+1)"), ev("3(x+1)"));
        assertEquals(ev("1 + 1"), ev("1+1"));   // whitespace is dropped, not rejected
        assertEquals(ev("1/0"), ev("1÷0"));     // a keyboard cannot reach ÷
        assertEquals(ev("2*3"), ev("2×3"));
    }

    /** Powers associate to the right, and print with the brackets that say so. */
    @Test
    void powersAssociateRight() {
        assertEquals(ev("2^(3^2)"), ev("2^3^2"));
        assertEquals("0^-6", ev("(w^2)^3"));
        // a bare exponent still takes one signed factor, so this is a quotient of the power
        assertEquals(ev("(0^w)÷2"), ev("0^w÷2"));
    }

    /**
     * Every rendering re-parses to the rendering it came from. This is the invariant that keeps the
     * display usable as an editor: the result of one evaluation is the entry for the next.
     *
     * <p>Note that this is a fixed point on the STRING, not on the term. {@code 1^x} is both the
     * multiplicative residue and a literal power of one, and the notation cannot tell them apart —
     * so re-reading a displayed residue yields a power. The theory has both objects and one spelling
     * for them; that is a gap in the notation, not in the evaluator.
     */
    @Test
    void outputReadsBackAsItself() {
        List<String> entries = List.of(
                "2×3", "1+1", "2÷0", "1÷2w", "w×w", "0^0", "0^w", "w^w", "i^2", "0+0",
                "x×0÷0", "x+0÷0", "x×(y÷y)", "x+y÷y", "x×(y−y)", "x+(y−y)",
                "xy", "x+y", "πe", "x^2", "0^x", "(2w)^w", "−1^w", "2.5", "2−1",
                "0^(w÷5)", "0^(−1÷2)", "log(x, 0)", "3(x+1)", "1÷3", "2^3^2");
        for (String entry : entries) {
            String once = ev(entry);
            assertEquals(once, ev(once), "not a fixed point: " + entry + " -> " + once);
        }
    }

    /** Rejections keep the entry, so they have to be reported rather than substituted. */
    @Test
    void rejections() {
        assertThrows(SyntaxException.class, () -> ev("2+"));
        assertThrows(SyntaxException.class, () -> ev("(2"));
        assertThrows(SyntaxException.class, () -> ev("2..5"));
        assertEquals("Error", assertThrows(SyntaxException.class, () -> ev("2&3")).getMessage());
        assertEquals("'&' not in COTT",
                assertThrows(SyntaxException.class, () -> ev("&2")).getMessage());
    }

    /**
     * THE ONE KNOWN NOTATION GAP, recorded rather than worked around. The theory writes the residue
     * families as {@code 1^a} and {@code 0^a}, which is also how a literal power of 1 or 0 is
     * written, and the two are different objects. So a displayed residue does not read back as
     * itself: pressing = again turns it into the power, which then evaluates.
     *
     * <p>This is not a regression — the equational engine spelled them the same way and lost them
     * the same way. Closing it needs a spelling for the residues that the parser can tell apart,
     * which is a decision about the notation and not one this port should make on its own.
     */
    @Test
    void residueSpellingCollidesWithAPower() {
        // the visible half: the display changes under a second =
        assertEquals("1^1", ev("1÷1"));
        assertEquals("1", ev("1^1"));
        assertEquals("1^0", ev("0×w"));
        assertEquals("1", ev("1^0"));
        assertEquals("1^ω", ev("w÷w"));
        assertEquals("1", ev("1^ω"));
        assertEquals("0^1", ev("1−1"));
        assertEquals("0", ev("0^1"));
        assertEquals("0^ω", ev("w−w"));
        assertEquals("-1", ev("0^ω"));
        // the invisible half: the string holds still while the term underneath becomes a power
        assertEquals("1^y", ev("y÷y"));
        assertEquals("wind(y)", reduced("y÷y"));
        assertEquals("pow(pt(1, xp(0, 0, 0)), y)", reduced("1^y"));
    }

    private static String reduced(String entry) {
        return Cott.reduce(Parser.parse(Notation.normalize(entry))).toString();
    }
}
