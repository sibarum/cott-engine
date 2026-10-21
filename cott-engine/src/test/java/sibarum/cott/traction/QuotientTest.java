package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.traction.Pairs.big;

class QuotientTest {

    private static final List<T> XS = Pairs.flat(10);

    /** Multipliers in each quotient's set {@code S}. */
    private static List<BigInteger> multipliers(Quotient q) {
        return switch (q) {
            case NONE -> List.of(big(1));
            case RAY -> List.of(big(1), big(2), big(3), big(7));
            case RATIO -> List.of(big(1), big(-1), big(2), big(-3), big(7));
        };
    }

    @Test
    @Proves("T.rel_bot_iff")
    void noneIdentifiesNothing() {
        for (T x : XS)
            for (T y : XS) assertEquals(x.equals(y), Quotient.NONE.equivalent(x, y));
    }

    @Test
    @Proves("T.ratioRel_iff")
    void ratioIsSameRatioWithZeroOmegaApart() {
        for (T x : XS)
            for (T y : XS) {
                boolean sameRatio = x.p().multiply(y.q()).equals(y.p().multiply(x.q()));
                boolean apart = x.equals(T.ZERO_OMEGA) == y.equals(T.ZERO_OMEGA);
                assertEquals(apart && sameRatio, Quotient.RATIO.equivalent(x, y), x + " ~ " + y);
            }
    }

    @Test
    @Proves("T.Rel")
    void aMultiplierFromSIdentifies() {
        for (Quotient q : Quotient.values())
            for (T x : XS)
                for (BigInteger s : multipliers(q))
                    for (BigInteger t : multipliers(q))
                        assertTrue(q.equivalent(x.scale(s), x.scale(t)), q + ": " + s + "·" + x + " ~ " + t + "·" + x);
    }

    @Test
    @Proves("T.Rel")
    void theRayKeepsSign() {
        for (T x : XS)
            if (!x.equals(T.ZERO_OMEGA)) assertFalse(Quotient.RAY.equivalent(x, x.scale(big(-1))), x.toString());
    }

    @Test
    @Proves("T.Rel")
    void aRepresentativeIsInItsClass() {
        for (Quotient q : Quotient.values())
            for (T x : XS) {
                T r = q.representative(x);
                BigInteger g = x.p().gcd(x.q());
                if (q == Quotient.NONE || g.signum() == 0) { assertEquals(x, r); continue; }
                // x = k·r for k = ±g, and that k must be in S
                BigInteger k = r.scale(g).equals(x) ? g : g.negate();
                assertEquals(x, r.scale(k), q + ": " + x + " from " + r);
                if (q == Quotient.RAY) assertTrue(k.signum() > 0, "the ray divides by a positive multiplier");
            }
    }

    @Test
    @Proves({"T.rel_plus_left", "T.rel_times_left", "T.rel_reciprocal", "T.rel_neg"})
    void theOperationsRespectEveryQuotient() {
        List<T> xs = XS.subList(0, 40);
        for (Quotient q : Quotient.values())
            for (T x : xs)
                for (BigInteger s : multipliers(q)) {
                    T x2 = x.scale(s);
                    assertTrue(q.equivalent(x.reciprocal(), x2.reciprocal()));
                    assertTrue(q.equivalent(x.neg(), x2.neg()));
                    for (T y : xs) {
                        assertTrue(q.equivalent(x.plus(y), x2.plus(y)));
                        assertTrue(q.equivalent(x.times(y), x2.times(y)));
                    }
                }
    }
}
