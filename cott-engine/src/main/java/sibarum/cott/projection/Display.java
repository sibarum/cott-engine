package sibarum.cott.projection;

import sibarum.cott.traction.T;

import java.math.BigInteger;
import java.util.Map;

/**
 * How a pair is written: one of the nine named values by its name, otherwise {@code p} over {@code 1},
 * {@code pω} over {@code 0}, or {@code p/q}.
 */
public final class Display {

    private Display() {}

    private static final Map<T, String> NAMED = Map.of(
            T.ZERO, "0", T.ONE, "1", T.OMEGA, "ω", T.UNDER_ONE, "_1", T.UNDER_ZERO, "_0",
            T.NEG_UNDER_ONE, "-_1", T.NEG_OMEGA, "-ω", T.NEG_ONE, "-1", T.ZERO_OMEGA, "0ω");

    public static String of(T x) {
        String named = NAMED.get(x);
        if (named != null) return named;
        if (x.q().equals(BigInteger.ONE)) return x.p().toString();
        // T(p, 0) is p·ω = T(p,1)·T(1,0) exactly, as 0ω and -ω already are.
        if (x.q().signum() == 0) return x.p() + "ω";
        return x.p() + "/" + x.q();
    }
}
