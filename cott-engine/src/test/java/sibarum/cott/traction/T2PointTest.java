package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class T2PointTest {

    private static final List<T> FLAT = Pairs.flat(10);
    private static final List<T2> XS = Pairs.nested();

    @Test
    @Proves({"T2.ofPoint_injective", "T2.ofPoint_oplus", "T2.ofPoint_otimes"})
    void theFlatPointsSitInsideExactly() {
        for (T x : FLAT)
            for (T y : FLAT) {
                if (!x.equals(y)) assertNotEquals(T2.ofPoint(x), T2.ofPoint(y));
                assertEquals(T2.ofPoint(x.oplus(y)), T2.ofPoint(x).oplus(T2.ofPoint(y)));
                assertEquals(T2.ofPoint(x.otimes(y)), T2.ofPoint(x).otimes(T2.ofPoint(y)));
            }
    }

    @Test
    @Proves("T2.pointInv_ofPoint")
    void aGaussianIntegerGainsItsInverse() {
        for (T z : FLAT) {
            BigInteger n = z.p().pow(2).add(z.q().pow(2));
            assertEquals(new T2(new T(z.p().negate(), n), new T(z.q(), n)), T2.ofPoint(z).pointInv());
        }
    }

    @Test
    @Proves({"T2.pointInv_eq", "T2.otimes_pointInv", "T2.lenSq"})
    void theInverseReturnsToOneUpToOneInteger() {
        for (T2 x : XS) {
            BigInteger a = x.p().p(), b = x.p().q(), c = x.q().p(), d = x.q().q();
            BigInteger lenSq = a.pow(2).multiply(d.pow(2)).add(b.pow(2).multiply(c.pow(2)));
            BigInteger b2d2 = b.pow(2).multiply(d.pow(2));
            assertEquals(new T2(new T(a.negate().multiply(b2d2), b.multiply(lenSq)), new T(c.multiply(b2d2), d.multiply(lenSq))),
                    x.pointInv(), x.toString());
            BigInteger k = b.multiply(d).multiply(lenSq).pow(2);
            assertEquals(new T2(new T(BigInteger.ZERO, k), new T(k, k)), x.otimes(x.pointInv()), x.toString());
        }
    }

    @Test
    @Proves({"T2.ofRatio_injective", "T2.ofRatio_plus"})
    void aRatioIsARealPoint() {
        for (T x : FLAT)
            for (T y : FLAT) {
                if (!x.equals(y)) assertNotEquals(T2.ofRatio(x), T2.ofRatio(y));
                assertEquals(T2.ofRatio(x.plus(y)), T2.ofRatio(x).oplus(T2.ofRatio(y)));
            }
    }

    @Test
    @Proves({"T2.pointInv_ofRatio_zero", "T2.pointInv_ofRatio_omega"})
    void onTheAxesTheTwoInversesSplit() {
        assertEquals(T.OMEGA, T.ZERO.reciprocal());
        assertEquals(new T2(T.ZERO_OMEGA, T.ZERO_OMEGA), T2.ofRatio(T.ZERO).pointInv());
        assertEquals(T.ZERO, T.OMEGA.reciprocal());
        assertEquals(new T2(T.ZERO, T.ZERO_OMEGA), T2.ofRatio(T.OMEGA).pointInv());
    }

    @Test
    @Proves("T2.pointInv_ofRatio")
    void theInverseOfARealIsTheReciprocalScaledOverAResidue() {
        for (T x : FLAT)
            assertEquals(new T2(new T(BigInteger.ZERO, x.p().pow(2)), x.reciprocal().scale(x.p().multiply(x.q()))),
                    T2.ofRatio(x).pointInv());
    }
}
