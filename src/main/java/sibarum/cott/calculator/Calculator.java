package sibarum.cott.calculator;

import sibarum.cott.algebra.Evaluator;
import sibarum.cott.algebra.Form;
import sibarum.cott.algebra.IeeeLevel;
import sibarum.cott.algebra.NumberType;
import sibarum.cott.algebra.Pair;
import sibarum.cott.algebra.Readings;
import sibarum.cott.algebra.Rung;
import sibarum.cott.algebra.SizeLimit;
import sibarum.cott.algebra.TractionAlgebra;
import sibarum.cott.notation.Expr;
import sibarum.cott.notation.Parser;
import sibarum.cott.notation.Printer;
import sibarum.cott.notation.Statement;
import sibarum.cott.projection.Projection;
import sibarum.cott.projection.Projections;
import sibarum.cott.traction.T;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A calculator: reads a line, substitutes its variables and inline functions, and either leaves it as it
 * is, when a variable is still free, or evaluates it in the traction algebras over the current
 * {@link NumberType}.
 *
 * <p>It is in one {@link Mode} of each {@link Modeset} at a time, starting in each modeset's
 * {@link Modeset#initial() initial} mode. Definitions are kept as written, not as values, so changing a
 * mode changes what every later line that uses them evaluates to.
 */
public final class Calculator {

    private final Map<String, Expr> variables = new HashMap<>();
    private final Map<String, Statement.Define> functions = new HashMap<>();
    private final Map<Modeset, Mode> modes = new EnumMap<>(Modeset.class);

    public Calculator() {
        for (Modeset m : Modeset.values()) modes.put(m, m.initial());
    }

    /** A calculator in these modes, and in the initial mode of every modeset not named. */
    public Calculator(Mode... modes) {
        this();
        for (Mode m : modes) set(m);
    }

    /** The current mode of a modeset. */
    public Mode mode(Modeset modeset) {
        return modes.get(modeset);
    }

    /** Puts the calculator in a mode, in place of the current mode of the same modeset. */
    public void set(Mode mode) {
        modes.put(mode.modeset(), mode);
    }

    public Limits limits() {
        return (Limits) modes.get(Modeset.LIMITS);
    }

    public NumberType numberType() {
        return (NumberType) modes.get(Modeset.NUMBER_TYPE);
    }

    public SizeLimit sizeLimit() {
        return (SizeLimit) modes.get(Modeset.SIZE_LIMIT);
    }

    public Form form() {
        return (Form) modes.get(Modeset.FORM);
    }

    public Result enter(String line) {
        Set<String> callable = new HashSet<>(functions.keySet());
        callable.addAll(Trig.NAMES);
        Statement s = new Parser(callable, variables.keySet()).statement(line);
        if (!(s instanceof Statement.Expression)) {
            String name = s instanceof Statement.Assign a ? a.name() : ((Statement.Define) s).name();
            if (Trig.isBuiltIn(name)) throw new CalculatorException("'" + name + "' is built in, and cannot be redefined");
        }
        return switch (s) {
            case Statement.Assign a -> {
                functions.remove(a.name());
                variables.put(a.name(), a.value());
                yield new Result.Defined(a.name(), a.name() + " = " + Printer.print(a.value()));
            }
            case Statement.Define d -> {
                variables.remove(d.name());
                functions.put(d.name(), d);
                yield new Result.Defined(d.name(),
                        d.name() + "(" + String.join(", ", d.params()) + ") = " + Printer.print(d.body()));
            }
            case Statement.Expression e -> answer(e.expr());
        };
    }

    private Result answer(Expr expr) {
        Expr substituted = substitute(expr, List.of());
        if (!free(substituted).isEmpty()) return new Result.Unevaluated(substituted, Printer.print(substituted));
        Map<String, String> certificates = new LinkedHashMap<>();
        Evaluator.Result r = Evaluator.evaluate(substituted, numberType(), sizeLimit(), form(),
                (call, args) -> builtIn(call, args, certificates));
        Map<String, String> readings = new LinkedHashMap<>();
        Map<String, Rung> rungs = new LinkedHashMap<>();
        Rung rung = rung(r, certificates);
        readings.put("rung", rung.label() + reason(r, certificates));
        rungs.put("rung", rung);
        switch (r.value()) {
            case Double d -> {
                readings.put("exact", IeeeLevel.exact(d));
                readings.put("hex", Double.toHexString(d));
                readings.put("bits", IeeeLevel.bits(d));
            }
            case Pair<?>(TractionAlgebra a, BigInteger p, BigInteger q) when a == TractionAlgebra.Q -> {
                for (Projection<?> projection : Projections.ALL) {
                    readings.put(projection.name(), projection.read(new T(p, q)));
                    Rung of = PROJECTION_RUNGS.get(projection.name());
                    if (of != null) rungs.put(projection.name(), of);
                }
            }
            default -> {}
        }
        for (Readings.Reading reading : Readings.of(r.value())) {
            readings.put(reading.name(), reading.text());
            if (reading.rung() != null) rungs.put(reading.name(), reading.rung());
        }
        certificates.forEach((call, certificate) -> {
            readings.put(call, certificate);
            rungs.put(call, Rung.UP_TO_ERROR);
        });
        return new Result.Value(r.value(), r.text(), readings, rungs);
    }

    /** The rungs of the flat ratio's projections: three quotients, and an angle that is a direction in a double. */
    private static final Map<String, Rung> PROJECTION_RUNGS = Map.of(
            "ray", Rung.QUOTIENT, "ratio", Rung.QUOTIENT, "classical", Rung.QUOTIENT, "angle", Rung.UP_TO_ERROR);

    /**
     * The answer's rung: an error where a double is {@code NaN}; up to error where a double, a rounded Decimal or a
     * dialed {@code cos} or {@code sin} is in it; otherwise exact, since evaluation reduces nothing it cannot undo.
     */
    private Rung rung(Evaluator.Result r, Map<String, String> certificates) {
        if (hasNaN(r.value())) return Rung.ERROR;
        if (numberType() == NumberType.IEEE || r.rounded() || !certificates.isEmpty()) return Rung.UP_TO_ERROR;
        return Rung.EXACT;
    }

    private String reason(Evaluator.Result r, Map<String, String> certificates) {
        if (hasNaN(r.value())) return ": NaN";
        if (numberType() == NumberType.IEEE) return ": IEEE 64-bit rounding";
        if (r.rounded()) return ": rounded to " + sizeLimit().decimalDigits() + " significant digits";
        if (!certificates.isEmpty()) return ": cos and sin are dialed to a bracket";
        return "";
    }

    private static boolean hasNaN(Object v) {
        return v instanceof Double d ? d.isNaN() : v instanceof Pair<?> x && (hasNaN(x.p()) || hasNaN(x.q()));
    }

    /**
     * {@code cos} and {@code sin}, for a turn written over the Integers: a whole number, or {@code Q(a, b)}. The
     * answer is the {@code Q} pair the descent dials in.
     */
    private Object builtIn(Expr.Call call, List<Object> args, Map<String, String> certificates) {
        if (!Trig.isBuiltIn(call.name())) throw new IllegalStateException("unexpanded call " + call.name());
        T turn = switch (args.getFirst()) {
            case BigInteger n -> new T(n, BigInteger.ONE);
            case Pair<?>(TractionAlgebra a, BigInteger p, BigInteger q) when a == TractionAlgebra.Q -> new T(p, q);
            default -> throw new CalculatorException(call.name() + " takes a turn over the Integers, a whole number or Q(a, b), not "
                    + Printer.print(call.args().getFirst()));
        };
        T answer = Trig.evaluate(call, turn, limits(), certificates);
        return new Pair<>(TractionAlgebra.Q, answer.p(), answer.q());
    }

    // ---- substitution ----

    /** Replaces every bound variable and every call, until only free variables are left. */
    private Expr substitute(Expr e, List<String> expanding) {
        return switch (e) {
            case Expr.Num n -> n;
            case Expr.Decimal d -> d;
            case Expr.Omega o -> o;
            case Expr.Named n -> n;
            case Expr.Construct c -> new Expr.Construct(c.algebra(), substitute(c.p(), expanding), substitute(c.q(), expanding));
            case Expr.Var v -> {
                Expr bound = variables.get(v.name());
                if (bound == null) yield v;
                yield substitute(bound, expand(expanding, v.name()));
            }
            case Expr.Call c when Trig.isBuiltIn(c.name()) -> {
                Trig.checkArity(c);
                yield new Expr.Call(c.name(), c.args().stream().map(a -> substitute(a, expanding)).toList());
            }
            case Expr.Call c -> {
                Statement.Define f = functions.get(c.name());
                if (f == null) throw new CalculatorException("'" + c.name() + "' is no longer a function");
                if (f.params().size() != c.args().size())
                    throw new CalculatorException(c.name() + " takes " + f.params().size() + " argument"
                            + (f.params().size() == 1 ? "" : "s") + ", not " + c.args().size());
                Map<String, Expr> actual = new HashMap<>();
                for (int i = 0; i < f.params().size(); i++)
                    actual.put(f.params().get(i), substitute(c.args().get(i), expanding));
                yield substitute(bind(f.body(), actual), expand(expanding, c.name()));
            }
            case Expr.Neg n -> new Expr.Neg(substitute(n.operand(), expanding));
            case Expr.Add a -> new Expr.Add(substitute(a.left(), expanding), substitute(a.right(), expanding));
            case Expr.Sub s -> new Expr.Sub(substitute(s.left(), expanding), substitute(s.right(), expanding));
            case Expr.Mul m -> new Expr.Mul(substitute(m.left(), expanding), substitute(m.right(), expanding), m.implicit());
            case Expr.Div d -> new Expr.Div(substitute(d.left(), expanding), substitute(d.right(), expanding));
            case Expr.Pow p -> new Expr.Pow(substitute(p.base(), expanding), substitute(p.exponent(), expanding));
        };
    }

    private static List<String> expand(List<String> expanding, String name) {
        if (expanding.contains(name)) {
            List<String> cycle = new ArrayList<>(expanding.subList(expanding.indexOf(name), expanding.size()));
            cycle.add(name);
            throw new CalculatorException("'" + name + "' is defined in terms of itself: " + String.join(" → ", cycle));
        }
        List<String> out = new ArrayList<>(expanding);
        out.add(name);
        return out;
    }

    /** A function's body with its parameters replaced by the arguments, and nothing else touched. */
    private static Expr bind(Expr e, Map<String, Expr> actual) {
        return switch (e) {
            case Expr.Num n -> n;
            case Expr.Decimal d -> d;
            case Expr.Omega o -> o;
            case Expr.Named n -> n;
            case Expr.Construct c -> new Expr.Construct(c.algebra(), bind(c.p(), actual), bind(c.q(), actual));
            case Expr.Var v -> actual.getOrDefault(v.name(), v);
            case Expr.Call c -> new Expr.Call(c.name(), c.args().stream().map(a -> bind(a, actual)).toList());
            case Expr.Neg n -> new Expr.Neg(bind(n.operand(), actual));
            case Expr.Add a -> new Expr.Add(bind(a.left(), actual), bind(a.right(), actual));
            case Expr.Sub s -> new Expr.Sub(bind(s.left(), actual), bind(s.right(), actual));
            case Expr.Mul m -> new Expr.Mul(bind(m.left(), actual), bind(m.right(), actual), m.implicit());
            case Expr.Div d -> new Expr.Div(bind(d.left(), actual), bind(d.right(), actual));
            case Expr.Pow p -> new Expr.Pow(bind(p.base(), actual), bind(p.exponent(), actual));
        };
    }

    private static Set<String> free(Expr e) {
        Set<String> out = new LinkedHashSet<>();
        collectFree(e, out);
        return out;
    }

    private static void collectFree(Expr e, Set<String> out) {
        switch (e) {
            case Expr.Num n -> {}
            case Expr.Decimal d -> {}
            case Expr.Omega o -> {}
            case Expr.Named n -> {}
            case Expr.Construct c -> { collectFree(c.p(), out); collectFree(c.q(), out); }
            case Expr.Var v -> out.add(v.name());
            case Expr.Call c -> c.args().forEach(a -> collectFree(a, out));
            case Expr.Neg n -> collectFree(n.operand(), out);
            case Expr.Add a -> { collectFree(a.left(), out); collectFree(a.right(), out); }
            case Expr.Sub s -> { collectFree(s.left(), out); collectFree(s.right(), out); }
            case Expr.Mul m -> { collectFree(m.left(), out); collectFree(m.right(), out); }
            case Expr.Div d -> { collectFree(d.left(), out); collectFree(d.right(), out); }
            case Expr.Pow p -> { collectFree(p.base(), out); collectFree(p.exponent(), out); }
        }
    }
}
