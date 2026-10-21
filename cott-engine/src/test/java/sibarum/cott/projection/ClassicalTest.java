package sibarum.cott.projection;

import org.junit.jupiter.api.Test;
import sibarum.cott.traction.Pairs;
import sibarum.cott.traction.Proves;
import sibarum.cott.traction.T;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassicalTest {

    private static final List<T> XS = Pairs.flat(10);
    private static final Classical C = Projections.CLASSICAL;

    // The common meadow's operations, with the error element (empty) absorbing.

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
    @Proves({"T.toQa_plus", "T.toQa_times", "T.Qa.add", "T.Qa.mul"})
    void plusAndTimesSurviveExactly() {
        for (T x : XS)
            for (T y : XS) {
                assertEquals(add(C.apply(x), C.apply(y)), C.apply(x.plus(y)), x + " + " + y);
                assertEquals(mul(C.apply(x), C.apply(y)), C.apply(x.times(y)), x + " * " + y);
            }
    }

    @Test
    @Proves({"T.toQa_neg", "T.Qa.neg"})
    void negSurvives() {
        for (T x : XS) assertEquals(C.apply(x).map(Rational::neg), C.apply(x.neg()));
    }

    @Test
    @Proves({"T.toQa_reciprocal", "T.Qa.inv"})
    void theReciprocalSurvivesAwayFromTheQuarterTurns() {
        for (T x : XS)
            if (x.q().signum() != 0 || x.p().signum() == 0)
                assertEquals(inv(C.apply(x)), C.apply(x.reciprocal()), x.toString());
    }

    @Test
    @Proves("T.toQa_reciprocal_quarterTurn")
    void atAQuarterTurnTSaysZeroAndTheMeadowSaysError() {
        for (T x : XS)
            if (x.q().signum() == 0 && x.p().signum() != 0) {
                assertEquals(Optional.of(Rational.ZERO), C.apply(x.reciprocal()));
                assertEquals(Optional.empty(), inv(C.apply(x)));
            }
    }

    @Test
    @Proves("T.toQa")
    void theNamedValues() {
        assertEquals("0", C.read(T.ZERO));
        assertEquals("0", C.read(T.UNDER_ZERO));
        assertEquals("-1", C.read(T.UNDER_ONE));
        assertEquals("undefined", C.read(T.OMEGA));
        assertEquals("undefined", C.read(T.ZERO_OMEGA));
        assertEquals("1/2", C.read(T.of(2, 4)));
    }
}
