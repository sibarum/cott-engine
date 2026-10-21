package sibarum.cott.traction;

import org.junit.jupiter.api.Test;
import sibarum.cott.projection.Rational;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.traction.Pairs.big;

class SpinTest {

    private static final List<T> XS = Pairs.flat(10);

    /** The Lean's ratio: {@code p/q} in ℚ, where dividing by zero gives {@code 0}. */
    static Rational q(T x) {
        return x.q().signum() == 0 ? Rational.ZERO : new Rational(x.p(), x.q());
    }

    static Rational cos(T x) {
        return q(Spin.rotCos(x));
    }

    static Rational sin(T x) {
        return q(Spin.rotSin(x));
    }

    @Test
    @Proves({"T.rotCos_eq", "T.rotSin_eq", "T.doubleAngle_eq"})
    void theEntriesAreRatiosOfIntegers() {
        assertEquals(T.of(3, 5), Spin.rotCos(T.of(1, 2)));
        assertEquals(T.of(4, 5), Spin.rotSin(T.of(1, 2)));
        for (T x : XS) {
            BigInteger n = x.p().pow(2).add(x.q().pow(2));
            assertEquals(new T(x.q().pow(2).subtract(x.p().pow(2)), n), Spin.rotCos(x));
            assertEquals(new T(big(2).multiply(x.p()).multiply(x.q()), n), Spin.rotSin(x));
        }
    }

    @Test
    @Proves({"T.rot", "T.rotCos", "T.rotSin"})
    void oneIsAQuarterTurnAndOmegaAHalfTurn() {
        assertEquals(Rational.ZERO, cos(T.ONE));
        assertEquals(Rational.of(1, 1), sin(T.ONE));
        assertEquals(Rational.of(-1, 1), cos(T.OMEGA));
        assertEquals(Rational.ZERO, sin(T.OMEGA));
    }

    @Test
    @Proves({"T.rotCos_sq_add_rotSin_sq", "T.rot_mem_specialOrthogonalGroup", "T.norm_ne_zero_of_ne"})
    void offZeroOmegaItIsExactlyOnTheCircle() {
        for (T x : XS) {
            if (x.equals(T.ZERO_OMEGA)) continue;
            Rational c = cos(x), s = sin(x);
            assertEquals(Rational.of(1, 1), c.times(c).plus(s.times(s)), x.toString());
        }
    }

    @Test
    @Proves({"T.doubleAngle_norm", "T.norm_otimes"})
    void theSquareHasNormNSquared() {
        for (T x : XS) {
            assertEquals(x.norm().pow(2), x.doubleAngle().norm());
            for (T y : XS.subList(0, 30)) assertEquals(x.norm().multiply(y.norm()), x.otimes(y).norm());
        }
    }

    @Test
    @Proves({"T.rot_otimes", "T.rotCos_otimes", "T.rotSin_otimes"})
    void rotationsComposeAsPairs() {
        for (T x : XS)
            for (T y : XS) {
                assertEquals(cos(x).times(cos(y)).plus(sin(x).times(sin(y)).neg()), cos(x.otimes(y)));
                assertEquals(sin(x).times(cos(y)).plus(cos(x).times(sin(y))), sin(x.otimes(y)));
            }
    }

    @Test
    @Proves("T.rot_zero")
    void zeroIsTheIdentity() {
        assertEquals(Rational.of(1, 1), cos(T.ZERO));
        assertEquals(Rational.ZERO, sin(T.ZERO));
    }

    @Test
    @Proves("T.rot_eq_rot_iff")
    void twoPairsShareARotationExactlyOnOneLine() {
        for (T x : XS)
            for (T y : XS) {
                if (x.equals(T.ZERO_OMEGA) || y.equals(T.ZERO_OMEGA)) continue;
                boolean same = cos(x).equals(cos(y)) && sin(x).equals(sin(y));
                assertEquals(x.det(y).signum() == 0, same, x + " " + y);
            }
    }

    @Test
    @Proves({"T.rot_oplusInverse", "T.rot_scale"})
    void theDoubleCover() {
        for (T x : XS) {
            assertEquals(cos(x), cos(x.oplusInverse()));
            assertEquals(sin(x), sin(x.oplusInverse()));
            for (long k = -3; k <= 3; k++) {
                if (k == 0) continue;
                assertEquals(cos(x), cos(x.scale(big(k))));
                assertEquals(sin(x), sin(x.scale(big(k))));
            }
        }
    }

    @Test
    @Proves("T.rot_eq_one_iff")
    void theIdentityComesFromTheRealAxis() {
        for (T x : XS) {
            if (x.equals(T.ZERO_OMEGA)) continue;
            boolean one = cos(x).equals(Rational.of(1, 1)) && sin(x).equals(Rational.ZERO);
            assertEquals(x.p().signum() == 0, one, x.toString());
        }
    }

    @Test
    @Proves({"T.exists_rot", "T.rot_surjective"})
    void everyRationalRotationIsSomePairs() {
        List<Rational[]> circle = List.of(
                new Rational[]{Rational.of(3, 5), Rational.of(4, 5)},
                new Rational[]{Rational.of(-7, 25), Rational.of(24, 25)},
                new Rational[]{Rational.of(5, 13), Rational.of(-12, 13)},
                new Rational[]{Rational.of(-8, 17), Rational.of(-15, 17)},
                new Rational[]{Rational.of(1, 1), Rational.ZERO},
                new Rational[]{Rational.of(-1, 1), Rational.ZERO},
                new Rational[]{Rational.ZERO, Rational.of(1, 1)},
                new Rational[]{Rational.ZERO, Rational.of(-1, 1)});
        for (Rational[] cs : circle) {
            Rational c = cs[0], s = cs[1];
            T x;
            if (c.equals(Rational.of(-1, 1))) {
                x = T.OMEGA;
            } else {
                // T(s, 1 + c), cleared of denominators.
                Rational u = c.plus(Rational.of(1, 1));
                x = new T(s.num().multiply(u.den()), u.num().multiply(s.den()));
            }
            assertTrue(!x.equals(T.ZERO_OMEGA));
            assertEquals(c, cos(x));
            assertEquals(s, sin(x));
        }
    }

    @Test
    @Proves({"T.rotCos_eq_cos", "T.rotSin_eq_sin"})
    void itIsTheRotationByTwiceTheAngle() {
        for (T x : Pairs.flat(0)) {
            if (x.equals(T.ZERO_OMEGA)) continue;
            double theta = Math.atan2(x.p().doubleValue(), x.q().doubleValue());
            assertEquals(Math.cos(2 * theta), cos(x).num().doubleValue() / cos(x).den().doubleValue(), 1e-12);
            assertEquals(Math.sin(2 * theta), sin(x).num().doubleValue() / sin(x).den().doubleValue(), 1e-12);
        }
    }
}
