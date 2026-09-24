package sibarum.cott.traction;

import java.math.BigInteger;

/**
 * An invariant a use may specify, as the quotient by a multiplicative set {@code S}: two pairs are
 * identified when a multiplier from {@code S} takes each to the same place, {@code s·x = t·y}.
 *
 * <p>{@code +}, {@code *}, the reciprocal and {@code -} all respect every one of these ({@code T.rel_*}),
 * so a value may be read under any of them after it is computed and the reading does not depend on when
 * it is taken. Each quotient answers with a {@link #representative}: dividing both coordinates by their
 * common factor is a multiplier from {@code S}, so the representative is in the class it stands for.
 */
@Lean({"T.Rel", "T.setoid", "T.rel_plus_left", "T.rel_times_left", "T.rel_reciprocal", "T.rel_neg"})
public enum Quotient {

    /** {@code S = {1}}, which identifies nothing: the pairs as they are. */
    @Lean("T.rel_bot_iff")
    NONE,

    /** {@code S} the positive integers: a pair is its positive multiples. */
    @Lean("T.Rel")
    RAY,

    /** {@code S} the non-zero integers: a pair is its ratio, with {@code 0ω} kept apart. */
    @Lean("T.ratioRel_iff")
    RATIO;

    /** The one pair each class is answered with. */
    @Lean("T.Rel")
    public T representative(T x) {
        if (this == NONE) return x;
        BigInteger g = x.p().gcd(x.q());
        if (g.signum() == 0) return x; // 0ω is its own class under every S that excludes 0
        T ray = new T(x.p().divide(g), x.q().divide(g));
        if (this == RAY) return ray;
        boolean flip = ray.q().signum() < 0 || (ray.q().signum() == 0 && ray.p().signum() < 0);
        return flip ? ray.scale(BigInteger.ONE.negate()) : ray;
    }

    /** {@code x ~ y} under this invariant. */
    @Lean("T.Rel")
    public boolean equivalent(T x, T y) {
        return representative(x).equals(representative(y));
    }
}
