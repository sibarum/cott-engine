package sibarum.cott.parse;

import sibarum.cott.engine.ratio.T;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * The functions the engine installs: the model's own operations, and nothing else.
 * <p>
 * This is a vocabulary rather than a keypad. Every entry here is either an operation
 * docs/Traction-Model.md states that the grammar has no glyph for, or a reading {@link T} already
 * publishes -- so each one is a name for something the engine can already do, and none of them is a
 * catalogue of approximations. A session wanting the trigonometric table, a constant, or anything else it
 * has a use for installs it beside these with {@link Catalogue#or}, which is the point of the seam.
 *
 * <h2>Why these and not others</h2>
 * The grammar has {@code + - * / ^} and no more, and the exponent-position arithmetic has no spelling in
 * it at all: {@code ⊕} and {@code ⊗} are not operators the lexer accepts, so without a name for them the
 * mediant and the angle sum are unreachable from a typed line. The same is true of the two inverses, which
 * are the two motions a minus sign might be and is not -- {@link Folding} leaves that choice to the caller,
 * so neither of them can be reached by writing {@code -x}.
 *
 * <pre>
 *  name                arity  exact  projected  what it is
 *  --------------------------------------------------------------------------------------
 *  T(p, q)               2     yes      no      the pair, where the coordinates arrived as terms
 *  oplus(a, b)           2     yes      no      ⊕, the mediant
 *  otimes(a, b)          2     yes      no      ⊗, the angle sum
 *  oplusInverse(x)       1     yes      no      T(-a,-b)
 *  otimesInverse(x)      1     yes      no      T(-a,b), the point conjugated
 *  z(x)                  1     yes      no      the model's z: the angle doubled
 *  otimesPower(x, n)     2     yes     yes      the angle scaled n times
 *  tan(x)                1     yes      no      the tangent of an angle that arrived as a pair
 *  atan(x)               1     yes      no      the angle of a tangent, in the classical branch
 *  theta(x)              1      no     yes      the angle in radians, which no pair stands at
 * </pre>
 *
 * <h2>tan and atan are exact, and they are the same map</h2>
 * A pair is an angle and a tangent at once -- that is the model's first line -- so neither of these has
 * any arithmetic to do, and what is left of both is the branch: {@link T#principal()}, the half a
 * classical tangent answers in. Because the division that feeds a classical {@code atan} keeps the
 * quadrant here, there is no {@code atan2} to install; {@code y÷x} is already it. {@link #ATAN} carries
 * the argument.
 *
 * <h2>The three states, and where each one is earned here</h2>
 * {@code oplus} and its neighbours have an exact form and need no projected one: the answer is a pair, and
 * a pair already has a projection, so an entry that added one would be saying the same thing twice.
 *
 * <p>{@code theta} has no exact form and it is not an omission. An angle in radians is not the ratio of two
 * integers, so there is nothing for it to simplify <em>to</em>; the call stands through simplification, and
 * a caller that never asks for a number never gets an approximation it did not want. It is the only
 * approximation installed here, and it is installed because the radian reading is the one thing a pair can
 * be asked for that the pair cannot answer with.
 *
 * <p>{@code otimesPower} has both, and it is the one entry that genuinely needs both. Its exact form is
 * {@link T#otimesPower}, which is available only at a whole exponent and only while the coordinates still
 * fit -- they grow with every turn, so a large exponent is a pair nobody wants built. Its projected form is
 * the model's angle scaling as written, {@code tan(n · θ)}, which answers at every exponent including the
 * fractional ones where no pair stands at all. So the same call settles exactly at {@code otimesPower(x, 3)}
 * and defers to evaluation at {@code otimesPower(x, 1.5)}, which is the model's general exponent arriving
 * as far as it can come.
 *
 * <p>The fourth state, an entry with neither form, is not used here. {@link Catalogue.Entry#deferred} is
 * for a session that manages a name's substitution itself and still wants the name declared.
 */
public final class Standard {

    private Standard() {
    }

    /**
     * {@code T(p, q)}, where the coordinates were not literal when the text was read.
     * <p>
     * {@link Parse#of} builds the pair where it is written out in full, so what reaches here is the case it
     * cannot: {@code T(x, 1)} with something bound to {@code x} later. A coordinate is an integer, so this
     * declines a ratio -- {@code T(T(1,2), 1)} has no pair to be.
     */
    private static final Catalogue.Entry PAIR = Catalogue.Entry.exact("T", 2, arguments ->
            whole(arguments.get(0)).flatMap(p -> whole(arguments.get(1))
                    .map(q -> new Node.Lit(new T(p, q)))));

    /** {@code ⊕}, the mediant, which the grammar has no operator for. */
    private static final Catalogue.Entry OPLUS =
            Catalogue.Entry.exact("oplus", 2, arguments -> binary(arguments, T::oplus));

    /** {@code ⊗}, the angle sum, which the grammar has no operator for either. */
    private static final Catalogue.Entry OTIMES =
            Catalogue.Entry.exact("otimes", 2, arguments -> binary(arguments, T::otimes));

    /** {@code T(-a,-b)}: the ⊕ inverse, which is one of the two things a minus sign is not. */
    private static final Catalogue.Entry OPLUS_INVERSE =
            Catalogue.Entry.exact("oplusInverse", 1, arguments -> unary(arguments, T::oplusInverse));

    /** {@code T(-a,b)}: the ⊗ inverse, the point conjugated, and the other one. */
    private static final Catalogue.Entry OTIMES_INVERSE =
            Catalogue.Entry.exact("otimesInverse", 1, arguments -> unary(arguments, T::otimesInverse));

    /** The model's {@code z(T(a,b)) = T(2ab, b²−a²)}: the angle doubled. */
    private static final Catalogue.Entry Z =
            Catalogue.Entry.exact("z", 1, arguments -> unary(arguments, T::doubleAngle));

    /**
     * The angle scaled {@code n} times: exact where a pair stands at it and fits, projected always.
     *
     * @see Standard for why this is the entry that needs both forms
     */
    private static final Catalogue.Entry OTIMES_POWER = Catalogue.Entry.of("otimesPower", 2,
            arguments -> literal(arguments.get(0)).flatMap(x -> whole(arguments.get(1))
                    .filter(n -> n.bitLength() < 31)
                    .map(BigInteger::intValueExact)
                    .filter(n -> fits(x, n))
                    .map(n -> new Node.Lit(x.otimesPower(n)))),
            (arguments, projections) -> literal(arguments.get(0))
                    .map(x -> OptionalDouble.of(Math.tan(projections[1] * x.theta())))
                    .orElseGet(OptionalDouble::empty));

    /**
     * The angle the pair stands at, in radians.
     * <p>
     * Read off the term and not off the number beside it, because the number has already lost the
     * orientation: {@code theta(T(1,-1))} is three quarters of a half turn and {@code theta(T(-1,1))} is
     * minus a quarter of one, and both arrive at the projection -1. Where the argument is not a pair at all
     * this declines, since an angle is a thing a pair has.
     */
    private static final Catalogue.Entry THETA = Catalogue.Entry.projected("theta", 1,
            (arguments, projections) -> literal(arguments.get(0))
                    .map(x -> OptionalDouble.of(x.theta()))
                    .orElseGet(OptionalDouble::empty));

    /**
     * The tangent of an angle that arrived as a pair. Exact, because the pair is already its own tangent.
     * <p>
     * {@code T(p,q) = p÷q = tan(arg(q + pi))} is the model's first line, and it says that an angle and the
     * tangent of that angle are two readings of one pair rather than two values with a function between
     * them. So there is no arithmetic for this to do: everything a classical {@code tan} computes is
     * already written down. What is left is the branch, and that is what this answers --
     * {@link T#principal()}, the half a classical tangent reports in.
     *
     * <p>It takes an angle as a <b>pair</b> and not as a number of radians. A radian is a projection of an
     * angle rather than an angle, {@code Math.tan} of one is an approximation, and neither belongs in an
     * installed vocabulary -- see this class's own first paragraph. A session that wants the radian
     * tangent installs it beside these, where it can be seen to be a table of approximations.
     */
    private static final Catalogue.Entry TAN =
            Catalogue.Entry.exact("tan", 1, arguments -> unary(arguments, T::principal));

    /**
     * The angle whose tangent is this value, in the branch a classical {@code atan} answers in -- and it is
     * the same map as {@link #TAN}, which is the whole of what the model's first line claims.
     * <p>
     * A classical pair of these are inverses that do not quite compose: {@code tan} is onto and
     * {@code atan} only reaches {@code (-π/2, π/2)}, so {@code atan(tan(θ))} is {@code θ} folded into that
     * half. Here that fold is the entire content of both, so the two names answer identically and the
     * composition is the fold applied twice, which is the fold.
     *
     * <h2>Why there is no atan2 here</h2>
     * {@code atan2} exists because a classical {@code atan} is given {@code y÷x} and the division has
     * already destroyed the quadrant: {@code 1÷-1} and {@code -1÷1} are one number, and no function of
     * that number can tell which of the two turns it came from. So the two arguments have to be carried in
     * separately and the quadrant rebuilt from their signs.
     *
     * <p>The division here does not destroy it. {@code T(1,-1)} and {@code T(-1,1)} are different values,
     * the quotient of two pairs keeps the placement of the sign it was given, and the angle is read off the
     * pair rather than recovered from it. So {@code y÷x} is what {@code atan2(y, x)} was for -- at whole
     * arguments it is literally the pair {@code T(y,x)} -- and it is exact, total, and has no cases in it.
     * The one place it parts company with {@code atan2} is {@code 0÷0}, where IEEE answers zero and this
     * answers {@code 0ω}, which stands at no angle at all.
     */
    private static final Catalogue.Entry ATAN =
            Catalogue.Entry.exact("atan", 1, arguments -> unary(arguments, T::principal));

    /** What the engine installs. A session adds to it with {@link Catalogue#or}. */
    public static final Catalogue CATALOGUE = Catalogue.of(
            PAIR, OPLUS, OTIMES, OPLUS_INVERSE, OTIMES_INVERSE, Z, OTIMES_POWER, THETA, TAN, ATAN);

    /** The pair this argument is: {@link Catalogue#number}, so a quotient counts as the number it is. */
    private static Optional<T> literal(Node argument) {
        return Catalogue.number(argument);
    }

    /**
     * The whole number this argument is, or empty.
     * <p>
     * A coordinate and an exponent are both integers, and a literal here is a ratio, so this is the one
     * question worth asking of an argument: is its denominator one. {@code T(2,1)} is 2 and {@code T(1,2)}
     * is not a whole number of anything.
     */
    private static Optional<BigInteger> whole(Node argument) {
        return literal(argument)
                .filter(t -> t.q().equals(BigInteger.ONE))
                .map(T::p);
    }

    private static Optional<Node> unary(List<Node> arguments, UnaryOperator<T> f) {
        return literal(arguments.getFirst()).map(x -> new Node.Lit(f.apply(x)));
    }

    private static Optional<Node> binary(List<Node> arguments, BinaryOperator<T> f) {
        return literal(arguments.get(0))
                .flatMap(a -> literal(arguments.get(1)).map(b -> new Node.Lit(f.apply(a, b))));
    }

    /**
     * Whether the angle power of this pair lands inside {@link Node#WIDEST}.
     * <p>
     * The same bound {@link Node#fold} puts on an ordinary power, and for the same reason: the cost of a
     * power is not bounded by the length of what was typed. {@code ⊗} adds the two points' bit lengths at
     * every turn, so the exponent multiplies the width. Too wide is not folded, and the call stands --
     * where the projected form still answers, which is what having both forms buys.
     */
    private static boolean fits(T base, int power) {
        long bits = (long) Math.max(base.p().bitLength(), base.q().bitLength()) * Math.abs((long) power);
        return bits <= Node.WIDEST;
    }
}
