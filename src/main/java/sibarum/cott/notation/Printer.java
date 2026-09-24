package sibarum.cott.notation;

import java.util.stream.Collectors;

/**
 * Writes an expression back in the universal notation, with the fewest parentheses that read back as the
 * same tree. A product written by juxtaposition is printed that way where it reads back unambiguously.
 */
public final class Printer {

    private Printer() {}

    private static final int SUM = 1, PRODUCT = 2, SIGN = 3, POWER = 4, ATOM = 5;

    public static String print(Expr e) {
        return switch (e) {
            case Expr.Num n -> n.value().toString();
            case Expr.Omega o -> "ω";
            case Expr.Var v -> v.name();
            case Expr.Call c -> c.name() + c.args().stream().map(Printer::print).collect(Collectors.joining(", ", "(", ")"));
            case Expr.Neg n -> "-" + wrap(n.operand(), SIGN);
            case Expr.Add a -> wrap(a.left(), SUM) + " + " + wrap(a.right(), PRODUCT);
            case Expr.Sub s -> wrap(s.left(), SUM) + " - " + wrap(s.right(), PRODUCT);
            case Expr.Div d -> wrap(d.left(), PRODUCT) + "/" + factor(d.right());
            case Expr.Mul m -> {
                String left = wrap(m.left(), PRODUCT);
                String right = factor(m.right());
                boolean juxtapose = m.implicit() && !Character.isDigit(right.charAt(0))
                        && !(m.right() instanceof Expr.Var && m.left() instanceof Expr.Var);
                yield juxtapose ? left + right : left + "·" + right;
            }
            case Expr.Pow p -> wrap(p.base(), ATOM) + "^" + wrap(p.exponent(), SIGN);
        };
    }

    /** The right operand of a product or quotient: a sign is parenthesized, as in {@code 2(-3)}. */
    private static String factor(Expr e) {
        return precedence(e) <= SIGN ? "(" + print(e) + ")" : print(e);
    }

    private static String wrap(Expr e, int atLeast) {
        return precedence(e) < atLeast ? "(" + print(e) + ")" : print(e);
    }

    private static int precedence(Expr e) {
        return switch (e) {
            case Expr.Add a -> SUM;
            case Expr.Sub s -> SUM;
            case Expr.Mul m -> PRODUCT;
            case Expr.Div d -> PRODUCT;
            case Expr.Neg n -> SIGN;
            case Expr.Pow p -> POWER;
            case Expr.Num n -> ATOM;
            case Expr.Omega o -> ATOM;
            case Expr.Var v -> ATOM;
            case Expr.Call c -> ATOM;
        };
    }
}
