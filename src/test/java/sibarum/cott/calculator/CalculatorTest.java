package sibarum.cott.calculator;

import org.junit.jupiter.api.Test;
import sibarum.cott.algebra.Form;
import sibarum.cott.algebra.NumberType;
import sibarum.cott.projection.Projections;
import sibarum.cott.projection.Rational;
import sibarum.cott.traction.T;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculatorTest {

    private static Result.Value value(Calculator c, String line) {
        return assertInstanceOf(Result.Value.class, c.enter(line), line);
    }

    @Test
    void divisionByZero() {
        Calculator c = new Calculator();
        assertEquals("Q(1, 0)", c.enter("1/0").text());
        assertEquals("Q(0, 0)", c.enter("0/0").text());
        assertEquals("Q(1, 0)", c.enter("ω + 1").text());
        assertEquals("Q(0, 0)", c.enter("0ω").text());
        assertEquals("Q(0, 0)", c.enter("0·ω").text());
        assertEquals("Q(3, 0)", c.enter("3/0").text());
        assertEquals("Q(3, 0)", c.enter("3ω").text());
        assertEquals("Q(-3, 0)", c.enter("-3/0").text());
    }

    @Test
    void theGroundTruthHasNoQuotient() {
        Calculator c = new Calculator();
        Result.Value half = value(c, "1/2 + 1/2");
        assertEquals(Optional.of(T.of(4, 4)), half.flat());
        assertEquals("Q(4, 4)", half.text());
        assertEquals("Q(2, -4)", c.enter("2/(-4)").text());
        assertEquals("Q(6, 3)", c.enter("6/3").text());
    }

    @Test
    void projectionsAreReadingsOfTheSameValue() {
        Result.Value half = value(new Calculator(), "1/2 + 1/2");
        assertEquals(T.ONE, half.read(Projections.RAY));
        assertEquals(T.ONE, half.read(Projections.RATIO));
        assertEquals(Optional.of(Rational.of(1, 1)), half.read(Projections.CLASSICAL));
        assertEquals(Optional.of(T.of(4, 4)), half.flat(), "a reading leaves the value as it was");

        Result.Value neg = value(new Calculator(), "2/(-4)");
        assertEquals("1/-2", neg.readings().get("ray"));
        assertEquals("-1/2", neg.readings().get("ratio"));
        assertEquals("-1/2", neg.readings().get("classical"));
        assertEquals(List.of("rung", "ray", "ratio", "classical", "angle", "point", "rotation", "lowest terms", "cott-lean"),
                List.copyOf(neg.readings().keySet()));
        assertEquals("exact", neg.readings().get("rung"));
        assertEquals("Q(-1, 2)", neg.readings().get("lowest terms"));
    }

    @Test
    void theExampleFromTheBrief() {
        Calculator c = new Calculator();
        c.enter("x = 1");
        Result.Value r = value(c, "ω(2x)-(0(x+1)+0^2(2x-1))/2");
        assertEquals(Optional.of(T.of(4, 0)), r.flat());
        assertEquals("Q(4, 0)", r.text());
        assertEquals("ω", r.readings().get("ray"));
        assertEquals("ω", r.readings().get("ratio"));
        assertEquals("undefined", r.readings().get("classical"));
        assertEquals("90°", r.readings().get("angle"));
        assertEquals("4i", r.readings().get("point"));
        assertEquals("-1", r.readings().get("rotation"));
    }

    @Test
    void pairsNestAndMix() {
        Calculator c = new Calculator();
        assertEquals("C(3, Q(9, 2))", c.enter("1/2 + (4 + 3i)").text());
        assertEquals("C(3, 4)", c.enter("4 + 3i").text());
        assertEquals("S(P(2, 3), P(1, 5))", c.enter("P(2, 3) + P(1, 5)").text());
        c.set(Form.PRODUCT_OF_SUMS);
        assertEquals("Q(C(6, 9), 2)", c.enter("1/2 + (4 + 3i)").text());
    }

    @Test
    void aNumberIsTypelessUntilItMeetsAPair() {
        Calculator c = new Calculator();
        Result.Value seven = value(c, "3 + 4");
        assertEquals("7", seven.text());
        assertEquals(Optional.empty(), seven.flat());
        assertEquals("Q(7, 2)", c.enter("3 + Q(1, 2)").text());
    }

    @Test
    void aDecimalSaysWhenItWasRounded() {
        Calculator c = new Calculator(NumberType.DECIMAL);
        assertEquals("up to error: rounded to 34 significant digits", value(c, "1/3").readings().get("rung"));
        assertEquals("exact", value(c, "1/4").readings().get("rung"));
        assertEquals("0.25", c.enter("1/4").text());
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
        c.enter("z = Q(1, 2)");
        assertEquals("C(1, Q(1, 2))", c.enter("z + i").text());
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
    void exponentsAreNumbers() {
        Calculator c = new Calculator();
        assertEquals("8", c.enter("2^3").text());
        assertEquals("1", c.enter("0^0").text());
        c.enter("n = 2");
        assertEquals("9", c.enter("3^n").text());
        assertEquals("4", c.enter("2^(1+1)").text());
        assertEquals("Q(1, 2)", c.enter("2^-1").text());
        assertThrows(CalculatorException.class, () -> c.enter("2^Q(1, 2)"));
        assertTrue(c.enter("Q(2, 3)^2").text().equals("Q(4, 9)"));
    }
}
