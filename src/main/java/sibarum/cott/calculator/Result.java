package sibarum.cott.calculator;

import sibarum.cott.algebra.Pair;
import sibarum.cott.algebra.TractionAlgebra;
import sibarum.cott.notation.Expr;
import sibarum.cott.projection.Projection;
import sibarum.cott.traction.T;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** What one line of input answers with. {@link #text} is what the user sees first. */
public sealed interface Result {

    String text();

    /** A variable or function was defined. */
    record Defined(String name, String text) implements Result {}

    /** A variable is still free after substitution, so the expression is left as it is. */
    record Unevaluated(Expr expr, String text) implements Result {}

    /**
     * A value: a number of the number type, or a {@link Pair} of a traction algebra whose coordinates are values.
     * {@link #text} writes it as it was computed, by its constructors, and reads back as the same value.
     * {@link #readings} gives the other ways of reading it, by name, written.
     */
    record Value(Object value, String text, Map<String, String> readings) implements Result {

        public Value {
            readings = Collections.unmodifiableMap(new LinkedHashMap<>(readings));
        }

        /** The value as a flat pair {@code T(p, q)}, when it is a {@code Q} of two Integers. */
        public Optional<T> flat() {
            return value instanceof Pair<?>(TractionAlgebra a, BigInteger p, BigInteger q) && a == TractionAlgebra.Q
                    ? Optional.of(new T(p, q)) : Optional.empty();
        }

        /** A projection of the flat pair. */
        public <R> R read(Projection<R> projection) {
            return projection.apply(flat().orElseThrow(() -> new IllegalStateException(text + " is not a flat Q")));
        }
    }
}
