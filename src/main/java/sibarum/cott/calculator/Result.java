package sibarum.cott.calculator;

import sibarum.cott.notation.Expr;
import sibarum.cott.traction.Quotient;
import sibarum.cott.traction.T;
import sibarum.cott.traction.T2;

/** What one line of input answers with. {@link #text} is what the user sees. */
public sealed interface Result {

    String text();

    /** A variable or function was defined. */
    record Defined(String name, String text) implements Result {}

    /** A variable is still free after substitution, so the expression is left as it is. */
    record Unevaluated(Expr expr, String text) implements Result {}

    /**
     * A value: computed at Level 2, flattened, and read under the chosen quotient. The pairs are kept for
     * whoever needs them; the user sees only {@code text}.
     */
    record Value(T2 level2, T flat, Quotient quotient, T read, String text) implements Result {}
}
