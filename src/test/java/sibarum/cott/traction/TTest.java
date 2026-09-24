package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sibarum.cott.traction.Pairs.big;

class TTest {

    private static final List<T> XS = Pairs.flat(10);

    @Test
    @Proves({"T.zero_times_omega", "T.bottom_eq", "T.omega_times_zero"})
    void zeroTimesOmegaIsZeroOmega() {
        assertEquals(T.ZERO_OMEGA, T.ZERO.times(T.OMEGA));
        assertEquals(T.ZERO_OMEGA, T.ZERO.times(T.ZERO.reciprocal()));
        assertEquals(T.ZERO_OMEGA, T.OMEGA.times(T.ZERO));
    }

    @Test
    @Proves({"T.zeroOmega_plus", "T.zeroOmega_times"})
    void zeroOmegaAbsorbs() {
        for (T x : XS) {
            assertEquals(T.ZERO_OMEGA, T.ZERO_OMEGA.plus(x));
            assertEquals(T.ZERO_OMEGA, T.ZERO_OMEGA.times(x));
        }
    }

    @Test
    @Proves({"T.times_omega", "T.times_zero", "T.zero_times"})
    void timesTheSeeds() {
        for (T x : XS) {
            assertEquals(new T(x.p(), big(0)), x.times(T.OMEGA));
            assertEquals(new T(big(0), x.q()), x.times(T.ZERO));
            assertEquals(T.residue(x.q()), T.ZERO.times(x));
        }
    }

    @Test
    @Proves("T.plus_residue")
    void aResidueScales() {
        for (T y : XS)
            for (long k = -3; k <= 3; k++)
                assertEquals(y.scale(big(k)), y.plus(T.residue(big(k))));
    }

    @Test
    @Proves("T.distrib_residue")
    void distributionIsOffByOneResidue() {
        List<T> xs = XS.subList(0, 30);
        for (T x : xs)
            for (T y : xs)
                for (T z : xs)
                    assertEquals(x.plus(y).times(z).plus(T.residue(z.q())), x.times(z).plus(y.times(z)));
    }

    @Test
    @Proves({"T.plus_neg_residue", "T.times_reciprocal_residue"})
    void eachOperationAgainstItsInverse() {
        for (T x : XS) {
            assertEquals(T.ZERO.plus(T.residue(x.q().pow(2))), x.plus(x.neg()));
            assertEquals(T.ONE.plus(T.residue(x.p().multiply(x.q()))), x.times(x.reciprocal()));
        }
    }

    @Test
    @Proves({"T.power_add", "T.power_mul", "T.times_power"})
    void powers() {
        for (T x : XS)
            for (int m = 0; m <= 3; m++)
                for (int n = 0; n <= 3; n++) {
                    assertEquals(x.power(m).times(x.power(n)), x.power(m + n));
                    assertEquals(x.power(m * n), x.power(m).power(n));
                }
        for (T x : XS)
            for (T y : XS)
                assertEquals(x.power(3).times(y.power(3)), x.times(y).power(3));
    }
}
