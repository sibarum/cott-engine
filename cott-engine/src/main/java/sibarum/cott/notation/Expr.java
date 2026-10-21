package sibarum.cott.notation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/** An expression in the universal notation, as written: nothing here knows what a value is. */
public sealed interface Expr {

    /** A natural-number literal. */
    record Num(BigInteger value) implements Expr {
        public static Num of(long n) {
            return new Num(BigInteger.valueOf(n));
        }
    }

    /**
     * A decimal literal, {@code 0.5}, kept as written: {@code 0.50} keeps its scale. Whether it is a value
     * is up to the arithmetic that evaluates it.
     */
    record Decimal(BigDecimal value) implements Expr {}

    /** {@code ω}. */
    record Omega() implements Expr {}

    /** A named value other than {@code ω}: {@code _0}, {@code _1} or {@code i}. */
    record Named(String name) implements Expr {}

    /**
     * A pair written by its constructor, {@code Q(1, 2)}: the traction algebra by its letter, and the two
     * coordinates, each any expression.
     */
    record Construct(String algebra, Expr p, Expr q) implements Expr {}

    record Var(String name) implements Expr {}

    record Neg(Expr operand) implements Expr {}

    record Add(Expr left, Expr right) implements Expr {}

    record Sub(Expr left, Expr right) implements Expr {}

    /** A product, {@code implicit} when it was written by juxtaposition, as in {@code 2x}. */
    record Mul(Expr left, Expr right, boolean implicit) implements Expr {}

    record Div(Expr left, Expr right) implements Expr {}

    record Pow(Expr base, Expr exponent) implements Expr {}

    /** An application of a defined function, {@code f(x, y)}. */
    record Call(String name, List<Expr> args) implements Expr {
        public Call {
            args = List.copyOf(args);
        }
    }
}
