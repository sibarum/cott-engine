package sibarum.cott;

import sibarum.cott.engine.base.expr.AtomExpr;
import sibarum.cott.engine.base.expr.CallExpr;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.ExponentialOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.base.rule.Rewrite;
import sibarum.cott.engine.base.rule.Rule;
import sibarum.cott.engine.derivation.Derivation;
import sibarum.cott.engine.derivation.Deriver;
import sibarum.cott.engine.derivation.Step;
import sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The front of the syntax layer: a typed string in, a displayable string out.
 *
 * <h2>This is not the evaluator any more</h2>
 * The class of this name used to hold the rules. It does not now — the theory lives in the engine, in
 * {@code simplify()}, and this is the path an entry takes to get there and back: normalize, parse, expand the
 * session's names, answer the real-valued calls, simplify, render. Every step is somebody else's, and the order
 * is the only thing here.
 *
 * <p>Where the theory has no answer the term stands, and that is the engine being honest rather than a fault
 * in this path: {@code 0·w} comes back as {@code 0ω} because Problem 1 is open.
 *
 * <h2>Where the real functions are answered</h2>
 * A {@link Real} call is folded here and not in the engine, because it is not theory: it is a catalogue of
 * approximations, and the engine is exact. Keeping it on this side is also what lets pi and e have real
 * readings without the carrier having to know their names — see {@link #real}.
 */
public final class Cott {

    private Cott() {
    }

    /** Normalize, parse, expand, answer, simplify and render an entry, with nothing defined. */
    public static String evaluate(String entry) {
        return evaluate(entry, Bindings.EMPTY);
    }

    /** As {@link #evaluate(String)}, in a session's vocabulary. */
    public static String evaluate(String entry, Bindings session) {
        IExpr parsed = Parser.parse(Notation.normalize(entry, session), session);
        return Render.show(reduce(session.expand(parsed)));
    }

    /**
     * Answer what can be answered: the real-valued calls are folded, and the engine simplifies the rest.
     *
     * <p>Calls first, because an answered {@code sin(2)} is a coordinate the arithmetic around it can then
     * combine, while an unanswered one is a term that stands and stops that arithmetic — which is the correct
     * outcome and not one to reach a step early.
     */
    public static IExpr reduce(IExpr e) {
        return derive(e).to();
    }

    /**
     * The whole solve, kept: normalize, parse, expand, and every rewrite from there to the answer.
     *
     * <p>The real-valued calls are in the same chain as the algebra rather than in one of their own, because
     * an audit of "the answer" that stopped at the engine boundary would leave out the one place this engine
     * approximates — which is exactly the step a reader most needs to see.
     */
    public static Derivation derive(String entry) {
        return derive(entry, Bindings.EMPTY);
    }

    /** As {@link #derive(String)}, in a session's vocabulary. */
    public static Derivation derive(String entry, Bindings session) {
        return derive(session.expand(Parser.parse(Notation.normalize(entry, session), session)));
    }

    /**
     * The derivation of an expression already parsed and expanded.
     *
     * <p>{@link #reduce} is this and then the last term. There is no second walk that answers without
     * explaining, so the two cannot come apart -- not by a rule changing under one of them, and not by the
     * order they consult things drifting, because there is only one order.
     */
    public static Derivation derive(IExpr from) {
        List<Step> steps = new ArrayList<>();
        IExpr current = from;
        while (true) {
            // Calls first, for the same reason reduce does it: an answered sin(2) is a coordinate the
            // arithmetic around it can then combine, and an unanswered one stops that arithmetic correctly.
            IExpr term = current;
            Optional<Rewrite> next = call(term).or(() -> Deriver.step(term));
            if (next.isEmpty()) {
                return new Derivation(from, current, steps);
            }
            steps.add(new Step(current, next.get().result(), next.get().rule()));
            current = next.get().result();
        }
    }

    /** One real-valued call answered, anywhere in the expression, with the whole expression rebuilt. */
    private static Optional<Rewrite> call(IExpr e) {
        if (e instanceof CallExpr c) {
            for (int i = 0; i < c.args().size(); i++) {
                Optional<Rewrite> inner = call(c.args().get(i));
                if (inner.isPresent()) {
                    List<IExpr> args = new ArrayList<>(c.args());
                    args.set(i, inner.get().result());
                    return Optional.of(new Rewrite(new CallExpr(c.name(), args), inner.get().rule()));
                }
            }
            Real fn = Real.of(c.name());
            IExpr answered = fn == null ? null : answer(fn, c.args());
            return answered == null ? Optional.empty() : Optional.of(new Rewrite(answered, approximation(fn)));
        }
        return children(e).stream()
                .map(child -> call(child.expr()).map(r -> new Rewrite(child.rebuild().apply(r.result()), r.rule())))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Rule approximation(Real fn) {
        return new Rule(fn.label() + " of a real reading, rounded to " + Real.PLACES + " places",
                "Real: a catalogue, not theory", Rule.Status.APPROXIMATE);
    }

    /** The sub-expressions a call could be hiding in, each with the way to put it back. */
    private record Child(IExpr expr, java.util.function.UnaryOperator<IExpr> rebuild) {
    }

    private static List<Child> children(IExpr e) {
        return switch (e) {
            case AdditionOperationExpr(IExpr l, IExpr r) -> List.of(
                    new Child(l, x -> new AdditionOperationExpr(x, r)),
                    new Child(r, x -> new AdditionOperationExpr(l, x)));
            case MultiplicationOperationExpr(IExpr l, IExpr r) -> List.of(
                    new Child(l, x -> new MultiplicationOperationExpr(x, r)),
                    new Child(r, x -> new MultiplicationOperationExpr(l, x)));
            case ExponentialOperationExpr p -> List.of(
                    new Child(p.base(), x -> new ExponentialOperationExpr(x, p.exponent())),
                    new Child(p.exponent(), x -> new ExponentialOperationExpr(p.base(), x)));
            case NegationOperationExpr(IExpr operand) -> List.of(new Child(operand, NegationOperationExpr::new));
            case ReciprocalOperationExpr(IExpr operand) -> List.of(new Child(operand, ReciprocalOperationExpr::new));
            default -> List.of();
        };
    }

    /** The call's answer, or null where an argument has no real reading and the term therefore stands. */
    private static IExpr answer(Real fn, List<IExpr> args) {
        List<Double> reals = new ArrayList<>(args.size());
        for (IExpr arg : args) {
            Optional<Double> value = real(arg);
            if (value.isEmpty()) {
                return null;
            }
            reals.add(value.get());
        }
        return fn.apply(reals);
    }

    /**
     * The real reading of an expression: an ordinary numeral, pi, e, or anything built out of those.
     *
     * <p>Separate from {@link IExpr#evaluate} on purpose. That is the carrier's own projection and knows
     * nothing of pi — nor should it, since pi is an atom precisely because the theory cannot construct it. This
     * is the display's reading, and the two differ in exactly one place, which is the place the layering puts
     * them.
     *
     * <p>Empty where any part of the expression is not a number, which is how {@code sin(x)} stands.
     */
    static Optional<Double> real(IExpr e) {
        return switch (e) {
            // A zero denominator is omega, which has no real reading. The CARRIER projects it onto zero's
            // shadow, and that is right for a projection -- omega and zero do occupy the same coordinate on
            // the real line -- but a shadow is not a value, and handing it to sin would answer a question
            // nobody can ask. sin(ω) stands.
            case ProjectiveRationalLiteral p -> p.denominator().signum() == 0 ? Optional.empty() : p.evaluate();
            case AtomExpr a -> switch (a.name()) {
                case "π" -> Optional.of(Math.PI);
                case "e" -> Optional.of(Math.E);
                default -> Optional.empty();
            };
            case NegationOperationExpr n -> real(n.operand()).map(v -> -v);
            // Omega shares zero's shadow, which is the carrier's convention and is kept here so that a
            // reciprocal reads the same on both sides of the layering.
            case ReciprocalOperationExpr i -> real(i.operand()).map(v -> v == 0.0 ? 0.0 : 1.0 / v);
            case AdditionOperationExpr a -> both(a.left(), a.right(), Double::sum);
            case MultiplicationOperationExpr m -> both(m.left(), m.right(), (l, r) -> l * r);
            case ExponentialOperationExpr p -> both(p.base(), p.exponent(), Math::pow)
                    .filter(Double::isFinite);
            case CallExpr c -> {
                Real fn = Real.of(c.name());
                IExpr answered = fn == null ? null : answer(fn, c.args());
                yield answered == null ? Optional.empty() : real(answered);
            }
            default -> Optional.empty();
        };
    }

    private static Optional<Double> both(IExpr left, IExpr right, java.util.function.DoubleBinaryOperator op) {
        return real(left).flatMap(l -> real(right).map(r -> op.applyAsDouble(l, r)));
    }
}
