package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class T2Test {

    private static final List<T> FLAT = Pairs.flat(10);
    private static final List<T2> XS = Pairs.nested();

    @Test
    @Proves({"T2.of_zero", "T2.of_one", "T2.of_omega"})
    void theNamedValuesAreEmbedded() {
        assertEquals(T2.ZERO, T2.of(T.ZERO));
        assertEquals(T2.ONE, T2.of(T.ONE));
        assertEquals(T2.OMEGA, T2.of(T.OMEGA));
    }

    @Test
    @Proves({"T2.of_injective", "T2.flatten_of"})
    void flattenUndoesTheEmbedding() {
        for (T x : FLAT) {
            assertEquals(x, T2.of(x).flatten());
            for (T y : FLAT) if (!x.equals(y)) assertNotEquals(T2.of(x), T2.of(y));
        }
    }

    @Test
    @Proves({"T2.of_plus", "T2.of_times"})
    void theEmbeddingRespectsPlusAndTimes() {
        for (T x : FLAT)
            for (T y : FLAT) {
                assertEquals(T2.of(x).plus(T2.of(y)), T2.of(x.plus(y)));
                assertEquals(T2.of(x).times(T2.of(y)), T2.of(x.times(y)));
            }
    }

    @Test
    @Proves({"T2.of_neg", "T2.of_reciprocal", "T2.of_power"})
    void theEmbeddingRespectsNegReciprocalAndPower() {
        for (T x : FLAT) {
            assertEquals(T2.of(x).neg(), T2.of(x.neg()));
            assertEquals(T2.of(x).reciprocal(), T2.of(x.reciprocal()));
            for (int n = 0; n <= 4; n++) assertEquals(T2.of(x).power(n), T2.of(x.power(n)));
        }
    }

    @Test
    @Proves({"T2.flatten_times", "T2.flatten_neg", "T2.flatten_reciprocal", "T2.flatten_power"})
    void flattenRespectsAllButPlus() {
        for (T2 x : XS) {
            assertEquals(x.flatten().neg(), x.neg().flatten());
            assertEquals(x.flatten().reciprocal(), x.reciprocal().flatten());
            for (int n = 0; n <= 3; n++) assertEquals(x.flatten().power(n), x.power(n).flatten());
            for (T2 y : XS) assertEquals(x.flatten().times(y.flatten()), x.times(y).flatten());
        }
    }

    @Test
    @Proves("T2.flatten_plus")
    void flattenRespectsPlusUpToOneResidue() {
        for (T2 x : XS)
            for (T2 y : XS)
                assertEquals(x.flatten().plus(y.flatten()).plus(T.residue(x.q().q().multiply(y.q().q()))),
                        x.plus(y).flatten());
    }

    @Test
    @Proves({"T2.power_zero", "T2.power_succ", "T2.power_add"})
    void powerIsRepeatedTimes() {
        for (T2 x : XS) {
            assertEquals(T2.ONE, x.power(0));
            for (int n = 0; n <= 3; n++) {
                assertEquals(x.power(n).times(x), x.power(n + 1));
                for (int m = 0; m <= 3; m++) assertEquals(x.power(m).times(x.power(n)), x.power(m + n));
            }
        }
    }
}
