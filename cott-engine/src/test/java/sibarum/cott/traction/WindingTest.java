package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.traction.Pairs.big;

class WindingTest {

    /** The pairs the comparison is about: the upper half-plane and the positive real ray. */
    private static final List<T> UPPER = Pairs.flat(10).stream()
            .filter(x -> x.p().signum() > 0 || (x.p().signum() == 0 && x.q().signum() >= 0))
            .toList();

    private static double turn(T x) {
        return Math.atan2(x.p().doubleValue(), x.q().doubleValue()) / (2 * Math.PI);
    }

    @Test
    @Proves({"T.winding_eq_floor", "T.turn_mem", "T.pow_p_neg_iff"})
    void theWindingCountIsTheNumberOfWholeTurns() {
        for (T x : UPPER)
            for (long b = 0; b <= 40; b++) {
                double u = b * turn(x);
                if (Math.abs(u - Math.rint(u)) < 1e-9) continue; // too close to call in a double
                assertEquals((long) Math.floor(u), Winding.winding(x, b), x + " " + b);
            }
    }

    @Test
    @Proves({"T.turn_zero", "T.turn_one", "T.turn_omega"})
    void theNamedTurnsWindExactly() {
        for (long b = 0; b <= 40; b++) {
            assertEquals(0, Winding.winding(T.ZERO, b));
            assertEquals(b / 8, Winding.winding(T.ONE, b));
            assertEquals(b / 4, Winding.winding(T.OMEGA, b));
        }
    }

    @Test
    @Proves({"T.powWind_eq", "T.powWindAux_eq", "T.winding_double", "T.otimesPowNat_add"})
    void squaringFindsThePowerAndTheCountTogether() {
        for (T x : UPPER)
            for (long b = 0; b <= 40; b++)
                assertEquals(new Winding.Walk(x.otimesPowNat((int) b), Winding.winding(x, b)), Winding.powWind(x, b));
    }

    @Test
    @Proves("T.turnLt_iff")
    void theComparisonIsDecidedByIntegers() {
        assertTrue(Winding.turnLt(T.of(1, 2), big(1), 12));
        assertFalse(Winding.turnLt(T.of(1, 2), big(1), 14));
        assertEquals(1, Winding.winding(T.of(1, 2), 16));
        for (T x : UPPER)
            for (long b = 1; b <= 24; b++)
                for (long a = -2; a <= b; a++) {
                    double d = turn(x) - (double) a / b;
                    if (Math.abs(d) < 1e-9) continue;
                    assertEquals(d < 0, Winding.turnLt(x, big(a), b), x + " " + a + "/" + b);
                }
        // The equality case falls out with no special handling: 1's turn is not under 1/8.
        assertFalse(Winding.turnLt(T.ONE, big(1), 8));
        assertFalse(Winding.turnLt(T.OMEGA, big(1), 4));
        assertTrue(Winding.turnLt(T.ONE, big(2), 15));
    }
}
