package sibarum.cott.algebra;

import org.junit.jupiter.api.Test;
import sibarum.cott.notation.Parser;
import sibarum.cott.traction.Proves;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/** {@code Z(p, q) = p·0^q}, its logarithm {@code E(p, q) = q + log₀ p}, {@code B(p, q) = log_p q}, and the residual. */
class ZeroScaleTest {

    private static String in(Cancellation cancellation, String line) {
        return Evaluator.evaluate(new Parser().expression(line), NumberType.INTEGER, SizeLimit.MEDIUM,
                Form.SUM_OF_PRODUCTS, cancellation).text();
    }

    private static String residual(String line) {
        return in(Cancellation.RESIDUAL, line);
    }

    private static String absorb(String line) {
        return in(Cancellation.ABSORB, line);
    }

    // ---- Z ----

    @Test
    @Proves({"T.ZeroScale.Z.mul_def", "T.ZeroScale.Z.zero_mul_omega", "T.ZeroScale.Z.zero_pow", "T.ZeroScale.Z.omega_pow"})
    void zMultipliesCoefficientsAndAddsOrders() {
        assertEquals("Z(6, 3)", residual("Z(3, 2)·Z(2, 1)"));
        assertEquals("Z(1, 0)", residual("Z(1, 1)·Z(1, -1)"), "0·ω = 1");
        assertEquals("Z(1, 3)", residual("Z(1, 1)^3"));
        assertEquals("Z(1, -2)", residual("Z(1, -1)^2"));
    }

    @Test
    @Proves({"T.ZeroScale.Residual.R.inv", "T.ZeroScale.Residual.R.value_mul"})
    void zDividesCoefficientsAndSubtractsOrders() {
        assertEquals("Z(Q(6, 2), 2)", residual("Z(6, 3) / Z(2, 1)"));
        assertEquals("Z(Q(1, 1), -1)", residual("1 / Z(1, 1)"), "1/0 is ω, one order down");
    }

    @Test
    @Proves({"T.ZeroScale.Z.ofInt", "T.ZeroScale.toLaurent_add_same"})
    void aNumberIsOfOrderZeroAndSumsKeepTheirOrder() {
        assertEquals("Z(3, 1)", residual("3·Z(1, 1)"));
        assertEquals("Z(7, 1)", residual("Z(3, 1) + Z(4, 1)"));
        assertEquals("Z(5, 0)", residual("Z(3, 0) + 2"));
        assertEquals("S(Z(3, 1), Z(4, 2))", residual("Z(3, 1) + Z(4, 2)"), "two orders are a new element");
        assertEquals("S(Z(1, 1), 2)", residual("Z(1, 1) + 2"));
    }

    @Test
    @Proves({"T.ZeroScale.Z.negZero_eq", "T.ZeroScale.Z.negZero_ne_zero"})
    void minusZeroIsNotZero() {
        assertEquals("Z(-1, 1)", residual("-Z(1, 1)"));
        assertNotEquals(residual("Z(1, 1)"), residual("-Z(1, 1)"));
    }

    // ---- the residual ----

    @Test
    @Proves({"T.ZeroScale.Residual.R.add_neg_self", "T.ZeroScale.Residual.R.cancel_eq", "T.ZeroScale.Residual.R.add_one_neg"})
    void anExactCancellationLeavesZeroTimesWhatCancelled() {
        assertEquals("Z(3, 2)", residual("Z(3, 1) - Z(3, 1)"));
        assertEquals("Z(1, 1)", residual("Z(1, 0) - Z(1, 0)"), "1 − 1 is the 0 with an inverse");
        assertEquals("Z(3, 1)", residual("Z(3, 0) + Z(-3, 0)"), "the left one is kept");
        assertEquals("0", residual("1 - 1"), "only Z's + leaves a residual");
    }

    @Test
    @Proves({"T.ZeroScale.Residual.R.value_cancel_congr", "T.ZeroScale.Residual.R.value_cancel_either"})
    void theResidualKeepsWhatCancelledAsWritten() {
        assertEquals("Z(Q(6, 2), 1)", residual("Z(6/2, 0) - Z(6/2, 0)"));
        assertEquals("Z(Q(6, 2), 1)", residual("Z(6/2, 0) - Z(3, 0)"));
        assertEquals("Z(3, 1)", residual("Z(3, 0) - Z(6/2, 0)"));
    }

    @Test
    @Proves({"T.ZeroScale.Residual.R.cancel_div_cancel", "T.ZeroScale.Residual.R.cancel_div_self"})
    void zeroOverZeroIsTheRatioOfWhatCancelled() {
        assertEquals("Z(Q(3, 2), 0)", residual("(Z(3, 0) - Z(3, 0)) / (Z(2, 0) - Z(2, 0))"));
        assertEquals("Z(Q(3, 3), 1)", residual("(Z(3, 0) - Z(3, 0)) / Z(3, 0)"));
    }

    @Test
    @Proves({"T.ZeroScale.Residual.R.add_add_neg", "T.ZeroScale.Residual.R.add_neg_self"})
    void plusCountsExactCancellations() {
        assertEquals("Z(1, 0)", residual("(Z(1, 0) + Z(1, 0)) - Z(1, 0)"));
        assertEquals("S(Z(1, 0), Z(1, 1))", residual("Z(1, 0) + (Z(1, 0) - Z(1, 0))"));
    }

    @Test
    @Proves({"T.ZeroScale.toLaurent_add_same", "T.ZeroScale.Z.coeff_zero_ne"})
    void absorbingLetsTheCoefficientReachZero() {
        assertEquals("Z(0, 1)", absorb("Z(3, 1) - Z(3, 1)"));
        assertEquals("Z(Q(0, 4), 0)", absorb("Z(6/2, 0) - Z(6/2, 0)"));
        assertEquals("Z(0, 3)", absorb("(Z(3, 1) - Z(3, 1))·Z(5, 2)"), "0·b = 0 drops b");
        assertEquals("Z(Q(15, 1), 3)", residual("Z(3, 1)·Z(5, 2) / Z(1, 0)"));
    }

    // ---- E ----

    @Test
    @Proves({"T.ZeroScale.E_add", "T.ZeroScale.Z.log_mul", "T.ZeroScale.E_add_C"})
    void eAddsAsZMultiplies() {
        assertEquals("E(6, 3)", residual("E(2, 1) + E(3, 2)"));
        assertEquals("E(2, 6)", residual("E(2, 1) + 5"));
        assertEquals("E(Q(2, 2), 0)", residual("E(2, 1) - E(2, 1)"));
    }

    @Test
    @Proves("T.ZeroScale.E_add_zero")
    void againstACoefficientZeroTheSumKeepsTheOther() {
        assertEquals("E(5, 4)", residual("E(0, 1) + E(5, 2)"));
        assertEquals("E(5, 4)", residual("E(5, 2) + E(0, 1)"));
    }

    @Test
    @Proves("T.ZeroScale.E_nsmul")
    void aWholeMultipleIsZsPower() {
        assertEquals("E(8, 3)", residual("3·E(2, 1)"));
        assertEquals("E(8, 3)", residual("E(2, 1)·3"));
        assertEquals("E(Q(1, 8), -3)", residual("(-3)·E(2, 1)"));
        assertEquals("P(E(2, 1), E(3, 1))", residual("E(2, 1)·E(3, 1)"), "no product multiplies logarithms");
    }

    // ---- B ----

    @Test
    @Proves({"T.ZeroScale.E_eq_B_mul", "T.ZeroScale.B_mul_B"})
    void aRatioOfLogarithmsAtOrderZeroIsB() {
        assertEquals("B(2, 8)", residual("E(8, 0) / E(2, 0)"));
        assertEquals("Q(E(8, 1), E(2, 0))", residual("E(8, 1) / E(2, 0)"));
        assertEquals("B(2, 64)", residual("B(2, 8)·B(8, 64)"));
        assertEquals("P(B(2, 8), B(4, 64))", residual("B(2, 8)·B(4, 64)"));
        assertEquals("S(B(2, 8), B(2, 4))", residual("B(2, 8) + B(2, 4)"));
    }
}
