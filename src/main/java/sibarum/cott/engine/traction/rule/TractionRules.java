package sibarum.cott.engine.traction.rule;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.rule.Rewrite;
import sibarum.cott.engine.base.rule.Rule;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.ExponentialOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;

import java.math.BigInteger;
import java.util.Optional;

/**
 * The rules: the operations of Traction-Theory.md, one method each, and the two unit tables.
 *
 * <h2>The operations are the pair arithmetic</h2>
 * {@code (a, b) · (c, d) = (a·c, b+d)} is E1; {@code 1÷(a, b) = (1÷a, -b)} is E3; negation turns the real
 * coordinate. Those three are total and reversible on the pair and need no reading of anything as a power of
 * zero. Addition is not one of them -- see {@link #provisionalAddition}.
 *
 * <h2>Matching is on terms, never on values</h2>
 * A degenerate cell cannot be recognised by what it is worth. {@code 1·1} is the value 1 and {@code 1·(1÷1)}
 * is an erasure; they are the same class and different terms. So the erasure guards compare the exponents as
 * written -- {@code b} is {@code -a} when it is the negation of that term, not when the two happen to add to
 * something that projects onto zero -- and nothing is canonicalised on the way in.
 *
 * <h2>Reading a value as a power of zero</h2>
 * Every traction value is {@code 0^a} for one a (E6), but only the ones on the traction axis can be read that
 * way from the carrier: a bare pair, and the point zero by E4. {@code 1} and {@code -1} are the additive units
 * and are not read as powers of zero -- that is the axis restriction. Anything else would need the general
 * involution, which is theory-problems.md #4 and is not assumed here, and is why {@code 2^3} stands rather
 * than becoming a traction.
 */
public final class TractionRules {

    private static final RationalLiteral ZERO = RationalLiteral.ZERO;
    private static final RationalLiteral ONE = RationalLiteral.ONE;
    private static final RationalLiteral NEG_ONE = RationalLiteral.NEG_ONE;
    private static final TractionLiteral OMEGA = TractionLiteral.OMEGA;

    // ---------------------------------------------------------------- what a derivation cites

    public static final Rule PRODUCT =
            new Rule("a·0^b · c·0^d = (a·c)·0^(b+d)", "E1", Rule.Status.PROVEN);
    public static final Rule QUOTIENT =
            new Rule("a·0^b ÷ c·0^d = (a÷c)·0^(b-d)", "E1 + E3", Rule.Status.PROVEN);
    /**
     * The quotient rule, which Traction-Theory.md lists among the axioms as E2. The older docs derive it
     * instead, number it E10 and file it Chosen, since through the involution it inherits problem 4. It is an
     * axiom here because that is the model being implemented -- but a derivation that cites it names both.
     */
    public static final Rule DIFFERENCE =
            new Rule("0^a - 0^b = 0^(a÷b)", "E2, total (E10 and Chosen in the older docs)", Rule.Status.PROVEN);
    public static final Rule RECIPROCAL =
            new Rule("1÷(a·0^b) = (1÷a)·0^(-b)", "E3", Rule.Status.PROVEN);
    public static final Rule NEGATION =
            new Rule("-(a·0^b) = (-a)·0^b", "negation distributes over a product", Rule.Status.PROVEN);
    public static final Rule OMEGA_DEF =
            new Rule("1÷0 = ω", "E9", Rule.Status.PROVEN);
    public static final Rule LIKE_TERMS =
            new Rule("a·0^b + c·0^b = (a+c)·0^b", "distributivity", Rule.Status.PROVEN);
    public static final Rule INTEGER_POWER =
            new Rule("(0^a)^n = 0^(a·n), integer n", "E1, repeated multiplication", Rule.Status.PROVEN);
    public static final Rule NEGATIVE_POWER =
            new Rule("(0^a)^-n = 0^(-a·n)", "E3", Rule.Status.PROVEN);
    public static final Rule BASE_ZERO =
            new Rule("0^E is a traction", "the pair (1, E)", Rule.Status.PROVEN);
    public static final Rule POINT =
            new Rule("0^1 = 0, a·0^0 = a", "E4 and E5", Rule.Status.PROVEN);
    public static final Rule ROLL_IN =
            new Rule("(0, t) = (1, t+1)", "E4: the rational zero is 0^1", Rule.Status.PROVEN);
    public static final Rule ZERO_COORDINATES =
            new Rule("0÷d = (1÷d)·0", "E4: the rational zero is 0^1", Rule.Status.PROVEN);
    public static final Rule PLUS_ZERO =
            new Rule("x + 0 = x", "0 is invariant under addition", Rule.Status.PROVEN);
    public static final Rule TIMES_ONE =
            new Rule("x · 1 = x", "1 is invariant under multiplication", Rule.Status.PROVEN);
    public static final Rule LOG_INVERTS =
            new Rule("log_0(0^a) = a", "E8", Rule.Status.PROVEN);

    /**
     * The erasure, discharged to the identity of its own operation.
     * <p>
     * {@code z÷z = ∅} multiplicatively and {@code z-z = ∅} additively, so standing alone the first is 1 and
     * the second is 0. The kind is fixed by the operation it came FROM, not the one it lands in, which is what
     * settles {@code 0·w}: w is {@code 1÷0} by E9, so {@code 0·w} is {@code y·(1÷y)}, and it discharges to 1
     * even though it reaches the exponent as the additive erasure {@code 1 + -1}. The lift changes the kind.
     */
    public static final Rule ERASURE =
            new Rule("z÷z = 1, z-z = 0", "the erasure discharges to its own operation's identity", Rule.Status.PROVEN);

    /** The leap, folded one way only. See {@link #exponentOfZero}. */
    public static final Rule LEAP =
            new Rule("0^ω = -1", "E6 and E7, the leap", Rule.Status.CHOSEN);
    public static final Rule UNIT_POWER =
            new Rule("the unit exponentiation table", "Traction-Theory.md: Unit Exponentiation", Rule.Status.CHOSEN);
    public static final Rule UNIT_LOG =
            new Rule("the unit logarithm table", "Traction-Theory.md: Unit Logarithm", Rule.Status.CHOSEN);

    public static final Rule ADDITION_LAW =
            new Rule("a·0^b + c·0^d = (a+c)·a^d·c^b·0^(bd)", "Traction-Theory.md: Addition", Rule.Status.OPEN);
    public static final Rule INVOLUTION =
            new Rule("0^(0^n) = n", "E6, off the closure set", Rule.Status.OPEN);
    public static final Rule MINUS_ZERO =
            new Rule("-1·0 = ω", "conjectured, and refuted from E1 + E3 + E6", Rule.Status.OPEN);

    private TractionRules() {
    }

    // ---------------------------------------------------------------- reading and building

    /**
     * The pair {@code (n, t)} this expression is, where the carrier can say.
     * <p>
     * A nonzero rational is {@code (r, 0)}: its real part is itself and it is nowhere on the traction axis.
     * That is not E5 being used to read 1 as {@code 0^0} -- the real coordinate stays put, and nothing here
     * moves an additive unit onto the traction axis. The rational zero IS {@code 0^1} by E4, so it promotes
     * to {@code (1, 1)}.
     */
    private static Optional<Pair> pair(IExpr e) {
        if (e instanceof TractionLiteral t) {
            return Optional.of(new Pair(t.real(), t.exponent()));
        }
        if (e instanceof RationalLiteral r) {
            // A zero numerator keeps its denominator, exactly: (0, d) is 0÷d, which is (1÷d)·0. Sending it to
            // the point zero would drop the d, and the d is multiplicative -- a root, an orientation -- so
            // dropping it loses information the type is supposed to conserve.
            return Optional.of(r.isZero()
                    ? new Pair(new RationalLiteral(BigInteger.ONE, r.denominator()), ONE)
                    : new Pair(r, ZERO));
        }
        return Optional.empty();
    }

    /**
     * Whether this term has to be lifted before the pair rules can answer for it.
     * <p>
     * A traction obviously, and the rational zero, because the coordinate layer must never multiply that one:
     * as a rational it annihilates, and {@code 0·0} would come back as {@code 0} rather than {@code 0^2}. Two
     * ordinary rationals are left to the coordinates, which is not deference -- it is that lifting them would
     * put the pair rules in a loop, rebuilding {@code 2÷3} as {@code (2÷3)·0^0} for ever.
     */
    private static boolean liftable(IExpr e) {
        return e instanceof TractionLiteral || e instanceof RationalLiteral r && r.isZero();
    }

    /**
     * The exponent {@code a} for which this expression is a BARE {@code 0^a}, where that can be read off.
     * <p>
     * Two readings: a traction with no real part, and the point zero by E4. Omega is the traction
     * {@code (1, -1)}, so it is already one of them.
     * <p>
     * {@code 1} and {@code -1} are not read here, though E5 says the first is {@code 0^0} and the leap says
     * the second is {@code 0^ω}. They are the additive units, and reading one as a power of zero mixes the
     * axes. The leap in particular folds only in the other direction -- {@code 0^ω} becomes {@code -1} --
     * because reading {@code -1} as {@code 0^ω} turns {@code (-1)·(-1)} into {@code 0^(ω+ω)}, which no rule
     * finishes, where the real coordinate answers 1 directly.
     */
    public static Optional<IExpr> exponentOfZero(IExpr e) {
        if (e instanceof TractionLiteral t && t.isBare()) {
            return Optional.of(t.exponent());
        }
        if (e instanceof ExponentialOperationExpr p && ZERO.equals(p.base())) {
            return Optional.of(p.exponent());
        }
        if (ZERO.equals(e)) {
            return Optional.of(ONE);        // E4
        }
        return Optional.empty();
    }

    /** {@code 0^exponent}. */
    public static IExpr traction(IExpr exponent) {
        return TractionLiteral.of(exponent);
    }

    /**
     * The pair {@code (real, exponent)}.
     * <p>
     * Built rather than folded: {@code (a, 0)} is {@code a·0^0} and E5 is the rule that finishes it, which
     * {@link #point} does on the turn after, in view of the derivation.
     */
    private static IExpr pairOf(IExpr real, IExpr exponent) {
        return new TractionLiteral(real, exponent);
    }

    // ---------------------------------------------------------------- the pair's own folds

    /**
     * What a pair folds to, where it folds to something: {@code a·0^0 = a} (E5), {@code 0^1 = 0} (E4), and
     * {@code 0^ω = -1} (the leap).
     * <p>
     * A rewrite of its own rather than something the constructor does on the way past, so that a derivation
     * shows it happening instead of finding it already done.
     * <p>
     * {@code (1, -1)} does not fold: it IS omega, which is how the carrier spells it, and the printer names
     * it. Nor does {@code (-1, 1)}, which is {@code -1·0} -- the theory has not resolved that one, and
     * {@link #provisionalMinusZero} records both the conjecture and what refutes it.
     */
    public static Optional<Rewrite> point(TractionLiteral t) {
        // A real part that arrives as the rational zero rolls into the exponent: the rational zero is 0^1,
        // and a zero real part would be an annihilator, which this theory does not have.
        if (t.real() instanceof RationalLiteral r && r.isZero()) {
            return Optional.of(new Rewrite(
                    pairOf(new RationalLiteral(BigInteger.ONE, r.denominator()), plus(t.exponent(), ONE)),
                    ROLL_IN));
        }
        if (ZERO.equals(t.exponent())) {
            return Optional.of(new Rewrite(t.real(), POINT));           // a·0^0 = a·1 = a, E5
        }
        if (t.isBare() && ONE.equals(t.exponent())) {
            return Optional.of(new Rewrite(ZERO, POINT));               // 0^1 = 0, E4
        }
        if (OMEGA.equals(t.exponent())) {
            // The leap folds under a real part too, and it has to. Otherwise a product carries the real part
            // in first -- 0^ω · 0^ω becomes (-1, ω) by E1 -- and the fold can no longer see the term it was
            // waiting for, so the square of minus one stands instead of answering 1.
            return t.isBare()
                    ? Optional.of(new Rewrite(NEG_ONE, LEAP))
                    : Optional.of(new Rewrite(times(t.real(), NEG_ONE), LEAP));
        }
        return Optional.empty();
    }

    /**
     * A zero numerator away from the coordinate {@code (0, 1)}: {@code 0÷d} is {@code (1÷d)·0}, the pair.
     * <p>
     * The rational zero at {@code (0, 1)} is the point zero and stays as it is. Anything else with a zero
     * numerator is a multiple of it -- {@code (0, -1)} is what {@code 1 + 1÷(-1)} lands on, where the
     * magnitude cancels and the orientation survives -- and the pair is where a multiple of the point zero
     * lives. Without this the same value had two spellings and the display did not round-trip: the coordinate
     * sum produced {@code 0÷-1} and re-reading it produced {@code 1÷-1·0}.
     */
    public static Optional<Rewrite> zeroCoordinates(RationalLiteral r) {
        if (!r.isZero() || r.denominator().equals(BigInteger.ONE)) {
            return Optional.empty();
        }
        return Optional.of(new Rewrite(
                pairOf(new RationalLiteral(BigInteger.ONE, r.denominator()), ONE), ZERO_COORDINATES));
    }

    // ---------------------------------------------------------------- multiplication and division

    /**
     * {@code (a, b) · (c, d) = (a·c, b+d)}, E1, with division arriving as a product with a reciprocal in it,
     * written either way round.
     * <p>
     * Where the exponent operation is an erasure the term discharges to the multiplicative identity:
     * {@code b = -a} for the product, {@code a = b} for the quotient. Both present as the ADDITIVE erasure in
     * the exponent and both are multiplicative at the value level, so what they discharge to is
     * multiplication's identity. {@code 0·w} is the product case, and it is 1.
     */
    public static Optional<Rewrite> product(IExpr left, IExpr right) {
        if (right instanceof ReciprocalOperationExpr(IExpr by)) {
            return quotient(left, by);
        }
        // A division written the other way round is still a division: (1÷y)·x is x÷y, the same rule with the
        // roles swapped. Matching one order only made multiplication non-commutative on those terms.
        if (left instanceof ReciprocalOperationExpr(IExpr by)) {
            return quotient(right, by);
        }
        if (!liftable(left) && !liftable(right)) {
            return Optional.empty();
        }
        return pair(left).flatMap(l -> pair(right).map(r -> negates(l.t(), r.t())
                ? new Rewrite(pairOf(times(l.n(), r.n()), ZERO), ERASURE)
                : new Rewrite(pairOf(times(l.n(), r.n()), plus(l.t(), r.t())), PRODUCT)));
    }

    /**
     * {@code (a, b) ÷ (c, d) = (a÷c, b-d)}, E1 + E3.
     * <p>
     * {@code z÷z} is the whole-term erasure and is 1 before any coordinate is touched -- otherwise
     * {@code 2÷2} would come back as the pair {@code (2,2)}, which is the value one at coordinates that are
     * not one. Reading the erasure off the term is what makes it exact.
     */
    private static Optional<Rewrite> quotient(IExpr of, IExpr by) {
        // The whole-term erasure, but not between two rationals: there the coordinates answer, and what they
        // answer is (2,2) -- one at coordinates that are not one. That is deliberate and it is the same fact
        // the exponent erasure is read off the term to avoid, so overruling it here would contradict the
        // reason this engine does not reduce. Everywhere else -- tractions, atoms, whole terms -- z÷z is 1.
        boolean coordinates = of instanceof RationalLiteral && by instanceof RationalLiteral;
        if (!coordinates && of.equals(by)) {
            return Optional.of(new Rewrite(ONE, ERASURE));
        }
        if (!liftable(of) && !liftable(by)) {
            return Optional.empty();
        }
        if (of.equals(by)) {
            return Optional.of(new Rewrite(ONE, ERASURE));
        }
        return pair(of).flatMap(l -> pair(by).map(r -> l.t().equals(r.t())
                ? new Rewrite(pairOf(over(l.n(), r.n()), ZERO), QUOTIENT)
                : new Rewrite(pairOf(over(l.n(), r.n()), minus(l.t(), r.t())), QUOTIENT)));
    }

    // ---------------------------------------------------------------- addition and subtraction

    /**
     * What a sum answers: the like-terms case by distributivity, and subtraction by E2.
     * <p>
     * {@code a·0^b + c·0^b = (a+c)·0^b} is distributivity and nothing more -- it is what keeps
     * {@code ω + ω = 2ω} rather than sending it through a law. The general sum at unlike exponents is
     * {@link #provisionalAddition} and is not wired.
     */
    public static Optional<Rewrite> sum(IExpr left, IExpr right) {
        if (right instanceof NegationOperationExpr(IExpr taken)) {
            return difference(left, taken);
        }
        // A subtraction written the other way round is still a subtraction: -x + y is y - x.
        if (left instanceof NegationOperationExpr(IExpr taken)) {
            return difference(right, taken);
        }
        return likeTerms(left, right);
    }

    /**
     * Two terms alike in their traction part, whose real parts therefore add.
     * <p>
     * This is what makes {@code 0 + 0} come out as {@code 2·0} rather than as {@code 0}: it runs before the
     * identity, so {@code x + 0 = x} applies exactly where x is NOT a multiple of the point zero. Between
     * them they are the dominance condition REVIEW.md's P1-1 asks for -- the identity holds where x dominates,
     * and where the two are at the same order the real parts add instead.
     * <p>
     * Real parts that negate as terms are the additive erasure and discharge to the point zero. Without that,
     * {@code 1·0 + (-1)·0} would land on a zero real part, which rolls into the exponent and answers
     * {@code 0^2} -- the coordinates doing arithmetic where the term said erasure.
     */
    private static Optional<Rewrite> likeTerms(IExpr left, IExpr right) {
        if (!liftable(left) && !liftable(right)) {
            return Optional.empty();    // two rationals: the coordinates do this, and say so
        }
        return pair(left).flatMap(l -> pair(right)
                .filter(r -> l.t().equals(r.t()))
                .map(r -> negates(l.n(), r.n())
                        ? new Rewrite(ZERO, ERASURE)
                        : new Rewrite(pairOf(plus(l.n(), r.n()), l.t()), LIKE_TERMS)));
    }

    /**
     * {@code 0^a - 0^b = 0^(a÷b)}, E2, given the term subtracted FROM and the term taken away.
     * <p>
     * Total, including at {@code a = b}: {@code z-z} is the additive erasure and discharges to the additive
     * operation's own value, the point zero. Reading it off the term is what makes that exact --
     * {@code a÷a} at {@code a = (2,1)} is the pair {@code (2,2)}, one at coordinates that are not one, and
     * {@code 0^(2,2)} is not the point zero.
     * <p>
     * Bare powers of zero only. {@code 2·0^a - 3·0^b} has no rule and stands, and the additive units are not
     * read as powers of zero at all.
     */
    private static Optional<Rewrite> difference(IExpr from, IExpr taken) {
        if (from.equals(taken)) {
            return Optional.of(new Rewrite(ZERO, ERASURE));
        }
        return exponentOfZero(from).flatMap(a -> exponentOfZero(taken)
                .map(b -> new Rewrite(traction(over(a, b)), DIFFERENCE)));
    }

    // ---------------------------------------------------------------- the unary operations

    /**
     * {@code -(a·0^b) = (-a)·0^b}: negation turns the real coordinate and leaves the traction part alone.
     * <p>
     * So {@code -0} is {@code (-1, 1)}, which is {@code -1·0} and does not reduce -- there is no {@code -0}
     * among the four units, and the theory has not resolved what that product is. The alternative reading,
     * {@code -(0^a) = 0^(a+ω)}, is the same claim routed through the leap; it agrees at {@code -1} and is not
     * needed anywhere the real coordinate can answer.
     */
    public static Optional<Rewrite> negation(IExpr operand) {
        if (operand instanceof TractionLiteral t) {
            return Optional.of(new Rewrite(pairOf(t.real().negated(), t.exponent()), NEGATION));
        }
        // The rational zero has no sign to turn -- the exponent zero has no orientation -- so negating the
        // point zero has to lift it first: 0 is 0^1 by E4, and -(1·0^1) is (-1)·0^1. Leaving it to the
        // coordinates is what used to answer -0 = 0, and that is not what negation is.
        if (ZERO.equals(operand)) {
            return Optional.of(new Rewrite(pairOf(NEG_ONE, ONE), NEGATION));
        }
        return Optional.empty();
    }

    /**
     * {@code 1÷(a·0^b) = (1÷a)·0^(-b)}, E3, and {@code 1÷0 = ω}, E9.
     * <p>
     * E9 is where the old carrier's zero denominator went. A rational cannot hold {@code 1÷0}, so this is an
     * axiom being applied, and a derivation shows it being applied.
     */
    public static Optional<Rewrite> reciprocal(IExpr operand) {
        if (ZERO.equals(operand)) {
            return Optional.of(new Rewrite(OMEGA, OMEGA_DEF));
        }
        if (operand instanceof TractionLiteral t) {
            return Optional.of(new Rewrite(
                    pairOf(t.real().reciprocal(), t.exponent().negated()), RECIPROCAL));
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------- the identities

    /**
     * The identities, each invariant under its own operation: {@code x + 0 = x} and {@code x · 1 = x}.
     * <p>
     * The coordinate layer already does this between two literals. This is the same statement for any x at
     * all -- an atom, a traction, a call -- because it is a claim about the identity rather than about
     * coordinate arithmetic. And the result is the other operand UNCHANGED, which is what invariance means.
     * <p>
     * -1 and ω are invariant under neither, which is what separates them from these two: {@code 1 + ω} has no
     * single value and stands as the pair it is.
     */
    public static Optional<Rewrite> identity(IExpr left, IExpr right, boolean product) {
        if (product) {
            if (ONE.equals(right)) {
                return Optional.of(new Rewrite(left, TIMES_ONE));
            }
            return ONE.equals(left) ? Optional.of(new Rewrite(right, TIMES_ONE)) : Optional.empty();
        }
        if (isMagnitudeZero(right) && dominatesZero(left)) {
            return Optional.of(new Rewrite(left, PLUS_ZERO));
        }
        return isMagnitudeZero(left) && dominatesZero(right)
                ? Optional.of(new Rewrite(right, PLUS_ZERO))
                : Optional.empty();
    }

    /**
     * Whether {@code x + 0 = x} may be applied to this operand: whether it dominates the point zero.
     * <p>
     * The identity is not unconditional, and this is the condition REVIEW.md's P1-1 asks for. Where x is
     * itself at the point zero's order the real parts add instead -- {@code 2·0 + 0} is {@code 3·0}, not
     * {@code 2·0} -- and anywhere else on the vanishing side of the axis, {@code 0^2} or {@code 0^(1÷2)},
     * the sum stands rather than one term swallowing the other. Dominance would answer those, but answering
     * by dominance discards a term, and this type is supposed to conserve them; the two readings are only
     * forced to agree where x has no vanishing part at all, and that is what this allows. What is left is x
     * with no traction part, x on omega's side, and x the carrier cannot lift: an atom, a call, a term that
     * stands.
     * <p>
     * {@code 1 + 0} is the case that matters and it answers 1, which is where QUICK-REFERENCE.md's ledger
     * (P2-5) puts it.
     * <p>
     * Declining also lets the traversal work. This node is tried before its operands are reduced, so an
     * unreduced {@code 2·0} on the left is not yet a pair and the like-terms rule cannot see it; the identity
     * declining is what leaves the next turn to it.
     */
    private static boolean dominatesZero(IExpr x) {
        if (!liftable(x)) {
            return true;
        }
        return pair(x).map(p -> p.t() instanceof RationalLiteral t
                        && t.numerator().signum() * t.denominator().signum() <= 0)
                .orElse(false);
    }

    /**
     * The point zero and its multiples: the rational zero, and any pair at exponent 1.
     * <p>
     * {@code -0} is {@code (-1, 1)} and {@code 0÷2} is {@code ((1,2), 1)}; both are zero, and what
     * distinguishes them is multiplicative -- an orientation, a root -- so neither has anything to contribute
     * to a sum. Not {@code 0^2}: further along the traction axis is not the same point, and a sum with it
     * stands.
     */
    private static boolean isMagnitudeZero(IExpr e) {
        if (e instanceof RationalLiteral r) {
            return r.isZero();
        }
        return e instanceof TractionLiteral t && ONE.equals(t.exponent());
    }

    // ---------------------------------------------------------------- exponentiation

    /**
     * {@code base^exponent}: a traction where the base is zero, the unit table on the four points, and
     * repeated multiplication at an integer exponent.
     * <p>
     * {@code n = 0} is not repeated multiplication of anything -- zero copies is the empty product, and
     * calling that 1 is {@code x^0 = 1}, which this theory does not have; the unit table gives the four
     * points instead, and {@code 2^0} stands. And n has to be an integer <b>as a term</b>: the pair
     * {@code (6, 3)} is a coordinate pair and not the integer 2.
     */
    public static Optional<Rewrite> power(IExpr base, IExpr exponent) {
        if (ZERO.equals(base)) {
            return Optional.of(new Rewrite(traction(exponent), BASE_ZERO));
        }
        Optional<Rewrite> unit = unitPower(base, exponent);
        if (unit.isPresent()) {
            return unit;
        }
        if (!isNonZeroInteger(exponent)) {
            return Optional.empty();
        }
        BigInteger n = ((RationalLiteral) exponent).numerator();
        if (n.signum() > 0 && combines(base)) {
            return Optional.of(new Rewrite(repeated(base, n), INTEGER_POWER));
        }
        // A NEGATIVE power is 1/y^n, and E3 grants that at base zero only. For a general base it is a
        // convention rather than a consequence, so 2^-1 stands.
        return exponentOfZero(base).map(a -> new Rewrite(traction(times(a, exponent)), NEGATIVE_POWER));
    }

    /**
     * The unit exponentiation table: the twelve cells whose base is not zero.
     * <p>
     * Exponentiation is bijective over the units, and each of the four bases gives a different permutation of
     * them. Three cells are ordinary -- {@code 1^1}, {@code (-1)^1}, {@code ω^1}, one copy of the base -- and
     * the rest are the table's own, including the {@code x^0} 4-cycle {@code 0 -> 1 -> ω -> -1 -> 0}. That
     * cycle is the first map on those four points that is not an involution and it has no fixed point, so
     * not {@code x^0 = 1}.
     * <p>
     * A lookup on sixteen cells is a table, and a table cannot be checked by reading it. What checks this one
     * is that every row is a permutation and that the base-zero row is E5, E4, the leap and E3+E9 -- which is
     * why that row is not read from here but derived, in {@link #power} and {@link #point}.
     */
    private static Optional<Rewrite> unitPower(IExpr base, IExpr exponent) {
        int b = unit(base);
        int x = unit(exponent);
        if (b < 0 || x < 0 || b == UNIT_ZERO) {
            return Optional.empty();
        }
        return Optional.of(new Rewrite(value(TABLE[b][x]), UNIT_POWER));
    }

    /**
     * Ordered {@code 0, 1, ω, -1}, the order the cycle takes them in. Read as {@code TABLE[base][exponent]}.
     * <pre>
     * 0^0=1   0^1=0   0^ω=-1  0^-1=ω
     * 1^0=ω   1^1=1   1^ω=0   1^-1=-1
     * ω^0=-1  ω^1=ω   ω^ω=1   ω^-1=0
     * -1^0=0  -1^1=-1 -1^ω=ω  -1^-1=1
     * </pre>
     */
    private static final int[][] TABLE = {
            {1, 0, 3, 2},
            {2, 1, 0, 3},
            {3, 2, 1, 0},
            {0, 3, 2, 1},
    };

    /**
     * {@code log_0(0^a) = a}, which is E8, and the unit logarithm table at the other three bases.
     * <p>
     * E8 reads the four points where the arithmetic rules will not, and that is not the axis rule being bent:
     * inverting is not arithmetic, no addition law is involved, the result is the exponent itself, and nothing
     * anywhere produces a log for this to feed.
     */
    public static Optional<Rewrite> logarithm(IExpr base, IExpr operand) {
        if (ZERO.equals(base)) {
            Optional<IExpr> exponent = exponentOfZero(operand);
            if (exponent.isPresent()) {
                return exponent.map(a -> new Rewrite(a, LOG_INVERTS));
            }
            // The inverse of point(): 1 is 0^0 by E5, and -1 is 0^ω by the leap. Neither is something
            // exponentOfZero will say, and the second is the leap read backwards, so it cites the table.
            if (ONE.equals(operand)) {
                return Optional.of(new Rewrite(ZERO, LOG_INVERTS));
            }
            return NEG_ONE.equals(operand) ? Optional.of(new Rewrite(OMEGA, UNIT_LOG)) : Optional.empty();
        }
        int b = unit(base);
        int x = unit(operand);
        if (b < 0 || x < 0) {
            return Optional.empty();
        }
        // The inverse of the table: log_b(v) is the exponent whose cell holds v. Every row is a permutation,
        // so there is exactly one.
        for (int exponent = 0; exponent < TABLE[b].length; exponent++) {
            if (TABLE[b][exponent] == x) {
                return Optional.of(new Rewrite(value(exponent), UNIT_LOG));
            }
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------- provisional, and deliberately unwired

    /**
     * The traction addition law, {@code a·0^b + c·0^d = (a+c)·a^d·c^b·0^(bd)}.
     * <p>
     * Not wired, and it cannot simply be switched on. Its derivation needs the general involution off the
     * closure set -- theory-problems.md #4 -- and a power rule at general exponents, which the theory does
     * not have in the ω-direction. But the reason it is not wired is more immediate than either: the formula
     * disagrees with the rest of the theory at three edges.
     * <ul>
     * <li>{@code 0 + 1} is 2. With {@code 0 = 1·0^1} and {@code 1 = 1·0^0} the real parts add though the
     *     traction parts are at different orders, and "the two axes do not mix" says that sum stands.</li>
     * <li>{@code a^d} at {@code d = 0} has to read 1 for {@code 1 + 1} to be 2, and the {@code x^0} cycle
     *     says {@code 1^0} is ω. So the factor is a shadow reading and not this theory's power.</li>
     * <li>At {@code b = d} it gives {@code ω + ω = 2·0}, where distributivity gives {@code 2ω}.</li>
     * </ul>
     * Underneath all three, the real part 0 is doing two jobs: {@code (a+c)} reads that slot as the number
     * zero and {@code a^d} reads it as ∅. Each choice fixes one edge and breaks another.
     */
    public static Optional<Rewrite> provisionalAddition(IExpr left, IExpr right) {
        return pair(left).flatMap(l -> pair(right).map(r -> new Rewrite(
                pairOf(times(times(plus(l.n(), r.n()), raise(l.n(), r.t())), raise(r.n(), l.t())),
                        times(l.t(), r.t())),
                ADDITION_LAW)));
    }

    /**
     * {@code 0^(0^n) = n} off the closure set: E6 read as a general involution.
     * <p>
     * Not wired. On the four points the tables have it, and there it is forced. Off them it is
     * theory-problems.md #4, the largest unpaid assumption the theory has -- E2 depends on it in the older
     * docs, and so does the addition law by the same conjugation. Wiring it also makes every value a power of
     * zero, and the axis restriction exists because that does not terminate.
     */
    public static Optional<Rewrite> provisionalInvolution(IExpr e) {
        return exponentOfZero(e).flatMap(TractionRules::exponentOfZero)
                .map(n -> new Rewrite(n, INVOLUTION));
    }

    /**
     * {@code -1·0 = ω}, conjectured, and what refutes it.
     * <p>
     * Follow it through the primitives. {@code -1·0} is {@code 0^ω · 0^1}, which is {@code 0^(ω+1)} by E1, and
     * the conjecture makes that {@code 0^-1}, so E6 gives {@code ω + 1 = -1} and hence {@code ω = -2} as an
     * exponent. Then {@code -1 = 0^ω = 0^-2}, whose reciprocal is {@code 0^2} by E3; but {@code -1} is its own
     * reciprocal, so {@code 0^2 = 0^-2}, and E6 gives {@code 2 = -2}.
     * <p>
     * So the pair {@code (-1, 1)} stands. It is not a distinguished element, and there is no rule for it.
     */
    public static Optional<Rewrite> provisionalMinusZero(IExpr e) {
        if (e instanceof TractionLiteral t && NEG_ONE.equals(t.real()) && ONE.equals(t.exponent())) {
            return Optional.of(new Rewrite(OMEGA, MINUS_ZERO));
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------- the four units, as an index

    private static final int UNIT_ZERO = 0;

    /** Which of the four units this term is, in the cycle's order, or -1 for anything else. */
    private static int unit(IExpr e) {
        if (ZERO.equals(e)) {
            return 0;
        }
        if (ONE.equals(e)) {
            return 1;
        }
        if (OMEGA.equals(e)) {
            return 2;
        }
        return NEG_ONE.equals(e) ? 3 : -1;
    }

    private static IExpr value(int unit) {
        return switch (unit) {
            case 0 -> ZERO;
            case 1 -> ONE;
            case 2 -> OMEGA;
            default -> NEG_ONE;
        };
    }

    // ---------------------------------------------------------------- the exponent arithmetic

    private record Pair(IExpr n, IExpr t) {
    }

    // The exponent arithmetic is BUILT and not performed. A rule that reduced its own exponent would be doing
    // work no derivation could show -- 0·0 would arrive at 0^2 with the step from 1+1 to 2 missing -- and
    // since the derivation is the evaluator, work that cannot be shown is work that does not happen. The
    // driver reduces these on the turns that follow.

    private static IExpr plus(IExpr a, IExpr b) {
        return new AdditionOperationExpr(a, b);
    }

    private static IExpr minus(IExpr a, IExpr b) {
        return new AdditionOperationExpr(a, new NegationOperationExpr(b));
    }

    private static IExpr times(IExpr a, IExpr b) {
        return new MultiplicationOperationExpr(a, b);
    }

    private static IExpr over(IExpr a, IExpr b) {
        return new MultiplicationOperationExpr(a, new ReciprocalOperationExpr(b));
    }

    private static IExpr raise(IExpr a, IExpr b) {
        return new ExponentialOperationExpr(a, b);
    }

    /** Whether {@code b} is {@code -a} as a TERM: the negation of it, or its coordinates with the sign turned. */
    private static boolean negates(IExpr a, IExpr b) {
        if (b instanceof NegationOperationExpr(IExpr of) && of.equals(a)) {
            return true;
        }
        if (a instanceof NegationOperationExpr(IExpr of) && of.equals(b)) {
            return true;
        }
        return a instanceof RationalLiteral(BigInteger an, BigInteger ad)
                && b instanceof RationalLiteral(BigInteger bn, BigInteger bd)
                // A zero exponent is its own negation, and 1·1 is not an erasure.
                && an.signum() != 0
                && ad.equals(bd)
                && an.negate().equals(bn);
    }

    /**
     * Whether writing the copies out will actually join them back up.
     * <p>
     * {@code x^2} is not improved by becoming {@code x·x} -- the power is the better spelling of the same
     * term, and {@code (x^2)^2} would unfold to four x's for nothing. So the copies are only written where
     * something multiplies them: a coordinate, a pair, or a value that reads as a power of zero.
     */
    private static boolean combines(IExpr base) {
        return base instanceof RationalLiteral || base instanceof TractionLiteral
                || exponentOfZero(base).isPresent();
    }

    /**
     * {@code base} multiplied by itself n times, which is what a positive integer power IS.
     * <p>
     * No traction reading is needed for this and none is taken: it is the meaning of the notation, and it is
     * also the whole proof of the integer power rule. Where the base is {@code 0^a} the product rule turns
     * the n copies into {@code 0^(na)} on its own, so the two are one mechanism -- {@code (0^2)^3} lands on
     * {@code 0^6} by the same path that takes {@code 2^3} to 8.
     */
    private static IExpr repeated(IExpr base, BigInteger n) {
        IExpr out = base;
        for (BigInteger i = BigInteger.ONE; i.compareTo(n) < 0; i = i.add(BigInteger.ONE)) {
            out = new MultiplicationOperationExpr(out, base);
        }
        return out;
    }

    private static boolean isNonZeroInteger(IExpr e) {
        return e instanceof RationalLiteral r && r.isInteger() && !r.isZero();
    }
}
