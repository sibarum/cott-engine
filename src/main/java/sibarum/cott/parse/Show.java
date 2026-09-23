package sibarum.cott.parse;

import sibarum.cott.engine.ratio.T;

import java.util.List;

/**
 * A term as text: {@link Node#show()}'s implementation.
 *
 * <h2>The brackets are the ones the grammar needs, and no others</h2>
 * The levels below are the grammar's, loosest first. Each operand is asked for at its own level, or one
 * above it on a side where nesting would otherwise read back the other way round: a sum's right operand is
 * asked at the product's level, so {@code Sum(a, Sum(b, c))} prints {@code a + (b + c)} rather than
 * {@code a + b + c}, which reads back left-nested.
 *
 * <p>What the brackets keep is the term, not the value. All four of the model's operations associate on the
 * coordinates exactly -- {@code T(1,2) + (T(1,3) + T(1,4))} and the same sum grouped left are both
 * {@code (26,24)}, and for {@code +} it is identical on both sides, {@code (adf+bcf+bde, bdf)} -- so a
 * regrouped sum would fold to the same pair. It would still be a different term, and the term is what the
 * rules match on, so the rendering keeps the one it was given.
 */
final class Show {

    private static final int SUM = 1;
    private static final int PRODUCT = 2;
    private static final int INVERSE = 3;
    private static final int POWER = 4;
    private static final int ATOM = 5;

    private Show() {
    }

    static String of(Node node) {
        return show(node, SUM);
    }

    private static String show(Node n, int need) {
        return switch (n) {
            case Node.Lit(T v) -> "T(" + v.p() + "," + v.q() + ")";
            case Node.Var(String name) -> name;
            case Node.Call(String name, List<Node> args) ->
                    name + "(" + String.join(", ", args.stream().map(a -> show(a, SUM)).toList()) + ")";
            case Node.Power(Node b, Node e) ->
                    bracket(show(b, ATOM) + "^" + show(e, INVERSE), POWER, need);
            case Node.Negation(Node x) -> bracket("−" + show(x, INVERSE), INVERSE, need);
            case Node.Reciprocal(Node x) -> bracket("1/" + show(x, POWER), INVERSE, need);
            case Node.Product(Node l, Node.Reciprocal(Node r)) ->
                    bracket(show(l, PRODUCT) + " / " + show(r, POWER), PRODUCT, need);
            case Node.Product(Node l, Node r) ->
                    bracket(show(l, PRODUCT) + " · " + show(r, INVERSE), PRODUCT, need);
            case Node.Sum(Node l, Node.Negation(Node r)) ->
                    bracket(show(l, SUM) + " − " + show(r, PRODUCT), SUM, need);
            case Node.Sum(Node l, Node r) ->
                    bracket(show(l, SUM) + " + " + show(r, PRODUCT), SUM, need);
        };
    }

    private static String bracket(String text, int have, int need) {
        return have < need ? "(" + text + ")" : text;
    }
}
