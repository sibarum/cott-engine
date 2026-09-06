package sibarum.cott;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a session has named, and the notation that reaches it.
 *
 * <p>The subtle half is not the substitution — it is that a multi-letter name only becomes a name because the
 * vocabulary says so. {@code xy} has always been {@code x·y} and has to stay that way, so the tests below check
 * both directions: a defined name is one token, and an undefined run of letters is still juxtaposition.
 */
class BindingsTest {

    /** Evaluate {@code entry} in {@code session} — the whole path the keypad takes. */
    private static String ev(Bindings session, String entry) {
        return Render.show(Cott.reduce(
                session.expand(Parser.parse(Notation.normalize(entry, session), session))));
    }

    @Test
    void aValueIsSubstituted() {
        Bindings s = Bindings.EMPTY.define("k = 3");
        assertEquals("3", ev(s, "k"));
        assertEquals("9", ev(s, "k^2"));
        assertEquals("6", ev(s, "2k"));           // juxtaposition still multiplies by a name
        assertEquals("3x", ev(s, "kx"));
    }

    @Test
    void aFunctionIsApplied() {
        Bindings s = Bindings.EMPTY.define("f(t) = t^2+1");
        assertEquals("5", ev(s, "f(2)"));
        assertEquals("x^2+1", ev(s, "f(x)"));
        assertEquals("x^2+1+(y^2+1)", ev(s, "f(x)+f(y)"));
        assertEquals("10", ev(s, "2f(2)"));       // and a call is an ordinary operand
    }

    /** A definition may use what was defined before it, and the expansion goes all the way down. */
    @Test
    void definitionsCompose() {
        Bindings s = Bindings.EMPTY.define("k = 3").define("f(t) = k·t");
        assertEquals("12", ev(s, "f(4)"));
        assertEquals("3x", ev(s, "f(x)"));
        // A body keeps the NAME it was written with, and looks it up when it is used. So correcting k
        // corrects everything that mentions it, which is the whole reason to write k rather than 3 --
        // and it is why the window can list f as it was typed rather than as it was folded.
        assertEquals("40", ev(s.define("k = 10"), "f(4)"));
    }

    /** A parameter shadows whatever it is spelled like, so a definition reads the same wherever it is used. */
    @Test
    void parametersShadow() {
        Bindings s = Bindings.EMPTY.define("x = 5").define("g(x) = x+1");
        assertEquals("3", ev(s, "g(2)"));
        assertEquals("6", ev(s, "x+1"));          // the free x is still the defined one
    }

    /** The names have to be words, and a word must not disturb juxtaposition. */
    @Test
    void aNameIsOneTokenOnlyOnceItIsDefined() {
        // Undefined, it is five juxtaposed letters -- and t is not one of COTT's three variables, so it is
        // the letter that is refused rather than the word. That is the old behaviour, exactly.
        assertEquals("'t' not in COTT",
                assertThrows(SyntaxException.class, () -> Cott.evaluate("theta")).getMessage());
        Bindings s = Bindings.EMPTY.define("theta = 2");
        assertEquals("2", ev(s, "theta"));
        assertEquals("4", ev(s, "2theta"));
        assertEquals("xy", ev(s, "xy"));                     // and the old rule is untouched
        // A one-letter name must not shadow a longer built-in: the scan is longest-first over the whole
        // vocabulary, not over the session's names and then the catalogue's.
        Bindings a = Bindings.EMPTY.define("a = 7");
        assertEquals("0", ev(a, "atan(0)"));
        assertEquals("7", ev(a, "a"));
    }

    @Test
    void aFunctionNeedsItsBrackets() {
        Bindings s = Bindings.EMPTY.define("f(t) = t+1");
        assertTrue(s.isFunction("f"));
        assertFalse(s.isFunction("t"));
        // A function named alone is not a value -- expanding it would put a loose parameter in the answer,
        // so it is refused where it is written rather than answered with something meaningless.
        assertEquals("f needs its argument in brackets",
                assertThrows(SyntaxException.class, () -> ev(s, "f")).getMessage());
    }

    /** A parameter written with empty brackets is a FUNCTION, and is given the name of one. */
    @Test
    void aFunctorAppliesWhatItIsGiven() {
        Bindings s = Bindings.EMPTY.define("g(t) = t^2").define("iter(f(), n) = f(f(n))");
        assertEquals("81", ev(s, "iter(g, 3)"));
        assertEquals("(x^2)^2", ev(s, "iter(g, x)"));   // g of g of x, unfolded and left as COTT leaves it
        // The built-in catalogue is passable too, and for the same reason it needs no special case: what is
        // passed is a NAME, and the lookup that resolves it is the one every other name goes through.
        assertEquals("sin(sin(x))", ev(s, "iter(sin, x)"));
        assertEquals("0", ev(s, "iter(sin, 0)"));
        // And the functor is an ordinary definition otherwise: it composes, and its own name is a word.
        assertEquals("43046721", ev(s.define("four(f(), n) = iter(f, iter(f, n))"), "four(g, 3)"));
    }

    /**
     * The brackets are not decoration. Without them the adjacency rule reads {@code f(n)} as {@code f·(n)},
     * which is a perfectly good reading of the same characters — and so exactly the thing that cannot be
     * guessed at from the body.
     */
    @Test
    void theBracketsAreWhatMakeTheBodyACall() {
        assertTrue(Bindings.EMPTY.define("iter(f(), n) = f(f(n))").isFunction("iter"));
        // The same body with f declared as a VALUE is a product, and answers as one rather than failing.
        Bindings product = Bindings.EMPTY.define("m(f, n) = f(f(n))");
        assertEquals("18", ev(product, "m(3, 2)"));
        assertEquals("m(f, n) = f·f·n", product.get("m").source());
        assertEquals("iter(f(), n) = f(f(n))",
                Bindings.EMPTY.define("iter(f(), n) = f(f(n))").get("iter").source());
    }

    /** A parameter's kind is a promise, and it is checked in both directions where the argument is bound. */
    @Test
    void aFunctorIsGivenAFunctionAndAValueIsNot() {
        Bindings s = Bindings.EMPTY.define("g(t) = t^2").define("iter(f(), n) = f(f(n))");
        assertEquals("iter's f is a function, so it is given the NAME of one -- a defined function, or one of "
                        + "the built-in ones like sin",
                assertThrows(SyntaxException.class, () -> ev(s, "iter(3, 2)")).getMessage());
        // The other way round: a bare function name where a value was wanted would otherwise ride into the
        // answer as though it were a variable, and then be plotted as one.
        assertEquals("g is a function and needs its arguments in brackets, since g's t is a value and not a "
                        + "function",
                assertThrows(SyntaxException.class, () -> ev(s, "g(g)")).getMessage());
        // The ARITY is not declared and is not checked until it is applied -- against the function that was
        // actually passed, which is the only place the number is known.
        Bindings two = s.define("pair(f(), n) = f(n, n)");
        assertEquals("g takes 1 argument, not 2",
                assertThrows(SyntaxException.class, () -> ev(two, "pair(g, 3)")).getMessage());
        assertEquals("0", ev(two, "pair(atan2, 0)"));
    }

    /** A functor makes a new way to write a ring, and it is caught by the same guard as the old one. */
    @Test
    void aFunctorCannotLoopForever() {
        Bindings s = Bindings.EMPTY.define("loop(f(), n) = f(f, n)");
        assertEquals("a definition here expands into itself",
                assertThrows(SyntaxException.class, () -> ev(s, "loop(loop, 3)")).getMessage());
    }

    /** What a slot may hold, and what it may not. */
    @Test
    void functorRefusals() {
        // The obvious guess. It is refused by name rather than torn in half by the comma first.
        assertEquals("a function parameter is written f(), with nothing between the brackets -- how many "
                        + "arguments it takes is settled by the function that is passed to it",
                assertThrows(SyntaxException.class,
                        () -> Bindings.EMPTY.define("iter(f(u), n) = f(f(n))")).getMessage());
        assertEquals("a function parameter is written f(), with nothing between the brackets -- how many "
                        + "arguments it takes is settled by the function that is passed to it",
                assertThrows(SyntaxException.class,
                        () -> Bindings.EMPTY.define("h(f(a, b), n) = n")).getMessage());
        // A name is a name wherever it stands, so a functor parameter is checked like any other.
        assertEquals("sin already means something here",
                assertThrows(SyntaxException.class, () -> Bindings.EMPTY.define("h(sin(), n) = n")).getMessage());
        // Passing is only ever the whole of a slot: elsewhere the missing brackets are still missing.
        Bindings s = Bindings.EMPTY.define("g(t) = t^2").define("iter(f(), n) = f(f(n))");
        assertEquals("g needs its argument in brackets",
                assertThrows(SyntaxException.class, () -> ev(s, "iter(g+1, 2)")).getMessage());
        // And a built-in takes values, so a name in one of its slots is the mistake it always was.
        assertEquals("g needs its argument in brackets",
                assertThrows(SyntaxException.class, () -> ev(s, "sin(g)")).getMessage());
    }

    @Test
    void forgettingANameUndoesIt() {
        Bindings s = Bindings.EMPTY.define("x = 3");
        assertEquals("3", ev(s, "x"));
        assertEquals("x", ev(s.without("x"), "x"));   // back to being COTT's own variable
        assertTrue(s.without("x").isEmpty());
    }

    /** Redefining keeps its place in the list, because the window lists them in the order they were made. */
    @Test
    void redefiningKeepsItsPlace() {
        Bindings s = Bindings.EMPTY.define("a = 1").define("b = 2").define("a = 9");
        assertEquals(java.util.List.of("a", "b"), s.all().stream().map(Bindings.Definition::name).toList());
        assertEquals("9", ev(s, "a"));
    }

    @Test
    void aDefinitionReadsBackAsItWasWritten() {
        Bindings s = Bindings.EMPTY.define("k = 3").define("f(t, u) = t·u+k");
        assertEquals("k = 3", s.get("k").source());
        assertEquals("f(t, u) = t·u+k", s.get("f").source());   // as typed, not as folded
    }

    @Test
    void refusals() {
        assertEquals("a definition is name = expression",
                assertThrows(SyntaxException.class, () -> Bindings.EMPTY.define("k 3")).getMessage());
        assertEquals("a definition needs a right-hand side",
                assertThrows(SyntaxException.class, () -> Bindings.EMPTY.define("k =")).getMessage());
        assertEquals("a name starts with a letter, so 2k is not one",
                assertThrows(SyntaxException.class, () -> Bindings.EMPTY.define("2k = 1")).getMessage());
        assertEquals("sin already means something here",
                assertThrows(SyntaxException.class, () -> Bindings.EMPTY.define("sin(t) = t")).getMessage());
        assertEquals("e already means something here",
                assertThrows(SyntaxException.class, () -> Bindings.EMPTY.define("e = 3")).getMessage());
        // A name that could be defined and then never typed again is worse than one that is refused.
        assertEquals("a w is read as ω here, so a name cannot contain one",
                assertThrows(SyntaxException.class, () -> Bindings.EMPTY.define("width = 3")).getMessage());
        // the count is checked where the promise lives, which is the definition
        Bindings s = Bindings.EMPTY.define("f(t) = t");
        assertEquals("f takes 1 argument, not 2",
                assertThrows(SyntaxException.class, () -> ev(s, "f(1, 2)")).getMessage());
    }

    /** A ring of definitions has no expansion, and running out of stack is not a way of saying so. */
    @Test
    void selfReferenceIsRefused() {
        // f cannot mention itself as it is written -- f is not yet a word -- so the ring is built by
        // redefining a name that something else already points at.
        Bindings s = Bindings.EMPTY.define("a = 1").define("b = a+1").define("a = b");
        assertEquals("a definition here expands into itself",
                assertThrows(SyntaxException.class, () -> ev(s, "a")).getMessage());
    }
}
