package sibarum.cott.engine.projection;

import sibarum.cott.engine.base.expr.IExpr;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.rational.expr.RationalLiteral;
import sibarum.cott.engine.traction.expr.AdditiveTractionLiteral;
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
 * <h2>A sum at unlike orders is placed, by the additive reading</h2>
 * {@code 1 + 0} and {@code 0^2 + 1} stand in the carrier, because the rules do not absorb a point zero into
 * a value. They are still one point each: they are the additive pair {@code n + 0^t} of Traction-Theory.md's
 * Carrier section, and its two coordinates are the two axes that are already here. So {@code 1 + 0} is
 * {@code (1, 1)} and {@code 0^2 + 1} is {@code (1, 2)} -- the 45 degree positions, placed rather than
 * declined.
 *
 * <p>Nothing had to move aside to let them in. A multiplicative pair whose real part is exactly one collapses
 * by {@code x·1 = x} -- {@code 1·0^2} is {@code 0^2} -- so those coordinates were unreachable from that side,
 * and were free for this one.
 *
 * <p>That is not true further out: {@code 2·0^2} and {@code 2 + 0^2} are two terms, and they are placed
 * together at {@code (2, 2)}. Which join reached a coordinate is not recorded here, so a client reads where
 * the value landed rather than which node holds it. Whether those two ought to land together is the
 * carrier's question and it is open -- see {@link ITractionPair}.
 *
 * <p>A sum wanting three numbers is placed on three. {@code 1 + (1÷2)·0} is a real part, a multiplicity and
 * an exponent, and {@code 1 - 0} is the same shape, since a negated traction part carries a real part of -1
 * rather than staying bare. Those three are the three the nesting already spells, in the order it spells
 * them, so they take no axis of their own: {@code 1 - 0} is {@code (1, -1, 1)} the way {@code 0^(w÷2)} is
 * {@code (0, 1÷2, -1)}. This used to decline, on the ground that there was no coordinate left to keep the
 * multiplicity in. There was -- the third.
 *
 * <p>What still has no place is a sum of two unlike traction parts, {@code 0^2 + 0^3} or {@code 2·0 - ω}:
 * two exponents and no real part to hang the chain from. That wants four numbers as two pairs rather than as
 * one chain, which is a different shape from anything here, and the way out of it is an addition law that
 * settles the sum to one pair first. That is the carrier's question, not this one's.
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
                // The sign below is the MULTIPLICATIVE one: -(n·0^t) is (-n)·0^t, so it turns the real part
                // and the traction part is untouched. Under the other join it is not that -- negating
                // n + 0^t negates both parts, which is the inverse the change of join did not carry -- so a
                // negated additive pair declines, exactly as its spelling as a standing sum already does
                // below. Placing it here would put -(1 + 0) where (-1) + 0 is.
                if (node != current && node instanceof AdditiveTractionLiteral) {
                    return Optional.empty();
                }
                if (!(seenThrough(pair.real()) instanceof RationalLiteral real)) {
                    return Optional.empty();
                }
                out.add(signed(real, node != current));
                current = pair.exponent();
                value = false;
                continue;
            }
            if (node == current && node instanceof AdditionOperationExpr sum) {
                Optional<Pair> read = asAdditivePair(sum);
                if (read.isEmpty()) {
                    return Optional.empty();
                }
                out.addAll(read.get().leading());
                current = read.get().exponent();
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
     * A sum read as the additive pair: the coordinates it contributes, and the exponent still to walk.
     * <p>
     * Usually one coordinate, the real part. Two where the traction addend carries a multiplicity of its own
     * -- {@code 1 - 0} is {@code 1 + (-1)·0^1}, a real part, a multiplicity and an exponent. Those are the
     * same three the nesting already spells, in the same order, so they are contributed here rather than
     * given an axis of their own: outermost real part first, innermost exponent last.
     */
    private record Pair(List<RationalLiteral> leading, IExpr exponent) {
    }

    /**
     * A standing sum read as {@code n + 0^t}, or empty where it is not that shape.
     * <p>
     * Either side may be the real part, since nothing ordered the sum on its way here. A negated sum is not
     * read at all -- that is handled by the caller, because negating {@code n + 0^t} negates both parts and
     * the additive node's inverse is not one of the things that survived the change of join.
     */
    private static Optional<Pair> asAdditivePair(AdditionOperationExpr sum) {
        return paired(sum.left(), sum.right()).or(() -> paired(sum.right(), sum.left()));
    }

    private static Optional<Pair> paired(IExpr real, IExpr traction) {
        Optional<RationalLiteral> n = realPart(real);
        if (n.isEmpty()) {
            return Optional.empty();
        }
        Optional<IExpr> bare = bareExponent(traction);
        if (bare.isPresent()) {
            return Optional.of(new Pair(List.of(n.get()), bare.get()));
        }
        return multiplied(traction)
                .map(m -> new Pair(List.of(n.get(), m.leading().getFirst()), m.exponent()));
    }

    /**
     * An addend that is a traction part with a multiplicity: its multiplicity, and its exponent.
     * <p>
     * This is the half {@link #bareExponent} declines. {@code -0^t} is {@code (-1)·0^t} and {@code 2·0} is
     * {@code 2·0^1}; both have a real part of their own, and it is a coordinate rather than something to
     * drop. An absent real part is one copy, which is what lets a negated bare traction through --
     * {@code -ω} is {@code (-1)·0^-1}, so the sign becomes the multiplicity the pair had no room for.
     * <p>
     * The point zero is not read here. It reaches {@link #bareExponent} as {@code 0^1} and belongs there;
     * arriving with a denominator it is {@code (1÷d)·0}, and that d is a multiplicity this would have to
     * invent an exponent for.
     */
    private static Optional<Pair> multiplied(IExpr e) {
        IExpr node = seenThrough(e);
        if (!(node instanceof ITractionPair pair)) {
            return Optional.empty();
        }
        IExpr real = seenThrough(pair.real());
        if (!(real instanceof RationalLiteral r)) {
            return Optional.empty();
        }
        RationalLiteral copies = pair.isBare() ? RationalLiteral.ONE : signed(r, real != pair.real());
        return Optional.of(new Pair(List.of(signed(copies, node != e)), pair.exponent()));
    }

    /**
     * An addend that is a real part: a rational that is not zero.
     * <p>
     * The rational zero is excluded because as a value it is not a real part at all -- it is the point zero,
     * which is the other side of this pair. A zero numerator over a denominator is excluded for that reason
     * and one more: it is {@code (1÷d)·0}, a multiplicity of the point zero, and there is no coordinate left
     * to keep the d in.
     */
    private static Optional<RationalLiteral> realPart(IExpr e) {
        IExpr node = seenThrough(e);
        return node instanceof RationalLiteral r && !r.isZero()
                ? Optional.of(signed(r, node != e))
                : Optional.empty();
    }

    /**
     * An addend that is a bare traction part, as its exponent.
     * <p>
     * The rational zero counts, because the point zero is {@code 0^1}: that is what makes {@code 1 + 0} the
     * pair {@code (1, 1)} rather than a shape this declines. A negated one does not count -- {@code -0^t} is
     * {@code (-1)·0^t}, which has a real part of its own and so wants a third number.
     */
    private static Optional<IExpr> bareExponent(IExpr e) {
        if (seenThrough(e) != e) {
            return Optional.empty();
        }
        if (e instanceof RationalLiteral r && r.isZero()) {
            return r.isInteger() ? Optional.of(RationalLiteral.ONE) : Optional.empty();
        }
        return e instanceof ITractionPair pair && pair.isBare()
                ? Optional.of(pair.exponent())
                : Optional.empty();
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
