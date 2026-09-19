package sibarum.cott.parse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * What a call means, supplied from outside the parser.
 * <p>
 * The grammar reads {@code f(x)} without knowing whether {@code f} is anything, and
 * {@link Node#resolve(Functions)} then offers each call here. Empty means the resolver declines and the call
 * stands as a term -- which is the right answer for a function nobody has defined, and the right answer for
 * one that is defined but has an argument it cannot use yet.
 *
 * <p>Keeping this outside means the parser does not carry a function table, so two callers can read the same
 * text and answer its calls differently -- a plotter resolving {@code sin} on the reals, a derivation
 * resolving it symbolically -- without either of them being the parser's business.
 */
@FunctionalInterface
public interface Functions {

    /**
     * The value of {@code name} applied to {@code arguments}, or empty to leave the call standing.
     *
     * @param arguments already resolved and in the order written
     */
    Optional<Node> apply(String name, List<Node> arguments);

    /** Nothing is defined, so every call stands. */
    Functions NONE = (name, arguments) -> Optional.empty();

    /**
     * A table of them, by name. A name outside the table stands, and so does one whose entry returns null
     * -- which is how an entry declines the arguments it was given.
     */
    static Functions of(Map<String, Function<List<Node>, Node>> table) {
        Map<String, Function<List<Node>, Node>> copy = Map.copyOf(table);
        return (name, arguments) -> {
            Function<List<Node>, Node> f = copy.get(name);
            return f == null ? Optional.empty() : Optional.ofNullable(f.apply(arguments));
        };
    }

    /** This resolver, and where it declines, {@code next}. */
    default Functions or(Functions next) {
        return (name, arguments) -> {
            Optional<Node> mine = apply(name, arguments);
            return mine.isPresent() ? mine : next.apply(name, arguments);
        };
    }
}
