package sibarum.cott.calculator;

import sibarum.cott.algebra.Base;
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
import sibarum.cott.projection.Rational;
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
    private Base base;

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

    /** What {@code e} is: as set by {@code e = b}, or else the classical {@code e} in IEEE 64-bit and the full-turn {@code 1} otherwise. */
    public Base base() {
        return base != null ? base : numberType() == NumberType.IEEE ? Base.E : Base.ONE;
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
            case Statement.Assign a when a.name().equals("e") -> {
                String written = Printer.print(a.value());
                base = Base.named(written).orElseThrow(() -> new CalculatorException("e is a base: 1, -1, i, 0, ω or e, not " + written));
                yield new Result.Defined("e", "e = " + base.written() + ": e^x is " + base.written() + "^x, counted in " + base.unit());
            }
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
     * {@code cos(x)}, {@code sin(x)} and {@code exp(x)}, which {@code e^x} is, with {@code x} counted in the base's
     * units: {@code e^x} is the rotation by {@code x} units, {@code C(sin, cos)}. Over the Integers the turn is
     * {@code x} times the base's turn unit, and the answer is the pair the descent dials in. In IEEE 64-bit they are a
     * double's, and with the classical {@code e} {@code x} is in radians and {@code e^x} is the real exponential.
     */
    private Object builtIn(Expr.Call call, List<Object> args, Map<String, String> certificates) {
        if (!Trig.isBuiltIn(call.name())) throw new IllegalStateException("unexpanded call " + call.name());
        Base b = base();
        String written = call.name().equals("exp") ? "e^(" + Printer.print(call.args().getFirst()) + ")" : Printer.print(call);
        if (numberType() == NumberType.IEEE) return inDoubles(call, args.getFirst(), b, written);
        if (numberType() == NumberType.DECIMAL)
            throw new CalculatorException(written + " is dialed over the Integers or computed in IEEE 64-bit, not in Decimal");
        if (b == Base.E) throw new CalculatorException(written + " counts in radians with e = e, which take π: IEEE 64-bit has it");
        Rational unit = b.turnUnit().orElseThrow(() ->
                new CalculatorException(written + " needs a turn, and e = " + b.written() + " has " + b.unit()));
        T turn = switch (args.getFirst()) {
            case BigInteger n -> new T(n.multiply(unit.num()), unit.den());
            case Pair<?>(TractionAlgebra a, BigInteger p, BigInteger q) when a == TractionAlgebra.Q ->
                    new T(p.multiply(unit.num()), q.multiply(unit.den()));
            default -> throw new CalculatorException(written + " takes a number of " + b.unit()
                    + " over the Integers, a whole number or Q(a, b), not " + Printer.print(call.args().getFirst()));
        };
        if (!call.name().equals("exp")) {
            T answer = Trig.evaluate(call, turn, limits(), certificates);
            return new Pair<>(TractionAlgebra.Q, answer.p(), answer.q());
        }
        Map<String, String> dialed = new LinkedHashMap<>();
        T cos = Trig.evaluate(new Expr.Call("cos", call.args()), turn, limits(), dialed);
        T sin = Trig.evaluate(new Expr.Call("sin", call.args()), turn, limits(), dialed);
        certificates.put(written, dialed.values().iterator().next());
        return new Pair<>(TractionAlgebra.C, new Pair<>(TractionAlgebra.Q, sin.p(), sin.q()),
                new Pair<>(TractionAlgebra.Q, cos.p(), cos.q()));
    }

    /** The built-ins as a double's: a turn of {@code 2π}, or radians with the classical {@code e}. */
    private static Object inDoubles(Expr.Call call, Object arg, Base b, String written) {
        if (!(arg instanceof Double x))
            throw new CalculatorException(written + " takes a number, not " + Printer.print(call.args().getFirst()));
        double radians;
        if (b == Base.E) {
            if (call.name().equals("exp")) return Math.exp(x);
            radians = x;
        } else {
            Rational unit = b.turnUnit().orElseThrow(() ->
                    new CalculatorException(written + " needs a turn, and e = " + b.written() + " has " + b.unit()));
            radians = 2 * Math.PI * x * unit.num().doubleValue() / unit.den().doubleValue();
        }
        return switch (call.name()) {
            case "cos" -> Math.cos(radians);
            case "sin" -> Math.sin(radians);
            default -> new Pair<>(TractionAlgebra.C, Math.sin(radians), Math.cos(radians));
        };
    }

    /** What {@code e} stands for as a value: the base's value, the full-turn {@code 1} being {@code 1}. */
    private Expr baseValue() {
        return switch (base()) {
            case ONE -> Expr.Num.of(1);
            case NEG_ONE -> new Expr.Neg(Expr.Num.of(1));
            case I -> new Expr.Named("i");
            case ZERO -> Expr.Num.of(0);
            case OMEGA -> new Expr.Omega();
            case E -> {
                if (numberType() != NumberType.IEEE)
                    throw new CalculatorException("the classical e is a value in IEEE 64-bit only");
                yield new Expr.Decimal(new java.math.BigDecimal(Double.toString(Math.E)));
            }
        };
    }

    // ---- substitution ----

    /** Replaces every bound variable and every call, until only free variables are left. */
    private Expr substitute(Expr e, List<String> expanding) {
        return switch (e) {
            case Expr.Num n -> n;
            case Expr.Decimal d -> d;
            case Expr.Omega o -> o;
            case Expr.Named n when n.name().equals("e") -> baseValue();
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
            case Expr.Pow p when p.base() instanceof Expr.Named(String name) && name.equals("e") ->
                    new Expr.Call("exp", List.of(substitute(p.exponent(), expanding)));
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
