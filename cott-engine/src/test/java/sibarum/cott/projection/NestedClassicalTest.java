package sibarum.cott.projection;

import org.junit.jupiter.api.Test;
import sibarum.cott.traction.Pairs;
import sibarum.cott.traction.Proves;
import sibarum.cott.traction.T;
import sibarum.cott.traction.T2;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NestedClassicalTest {

    private static final List<T2> XS = Pairs.nested();
    private static final Classical C = Projections.CLASSICAL;

    private static Optional<Rational> add(Optional<Rational> x, Optional<Rational> y) {
        return x.isPresent() && y.isPresent() ? Optional.of(x.get().plus(y.get())) : Optional.empty();
    }

    private static Optional<Rational> mul(Optional<Rational> x, Optional<Rational> y) {
        return x.isPresent() && y.isPresent() ? Optional.of(x.get().times(y.get())) : Optional.empty();
    }

    private static Optional<Rational> inv(Optional<Rational> x) {
        return x.filter(r -> r.num().signum() != 0).map(Rational::inverse);
    }

    @Test
    @Proves("T2.toQa_flatten")
    void theClassicalValueIsAOverBExceptAtAQuarterTurn() {
        for (T2 x : XS)
            if (x.q().q().signum() != 0 || x.q().p().signum() == 0)
                assertEquals(mul(C.apply(x.p()), inv(C.apply(x.q()))), C.apply(x.flatten()), x.toString());
    }

    @Test
    @Proves("T2.toQa_flatten_plus")
    void plusSurvivesWhenNeitherOuterDenominatorIsAQuarterTurn() {
        for (T2 x : XS)
            for (T2 y : XS)
                if (x.q().q().signum() != 0 && y.q().q().signum() != 0)
                    assertEquals(add(C.apply(x.flatten()), C.apply(y.flatten())), C.apply(x.plus(y).flatten()),
                            x + " + " + y);
    }

    @Test
    @Proves("T2.plus_quarterTurn")
    void andFailsWhenOneIs() {
        T2 x = new T2(T.ONE, T.OMEGA);
        assertEquals(Optional.empty(), C.apply(x.plus(T2.ONE).flatten()));
        assertEquals(Optional.of(Rational.of(1, 1)), add(C.apply(x.flatten()), C.apply(T2.ONE.flatten())));
    }
}
