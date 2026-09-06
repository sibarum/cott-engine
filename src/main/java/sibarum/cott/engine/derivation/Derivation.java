package sibarum.cott.engine.derivation;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.rule.Rule;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An answer with its proof: what was asked, what came back, and every rewrite in between.
 * <p>
 * Information conservation is the theory's founding commitment, and an evaluator that returns only an answer
 * discards the derivation that produced it. This is the engine's output made as conservative as its algebra
 * claims to be — the answer is recoverable from the steps, and so is the reason for it.
 *
 * @param from  the expression as it arrived
 * @param to    the expression it settled on
 * @param steps every rewrite, in order
 */
public record Derivation(IExpr from, IExpr to, List<Step> steps) {

    public Derivation {
        steps = List.copyOf(steps);
    }

    /** Nothing fired: the term stood as it was written. */
    public boolean stands() {
        return steps.isEmpty();
    }

    /**
     * Every rule used here that the theory has not proven, in the order it was first used.
     * <p>
     * This is the question the status column exists to answer. An answer resting on the Chosen leap or on the
     * Maybe addition law is not wrong, but it is not the same kind of answer as one resting on E1, and the
     * difference should be visible per answer rather than reconstructed from the docs afterwards.
     */
    public Set<Rule> assumptions() {
        Set<Rule> out = new LinkedHashSet<>();
        for (Step step : steps) {
            if (!step.rule().isProven()) {
                out.add(step.rule());
            }
        }
        return out;
    }

    /** Whether every rewrite here follows from the primitives alone. */
    public boolean isProven() {
        return assumptions().isEmpty();
    }

    /** The derivation as a chain of equalities, one rewrite to a line, each with what licensed it. */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(from.toString());
        for (Step step : steps) {
            out.append("\n  = ").append(step.after()).append("        ").append(step.rule());
        }
        return out.toString();
    }
}
