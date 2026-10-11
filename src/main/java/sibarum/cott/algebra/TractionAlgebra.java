package sibarum.cott.algebra;

import sibarum.cott.traction.Lean;

/**
 * The traction algebras: what a pair {@code (p, q)} means. The pair type says what {@code p} and {@code q} are and what
 * {@code +} and {@code ·} do, so the same two numbers are a different value in each, as a vector is not a bivector.
 *
 * <p>Each algebra is the one its reading turns into {@code +} and {@code ×} ({@code PairAlgebras}). A number enters
 * on one coordinate, its slot, with the other the identity there: {@code q} is the slot, except in {@code Q}, where
 * {@code q} is the denominator, and in {@code Z}, where it is the order. {@code B} has no slot: a number that meets
 * it takes the pair whose reading is the operation.
 */
public enum TractionAlgebra {

    /** {@code q + ip}: {@code +} is the sum of the points and {@code ·} their product, {@code ⊗}. A number {@code v} is {@code C(0, v)}. */
    @Lean({"T.Unquotiented.C", "T.Unquotiented.C_add", "T.Unquotiented.C_pairOtimes"})
    C("q + ip"),
    /** {@code q − p}: {@code +} is the sum of the points and {@code ·} is {@code ⊚}. A number {@code v} is {@code D(0, v)}. */
    @Lean({"T.Unquotiented.D", "T.Unquotiented.D_add", "T.Unquotiented.D_pairSplit"})
    D("q − p"),
    /** {@code p + q}: the same {@code +} and {@code ⊚} as {@code D}, read as a sum. A number {@code v} is {@code S(0, v)}. */
    @Lean({"T.Unquotiented.S", "T.Unquotiented.S_add", "T.Unquotiented.S_pairSplit"})
    S("p + q"),
    /** {@code p / q}: fraction {@code +} and {@code ·}, the wheel. A number {@code v} is {@code Q(v, 1)}. */
    @Lean({"T.Unquotiented.Q", "T.Unquotiented.Q_pairPlus", "T.Unquotiented.Q_pairTimes"})
    Q("p / q"),
    /** {@code p · q}: {@code ·} only, coordinate by coordinate; no flat {@code +} adds it. A number {@code v} is {@code P(1, v)}. */
    @Lean({"T.Unquotiented.P", "T.Unquotiented.P_pairTimes", "T.Unquotiented.no_homogeneous_P_add"})
    P("p · q"),
    /**
     * {@code p · 0^q}: a coefficient and an order of zero, the {@code 0} invertible, so {@code 0 = Z(1, 1)} and
     * {@code ω = Z(1, −1)}. {@code ·} multiplies the coefficients and adds the orders; {@code +} adds the coefficients
     * at one order, and what an exact cancellation leaves is the {@link Cancellation}'s. A number {@code v} is
     * {@code Z(v, 0)}.
     */
    @Lean({"T.ZeroScale.Z", "T.ZeroScale.Z.mul_def", "T.ZeroScale.toLaurent_add_same", "T.ZeroScale.Residual.R"})
    Z("p · 0^q"),
    /**
     * {@code q + log₀ p}, the logarithm of {@code Z(p, q)} in base {@code 0}: its {@code +} is {@code Z}'s {@code ·},
     * and no product multiplies two of them. A number {@code v} is {@code E(1, v)}.
     */
    @Lean({"T.ZeroScale.E", "T.ZeroScale.E_add", "T.ZeroScale.Z.log"})
    E("q + log₀ p"),
    /** {@code log_p q}, the ratio of two {@code E}s of order {@code 0}. Its one product composes, {@code B(p, q)·B(q, r) = B(p, r)}. */
    @Lean({"T.ZeroScale.B", "T.ZeroScale.E_eq_B_mul", "T.ZeroScale.B_mul_B"})
    B("log_p q");

    private final String meaning;

    TractionAlgebra(String meaning) {
        this.meaning = meaning;
    }

    /** What a pair of this algebra stands for, in {@code p} and {@code q}. */
    public String meaning() {
        return meaning;
    }

    /**
     * Whether this algebra reads its pair as a sum ({@code C}, {@code D}, {@code S}, {@code E}) rather than a product
     * ({@code Q}, {@code P}, {@code Z}) or a ratio of logarithms ({@code B}).
     */
    public boolean isSum() {
        return this == C || this == D || this == S || this == E;
    }
}
