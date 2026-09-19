package sibarum.cott.engine.projection;

import sibarum.cott.engine.base.expr.IExpr;

import java.util.Optional;

/**
 * A traction expression as three floating point numbers, for a plot to draw.
 * <p>
 * {@link Place} is the exact answer; this is the lossy one. Two things are given up here and both of them are
 * a chart's decisions rather than the carrier's: the coordinates become doubles, and a value needing more
 * than three axes is declined instead of trimmed to fit. Neither belongs on {@link Place}, which keeps the
 * rationals it was handed so that a caller wanting the exact value still has one.
 *
 * <h2>An unoccupied axis is still an axis</h2>
 * Most values land on two coordinates and this reports three, with the absence marker in the last. That is
 * not padding to reach a fixed width. The last coordinate is always the innermost exponent, and an exponent
 * with no traction in it is {@code b}, which is {@code b·0^0} — so {@code a·0^b} and {@code a·0^(b·0^0)} are
 * the same value, and the appended zero says the third axis is unoccupied rather than missing. It goes on the
 * end for the same reason: {@code b} stays where it was, as the exponent's real part.
 *
 * <p>The distinction {@link Place} exists to keep survives the widening, because the marker is carried rather
 * than dropped: {@code 1} is {@code (1, 0, 0)} and the point zero is {@code (0, 1, 0)}, two places and not
 * one.
 *
 * <h2>Declining is an answer</h2>
 * Empty comes back in two cases and they are different questions. A term that is not one point — a variable,
 * a sum wanting a third number — has no place at all, and a plot draws it rather than plotting it. A value
 * deeper than three coordinates has a place and cannot be shown in a volume; {@link Place#of} still reports
 * it, and a caller that wants to see it should read the coordinates directly rather than have an axis chosen
 * for it here.
 *
 * <p>A standing sum at unlike orders is in neither case. It is one point, read as the additive pair, and it
 * projects: {@code 0^2 + 1} is {@code (1, 2, 0)}. See {@link Place}.
 */
public final class Projector {

    /** The axes a volume has, and the width of every projection this returns. */
    public static final int AXES = Place.VOLUME;

    private Projector() {
    }

    /**
     * Where this expression lands, in three axes, or empty where it is not one point or does not fit.
     *
     * <p>Simplified first, so a caller may hand in an expression as parsed. Simplification is idempotent, so
     * an expression already settled is unchanged by it. An entry holding a real-valued call is the exception
     * and is not this layer's to answer — route it through the syntax layer and project what comes back.
     */
    public static Optional<double[]> project(IExpr expr) {
        return Place.of(expr.simplify()).flatMap(Projector::project);
    }

    /** As {@link #project(IExpr)}, for a place already found. */
    public static Optional<double[]> project(Place place) {
        if (!place.withinVolume()) {
            return Optional.empty();
        }
        double[] out = new double[AXES];
        double[] found = place.toDoubles();
        System.arraycopy(found, 0, out, 0, found.length);
        return Optional.of(out);
    }
}
