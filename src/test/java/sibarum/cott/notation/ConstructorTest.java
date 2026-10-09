package sibarum.cott.notation;

import org.junit.jupiter.api.Test;
import sibarum.cott.notation.Expr.Add;
import sibarum.cott.notation.Expr.Construct;
import sibarum.cott.notation.Expr.Mul;
import sibarum.cott.notation.Expr.Named;
import sibarum.cott.notation.Expr.Neg;
import sibarum.cott.notation.Expr.Num;
import sibarum.cott.notation.Expr.Omega;
import sibarum.cott.notation.Expr.Var;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pairs written by their constructors, and the named values. */
class ConstructorTest {

    private static Expr parse(String s) {
        return new Parser().expression(s);
    }

    private static Num n(long v) {
        return Num.of(v);
    }

    @Test
    void aConstructorTakesTwoCoordinates() {
        assertEquals(new Construct("Q", n(1), n(2)), parse("Q(1, 2)"));
        assertEquals(new Construct("C", new Construct("Q", n(0), n(1)), new Construct("Q", n(1), n(2))),
                parse("C(Q(0,1), Q(1,2))"));
        assertEquals(new Construct("D", new Add(n(1), n(2)), new Neg(new Var("x"))), parse("D(1 + 2, -x)"));
        for (String algebra : Parser.CONSTRUCTORS)
            assertInstanceOf(Construct.class, parse(algebra + "(0, 1)"));
    }

    @Test
    void aConstructorIsAPrimary() {
        assertEquals(new Mul(n(2), new Construct("Q", n(1), n(2)), true), parse("2Q(1, 2)"));
        assertEquals(new Add(new Construct("Q", n(1), n(2)), n(3)), parse("Q(1, 2) + 3"));
        assertEquals(new Mul(new Var("x"), new Construct("C", n(1), n(0)), true), parse("xC(1, 0)"));
    }

    @Test
    void aConstructorNeedsBothCoordinates() {
        assertThrows(SyntaxException.class, () -> parse("Q"));
        assertThrows(SyntaxException.class, () -> parse("Q(1)"));
        assertThrows(SyntaxException.class, () -> parse("Q(1, 2, 3)"));
        assertThrows(SyntaxException.class, () -> parse("Q 1, 2"));
    }

    @Test
    void aConstructorCannotBeDefined() {
        assertThrows(SyntaxException.class, () -> new Parser().statement("Q = 3"));
        assertThrows(SyntaxException.class, () -> new Parser().statement("C(x) = x"));
        assertThrows(SyntaxException.class, () -> new Parser().statement("f(P) = P"));
        assertEquals(new Construct("Q", n(1), n(2)),
                new Parser(Set.of("Q"), Set.of("C")).expression("Q(1, 2)"), "a constructor is never a call");
    }

    @Test
    void namedValues() {
        assertEquals(new Named("_0"), parse("_0"));
        assertEquals(new Neg(new Named("_1")), parse("-_1"));
        assertEquals(new Mul(n(0), new Omega(), true), parse("0ω"));
        assertEquals(new Construct("Q", new Named("_1"), new Omega()), parse("Q(_1, ω)"));
        assertEquals(new Add(n(4), new Mul(n(3), new Named("i"), true)), parse("4 + 3i"));
        assertEquals(new Mul(new Var("x"), new Named("i"), true), parse("xi"));
        assertThrows(SyntaxException.class, () -> new Parser().statement("i = 2"));
        assertThrows(SyntaxException.class, () -> parse("_2"));
        assertThrows(SyntaxException.class, () -> parse("_"));
    }

    @Test
    void whatIsPrintedReadsBack() {
        for (String s : new String[]{"Q(1, 2)", "C(Q(0, 1), Q(1, 2))", "2Q(1, 2) + D(x, -1)", "-_1·_0", "Q(1, 2)^3"}) {
            Expr e = parse(s);
            assertEquals(s, Printer.print(e));
            assertEquals(e, parse(Printer.print(e)));
        }
    }
}
