package sibarum.cott.parse;

import sibarum.cott.engine.ratio.T;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
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
 * <h2>The three ways a term moves, and the one that leaves</h2>
 * {@link #substitute(Map)} puts nodes in for names, {@link #resolve(Functions)} hands each call to an
 * outside resolver, and {@link #fold()} does the ratio arithmetic. They compose in that order, which is
 * {@link #evaluate(Map, Functions)}, and each is separately useful: substituting without folding gives the
 * term at a point, folding without substituting settles the numeric parts of a term that still has unknowns
 * in it. All three answer with a term, so anything they cannot do is left standing and can be done later.
 *
 * <p>{@link #projection(Catalogue)} is not one of them. It answers with a number rather than a term, which
 * is a door out of the exact arithmetic and is the last thing a caller does -- a plot needs it and a
 * derivation never asks.
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
     * The whole term as one number, or empty where it is not one number -- the fourth walk, and the only
     * one here that approximates.
     * <p>
     * A literal answers by {@link T#projection()}, the model table's column, so what comes back is what
     * that column says including where it says something lossy: {@code ω} is an infinity, {@code -0} keeps
     * its sign, and {@code T(0,0)} is {@code NaN}. Those are answers. Empty is the other thing entirely --
     * the term is not one number at all, because a name in it has nothing bound or a call in it has no
     * answer to give.
     *
     * <p>Everything above the leaves is done in doubles, which is why this is the walk that approximates:
     * the exact arithmetic is {@link #fold()} and it has already taken what it can. What is left standing
     * is what needed a number -- {@code tan(1) + 1} is a sum the ratio arithmetic cannot do and this one
     * can.
     *
     * <h2>What a call does here, by which forms its entry has</h2>
     * A projected form is preferred where there is one, and it is offered the arguments together with their
     * projections -- see {@link Catalogue.Projected}. Where there is none, the exact form is applied and its
     * answer projected, which is how a function that only simplifies still evaluates. Where there is
     * neither, or where the call is written at another arity, or where the name is not in the catalogue at
     * all: empty, and the term was not a number.
     *
     * <p>A form returning a term that contains the call it was given will not terminate. That is the same
     * trust {@link #resolve} extends: a form is taken to have meant what it answered.
     */
    default OptionalDouble projection(Catalogue catalogue) {
        return switch (this) {
            case Lit(T value) -> OptionalDouble.of(value.projection());
            case Var ignored -> OptionalDouble.empty();
            case Call(String name, List<Node> arguments) -> project(catalogue, name, arguments);
            case Sum(Node l, Node r) -> both(catalogue, l, r, Double::sum);
            case Product(Node l, Node r) -> both(catalogue, l, r, (a, b) -> a * b);
            case Power(Node b, Node e) -> both(catalogue, b, e, Math::pow);
            case Negation(Node x) -> one(catalogue, x, a -> -a);
            case Reciprocal(Node x) -> one(catalogue, x, a -> 1.0 / a);
        };
    }

    /** A call's number: its projected form where it has one, and its exact form projected where it does not. */
    private static OptionalDouble project(Catalogue catalogue, String name, List<Node> arguments) {
        Optional<Catalogue.Entry> found = catalogue.lookup(name).filter(e -> e.accepts(arguments));
        if (found.isEmpty()) {
            return OptionalDouble.empty();
        }
        Catalogue.Entry entry = found.get();
        if (entry.projects()) {
            double[] projections = new double[arguments.size()];
            for (int i = 0; i < projections.length; i++) {
                OptionalDouble value = arguments.get(i).projection(catalogue);
                if (value.isEmpty()) {
                    return OptionalDouble.empty();
                }
                projections[i] = value.getAsDouble();
            }
            return entry.projected().orElseThrow().apply(arguments, projections);
        }
        return entry.exact()
                .flatMap(exact -> exact.apply(arguments))
                .map(answer -> answer.projection(catalogue))
                .orElseGet(OptionalDouble::empty);
    }

    private static OptionalDouble one(Catalogue catalogue, Node x, DoubleUnaryOperator f) {
        OptionalDouble value = x.projection(catalogue);
        return value.isPresent() ? OptionalDouble.of(f.applyAsDouble(value.getAsDouble())) : value;
    }

    private static OptionalDouble both(Catalogue catalogue, Node l, Node r, DoubleBinaryOperator f) {
        OptionalDouble left = l.projection(catalogue);
        OptionalDouble right = r.projection(catalogue);
        return left.isPresent() && right.isPresent()
                ? OptionalDouble.of(f.applyAsDouble(left.getAsDouble(), right.getAsDouble()))
                : OptionalDouble.empty();
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
