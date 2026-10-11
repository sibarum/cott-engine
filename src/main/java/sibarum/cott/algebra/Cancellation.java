package sibarum.cott.algebra;

import sibarum.cott.calculator.Mode;
import sibarum.cott.calculator.Modeset;
import sibarum.cott.traction.Lean;

/**
 * What an exact cancellation in {@code Z}'s {@code +} leaves: {@code Z(3, 1) + Z(-3, 1)}. Both keep every other sum,
 * and both are kept, since which invariants are tracked and which erased depends on the use.
 */
public enum Cancellation implements Mode {

    /**
     * {@code x − x = (1 − 1)·x = 0·x}: the residual keeps what cancelled and gains one order, so {@code Z(0, n)} is
     * never reached, and {@code 0/0} is the ratio of what cancelled.
     */
    @Lean({"T.ZeroScale.Residual.R.add", "T.ZeroScale.Residual.R.cancel_eq", "T.ZeroScale.Residual.R.cancel_div_cancel"})
    RESIDUAL("residual", "Residual: x − x = 0·x"),
    /** The coefficients add to {@code 0}, which absorbs: {@code Z(0, n)}, and {@code 0·b = 0} drops {@code b}. */
    @Lean({"T.ZeroScale.toLaurent_add_same", "T.ZeroScale.Z.log_mul_coeff_zero_ne"})
    ABSORB("absorb", "Absorb: x − x has coefficient 0");

    private final String key;
    private final String label;

    Cancellation(String key, String label) {
        this.key = key;
        this.label = label;
    }

    @Override
    public Modeset modeset() {
        return Modeset.CANCELLATION;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String label() {
        return label;
    }
}
