package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.AtomExpr;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.ExponentialOperationExpr;
import sibarum.cott.engine.operation.binary.LogarithmOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;
import sibarum.cott.engine.traction.rule.TractionRules;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.engine.rational.expr.RationalLiteral.NEG_ONE;
import static sibarum.cott.engine.rational.expr.RationalLiteral.ONE;
import static sibarum.cott.engine.rational.expr.RationalLiteral.ZERO;
import static sibarum.cott.engine.traction.expr.TractionLiteral.OMEGA;

/**
 * One test per operation in {@code docs/Traction-Theory.md}, one per cell of the two unit tables, and one per
 * cell that is deliberately left standing with the problem that blocks it named in the comment.
 */
class TractionRulesTest {

    private static RationalLiteral at(int numerator, int denominator) {
        return RationalLiteral.of(numerator, denominator);
    }

    /** {@code 0^a}: the pair with no real part. */
    private static IExpr pow0(int numerator, int denominator) {
        return TractionLiteral.of(at(numerator, denominator));
    }

    /** {@code n·0^t}: the pair with one. */
    private static IExpr pair(int real, int exponent) {
        return new TractionLiteral(at(real, 1), at(exponent, 1));
    }

    // ---------------------------------------------------------------- reading the four points

    /**
     * The two on the traction axis read; the two additive units do not.
     *
     * <p>1 and -1 are the additive units, 0 and w the multiplicative ones, and a value is a + 0^b with one of
     * each. Reading an additive unit as a power of zero mixes the two axes. E5 is untouched as a rule about
     * the TERM 0^0 -- see {@link #anIntegerPowerFlattens}.
     */
    @Test
    void theTwoMultiplicativeUnitsReadAsPowersOfZero() {
        assertEquals(Optional.of(ONE), TractionRules.exponentOfZero(ZERO));          // E4: 0 = 0^1
        assertEquals(Optional.of(NEG_ONE), TractionRules.exponentOfZero(OMEGA));     // w is the pair (1, -1)

        assertEquals(Optional.empty(), TractionRules.exponentOfZero(ONE));
        assertEquals(Optional.empty(), TractionRules.exponentOfZero(NEG_ONE));
    }

    /**
     * And the cycle that would not terminate does not arise. {@code 0 - 1} settles on -1.
     *
     * <p>Worth knowing that it settles for a second reason now. E2 needs both operands readable as powers of
     * zero and 1 is an additive unit, so the identity answers this -- but were the axis restriction lifted,
     * E2 would send it to {@code 0^(1÷0)}, and this carrier finishes that: {@code 1÷0} is w by E9 and
     * {@code 0^w} is -1 by the leap. The old loop was the coordinate pair reading its own zero denominator
     * back as an exponent, and there is no zero denominator here to read.
     */
    @Test
    void subtractingAnAdditiveUnitFromAMultiplicativeOneSettles() {
        IExpr mixed = new AdditionOperationExpr(ZERO, new NegationOperationExpr(ONE));
        assertEquals(NEG_ONE, mixed.simplify());
    }

    /**
     * The leap folds one way only: {@code 0^w} is -1, and -1 is not read back as {@code 0^w}.
     *
     * <p>Reading it back would turn {@code (-1)·(-1)} into {@code 0^(w+w)}, and {@code w+w} is {@code 2w},
     * which no rule finishes -- so the square of minus one would stand instead of answering 1. It does answer
     * 1, and the real coordinate is what answers it: {@code (-1, 0)·(-1, 0) = (1, 0)}. The old carrier had a
     * worse version of this, where {@code w+w} came out as 1 and the square of minus one came out as zero.
     */
    @Test
    void theLeapFoldsOneWayOnly() {
        assertEquals(NEG_ONE, TractionLiteral.of(OMEGA).simplify());
        assertEquals(Optional.empty(), TractionRules.exponentOfZero(NEG_ONE));
        assertEquals(ONE, new MultiplicationOperationExpr(NEG_ONE, NEG_ONE).simplify());
        // and w+w is 2w, by distributivity: two terms alike in their traction part.
        assertEquals(new TractionLiteral(at(2, 1), NEG_ONE), new AdditionOperationExpr(OMEGA, OMEGA).simplify());
    }

    /**
     * And nothing else reads as a power of zero. Reading 2 that way is the general involution, which is
     * theory-problems.md #4 and is not assumed -- so {@code 2^3} has to stand, and does.
     */
    @Test
    void nothingElseReadsAsAPowerOfZero() {
        assertEquals(Optional.empty(), TractionRules.exponentOfZero(at(2, 1)));
        assertEquals(Optional.empty(), TractionRules.exponentOfZero(new AtomExpr("x")));
        // (0, 2) is 0÷2, a root of the residue zero and not the point zero, which is the whole reason
        // coordinates do not reduce. It must not be read as 0^1.
        assertEquals(Optional.empty(), TractionRules.exponentOfZero(at(0, 2)));
    }

    /**
     * {@code x + 0 = x} and {@code x · 1 = x}, returning the other operand itself rather than the same value
     * at other coordinates -- which is what invariance means, and is why the identity is a rule rather than
     * being left to the coordinate arithmetic.
     */
    @Test
    void theIdentitiesReturnTheOtherOperandUnchanged() {
        assertEquals(ONE, new AdditionOperationExpr(ONE, ZERO).simplify());
        assertEquals(ONE, new AdditionOperationExpr(ZERO, ONE).simplify());
        assertEquals(new AtomExpr("x"), new AdditionOperationExpr(new AtomExpr("x"), ZERO).simplify());
        assertEquals(at(2, 3), new MultiplicationOperationExpr(at(2, 3), ONE).simplify());

        // Any magnitude-zero value, not only (0,1): 0÷2 and -0 add without effect too.
        assertEquals(ONE, new AdditionOperationExpr(ONE, at(0, 2)).simplify());
        assertEquals(ONE, new AdditionOperationExpr(ONE, new NegationOperationExpr(ZERO)).simplify());
    }

    /**
     * But at the same order the real parts add, and that runs first.
     *
     * <p>So {@code 0 + 0} is {@code 2·0} and not {@code 0}. The identity and this rule are the dominance
     * condition between them: {@code x + 0 = x} holds where x is not itself a multiple of the point zero,
     * which is what REVIEW.md's P1-1 says the identity actually needs.
     */
    @Test
    void sumsAtTheSameOrderAddTheirRealParts() {
        assertEquals(pair(2, 1), new AdditionOperationExpr(ZERO, ZERO).simplify());
        assertEquals(pair(3, 1), new AdditionOperationExpr(pair(2, 1), ZERO).simplify());
        assertEquals(new TractionLiteral(at(2, 1), NEG_ONE), new AdditionOperationExpr(OMEGA, OMEGA).simplify());

        // and real parts that negate as terms are the additive erasure, which discharges to the point zero
        // rather than leaving a zero real part for the coordinates to roll into the exponent.
        assertEquals(ZERO, new AdditionOperationExpr(pair(1, 1), pair(-1, 1)).simplify());
    }

    /** w is invariant under neither operation, which is why the sum stands. */
    @Test
    void omegaIsNotAnIdentity() {
        IExpr sum = new AdditionOperationExpr(ONE, OMEGA);
        assertEquals(sum, sum.simplify());
    }

    // ---------------------------------------------------------------- the operations

    /** {@code (a, b)·(c, d) = (a·c, b+d)}, E1. */
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

    /**
     * And it keeps the other factor, which is the whole reason the real part may not be zero.
     *
     * <p>{@code 2·0} is the pair {@code (2, 1)}. Were it the point zero, dividing by zero would prove
     * {@code 2 = 1}, and the coordinate layer used to say exactly that -- a rational zero annihilates, so
     * {@code (0,1)·(2,1)} came out as zero with the 2 gone.
     */
    @Test
    void multiplyingByZeroKeepsTheOtherFactor() {
        assertEquals(pair(2, 1), new MultiplicationOperationExpr(at(2, 1), ZERO).simplify());
        assertEquals(pair(-1, 1), new MultiplicationOperationExpr(NEG_ONE, ZERO).simplify());
        assertEquals(pow0(3, 1), new MultiplicationOperationExpr(ZERO, pow0(2, 1)).simplify());
    }

    /** {@code (a, b)÷(c, d) = (a÷c, b-d)}, E1 + E3, division arriving as a product with a reciprocal. */
    @Test
    void divisionSubtractsTheExponents() {
        IExpr quotient = new MultiplicationOperationExpr(pow0(5, 1), new ReciprocalOperationExpr(pow0(2, 1)));
        assertEquals(pow0(3, 1), quotient.simplify());
    }

    /**
     * E1 matches a division written either way round. {@code (1÷y)·x} is {@code x÷y}, and the pattern used to
     * require the reciprocal on the right -- so {@code 0^5÷0^3} answered {@code 0^2} while {@code (1÷0^3)·0^5}
     * stood, and {@code y÷y} discharged to 1 while {@code (1÷y)·y} stood. Multiplication was not commutative
     * because half of division was invisible.
     */
    @Test
    void aDivisionIsRecognisedFromEitherSide() {
        IExpr forward = new MultiplicationOperationExpr(pow0(5, 1), new ReciprocalOperationExpr(pow0(3, 1)));
        IExpr backward = new MultiplicationOperationExpr(new ReciprocalOperationExpr(pow0(3, 1)), pow0(5, 1));
        assertEquals(pow0(2, 1), forward.simplify());
        assertEquals(forward.simplify(), backward.simplify());

        IExpr erasureForward = new MultiplicationOperationExpr(pow0(3, 1), new ReciprocalOperationExpr(pow0(3, 1)));
        IExpr erasureBackward = new MultiplicationOperationExpr(new ReciprocalOperationExpr(pow0(3, 1)), pow0(3, 1));
        assertEquals(ONE, erasureForward.simplify());
        assertEquals(ONE, erasureBackward.simplify());
    }

    /** {@code 1÷0 = ω}, E9, which the coordinate layer cannot hold and so does not answer. */
    @Test
    void theReciprocalOfZeroIsOmega() {
        assertEquals(OMEGA, new ReciprocalOperationExpr(ZERO).simplify());
        assertEquals(ZERO, new ReciprocalOperationExpr(OMEGA).simplify());
        assertEquals(OMEGA, new MultiplicationOperationExpr(ONE, new ReciprocalOperationExpr(ZERO)).simplify());
    }

    /** {@code 0^a - 0^b -> 0^(a÷b)}, E2, and total. */
    @Test
    void subtractionDividesTheExponents() {
        IExpr difference = new AdditionOperationExpr(pow0(2, 1), new NegationOperationExpr(pow0(3, 1)));
        assertEquals(pow0(2, 3), difference.simplify());
    }

    /**
     * E2 matches a subtraction written either way round. {@code -x + y} is {@code y - x}, and the pattern used
     * to require the negation on the right -- so {@code 0 + (-0)} matched and answered 0, the erasure, while
     * {@code (-0) + 0} did not match, fell through to the identity, and answered -0.
     */
    @Test
    void aSubtractionIsRecognisedFromEitherSide() {
        IExpr forward = new AdditionOperationExpr(pow0(2, 1), new NegationOperationExpr(pow0(3, 1)));
        IExpr backward = new AdditionOperationExpr(new NegationOperationExpr(pow0(3, 1)), pow0(2, 1));
        assertEquals(pow0(2, 3), forward.simplify());
        assertEquals(forward.simplify(), backward.simplify());

        assertEquals(ZERO, new AdditionOperationExpr(ZERO, new NegationOperationExpr(ZERO)).simplify());
        assertEquals(ZERO, new AdditionOperationExpr(new NegationOperationExpr(ZERO), ZERO).simplify());
    }

    /**
     * Totality: {@code y - y} is the additive erasure and discharges to the point zero, for every y, read off
     * the term rather than off what the coordinates would compute.
     */
    @Test
    void subtractionIsTotalAtEqualExponents() {
        assertEquals(ZERO, new AdditionOperationExpr(pow0(2, 1), new NegationOperationExpr(pow0(2, 1))).simplify());
        assertEquals(ZERO, new AdditionOperationExpr(ONE, new NegationOperationExpr(ONE)).simplify());
        assertEquals(ZERO, new AdditionOperationExpr(ZERO, new NegationOperationExpr(ZERO)).simplify());
        // any term at all, not only a value: x - x is the same erasure
        AtomExpr x = new AtomExpr("x");
        assertEquals(ZERO, new AdditionOperationExpr(x, new NegationOperationExpr(x)).simplify());
    }

    /**
     * {@code -(a·0^b) = (-a)·0^b}: negation turns the real coordinate.
     *
     * <p>So it is an involution on the pair, and {@code -0} is {@code (-1, 1)} -- which is {@code -1·0} and
     * has no further answer. The old carrier had to put the sign on a denominator to keep {@code -0} apart
     * from {@code 0}; here the two coordinates do it.
     */
    @Test
    void negationTurnsTheRealCoordinate() {
        assertEquals(pair(-1, 1), new NegationOperationExpr(ZERO).simplify());
        assertEquals(ZERO, new NegationOperationExpr(new NegationOperationExpr(ZERO)).simplify());
        assertEquals(new TractionLiteral(NEG_ONE, NEG_ONE), new NegationOperationExpr(OMEGA).simplify());
        assertEquals(new TractionLiteral(at(-1, 1), at(2, 1)), new NegationOperationExpr(pow0(2, 1)).simplify());
    }

    // ---------------------------------------------------------------- exponentiation and the tables

    /** {@code (0^a)^n -> 0^(a·n)} for nonzero integer n, which is E1 as repeated multiplication. */
    @Test
    void anIntegerPowerFlattens() {
        assertEquals(pow0(6, 1), new ExponentialOperationExpr(pow0(2, 1), at(3, 1)).simplify());
        // a typed 0^2 is a traction, recognised rather than derived
        assertEquals(pow0(2, 1), new ExponentialOperationExpr(ZERO, at(2, 1)).simplify());
        // E5 and E4, the same recognition folded back to the point
        assertEquals(ONE, new ExponentialOperationExpr(ZERO, ZERO).simplify());
        assertEquals(ZERO, new ExponentialOperationExpr(ZERO, ONE).simplify());
    }

    /**
     * The exponent has to be an integer AS A TERM. The pair (6, 3) is a coordinate pair, not the integer 2,
     * and asking what it projects to would be reducing.
     */
    @Test
    void anIntegerPowerIsRecognisedByItsTermAndNotByItsValue() {
        IExpr byCoordinates = new ExponentialOperationExpr(pow0(1, 1), at(6, 3));
        assertEquals(pow0(6, 3), byCoordinates.simplify());
    }

    /**
     * The unit exponentiation table, all sixteen cells.
     *
     * <p>Ordered {@code 0, 1, ω, -1} in both directions, as the {@code x^0} cycle takes them. The base-zero
     * row is not read from the table -- it is E5, E4, the leap and E3+E9 -- and the point of asserting it
     * here beside the others is that the two agree.
     */
    @Test
    void theUnitExponentiationTable() {
        IExpr[] units = {ZERO, ONE, OMEGA, NEG_ONE};
        IExpr[][] expected = {
                {ONE, ZERO, NEG_ONE, OMEGA},
                {OMEGA, ONE, ZERO, NEG_ONE},
                {NEG_ONE, OMEGA, ONE, ZERO},
                {ZERO, NEG_ONE, OMEGA, ONE},
        };
        for (int base = 0; base < units.length; base++) {
            for (int exponent = 0; exponent < units.length; exponent++) {
                assertEquals(expected[base][exponent],
                        new ExponentialOperationExpr(units[base], units[exponent]).simplify(),
                        units[base] + "^" + units[exponent]);
            }
        }
    }

    /** Every row of it is a permutation of the four, which is what "bijective over the units" means. */
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

    /**
     * {@code log_0(0^a) = a}, E8, and the leap read backwards: {@code log_0(-1) = ω}.
     *
     * <p>That last cell was unwired before, because reading -1 as a power of zero was what broke the square of
     * minus one. It is safe here for the reason E8 was always safe -- inverting is not arithmetic, and nothing
     * in the engine produces a log for this to feed -- and now the real coordinate answers the square anyway.
     */
    @Test
    void logBaseZeroInvertsThePowerOfZero() {
        assertEquals(at(3, 1), new LogarithmOperationExpr(ZERO, pow0(3, 1)).simplify());
        assertEquals(ONE, new LogarithmOperationExpr(ZERO, ZERO).simplify());        // 0 = 0^1
        assertEquals(ZERO, new LogarithmOperationExpr(ZERO, ONE).simplify());        // 1 = 0^0, E5
        assertEquals(NEG_ONE, new LogarithmOperationExpr(ZERO, OMEGA).simplify());   // w = 0^-1
        assertEquals(OMEGA, new LogarithmOperationExpr(ZERO, NEG_ONE).simplify());   // -1 = 0^w, the leap
    }

    /**
     * The unit logarithm table, which is the exponentiation table read backwards -- so it is asserted by
     * inverting, rather than by copying the doc's second table into the test and checking a table against
     * itself.
     */
    @Test
    void theUnitLogarithmTableInvertsTheOther() {
        IExpr[] units = {ZERO, ONE, OMEGA, NEG_ONE};
        for (IExpr base : units) {
            for (IExpr exponent : units) {
                IExpr value = new ExponentialOperationExpr(base, exponent).simplify();
                assertEquals(exponent, new LogarithmOperationExpr(base, value).simplify(),
                        "log(" + value + ", " + base + ")");
            }
        }
    }

    /**
     * A log to any other base stands, and it cannot loop: nothing in the engine PRODUCES a log node, so a log
     * rule cannot be part of a cycle. The parser is the only source of them.
     */
    @Test
    void logToAnyOtherBaseStands() {
        IExpr general = new LogarithmOperationExpr(at(2, 1), at(8, 1));
        assertEquals(general, general.simplify());
    }

    // ---------------------------------------------------------------- the erasure

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
        // and by the TERM rather than the value: (2,2) and (-2,2) add to (0,4), which is not the exponent
        // zero, yet the pair is still an erasure and still discharges.
        assertEquals(ONE, new MultiplicationOperationExpr(pow0(2, 2), pow0(-2, 2)).simplify());
    }

    /**
     * {@code z÷z} is 1 and {@code z-z} is 0, read off the term -- except between two rationals, where the
     * coordinates answer.
     *
     * <p>Traction-Theory.md's erasure section says {@code z/z} is ∅ multiplicatively, so standing alone it is
     * the multiplicative identity. But {@code 2÷2} is {@code (2,2)}, one at coordinates that are not one, and
     * that is deliberate: it is the same fact the exponent erasure is read off the term to avoid, so
     * overruling it here would contradict the reason this carrier does not reduce. Between anything else --
     * tractions, whole terms, atoms -- there are no coordinates to keep and the erasure discharges.
     */
    @Test
    void theWholeTermErasureDischargesToItsOwnIdentity() {
        assertEquals(at(2, 2),
                new MultiplicationOperationExpr(at(2, 1), new ReciprocalOperationExpr(at(2, 1))).simplify());
        assertEquals(ONE, new MultiplicationOperationExpr(ZERO, new ReciprocalOperationExpr(ZERO)).simplify());
        assertEquals(ONE, new MultiplicationOperationExpr(pow0(2, 1), new ReciprocalOperationExpr(pow0(2, 1))).simplify());
        IExpr twoThirds = new MultiplicationOperationExpr(at(2, 1), new ReciprocalOperationExpr(at(3, 1)));
        assertEquals(ONE, new MultiplicationOperationExpr(twoThirds, new ReciprocalOperationExpr(twoThirds)).simplify());
        AtomExpr x = new AtomExpr("x");
        assertEquals(ONE, new MultiplicationOperationExpr(x, new ReciprocalOperationExpr(x)).simplify());
    }

    /** No rule reaches a power off the integers -- {@code x^0}, {@code x^w}, and everything between. */
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
     * The traction addition law runs, and is deliberately not wired.
     *
     * <p>What it answers here is the fourth disagreement listed on the method: on two bare powers it gives
     * {@code 2·0^6}, where the older docs' Maybe law gives {@code 0^6}. The factor is {@code (a+c)} at
     * {@code a = c = 1}, and it disappears only if a bare {@code 0^b} is read as having real part 0 rather
     * than 1 -- the same ∅-against-zero ambiguity that decides the other three.
     */
    @Test
    void theAdditionLawIsProvisionalAndNotWired() {
        assertEquals(Optional.of(new TractionLiteral(at(2, 1), at(6, 1))),
                TractionRules.provisionalAddition(pow0(2, 1), pow0(3, 1))
                        .map(rewrite -> rewrite.result().simplify()));

        // Unwired: an unlike sum stands, and an ordinary one is still the coordinates adding.
        IExpr unlike = new AdditionOperationExpr(pow0(2, 1), pow0(3, 1));
        assertEquals(unlike, unlike.simplify());
        assertEquals(at(2, 1), new AdditionOperationExpr(ONE, ONE).simplify());
    }

    /**
     * The general involution is not wired either: on the four points the tables have it, and off them it is
     * theory-problems.md #4.
     */
    @Test
    void theGeneralInvolutionIsProvisionalAndNotWired() {
        IExpr nested = TractionLiteral.of(pow0(2, 1));          // 0^(0^2)
        assertEquals(nested, nested.simplify());
        assertEquals(Optional.of(at(2, 1)),
                TractionRules.provisionalInvolution(nested).map(rewrite -> rewrite.result()));
        assertTrue(TractionRules.provisionalInvolution(pow0(2, 1)).isEmpty());
    }

    /**
     * And {@code -1·0 = ω} is recorded as a conjecture with its refutation, not applied.
     *
     * <p>Through the primitives it forces {@code ω = -2} as an exponent, and then {@code 0^2 = 0^-2} and
     * {@code 2 = -2}. So the pair stands.
     */
    @Test
    void minusZeroIsRecordedAsAConjectureAndNotApplied() {
        IExpr minusZero = new MultiplicationOperationExpr(NEG_ONE, ZERO).simplify();
        assertEquals(pair(-1, 1), minusZero);
        assertEquals(Optional.of(OMEGA), TractionRules.provisionalMinusZero(minusZero).map(rewrite -> rewrite.result()));
        assertTrue(TractionRules.provisionalMinusZero(ZERO).isEmpty());
    }

}
