package sibarum.cott.projection;

import org.junit.jupiter.api.Test;
import sibarum.cott.traction.Pairs;
import sibarum.cott.traction.Proves;
import sibarum.cott.traction.T;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RotationTest {

    private static final Rotation R = Projections.ROTATION;

    @Test
    @Proves({"T.rot", "T.rotCos_eq", "T.rotSin_eq", "T.rot_zero"})
    void theNamedValuesTurnByTwiceTheirAngle() {
        assertEquals("1", R.read(T.ZERO));
        assertEquals("i", R.read(T.ONE));
        assertEquals("-1", R.read(T.OMEGA));
        assertEquals("-i", R.read(T.UNDER_ONE));
        assertEquals("1", R.read(T.UNDER_ZERO));
        assertEquals("i", R.read(T.NEG_UNDER_ONE));
        assertEquals("-1", R.read(T.NEG_OMEGA));
        assertEquals("-i", R.read(T.NEG_ONE));
        assertEquals("none", R.read(T.ZERO_OMEGA));
        assertEquals("3/5 + (4/5)·i", R.read(T.of(1, 2)));
        assertEquals("-3/5 - (4/5)·i", R.read(T.of(-2, 1)));
    }

    @Test
    @Proves({"T.rot_eq_rot_iff", "T.rot_scale", "T.rot_oplusInverse"})
    void onOneLineTheReadingIsTheSame() {
        for (T x : Pairs.flat(10)) {
            if (x.equals(T.ZERO_OMEGA)) {
                assertEquals(Optional.empty(), R.apply(x));
                continue;
            }
            assertEquals(R.apply(x), R.apply(x.oplusInverse()));
            assertEquals(R.apply(x), R.apply(x.scale(java.math.BigInteger.valueOf(-3))));
        }
    }
}
