package sibarum.cott.traction;

/**
 * Division with a rule at a zero denominator. {@link T2#flatten} reads {@code A / B} but drops the
 * numerator's denominator where {@code B = 0}; these keep the whole numerator there.
 *
 * <p>cott-lean has two rules, and they differ by one quarter turn ({@code divZero_eq_divAlong_otimes}). The
 * first line of the model, {@code p/0 = tan(arg(p·i))}, is {@link #divZero}; {@link #divAlong} is the same
 * without the {@code ·i}, the point at infinity of homogeneous coordinates. What they share is the
 * model's reading of an integer over zero: {@code tan(arg)} of {@code divAlong(T(n, 0))} is
 * {@code tan(arg(n·i))} ({@code tan_arg_divAlong_intCast}), though the two methods return different pairs
 * for the same input. Which one a calculator answers with is not decided here: both are available.
 */
public final class DivZero {

    private DivZero() {}

    /** {@code p/0 = p ⊗ ω = T(q, −p)}: the point turned a quarter turn. */
    @Lean({"DivZero.divZero", "DivZero.divZero_def"})
    public static T divZero(T p) {
        return p.otimes(T.OMEGA);
    }

    /** {@code A / B} by {@link T2#flatten}, with {@link #divZero} at the denominator {@code 0}. */
    @Lean({"DivZero.divide", "DivZero.divide_zero", "DivZero.divide_ne_zero"})
    public static T divide(T a, T b) {
        return b.equals(T.ZERO) ? divZero(a) : new T2(a, b).flatten();
    }

    /** {@code p/0 = p}: a pair over zero is its own direction. */
    @Lean("DivZero.divAlong")
    public static T divAlong(T p) {
        return p;
    }

    /** {@code A / B} by {@link T2#flatten}, with {@link #divAlong} at the denominator {@code 0}. */
    @Lean({"DivZero.divideAlong", "DivZero.divideAlong_zero"})
    public static T divideAlong(T a, T b) {
        return b.equals(T.ZERO) ? divAlong(a) : new T2(a, b).flatten();
    }
}
