package sibarum.cott;

import org.junit.jupiter.api.Test;
import sibarum.cott.Term.AWind;
import sibarum.cott.Term.Approx;
import sibarum.cott.Term.Atom;
import sibarum.cott.Term.Div;
import sibarum.cott.Term.Inv;
import sibarum.cott.Term.Lg;
import sibarum.cott.Term.Logb;
import sibarum.cott.Term.Neg;
import sibarum.cott.Term.Plus;
import sibarum.cott.Term.Pow;
import sibarum.cott.Term.Pt;
import sibarum.cott.Term.Times;
import sibarum.cott.Term.Val;
import sibarum.cott.Term.Wind;
import sibarum.cott.Term.Xp;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * The COTT-ONE semantics, ported case for case from the equational specification this evaluator
 * replaces. These are the recorded expected answers, and they are the reason the port is checkable:
 * the theory has been revised often enough that "it still compiles" is worth nothing.
 *
 * <p>Two differences from the recorded answers are deliberate and neither is semantic. Points are
 * always {@code pt} forms, because the named constants only ever existed so that equational matching
 * could see them literally — direct evaluation matches on the value. And the argument order inside a
 * sum or product is this evaluator's canonical order rather than the rewriting engine's internal
 * one; the operators are associative and commutative, so there was never an order to preserve.
 */
class CottTest {

    private static final String ONE = "pt(1, xp(0, 0, 0))";
    private static final String ZERO = "pt(1, xp(1, 0, 0))";
    private static final String OMEGA = "pt(1, xp(-1, 0, 0))";
    private static final String MINUS_ONE = "pt(1, xp(0, 1, 0))";
    private static final String IU = "pt(1, xp(0, 1/2, 0))";
    private static final String TWO = "pt(2, xp(0, 0, 0))";

    private static final Val X = new Atom("x");
    private static final Val Y = new Atom("y");
    private static final Val N = new Atom("n");

    private static Pt num(long k) {
        return Term.number(Rational.of(k));
    }

    private static Val times(Val... a) {
        return new Times(List.of(a));
    }

    private static Val plus(Val... a) {
        return new Plus(List.of(a));
    }

    private static String red(Term t) {
        return Cott.reduce(t).toString();
    }

    // ------------------------------------------------------------ the residue table

    /** Row zero: erases at home, PLAIN residues abroad. */
    @Test
    void rowZero() {
        assertEquals("y", red(times(Y, new Div(Term.ZERO, Term.ZERO))));
        assertEquals("plus(" + ONE + ", y)", red(plus(Y, new Div(Term.ZERO, Term.ZERO))));
        assertEquals("times(" + ZERO + ", y)",
                red(times(Y, plus(Term.ZERO, new Neg(Term.ZERO)))));
        assertEquals("y", red(plus(Y, plus(Term.ZERO, new Neg(Term.ZERO)))));
    }

    /** Row one: erases at home, and WINDS abroad — one over one is 1^1, not 1. */
    @Test
    void rowOne() {
        assertEquals("y", red(times(Y, new Div(Term.ONE, Term.ONE))));
        assertEquals("plus(y, wind(" + ONE + "))", red(plus(Y, new Div(Term.ONE, Term.ONE))));
        assertEquals("times(y, awind(" + ONE + "))",
                red(times(Y, plus(Term.ONE, new Neg(Term.ONE)))));
        assertEquals("y", red(plus(Y, plus(Term.ONE, new Neg(Term.ONE)))));
    }

    /** Row omega, which behaves exactly as row one — both are in the closure set. */
    @Test
    void rowOmega() {
        assertEquals("y", red(times(Y, new Div(Term.OMEGA, Term.OMEGA))));
        assertEquals("plus(y, wind(" + OMEGA + "))", red(plus(Y, new Div(Term.OMEGA, Term.OMEGA))));
        assertEquals("times(y, awind(" + OMEGA + "))",
                red(times(Y, plus(Term.OMEGA, new Neg(Term.OMEGA)))));
        assertEquals("y", red(plus(Y, plus(Term.OMEGA, new Neg(Term.OMEGA)))));
    }

    /** Row n on an atom: outside the closure set nothing erases, in either context. */
    @Test
    void rowAtom() {
        assertEquals("times(y, wind(n))", red(times(Y, new Div(N, N))));
        assertEquals("plus(y, wind(n))", red(plus(Y, new Div(N, N))));
        assertEquals("times(y, awind(n))", red(times(Y, plus(N, new Neg(N)))));
        assertEquals("plus(y, awind(n))", red(plus(Y, plus(N, new Neg(N)))));
    }

    /**
     * Row n on a NUMERAL, which is a point rather than an opaque atom. The residue still has to name
     * the operand it came from: 2 minus 2 is 0^2, not 0^1. That is why collection is forbidden from
     * producing a multiplicity of zero — zero copies of a point has no room to say which point.
     */
    @Test
    void rowNumeral() {
        assertEquals("times(y, wind(" + TWO + "))", red(times(Y, new Div(num(2), num(2)))));
        assertEquals("plus(y, wind(" + TWO + "))", red(plus(Y, new Div(num(2), num(2)))));
        assertEquals("times(y, awind(" + TWO + "))", red(times(Y, plus(num(2), new Neg(num(2))))));
        assertEquals("plus(y, awind(" + TWO + "))", red(plus(Y, plus(num(2), new Neg(num(2))))));
        // and the residues on different numerals stay different objects
        assertNotEquals(new AWind(num(2)), new AWind(num(3)));
        assertNotEquals(new AWind(num(2)), new AWind(Term.ONE));
    }

    // ------------------------------------------------------------ arithmetic

    /** Numeral arithmetic, which only the graded carrier could do before. */
    @Test
    void numeralsAreValues() {
        assertEquals("pt(6, xp(0, 0, 0))", red(times(num(2), num(3))));
        assertEquals(TWO, red(plus(Term.ONE, Term.ONE)));
        assertEquals("pt(1/3, xp(0, 0, 0))", red(new Div(Term.ONE, num(3))));
        // 2/0 is 2ω, and -(ω*3) + 2/0 is 2ω − 3ω = −ω
        assertEquals("pt(2, xp(-1, 0, 0))", red(new Div(num(2), Term.ZERO)));
        assertEquals("pt(-1, xp(-1, 0, 0))",
                red(plus(new Neg(times(Term.OMEGA, num(3))), new Div(num(2), Term.ZERO))));
        assertEquals("pt(5, xp(-1, 0, 0))",
                red(plus(times(Term.OMEGA, num(2)), times(Term.OMEGA, num(3)))));
    }

    /** The exponential laws, for any base, with no exclusion for zero. */
    @Test
    void exponentialLaws() {
        assertEquals("pt(1, xp(-2, 0, 0))", red(new Pow(Term.OMEGA, num(2))));
        assertEquals("pt(1, xp(-3, 0, 0))", red(times(new Pow(Term.OMEGA, num(2)), Term.OMEGA)));
        assertEquals("pt(1, xp(-3, 0, 0))", red(new Div(new Pow(Term.OMEGA, num(2)), Term.ZERO)));
        assertEquals("pt(1, xp(-6, 0, 0))", red(new Pow(new Pow(Term.OMEGA, num(2)), num(3))));
        assertEquals(ONE, red(new Pow(Term.ZERO, Term.ZERO)));
        assertEquals(ONE, red(new Inv(Term.ONE)));
        assertEquals(ONE, red(times(Term.MINUS_ONE, Term.MINUS_ONE)));
        assertEquals("pt(8, xp(0, 0, 0))", red(new Pow(num(2), num(3))));
    }

    /** Omega is the twist unit in an exponent, and the grade minus one as a point. */
    @Test
    void theTwoReadingsOfOmega() {
        assertEquals(ONE, red(new Pow(Term.ZERO, times(Term.OMEGA, num(2)))));   // twist closes at 2
        assertEquals("pt(1, xp(0, 1/5, 0))",
                red(new Pow(Term.ZERO, new Div(Term.OMEGA, num(5)))));
        assertEquals(MINUS_ONE, red(new Pow(Term.OMEGA, Term.OMEGA)));
        assertEquals(MINUS_ONE, red(new Pow(Term.ZERO, Term.OMEGA)));
        assertEquals("pt(1, xp(-1/2, 0, 0))",
                red(new Pow(Term.ZERO, new Div(new Neg(Term.ONE), num(2)))));
        // and the whole reported expression: 0^(2ω) · 0^(ω/5) + 0^(-1/2)
        assertEquals("plus(pt(1, xp(-1/2, 0, 0)), pt(1, xp(0, 1/5, 0)))",
                red(plus(times(new Pow(Term.ZERO, times(Term.OMEGA, num(2))),
                                new Pow(Term.ZERO, new Div(Term.OMEGA, num(5)))),
                        new Pow(Term.ZERO, new Div(new Neg(Term.ONE), num(2))))));
    }

    /** i is derived, not adjoined, and its powers cycle. */
    @Test
    void imaginaryUnitIsDerived() {
        assertEquals(IU, red(new Pow(Term.MINUS_ONE, new Div(Term.ONE, num(2)))));
        assertEquals(MINUS_ONE, red(new Pow(Term.IU, num(2))));
        assertEquals(ONE, red(new Pow(Term.IU, num(4))));
        assertEquals(MINUS_ONE, red(times(Term.IU, Term.IU)));
    }

    // ------------------------------------------------------------ logarithms

    /** The log table. lg is log base 0, and it is its own inverse. */
    @Test
    void logTable() {
        assertEquals("xp(1, 0, 0)", red(new Lg(Term.ZERO)));
        assertEquals("xp(0, 0, 0)", red(new Lg(Term.ONE)));
        assertEquals("xp(0, 1, 0)", red(new Lg(Term.MINUS_ONE)));
        assertEquals("xp(-1, 0, 0)", red(new Lg(Term.OMEGA)));
        assertEquals("xp(0, 1/2, 0)", red(new Lg(Term.IU)));
        // log to any base that is itself a point, and the log of the base is one
        assertEquals("xp(1, 0, 0)", red(new Logb(Term.OMEGA, Term.OMEGA)));
        assertEquals("xp(-1, 0, 0)", red(new Logb(Term.ZERO, Term.OMEGA)));
    }

    /** The sum law is a theorem here, not an axiom: log_b x + log_b y is log_b (xy). */
    @Test
    void theLogSumLawFollows() {
        Xp a = (Xp) Cott.reduce(new Logb(Term.OMEGA, Term.ZERO));
        Xp b = (Xp) Cott.reduce(new Logb(Term.OMEGA, Term.MINUS_ONE));
        Term product = Cott.reduce(new Logb(Term.OMEGA, times(Term.ZERO, Term.MINUS_ONE)));
        assertEquals(product, a.add(b));
    }

    /** The log of 2ω is not an exponent, so only a multiplicity of one has one. */
    @Test
    void onlyUnitMultiplicitiesHaveALog() {
        assertEquals("lg(pt(2, xp(-1, 0, 0)))", red(new Lg(times(Term.OMEGA, num(2)))));
        assertEquals("lg(x)", red(new Lg(X)));
        // base one is 0^0 and has no log at all
        assertEquals("logb(" + ONE + ", " + OMEGA + ")", red(new Logb(Term.ONE, Term.OMEGA)));
    }

    // ------------------------------------------------------------ what stays standing

    /** Pi and e have no exponential form and are not derivable, so they do not reduce. */
    @Test
    void piAndEAreParked() {
        assertEquals("π", red(new Atom("π")));
        assertEquals("e", red(new Atom("e")));
        assertEquals("times(e, π)", red(times(new Atom("π"), new Atom("e"))));
    }

    /** The user law that 0·ω IS 0/0, outranking the exponential law that would give 1. */
    @Test
    void residueOutranksTheLaw() {
        assertEquals("wind(" + ZERO + ")", red(times(Term.ZERO, Term.OMEGA)));
        assertEquals("x", red(times(Term.ZERO, Term.OMEGA, X)));
        assertEquals("wind(" + ONE + ")", red(new Div(Term.ONE, Term.ONE)));
        // and approx recovers the coarse answer
        assertEquals(ONE, red(new Approx(new Div(Term.ONE, Term.ONE))));
        assertEquals(ONE, red(new Approx(new Wind(N))));
        assertEquals(ZERO, red(new Approx(new AWind(N))));
        // minus zero is recorded in the projection, because -0 = 0-0 cannot be oriented as a rule
        assertEquals(ZERO, red(new Approx(new Neg(Term.ZERO))));
    }

    /** Atoms have neither a point nor an exponent reading, so they stay opaque in both slots. */
    @Test
    void atomsStayOpaque() {
        assertEquals("times(x, y)", red(times(X, Y)));
        assertEquals("wind(x)", red(new Div(X, X)));
        assertEquals("pow(x, " + TWO + ")", red(new Pow(X, num(2))));
        assertEquals("pow(" + ZERO + ", x)", red(new Pow(Term.ZERO, X)));
        // and twist times twist is still the omega-squared floor
        assertEquals("pow(" + MINUS_ONE + ", " + OMEGA + ")",
                red(new Pow(Term.MINUS_ONE, Term.OMEGA)));
    }

    /** Unlike exponents have no definite sum, so they are not rewritten. */
    @Test
    void unlikeTermsStayFormal() {
        Pt a = new Pt(Rational.ONE, new Xp(Rational.of(-1), Rational.ONE, Rational.ZERO));
        Pt b = new Pt(Rational.ONE, Xp.grade(Rational.of(-3)));
        assertEquals("plus(pt(1, xp(-1, 1, 0)), pt(1, xp(-3, 0, 0)))", red(plus(a, b)));
        assertEquals("plus(" + ONE + ", " + ZERO + ")", red(plus(Term.ONE, Term.ZERO)));
    }

    /**
     * The exponent slot is a multiplicative context, so the closure set erases there too, while an
     * additive identity in it splits into a quotient and keeps its winding.
     */
    @Test
    void theExponentSlotIsMultiplicative() {
        assertEquals("x", red(new Pow(X, new Div(Term.ZERO, Term.ZERO))));
        assertEquals("x", red(new Pow(X, new Div(Term.OMEGA, Term.OMEGA))));
        assertEquals("wind(pow(x, n))", red(new Pow(X, plus(N, new Neg(N)))));
    }

    /** The twist and torsion closures are constructor invariants, not rules that have to fire. */
    @Test
    void closuresAreInvariants() {
        assertEquals("xp(0, 0, 0)", new Xp(Rational.ZERO, Rational.TWO, Rational.ZERO).toString());
        assertEquals("xp(0, 1, 0)", new Xp(Rational.ZERO, Rational.of(-1), Rational.ZERO).toString());
        assertEquals("xp(0, 1/2, 0)",
                new Xp(Rational.ZERO, Rational.of(5, 2), Rational.ZERO).toString());
        assertEquals("xp(0, 0, 1/3)",
                new Xp(Rational.ZERO, Rational.ZERO, Rational.of(4, 3)).toString());
    }
}
