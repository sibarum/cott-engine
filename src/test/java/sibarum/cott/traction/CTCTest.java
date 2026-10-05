package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CTCTest {

    private static final T ZERO = TC.gaussianInt(Pairs.big(0));
    private static final T ONE = TC.gaussianInt(Pairs.big(1));
    /** The Gaussian {@code i}, which is the flat {@code ω}. */
    private static final T I = T.OMEGA;

    private static final List<T> GAUSSIANS = gaussians();

    private static List<T> gaussians() {
        List<T> out = new ArrayList<>(T.NINE);
        out.add(T.of(2, 3));
        out.add(T.of(-3, 2));
        return out;
    }

    private static List<CC> bicomplexIntegers() {
        List<CC> out = new ArrayList<>();
        for (T a : GAUSSIANS)
            for (T b : GAUSSIANS) out.add(new CC(a, b));
        return out;
    }

    /** {@code CTC.ofT2}: each integer of a {@code C(T, T)} point read as a Gaussian integer. */
    private static CTC ofT2(T2 x) {
        return new CTC(new TC(TC.gaussianInt(x.p().p()), TC.gaussianInt(x.p().q())),
                new TC(TC.gaussianInt(x.q().p()), TC.gaussianInt(x.q().q())));
    }

    @Test
    @Proves({"CTC.ofCC_oplus", "CTC.ofCC_otimes"})
    void theBicomplexIntegersSitInsideExactly() {
        for (CC x : bicomplexIntegers())
            for (CC y : bicomplexIntegers()) {
                assertEquals(CTC.ofCC(x.oplus(y)), CTC.ofCC(x).oplus(CTC.ofCC(y)));
                assertEquals(CTC.ofCC(x.otimes(y)), CTC.ofCC(x).otimes(CTC.ofCC(y)));
            }
    }

    @Test
    @Proves({"CTC.inv_ofCC", "CTC.den_ofCC"})
    void aBicomplexIntegerGainsTheInverseItLacked() {
        for (CC z : bicomplexIntegers()) {
            assertEquals(new CTC(new TC(z.p().oplusInverse(), z.nrm()), new TC(z.q(), z.nrm())), CTC.ofCC(z).inv(), z.toString());
            assertEquals(z.nrm(), CTC.ofCC(z).den(), z.toString());
        }
    }

    @Test
    @Proves({"CTC.otimes_inv", "CTC.inv_eq"})
    void theInverseReturnsToOneUpToOneGaussianInteger() {
        for (T u : GAUSSIANS)
            for (T v : GAUSSIANS)
                for (T s : List.of(T.ZERO, T.OMEGA, T.of(2, 3)))
                    for (T t : List.of(T.ZERO, T.of(-3, 2))) {
                        CTC x = new CTC(new TC(u, v), new TC(s, t));
                        T k = v.otimes(t).otimes(x.den());
                        k = k.otimes(k);
                        assertEquals(new CTC(new TC(ZERO, k), new TC(k, k)), x.otimes(x.inv()), x.toString());
                    }
    }

    @Test
    @Proves({"CTC.zeroDivisor_inv", "CTC.otimes_zeroDivisor_inv"})
    void theZeroDivisorsInverseIsInfiniteInBothCoordinates() {
        CTC zd = CTC.ofCC(new CC(I, ONE));
        assertEquals(new CTC(new TC(I.oplusInverse(), ZERO), new TC(ONE, ZERO)), zd.inv());
        assertEquals(new CTC(new TC(ZERO, ZERO), new TC(ZERO, ZERO)), zd.otimes(zd.inv()));
    }

    @Test
    @Proves({"CTC.inv_ofT2", "CTC.ofT2_otimes"})
    void theInverseRestrictsToPointsInverse() {
        for (T2 x : Pairs.nested()) {
            assertEquals(ofT2(x.pointInv()), ofT2(x).inv(), x.toString());
            for (T2 y : Pairs.nested()) assertEquals(ofT2(x.otimes(y)), ofT2(x).otimes(ofT2(y)));
        }
    }

    @Test
    @Proves("CTC.den_eq_zero_iff")
    void theLightLinesAreWhereTheDenominatorVanishes() {
        // B = i·A: the point 1·i + 1·j over ones, i.e. A = 1, B = i.
        CTC light = new CTC(new TC(ONE, ONE), new TC(I, ONE));
        assertEquals(ZERO, light.den());
        CTC off = new CTC(new TC(ONE, ONE), new TC(TC.gaussianInt(BigInteger.TWO), ONE));
        assertEquals(TC.gaussianInt(BigInteger.valueOf(5)), off.den());
    }
}
