package sibarum.cott.projection;

import org.junit.jupiter.api.Test;
import sibarum.cott.traction.Pairs;
import sibarum.cott.traction.Proves;
import sibarum.cott.traction.Quotient;
import sibarum.cott.traction.T;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AngleTest {

    private static final Angle A = Projections.ANGLE;
    private static final double EPS = 1e-12;
    private static final List<T> SMALL = Pairs.flat(0);

    @Test
    @Proves({"T.theta_zero", "T.theta_omega", "T.theta_underZero", "T.theta_negOmega", "T.theta_one",
            "T.theta_negOne", "T.theta_underOne", "T.theta_negUnderOne"})
    void theTable() {
        assertEquals(0, A.apply(T.ZERO), EPS);
        assertEquals(Math.PI / 2, A.apply(T.OMEGA), EPS);
        assertEquals(Math.PI, A.apply(T.UNDER_ZERO), EPS);
        assertEquals(-Math.PI / 2, A.apply(T.NEG_OMEGA), EPS);
        assertEquals(Math.PI / 4, A.apply(T.ONE), EPS);
        assertEquals(-Math.PI / 4, A.apply(T.NEG_ONE), EPS);
        assertEquals(3 * Math.PI / 4, A.apply(T.UNDER_ONE), EPS);
        assertEquals(-3 * Math.PI / 4, A.apply(T.NEG_UNDER_ONE), EPS);
        assertEquals("180°", A.read(T.UNDER_ZERO));
        assertEquals("90°", A.read(T.OMEGA));
    }

    @Test
    @Proves("T.tan_theta")
    void tanThetaIsTheRatio() {
        for (T x : SMALL)
            if (x.q().signum() != 0)
                assertEquals(x.p().doubleValue() / x.q().doubleValue(), Math.tan(A.apply(x)), EPS, x.toString());
    }

    @Test
    @Proves("T.theta_scale")
    void aPositiveMultiplierKeepsTheta() {
        BigInteger huge = BigInteger.ONE.shiftLeft(2000);
        for (T x : Pairs.flat(10))
            for (BigInteger k : List.of(BigInteger.TWO, BigInteger.valueOf(7), huge))
                assertEquals(A.apply(x), A.apply(x.scale(k)), EPS, k.bitLength() + "-bit multiple of " + x);
    }

    @Test
    @Proves("T.theta_eq_theta_iff_sameRay")
    void theAngleIsTheRay() {
        for (T x : SMALL)
            for (T y : SMALL)
                if (!x.equals(T.ZERO_OMEGA) && !y.equals(T.ZERO_OMEGA))
                    assertEquals(Quotient.RAY.equivalent(x, y), Math.abs(A.apply(x) - A.apply(y)) < EPS, x + ", " + y);
    }
}
