package sibarum.cott.engine.derivation;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.rule.Rule;

/**
 * One line of a derivation: the whole expression before, the whole expression after, and the rule between.
 * <p>
 * Whole terms rather than the sub-term that changed, so the chain reads the way the docs argue — a sequence of
 * equalities, each one justified — instead of as a log of what the evaluator did to itself.
 *
 * @param before the expression as it stood
 * @param after  the expression after this one rewrite
 * @param rule   what licensed it
 */
public record Step(IExpr before, IExpr after, Rule rule) {
}
