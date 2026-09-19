package sibarum.cott.engine;

import org.junit.jupiter.api.Test;
import sibarum.cott.Bindings;
import sibarum.cott.Cott;
import sibarum.cott.engine.projection.Place;
import sibarum.cott.engine.projection.Projector;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Three axes, from the entries a user types. Written against those rather than against constructed carrier
 * nodes for the reason {@link PlaceTest} is: what a client reads is the point of the projection.
 */
class ProjectorTest {

    private static final double EXACT = 0.0;

    private static double[] at(String entry) {
        Optional<double[]> p = Projector.project(Cott.derive(entry).to());
        assertTrue(p.isPresent(), entry + " should project");
        return p.get();
    }

    /** The four units, each on three axes with the third unoccupied. */
    @Test
    void theFourUnits() {
        assertArrayEquals(new double[]{1, 0, 0}, at("1"), EXACT);
        assertArrayEquals(new double[]{-1, 0, 0}, at("-1"), EXACT);
        assertArrayEquals(new double[]{0, 1, 0}, at("0"), EXACT);
        assertArrayEquals(new double[]{0, -1, 0}, at("w"), EXACT);
    }

    /** Widening to three keeps the distinction two were there to make. */
    @Test
    void oneAndThePointZeroDoNotCollide() {
        assertEquals(3, at("1").length);
        assertEquals(3, at("0").length);
        assertFalse(java.util.Arrays.equals(at("1"), at("0")));
    }

    /** The appended axis is the absence marker, so a two-coordinate value is unmoved by gaining it. */
    @Test
    void theThirdAxisIsUnoccupiedRatherThanMissing() {
        assertArrayEquals(new double[]{2, 1, 0}, at("2·0"), EXACT);
        assertArrayEquals(new double[]{2, -1, 0}, at("2w"), EXACT);
        assertArrayEquals(new double[]{0, 2, 0}, at("0^2"), EXACT);
    }

    /** A traction in the exponent occupies it, and the earlier coordinates do not shift to make room. */
    @Test
    void aNestedTractionOccupiesTheThirdAxis() {
        assertArrayEquals(new double[]{0, 0.5, -1}, at("0^(w÷2)"), EXACT);
        assertArrayEquals(new double[]{0, 0, 2}, at("0^(0^2)"), EXACT);
    }

    /** Projected by value, not by spelling: the same point however it was reached. */
    @Test
    void answersProjectByTheirValue() {
        assertArrayEquals(at("1"), at("0·w"), EXACT);
        assertArrayEquals(at("0"), at("1-1"), EXACT);
        assertArrayEquals(at("w"), at("1÷0"), EXACT);
        assertArrayEquals(new double[]{4, 0, 0}, at("2+2"), EXACT);
    }

    /** A term that is not one point does not project, which is how a plot knows to draw it instead. */
    @Test
    void aStandingTermDoesNotProject() {
        assertTrue(Projector.project(Cott.derive("x").to()).isEmpty());
        assertTrue(Projector.project(Cott.derive("x^2+1").to()).isEmpty());
        // Two exponents and no real part: a sum that is one point, but not one this can chain.
        assertTrue(Projector.project(Cott.derive("0^2+0^3").to()).isEmpty());
    }

    /** A sum at unlike orders is a point and lands on the same three axes as any other. */
    @Test
    void aSumAtUnlikeOrdersProjects() {
        assertArrayEquals(new double[]{1, 2, 0}, at("0^2+1"), EXACT);
        assertArrayEquals(new double[]{1, 1, 0}, at("1+0"), EXACT);
        assertArrayEquals(new double[]{1, -1, 0}, at("1+w"), EXACT);
    }

    /**
     * The whole of 0^x + x÷2 that is a point, sampled. Every x lands, and the curve has no gap in it: at
     * x = 0 the real term becomes a multiple of the point zero, so the value wants a third number and gets
     * one -- the multiplicity goes on the axis the other samples leave unoccupied.
     */
    @Test
    void aSampledCurveLandsOnTheAdditivePair() {
        assertArrayEquals(new double[]{1, 2, 0}, sample("2"), EXACT);
        assertArrayEquals(new double[]{1.5, 3, 0}, sample("3"), EXACT);
        assertArrayEquals(new double[]{0.25, 0.5, 0}, sample("1÷2"), EXACT);
        assertArrayEquals(new double[]{0.5, 1, 0}, sample("1"), EXACT);
        assertArrayEquals(new double[]{-0.5, -1, 0}, sample("-1"), EXACT);
        assertArrayEquals(new double[]{1, 0.5, 1}, sample("0"), EXACT);
    }

    private static double[] sample(String x) {
        Optional<double[]> p = curveAt(x);
        assertTrue(p.isPresent(), "0^x+x÷2 should project at x = " + x);
        return p.get();
    }

    private static Optional<double[]> curveAt(String x) {
        return Projector.project(Cott.derive("0^x+x÷2", Bindings.EMPTY.define("x=" + x)).to());
    }

    /** Deeper than a volume declines rather than dropping an axis, and the place is still there to read. */
    @Test
    void deeperThanAVolumeDeclines() {
        assertTrue(Projector.project(Cott.derive("0^(0^(0^2))").to()).isEmpty());
        Optional<Place> place = Place.of(Cott.derive("0^(0^(0^2))").to());
        assertTrue(place.isPresent());
        assertEquals(4, place.get().dimension());
    }

    /** Unsimplified in is the same answer, since the projector settles what it is given. */
    @Test
    void anUnsettledExpressionIsSettledFirst() {
        assertArrayEquals(at("0"), Projector.project(Cott.derive("1-1").from()).orElseThrow(), EXACT);
    }
}
