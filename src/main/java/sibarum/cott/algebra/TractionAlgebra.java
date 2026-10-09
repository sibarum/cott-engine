package sibarum.cott.algebra;

import sibarum.cott.traction.Lean;

/**
 * The traction algebras: what a pair {@code (p, q)} means. The pair type says what {@code p} and {@code q} are and what
 * {@code +} and {@code ·} do, so the same two numbers are a different value in each, as a vector is not a bivector.
 *
 * <p>Each algebra is the one its reading turns into {@code +} and {@code ×} ({@code PairAlgebras}). A number enters
 * on one coordinate, its slot, with the other the identity there: {@code q} is the slot, except in {@code Q}, where
 * {@code q} is the denominator.
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
    P("p · q");

    private final String meaning;

    TractionAlgebra(String meaning) {
        this.meaning = meaning;
    }

    /** What a pair of this algebra stands for, in {@code p} and {@code q}. */
    public String meaning() {
        return meaning;
    }

    /** Whether this algebra reads its pair as a sum ({@code C}, {@code D}, {@code S}) rather than a product ({@code Q}, {@code P}). */
    public boolean isSum() {
        return this == C || this == D || this == S;
    }
}
