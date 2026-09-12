package sibarum.cott;

import sibarum.cott.engine.base.expr.AtomExpr;
import sibarum.cott.engine.base.expr.CallExpr;
import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.binary.ExponentialOperationExpr;
import sibarum.cott.engine.operation.binary.LogarithmOperationExpr;
import sibarum.cott.engine.operation.binary.MultiplicationOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.operation.unary.ReciprocalOperationExpr;
import sibarum.cott.engine.traction.expr.ITractionPair;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The free names in a term: what it would have to be given before it is one value.
 *
 * <h2>Why this is not in the engine</h2>
 * {@link AtomExpr} holds a variable and a constant under one type and says so deliberately -- to the carrier
 * both are names it has no rule for, and both make the term stand. The difference only exists where names are
 * decided, which is here: pi and e are constants because this layer says they are, and a session that defines
 * one more turns that name from a variable into something {@link Bindings#expand} substitutes away before the
 * term ever reaches the engine. So the walk is over engine nodes, and the judgement is the syntax layer's.
 *
 * <h2>What a caller does with it</h2>
 * A plotter asks this to find out what it is drawing. Empty means the term is one value and belongs at a
 * point -- see {@code Place}; one name means a curve over that name; two means a surface. Expand first: what
 * comes out of {@link Bindings#expand} mentions no defined name, so plotting {@code f(x)} reports {@code x}
 * and draws f's body, rather than reporting {@code f} and drawing an opaque symbol.
 */
public final class Variables {

    /**
     * Names this layer answers for. They are atoms because the theory does not construct them, not because
     * they are unknown, so a term holding one is not a term with a free variable in it.
     */
    private static final Set<String> CONSTANTS = Set.of("π", "e", "i");

    private Variables() {
    }

    /** The free names in a term, in the order first met, so a caller's axes are the ones the user wrote. */
    public static Set<String> of(IExpr t) {
        Set<String> found = new LinkedHashSet<>();
        walk(t, found);
        return found;
    }

    /** Whether this term is one value: nothing left to give it. */
    public static boolean isClosed(IExpr t) {
        return of(t).isEmpty();
    }

    /**
     * The walk is over {@link IExpr}, which is not sealed, so it ends in a default rather than an exhaustive
     * switch -- the same shape {@link Bindings#expand} uses and for the same reason. A node this does not know
     * holds no names as far as this is concerned, which is the right answer for a leaf and the safe one for a
     * node added later.
     */
    private static void walk(IExpr v, Set<String> found) {
        switch (v) {
            case AtomExpr a -> {
                if (!CONSTANTS.contains(a.name())) {
                    found.add(a.name());
                }
            }
            case NegationOperationExpr n -> walk(n.operand(), found);
            case ReciprocalOperationExpr i -> walk(i.operand(), found);
            case AdditionOperationExpr a -> {
                walk(a.left(), found);
                walk(a.right(), found);
            }
            case MultiplicationOperationExpr m -> {
                walk(m.left(), found);
                walk(m.right(), found);
            }
            case ExponentialOperationExpr p -> {
                walk(p.base(), found);
                walk(p.exponent(), found);
            }
            case LogarithmOperationExpr l -> {
                walk(l.base(), found);
                walk(l.operand(), found);
            }
            // Both pairs, by the interface rather than by naming each: which of the two joins the carrier
            // settles on is open, and a walk that listed only the multiplicative one would silently stop
            // finding names the day that changes.
            case ITractionPair p -> {
                walk(p.real(), found);
                walk(p.exponent(), found);
            }
            case CallExpr c -> c.args().forEach(arg -> walk(arg, found));
            default -> {
            }
        }
    }
}
