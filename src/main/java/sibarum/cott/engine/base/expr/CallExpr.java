package sibarum.cott.engine.base.expr;

import java.util.List;
import java.util.Optional;

/**
 * A function applied to arguments, written the way it is typed: {@code sin(x)}, {@code f(2, 3)}.
 * <p>
 * The node says only that a call was written. Who answers it is not recorded here and is not the same in both
 * cases: a real-valued function is a catalogue entry the syntax layer can look up, and a defined function is
 * expanded away before it ever reaches simplification. Nothing in the carrier evaluates a call, so an
 * unanswered one stands -- {@code sin(x)} is a term, and a term a plot can still be drawn from.
 *
 * @param name as written
 * @param args in the order written; may be empty for a name used as a functor
 */
public record CallExpr(String name, List<IExpr> args) implements IExpr {

    public CallExpr {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("a call needs a name");
        }
        args = List.copyOf(args);
    }

    @Override
    public Optional<Double> evaluate() {
        return Optional.empty();
    }
}
