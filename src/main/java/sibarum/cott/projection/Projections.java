package sibarum.cott.projection;

import sibarum.cott.traction.Quotient;

import java.util.List;
import java.util.Optional;

/** Every projection the calculator offers, in the order they are shown. */
public final class Projections {

    private Projections() {}

    public static final QuotientProjection RAY = new QuotientProjection(Quotient.RAY);
    public static final QuotientProjection RATIO = new QuotientProjection(Quotient.RATIO);
    public static final Classical CLASSICAL = new Classical();
    public static final Angle ANGLE = new Angle();
    public static final Point POINT = new Point();

    public static final List<Projection<?>> ALL = List.of(RAY, RATIO, CLASSICAL, ANGLE, POINT);

    public static Optional<Projection<?>> named(String name) {
        return ALL.stream().filter(p -> p.name().equals(name)).findFirst();
    }
}
