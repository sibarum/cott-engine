package sibarum.cott.traction;

import java.math.BigInteger;

/**
 * Dialing in a turn: the mediant descent. Start from the bracket {@code (0, ω)}, a quarter turn wide, and
 * repeat: take the mediant {@code M = L ⊕ R}, ask whether {@code M}'s turn is under {@code a/b}, and let
 * {@code M} replace {@code L} if it is and {@code R} if not. With {@code s} the answer as {@code 1} or
 * {@code 0}, the step is {@code L' = L ⊕ s·R}, {@code R' = R ⊕ (1 − s)·L}.
 *
 * <p>For {@code a/b} in {@code (0, 1/4]}, at every depth {@code det L R = 1} and
 * {@code turn L < a/b ≤ turn R}: the answer is certified, not rounded. The bracket's width is exact too,
 * {@code sin²(θR − θL) = 1/(N(L)·N(R))}.
 */
public final class Dial {

    private Dial() {}

    /** The two ends: {@code lower}'s turn is under the target, {@code upper}'s is not. */
    public record Bracket(@Lean("T.dial") T lower, @Lean("T.dial") T upper) {

        /** The bracket the descent starts from, {@code (0, ω)}. */
        @Lean("T.dial")
        public static final Bracket START = new Bracket(T.ZERO, T.OMEGA);

        /** {@code N(L)·N(R)}: the sine of the angle between the ends, squared, is one over this. */
        @Lean("T.sin_sq_dial")
        public BigInteger widthDenominator() {
            return lower.norm().multiply(upper.norm());
        }
    }

    /** One step of the descent: the mediant replaces {@code L} if the target is past it, {@code R} otherwise. */
    @Lean("T.dialStep")
    public static Bracket step(BigInteger a, long b, Bracket br) {
        BigInteger s = Winding.turnLt(br.lower().oplus(br.upper()), a, b) ? BigInteger.ONE : BigInteger.ZERO;
        return new Bracket(br.lower().oplus(br.upper().scale(s)),
                br.upper().oplus(br.lower().scale(BigInteger.ONE.subtract(s))));
    }

    /** {@code n} steps from {@code (0, ω)}. */
    @Lean("T.dial")
    public static Bracket dial(BigInteger a, long b, int n) {
        Bracket br = Bracket.START;
        for (int i = 0; i < n; i++) br = step(a, b, br);
        return br;
    }
}
