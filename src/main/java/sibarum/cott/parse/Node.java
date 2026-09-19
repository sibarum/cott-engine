package sibarum.cott.parse;

import sibarum.cott.engine.ratio.T;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * A parsed term: {@link T} leaves and branching nodes, from docs/Traction-Model.md.
 * <p>
 * Every number in the text is a {@link Lit} holding a ratio, so there is one kind of leaf value and no
 * second numeric type anywhere above the parser. A name the grammar did not recognise as anything else is a
 * {@link Var}, and a name applied to arguments is a {@link Call}; neither is evaluated here. That is the
 * point of them -- a term holding an unknown is the thing a plot is drawn from, and refusing to parse it
 * would be refusing the ordinary case.
 *
 * <h2>Five operations, two of them unary</h2>
 * {@link Sum}, {@link Product} and {@link Power} branch; {@link Negation} and {@link Reciprocal} are the
 * inverses. Subtraction and division are not nodes: {@code a − b} parses as {@code Sum(a, Negation(b))} and
 * {@code a ÷ b} as {@code Product(a, Reciprocal(b))}, which is how the engine's own terms are shaped, so
 * one inverse rule serves both spellings of a difference. {@link #show()} prints them back as {@code −} and
 * {@code ÷}.
 *
 * <h2>Nothing is folded on the way in</h2>
 * The builder constructs nodes directly rather than through arithmetic, so {@code 1+1} parses as a sum of
 * two ones and stays that way until {@link #fold()} is asked for. A parser that added as it read would hand
 * everything downstream a term that had already lost its shape, and in this theory the shape is what the
 * rules match on -- {@code 1·1} is a value where {@code 1·(1÷1)} is an erasure.
 *
 * <p>{@link #fold()} is equally narrow: it combines a literal with a literal and stops. It applies no
 * identity -- not {@code x·1}, not {@code x+0} -- because which invariants may be erased and which must be
 * tracked is the caller's context to decide, not this layer's.
 *
 * <h2>The three ways a term moves</h2>
 * {@link #substitute(Map)} puts nodes in for names, {@link #resolve(Functions)} hands each call to an
 * outside resolver, and {@link #fold()} does the ratio arithmetic. They compose in that order, which is
 * {@link #evaluate(Map, Functions)}, and each is separately useful: substituting without folding gives the
 * term at a point, folding without substituting settles the numeric parts of a term that still has unknowns
 * in it.
 */
public sealed interface Node {

    /** A number, as the ratio it parsed to. */
    record Lit(T value) implements Node {
    }

    /** A name with nothing bound to it. Unknown is a normal state, not an error. */
    record Var(String name) implements Node {
    }

    /** A name applied to arguments. What it means is {@link Functions}' business. */
    record Call(String name, List<Node> arguments) implements Node {
        public Call {
            arguments = List.copyOf(arguments);
        }
    }

    /** {@code a + b}. */
    record Sum(Node left, Node right) implements Node {
    }

    /** {@code a · b}. */
    record Product(Node left, Node right) implements Node {
    }

    /** {@code a ^ b}, associating to the right. */
    record Power(Node base, Node exponent) implements Node {
    }

    /** {@code −a}: the ordinary inverse, and the right half of every subtraction. */
    record Negation(Node operand) implements Node {
    }

    /** {@code 1 ÷ a}: the reciprocal, and the right half of every division. */
    record Reciprocal(Node operand) implements Node {
    }

    /** A literal at these coordinates. */
    static Node of(long p, long q) {
        return new Lit(T.of(p, q));
    }

    /** The ratio this node is, where it is a literal at all. */
    default Optional<T> literal() {
        return this instanceof Lit(T value) ? Optional.of(value) : Optional.empty();
    }

    /** This node's operands, in the order they were written. */
    default List<Node> children() {
        return switch (this) {
            case Lit ignored -> List.of();
            case Var ignored -> List.of();
            case Call c -> c.arguments();
            case Sum(Node l, Node r) -> List.of(l, r);
            case Product(Node l, Node r) -> List.of(l, r);
            case Power(Node b, Node e) -> List.of(b, e);
            case Negation(Node x) -> List.of(x);
            case Reciprocal(Node x) -> List.of(x);
        };
    }

    /**
     * The same node with {@code f} applied to each operand. One rebuild for every walk below, so a new node
     * type is wired in one place rather than in each traversal.
     */
    default Node map(UnaryOperator<Node> f) {
        return switch (this) {
            case Lit ignored -> this;
            case Var ignored -> this;
            case Call(String name, List<Node> args) -> new Call(name, args.stream().map(f).toList());
            case Sum(Node l, Node r) -> new Sum(f.apply(l), f.apply(r));
            case Product(Node l, Node r) -> new Product(f.apply(l), f.apply(r));
            case Power(Node b, Node e) -> new Power(f.apply(b), f.apply(e));
            case Negation(Node x) -> new Negation(f.apply(x));
            case Reciprocal(Node x) -> new Reciprocal(f.apply(x));
        };
    }

    /**
     * Names replaced by the nodes bound to them. A name with no binding stands, and a bound node is
     * substituted as it is -- not walked again -- so a binding that mentions the name it binds does not
     * recurse.
     */
    default Node substitute(Map<String, Node> bindings) {
        if (this instanceof Var(String name)) {
            Node to = bindings.get(name);
            return to != null ? to : this;
        }
        return map(n -> n.substitute(bindings));
    }

    /**
     * Each call offered to {@code functions}, innermost first, and left standing where the resolver declines.
     * <p>
     * Arguments are resolved before the call that holds them, so a resolver sees them already answered, and
     * the node it returns is not resolved again -- a resolver that returns another call is trusted to have
     * meant it.
     */
    default Node resolve(Functions functions) {
        Node inner = map(n -> n.resolve(functions));
        if (inner instanceof Call(String name, List<Node> args)) {
            return functions.apply(name, args).orElse(inner);
        }
        return inner;
    }

    /** {@link #fold(Folding)} under {@link Folding#ORDINARY}. */
    default Node fold() {
        return fold(Folding.ORDINARY);
    }

    /**
     * The ratio arithmetic, where both sides of an operation are literals. Everything else stands.
     *
     * @param folding what negating a literal does -- the one operation docs/Traction-Model.md does not state
     */
    default Node fold(Folding folding) {
        Node n = map(x -> x.fold(folding));
        return switch (n) {
            case Sum(Lit(T a), Lit(T b)) -> new Lit(a.plus(b));
            case Product(Lit(T a), Lit(T b)) -> new Lit(a.times(b));
            case Power(Lit(T a), Lit(T e)) ->
                    exponent(e).filter(k -> fits(a, k)).<Node>map(k -> new Lit(a.power(k))).orElse(n);
            case Reciprocal(Lit(T a)) -> new Lit(a.reciprocal());
            case Negation(Lit(T a)) -> folding.negate(a).<Node>map(Lit::new).orElse(n);
            default -> n;
        };
    }

    /**
     * An exponent the power rule can use: a whole number, zero or more.
     * <p>
     * A negative one is not refused here, it is left unfolded -- {@code T(a,b)^-1} is
     * {@code T(1÷a, 1÷b)}, which is not a pair of integers, and swapping the coordinates instead would be a
     * reduction. The term stands and a caller that wants {@code T(b,a)} asks for the reciprocal by name.
     */
    private static Optional<Integer> exponent(T e) {
        if (!e.q().equals(BigInteger.ONE) || e.p().signum() < 0
                || e.p().bitLength() > 31) {
            return Optional.empty();
        }
        return Optional.of(e.p().intValueExact());
    }

    /** How wide a coordinate a fold may produce: about five million digits, which is two megabytes of it. */
    int WIDEST = 1 << 24;

    /**
     * Whether raising this base to this power lands inside {@link #WIDEST}.
     * <p>
     * A power is the one fold whose cost is not bounded by the size of what was typed. {@code 2^2000000000}
     * is twelve characters and asks for a coordinate of two billion bits, which is a wait and then an
     * {@link OutOfMemoryError} -- and an Error is not what a caller guarding a text field catches. So a
     * power too wide to hold is not folded, and the term stands. That is the same answer a negative exponent
     * gets and it means the same thing: the arithmetic is not wrong, it is not done.
     */
    private static boolean fits(T base, int power) {
        long bits = (long) Math.max(base.p().bitLength(), base.q().bitLength()) * power;
        return bits <= WIDEST;
    }

    /** Substituted, resolved, then folded -- the three walks in the order they compose. */
    default Node evaluate(Map<String, Node> bindings, Functions functions) {
        return substitute(bindings).resolve(functions).fold();
    }

    /**
     * The term as text, in the display glyphs, and parenthesised only where precedence needs it.
     * <p>
     * {@link Parse#of} reads this back: the grammar takes {@code · ÷ −} beside their ASCII spellings, and a
     * literal prints as {@code T(p,q)}, which is also how one is written. A bare {@link Reciprocal} is the
     * one exception -- it prints as {@code 1÷a}, since there is no unary spelling of it, and reads back as
     * the product that text says.
     */
    default String show() {
        return Show.of(this);
    }
}
