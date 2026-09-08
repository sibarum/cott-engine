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
import sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral;
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
 * <h2>The projective layer is in the trace too</h2>
 * The steps that have gone wrong here were coordinate arithmetic, not traction rules — {@code w+w} landing on
 * 1, {@code 0·w} answered by two pairs multiplying before any rule was consulted. A derivation that recorded
 * only the interesting-looking layer would have missed every one of them, so the pairs report themselves as
 * well, and say that is what they are.
 */
public final class Deriver {

    /** What the coordinates do on their own, which is a model's arithmetic rather than a claim of the theory. */
    public static final Rule PROJECTIVE =
            new Rule("the coordinates combine", "projective arithmetic", Rule.Status.PROVEN);

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
     * <p>A rewrite that returns what it was given is not a step. That is how an open cell reports itself —
     * {@link TractionRules#STANDS} — and recording it would be both a lie and a loop.
     */
    public static Optional<Rewrite> step(IExpr e) {
        // OUTERMOST FIRST, and the order is load-bearing. A rule that matches on a shape can have that shape
        // taken away by a rewrite inside it: E10 matches Addition(x, Negation(y)), and reducing Negation(1) to
        // the literal -1 first leaves an ordinary sum it can never see again. That is why E10 was reaching
        // tractions and never the four points -- 0^3 has no coordinate negation to collapse into, and 1 does.
        //
        // Descending second is not a compromise. A rule whose operands are not ready declines, the children
        // are reduced, and the next turn tries the shape again; trying it first only ever costs a failed
        // match.
        Optional<Rewrite> mine = here(e).filter(rewrite -> !rewrite.result().equals(e));
        if (mine.isPresent()) {
            return mine;
        }
        return inChildren(e);
    }

    /** The first rewrite available inside {@code e}, with the whole expression rebuilt around it. */
    private static Optional<Rewrite> inChildren(IExpr e) {
        return switch (e) {
            case MultiplicationOperationExpr(IExpr l, IExpr r) ->
                    left(l, x -> new MultiplicationOperationExpr(x, r))
                            .or(() -> left(r, x -> new MultiplicationOperationExpr(l, x)));
            case AdditionOperationExpr(IExpr l, IExpr r) ->
                    left(l, x -> new AdditionOperationExpr(x, r))
                            .or(() -> left(r, x -> new AdditionOperationExpr(l, x)));
            case ExponentialOperationExpr p ->
                    left(p.base(), x -> new ExponentialOperationExpr(x, p.exponent()))
                            .or(() -> left(p.exponent(), x -> new ExponentialOperationExpr(p.base(), x)));
            case TractionLiteral t ->
                    left(t.base(), x -> new TractionLiteral(x, t.exp()))
                            .or(() -> left(t.exp(), x -> new TractionLiteral(t.base(), x)));
            case NegationOperationExpr(IExpr operand) ->
                    left(operand, NegationOperationExpr::new);
            case ReciprocalOperationExpr(IExpr operand) ->
                    left(operand, ReciprocalOperationExpr::new);
            case CallExpr c -> inArguments(c);
            default -> Optional.empty();
        };
    }

    private static Optional<Rewrite> left(IExpr child, java.util.function.UnaryOperator<IExpr> rebuild) {
        return step(child).map(rewrite -> new Rewrite(rebuild.apply(rewrite.result()), rewrite.rule()));
    }

    private static Optional<Rewrite> inArguments(CallExpr call) {
        List<IExpr> args = call.args();
        for (int i = 0; i < args.size(); i++) {
            Optional<Rewrite> rewrite = step(args.get(i));
            if (rewrite.isPresent()) {
                List<IExpr> next = new ArrayList<>(args);
                next.set(i, rewrite.get().result());
                return Optional.of(new Rewrite(new CallExpr(call.name(), next), rewrite.get().rule()));
            }
        }
        return Optional.empty();
    }

    /** A rewrite of this node itself: the traction rules first, then what the coordinates can do. */
    private static Optional<Rewrite> here(IExpr e) {
        return switch (e) {
            // The rules come before the identities, and the order is not cosmetic. 0 - 0 is both "0 added to
            // something" and the additive erasure, and the identity reading answers -0 while the erasure
            // answers 0. The erasure is the more specific match and it is the right one, so it goes first.
            case MultiplicationOperationExpr(IExpr l, IExpr r) ->
                    TractionRules.product(l, r)
                            .or(() -> TractionRules.identity(l, r, true))
                            .or(() -> projective(l.times(r), e));
            case AdditionOperationExpr(IExpr l, IExpr r) ->
                    TractionRules.sum(l, r)
                            .or(() -> TractionRules.identity(l, r, false))
                            .or(() -> projective(l.plus(r), e));
            case ExponentialOperationExpr p -> TractionRules.power(p.base(), p.exponent());
            case TractionLiteral t -> TractionRules.point(t.base(), t.exp());
            case LogarithmOperationExpr l -> TractionRules.logarithm(l.base(), l.operand());
            // Uncovering an operand from under two negations is not the coordinates combining, and labelling
            // it that way would have put a false reason in a derivation. It is reversibility, which is what
            // COTT asks of every operation before anything else.
            case NegationOperationExpr(NegationOperationExpr(IExpr under)) ->
                    Optional.of(new Rewrite(under, NEGATION_INVOLUTION));
            case ReciprocalOperationExpr(ReciprocalOperationExpr(IExpr under)) ->
                    Optional.of(new Rewrite(under, RECIPROCAL_INVOLUTION));
            case NegationOperationExpr(IExpr operand) -> projective(operand.negated(), e);
            case ReciprocalOperationExpr(IExpr operand) -> projective(operand.reciprocal(), e);
            default -> Optional.empty();
        };
    }

    /** A coordinate answer, reported only where it actually combined rather than rebuilding the node. */
    private static Optional<Rewrite> projective(IExpr result, IExpr before) {
        return result.equals(before) ? Optional.empty() : Optional.of(new Rewrite(result, PROJECTIVE));
    }
}
