package sibarum.cott.projection;

import sibarum.cott.traction.Lean;
import sibarum.cott.traction.Quotient;
import sibarum.cott.traction.T;

import java.util.Locale;

/** A value read under an invariant: its class, answered by the class's representative. */
public record QuotientProjection(Quotient quotient) implements Projection<T> {

    @Override
    public String name() {
        return quotient.name().toLowerCase(Locale.ROOT);
    }

    @Override
    @Lean("T.Rel")
    public T apply(T x) {
        return quotient.representative(x);
    }

    @Override
    public String display(T reading) {
        return Display.of(reading);
    }
}
