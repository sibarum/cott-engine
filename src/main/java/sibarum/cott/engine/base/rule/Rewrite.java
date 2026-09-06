package sibarum.cott.engine.base.rule;

import sibarum.cott.engine.base.expr.IExpr;

/**
 * One rewrite: what it produced, and what allowed it.
 * <p>
 * Rules return this rather than a bare expression so that there is one code path and not two. A tracing
 * evaluator that runs different code from the real one can disagree with it, and then the trace is worse than
 * no trace at all; here the fast path simply drops the justification it was handed.
 *
 * @param result the expression after the rewrite
 * @param rule   what licensed it
 */
public record Rewrite(IExpr result, Rule rule) {
}
