package sibarum.cott.traction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Pairs to check theorems on: the nine named values, every small pair, and some large ones. */
final class Pairs {

    private Pairs() {}

    /** The nine, then every pair with coordinates in [-3, 3], then {@code large} pairs of 40-bit coordinates. */
    static List<T> flat(int large) {
        List<T> out = new ArrayList<>(T.NINE);
        for (int p = -3; p <= 3; p++)
            for (int q = -3; q <= 3; q++) {
                T t = T.of(p, q);
                if (!out.contains(t)) out.add(t);
            }
        Random r = new Random(20260924);
        for (int i = 0; i < large; i++)
            out.add(new T(new BigInteger(40, r).subtract(BigInteger.ONE.shiftLeft(39)),
                    new BigInteger(40, r).subtract(BigInteger.ONE.shiftLeft(39))));
        return out;
    }

    /** Pairs of pairs: the named values and embeddings, then pairs of the small flat pairs. */
    static List<T2> nested() {
        List<T2> out = new ArrayList<>(List.of(T2.ZERO, T2.ONE, T2.OMEGA));
        for (T x : T.NINE) out.add(T2.of(x));
        List<T> small = new ArrayList<>(T.NINE);
        small.add(T.of(2, 3));
        small.add(T.of(-3, 2));
        for (T a : small)
            for (T b : small) out.add(new T2(a, b));
        return out;
    }

    static BigInteger big(long n) {
        return BigInteger.valueOf(n);
    }
}
