package sibarum.cott.parse;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.ratio.T;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The four states an entry can be in, and the two stages they answer at. */
class CatalogueTest {

    private static final double TOLERANCE = 1e-12;

    private static final Catalogue INSTALLED = Standard.CATALOGUE;

    private static Node.Lit lit(long p, long q) {
        return new Node.Lit(T.of(p, q));
    }

    private static Node exact(String text) {
        return Parse.of(text).resolve(INSTALLED).fold();
    }

    private static OptionalDouble number(String text) {
        return exact(text).projection(INSTALLED);
    }

    // ---- state one: an exact form and no projected one

    /** It simplifies, and the answer is a pair rather than a number. */
    @Test
    void anExactFormSimplifies() {
        assertEquals(lit(2, 5), exact("oplus(T(1,2),T(1,3))"));
        assertEquals(lit(5, 5), exact("otimes(T(1,2),T(1,3))"));   // (ad+bc, bd-ac)
        assertEquals(lit(-3, -4), exact("oplusInverse(T(3,4))"));
        assertEquals(lit(-3, 4), exact("otimesInverse(T(3,4))"));
        assertEquals(lit(24, 7), exact("z(T(3,4))"));
    }

    /** And it evaluates, through the pair it simplified to -- without a projected form of its own. */
    @Test
    void anExactFormEvaluatesThroughItsPair() {
        assertEquals(0.4, number("oplus(T(1,2),T(1,3))").getAsDouble(), TOLERANCE);
        assertFalse(INSTALLED.lookup("oplus").orElseThrow().projects());
    }

    /** The exact form answers at projection too, so the stage order is the caller's and not a requirement. */
    @Test
    void anExactFormProjectsWithoutHavingBeenResolvedFirst() {
        assertEquals(0.4, Parse.of("oplus(T(1,2),T(1,3))").projection(INSTALLED).getAsDouble(), TOLERANCE);
    }

    /** {@code ⊕} and {@code ⊗} have no glyph in the grammar, which is why they are installed by name. */
    @Test
    void theExponentPositionIsOnlyReachableByName() {
        assertThrows(RuntimeException.class, () -> Parse.of("T(1,2) ⊕ T(1,3)"));
        assertEquals(lit(2, 5), exact("oplus(T(1,2),T(1,3))"));
    }

    // ---- state two: a projected form and no exact one

    /** It does not simplify. The call stands, and standing is what carries it to the second stage. */
    @Test
    void aProjectedFormDoesNotSimplify() {
        assertEquals(Parse.of("theta(T(3,4))"), exact("theta(T(3,4))"));
        assertFalse(INSTALLED.lookup("theta").orElseThrow().simplifies());
        assertEquals(Math.atan(0.75), number("theta(T(3,4))").getAsDouble(), TOLERANCE);
    }

    /** It stands inside a term, and the term around it is done in doubles at the second stage. */
    @Test
    void aStandingCallIsEvaluatedInPlace() {
        double angle = Math.atan(0.75);
        assertEquals(new Node.Sum(Parse.of("theta(T(3,4))"), lit(1, 1)), exact("theta(T(3,4))+1"));
        assertEquals(angle + 1.0, number("theta(T(3,4))+1").getAsDouble(), TOLERANCE);
        assertEquals(angle * 2.0, number("theta(T(3,4))*2").getAsDouble(), TOLERANCE);
        assertEquals(-angle, number("-theta(T(3,4))").getAsDouble(), TOLERANCE);
        assertEquals(1.0 / angle, number("1/theta(T(3,4))").getAsDouble(), TOLERANCE);
    }

    /**
     * A projected form that needs the orientation reads the term, because the number beside it has already
     * lost it: these two are one projection and half a turn apart.
     */
    @Test
    void aProjectedFormMayReadTheTermRatherThanTheNumber() {
        assertEquals(-1.0, T.of(1, -1).projection());
        assertEquals(-1.0, T.of(-1, 1).projection());
        assertEquals(3 * Math.PI / 4, number("theta(T(1,-1))").getAsDouble(), TOLERANCE);
        assertEquals(-Math.PI / 4, number("theta(T(-1,1))").getAsDouble(), TOLERANCE);
    }

    // ---- tan and atan, which are exact and are one map

    /**
     * A pair is its own tangent, so there is no arithmetic for {@code tan} to do: what is left of it is
     * the branch, and the answer is a pair rather than a number.
     */
    @Test
    void tanIsExactAndIsTheBranch() {
        assertEquals(lit(3, 4), exact("tan(T(3,4))"));
        assertEquals(lit(-1, 1), exact("tan(T(1,-1))"));     // one tangent, in the half tan answers in
        assertEquals(lit(0, 1), exact("tan(T(0,-1))"));
        assertEquals(lit(1, 0), exact("tan(T(1,0))"));       // the pole is not folded
        assertTrue(INSTALLED.lookup("tan").orElseThrow().simplifies());
    }

    /** And {@code atan} is the same map, which is what the model's first line amounts to. */
    @Test
    void tanAndAtanAreOneMap() {
        for (String pair : new String[]{"T(3,4)", "T(1,-1)", "T(-1,1)", "T(-1,-1)", "T(0,-1)",
                "T(1,0)", "T(-1,0)", "T(0,0)"}) {
            assertEquals(exact("tan(" + pair + ")"), exact("atan(" + pair + ")"), pair);
        }
        assertEquals(lit(-1, 1), exact("atan(tan(T(1,-1)))"));
        assertEquals(lit(-1, 1), exact("tan(atan(T(1,-1)))"));
    }

    /** The fold keeps the tangent and moves the angle, which is the period it is folding by. */
    @Test
    void theBranchKeepsTheValueAndTurnsTheAngle() {
        assertEquals(-1.0, number("tan(T(1,-1))").getAsDouble(), TOLERANCE);
        assertEquals(-1.0, number("T(1,-1)").getAsDouble(), TOLERANCE);
        assertEquals(3 * Math.PI / 4, number("theta(T(1,-1))").getAsDouble(), TOLERANCE);
        assertEquals(-Math.PI / 4, number("theta(tan(T(1,-1)))").getAsDouble(), TOLERANCE);
    }

    // ---- and why there is no atan2

    /**
     * A quotient keeps the quadrant, so {@code y÷x} is what {@code atan2(y, x)} was for -- and at whole
     * arguments it is the pair itself, with no function called at all.
     */
    @Test
    void divisionIsWhatAtan2WasFor() {
        assertEquals(lit(1, -1), exact("1/-1"));
        assertEquals(lit(-1, 1), exact("-1/1"));
        assertEquals(lit(-1, -1), exact("-1/-1"));
        assertEquals(lit(1, 0), exact("1/0"));
        assertEquals(Math.atan2(1, -1), number("theta(1/-1)").getAsDouble(), TOLERANCE);
        assertEquals(Math.atan2(-1, 1), number("theta(-1/1)").getAsDouble(), TOLERANCE);
        assertEquals(Math.atan2(-1, -1), number("theta(-1/-1)").getAsDouble(), TOLERANCE);
        assertEquals(Math.atan2(1, 0), number("theta(1/0)").getAsDouble(), TOLERANCE);
    }

    /**
     * Which is exactly what a classical {@code atan} cannot do: applying the branch to the quotient throws
     * away the turn it came from, and the two quadrants arrive at one answer.
     */
    @Test
    void theBranchIsWhereTheQuadrantIsLost() {
        assertEquals(exact("atan(1/-1)"), exact("atan(-1/1)"));
        assertEquals(-Math.PI / 4, number("theta(atan(1/-1))").getAsDouble(), TOLERANCE);
        assertEquals(-Math.PI / 4, number("theta(atan(-1/1))").getAsDouble(), TOLERANCE);
        // without it, they are two values half a turn apart
        assertNotEquals(exact("1/-1"), exact("-1/1"));
    }

    /** The one case where the two part company: IEEE answers an angle at the origin and this does not. */
    @Test
    void theOriginIsTheOneCaseAtan2Invents() {
        assertEquals(lit(0, 0), exact("0/0"));
        assertTrue(Double.isNaN(number("theta(0/0)").getAsDouble()));
        assertEquals(0.0, Math.atan2(0.0, 0.0));
    }

    // ---- state three: both forms

    /** The exact form where the exponent is a whole number and the pair still fits. */
    @Test
    void bothFormsSimplifyWhereTheExactOneCan() {
        assertEquals(new Node.Lit(T.of(3, 4).otimesPower(3)), exact("otimesPower(T(3,4),3)"));
        assertEquals(lit(24, 7), exact("otimesPower(T(3,4),2)"));
        assertEquals(lit(0, 1), exact("otimesPower(T(3,4),0)"));
    }

    /**
     * And the projected form everywhere else -- including the exponents where no pair stands at all, which
     * is the model's general angle scaling arriving as far as it can.
     */
    @Test
    void bothFormsDeferToTheProjectedOneWhereNoPairStands() {
        assertEquals(Parse.of("otimesPower(T(3,4),1.5)"), exact("otimesPower(T(3,4),1.5)"));
        assertEquals(Math.tan(1.5 * T.of(3, 4).theta()),
                number("otimesPower(T(3,4),1.5)").getAsDouble(), TOLERANCE);
    }

    /**
     * A power too wide to hold is not built, which is the reason this entry has two forms: the exact one
     * declines and the term stands, and the projected one still answers.
     */
    @Test
    void bothFormsMeanAWidePowerStillHasANumber() {
        String wide = "otimesPower(T(3,4),1000000000)";
        assertEquals(Parse.of(wide), exact(wide));
        assertTrue(number(wide).isPresent());
    }

    /** At the second stage the projected form is preferred, so the exact pair is not built to get a number. */
    @Test
    void theProjectedFormIsTheOneUsedAtProjection() {
        assertEquals(Math.tan(3 * T.of(3, 4).theta()),
                Parse.of("otimesPower(T(3,4),3)").projection(INSTALLED).getAsDouble(), TOLERANCE);
    }

    // ---- state four: neither form

    /** A declared name that always stands. The caller substitutes it; the catalogue only knows it exists. */
    @Test
    void aDeferredEntryStandsAtBothStages() {
        Catalogue session = Catalogue.of(Catalogue.Entry.deferred("f", 1));
        Node term = Parse.of("f(2)+1");
        assertEquals(term, term.resolve(session).fold());
        assertEquals(OptionalDouble.empty(), term.projection(session));
        assertTrue(session.names().contains("f"));
        Catalogue.Entry entry = session.lookup("f").orElseThrow();
        assertFalse(entry.simplifies());
        assertFalse(entry.projects());
    }

    /** Substituting the name is the caller's business, and after it the ordinary stages apply. */
    @Test
    void aDeferredNameIsTheCallersToSubstitute() {
        Catalogue session = Catalogue.of(Catalogue.Entry.deferred("f", 1));
        Node term = Parse.of("f+1").substitute(Map.of("f", lit(2, 1)));
        assertEquals(lit(3, 1), term.resolve(session).fold());
    }

    // ---- the pair, where its coordinates arrived as terms

    /**
     * {@code T(x,1)} is a call until something is bound to {@code x}, and then it is the pair -- which
     * nothing in the layer could do before, since the parser only builds one from coordinates it can see.
     */
    @Test
    void thePairFormsOnceItsCoordinatesAreNumbers() {
        assertEquals(new Node.Call("T", java.util.List.of(new Node.Var("x"), lit(1, 1))), Parse.of("T(x,1)"));
        assertEquals(lit(2, 1), Parse.of("T(x,1)").evaluate(Map.of("x", lit(2, 1)), INSTALLED));
        assertEquals(lit(1, 0), Parse.of("T(1,x)").evaluate(Map.of("x", lit(0, 1)), INSTALLED));
        assertEquals(lit(0, 0), Parse.of("T(x,x)").evaluate(Map.of("x", lit(0, 1)), INSTALLED));
    }

    /** A coordinate is an integer, so a ratio in that position is declined and the call stands. */
    @Test
    void aCoordinateThatIsNotWholeIsDeclined() {
        assertEquals(Parse.of("T(T(1,2),1)"), exact("T(T(1,2),1)"));
        assertEquals(Parse.of("T(0.5,1)"), exact("T(0.5,1)"));
    }

    // ---- arity

    /** The grammar accepts any number of arguments; an entry answers at one and declines at the rest. */
    @Test
    void aCallAtAnotherArityStands() {
        assertEquals(Parse.of("tan(1,2)"), exact("tan(1,2)"));
        assertEquals(OptionalDouble.empty(), number("tan(1,2)"));
        assertEquals(Parse.of("oplus(T(1,2))"), exact("oplus(T(1,2))"));
        assertEquals(OptionalDouble.empty(), number("oplus(T(1,2))"));
    }

    // ---- what the caller adds

    /** The installed catalogue is expanded by composition, and the side written first wins a shared name. */
    @Test
    void theCallerAddsToTheInstalledNames() {
        Catalogue mine = Catalogue.of(
                Catalogue.Entry.projected("half", 1, (a, p) -> OptionalDouble.of(p[0] / 2)),
                Catalogue.Entry.projected("tan", 1, (a, p) -> OptionalDouble.of(0.0)));

        Catalogue callerWins = mine.or(INSTALLED);
        assertEquals(1.5, Parse.of("half(3)").projection(callerWins).getAsDouble(), TOLERANCE);
        assertEquals(0.0, Parse.of("tan(1)").projection(callerWins).getAsDouble(), TOLERANCE);

        // an override may change what state a name is in: installed, tan is exact and answers 1÷1
        Catalogue installedWins = INSTALLED.or(mine);
        assertEquals(1.5, Parse.of("half(3)").projection(installedWins).getAsDouble(), TOLERANCE);
        assertEquals(1.0, Parse.of("tan(1)").projection(installedWins).getAsDouble(), TOLERANCE);
        assertEquals(lit(1, 1), Parse.of("tan(1)").resolve(installedWins).fold());
    }

    @Test
    void theNamesAreBothCataloguesNames() {
        Catalogue mine = Catalogue.of(Catalogue.Entry.deferred("f", 1));
        assertTrue(mine.or(INSTALLED).names().containsAll(INSTALLED.names()));
        assertTrue(mine.or(INSTALLED).names().contains("f"));
        assertEquals(INSTALLED.names().size() + 1, mine.or(INSTALLED).names().size());
    }

    /** A caller's own exact form is resolved by the same seam the installed ones use. */
    @Test
    void aCallersExactFormIsResolvedLikeAnyOther() {
        Catalogue mine = Catalogue.of(Catalogue.Entry.exact("twice", 1, arguments ->
                arguments.getFirst().literal().<Node>map(t -> new Node.Lit(t.plus(t)))));
        assertEquals(lit(4, 1), Parse.of("twice(2)").resolve(mine).fold());
        assertEquals(Optional.of(lit(4, 1)), mine.apply("twice", java.util.List.of(lit(2, 1))));
    }

    /** Two entries for one name is a table that is wrong, not a precedence to be guessed. */
    @Test
    void oneNameCannotHaveTwoEntries() {
        assertThrows(IllegalArgumentException.class, () -> Catalogue.of(
                Catalogue.Entry.deferred("f", 1),
                Catalogue.Entry.deferred("f", 2)));
    }

    @Test
    void anEmptyCatalogueAnswersNothing() {
        assertTrue(Catalogue.EMPTY.names().isEmpty());
        assertEquals(Parse.of("tan(1)"), Parse.of("tan(1)").resolve(Catalogue.EMPTY).fold());
        assertEquals(OptionalDouble.empty(), Parse.of("tan(1)").projection(Catalogue.EMPTY));
    }

    // ---- the projection walk itself

    /**
     * A literal answers by the model table's column, so the lossy answers are answers: the signed zero, the
     * two infinities, and the origin's NaN all arrive rather than being declined.
     */
    @Test
    void aLiteralProjectsByTheTableColumn() {
        assertEquals(0.75, number("T(3,4)").getAsDouble(), TOLERANCE);
        assertEquals(Double.POSITIVE_INFINITY, number("T(1,0)").getAsDouble());
        assertEquals(Double.NEGATIVE_INFINITY, number("T(-1,0)").getAsDouble());
        assertEquals(-1, Math.copySign(1, number("T(0,-1)").getAsDouble()));
        assertTrue(Double.isNaN(number("T(0,0)").getAsDouble()));
    }

    /** Empty is the other thing: not a lossy number but no number at all. */
    @Test
    void aTermThatIsNotOneNumberDeclines() {
        assertEquals(OptionalDouble.empty(), number("x"));
        assertEquals(OptionalDouble.empty(), number("x+1"));
        assertEquals(OptionalDouble.empty(), number("nosuchname(1)"));
        assertEquals(OptionalDouble.empty(), number("tan(x)"));
    }

    /** Bound, it is a number again. */
    @Test
    void aBoundNameProjects() {
        assertEquals(3.0, Parse.of("x+1").substitute(Map.of("x", lit(2, 1)))
                .projection(INSTALLED).getAsDouble(), TOLERANCE);
    }
}
