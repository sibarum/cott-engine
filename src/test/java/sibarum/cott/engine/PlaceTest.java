package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.Cott;
import sibarum.cott.engine.projection.Place;
import sibarum.cott.engine.rational.expr.RationalLiteral;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Where values land. Written against the entries a user types rather than against constructed carrier nodes,
 * because the projection's whole job is to be what a client reads instead of the carrier's shape.
 */
class PlaceTest {

    private static Place place(String entry) {
        Optional<Place> p = Place.of(Cott.derive(entry).to());
        assertTrue(p.isPresent(), entry + " should have a place");
        return p.get();
    }

    private static List<RationalLiteral> at(int... pairs) {
        List<RationalLiteral> out = new java.util.ArrayList<>();
        for (int i = 0; i < pairs.length; i += 2) {
            out.add(RationalLiteral.of(pairs[i], pairs[i + 1]));
        }
        return out;
    }

    /** The four points are four distinct places, and none of them is on fewer than two axes. */
    @Test
    void theFourPoints() {
        assertEquals(at(1, 1, 0, 1), place("1").coordinates());
        assertEquals(at(-1, 1, 0, 1), place("-1").coordinates());
        assertEquals(at(0, 1, 1, 1), place("0").coordinates());
        assertEquals(at(0, 1, -1, 1), place("w").coordinates());
    }

    /**
     * The distinction the chart exists for. An absent coordinate is kept, so the point zero and one are two
     * places rather than one -- dropping it would put both at the same coordinate.
     */
    @Test
    void oneAndTheePointZeroDoNotCollide() {
        assertEquals(2, place("1").dimension());
        assertEquals(2, place("0").dimension());
        assertFalse(place("1").coordinates().equals(place("0").coordinates()));
    }

    /** A multiplicity is the real coordinate, and it survives: 2·0 keeps its 2. */
    @Test
    void aMultiplicityIsTheRealCoordinate() {
        assertEquals(at(2, 1, 1, 1), place("2·0").coordinates());
        assertEquals(at(2, 1, -1, 1), place("2w").coordinates());
        // 0÷3 is (1÷3)·0 -- the divisor is conserved rather than dropped.
        assertEquals(at(1, 3, 1, 1), place("0÷3").coordinates());
    }

    /** An order of vanishing is the exponent coordinate, with no real part. */
    @Test
    void aPowerOfZeroIsOnTheTractionAxis() {
        assertEquals(at(0, 1, 2, 1), place("0^2").coordinates());
        assertEquals(at(0, 1, 2, 3), place("0^2-0^3").coordinates());
        assertEquals(at(0, 1, 5, 1), place("0^2·0^3").coordinates());
    }

    /** A traction in the exponent is the third coordinate, read outermost first. */
    @Test
    void aNestedTractionIsThreeDimensional() {
        assertEquals(at(0, 1, 1, 2, -1, 1), place("0^(w÷2)").coordinates());
        assertEquals(3, place("0^(w÷2)").dimension());
        assertEquals(at(0, 1, 0, 1, 2, 1), place("0^(0^2)").coordinates());
        assertTrue(place("0^(0^2)").withinVolume());
    }

    /** Deeper than a volume is reported, not dropped: a plot declines rather than losing a coordinate. */
    @Test
    void deeperThanAVolumeIsReportedRatherThanFlattened() {
        Place deep = place("0^(0^(0^2))");
        assertEquals(4, deep.dimension());
        assertFalse(deep.withinVolume());
    }

    /** Arithmetic lands where the algebra says, not where the spelling suggests. */
    @Test
    void answersArePlacedByTheirValue() {
        assertEquals(place("1").coordinates(), place("0·w").coordinates());
        assertEquals(place("0").coordinates(), place("1-1").coordinates());
        assertEquals(place("w").coordinates(), place("1÷0").coordinates());
        assertEquals(at(4, 1, 0, 1), place("2+2").coordinates());
    }

    /** A term that is not one point has no place, which is how a plot knows to draw it instead. */
    @Test
    void aStandingTermHasNoPlace() {
        assertTrue(Place.of(Cott.derive("x").to()).isEmpty());
        assertTrue(Place.of(Cott.derive("x^2+1").to()).isEmpty());
        assertTrue(Place.of(Cott.derive("1+0").to()).isEmpty());
        assertTrue(Place.of(Cott.derive("π").to()).isEmpty());
    }
}
