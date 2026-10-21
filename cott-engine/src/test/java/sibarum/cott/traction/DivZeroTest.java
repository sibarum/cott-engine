package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DivZeroTest {

    private static final List<T> XS = Pairs.flat(10);

    @Test
    @Proves({"DivZero.divZero_def", "DivZero.divZero"})
    void aPairOverZeroIsTheQuarterTurn() {
        for (T p : XS) assertEquals(new T(p.q(), p.p().negate()), DivZero.divZero(p));
    }

    @Test
    @Proves({"DivZero.divZero_otimes_negOmega", "DivZero.divZero_injective"})
    void theQuarterTurnLosesNothing() {
        for (T p : XS) assertEquals(p, DivZero.divZero(p).otimes(T.NEG_OMEGA));
        for (T p : XS)
            for (T q : XS) if (!p.equals(q)) assertNotEquals(DivZero.divZero(p), DivZero.divZero(q));
    }

    @Test
    @Proves({"DivZero.divZero_times_zero", "DivZero.divZero_times_zero_not_injective"})
    void timesZeroDoesNotUndoIt() {
        for (T p : XS) assertEquals(new T(BigInteger.ZERO, p.p().negate()), DivZero.divZero(p).times(T.ZERO));
        assertEquals(DivZero.divZero(T.of(1, 1)).times(T.ZERO), DivZero.divZero(T.of(1, 2)).times(T.ZERO));
    }

    @Test
    @Proves("DivZero.divZero_perp")
    void itIsPerpendicularToTheValue() {
        for (T p : XS) {
            T r = DivZero.divZero(p);
            assertEquals(BigInteger.ZERO, p.p().multiply(r.p()).add(p.q().multiply(r.q())));
        }
    }

    @Test
    @Proves({"DivZero.flatten_zero", "DivZero.flatten_zero_not_injective"})
    void flattenDropsTheNumeratorsDenominatorAtZero() {
        for (T a : XS) assertEquals(new T(a.p(), BigInteger.ZERO), new T2(a, T.ZERO).flatten());
        assertEquals(new T2(T.of(1, 1), T.ZERO).flatten(), new T2(T.of(1, 2), T.ZERO).flatten());
    }

    @Test
    @Proves({"DivZero.divide_zero", "DivZero.divide_ne_zero", "DivZero.divide_zero_injective"})
    void divideKeepsTheWholeNumeratorAtZero() {
        for (T a : XS) {
            assertEquals(DivZero.divZero(a), DivZero.divide(a, T.ZERO));
            for (T b : XS) if (!b.equals(T.ZERO)) assertEquals(new T2(a, b).flatten(), DivZero.divide(a, b));
            for (T a2 : XS) if (!a.equals(a2)) assertNotEquals(DivZero.divide(a, T.ZERO), DivZero.divide(a2, T.ZERO));
        }
    }

    @Test
    @Proves({"DivZero.divAlong", "DivZero.divAlong_injective", "DivZero.divideAlong_zero",
            "DivZero.divideAlong_zero_injective", "DivZero.divAlong_times_zero"})
    void withoutTheQuarterTurnAPairOverZeroIsItself() {
        for (T a : XS) {
            assertEquals(a, DivZero.divideAlong(a, T.ZERO));
            assertEquals(new T(BigInteger.ZERO, a.q()), DivZero.divAlong(a).times(T.ZERO));
            for (T b : XS) if (!b.equals(T.ZERO)) assertEquals(new T2(a, b).flatten(), DivZero.divideAlong(a, b));
        }
    }

    @Test
    @Proves("DivZero.divZero_eq_divAlong_otimes")
    void theFirstRuleIsTheSecondTurnedByOmega() {
        for (T p : XS) assertEquals(DivZero.divAlong(p).otimes(T.OMEGA), DivZero.divZero(p));
    }
}
