package sibarum.cott.engine.derivation;

import sibarum.cott.engine.base.expr.CallExpr;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.rule.Rewrite;
import sibarum.cott.engine.base.rule.Rule;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.ExponentialOperationExpr;
import sibarum.cott.engine.operation.binary.LogarithmOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.traction.expr.TractionLiteral;
import sibarum.cott.engine.traction.rule.TractionRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Simplification, one rewrite at a time, with a record of each.
 *
 * <h2>There is no other evaluator</h2>
 * This is it. {@link IExpr#simplify()} is this walk with the reasons dropped, so an answer and its account of
 * itself cannot come apart: not by a rule changing under one of them, not by the order they consult things
 * drifting, not by one being optimised and the other left behind. There was a second, faster path here for a
 * while, and the reason it is gone is that nothing about it could be verified except by testing the two
 * against each other on inputs somebody thought to write down.
 *
 * <h2>Small steps, whole terms</h2>
 * One rewrite per turn, the entire expression handed back each time, so the result reads as a chain of
 * equalities rather than as a log of what the evaluator did to itself. The rewrite chosen is the leftmost
 * innermost one: operands before the operation.
 *
 * <p>Rules do not reduce what they build, either. {@link TractionRules} returns {@code 0^(1+1)} and this
 * reduces the exponent on the next turn, because a rule that finished its own arithmetic would be doing work
 * no derivation could show -- and work that cannot be shown is work that does not happen here.
 *
 * <h2>The coordinate layer is in the trace too</h2>
 * The steps that have gone wrong here were coordinate arithmetic, not traction rules — {@code w+w} landing on
 * 1, {@code 0·w} answered by two pairs multiplying before any rule was consulted. A derivation that recorded
 * only the interesting-looking layer would have missed every one of them, so the pairs report themselves as
 * well, and say that is what they are.
 */
public final class Deriver {

    /** What the coordinates do on their own, which is a model's arithmetic rather than a claim of the theory. */
    public static final Rule COORDINATES =
            new Rule("the coordinates combine", "coordinate arithmetic", Rule.Status.PROVEN);

    /** Reversibility, which COTT asks of an operation before it asks anything else. */
    public static final Rule NEGATION_INVOLUTION =
            new Rule("-(-x) = x", "negation is reversible", Rule.Status.PROVEN);
    public static final Rule RECIPROCAL_INVOLUTION =
            new Rule("1÷(1÷x) = x", "the reciprocal is reversible", Rule.Status.PROVEN);

    /**
     * Enough turns for any expression a person will type, and a stop rather than a hang if a rule is ever
     * written that undoes another.
     */
    private static final int LIMIT = 10_000;

    private Deriver() {
    }

    /**
     * Simplify {@code expr}, keeping every rewrite.
     *
     * <p>This is the evaluator. {@link IExpr#simplify()} is this and then the last term, so there is no second
     * path that could answer differently from the one that explains itself -- the audit and the result are
     * the same walk, and a divergence between them is not something that has to be tested for because there
     * is nothing to diverge.
     */
    public static Derivation derive(IExpr expr) {
        List<Step> steps = new ArrayList<>();
        IExpr current = expr;
        for (int i = 0; i < LIMIT; i++) {
            Optional<Rewrite> next = step(current);
            if (next.isEmpty()) {
                return new Derivation(expr, current, steps);
            }
            steps.add(new Step(current, next.get().result(), next.get().rule()));
            current = next.get().result();
        }
        throw new IllegalStateException("a rewrite here does not settle: " + expr);
    }

    /**
     * One rewrite of the whole expression, innermost first, or empty when nothing applies.
     *
     * <p>Public so that a layer above can interleave its own rewrites with these -- the syntax layer answers
     * the real-valued calls, and those belong in the same chain as the algebra rather than in a chain of
     * their own.
     *
     * <p>A rewrite that returns what it was given is not a step. That is how an open cell reports itself, and
     * recording it would be both a lie and a loop.
     *
     * <h2>Three phases, and the order is load-bearing</h2>
     * <ol>
     * <li><b>The rules that match on a shape</b>, outermost first. A shape can be taken away by a rewrite
     * inside it: E2 matches {@code Addition(x, Negation(y))}, and reducing {@code Negation(0)} to the pair
     * {@code (-1, 1)} first leaves an ordinary sum it can never see again. That is why E2 was reaching
     * tractions and never the four points.</li>
     * <li><b>The operands.</b> A rule whose operands are not ready declines, they are reduced, and the next
     * turn tries the shape again -- so trying the shape first only ever costs a failed match.</li>
     * <li><b>The identities, and what the coordinates can do.</b> Last, because both need their operands
     * settled to be right. {@code x + 0 = x} is not unconditional -- it does not hold where x is itself at
     * the point zero's order -- and {@code 2·0 + 0} cannot be told apart from {@code x + 0} until the
     * {@code 2·0} has become a pair. Running the identity first answered {@code 2·0} there, where
     * distributivity answers {@code 3·0}.</li>
     * </ol>
     */
    public static Optional<Rewrite> step(IExpr e) {
        return step(e, false);
    }

    /**
     * One rewrite, knowing whether this term is an exponent or a value.
     *
     * <h2>The one place the two sorts are told apart</h2>
     * They have to be. The rational zero is the absence marker in an exponent and the point zero as a value,
     * and the same node means different things in the two places: {@code 1 + 0} as an exponent is 1, because
     * zero is what the exponents add without effect, while as a value it is a sum holding the point zero and
     * it stands. Getting that wrong is what let a coordinate sum absorb a point zero that the traction rules
     * had stopped absorbing -- {@code 0 + (0 + 1)} answered 1, two zeros gone.
     *
     * <p>This is the narrow version of REVIEW.md's P0-4. It distinguishes the sorts by POSITION, which is all
     * the walk needs, rather than by giving them separate types.
     */
    public static Optional<Rewrite> step(IExpr e, boolean inExponent) {
        Optional<Rewrite> shape = shape(e, inExponent).filter(rewrite -> !rewrite.result().equals(e));
        if (shape.isPresent()) {
            return shape;
        }
        Optional<Rewrite> inside = inChildren(e, inExponent);
        if (inside.isPresent()) {
            return inside;
        }
        return settle(e, inExponent).filter(rewrite -> !rewrite.result().equals(e));
    }

    /** The first rewrite available inside {@code e}, with the whole expression rebuilt around it. */
    private static Optional<Rewrite> inChildren(IExpr e, boolean inExponent) {
        return switch (e) {
            case MultiplicationOperationExpr(IExpr l, IExpr r) ->
                    left(l, inExponent, x -> new MultiplicationOperationExpr(x, r))
                            .or(() -> left(r, inExponent, x -> new MultiplicationOperationExpr(l, x)));
            case AdditionOperationExpr(IExpr l, IExpr r) ->
                    left(l, inExponent, x -> new AdditionOperationExpr(x, r))
                            .or(() -> left(r, inExponent, x -> new AdditionOperationExpr(l, x)));
            // The exponent slots, and the only places the sort changes.
            case ExponentialOperationExpr p ->
                    left(p.base(), inExponent, x -> new ExponentialOperationExpr(x, p.exponent()))
                            .or(() -> left(p.exponent(), true, x -> new ExponentialOperationExpr(p.base(), x)));
            case TractionLiteral t ->
                    left(t.real(), inExponent, x -> new TractionLiteral(x, t.exponent()))
                            .or(() -> left(t.exponent(), true, x -> new TractionLiteral(t.real(), x)));
            // A log's operands were never descended into, which went unnoticed while the only log rules read
            // literals the parser produces directly. log(-1, 0) is the leap read backwards and the parser
            // hands it Negation(1), so the cell could not be reached until this case existed.
            case LogarithmOperationExpr l ->
                    left(l.operand(), inExponent, x -> new LogarithmOperationExpr(l.base(), x))
                            .or(() -> left(l.base(), inExponent, x -> new LogarithmOperationExpr(x, l.operand())));
            case NegationOperationExpr(IExpr operand) ->
                    left(operand, inExponent, NegationOperationExpr::new);
            case ReciprocalOperationExpr(IExpr operand) ->
                    left(operand, inExponent, ReciprocalOperationExpr::new);
            case CallExpr c -> inArguments(c, inExponent);
            default -> Optional.empty();
        };
    }

    private static Optional<Rewrite> left(IExpr child, boolean inExponent,
                                          java.util.function.UnaryOperator<IExpr> rebuild) {
        return step(child, inExponent).map(rewrite -> new Rewrite(rebuild.apply(rewrite.result()), rewrite.rule()));
    }

    private static Optional<Rewrite> inArguments(CallExpr call, boolean inExponent) {
        List<IExpr> args = call.args();
        for (int i = 0; i < args.size(); i++) {
            Optional<Rewrite> rewrite = step(args.get(i), inExponent);
            if (rewrite.isPresent()) {
                List<IExpr> next = new ArrayList<>(args);
                next.set(i, rewrite.get().result());
                return Optional.of(new Rewrite(new CallExpr(call.name(), next), rewrite.get().rule()));
            }
        }
        return Optional.empty();
    }

    /**
     * Phase one: a rule that matches on this node's shape.
     * <p>
     * These come before the identities, and the order is not cosmetic. {@code 0 - 0} is both "0 added to
     * something" and the additive erasure, and the identity reading answers {@code -0} while the erasure
     * answers 0. The erasure is the more specific match and it is the right one, so it goes first.
     */
    private static Optional<Rewrite> shape(IExpr e, boolean inExponent) {
        return switch (e) {
            // The rules take the sort with them: in an exponent the rational zero is the absence marker and
            // is not lifted to the point zero, so a sum of exponents is coordinate arithmetic. The erasures
            // still apply there -- z-z is an erasure wherever it is written.
            case MultiplicationOperationExpr(IExpr l, IExpr r) -> TractionRules.product(l, r, inExponent);
            case AdditionOperationExpr(IExpr l, IExpr r) -> TractionRules.sum(l, r, inExponent);
            case ExponentialOperationExpr p -> TractionRules.power(p.base(), p.exponent());
            case sibarum.cott.engine.rational.expr.RationalLiteral r -> TractionRules.zeroCoordinates(r, inExponent);
            case TractionLiteral t -> TractionRules.point(t);
            case LogarithmOperationExpr l -> TractionRules.logarithm(l.base(), l.operand());
            // Uncovering an operand from under two negations is not the coordinates combining, and labelling
            // it that way would have put a false reason in a derivation. It is reversibility, which is what
            // COTT asks of every operation before anything else.
            case NegationOperationExpr(NegationOperationExpr(IExpr under)) ->
                    Optional.of(new Rewrite(under, NEGATION_INVOLUTION));
            case ReciprocalOperationExpr(ReciprocalOperationExpr(IExpr under)) ->
                    Optional.of(new Rewrite(under, RECIPROCAL_INVOLUTION));
            // The pair's own negation and reciprocal are named rules, not coordinates combining: negation
            // distributes over the product and the reciprocal is E3. 1÷0 belongs to the same method, because
            // a rational cannot hold it -- that is E9 being applied, and a derivation should say so.
            case NegationOperationExpr(IExpr operand) -> TractionRules.negation(operand, inExponent);
            case ReciprocalOperationExpr(IExpr operand) -> TractionRules.reciprocal(operand);
            default -> Optional.empty();
        };
    }

    /**
     * Phase three: the identities, and what the coordinates can do between two settled operands.
     * <p>
     * Last of the three, because both need their operands settled. The coordinates cannot combine what is not
     * yet a literal, and {@code x + 0 = x} has a condition on x that cannot be checked until x is one.
     */
    private static Optional<Rewrite> settle(IExpr e, boolean inExponent) {
        return switch (e) {
            case MultiplicationOperationExpr(IExpr l, IExpr r) ->
                    TractionRules.identity(l, r, true).or(() -> coordinates(l.times(r), e));
            case AdditionOperationExpr(IExpr l, IExpr r) ->
                    inExponent || TractionRules.addsAsCoordinates(l, r)
                            ? coordinates(l.plus(r), e) : Optional.empty();
            case NegationOperationExpr(IExpr operand) -> coordinates(operand.negated(), e);
            case ReciprocalOperationExpr(IExpr operand) -> coordinates(operand.reciprocal(), e);
            default -> Optional.empty();
        };
    }

    /** A coordinate answer, reported only where it actually combined rather than rebuilding the node. */
    private static Optional<Rewrite> coordinates(IExpr result, IExpr before) {
        return result.equals(before) ? Optional.empty() : Optional.of(new Rewrite(result, COORDINATES));
    }
}
