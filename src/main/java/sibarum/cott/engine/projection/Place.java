package sibarum.cott.engine.projection;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;
import sibarum.cott.engine.traction.expr.ITractionPair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Where a settled value lands, as coordinates.
 * <p>
 * A client draws values; it must not learn the carrier's shape to do it. IMPLEMENTATION-PLAN.md's phase 4
 * records what that costs -- a bridge that read the carrier structurally "encoded the abandoned theory in its
 * shape, not just its imports", and had to go when the carrier moved. The carrier is going to move again, so
 * the projection is published from here and the client reads coordinates.
 *
 * <h2>Two coordinates is the minimum, not the usual case</h2>
 * A value is the pair {@code (a, b)}, and every value has both coordinates even where one of them is the
 * absence marker. So {@code 1} is {@code (1, 0)} and {@code 0} is {@code (0, 1)}: they differ, and a chart
 * that dropped an absent coordinate would put them at the same place and lose the distinction the theory is
 * built on. Some values land on an axis; none of them land on fewer axes.
 *
 * <h2>A third coordinate is a traction in the exponent</h2>
 * The exponent nests, and when it holds a pair of its own that pair's real part is a coordinate too. The
 * spelling reads outermost first, left to right:
 * <pre>
 *  1         (1, 0)             -1        (-1, 0)
 *  0         (0, 1)             w         (0, -1)
 *  2·0       (2, 1)             0^2       (0, 2)
 *  0^(w÷2)   (0, 1÷2, -1)       0^(0^2)   (0, 0, 2)
 * </pre>
 * Three is as deep as a value in simplest form is expected to go. Deeper is not an error and is not refused
 * here -- {@link #of} still reports the coordinates -- but {@link #withinVolume()} is false and a plot in
 * three dimensions should decline to draw it rather than drop a coordinate to make it fit.
 *
 * @param coordinates outermost real part first, the innermost exponent last; never fewer than two
 */
public record Place(List<RationalLiteral> coordinates) {

    /** Every value has both coordinates, including the ones whose second is the absence marker. */
    public static final int MINIMUM = 2;

    /** What a three-dimensional plot can show without dropping anything. */
    public static final int VOLUME = 3;

    public Place {
        if (coordinates.size() < MINIMUM) {
            throw new IllegalArgumentException("a value has at least " + MINIMUM + " coordinates: " + coordinates);
        }
        coordinates = List.copyOf(coordinates);
    }

    /**
     * Where this expression lands, or empty where it is not a value the carrier can place.
     * <p>
     * Empty is the ordinary answer for anything still standing: a term holding a variable, an unanswered call,
     * a sum the rules did not fold. Those are not failures -- a term with a variable in it is the thing a plot
     * is drawn FROM, and it is placed nowhere precisely because it is not one point.
     *
     * @param settled an expression already simplified; an unsimplified one is placed only if it happens to be
     *                in carrier form already
     */
    public static Optional<Place> of(IExpr settled) {
        List<RationalLiteral> out = new ArrayList<>();
        IExpr current = settled;
        boolean value = true;
        while (true) {
            IExpr node = seenThrough(current);
            if (node instanceof ITractionPair pair) {
                if (!(seenThrough(pair.real()) instanceof RationalLiteral real)) {
                    return Optional.empty();
                }
                out.add(signed(real, node != current));
                current = pair.exponent();
                value = false;
                continue;
            }
            if (node instanceof RationalLiteral rational) {
                RationalLiteral it = signed(rational, node != current);
                // At the top a bare rational is a VALUE and reads as a pair; deeper it is an exponent and is
                // the last coordinate as it stands. E4 is the difference: the rational zero as a value is the
                // point zero, the pair (0, 1), and as an exponent it is the absence marker.
                if (value) {
                    out.addAll(asValue(it));
                } else {
                    out.add(it);
                }
                return Optional.of(new Place(out));
            }
            return Optional.empty();
        }
    }

    /**
     * A bare rational read as a value: the pair it is.
     * <p>
     * A nonzero rational is {@code (r, 0)} -- its real part is itself, its traction part absent. The rational
     * zero is the point zero, {@code (0, 1)} by E4, and a zero numerator over a denominator is a multiple of
     * it: {@code 0÷d} is {@code (1÷d)·0}. This mirrors {@code TractionRules.pair}, which is the reading the
     * rules themselves use, so a value is placed where the algebra says it is rather than where its spelling
     * suggests.
     */
    private static List<RationalLiteral> asValue(RationalLiteral r) {
        if (!r.isZero()) {
            return List.of(r, RationalLiteral.ZERO);
        }
        return r.isInteger()
                ? List.of(RationalLiteral.ZERO, RationalLiteral.ONE)
                : List.of(new RationalLiteral(BigInteger.ONE, r.denominator()), RationalLiteral.ONE);
    }

    /**
     * The operand of a negation, or the term itself.
     * <p>
     * Seen through for the reason {@code TractionRules.isCoordinate} sees through it: whether a term is a
     * coordinate must not depend on how far its sign happened to reduce, or one spelling of a value is placed
     * and another identical one is not.
     */
    private static IExpr seenThrough(IExpr e) {
        return e instanceof NegationOperationExpr(IExpr operand) ? seenThrough(operand) : e;
    }

    private static RationalLiteral signed(RationalLiteral r, boolean negated) {
        return negated ? (RationalLiteral) r.negated() : r;
    }

    /** How many axes it takes to show this: two for most values, three for a traction in the exponent. */
    public int dimension() {
        return coordinates.size();
    }

    /** Whether three axes are enough. False means a plot should decline rather than drop a coordinate. */
    public boolean withinVolume() {
        return dimension() <= VOLUME;
    }

    /**
     * The coordinates as reals, in order.
     * <p>
     * This is where exactness ends, and it ends here rather than in the coordinates themselves: the record
     * keeps the rationals it was given, so a caller that wants the exact value still has it.
     */
    public double[] toDoubles() {
        double[] out = new double[coordinates.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = coordinates.get(i).evaluate().orElse(Double.NaN);
        }
        return out;
    }
}
