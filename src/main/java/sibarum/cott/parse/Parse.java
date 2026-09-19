package sibarum.cott.parse;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import sibarum.cott.SyntaxException;
import sibarum.cott.engine.ratio.T;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * Text to a {@link Node}, by the grammar in {@code src/main/antlr4/sibarum/cott/parse/Traction.g4}.
 * <p>
 * The grammar is the readable statement of what a calculator here accepts, and ANTLR generates the parser
 * from it, so the precedence table lives in one file that can be read by someone who does not read Java.
 * That is the whole reason for the dependency: the hand-written parser beside this one is correct, and it is
 * also the place where a change to the notation has to be made twice.
 *
 * <h2>What it accepts</h2>
 * <pre>
 *  numbers      2      3.25              a ratio each: T(2,1) and T(325,100), unreduced
 *  pairs        T(1,0)  T(-1,0)          a literal, written as the type writes it
 *  operators    + - * / ^  and  · ÷ −    with ^ to the right, unary minus above · and below ^
 *  names        x  omega  theta_2        anything the theory spells, Greek included
 *  calls        f(x)  log(2, x)          arity is the resolver's business, not the grammar's
 * </pre>
 * There is no juxtaposition. A run of letters is one name, so {@code xy} is the name {@code xy} rather than
 * a product, and {@code 2x} does not parse. Guessing there needs a dictionary of which letter runs are
 * words, and a standard to build calculators on should not need one.
 *
 * <p>A number carries no sign of its own -- {@code -2} is {@link Node.Negation} of {@code T(2,1)} -- which
 * is why {@code T(-1,0)} is read by {@link #of} rather than by the grammar: the coordinates of a literal
 * pair are taken from a negated literal as readily as a plain one, and nowhere else does a sign get to
 * collapse into a coordinate on the way in.
 */
public final class Parse {

    private Parse() {
    }

    /** The term this text spells, unfolded. */
    public static Node of(String text) {
        TractionLexer lexer = new TractionLexer(CharStreams.fromString(text));
        TractionParser parser = new TractionParser(new CommonTokenStream(lexer));
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(REPORTING);
        parser.addErrorListener(REPORTING);
        return new Builder().visit(parser.entry());
    }

    /** Whatever the recogniser could not read, as the exception the display layer already shows. */
    private static final BaseErrorListener REPORTING = new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offending, int line,
                               int position, String message, RecognitionException cause) {
            throw new SyntaxException("cannot read this at character " + (position + 1) + ": " + message);
        }
    };

    /** The parse tree to the term. One visit per alternative in the grammar, and nothing else here. */
    private static final class Builder extends TractionBaseVisitor<Node> {

        @Override
        public Node visitEntry(TractionParser.EntryContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public Node visitGroup(TractionParser.GroupContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public Node visitNumber(TractionParser.NumberContext ctx) {
            return new Node.Lit(ratio(ctx.NUMBER().getText()));
        }

        @Override
        public Node visitName(TractionParser.NameContext ctx) {
            return new Node.Var(ctx.ID().getText());
        }

        @Override
        public Node visitApply(TractionParser.ApplyContext ctx) {
            String name = ctx.ID().getText();
            List<Node> arguments = ctx.args() == null
                    ? List.of()
                    : ctx.args().expr().stream().map(this::visit).toList();
            return pair(name, arguments).orElseGet(() -> new Node.Call(name, arguments));
        }

        @Override
        public Node visitPower(TractionParser.PowerContext ctx) {
            return new Node.Power(visit(ctx.expr(0)), visit(ctx.expr(1)));
        }

        @Override
        public Node visitNegate(TractionParser.NegateContext ctx) {
            return new Node.Negation(visit(ctx.expr()));
        }

        @Override
        public Node visitScale(TractionParser.ScaleContext ctx) {
            Node left = visit(ctx.expr(0));
            Node right = visit(ctx.expr(1));
            return ctx.MUL() != null
                    ? new Node.Product(left, right)
                    : new Node.Product(left, new Node.Reciprocal(right));
        }

        @Override
        public Node visitTotal(TractionParser.TotalContext ctx) {
            Node left = visit(ctx.expr(0));
            Node right = visit(ctx.expr(1));
            return ctx.PLUS() != null
                    ? new Node.Sum(left, right)
                    : new Node.Sum(left, new Node.Negation(right));
        }
    }

    /**
     * {@code T(p,q)} read as one literal, where both arguments are whole numbers.
     * <p>
     * Anything else keeps the name: {@code T(x,1)} is a call on a variable, and a resolver may still answer
     * it. So {@code T} is not a reserved word and stays usable as a name.
     */
    private static Optional<Node> pair(String name, List<Node> arguments) {
        if (!name.equals("T") || arguments.size() != 2) {
            return Optional.empty();
        }
        Optional<BigInteger> p = whole(arguments.get(0));
        Optional<BigInteger> q = whole(arguments.get(1));
        return p.isPresent() && q.isPresent()
                ? Optional.of(new Node.Lit(new T(p.get(), q.get())))
                : Optional.empty();
    }

    /** A coordinate as written: a whole literal, or a negated one, since a number carries no sign. */
    private static Optional<BigInteger> whole(Node node) {
        return switch (node) {
            case Node.Lit(T v) -> v.q().equals(BigInteger.ONE) ? Optional.of(v.p()) : Optional.empty();
            case Node.Negation(Node.Lit(T v)) ->
                    v.q().equals(BigInteger.ONE) ? Optional.of(v.p().negate()) : Optional.empty();
            default -> Optional.empty();
        };
    }

    /**
     * A number as a ratio. {@code 2} is {@code T(2,1)} and {@code 3.25} is {@code T(325,100)} -- the power
     * of ten the digits asked for, not reduced, since nothing in this model reduces.
     */
    private static T ratio(String text) {
        int point = text.indexOf('.');
        if (point < 0) {
            return new T(new BigInteger(text), BigInteger.ONE);
        }
        String digits = text.substring(0, point) + text.substring(point + 1);
        int places = text.length() - point - 1;
        return new T(new BigInteger(digits), BigInteger.TEN.pow(places));
    }
}
