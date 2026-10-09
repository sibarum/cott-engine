package sibarum.cott.algebra;

import org.junit.jupiter.api.Test;
import sibarum.cott.calculator.CalculatorException;
import sibarum.cott.notation.Parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sibarum.cott.algebra.Form.PRODUCT_OF_SUMS;
import static sibarum.cott.algebra.Form.SUM_OF_PRODUCTS;

class EvaluatorTest {

    private static Evaluator.Result in(NumberType type, Form form, String line) {
        return Evaluator.evaluate(new Parser().expression(line), type, SizeLimit.MEDIUM, form);
    }

    private static String integers(String line) {
        return in(NumberType.INTEGER, SUM_OF_PRODUCTS, line).text();
    }

    private static String factored(String line) {
        return in(NumberType.INTEGER, PRODUCT_OF_SUMS, line).text();
    }

    @Test
    void numbersAreTypelessUntilTheyMeetAPair() {
        assertEquals("7", integers("3 + 4"));
        assertEquals("Q(7, 2)", integers("3 + Q(1, 2)"));
        assertEquals("C(3, 4)", integers("C(1, 2) + C(2, 2)"));
        assertEquals("C(1, 5)", integers("C(1, 2) + 3"));
    }

    @Test
    void integersHaveNoDivisionSoItIsQ() {
        assertEquals("Q(6, 3)", integers("6/3"));
        assertEquals("Q(1, 8)", integers("2^-3"));
        assertEquals("Q(1, 0)", integers("1/0"));
    }

    @Test
    void namedValuesAreExactPairs() {
        assertEquals("Q(1, 0)", integers("ω"));
        assertEquals("Q(0, -1)", integers("_0"));
        assertEquals("Q(-1, -1)", integers("-_1"));
        assertEquals("Q(0, 0)", integers("0ω"));
        assertEquals("C(1, 0)", integers("i"));
        assertEquals("C(0, -1)", integers("i·i"));
        assertEquals("C(3, 4)", integers("4 + 3i"));
    }

    @Test
    void aSumMeetsAProductAsTheFormSays() {
        // ½ + (4 + 3i) = 9/2 + 3i: the Q enters C whole, beside a typeless 0, and nothing else is embedded
        assertEquals("C(3, Q(9, 2))", integers("1/2 + (4 + 3i)"));
        assertEquals("C(3, Q(9, 2))", integers("Q(1, 2) + C(3, 4)"));
        assertEquals("C(3, Q(9, 2))", integers("C(3, 4) + Q(1, 2)"));
        assertEquals("Q(C(6, 9), 2)", factored("Q(1, 2) + C(3, 4)"));
        assertEquals("C(1, Q(1, 0))", integers("C(1, 2) + ω"));
    }

    @Test
    void twoSumsOrTwoProductsMeetInTheLeftOne() {
        assertEquals("C(1, D(3, 6))", integers("C(1, 2) + D(3, 4)"));
        assertEquals("D(3, C(1, 6))", integers("D(3, 4) + C(1, 2)"));
    }

    @Test
    void anOperationAnAlgebraLacksIsThePairWhoseReadingItIs() {
        assertEquals("S(P(2, 3), P(1, 5))", integers("P(2, 3) + P(1, 5)"));
        assertEquals("D(P(1, 5), P(2, 3))", integers("P(2, 3) - P(1, 5)"));
        assertEquals("Q(C(1, 2), C(3, 4))", factored("C(1, 2) / C(3, 4)"));
    }

    @Test
    void aSumDividedHasQCoordinates() {
        // (2 + i)/(4 + 3i) = (11 − 2i)/25
        assertEquals("C(Q(-50, 625), Q(275, 625))", integers("C(1, 2) / C(3, 4)"));
        assertEquals("C(Q(-5, 25), Q(10, 25))", integers("C(1, 2)^-1"));
    }

    @Test
    void powers() {
        assertEquals("C(4, 3)", integers("C(1, 2)^2"));
        assertEquals("Q(27, 8)", integers("Q(2, 3)^-3"));
        assertEquals("C(0, 1)", integers("C(1, 2)^0"));
        assertThrows(CalculatorException.class, () -> integers("2^Q(1, 2)"));
        assertEquals("1.4142135623730951", in(NumberType.IEEE, SUM_OF_PRODUCTS, "2^0.5").text());
    }

    @Test
    void whatIsWrittenIsKept() {
        assertEquals("C(Q(0, 1), 2)", integers("C(Q(0, 1), 2)"));
        assertEquals("Q(6, 1)", integers("Q(6, 1)"));
    }

    @Test
    void decimalsCanDivideAndSayWhenTheyRounded() {
        Evaluator.Result third = in(NumberType.DECIMAL, SUM_OF_PRODUCTS, "1/3");
        assertEquals("0.3333333333333333333333333333333333", third.text());
        assertTrue(third.rounded());
        Evaluator.Result exact = in(NumberType.DECIMAL, SUM_OF_PRODUCTS, "C(1, 2) / C(3, 4)");
        assertEquals("C(-0.08, 0.44)", exact.text());
        assertFalse(exact.rounded());
        assertEquals("Q(0.5, 1)", in(NumberType.DECIMAL, SUM_OF_PRODUCTS, "Q(0.5, 1)").text());
        assertThrows(CalculatorException.class, () -> integers("0.5"));
    }

    @Test
    void ieeeKeepsItsOwnDivision() {
        assertEquals("∞", in(NumberType.IEEE, SUM_OF_PRODUCTS, "1/0").text());
        assertEquals("Q(1, 0)", in(NumberType.IEEE, SUM_OF_PRODUCTS, "Q(1, 0)").text());
    }
}
