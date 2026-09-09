package sibarum.cott;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The real-valued functions: the one corner of this engine that approximates, and the notation that reaches it.
 *
 * <p>Two properties carry most of the weight here. A call <b>answers when it can and stands when it cannot</b>,
 * so {@code sin(2)} is a number and {@code sin(x)} is a term — and standing is not failure, it is what lets the
 * plotter draw the thing. And the answer is <b>rounded</b>, which is what makes {@code sin(π)} zero rather than
 * the 1.22e-16 the floating-point arithmetic actually computes.
 */
class RealTest {

    private static String ev(String entry) {
        return Cott.evaluate(entry);
    }

    /** The exact answers, which are the ones a keypad is judged on. */
    @Test
    void exactAtTheLandmarks() {
        assertEquals("0", ev("sin(0)"));
        assertEquals("1", ev("cos(0)"));
        assertEquals("1", ev("sin(π÷2)"));
        // Coordinates do not reduce, so this is the pair (5, 10) and prints as the decimal that names it.
        // The old engine reduced it to 5/10 = 1/2 and printed 1÷2; that reduction is what the carrier gave up.
        assertEquals("0.5", ev("cos(π÷3)"));
        assertEquals("1", ev("tan(π÷4)"));
        // The reason the answer is rounded at all: sin π is 1.22e-16 in binary floating point, and a
        // calculator that prints that is reporting the arithmetic's error as the answer.
        assertEquals("0", ev("sin(π)"));
        assertEquals("0", ev("cos(π÷2)"));
        // The display's minus is U+2212, the glyph Notation normalizes to and the parser reads natively --
        // the same reason the display prints · rather than the * that was typed.
        assertEquals("−1", ev("cos(π)"));
    }

    /** The reciprocals and the inverses, including the branch acot was given deliberately. */
    @Test
    void theRestOfTheCircle() {
        assertEquals("2", ev("sec(π÷3)"));
        assertEquals("1", ev("csc(π÷2)"));
        assertEquals("0", ev("cot(π÷2)"));
        assertEquals("0", ev("asin(0)"));
        assertEquals("0", ev("atan(0)"));
        assertEquals("0", ev("acos(1)"));
        // acot is the CONTINUOUS branch, so acot(0) is π/2 rather than the tear atan(1/x) would leave.
        // Compared against another quarter turn rather than against π÷2, which is symbolic: π is an atom,
        // so an expression naming it stays exact while a computed angle is the rounded decimal.
        assertEquals(ev("asin(1)"), ev("acot(0)"));
        assertEquals("0", ev("asec(1)"));
        assertEquals(ev("asin(1)"), ev("acsc(1)"));
    }

    @Test
    void hyperbolics() {
        assertEquals("0", ev("sinh(0)"));
        assertEquals("1", ev("cosh(0)"));
        assertEquals("0", ev("tanh(0)"));
        assertEquals("0", ev("asinh(0)"));
        assertEquals("0", ev("acosh(1)"));
        assertEquals("0", ev("atanh(0)"));
    }

    /**
     * Angles are constructed, not switched. A DEG/RAD mode makes every stored expression ambiguous about which
     * one it was written in; {@code deg(90)} cannot be.
     */
    @Test
    void degreesAndRadians() {
        assertEquals("1", ev("sin(deg(90))"));
        assertEquals("−1", ev("cos(deg(180))"));
        assertEquals(ev("acos(−1)"), ev("deg(180)"));   // half a turn, both computed
        assertEquals("2", ev("rad(2)"));
        assertEquals("1", ev("sin(rad(π÷2))"));
    }

    /** The argument order the keypad's label promises: the angle of the point (x, y). */
    @Test
    void atan2TakesThePointInTheOrderItIsWritten() {
        assertEquals("0", ev("atan2(1, 0)"));
        assertEquals(ev("asin(1)"), ev("atan2(0, 1)"));   // straight up is a quarter turn
        assertEquals(ev("atan(1)"), ev("atan2(1, 1)"));   // the diagonal is an eighth of one
    }

    /** With a variable in it there is no number to work on, so the call stands -- and stays plottable. */
    @Test
    void aCallWithNoRealArgumentStands() {
        assertEquals("sin(x)", ev("sin(x)"));
        // Written order, since nothing canonicalises a sum yet -- that is phase 2. The call standing is the
        // property under test here and it is unaffected.
        assertEquals("cos(x+1)", ev("cos(x+1)"));
        assertEquals("x·sin(x)", ev("x·sin(x)"));
        assertEquals("2·sin(x)", ev("2sin(x)"));
        // and a call that HAS reduced is an ordinary number in whatever surrounds it
        assertEquals("2", ev("1+sin(π÷2)"));
    }

    /**
     * Where the answer is not a finite real the term stands, which is this engine's standing habit. A NaN
     * leaking into the display would be the one kind of answer it has never given.
     */
    @Test
    void outOfDomainStands() {
        assertEquals("asin(2)", ev("asin(2)"));
        assertEquals("acosh(0)", ev("acosh(0)"));
        assertEquals("atanh(1)", ev("atanh(1)"));
        // ω has no real reading, so a call on it is not arithmetic either
        assertEquals("sin(ω)", ev("sin(w)"));
    }

    /** A call is a value, unlike log -- so it is an operand of everything, and reads back as itself. */
    @Test
    void callsAreValuesAndReadBack() {
        for (String entry : new String[]{"sin(x)", "atan2(x, y)", "cos(x)+sin(y)", "2·sin(x)", "sin(x)^2"}) {
            String once = ev(entry);
            assertEquals(once, ev(once), "not a fixed point: " + entry + " -> " + once);
        }
    }

    /** A name is only a name when the vocabulary says so, and the vocabulary must not break juxtaposition. */
    @Test
    void wordsDoNotDisturbJuxtaposition() {
        assertEquals("xy", ev("xy"));            // still a product, as it has always been
        assertEquals(ev("x·y"), ev("xy"));
        assertEquals("2·sin(x)", ev("2 sin(x)"));
        // Written order. Canonical ordering was the evaluator sorting a product, and there is no rule to do
        // it yet -- phase 2. What is under test here is that the word scan found the call at all.
        assertEquals("sin(x)y", ev("sin(x)y"));
        // A sign is needed here and not above: c does not start an operand, so juxtaposition would not read
        // back as a product. That rule is Notation.implied and is exactly what this test is about.
        assertEquals("sin(x)·cos(y)", ev("sin(x)cos(y)"));
        // sinh wins over sin, because the scan takes the longest word standing at that point
        assertEquals("sinh(x)", ev("sinh(x)"));
    }

    @Test
    void aCallSaysWhatItNeeds() {
        assertEquals("sin needs its argument in brackets",
                assertThrows(SyntaxException.class, () -> ev("sin x")).getMessage());
        assertEquals("atan2 takes 2 arguments, not 1",
                assertThrows(SyntaxException.class, () -> ev("atan2(1)")).getMessage());
        assertEquals("sin takes 1 argument, not 2",
                assertThrows(SyntaxException.class, () -> ev("sin(1, 2)")).getMessage());
    }

    /**
     * Twelve places is the promise; the digits below it are the arithmetic's, not the answer's. And the answer
     * is SHOWN as a decimal — exactly the same exact rational either way, but the quotient spelling of this one
     * is {@code 454648713413÷500000000000}, which nobody can read.
     */
    @Test
    void roundedToTwelvePlacesAndShownAsOne() {
        assertEquals("0.909297426826", ev("sin(2)"));
        assertEquals("0.785398163397", ev("atan(1)"));
        assertEquals("1.557407724655", ev("tan(1)"));
        // A typed decimal is the pair it was written at -- 2.5 is (25, 10) -- so it comes back as typed rather
        // than as the reduced fraction the old carrier turned it into.
        assertEquals("2.5", ev("2.5"));
        assertEquals("1÷3", ev("1÷3"));
        // 1÷2w is HALF of omega now, and the pair says which half: ((1,2), -1). It used to be omega itself,
        // because omega was the coordinate pair (1,0) and a zero denominator absorbed the factor -- so
        // halving omega did not move it. Omega is 0^-1 here and the real part holds the 1÷2, which is also
        // what makes 0^(w÷2) spellable; see Parser on i.
        assertEquals("1÷2ω", ev("1÷2w"));
        assertEquals("1÷2ω", ev(ev("1÷2w")));
    }

    /**
     * The window is twelve places <em>downstream</em> too, and it has to be: arithmetic after an approximation
     * is exact arithmetic on an approximation, so the digits accumulate. Squaring a twelve-place value gives a
     * twenty-four-place one, every digit correct and none of them useful.
     */
    @Test
    void theWindowHoldsDownstreamOfACall() {
        assertEquals("1", ev("sin(π÷4)^2+cos(π÷4)^2"));
        assertEquals("0.826821810432", ev("sin(2)^2"));
        // A rounding that would lose the number is not a spelling of it: 2^-50 is small and is not zero, and
        // its quotient says so in fewer characters than a decimal full of zeros would.
        assertEquals("1÷1125899906842624", ev("1÷2^50"));
    }
}
