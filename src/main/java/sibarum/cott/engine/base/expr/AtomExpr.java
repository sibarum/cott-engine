package sibarum.cott.engine.base.expr;

import java.util.Optional;

/**
 * A name standing for itself: a variable, or a constant with no construction in the theory.
 * <p>
 * Two kinds of name arrive here and they are deliberately not distinguished. A variable -- {@code x} -- has no
 * value because none was given. A constant like pi or e has no value because neither has a base-0 exponential
 * form and neither is derivable from the primitives, so there is nothing for it to reduce to. In both cases the
 * term stands, which is the same answer the engine gives everywhere else it has no rule, and a plotter can still
 * draw what it holds.
 * <p>
 * An atom is a leaf. It carries no arithmetic of its own, so an operation on one builds a node and stops.
 *
 * @param name as it was written, and as it will be printed
 */
public record AtomExpr(String name) implements IExpr {

    public AtomExpr {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("an atom needs a name");
        }
    }

    @Override
    public Optional<Double> evaluate() {
        return Optional.empty();
    }
}
