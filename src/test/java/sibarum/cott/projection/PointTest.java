package sibarum.cott.projection;

import org.junit.jupiter.api.Test;
import sibarum.cott.traction.Pairs;
import sibarum.cott.traction.Proves;
import sibarum.cott.traction.T;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PointTest {

    private static final Point P = Projections.POINT;

    @Test
    @Proves({"T.toC", "T.toC_re", "T.toC_im"})
    void qIsRealAndPIsImaginary() {
        assertEquals("1", P.read(T.ZERO));
        assertEquals("i", P.read(T.OMEGA));
        assertEquals("-1", P.read(T.UNDER_ZERO));
        assertEquals("-i", P.read(T.NEG_OMEGA));
        assertEquals("1 + i", P.read(T.ONE));
        assertEquals("-1 + i", P.read(T.UNDER_ONE));
        assertEquals("3 - 2i", P.read(T.of(-2, 3)));
    }

    @Test
    @Proves("T.toC_eq_zero_iff")
    void thePointIsZeroOnlyAtZeroOmega() {
        for (T x : Pairs.flat(10)) assertEquals(x.equals(T.ZERO_OMEGA), P.read(x).equals("0"), x.toString());
    }
}
