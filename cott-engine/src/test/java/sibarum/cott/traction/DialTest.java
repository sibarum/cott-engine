package sibarum.cott.traction;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.traction.Pairs.big;

class DialTest {

    /** Every target {@code a/b} in {@code (0, 1/4]} with {@code b ≤ 24}, and each depth up to 30. */
    private static void eachBracket(BiConsumer<long[], Dial.Bracket> check) {
        for (long b = 1; b <= 24; b++)
            for (long a = 1; 4 * a <= b; a++) {
                Dial.Bracket br = Dial.Bracket.START;
                for (int n = 0; n <= 30; n++) {
                    check.accept(new long[]{a, b, n}, br);
                    br = Dial.step(big(a), b, br);
                }
            }
    }

    @Test
    @Proves({"T.dial", "T.dialStep_eq"})
    void fiveStepsTowardATwelfth() {
        assertEquals(new Dial.Bracket(T.of(4, 7), T.of(3, 5)), Dial.dial(big(1), 12, 5));
        assertEquals(Dial.Bracket.START, Dial.dial(big(1), 12, 0));
    }

    @Test
    @Proves({"T.dialStep_eq", "T.dialStep"})
    void theStepReplacesOneEndByTheMediant() {
        eachBracket((t, br) -> {
            Dial.Bracket next = Dial.step(big(t[0]), t[1], br);
            T m = br.lower().oplus(br.upper());
            if (Winding.turnLt(m, big(t[0]), t[1])) assertEquals(new Dial.Bracket(m, br.upper()), next);
            else assertEquals(new Dial.Bracket(br.lower(), m), next);
        });
    }

    @Test
    @Proves({"T.det_dial", "T.dial_isCoprime", "T.dial_nonneg"})
    void theEndsAreNeighboursInTheFirstQuadrant() {
        eachBracket((t, br) -> {
            assertEquals(BigInteger.ONE, br.lower().det(br.upper()));
            assertEquals(BigInteger.ONE, br.lower().p().gcd(br.lower().q()));
            assertEquals(BigInteger.ONE, br.upper().p().gcd(br.upper().q()));
            for (BigInteger c : new BigInteger[]{br.lower().p(), br.lower().q(), br.upper().p(), br.upper().q()})
                assertTrue(c.signum() >= 0);
        });
    }

    @Test
    @Proves({"T.dial_bracket", "T.turnLt_iff"})
    void theBracketIsCertified() {
        eachBracket((t, br) -> {
            assertTrue(Winding.turnLt(br.lower(), big(t[0]), t[1]), "lower " + br + " " + t[0] + "/" + t[1]);
            assertFalse(Winding.turnLt(br.upper(), big(t[0]), t[1]), "upper " + br + " " + t[0] + "/" + t[1]);
        });
    }

    @Test
    @Proves({"T.norm_dial", "T.one_le_norm"})
    void thePairsKeepGrowing() {
        eachBracket((t, br) -> assertTrue(
                br.lower().norm().add(br.upper().norm()).compareTo(big(t[2] + 2)) >= 0));
    }

    @Test
    @Proves({"T.sin_sq_dial", "T.sin_theta_sub", "T.turn_width_dial"})
    void theBracketsWidthIsExact() {
        eachBracket((t, br) -> {
            double l = Math.atan2(br.lower().p().doubleValue(), br.lower().q().doubleValue());
            double r = Math.atan2(br.upper().p().doubleValue(), br.upper().q().doubleValue());
            double sin = Math.sin(r - l);
            assertEquals(1 / br.widthDenominator().doubleValue(), sin * sin, 1e-12);
            double width = (r - l) / (2 * Math.PI);
            assertTrue(width * width <= 1 / (16 * (t[2] + 1.0)) + 1e-15);
        });
    }
}
