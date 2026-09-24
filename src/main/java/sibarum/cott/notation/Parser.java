package sibarum.cott.notation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads the universal notation: {@code 2x^2+4x+2}, {@code ω(2x)-(0(x+1)+0^2(2x-1))/2}, {@code f(x) = x²}.
 *
 * <ul>
 *   <li>Juxtaposition is multiplication, at the same precedence as {@code ·} and {@code /} and left to
 *       right, so {@code 1/2x} is {@code (1/2)·x}. It binds looser than {@code ^}: {@code 2x^2} is
 *       {@code 2·(x^2)}.</li>
 *   <li>{@code ^} is right-associative and binds tighter than a sign: {@code -2^2} is {@code -(2^2)}.
 *       Superscript digits are an exponent: {@code x²} is {@code x^2}.</li>
 *   <li>A run of letters is split into names. A known name is taken whole, longest first; any other
 *       letter is a name of its own. So {@code xy} is {@code x·y} unless {@code xy} has been defined.</li>
 *   <li>A name followed by {@code (} is a call only when it names a function; otherwise it is
 *       multiplication, as in {@code ω(2x)} or {@code x(x+1)}.</li>
 *   <li>{@code ω} is always the constant, never part of a name.</li>
 * </ul>
 */
public final class Parser {

    private final Set<String> functions;
    private final Set<String> variables;

    /** @param functions the defined function names; @param variables the defined variable names */
    public Parser(Set<String> functions, Set<String> variables) {
        this.functions = Set.copyOf(functions);
        this.variables = Set.copyOf(variables);
    }

    public Parser() {
        this(Set.of(), Set.of());
    }

    // ---- tokens ----

    enum Kind { NUMBER, WORD, NAME, OMEGA, PLUS, MINUS, TIMES, DIVIDE, CARET, LPAREN, RPAREN, COMMA, EQUALS, END }

    record Token(Kind kind, String text, int pos) {}

    private static final String SUPERSCRIPTS = "⁰¹²³⁴⁵⁶⁷⁸⁹";

    static List<Token> tokenize(String s) {
        List<Token> out = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            int start = i;
            if (Character.isDigit(c)) {
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
                if (i < s.length() && s.charAt(i) == '.')
                    throw new SyntaxException("decimal literals are not supported yet", i);
                out.add(new Token(Kind.NUMBER, s.substring(start, i), start));
            } else if (SUPERSCRIPTS.indexOf(c) >= 0) {
                StringBuilder digits = new StringBuilder();
                while (i < s.length() && SUPERSCRIPTS.indexOf(s.charAt(i)) >= 0)
                    digits.append((char) ('0' + SUPERSCRIPTS.indexOf(s.charAt(i++))));
                out.add(new Token(Kind.CARET, "^", start));
                out.add(new Token(Kind.NUMBER, digits.toString(), start));
            } else if (c == 'ω') {
                out.add(new Token(Kind.OMEGA, "ω", i++));
            } else if (Character.isLetter(c)) {
                while (i < s.length() && Character.isLetter(s.charAt(i)) && s.charAt(i) != 'ω') i++;
                out.add(new Token(Kind.WORD, s.substring(start, i), start));
            } else {
                Kind k = switch (c) {
                    case '+' -> Kind.PLUS;
                    case '-', '−' -> Kind.MINUS;
                    case '*', '·', '×', '⋅' -> Kind.TIMES;
                    case '/', '÷' -> Kind.DIVIDE;
                    case '^' -> Kind.CARET;
                    case '(' -> Kind.LPAREN;
                    case ')' -> Kind.RPAREN;
                    case ',' -> Kind.COMMA;
                    case '=' -> Kind.EQUALS;
                    default -> throw new SyntaxException("unexpected character '" + c + "'", i);
                };
                out.add(new Token(k, String.valueOf(c), i++));
            }
        }
        out.add(new Token(Kind.END, "", s.length()));
        return out;
    }

    /** Splits each run of letters into names: known names whole, longest first, other letters alone. */
    static List<Token> splitWords(List<Token> tokens, Set<String> known) {
        List<Token> out = new ArrayList<>();
        for (Token t : tokens) {
            if (t.kind() != Kind.WORD) { out.add(t); continue; }
            String w = t.text();
            int i = 0;
            while (i < w.length()) {
                int end = i + 1;
                for (int j = w.length(); j > i + 1; j--) {
                    if (known.contains(w.substring(i, j))) { end = j; break; }
                }
                out.add(new Token(Kind.NAME, w.substring(i, end), t.pos() + i));
                i = end;
            }
        }
        return out;
    }

    // ---- entry points ----

    /** One line: {@code name = expr}, {@code name(params) = expr}, or an expression. */
    public Statement statement(String text) {
        List<Token> raw = tokenize(text);
        int eq = -1;
        for (int i = 0; i < raw.size(); i++) if (raw.get(i).kind() == Kind.EQUALS) { eq = i; break; }
        if (eq < 0) return new Statement.Expression(expression(raw, known(Set.of(), Set.of())));

        List<Token> lhs = raw.subList(0, eq);
        List<Token> rhs = new ArrayList<>(raw.subList(eq + 1, raw.size()));
        if (lhs.isEmpty() || lhs.getFirst().kind() != Kind.WORD)
            throw new SyntaxException("a definition starts with the name it defines", raw.getFirst().pos());
        String name = lhs.getFirst().text();
        if (lhs.size() == 1) {
            return new Statement.Assign(name, expression(rhs, known(Set.of(name), Set.of())));
        }
        List<String> params = new ArrayList<>();
        int i = 1;
        expectAt(lhs, i++, Kind.LPAREN);
        while (true) {
            Token p = lhs.size() > i ? lhs.get(i) : raw.get(eq);
            if (p.kind() != Kind.WORD) throw new SyntaxException("expected a parameter name", p.pos());
            if (params.contains(p.text())) throw new SyntaxException("parameter '" + p.text() + "' repeats", p.pos());
            params.add(p.text());
            i++;
            Token sep = lhs.size() > i ? lhs.get(i) : raw.get(eq);
            i++;
            if (sep.kind() == Kind.RPAREN) break;
            if (sep.kind() != Kind.COMMA) throw new SyntaxException("expected ',' or ')'", sep.pos());
        }
        if (i != lhs.size()) throw new SyntaxException("expected '=' after the parameters", lhs.get(i).pos());
        Set<String> bodyFunctions = with(functions, name);
        bodyFunctions.removeAll(params); // a parameter shadows a function of the same name
        Parser body = new Parser(bodyFunctions, with(variables, params));
        return new Statement.Define(name, params, body.expression(rhs, body.known(Set.of(), Set.of())));
    }

    /** An expression, with nothing defined by it. */
    public Expr expression(String text) {
        return expression(tokenize(text), known(Set.of(), Set.of()));
    }

    private Set<String> known(Set<String> moreVariables, Set<String> moreFunctions) {
        Set<String> k = new HashSet<>(functions);
        k.addAll(variables);
        k.addAll(moreVariables);
        k.addAll(moreFunctions);
        return k;
    }

    private Expr expression(List<Token> raw, Set<String> known) {
        return new Reader(splitWords(raw, known)).whole();
    }

    private static void expectAt(List<Token> ts, int i, Kind k) {
        if (i >= ts.size() || ts.get(i).kind() != k)
            throw new SyntaxException("expected " + k, i < ts.size() ? ts.get(i).pos() : -1);
    }

    private static Set<String> with(Set<String> s, String x) {
        Set<String> out = new HashSet<>(s);
        out.add(x);
        return out;
    }

    private static Set<String> with(Set<String> s, List<String> xs) {
        Set<String> out = new HashSet<>(s);
        out.addAll(xs);
        return out;
    }

    // ---- the grammar ----

    private final class Reader {
        private final List<Token> ts;
        private int i;

        Reader(List<Token> ts) {
            this.ts = ts;
        }

        Expr whole() {
            if (peek().kind() == Kind.END) throw new SyntaxException("empty expression", peek().pos());
            Expr e = additive();
            if (peek().kind() != Kind.END)
                throw new SyntaxException("unexpected '" + peek().text() + "'", peek().pos());
            return e;
        }

        private Token peek() {
            return ts.get(i);
        }

        private Token next() {
            return ts.get(i++);
        }

        private boolean accept(Kind k) {
            if (peek().kind() != k) return false;
            i++;
            return true;
        }

        private void expect(Kind k, String what) {
            if (!accept(k)) throw new SyntaxException("expected " + what, peek().pos());
        }

        Expr additive() {
            Expr left = multiplicative();
            while (true) {
                if (accept(Kind.PLUS)) left = new Expr.Add(left, multiplicative());
                else if (accept(Kind.MINUS)) left = new Expr.Sub(left, multiplicative());
                else return left;
            }
        }

        Expr multiplicative() {
            Expr left = unary();
            while (true) {
                if (accept(Kind.TIMES)) left = new Expr.Mul(left, unary(), false);
                else if (accept(Kind.DIVIDE)) left = new Expr.Div(left, unary());
                else if (startsPrimary()) left = new Expr.Mul(left, power(), true);
                else return left;
            }
        }

        Expr unary() {
            if (accept(Kind.MINUS)) return new Expr.Neg(unary());
            if (accept(Kind.PLUS)) return unary();
            return power();
        }

        Expr power() {
            Expr base = primary();
            if (accept(Kind.CARET)) return new Expr.Pow(base, unary());
            return base;
        }

        private boolean startsPrimary() {
            return switch (peek().kind()) {
                case NUMBER, NAME, OMEGA, LPAREN -> true;
                default -> false;
            };
        }

        Expr primary() {
            Token t = next();
            return switch (t.kind()) {
                case NUMBER -> new Expr.Num(new BigInteger(t.text()));
                case OMEGA -> new Expr.Omega();
                case NAME -> {
                    if (!functions.contains(t.text())) yield new Expr.Var(t.text());
                    if (peek().kind() != Kind.LPAREN)
                        throw new SyntaxException("function '" + t.text() + "' needs its arguments", peek().pos());
                    next();
                    List<Expr> args = new ArrayList<>();
                    do args.add(additive()); while (accept(Kind.COMMA));
                    expect(Kind.RPAREN, "')'");
                    yield new Expr.Call(t.text(), args);
                }
                case LPAREN -> {
                    Expr e = additive();
                    expect(Kind.RPAREN, "')'");
                    yield e;
                }
                default -> throw new SyntaxException(
                        t.kind() == Kind.END ? "expression ends too soon" : "unexpected '" + t.text() + "'", t.pos());
            };
        }
    }
}
