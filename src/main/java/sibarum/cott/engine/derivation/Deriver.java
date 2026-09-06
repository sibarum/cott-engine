package sibarum.cott.engine.derivation;

import sibarum.cott.engine.base.expr.CallExpr;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.rule.Rewrite;
import sibarum.cott.engine.base.rule.Rule;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.ExponentialOperationExpr;
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
 * <h2>Small steps, whole terms</h2>
 * {@code simplify()} answers in one recursive pass, which is what an evaluator should do and what a plotter
 * sampling a thousand points needs. This walks the same rules but applies one rewrite per turn and hands back
 * the entire expression each time, so the result reads as a chain of equalities rather than as a log of what
 * the evaluator did to itself. The rewrite chosen is the leftmost innermost one, which is the order
 * {@code simplify()} works in — operands before the operation.
 *
 * <h2>One set of rules, not two</h2>
 * Nothing here reimplements a rule. {@link TractionRules} hands back what it did and why, and the fast path
 * simply drops the why; this keeps it. A tracing evaluator that runs its own copy of the rules can disagree
 * with the real one, and a proof of something the engine did not do is worse than no proof — so
 * {@code DerivationTest} pins the two together: the last term of a derivation is what {@code simplify()}
 * returns.
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

    /**
     * Enough turns for any expression a person will type, and a stop rather than a hang if a rule is ever
     * written that undoes another.
     */
    private static final int LIMIT = 10_000;

    private Deriver() {
    }

    /** Simplify {@code expr}, keeping every rewrite. */
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
        Optional<Rewrite> inner = inChildren(e);
        if (inner.isPresent()) {
            return inner;
        }
        return here(e).filter(rewrite -> !rewrite.result().equals(e));
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
            case MultiplicationOperationExpr(IExpr l, IExpr r) ->
                    TractionRules.product(l, r).or(() -> projective(l.times(r), e));
            case AdditionOperationExpr(IExpr l, IExpr r) ->
                    TractionRules.sum(l, r).or(() -> projective(l.plus(r), e));
            case ExponentialOperationExpr p -> TractionRules.power(p.base(), p.exponent());
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
