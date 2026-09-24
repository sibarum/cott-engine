package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;
import sibarum.cott.projection.Projections;
import sibarum.cott.projection.Rational;
import sibarum.cott.traction.T;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorTest {

    private static Result.Value value(Calculator c, String line) {
        return assertInstanceOf(Result.Value.class, c.enter(line), line);
    }

    @Test
    void divisionByZero() {
        Calculator c = new Calculator();
        assertEquals("ω", c.enter("1/0").text());
        assertEquals("0ω", c.enter("0/0").text());
        assertEquals("ω", c.enter("ω + 1").text());
        assertEquals("0ω", c.enter("0ω").text());
        assertEquals("0ω", c.enter("0·ω").text());
    }

    @Test
    void theGroundTruthHasNoQuotient() {
        Calculator c = new Calculator();
        Result.Value half = value(c, "1/2 + 1/2");
        assertEquals(T.of(4, 4), half.flat());
        assertEquals("4/4", half.text());
        assertEquals("2/-4", c.enter("2/(-4)").text());
    }

    @Test
    void projectionsAreReadingsOfTheSameValue() {
        Result.Value half = value(new Calculator(), "1/2 + 1/2");
        assertEquals(T.ONE, half.read(Projections.RAY));
        assertEquals(T.ONE, half.read(Projections.RATIO));
        assertEquals(Optional.of(Rational.of(1, 1)), half.read(Projections.CLASSICAL));
        assertEquals(T.of(4, 4), half.flat(), "a reading leaves the value as it was");

        Result.Value neg = value(new Calculator(), "2/(-4)");
        assertEquals("1/-2", neg.readings().get("ray"));
        assertEquals("-1/2", neg.readings().get("ratio"));
        assertEquals("-1/2", neg.readings().get("classical"));
        assertEquals(List.of("ray", "ratio", "classical", "angle", "point"), List.copyOf(neg.readings().keySet()));
    }

    @Test
    void theExampleFromTheBrief() {
        Calculator c = new Calculator();
        c.enter("x = 1");
        Result.Value r = value(c, "ω(2x)-(0(x+1)+0^2(2x-1))/2");
        assertEquals(T.of(4, 0), r.flat());
        assertEquals("4/0", r.text());
        assertEquals("ω", r.readings().get("ray"));
        assertEquals("ω", r.readings().get("ratio"));
        assertEquals("undefined", r.readings().get("classical"));
        assertEquals("90°", r.readings().get("angle"));
        assertEquals("4i", r.readings().get("point"));
    }

    @Test
    void aFreeVariableLeavesTheExpressionAsItIs() {
        Calculator c = new Calculator();
        Result r = c.enter("2x^2+4x+2");
        assertInstanceOf(Result.Unevaluated.class, r);
        assertEquals("2x^2 + 4x + 2", r.text());
        c.enter("y = 3");
        assertEquals("x·3", c.enter("xy").text());
    }

    @Test
    void variablesAndFunctionsSubstitute() {
        Calculator c = new Calculator();
        c.enter("f(x) = 2x^2+4x+2");
        assertEquals("32", c.enter("f(3)").text());
        c.enter("a = 3");
        c.enter("g(x, y) = x - y");
        assertEquals("1", c.enter("g(a, 2)").text());
        assertEquals("t - 3", c.enter("g(t, a)").text());
    }

    @Test
    void aParameterShadowsAVariable() {
        Calculator c = new Calculator();
        c.enter("x = 100");
        c.enter("f(x) = x + 1");
        assertEquals("2", c.enter("f(1)").text());
    }

    @Test
    void cyclesAreRefused() {
        Calculator c = new Calculator();
        c.enter("x = x + 1");
        assertThrows(CalculatorException.class, () -> c.enter("x"));
        c.enter("f(n) = f(n)");
        assertThrows(CalculatorException.class, () -> c.enter("f(1)"));
    }

    @Test
    void exponentsAreWholeNumbersForNow() {
        Calculator c = new Calculator();
        assertEquals("8", c.enter("2^3").text());
        assertEquals("1", c.enter("0^0").text());
        c.enter("n = 2");
        assertEquals("9", c.enter("3^n").text());
        assertThrows(CalculatorException.class, () -> c.enter("2^(1+1)"));
        assertThrows(CalculatorException.class, () -> c.enter("2^-1"));
    }
}
