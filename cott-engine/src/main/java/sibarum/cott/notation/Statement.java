package sibarum.cott.notation;

import java.util.List;

/** One line of input: an expression, or a definition of a variable or a function. */
public sealed interface Statement {

    record Expression(Expr expr) implements Statement {}

    /** {@code x = 3}. */
    record Assign(String name, Expr value) implements Statement {}

    /** {@code f(x, y) = x + y}. */
    record Define(String name, List<String> params, Expr body) implements Statement {
        public Define {
            params = List.copyOf(params);
        }
    }
}
