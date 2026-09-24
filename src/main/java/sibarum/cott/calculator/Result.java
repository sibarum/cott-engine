package sibarum.cott.calculator;

import sibarum.cott.notation.Expr;
import sibarum.cott.projection.Projection;
import sibarum.cott.projection.Projections;
import sibarum.cott.traction.T;
import sibarum.cott.traction.T2;

import java.util.LinkedHashMap;
import java.util.Map;

/** What one line of input answers with. {@link #text} is what the user sees first. */
public sealed interface Result {

    String text();

    /** A variable or function was defined. */
    record Defined(String name, String text) implements Result {}

    /** A variable is still free after substitution, so the expression is left as it is. */
    record Unevaluated(Expr expr, String text) implements Result {}

    /**
     * A value: computed at Level 2 and flattened. {@code flat} is the ground truth, with no quotient, and
     * {@code text} writes it. Every other reading is a {@link Projection} of it, taken on demand.
     */
    record Value(T2 level2, T flat, String text) implements Result {

        public <R> R read(Projection<R> projection) {
            return projection.apply(flat);
        }

        /** Every projection the calculator offers, by name, as written. */
        public Map<String, String> readings() {
            Map<String, String> out = new LinkedHashMap<>();
            for (Projection<?> p : Projections.ALL) out.put(p.name(), p.read(flat));
            return out;
        }
    }
}
