package sibarum.cott;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.base.rule.Rule;
import sibarum.cott.engine.derivation.Derivation;
import sibarum.cott.engine.derivation.Deriver;
import sibarum.cott.engine.derivation.Step;
import sibarum.cott.engine.traction.rule.TractionRules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The answer comes with its proof, and the proof is of what actually happened.
 *
 * <p>{@link #aDerivationEndsWhereSimplifyDoes} used to be the load-bearing test here, pinning two
 * implementations together on whatever inputs somebody had thought to write down. It is now true by
 * construction — {@code simplify()} is the derivation with the reasons dropped — and it is kept as a
 * tripwire rather than as a proof: it fails the moment anyone gives a node its own {@code simplify()} again,
 * which is how a second evaluator would come back.
 */
class DerivationTest {

    private static Derivation of(String entry) {
        return Cott.derive(entry);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1+1", "2-1", "1÷2", "5÷10", "2.5", "0w", "1-1", "x-x", "2·3", "1÷2w", "0^(1+w)", "sin(0)",
            "cos(π÷3)", "sin(2)", "x+3x", "2^3", "0*0", "0^2*0^3", "0^5÷0^2", "0^2-0^3", "w+w", "(-1)*(-1)",
            "x^2", "2^-1", "0^w", "sin(x)+1",
    })
    void aDerivationEndsWhereSimplifyDoes(String entry) {
        IExpr parsed = Parser.parse(Notation.normalize(entry));
        assertEquals(Cott.reduce(parsed), of(entry).to(), entry);
    }

    /** And the engine's own derivation agrees with its own simplify, without the syntax layer involved. */
    @ParameterizedTest
    @ValueSource(strings = {"0*0", "0^2*0^3", "1-1", "w+w", "2^3", "0*w", "0^2÷0^2"})
    void theEngineDerivationEndsWhereItsSimplifyDoes(String entry) {
        IExpr parsed = Parser.parse(Notation.normalize(entry));
        assertEquals(parsed.simplify(), Deriver.derive(parsed).to(), entry);
    }

    /** The chain is a chain: each step starts where the last one ended. */
    @Test
    void theStepsJoinUp() {
        Derivation d = of("0^2*0^3");
        assertFalse(d.steps().isEmpty());
        IExpr at = d.from();
        for (Step step : d.steps()) {
            assertEquals(at, step.before());
            at = step.after();
        }
        assertEquals(d.to(), at);
    }

    @Test
    void anAnswerFromThePrimitivesAloneSaysSo() {
        Derivation d = of("0*0");
        assertEquals("0^2", Render.show(d.to()));
        assertTrue(d.isProven());
        assertTrue(d.assumptions().isEmpty());
        // E1 fires, and then the exponent sum it built is reduced -- two steps, both visible, which is the
        // point of the rules no longer doing their own arithmetic behind the derivation's back.
        assertTrue(d.steps().stream().anyMatch(s -> s.rule().equals(TractionRules.PRODUCT)));
        assertEquals(Deriver.COORDINATES, d.steps().getLast().rule());
    }

    /**
     * An approximation is not a proof, and the status column is what says so. This is the question the whole
     * apparatus exists to answer per-answer rather than per-document.
     */
    @Test
    void anApproximationIsDeclared() {
        Derivation d = of("cos(0)");
        assertEquals("1", Render.show(d.to()));
        assertFalse(d.isProven());
        assertEquals(1, d.assumptions().size());
        assertEquals(Rule.Status.APPROXIMATE, d.assumptions().iterator().next().status());
    }

    /**
     * A term the theory has not settled takes no steps, and the record says nothing happened.
     *
     * <p>This has been rewritten twice. It was 0w, which is 1 now; then 1+0, which is 1 as well, since 0 is
     * invariant under addition. What stands is 1+w -- w is invariant under neither operation, so the sum has
     * no single value and is the canonical form a + 0^b at a = 1, b = -1.
     */
    @Test
    void aStandingTermHasAnEmptyDerivation() {
        Derivation d = of("1+w");
        assertTrue(d.stands());
        assertEquals(d.from(), d.to());
        assertEquals("1+ω", Render.show(d.to()));
    }

    /**
     * The coordinate layer is in the trace. Every bug found in this engine so far has been coordinate
     * arithmetic rather than a traction rule, and a derivation that recorded only the interesting-looking
     * layer would have missed all of them.
     */
    @Test
    void theCoordinatesReportThemselves() {
        Derivation d = of("w+w");
        assertEquals("2ω", Render.show(d.to()));
        // Two steps: distributivity puts the real parts together, and then the coordinates add them. The rule
        // builds 1+1 rather than 2, so the arithmetic is a step of its own and visible as one.
        assertEquals(2, d.steps().size());
        assertSame(TractionRules.LIKE_TERMS, d.steps().getFirst().rule());
        assertSame(Deriver.COORDINATES, d.steps().getLast().rule());
    }

    /**
     * The unwired rules carry the status that keeps them unwired, and the wired ones carry theirs.
     *
     * <p>Traction-Theory.md's addition law is Open rather than Maybe: it needs the general involution, it
     * needs a power rule the theory does not have in the ω-direction, and it disagrees with the rest of the
     * theory at four edges. Negation is Proven now, because turning the real coordinate does not need the
     * leap; the leap itself and both unit tables are Chosen, so any answer that folds {@code 0^ω} says so.
     */
    @Test
    void theProvisionalRulesDeclareWhatTheyAre() {
        assertEquals(Rule.Status.OPEN, TractionRules.ADDITION_LAW.status());
        assertEquals(Rule.Status.OPEN, TractionRules.INVOLUTION.status());
        assertEquals(Rule.Status.OPEN, TractionRules.MINUS_ZERO.status());

        assertEquals(Rule.Status.CHOSEN, TractionRules.LEAP.status());
        assertEquals(Rule.Status.CHOSEN, TractionRules.UNIT_POWER.status());
        assertEquals(Rule.Status.CHOSEN, TractionRules.UNIT_LOG.status());

        assertTrue(TractionRules.PRODUCT.isProven());
        assertTrue(TractionRules.NEGATION.isProven());
        assertTrue(TractionRules.OMEGA_DEF.isProven());
    }
}
