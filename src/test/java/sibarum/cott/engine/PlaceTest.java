package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.Cott;
import sibarum.cott.engine.operation.binary.AdditionOperationExpr;
import sibarum.cott.engine.operation.unary.NegationOperationExpr;
import sibarum.cott.engine.projection.Place;
import sibarum.cott.engine.rational.expr.RationalLiteral;
import sibarum.cott.engine.traction.expr.AdditiveTractionLiteral;
import sibarum.cott.engine.traction.expr.TractionLiteral;

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
        assertTrue(Place.of(Cott.derive("π").to()).isEmpty());
    }

    /**
     * A sum at unlike orders is a point: the additive pair, whose coordinates are the two axes already here.
     * The real part is the first coordinate whichever side of the sum it was written on.
     */
    @Test
    void aSumAtUnlikeOrdersIsTheAdditivePair() {
        assertEquals(at(1, 1, 2, 1), place("0^2+1").coordinates());
        assertEquals(at(1, 1, 2, 1), place("1+0^2").coordinates());
        assertEquals(at(1, 1, 1, 1), place("1+0").coordinates());
        assertEquals(at(1, 1, -1, 1), place("1+w").coordinates());
        assertEquals(at(-1, 1, 1, 1), place("0-1").coordinates());
        assertEquals(at(3, 2, 3, 1), place("0^3+3÷2").coordinates());
    }

    /**
     * The coordinates a sum takes were unreachable multiplicatively, since a real part of exactly one
     * collapses by {@code x·1 = x}. So the additive reading is an addition to the chart and not an overwrite.
     */
    @Test
    void theAdditivePairTakesCoordinatesTheProductCannotReach() {
        assertEquals(at(0, 1, 2, 1), place("1·0^2").coordinates());
        assertFalse(place("1·0^2").coordinates().equals(place("1+0^2").coordinates()));
    }

    /** Further out the two joins do collide, and this places them together rather than deciding between them. */
    @Test
    void aProductAndASumOfTheSameTwoNumbersLandTogether() {
        assertEquals(at(2, 1, 2, 1), place("2·0^2").coordinates());
        assertEquals(at(2, 1, 2, 1), place("2+0^2").coordinates());
    }

    /** A sum in an exponent is read the same way, which is what gives 0^(1+ω) a place. */
    @Test
    void aSumInAnExponentIsTheSameReading() {
        assertEquals(at(0, 1, 1, 1, -1, 1), place("0^(1+w)").coordinates());
        assertEquals(3, place("0^(1+w)").dimension());
    }

    /**
     * A sum wanting three numbers is placed on three, in the order the nesting already uses: the real part,
     * the traction part's multiplicity, then its exponent.
     */
    @Test
    void aSumWantingAThirdNumberIsPlacedOnThree() {
        // 1 + (1÷2)·0 -- a real part, a multiplicity and an exponent.
        assertEquals(at(1, 1, 1, 2, 1, 1), place("1+0÷2").coordinates());
        // -0 is (-1)·0, so the traction part is not bare and carries a real part of its own.
        assertEquals(at(1, 1, -1, 1, 1, 1), place("1-0").coordinates());
        // The multiplicity is a coordinate like any other, whatever it is.
        assertEquals(at(1, 1, 2, 1, 1, 1), place("1+2·0").coordinates());
        assertEquals(at(1, 1, -1, 1, 2, 1), place("1-0^2").coordinates());
        assertEquals(3, place("1-0").dimension());
    }

    /**
     * A sum of two unlike traction parts still has no place: two exponents and no real part to hang the
     * chain from, which wants four numbers as two pairs rather than as one chain.
     */
    @Test
    void aSumOfTwoTractionPartsHasNoPlace() {
        assertTrue(Place.of(Cott.derive("0^2+0^3").to()).isEmpty());
        assertTrue(Place.of(Cott.derive("2·0-w").to()).isEmpty());
    }

    /**
     * A negated additive pair declines, in either spelling.
     *
     * <p>The sign rule for a pair is the multiplicative one -- {@code -(n·0^t)} is {@code (-n)·0^t}, which
     * turns the real part and leaves the traction part alone. Under the other join it is not that:
     * {@code -(n + 0^t)} negates both parts, and that inverse is not one of the things that survived the
     * change of join. So the node declines the way the standing sum already did; placing it by the
     * multiplicative rule put {@code -(1 + 0)} exactly where {@code (-1) + 0} is.
     */
    @Test
    void aNegatedAdditivePairIsNotPlacedByTheMultiplicativeSignRule() {
        AdditiveTractionLiteral onePlusZero =
                new AdditiveTractionLiteral(RationalLiteral.ONE, RationalLiteral.ONE);
        assertEquals(at(1, 1, 1, 1), Place.of(onePlusZero).orElseThrow().coordinates());
        assertTrue(Place.of(new NegationOperationExpr(onePlusZero)).isEmpty());
        // the same value written as a standing sum, which already declined
        assertTrue(Place.of(new NegationOperationExpr(
                new AdditionOperationExpr(RationalLiteral.ONE, TractionLiteral.of(RationalLiteral.ONE))))
                .isEmpty());
        // and the multiplicative pair is untouched: -(n·0^t) still turns its real part
        assertEquals(at(-1, 1, 1, 1),
                Place.of(new NegationOperationExpr(
                        new TractionLiteral(RationalLiteral.ONE, RationalLiteral.ONE)))
                        .orElseThrow().coordinates());
    }
}
