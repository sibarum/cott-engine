package sibarum.cott.algebra;

import org.junit.jupiter.api.Test;
import sibarum.cott.calculator.CalculatorException;
import sibarum.cott.notation.Parser;
import sibarum.cott.projection.Rational;
import sibarum.cott.traction.Proves;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code L(s, t)}: a value's logarithm as a scale ratio and a turn ratio. {@code +} adds both (the product of the
 * values), a rational times it is the value's power, {@code 0^(p/q)} follows {@code zeroPow}, and {@code log_b} divides
 * by the base's units.
 */
class LogPairTest {

    private static Object value(NumberType type, String line) {
        return Evaluator.evaluate(new Parser().expression(line), type, SizeLimit.MEDIUM, Form.SUM_OF_PRODUCTS,
                Cancellation.RESIDUAL).value();
    }

    private static String text(String line) {
        return Evaluator.evaluate(new Parser().expression(line), NumberType.INTEGER, SizeLimit.MEDIUM,
                Form.SUM_OF_PRODUCTS, Cancellation.RESIDUAL).text();
    }

    private static Map<String, String> readings(String line) {
        Map<String, String> out = new LinkedHashMap<>();
        Readings.of(value(NumberType.INTEGER, line)).forEach(r -> out.put(r.name(), r.text()));
        return out;
    }

    // ---- 0^(p/q) ----

    @Test
    @Proves({"T.Unquotiented.zeroPow_one_one", "T.Unquotiented.zeroPow"})
    void aFractionalPowerOfZeroIsAZeroMagnitudeTurnedByItsExponent() {
        assertEquals("L(Q(-1, 0), Q(1, 2))", text("0^(1/2)"));
        assertEquals("L(Q(-1, 0), Q(1, 1))", text("0^(1/1)"), "0^(1/1) is the rotation by one turn, not 0");
        assertEquals("L(Q(-1, 0), Q(2, 4))", text("0^(2/4)"), "the exponent's ratio is kept unreduced");
        Map<String, String> half = readings("0^(1/2)");
        assertEquals("0", half.get("magnitude"));
        assertEquals("0, turned 1/2", half.get("value"));
        assertEquals(null, half.get("lowest terms"), "reducing -1 : 0 would make it 1 : 0, the magnitude ω");
    }

    @Test
    @Proves({"T.Unquotiented.zeroPow_one_zero", "T.Unquotiented.zeroPow_zero_one", "T.Unquotiented.zeroPow_axis_iff",
            "T.Unquotiented.Lr_zero_one", "T.Unquotiented.Lr_zero_negOne"})
    void onTheAxesZerosPowerIsOneOrMinusOne() {
        assertEquals("-1", readings("0^(1/0)").get("value"));
        assertEquals("-1", readings("0^ω").get("value"), "ω is the ratio 1 : 0");
        assertEquals("1", readings("0^(0/1)").get("value"));
        assertEquals("-1", readings("0^(0/-1)").get("value"));
        assertEquals("1", readings("0^(-1/0)").get("value"));
    }

    @Test
    @Proves("T.Unquotiented.zeroPow_zero_zero")
    void zeroToTheZeroOverZeroHasNoRule() {
        assertThrows(CalculatorException.class, () -> text("0^(0/0)"));
    }

    @Test
    void aWholePowerOfZeroIsStillZsOwn() {
        assertEquals("Z(1, 2)", text("0^2"));
        assertEquals("Z(Q(1, 1), -3)", text("0^-3"), "1 over 0^3, as before");
    }

    // ---- powers of the units, and the product ----

    @Test
    @Proves({"T.Unquotiented.turn_zero_negOne", "T.Unquotiented.turn_one_zero"})
    void aFractionalPowerOfAUnitIsAFractionOfItsTurn() {
        assertEquals("L(Q(0, 2), Q(1, 4))", text("(-1)^(1/2)"));
        assertEquals("i", readings("(-1)^(1/2)").get("value"));
        assertEquals("1/8", readings("i^(1/2)").get("turn"));
        assertEquals("i", readings("(-1)^(2/4)").get("value"));
        assertEquals("-i", readings("(-1)^(3/2)").get("value"));
        assertEquals("L(Q(0, 2), Q(0, 2))", text("1^(1/2)"), "the identity 1 has no turn");
    }

    /** The complex number a plot reads off a value: an {@code L} too, and nothing for a zero magnitude turned. */
    @Test
    void aLogPairIsAComplexNumber() {
        double[] eighth = Readings.complex(value(NumberType.INTEGER, "i^(1/2)")).orElseThrow();
        assertEquals(Math.sqrt(0.5), eighth[0], 1e-15);
        assertEquals(Math.sqrt(0.5), eighth[1], 1e-15);
        assertArrayEquals(new double[]{0, -1}, Readings.complex(value(NumberType.INTEGER, "(-1)^(3/2)")).orElseThrow(),
                "a whole quarter turn is exact");
        assertArrayEquals(new double[]{0, 1}, Readings.complex(value(NumberType.INTEGER, "i")).orElseThrow());
        assertTrue(Readings.complex(value(NumberType.INTEGER, "0^(1/2)")).isEmpty(), "0 turned is no complex number");
        assertTrue(Readings.complex(value(NumberType.INTEGER, "0")).isEmpty(), "nor is a written 0, Z(1, 1)");
    }

    @Test
    @Proves({"T.Unquotiented.Lmul", "T.Unquotiented.Lr_negOne_sq"})
    void addingLogPairsMultipliesTheirValues() {
        assertEquals("-1", readings("(-1)^(1/2) + (-1)^(1/2)").get("value"), "i·i");
        assertEquals("L(Q(0, 4), Q(8, 16))", text("(-1)^(1/2) + (-1)^(1/2)"), "each coordinate a fraction +, unreduced");
        assertEquals("1", readings("(-1)^(1/2) - (-1)^(1/2)").get("value"));
        assertEquals("S(L(Q(0, 2), Q(1, 4)), 1)", text("(-1)^(1/2) + 1"), "an L and anything else is their S");
    }

    @Test
    void aRationalTimesALogPairIsAPower() {
        assertEquals("L(Q(0, 4), Q(1, 8))", text("(1/2)·(-1)^(1/2)"), "the square root of i");
        assertEquals("L(Q(0, 2), Q(2, 4))", text("2·(-1)^(1/2)"), "i squared");
        assertEquals("L(Q(0, 4), Q(1, 8))", text("(-1)^(1/2) / 2"));
        assertEquals("P(L(Q(0, 2), Q(1, 4)), L(Q(0, 2), Q(1, 4)))", text("(-1)^(1/2) · (-1)^(1/2)"),
                "no operation multiplies two logarithms");
    }

    // ---- log_b by units ----

    @Test
    @Proves({"T.Unquotiented.logBase", "T.Unquotiented.rdiv", "T.Unquotiented.logBase_one",
            "T.Unquotiented.logBase_negOne", "T.Unquotiented.logBase_i", "T.Unquotiented.logBase_zero"})
    void logBaseDividesByTheBasesUnits() {
        Logs.Ratio scale = Logs.Ratio.of(0, 1), turn = new Logs.Ratio(Rational.of(1, 1), Rational.of(8, 1));
        assertEquals("1 : 8", Logs.logBase(Logs.units(Base.ONE).orElseThrow(), scale, turn).turn().toString());
        assertEquals("1 : 4", Logs.logBase(Logs.units(Base.NEG_ONE).orElseThrow(), scale, turn).turn().toString());
        assertEquals("1 : 2", Logs.logBase(Logs.units(Base.I).orElseThrow(), scale, turn).turn().toString());
        Logs.Units zero = Logs.logBase(Logs.units(Base.ZERO).orElseThrow(), new Logs.Ratio(Rational.of(3, 1),
                Rational.of(1, 1)), turn);
        assertEquals("0 : -1", zero.scale().toString(), "scale 0 : −1 for every value, as logBase_zero has it");
        assertEquals("0 : 0", zero.turn().toString());
        assertEquals("scale 0 : 0, turn 1 : 2", readings("i^(1/2)").get("log_i"), "an eighth turn is half a quarter turn");
    }

    @Test
    void overDecimalsAnExponentIsItsOwnRatio() {
        Object half = value(NumberType.DECIMAL, "0^0.5");
        assertEquals("L(Q(-1, 0), Q(0.5, 1))", new Evaluator(NumberType.DECIMAL, SizeLimit.MEDIUM, Form.SUM_OF_PRODUCTS,
                Cancellation.RESIDUAL, null).write(half));
    }
}
