package sibarum.cott.parse;

import org.junit.jupiter.api.Test;
import sibarum.cott.SyntaxException;
import sibarum.cott.engine.ratio.T;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParseTest {

    private static Node.Lit lit(long p, long q) {
        return new Node.Lit(T.of(p, q));
    }

    private static Node.Var v(String name) {
        return new Node.Var(name);
    }

    // ---- leaves

    @Test
    void everyNumberIsARatio() {
        assertEquals(lit(2, 1), Parse.of("2"));
        assertEquals(lit(0, 1), Parse.of("0"));
        assertEquals(lit(120, 1), Parse.of("120"));
    }

    /** The denominator is the power of ten the digits asked for, and it is not reduced. */
    @Test
    void aDecimalIsTheRatioItsDigitsSpell() {
        assertEquals(lit(325, 100), Parse.of("3.25"));
        assertEquals(lit(5, 10), Parse.of("0.5"));
    }

    @Test
    void aPairIsWrittenTheWayTheTypeWritesIt() {
        assertEquals(lit(1, 0), Parse.of("T(1,0)"));
        assertEquals(lit(-1, 0), Parse.of("T(-1,0)"));
        assertEquals(lit(0, -1), Parse.of("T(0,-1)"));
    }

    /** So {@code T} is not a reserved word: with anything but coordinates in it, it is a call. */
    @Test
    void aPairWithANameInItStaysACall() {
        assertEquals(new Node.Call("T", List.of(v("x"), lit(1, 1))), Parse.of("T(x,1)"));
        assertEquals(v("T"), Parse.of("T"));
    }

    // ---- shape

    @Test
    void multiplicationBindsTighterThanAddition() {
        assertEquals(new Node.Sum(lit(1, 1), new Node.Product(lit(2, 1), lit(3, 1))),
                Parse.of("1+2*3"));
    }

    @Test
    void thePowerBindsTighterThanMultiplication() {
        assertEquals(new Node.Product(lit(2, 1), new Node.Power(lit(3, 1), lit(2, 1))),
                Parse.of("2*3^2"));
    }

    @Test
    void thePowerAssociatesToTheRight() {
        assertEquals(new Node.Power(lit(2, 1), new Node.Power(lit(3, 1), lit(2, 1))),
                Parse.of("2^3^2"));
    }

    /** Below the power and above the product, which is what both spellings are read to mean. */
    @Test
    void unaryMinusSitsBetweenThem() {
        assertEquals(new Node.Negation(new Node.Power(v("x"), lit(2, 1))), Parse.of("-x^2"));
        assertEquals(new Node.Product(new Node.Negation(v("x")), v("y")), Parse.of("-x*y"));
        assertEquals(new Node.Power(lit(2, 1), new Node.Negation(lit(1, 1))), Parse.of("2^-1"));
    }

    /** Subtraction and division are not nodes; they are the two inverses. */
    @Test
    void aDifferenceIsASumOfANegation() {
        assertEquals(new Node.Sum(v("a"), new Node.Negation(v("b"))), Parse.of("a-b"));
        assertEquals(new Node.Product(v("a"), new Node.Reciprocal(v("b"))), Parse.of("a/b"));
    }

    @Test
    void theDisplayGlyphsReadTheSame() {
        assertEquals(Parse.of("a*b/c-d"), Parse.of("a · b ÷ c − d"));
    }

    /** A term arrives with its shape intact; the arithmetic happens when it is asked for. */
    @Test
    void nothingIsFoldedOnTheWayIn() {
        assertEquals(new Node.Sum(lit(1, 1), lit(1, 1)), Parse.of("1+1"));
        assertInstanceOf(Node.Sum.class, Parse.of("1+1"));
        assertEquals(lit(2, 1), Parse.of("1+1").fold());
    }

    // ---- names and calls

    @Test
    void anUnknownNameOrCallIsAnOrdinaryTerm() {
        assertEquals(new Node.Sum(new Node.Call("f", List.of(v("x"))), lit(1, 1)),
                Parse.of("f(x)+1"));
        assertEquals(new Node.Call("log", List.of(lit(2, 1), v("x"))), Parse.of("log(2, x)"));
        assertEquals(new Node.Call("now", List.of()), Parse.of("now()"));
    }

    /** Greek is in, and the parser binds nothing: omega is a name until something says what it is. */
    @Test
    void aNameMayBeSpelledTheWayTheTheorySpellsIt() {
        assertEquals(new Node.Product(v("ω"), lit(2, 1)), Parse.of("ω*2"));
        assertEquals(lit(2, 0),
                Parse.of("ω*2").substitute(Map.of("ω", lit(1, 0))).fold());
    }

    // ---- substitution

    @Test
    void substitutionPutsNodesInForNames() {
        assertEquals(lit(3, 1), Parse.of("x+1").substitute(Map.of("x", lit(2, 1))).fold());
        assertEquals(new Node.Sum(lit(2, 1), v("y")),
                Parse.of("x+y").substitute(Map.of("x", lit(2, 1))));
    }

    @Test
    void substitutionAcceptsATermAndNotJustAValue() {
        assertEquals(Parse.of("(a+b)*2"),
                Parse.of("x*2").substitute(Map.of("x", Parse.of("(a+b)"))));
    }

    // ---- external functions

    private static final Functions SQUARES = Functions.of(Map.of(
            "sq", args -> args.getFirst().literal()
                    .<Node>map(t -> new Node.Lit(t.times(t)))
                    .orElse(null)));

    @Test
    void aResolvedCallBecomesWhatTheResolverSays() {
        assertEquals(lit(9, 1), Parse.of("sq(3)").resolve(SQUARES).fold());
        assertEquals(lit(19, 2), Parse.of("sq(3)+T(1,2)").evaluate(Map.of(), SQUARES));
    }

    /** Declining is an answer: the call stands, and so does everything holding it. */
    @Test
    void anUnresolvedCallStands() {
        assertEquals(Parse.of("sq(x)"), Parse.of("sq(x)").resolve(SQUARES));
        assertEquals(Parse.of("sq(3)"), Parse.of("sq(3)").resolve(Functions.NONE));
    }

    @Test
    void argumentsAreResolvedBeforeTheCallHoldingThem() {
        assertEquals(lit(81, 1), Parse.of("sq(sq(3))").resolve(SQUARES).fold());
    }

    @Test
    void aSubstitutedNameReachesTheResolver() {
        assertEquals(lit(4, 1),
                Parse.of("sq(x)").evaluate(Map.of("x", lit(2, 1)), SQUARES));
    }

    // ---- folding

    @Test
    void foldingIsTheModelsArithmetic() {
        assertEquals(lit(5, 6), Parse.of("T(1,2)+T(1,3)").fold());
        assertEquals(lit(3, 8), Parse.of("T(1,2)*T(3,4)").fold());
        assertEquals(lit(8, 27), Parse.of("T(2,3)^3").fold());
    }

    /** The reciprocal swaps the coordinates, which is what makes zero invertible. */
    @Test
    void oneOverZeroIsOmega() {
        assertEquals(lit(1, 0), Parse.of("1/0").fold());
        assertEquals(lit(0, 1), Parse.of("1/T(1,0)").fold());
    }

    /** As the law is written: every cross term picks up the zero denominator. */
    @Test
    void omegaPlusOmegaLandsOnTheOriginPair() {
        assertEquals(lit(0, 0), Parse.of("T(1,0)+T(1,0)").fold());
    }

    /** Not a pair of integers, so the term stands rather than being reduced into one. */
    @Test
    void aNegativePowerStands() {
        assertEquals(new Node.Power(lit(2, 3), lit(-1, 1)), Parse.of("T(2,3)^-1").fold());
    }

    /**
     * A power too wide to hold stands, which is the same answer a negative exponent gets and means the same
     * thing: the arithmetic is not wrong, it is not done.
     *
     * <p>The cost of a power is not bounded by the length of what was typed. Twelve characters asked for a
     * coordinate of two billion bits, and the wait ended in an {@code OutOfMemoryError} -- which a caller
     * guarding a text field does not catch, because it is not a {@code RuntimeException}.
     */
    @Test
    void aPowerTooWideToHoldStands() {
        assertEquals(new Node.Power(lit(2, 1), lit(2000000000, 1)), Parse.of("2^2000000000").fold());
        assertEquals(new Node.Power(lit(10, 1), lit(90000000, 1)), Parse.of("10^90000000").fold());
        // and the ordinary ones still fold, including ones that are merely large
        assertEquals(lit(1024, 1), Parse.of("2^10").fold());
        assertEquals(BigInteger.TWO.pow(1000000),
                Parse.of("2^1000000").fold().literal().orElseThrow().p());
    }

    /**
     * The one fold the model does not state, so it is the caller's switch.
     *
     * <p>Under {@link Folding#ORDINARY} the numerator turns, which is the table's {@code T(-a,b)} -- and at
     * the point zero that comes back where it started, so the table's {@code -0} is not reachable this way.
     * {@link Folding#STANDING} places no sign at all.
     */
    @Test
    void negationIsTheCallersChoice() {
        assertEquals(lit(-2, 3), Parse.of("-T(2,3)").fold(Folding.ORDINARY));
        assertEquals(new Node.Negation(lit(2, 3)), Parse.of("-T(2,3)").fold(Folding.STANDING));
        assertEquals(lit(0, 1), Parse.of("-T(0,1)").fold(Folding.ORDINARY));
        assertEquals(lit(0, -1), Parse.of("T(0,-1)").fold(Folding.ORDINARY));
    }

    /** Folding combines a literal with a literal and stops. No identity is applied to anything. */
    @Test
    void foldingAppliesNoIdentities() {
        assertEquals(Parse.of("x*1"), Parse.of("x*1").fold());
        assertEquals(Parse.of("x+0"), Parse.of("x+0").fold());
        assertEquals(Parse.of("x/x"), Parse.of("x/x").fold());
        // and it does reach inside a term that is still open
        assertEquals(new Node.Sum(v("x"), lit(2, 1)), Parse.of("x+(1+1)").fold());
    }

    // ---- rendering

    @Test
    void aTermPrintsBackAsSomethingThatReadsTheSame() {
        List<String> written = List.of(
                "1+2*3", "2^3^2", "-x^2", "a-b", "a/b", "f(x)+1", "T(1,0)*x",
                "(a+b)*(c-d)", "log(2, x)/2", "-(a+b)", "2^-1", "x*y+z*w",
                // the groupings a rendering must keep -- these operations do associate on the coordinates,
                // so the value is the same either way, but the TERM is not, and the term is what is read back
                "a+(b+c)", "a-(b-c)", "a*(b*c)", "a/(b/c)", "(a^b)^c", "-(a*b)",
                "x^(a*b)", "(a+b)^2", "(-a)^2", "1/(a*b)", "a-(b+c)", "2*(3+4)");
        for (String text : written) {
            Node once = Parse.of(text);
            assertEquals(once, Parse.of(once.show()), text + "  ->  " + once.show());
        }
    }

    @Test
    void theRenderingUsesTheDisplayGlyphs() {
        assertEquals("T(1,1) + T(2,1) · T(3,1)", Parse.of("1+2*3").show());
        assertEquals("a − b", Parse.of("a-b").show());
        assertEquals("a ÷ b", Parse.of("a/b").show());
        assertEquals("(a + b) · c", Parse.of("(a+b)*c").show());
        assertEquals("−x^T(2,1)", Parse.of("-x^2").show());
        // bracketed to keep the tree: a + b + c reads back left-nested, which is a different term
        assertEquals("a + (b + c)", Parse.of("a+(b+c)").show());
        assertEquals("a + b + c", Parse.of("a+b+c").show());
    }

    // ---- structure

    @Test
    void theChildrenAreTheOperandsInOrder() {
        assertEquals(List.of(v("a"), v("b")), Parse.of("a+b").children());
        assertEquals(List.of(), Parse.of("x").children());
        assertEquals(List.of(lit(2, 1), v("x")), Parse.of("log(2, x)").children());
    }

    @Test
    void aLiteralReportsItsRatioAndNothingElseDoes() {
        assertEquals(Optional.of(T.of(2, 1)), Parse.of("2").literal());
        assertEquals(Optional.empty(), Parse.of("x").literal());
    }

    // ---- what it refuses

    @Test
    void whatItCannotReadItSaysSoAbout() {
        assertThrows(SyntaxException.class, () -> Parse.of("1+"));
        assertThrows(SyntaxException.class, () -> Parse.of("1+*2"));
        assertThrows(SyntaxException.class, () -> Parse.of("(1+2"));
        assertThrows(SyntaxException.class, () -> Parse.of("1 $ 2"));
    }

    /**
     * There is no juxtaposition rule. A run of letters is one name, so {@code xy} is the name {@code xy} and
     * not a product, and {@code 2x} is not an expression at all. Write the operator.
     */
    @Test
    void thereIsNoJuxtaposition() {
        assertEquals(v("xy"), Parse.of("xy"));
        assertThrows(SyntaxException.class, () -> Parse.of("2x"));
        assertEquals(new Node.Product(lit(2, 1), v("x")), Parse.of("2*x"));
        assertEquals(new Node.Product(v("x"), v("y")), Parse.of("x*y"));
    }
}
