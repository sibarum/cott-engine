package sibarum.cott.notation;

import org.junit.jupiter.api.Test;
import sibarum.cott.notation.Expr.Add;
import sibarum.cott.notation.Expr.Call;
import sibarum.cott.notation.Expr.Div;
import sibarum.cott.notation.Expr.Mul;
import sibarum.cott.notation.Expr.Neg;
import sibarum.cott.notation.Expr.Num;
import sibarum.cott.notation.Expr.Omega;
import sibarum.cott.notation.Expr.Pow;
import sibarum.cott.notation.Expr.Sub;
import sibarum.cott.notation.Expr.Var;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {

    private static final Parser P = new Parser();

    private static Expr parse(String s) {
        return P.expression(s);
    }

    private static Num n(long v) {
        return Num.of(v);
    }

    private static Var v(String name) {
        return new Var(name);
    }

    private static Mul jux(Expr a, Expr b) {
        return new Mul(a, b, true);
    }

    @Test
    void aPolynomial() {
        assertEquals(new Add(new Add(jux(n(2), new Pow(v("x"), n(2))), jux(n(4), v("x"))), n(2)),
                parse("2x^2+4x+2"));
    }

    @Test
    void omegaAndZeroMultiplyByJuxtaposition() {
        Expr twoX = jux(n(2), v("x"));
        Expr expected = new Sub(
                jux(new Omega(), twoX),
                new Div(new Add(
                        jux(n(0), new Add(v("x"), n(1))),
                        jux(new Pow(n(0), n(2)), new Sub(twoX, n(1)))), n(2)));
        assertEquals(expected, parse("ω(2x)-(0(x+1)+0^2(2x-1))/2"));
    }

    @Test
    void precedence() {
        assertEquals(new Neg(new Pow(n(2), n(2))), parse("-2^2"));
        assertEquals(new Pow(n(2), new Pow(n(3), n(2))), parse("2^3^2"));
        assertEquals(jux(new Div(n(1), n(2)), v("x")), parse("1/2x"));
        assertEquals(new Sub(new Sub(n(1), n(2)), n(3)), parse("1-2-3"));
        assertEquals(new Pow(n(2), new Neg(n(1))), parse("2^-1"));
        assertEquals(new Mul(n(2), new Neg(n(3)), false), parse("2·-3"));
    }

    @Test
    void operatorSpellings() {
        assertEquals(parse("6*2/3-1"), parse("6×2÷3−1"));
        assertEquals(parse("6*2"), parse("6·2"));
        assertEquals(parse("x^2 + y^10"), parse("x² + y¹⁰"));
    }

    @Test
    void lettersSplitUnlessKnown() {
        assertEquals(jux(jux(n(2), v("x")), v("y")), parse("2xy"));
        assertEquals(jux(n(2), v("xy")), new Parser(Set.of(), Set.of("xy")).expression("2xy"));
        assertEquals(jux(v("x"), new Omega()), parse("xω"));
    }

    @Test
    void aCallOnlyForAFunction() {
        assertEquals(jux(v("f"), v("x")), parse("f(x)"));
        Parser withF = new Parser(Set.of("f"), Set.of());
        assertEquals(new Call("f", List.of(v("x"))), withF.expression("f(x)"));
        assertEquals(jux(n(2), new Call("f", List.of(n(1), n(2)))), withF.expression("2f(1, 2)"));
        assertThrows(SyntaxException.class, () -> withF.expression("f + 1"));
    }

    @Test
    void definitions() {
        assertEquals(new Statement.Assign("x", n(3)), P.statement("x = 3"));
        Statement.Define d = assertInstanceOf(Statement.Define.class, P.statement("f(x, y) = xy + 1"));
        assertEquals(List.of("x", "y"), d.params());
        assertEquals(new Add(jux(v("x"), v("y")), n(1)), d.body());
        Statement.Define area = assertInstanceOf(Statement.Define.class, P.statement("area(r) = 3r^2"));
        assertEquals(jux(n(3), new Pow(v("r"), n(2))), area.body());
    }

    @Test
    void errors() {
        for (String bad : List.of("", "2+", "(1", "1)", "2 = x", "f(x = 1", "0.5", "2 $ 3", "f(x, x) = x"))
            assertThrows(SyntaxException.class, () -> P.statement(bad), bad);
    }

    @Test
    void printingReadsBackAsTheSameTree() {
        for (String s : List.of("2x^2+4x+2", "ω(2x)-(0(x+1)+0^2(2x-1))/2", "-2^2", "(-2)^2", "2^3^2",
                "(2^3)^2", "1/2x", "1/(2x)", "1-(2-3)", "2·-3", "x(y+1)", "-(2x)", "2^-1", "(1+2)(3+4)",
                "2·3", "x^2y"))
            assertEquals(parse(s), parse(Printer.print(parse(s))), s + " printed as " + Printer.print(parse(s)));
    }
}
