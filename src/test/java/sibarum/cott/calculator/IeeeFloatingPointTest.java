package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IeeeFloatingPointTest {

    private static Calculator ieee() {
        return new Calculator(Arithmetic.IEEE_FLOATING_POINT);
    }

    private static Result.IeeeValue value(String line) {
        return assertInstanceOf(Result.IeeeValue.class, ieee().enter(line), line);
    }

    private static String text(String line) {
        return value(line).text();
    }

    @Test
    void theOperationsAreTheIeeeOperations() {
        assertEquals(0.1 + 0.2, value("0.1 + 0.2").value());
        assertEquals("0.30000000000000004", text("0.1 + 0.2"));
        assertEquals(1.0 / 3.0, value("1/3").value());
        assertEquals(StrictMath.pow(2, 0.5), value("2^0.5").value());
        assertEquals("0.5", text("2^-1"));
        assertEquals("-4", text("-2^2"));
    }

    @Test
    void divisionIsDivisionAndNotTimesTheReciprocal() {
        assertEquals("0.6", text("3/5"));
        assertEquals("0.6000000000000001", text("3·(1/5)"));
    }

    @Test
    void zerosInfinitiesAndNaN() {
        assertEquals("∞", text("1/0"));
        assertEquals("-∞", text("-1/0"));
        assertEquals("-∞", text("1/(-0)"));
        assertEquals("NaN", text("0/0"));
        assertEquals("NaN", text("1/0 - 1/0"));
        assertEquals("-0", text("-0"));
        assertEquals("0", text("-0 + 0"));
        assertEquals("∞", text("10^309"));
    }

    @Test
    void aLiteralIsRoundedOnceToTheNearestDouble() {
        assertEquals("9007199254740992", text("9007199254740993"));
        assertEquals(0.1, value("0.1").value());
    }

    @Test
    void writtenWithTheFewestDigitsThatReadBack() {
        assertEquals("4", text("2+2"));
        assertEquals("100000000000000000000", text("10^20"));
        assertEquals("1e21", text("10^21"));
        assertEquals("0.000001", text("10^-6"));
        assertEquals("1e-7", text("10^-7"));
        assertEquals("-1.5e-7", text("-1.5/10^7"));
        assertEquals("1.7976931348623157e308", text("2^1023·(2 - 2^-52)"));
        assertEquals("5e-324", text("2^-1074"));
    }

    @Test
    void whatIsWrittenReadsBackAsTheSameDouble() {
        java.util.Random random = new java.util.Random(754);
        for (int i = 0; i < 200_000; i++) {
            double x = Double.longBitsToDouble(random.nextLong());
            if (!Double.isFinite(x)) continue;
            String written = IeeeFloatingPoint.display(x);
            assertEquals(x, Double.parseDouble(written), written);
            assertEquals(x, new java.math.BigDecimal(IeeeFloatingPoint.exact(x)).doubleValue(), written);
            int digits = new java.math.BigDecimal(written.replace('e', 'E')).stripTrailingZeros().precision();
            int javaDigits = new java.math.BigDecimal(Double.toString(x)).stripTrailingZeros().precision();
            assertTrue(digits <= javaDigits, written + " vs " + x);
        }
    }

    @Test
    void readingsAreTheExactValueTheHexAndTheBits() {
        Result.IeeeValue tenth = value("0.1");
        assertEquals("0.1000000000000000055511151231257827021181583404541015625", tenth.readings().get("exact"));
        assertEquals("0x1.999999999999ap-4", tenth.readings().get("hex"));
        assertEquals("0x3FB999999999999A", tenth.readings().get("bits"));
        assertEquals(List.of("exact", "hex", "bits"), List.copyOf(tenth.readings().keySet()));
        assertEquals("0x8000000000000000", value("-0").readings().get("bits"));
        assertEquals("∞", value("1/0").readings().get("exact"));
        assertEquals("0x7FF8000000000000", value("0/0").readings().get("bits"));
        assertEquals(Arithmetic.IEEE_FLOATING_POINT, tenth.arithmetic());
    }

    @Test
    void omegaIsNotAnIeeeValue() {
        assertThrows(CalculatorException.class, () -> ieee().enter("ω + 1"));
    }

    @Test
    void definitionsWorkAsInEveryArithmetic() {
        Calculator c = ieee();
        c.enter("x = 0.5");
        c.enter("f(a, b) = a^b + x");
        assertEquals("2.5", c.enter("f(2, 1)").text());
        assertInstanceOf(Result.Unevaluated.class, c.enter("y^2"));
    }
}
