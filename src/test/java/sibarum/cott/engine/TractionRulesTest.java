package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.AtomExpr;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.rule.Rewrite;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.ExponentialOperationExpr;
import sibarum.cott.engine.operation.binary.LogarithmOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;
import sibarum.cott.engine.traction.rule.TractionRules;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.NEG_ONE;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.OMEGA;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.ONE;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.ZERO;

/**
 * One test per row of the table in {@code docs/rule-combinations.md}, plus one per cell that is deliberately
 * left standing with the problem that blocks it named in the comment.
 */
class TractionRulesTest {

    private static ProjectiveRationalLiteral at(int numerator, int denominator) {
        return ProjectiveRationalLiteral.of(numerator, denominator);
    }

    /** {@code 0^a}, built the way the rules build it. */
    private static IExpr pow0(int numerator, int denominator) {
        return new TractionLiteral(ZERO, at(numerator, denominator));
    }

    // ---------------------------------------------------------------- reading the four points

    /**
     * The two on the traction axis read; the two additive units do not.
     *
     * <p>1 and -1 are the additive units, 0 and w the multiplicative ones, and a value is a + 0^b with one of
     * each. Reading an additive unit as a power of zero mixes the two axes, and it does not terminate: E10
     * turned 0 - 1 into 0^(1÷0), E1+E3 read that exponent as 0^0 ÷ 0^1, and that is 0^(0-1) again.
     * E5 is untouched as a rule about the TERM 0^0 -- see {@link #anIntegerPowerFlattens}.
     */
    @Test
    void theTwoMultiplicativeUnitsReadAsPowersOfZero() {
        assertEquals(Optional.of(ONE), TractionRules.exponentOfZero(ZERO));          // E4: 0 = 0^1
        assertEquals(Optional.of(NEG_ONE), TractionRules.exponentOfZero(OMEGA));     // Proven: w = 0^-1

        assertEquals(Optional.empty(), TractionRules.exponentOfZero(ONE));
        assertEquals(Optional.empty(), TractionRules.exponentOfZero(NEG_ONE));
    }

    /**
     * And with them unread, the cycle that would not terminate does not arise.
     *
     * <p>0 - 1 settles on -1, because 0 is invariant under addition. It is not an E10 case: E10 needs both
     * operands readable as powers of zero, and 1 is an additive unit.
     */
    @Test
    void subtractingAnAdditiveUnitFromAMultiplicativeOneSettles() {
        IExpr mixed = new AdditionOperationExpr(ZERO, new NegationOperationExpr(ONE));
        assertEquals(NEG_ONE, mixed.simplify());
    }

    /**
     * The fourth is the leap, and it is unwired — but no longer because it gave a wrong answer.
     *
     * <p>It used to: {@code w+w} came out as 1 from a defect in the coordinate addition, so reading
     * {@code -1 = 0^w} sent {@code (-1)·(-1)} to {@code 0^1}, which is the point zero. With the addition
     * fixed, {@code w+w} is {@code 2w} and the same route reaches {@code 0^(2w)}, which is not an answer but
     * is not a falsehood either — it is the term standing.
     *
     * <p>So what wiring the leap now costs is that {@code (-1)·(-1)} stops answering 1 and stands instead,
     * because the traction reading runs first and cannot finish. It would finish if {@code 2w = 0} were
     * adopted, since then {@code 0^(2w)} is {@code 0^0}, which is 1 — and {@code 2w = 0} is exactly the
     * condition rule-combinations.md already names for negation to be an involution.
     */
    @Test
    void theLeapIsNotWiredAndNoLongerGivesAWrongAnswer() {
        assertEquals(Optional.empty(), TractionRules.exponentOfZero(NEG_ONE));
        assertEquals(Optional.of(OMEGA), TractionRules.provisionalMinusOne(NEG_ONE));
        assertEquals(ONE, new MultiplicationOperationExpr(NEG_ONE, NEG_ONE).simplify());
        // The route it would take, and where it now stops: 0^(w+w) = 0^2w, standing.
        assertEquals(ProjectiveRationalLiteral.of(2, 0), new AdditionOperationExpr(OMEGA, OMEGA).simplify());
        assertEquals(new TractionLiteral(ZERO, ProjectiveRationalLiteral.of(2, 0)),
                TractionRules.traction(new AdditionOperationExpr(OMEGA, OMEGA).simplify()));
    }

    /**
     * And nothing else does. Reading 2 as a power of zero is the general involution, which is
     * theory-problems.md #4 and is not assumed — so {@code 2^3} has to stand, and does.
     */
    @Test
    void nothingElseReadsAsAPowerOfZero() {
        assertEquals(Optional.empty(), TractionRules.exponentOfZero(at(2, 1)));
        assertEquals(Optional.empty(), TractionRules.exponentOfZero(new AtomExpr("x")));
        // (0, 2) is a root of the residue zero and is NOT the point zero, which is the whole reason
        // coordinates do not reduce. It must not be read as 0^1.
        assertEquals(Optional.empty(), TractionRules.exponentOfZero(at(0, 2)));
    }

    /**
     * {@code x + 0 = x} and {@code x · 1 = x}, and the sum returns x itself rather than x at other
     * coordinates -- which is what invariance means and is why the identity is a rule and not left to the
     * coordinate arithmetic.
     */
    @Test
    void theIdentitiesReturnTheOtherOperandUnchanged() {
        assertEquals(ONE, new AdditionOperationExpr(ONE, ZERO).simplify());
        assertEquals(ONE, new AdditionOperationExpr(ZERO, ONE).simplify());
        assertEquals(new AtomExpr("x"), new AdditionOperationExpr(new AtomExpr("x"), ZERO).simplify());
        assertEquals(at(2, 3), new MultiplicationOperationExpr(at(2, 3), ONE).simplify());

        // Any magnitude-zero value, not only (0,1): -0 and 0÷2 add without effect too. 1 - 0 is 1 + (0,-1),
        // and letting the coordinates have it would give (-1,-1) -- one, written -1÷-1.
        assertEquals(ONE, new AdditionOperationExpr(ONE, at(0, -1)).simplify());
        assertEquals(ONE, new AdditionOperationExpr(ONE, at(0, 2)).simplify());
        assertEquals(ONE, new AdditionOperationExpr(ONE, new NegationOperationExpr(ZERO)).simplify());
    }

    /** w is invariant under neither operation, which is why the sum stands. */
    @Test
    void omegaIsNotAnIdentity() {
        IExpr sum = new AdditionOperationExpr(ONE, OMEGA);
        assertEquals(sum, sum.simplify());
    }

    // ---------------------------------------------------------------- the settled rows

    /** {@code 0^a · 0^b -> 0^(a+b)}, E1. */
    @Test
    void multiplicationAddsTheExponents() {
        assertEquals(pow0(5, 1), new MultiplicationOperationExpr(pow0(2, 1), pow0(3, 1)).simplify());
        // 0·0 = 0^2, and so 0² is not 0 -- E1 through the point zero read as 0^1.
        assertEquals(pow0(2, 1), new MultiplicationOperationExpr(ZERO, ZERO).simplify());
        // w·w = 0^-2
        assertEquals(pow0(-2, 1), new MultiplicationOperationExpr(OMEGA, OMEGA).simplify());
        // 1·1 is the value 1 and not an erasure: a zero exponent is its own negation and that must not count.
        assertEquals(ONE, new MultiplicationOperationExpr(ONE, ONE).simplify());
    }

    /** {@code 0^a ÷ 0^b -> 0^(a-b)}, E1 + E3, division arriving as a product with a reciprocal. */
    @Test
    void divisionSubtractsTheExponents() {
        IExpr quotient = new MultiplicationOperationExpr(pow0(5, 1), new ReciprocalOperationExpr(pow0(2, 1)));
        assertEquals(pow0(3, 1), quotient.simplify());
    }

    /** {@code 0^a - 0^b -> 0^(a÷b)}, E10, and total. */
    @Test
    void subtractionDividesTheExponents() {
        IExpr difference = new AdditionOperationExpr(pow0(2, 1), new NegationOperationExpr(pow0(3, 1)));
        assertEquals(new TractionLiteral(ZERO, at(2, 3)), difference.simplify());
    }

    /**
     * E10 matches a subtraction written either way round. {@code -x + y} is {@code y - x}, and the pattern
     * used to require the negation on the right -- so {@code 0 + (-0)} matched and answered 0, the erasure,
     * while {@code (-0) + 0} did not match, fell through to the identity, and answered -0. Addition was not
     * commutative because half of subtraction was invisible.
     */
    @Test
    void aSubtractionIsRecognisedFromEitherSide() {
        IExpr forward = new AdditionOperationExpr(pow0(2, 1), new NegationOperationExpr(pow0(3, 1)));
        IExpr backward = new AdditionOperationExpr(new NegationOperationExpr(pow0(3, 1)), pow0(2, 1));
        assertEquals(new TractionLiteral(ZERO, at(2, 3)), forward.simplify());
        assertEquals(forward.simplify(), backward.simplify());

        // and the erasure from either side
        assertEquals(ZERO, new AdditionOperationExpr(ZERO, new NegationOperationExpr(ZERO)).simplify());
        assertEquals(ZERO, new AdditionOperationExpr(new NegationOperationExpr(ZERO), ZERO).simplify());
    }

    /**
     * Totality is the branch this engine is on: at {@code a = b} the exponent is a multiplicative erasure and
     * materialises as 1, so {@code y - y} is {@code 0^1}, the point zero. It does not discharge.
     */
    @Test
    void subtractionIsTotalAtEqualExponents() {
        IExpr difference = new AdditionOperationExpr(pow0(2, 1), new NegationOperationExpr(pow0(2, 1)));
        assertEquals(ZERO, difference.simplify());
        // and at the points: 1-1 and 0-0 are both the point zero
        assertEquals(ZERO, new AdditionOperationExpr(ONE, new NegationOperationExpr(ONE)).simplify());
        assertEquals(ZERO, new AdditionOperationExpr(ZERO, new NegationOperationExpr(ZERO)).simplify());
    }

    /** {@code (0^a)^n -> 0^(a·n)} for nonzero integer n, which is E1 and not the withdrawn E2. */
    @Test
    void anIntegerPowerFlattens() {
        assertEquals(pow0(6, 1), new ExponentialOperationExpr(pow0(2, 1), at(3, 1)).simplify());
        // a typed 0^2 is a traction, recognised rather than derived
        assertEquals(pow0(2, 1), new ExponentialOperationExpr(ZERO, at(2, 1)).simplify());
        // E5 and E4, which are the same recognition folded back to the point
        assertEquals(ONE, new ExponentialOperationExpr(ZERO, ZERO).simplify());
        assertEquals(ZERO, new ExponentialOperationExpr(ZERO, ONE).simplify());
    }

    /**
     * The exponent has to be an integer AS A TERM. The pair (6, 3) is a coordinate pair, not the integer 2,
     * and asking what it projects to would let E2 back in at exactly the point it was withdrawn from.
     */
    @Test
    void anIntegerPowerIsRecognisedByItsTermAndNotByItsValue() {
        // The base 0^1 is the point zero, so this is 0^(6÷3) -- and the exponent STAYS (6,3). Had the pair
        // been read as the integer 2, the rule would have fired and produced 0^2.
        IExpr byCoordinates = new ExponentialOperationExpr(pow0(1, 1), at(6, 3));
        assertEquals(new TractionLiteral(ZERO, at(6, 3)), byCoordinates.simplify());
    }

    /**
     * {@code log_0(0^a) = a}, E8. A primitive, and it had been left unimplemented since the general-base row
     * was deleted with E2 -- the rule fell through the gap between the two.
     */
    @Test
    void logBaseZeroInvertsThePowerOfZero() {
        assertEquals(at(3, 1), new LogarithmOperationExpr(ZERO, pow0(3, 1)).simplify());
        assertEquals(ONE, new LogarithmOperationExpr(ZERO, ZERO).simplify());        // 0 = 0^1
        assertEquals(ZERO, new LogarithmOperationExpr(ZERO, ONE).simplify());        // 1 = 0^0, E5
        assertEquals(NEG_ONE, new LogarithmOperationExpr(ZERO, OMEGA).simplify());   // w = 0^-1
    }

    /**
     * It cannot loop, and the reason is structural rather than lucky: nothing in the engine PRODUCES a log
     * node, so a log rule cannot be part of a cycle. The parser is the only source of them.
     */
    @Test
    void logToAnyOtherBaseStands() {
        IExpr general = new LogarithmOperationExpr(at(2, 1), at(8, 1));
        assertEquals(general, general.simplify());
        // and the leap is not taken here either: log_0(-1) = w needs -1 = 0^w.
        IExpr leap = new LogarithmOperationExpr(ZERO, NEG_ONE);
        assertEquals(leap, leap.simplify());
    }

    /**
     * The {@code x^0} 4-cycle, wired. Chosen, so a derivation that uses it says so.
     *
     * <p>Applying it four times returns to where it started, which is the cycle checked end to end rather
     * than value by value. {@code 0^0 = 1} is E5 and comes from {@link #point} instead.
     */
    @Test
    void theZeroPowerCycleIsWiredAndCloses() {
        assertEquals(OMEGA, new ExponentialOperationExpr(ONE, ZERO).simplify());
        assertEquals(NEG_ONE, new ExponentialOperationExpr(OMEGA, ZERO).simplify());
        assertEquals(ZERO, new ExponentialOperationExpr(NEG_ONE, ZERO).simplify());
        assertEquals(ONE, new ExponentialOperationExpr(ZERO, ZERO).simplify());   // E5

        IExpr round = ONE;
        for (int i = 0; i < 4; i++) {
            round = new ExponentialOperationExpr(round, ZERO).simplify();
        }
        assertEquals(ONE, round);
    }

    /** Off the closure set it reaches nothing: the counting argument is E7's and cannot extend. */
    @Test
    void theZeroPowerReachesNothingOffTheClosureSet() {
        IExpr two = new ExponentialOperationExpr(at(2, 1), ZERO);
        assertEquals(two, two.simplify());
        IExpr atom = new ExponentialOperationExpr(new AtomExpr("x"), ZERO);
        assertEquals(atom, atom.simplify());
    }

    // ---------------------------------------------------------------- the multiplicative erasure

    /**
     * {@code 0·w} discharges to 1, and Problem 1 is closed.
     *
     * <p>w is {@code 1÷0} by E9, so {@code 0·w} is a value times its own reciprocal -- the multiplicative
     * erasure, which discharges to the identity of its own operation. It arrives in the exponent as the
     * ADDITIVE erasure {@code 1 + -1}, and that is what made it look open for so long: the lift changes the
     * kind of an erasure, and the kind that decides it is the operation it came FROM.
     */
    @Test
    void zeroTimesOmegaDischargesToOne() {
        assertEquals(ONE, new MultiplicationOperationExpr(ZERO, OMEGA).simplify());
        assertEquals(ONE, new MultiplicationOperationExpr(OMEGA, ZERO).simplify());
    }

    /** The same erasure away from the points: {@code 0^a · 0^-a} is y·(1÷y) by E3. */
    @Test
    void anExponentSumThatErasesDischarges() {
        assertEquals(ONE, new MultiplicationOperationExpr(pow0(2, 1), pow0(-2, 1)).simplify());
        // and by the TERM rather than the value: (2,2) and (-2,2) add to (0,4), which is not the point zero,
        // yet the pair is still an erasure and still discharges.
        assertEquals(ONE, new MultiplicationOperationExpr(pow0(2, 2), pow0(-2, 2)).simplify());
    }

    /** {@code 0^a ÷ 0^a} is y÷y, the same erasure written the other way. */
    @Test
    void aQuotientOfEqualPowersDischarges() {
        IExpr quotient = new MultiplicationOperationExpr(pow0(2, 1), new ReciprocalOperationExpr(pow0(2, 1)));
        assertEquals(ONE, quotient.simplify());
    }

    /**
     * But an ordinary quotient keeps its coordinates. {@code (2÷3)÷(2÷3)} is one AT (6,6), and sending it to
     * the literal one would be reducing -- which is the thing this carrier does not do.
     */
    @Test
    void anOrdinaryQuotientKeepsTheCoordinatesItArrivesAt() {
        assertEquals(at(6, 6), at(2, 3).dividedBy(at(2, 3)));
    }

    /** No rule reaches a power off the integers — {@code x^0}, {@code x^w}, and everything between. */
    @Test
    void aPowerOffTheIntegersStands() {
        for (IExpr exponent : new IExpr[]{ZERO, OMEGA, at(1, 2), new AtomExpr("x")}) {
            IExpr power = new ExponentialOperationExpr(pow0(2, 1), exponent);
            assertEquals(power, power.simplify(), "0^2 to the " + exponent);
        }
    }

    /**
     * An ordinary power is repeated multiplication and needs no traction reading at all, which is also the
     * whole proof of the rule above: the copies of 2 multiply as coordinates, the copies of 0^2 go through E1.
     */
    @Test
    void anOrdinaryPowerIsRepeatedMultiplication() {
        assertEquals(at(8, 1), new ExponentialOperationExpr(at(2, 1), at(3, 1)).simplify());
    }

    /**
     * But only where the copies join back up. x^2 is not improved by becoming x·x, and (x^2)^2 would unfold
     * to four of them for nothing.
     */
    @Test
    void aSymbolicPowerKeepsItsExponent() {
        IExpr power = new ExponentialOperationExpr(new AtomExpr("x"), at(2, 1));
        assertEquals(power, power.simplify());
    }

    /** A negative power is 1÷y^n, which E3 grants at base zero only, so 2^-1 stands. */
    @Test
    void aNegativeOrdinaryPowerStands() {
        IExpr power = new ExponentialOperationExpr(at(2, 1), at(-1, 1));
        assertEquals(power, power.simplify());
    }

    // ---------------------------------------------------------------- provisional, and unwired

    /**
     * The Maybe addition law works, and is deliberately not wired: it makes the value 0 an additive identity,
     * which contradicts the negation rule below. Whichever is wired, the other must not be.
     */
    @Test
    void theAdditionLawIsProvisionalAndNotWired() {
        assertEquals(Optional.of(pow0(6, 1)), TractionRules.provisionalSum(pow0(2, 1), pow0(3, 1)).map(rewrite -> rewrite.result().simplify()));
        // x + 0 = x is the consequence that collides with negation: 0^u + 0^1 = 0^(u·1) = 0^u.
        assertEquals(Optional.of(pow0(2, 1)), TractionRules.provisionalSum(pow0(2, 1), ZERO).map(rewrite -> rewrite.result().simplify()));
        // Unwired: an ordinary sum still goes to the projective layer, where 1+1 is 2 and not 0^(0·0).
        assertEquals(ProjectiveRationalLiteral.of(2, 1), new AdditionOperationExpr(ONE, ONE).simplify());
    }

    /**
     * And the negation rule, also unwired, with the second reason it cannot simply be switched on: the
     * coordinates make {@code 1 + w} equal {@code w}, so this sends {@code -0} to {@code 0^w}, which is
     * {@code -1}. The carrier's own negation says {@code -0 = 0}. Three answers, one term.
     */
    @Test
    void theNegationRuleIsProvisionalAndDisagreesWithTheCoordinates() {
        assertEquals(Optional.of(new TractionLiteral(ZERO, new AdditionOperationExpr(ONE, OMEGA))),
                TractionRules.provisionalNegation(ZERO).map(rewrite -> rewrite.result().simplify()));
        // The carrier used to say -0 = 0, which made three answers for one term. It says -0 = (0,-1) now,
            // so the disagreement is down to two: 0·(-1) against 0^(1+w), and the second needs the leap.
        assertEquals(ProjectiveRationalLiteral.of(0, -1), ZERO.negated());
        assertTrue(TractionRules.provisionalNegation(new AtomExpr("x")).isEmpty());
    }
}
