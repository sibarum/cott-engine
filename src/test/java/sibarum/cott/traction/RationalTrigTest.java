package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.traction.Pairs.big;

class RationalTrigTest {

    private static double ratio(T x) {
        return x.p().doubleValue() / x.q().doubleValue();
    }

    @Test
    @Proves("T.quarter_split")
    void aTurnSplitsIntoQuarterTurns() {
        for (long a = -40; a <= 40; a++)
            for (long b = 1; b <= 30; b++) {
                RationalTrig.Split s = RationalTrig.quarterSplit(big(a), big(b));
                assertTrue(s.rest().signum() > 0 && s.rest().compareTo(big(b)) <= 0);
                assertEquals(big(4 * a), s.quarters().multiply(big(b)).add(s.rest()));
            }
    }

    @Test
    @Proves({"T.spinTurn_scale", "T.cosTurn_scale", "T.sinTurn_scale"})
    void aPositiveMultipleOfTheTurnIsTheSameTurn() {
        for (long a = -13; a <= 13; a++)
            for (long b = 1; b <= 12; b++)
                for (long k = 2; k <= 5; k++)
                    for (int n = 0; n <= 20; n += 4)
                        assertEquals(RationalTrig.spinTurn(big(a), b, n), RationalTrig.spinTurn(big(k * a), k * b, n),
                                k + "·" + a + "/" + k + "·" + b + " at " + n);
    }

    @Test
    @Proves({"T.cosTurn_sq_add_sinTurn_sq", "T.spinTurn_ne"})
    void theAnswerIsExactlyOnTheCircle() {
        for (long a = -13; a <= 13; a++)
            for (long b = 1; b <= 12; b++)
                for (int n = 0; n <= 20; n += 4) {
                    assertNotEquals(T.ZERO_OMEGA, RationalTrig.spinTurn(big(a), b, n));
                    T c = RationalTrig.cosTurn(big(a), b, n);
                    T s = RationalTrig.sinTurn(big(a), b, n);
                    assertEquals(c.q(), s.q());
                    assertEquals(c.q().pow(2), c.p().pow(2).add(s.p().pow(2)), a + "/" + b + " at " + n);
                }
    }

    @Test
    @Proves({"T.cosTurn_err", "T.sinTurn_err", "T.turn_target", "T.rotCos_otimes_omegaPow"})
    void theErrorIsBoundedByTheDepth() {
        for (long a = -13; a <= 13; a++)
            for (long b = 1; b <= 12; b++)
                for (int n = 0; n <= 40; n += 5) {
                    double bound = Math.PI * Math.PI / (n + 1) + 1e-12;
                    double c = ratio(RationalTrig.cosTurn(big(a), b, n)) - Math.cos(2 * Math.PI * a / b);
                    double s = ratio(RationalTrig.sinTurn(big(a), b, n)) - Math.sin(2 * Math.PI * a / b);
                    assertTrue(c * c <= bound, "cos " + a + "/" + b + " at " + n);
                    assertTrue(s * s <= bound, "sin " + a + "/" + b + " at " + n);
                }
    }

    @Test
    @Proves({"T.cosTurn", "T.sinTurn", "T.spinTurn"})
    void aSixthOfATurn() {
        T c = RationalTrig.cosTurn(big(1), 6, 8);
        T s = RationalTrig.sinTurn(big(1), 6, 8);
        assertEquals(c.q().pow(2), c.p().pow(2).add(s.p().pow(2)));
        // The quarter and half turns land exactly, since the bracket's upper end reaches them.
        assertEquals(T.of(0, 2), RationalTrig.cosTurn(big(1), 4, 3));
        assertEquals(T.of(2, 2), RationalTrig.sinTurn(big(1), 4, 3));
        assertEquals(T.of(-1, 1), RationalTrig.cosTurn(big(1), 2, 3));
        assertEquals(T.of(1, 1), RationalTrig.cosTurn(big(0), 1, 3));
    }

    @Test
    @Proves({"T.spinTurn", "T.dial", "T.sin_sq_dial"})
    void stoppingAtAWidthIsSomeDepth() {
        BigInteger width = BigInteger.ONE.shiftLeft(40);
        for (long a = -7; a <= 7; a++)
            for (long b = 1; b <= 12; b++) {
                RationalTrig.Dialed d = RationalTrig.dialed(big(a), b, 10_000, width);
                assertEquals(RationalTrig.spinTurn(big(a), b, d.depth()), d.spin());
                boolean reached = d.bracket().widthDenominator().compareTo(width) >= 0;
                assertTrue(reached || d.depth() == 10_000);
                if (d.depth() > 0) {
                    RationalTrig.Dialed before = RationalTrig.dialed(big(a), b, d.depth() - 1, null);
                    assertTrue(before.bracket().widthDenominator().compareTo(width) < 0);
                }
            }
    }
}
