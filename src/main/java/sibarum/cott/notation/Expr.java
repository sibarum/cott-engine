package sibarum.cott.notation;

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

    /** {@code ω}. */
    record Omega() implements Expr {}

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
