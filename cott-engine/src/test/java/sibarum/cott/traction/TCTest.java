package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TCTest {

    /** Ratios of Gaussian integers, each coordinate a flat pair read as {@code q + p·i}. */
    private static final List<TC> XS = ratios();

    private static List<TC> ratios() {
        List<T> gaussians = new ArrayList<>(T.NINE);
        gaussians.add(T.of(2, 3));
        gaussians.add(T.of(-3, 2));
        List<TC> out = new ArrayList<>();
        for (T z : gaussians)
            for (T w : gaussians) out.add(new TC(z, w));
        return out;
    }

    @Test
    @Proves({"TC.times_reciprocal_residue", "TOver.times_reciprocal_self"})
    void theReciprocalLeavesTheProductOfTheCoordinates() {
        for (TC x : XS) {
            T zw = x.p().otimes(x.q());
            assertEquals(new TC(zw, zw), x.times(x.reciprocal()), x.toString());
        }
    }

    @Test
    @Proves("TC.rationalize_infinite")
    void everyPointAtInfinityRationalizesToZeroOmega() {
        T gaussianZero = TC.gaussianInt(Pairs.big(0));
        for (T z : Pairs.flat(10))
            assertEquals(new T2(T.ZERO_OMEGA, T.ZERO_OMEGA), new TC(z, gaussianZero).rationalize(), z.toString());
    }

    @Test
    @Proves({"T.toGaussian_complexInt", "T.toGaussian_zeroOmega"})
    void anIntegerIsTheGaussianIntegerOverOne() {
        assertEquals(T.ZERO_OMEGA, TC.gaussianInt(Pairs.big(0)));
        assertEquals(T.ZERO, TC.gaussianInt(Pairs.big(1)));
        assertEquals(new TC(T.of(0, 3), T.ZERO), TC.of(Pairs.big(3)));
    }
}
