package sibarum.cott.algebra;

import org.junit.jupiter.api.Test;
import sibarum.cott.calculator.CalculatorException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelTest {

    private static BigInteger i(long n) {
        return BigInteger.valueOf(n);
    }

    private static BigDecimal d(String s) {
        return new BigDecimal(s);
    }

    // ---- Integer ----

    @Test
    void integersAreExactAndNeverDivide() {
        IntegerLevel z = new IntegerLevel(SizeLimit.MEDIUM.integerBits());
        assertEquals(i(7), z.add(i(3), i(4)));
        assertEquals(i(-1), z.sub(i(3), i(4)));
        assertEquals(i(12), z.mul(i(3), i(4)));
        assertEquals(i(-3), z.neg(i(3)));
        assertEquals(Optional.empty(), z.divide(i(6), i(3)), "6/3 is the pair Q(6,3), not 2");
        assertEquals(Optional.empty(), z.ofDecimal(d("0.5")));
        assertEquals("-12", z.write(i(-12)));
    }

    @Test
    void integerPowersAreWholeAndNonNegative() {
        IntegerLevel z = new IntegerLevel(SizeLimit.MEDIUM.integerBits());
        assertEquals(Optional.of(i(1024)), z.pow(i(2), i(10)));
        assertEquals(Optional.of(i(1)), z.pow(i(0), i(0)));
        assertEquals(Optional.of(i(-1)), z.pow(i(-1), i(3)));
        assertEquals(Optional.of(i(1)), z.pow(i(-1), i(1L << 40)));
        assertEquals(Optional.empty(), z.pow(i(2), i(-1)));
    }

    @Test
    void anIntegerPastTheLimitIsRefusedNotRounded() {
        IntegerLevel z = new IntegerLevel(SizeLimit.SMALL.integerBits());
        BigInteger top = BigInteger.ONE.shiftLeft(63);
        assertEquals(64, z.ofInteger(top).bitLength());
        assertThrows(CalculatorException.class, () -> z.add(top, top));
        assertThrows(CalculatorException.class, () -> z.mul(top, i(2)));
        assertThrows(CalculatorException.class, () -> z.pow(i(2), i(64)));
        assertThrows(CalculatorException.class, () -> z.pow(i(3), i(Long.MAX_VALUE)));
        assertEquals(Optional.of(top), z.pow(i(2), i(63)));
    }

    // ---- Decimal ----

    @Test
    void decimalsAreExactWithinTheLimit() {
        DecimalLevel x = new DecimalLevel(SizeLimit.MEDIUM.decimalDigits());
        assertEquals(0, d("0.3").compareTo(x.add(d("0.1"), d("0.2"))));
        assertEquals(Optional.of(d("0.25")), x.divide(d("1"), d("4")));
        assertEquals(Optional.of(d("0.001")), x.pow(d("10"), d("-3")));
        assertEquals("0.3", x.write(x.add(d("0.1"), d("0.2"))));
        assertFalse(x.rounded());
    }

    @Test
    void aDecimalPastTheLimitIsRoundedAndSaysSo() {
        DecimalLevel x = new DecimalLevel(SizeLimit.SMALL.decimalDigits());
        assertEquals("0.3333333333333333", x.write(x.divide(d("1"), d("3")).orElseThrow()));
        assertTrue(x.rounded());

        DecimalLevel y = new DecimalLevel(4);
        assertEquals("1235", y.write(y.mul(d("1234"), d("1.0005"))));
        assertTrue(y.rounded());

        DecimalLevel exact = new DecimalLevel(4);
        exact.mul(d("1000"), d("1000"));
        assertFalse(exact.rounded(), "trailing zeros past the limit change no value");
    }

    @Test
    void aDecimalDividedByZeroMeetsQ() {
        DecimalLevel x = new DecimalLevel(SizeLimit.MEDIUM.decimalDigits());
        assertEquals(Optional.empty(), x.divide(d("1"), d("0")));
        assertEquals(Optional.empty(), x.pow(d("0"), d("-1")));
        assertEquals(Optional.empty(), x.pow(d("2"), d("0.5")));
    }

    @Test
    void decimalsAreWrittenAsDoublesAre() {
        DecimalLevel x = new DecimalLevel(SizeLimit.MEDIUM.decimalDigits());
        assertEquals("1.5e-7", x.write(d("0.00000015")));
        assertEquals("1e21", x.write(d("1E+21")));
        assertEquals("-2.5", x.write(d("-2.5")));
        assertEquals("0", x.write(BigDecimal.ZERO));
    }

    // ---- IEEE ----

    @Test
    void ieeeIsTheDouble() {
        IeeeLevel f = new IeeeLevel();
        assertEquals(0.1 + 0.2, f.add(0.1, 0.2));
        assertEquals("0.6", f.write(f.divide(3.0, 5.0).orElseThrow()));
        assertEquals("0.6000000000000001", f.write(f.mul(3.0, f.divide(1.0, 5.0).orElseThrow())));
        assertEquals("∞", f.write(f.divide(1.0, 0.0).orElseThrow()));
        assertEquals("-0", f.write(f.neg(0.0)));
        assertEquals(StrictMath.pow(2, 0.5), f.pow(2.0, 0.5).orElseThrow());
    }

    // ---- the types ----

    @Test
    void eachNumberTypeGivesItsLevel() {
        assertInstanceOf(IntegerLevel.class, NumberType.INTEGER.level(SizeLimit.MEDIUM));
        assertInstanceOf(DecimalLevel.class, NumberType.DECIMAL.level(SizeLimit.MEDIUM));
        assertInstanceOf(IeeeLevel.class, NumberType.IEEE.level(SizeLimit.SMALL));
        assertEquals("IEEE 64-bit", NumberType.IEEE.label());
    }
}
