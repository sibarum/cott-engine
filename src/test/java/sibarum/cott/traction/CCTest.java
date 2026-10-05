package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CCTest {

    private static final T ZERO = TC.gaussianInt(Pairs.big(0));
    private static final T ONE = TC.gaussianInt(Pairs.big(1));
    /** The Gaussian {@code i}, which is the flat {@code ω}. */
    private static final T I = T.OMEGA;

    private static final List<CC> XS = points();

    private static List<CC> points() {
        List<T> gaussians = new ArrayList<>(T.NINE);
        gaussians.add(T.of(2, 3));
        gaussians.add(T.of(-3, 2));
        List<CC> out = new ArrayList<>();
        for (T a : gaussians)
            for (T b : gaussians) out.add(new CC(a, b));
        return out;
    }

    @Test
    @Proves("CC.ij_sq")
    void ijSquaresToOne() {
        CC ij = new CC(I, ZERO);
        assertEquals(new CC(ZERO, ONE), ij.otimes(ij));
    }

    @Test
    @Proves("CC.zero_divisors")
    void onePlusIjTimesOneMinusIjIsZero() {
        CC plus = new CC(I, ONE), minus = new CC(I.oplusInverse(), ONE);
        assertEquals(new CC(ZERO, ZERO), plus.otimes(minus));
        assertNotEquals(new CC(ZERO, ZERO), plus);
        assertNotEquals(new CC(ZERO, ZERO), minus);
    }

    @Test
    @Proves("CC.otimes_conj")
    void aPointTimesItsConjugateIsItsNorm() {
        for (CC x : XS)
            assertEquals(new CC(ZERO, x.nrm()), x.otimes(new CC(x.p().oplusInverse(), x.q())), x.toString());
    }

    @Test
    @Proves({"CC.nrm_eq_ev_mul", "CC.evPlus_otimes", "CC.evMinus_otimes"})
    void theNormIsTheProductOfTheTwoEvaluations() {
        for (CC x : XS) {
            assertEquals(x.nrm(), x.evPlus().otimes(x.evMinus()), x.toString());
            for (CC y : XS) {
                assertEquals(x.otimes(y).evPlus(), x.evPlus().otimes(y.evPlus()));
                assertEquals(x.otimes(y).evMinus(), x.evMinus().otimes(y.evMinus()));
            }
        }
    }

    @Test
    @Proves({"CC.ofOuter_otimes", "CC.ofOuter_oplus"})
    void aFlatPointOnTheOuterUnitCarriesBothOperations() {
        List<T> flat = Pairs.flat(5);
        for (T x : flat)
            for (T y : flat) {
                assertEquals(CC.ofOuter(x.otimes(y)), CC.ofOuter(x).otimes(CC.ofOuter(y)));
                assertEquals(CC.ofOuter(x.oplus(y)), CC.ofOuter(x).oplus(CC.ofOuter(y)));
            }
    }
}
