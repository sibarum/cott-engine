package sibarum.cott;

import sibarum.cott.Term.AWind;
import sibarum.cott.Term.Approx;
import sibarum.cott.Term.Atom;
import sibarum.cott.Term.Call;
import sibarum.cott.Term.Div;
import sibarum.cott.Term.Exp;
import sibarum.cott.Term.Inv;
import sibarum.cott.Term.Lg;
import sibarum.cott.Term.Logb;
import sibarum.cott.Term.Neg;
import sibarum.cott.Term.Plus;
import sibarum.cott.Term.Pow;
import sibarum.cott.Term.Pt;
import sibarum.cott.Term.Times;
import sibarum.cott.Term.Val;
import sibarum.cott.Term.Wind;
import sibarum.cott.Term.Xp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The COTT evaluator: one theory, no modes.
 *
 * <p>Evaluation is innermost-first — arguments reach normal form before the rules at the root are
 * tried — and the rules at each root are tried in an <em>explicit order</em>. That order is the one
 * substantive thing this gains over the equational presentation it replaces. There, "the finer
 * reading wins" had to be smuggled into the exponential law as a negative guard
 * ({@code P =/= zero or Q =/= omega}); here the residue is simply checked first, and the law only
 * sees the pairs the residue did not claim. The comment block became the control flow.
 *
 * <h2>The residue table this answers to</h2>
 *
 * <pre>
 *   x    x·(x/x)    x+(x/x)    x·(x−x)    x+(x−x)
 *   0    erases     1          0          erases
 *   1    erases     1^1        0^1        erases
 *   ω    erases     1^ω        0^ω        erases
 *   n    1^n        1^n        0^n        0^n
 * </pre>
 *
 * <p>The closure set {0, 1, ω} erases in the home context of its family, leaves a plain residue at
 * zero only, and winds everywhere else. An erasure is the operand <em>disappearing</em>, not a value
 * standing for nothing — which is why {@link Term.Plus} and {@link Term.Times} drop the erased
 * operand from the argument list rather than replacing it with anything.
 *
 * <h2>What is deliberately left alone</h2>
 *
 * A sum of unlike exponents, a product of atoms, {@code 0^x}, and {@code (−1)^ω} all stay standing.
 * Nothing here is total: where the theory has no definite answer, the term is returned unchanged.
 * That is a feature and the tests assert it.
 */
public final class Cott {

    private Cott() {
    }

    // ---------------------------------------------------------------- entry points

    /** Parse a display expression, reduce it, and render the result. */
    public static String evaluate(String displayForm) {
        return Render.show(reduce(Parser.parse(Notation.normalize(displayForm))));
    }

    /** Reduce a term to normal form. Total: a term with no applicable rule comes back unchanged. */
    public static Term reduce(Term t) {
        return t instanceof Val v ? eval(v) : evalExp((Exp) t);
    }

    // ---------------------------------------------------------------- the driver

    private static Val eval(Val v) {
        return switch (v) {
            case Pt p -> p;
            case Atom a -> a;
            case Neg n -> neg(eval(n.of()));
            case Inv i -> inv(eval(i.of()));
            case Div d -> div(eval(d.of()), eval(d.by()));
            case Pow p -> pow(eval(p.base()), eval(p.exponent()));
            case Wind w -> new Wind(eval(w.of()));
            case AWind w -> new AWind(eval(w.of()));
            case Approx a -> approx(eval(a.of()));
            case Call c -> call(c);
            case Plus p -> plus(flatten(p.args(), true));
            case Times t -> times(flatten(t.args(), false));
        };
    }

    private static Exp evalExp(Exp e) {
        return switch (e) {
            case Xp x -> x;
            case Lg l -> lg(eval(l.of()));
            case Logb l -> logb(eval(l.base()), eval(l.of()));
        };
    }

    /**
     * Evaluate every argument and splice same-headed results into one flat list. Associativity is
     * represented rather than derived: a {@code Plus} never holds a {@code Plus}, so the pairwise
     * rules below see every pair of operands however the expression was bracketed.
     */
    private static List<Val> flatten(List<Val> args, boolean sum) {
        List<Val> out = new ArrayList<>(args.size());
        for (Val a : args) {
            Val e = eval(a);
            List<Val> nested = sum
                    ? (e instanceof Plus p ? p.args() : null)
                    : (e instanceof Times t ? t.args() : null);
            if (nested != null) {
                out.addAll(nested);
            } else {
                out.add(e);
            }
        }
        return out;
    }

    // ---------------------------------------------------------------- the two readings

    /**
     * The POINT reading — {@code lift}. Null where there is none, which is how a rule declines to
     * fire: an atom has no point reading, so every law that needs one steps aside and the term
     * stands.
     *
     * <p>{@link Neg} is read here rather than rewritten, because {@code plus(A, neg(A))} is the
     * additive residue form and rewriting the {@code neg} into a twist would destroy it.
     */
    static Pt asPoint(Val v) {
        return switch (v) {
            case Pt p -> p;
            case Neg n -> {
                Pt p = asPoint(n.of());
                yield p == null ? null : new Pt(p.mult(), p.exp().add(Xp.TWIST));
            }
            default -> null;
        };
    }

    /**
     * The EXPONENT reading — {@code ex}. This is where ω changes meaning: as a point it is the grade
     * −1, as an exponent it <em>is</em> the twist unit, so k copies of the point ω read as k twists.
     *
     * <p>Note the first case. The point 0 reads as the exponent 0 — that is what makes 0^0 = 1 — and
     * it is a standing axiom about the single point 0, not an instance of any pattern here: 2·0 and
     * 0² have no exponent reading at all. The equational presentation hid this behind an irreducible
     * constructor, where {@code ex(zero)} and {@code ex(pt(1, xp(1,0,0)))} could disagree about the
     * same point without anything complaining.
     */
    static Xp asExponent(Val v) {
        return switch (v) {
            case Pt p -> exponentOfPoint(p);
            case Neg n -> {
                Xp x = asExponent(n.of());
                yield x == null ? null : x.negate();
            }
            case Plus p -> {
                Xp sum = Xp.UNIT;
                for (Val a : p.args()) {
                    Xp x = asExponent(a);
                    if (x == null) {
                        yield null;
                    }
                    sum = sum.add(x);
                }
                yield sum;
            }
            // The exponent product is PARTIAL: at most one factor may twist, because twist times
            // twist is ω², the next floor of the tower and not an exponent this carrier can hold.
            case Times t -> {
                Rational scale = Rational.ONE;
                Xp twisting = null;
                for (Val a : t.args()) {
                    Xp x = asExponent(a);
                    if (x == null) {
                        yield null;
                    }
                    if (x.isPlain()) {
                        scale = scale.multiply(x.grade());
                    } else if (twisting == null) {
                        twisting = x;
                    } else {
                        yield null;
                    }
                }
                yield twisting == null ? Xp.grade(scale) : twisting.scale(scale);
            }
            case Div d -> {
                Xp of = asExponent(d.of());
                Xp by = asExponent(d.by());
                yield of == null || by == null || !by.isPlain() || by.grade().isZero()
                        ? null
                        : of.scale(by.grade().reciprocal());
            }
            default -> null;
        };
    }

    private static Xp exponentOfPoint(Pt p) {
        if (p.equals(Term.ZERO)) {
            return Xp.UNIT;                       // the axiom: 0 in an exponent slot is 0
        }
        if (p.exp().isUnit()) {
            return Xp.grade(p.mult());            // k copies of 1 read as the rational k
        }
        if (p.exp().isPlain() && p.exp().grade().equals(Rational.of(-1))) {
            return new Xp(Rational.ZERO, p.mult(), Rational.ZERO);   // k copies of ω are k twists
        }
        if (p.exp().twist().compareTo(Rational.ONE) >= 0) {
            // a WHOLE twist is exactly the sign, so fold it into the multiplicity and retry. That
            // reduces every readable case to the two above instead of enumerating four.
            return exponentOfPoint(new Pt(p.mult().negate(),
                    new Xp(p.exp().grade(), p.exp().twist().subtract(Rational.ONE), p.exp().torsion())));
        }
        return null;   // a half twist is i, which has no exponent reading at all
    }

    /**
     * The REAL reading, and the third of these after {@link #asPoint} and {@link #asExponent}. Null where there
     * is none, which is how {@link Real}'s functions decline: {@code sin(x)} has no number to work on, so the
     * call stands and the plotter can still draw it.
     *
     * <p>The table is {@link Term.Pt}'s, read on the line: grade 0 is the multiplicity, a positive grade is
     * zero, a negative one is ω and has no real value, a whole twist negates and a half twist is i. π and e
     * answer here and nowhere else in this file, which is the one place their being atoms is a nuisance rather
     * than the point. Everything a residue could be — {@link Wind}, {@link AWind}, {@link Approx} — has no
     * reading at all, and neither does a call that did not reduce.
     */
    static Double asReal(Val v) {
        return switch (v) {
            case Pt p -> realOfPoint(p);
            case Atom a -> switch (a.name()) {
                case "π" -> Math.PI;
                case "e" -> Math.E;
                default -> null;
            };
            case Neg n -> combine(asReal(n.of()), 0.0, (x, ignored) -> -x);
            case Inv i -> combine(1.0, asReal(i.of()), (x, y) -> x / y);
            case Div d -> combine(asReal(d.of()), asReal(d.by()), (x, y) -> x / y);
            case Pow p -> combine(asReal(p.base()), asReal(p.exponent()), Math::pow);
            case Plus p -> fold(p.args(), 0.0, Double::sum);
            case Times t -> fold(t.args(), 1.0, (x, y) -> x * y);
            default -> null;
        };
    }

    private static Double realOfPoint(Pt p) {
        Xp e = p.exp();
        if (!e.torsion().isZero()) {
            return null;                      // a root of the residue zero is not on the line
        }
        boolean negated = e.twist().isOne();
        if (!negated && !e.twist().isZero()) {
            return null;                      // a half twist is i
        }
        int grade = e.grade().signum();
        if (grade < 0) {
            return null;                      // ω is not a value the line holds
        }
        double magnitude = grade > 0 ? 0.0 : decimal(p.mult());
        return finite(negated ? -magnitude : magnitude);
    }

    /** A multiplicity as a double, through {@link java.math.BigDecimal} so a large exact fraction survives. */
    private static double decimal(Rational r) {
        return new java.math.BigDecimal(r.numerator())
                .divide(new java.math.BigDecimal(r.denominator()), java.math.MathContext.DECIMAL64)
                .doubleValue();
    }

    private static Double combine(Double a, Double b, java.util.function.DoubleBinaryOperator op) {
        return a == null || b == null ? null : finite(op.applyAsDouble(a, b));
    }

    private static Double fold(List<Val> args, double unit, java.util.function.DoubleBinaryOperator op) {
        double acc = unit;
        for (Val a : args) {
            Double x = asReal(a);
            if (x == null) {
                return null;
            }
            acc = op.applyAsDouble(acc, x);
        }
        return finite(acc);
    }

    /** An overflow or a division by zero is not a real answer, so it is no answer — the term stands. */
    private static Double finite(double d) {
        return Double.isFinite(d) ? d : null;
    }

    // ---------------------------------------------------------------- calls

    /**
     * A call. Arguments first, as everywhere here, and then the one question that decides everything: does every
     * argument have a real reading? If so this is arithmetic and {@link Real} answers it; if not, the call is a
     * term like any other and stands. A name {@link Real} does not know stands too — {@link Bindings} has
     * already expanded whatever was defined, so anything left is genuinely undefined and saying so by standing
     * is this engine's habit.
     */
    private static Val call(Call c) {
        List<Val> args = new ArrayList<>(c.args().size());
        for (Val a : c.args()) {
            args.add(eval(a));
        }
        Call evaluated = new Call(c.name(), args);
        Real fn = Real.of(c.name());
        if (fn == null || args.size() != fn.arity()) {
            return evaluated;
        }
        List<Double> reals = new ArrayList<>(args.size());
        for (Val a : args) {
            Double x = asReal(a);
            if (x == null) {
                return evaluated;
            }
            reals.add(x);
        }
        Rational answer = fn.apply(reals);
        return answer == null ? evaluated : Term.number(answer);
    }

    // ---------------------------------------------------------------- unary

    private static Val neg(Val a) {
        return a instanceof Neg n ? n.of() : new Neg(a);
    }

    private static Val inv(Val a) {
        if (a instanceof Inv i) {
            return i.of();
        }
        Pt p = asPoint(a);
        // inv of b^x is b^-x, which closes the exponential points under inversion, inv 1 included.
        return p == null || p.mult().isZero() ? new Inv(a) : new Pt(p.mult().reciprocal(), p.exp().negate());
    }

    /**
     * The coarse projection. It is shallow and it is not a rewrite rule — that is the whole point of
     * having it. {@code −0} is recorded here because the identity {@code −0 = 0−0} cannot be oriented
     * as a rewrite without breaking the additive erasure.
     */
    private static Val approx(Val a) {
        return switch (a) {
            case Neg n when n.of().equals(Term.ZERO) -> Term.ZERO;
            case Wind w -> Term.ONE;
            case AWind w -> Term.ZERO;
            default -> a;
        };
    }

    // ---------------------------------------------------------------- division

    private static Val div(Val of, Val by) {
        if (of.equals(by)) {
            return new Wind(of);   // the form, and it outranks the law below
        }
        Pt a = asPoint(of);
        Pt b = asPoint(by);
        return a == null || b == null || b.mult().isZero()
                ? new Div(of, by)
                : new Pt(a.mult().divide(b.mult()), a.exp().add(b.exp().negate()));
    }

    // ---------------------------------------------------------------- powers

    private static Val pow(Val base, Val exponent) {
        // The exponent slot is a MULTIPLICATIVE context, so the closure set erases there too.
        if (exponent instanceof Wind w && isClosure(w.of())) {
            return base;
        }
        // An ADDITIVE identity in a multiplicative slot splits into a quotient, so it keeps a winding.
        if (exponent instanceof AWind w) {
            return eval(new Wind(new Pow(base, w.of())));
        }
        Pt b = asPoint(base);
        Xp e = asExponent(exponent);
        if (b != null && e != null) {
            // b^x then ^y is b^(xy). A multiplicity of one takes any exponent this carrier holds...
            if (b.mult().isOne() && e.isPlain()) {
                return new Pt(Rational.ONE, b.exp().scale(e.grade()));
            }
            // ...and the other way round: a base with a plain grade lets a TWISTING exponent through,
            // which is how ω^ω resolves — grade −1 scales the twist unit to −1, closing back to 1.
            if (b.mult().isOne() && b.exp().isPlain()) {
                return new Pt(Rational.ONE, e.scale(b.exp().grade()));
            }
            // any other multiplicity needs a whole exponent, since a rational power of a
            // multiplicity is not rational
            if (e.isPlain() && e.grade().isNatural()) {
                BigInteger n = e.grade().numerator();
                return new Pt(b.mult().pow(n), b.exp().scale(e.grade()));
            }
        }
        return new Pow(base, exponent);
    }

    // ---------------------------------------------------------------- logarithms

    /**
     * Log to base 0, which is its own inverse: {@code lg} of 0^E is E. Only a multiplicity of one has
     * a log here — the log of 2ω is not an exponent.
     */
    private static Exp lg(Val of) {
        Pt p = asPoint(of);
        return p == null || !p.mult().isOne() ? new Lg(of) : p.exp();
    }

    /**
     * Log to a general base. The base is 0^G, so log_b of 0^X is X/G. The base needs a plain
     * exponent, since dividing by a twist is the ω² floor, and a nonzero one, since base 1 is 0^0 and
     * has no log. The sum law then <em>follows</em> rather than being assumed.
     */
    private static Exp logb(Val base, Val of) {
        if (lg(base) instanceof Xp b && b.isPlain() && !b.grade().isZero() && lg(of) instanceof Xp x) {
            return x.scale(b.grade().reciprocal());
        }
        return new Logb(base, of);
    }

    // ---------------------------------------------------------------- sums

    /**
     * A sum. Collection is the free module on the exponents — reversible, which is what makes it
     * admissible where tropical min was not.
     *
     * <p>Collection NEVER produces a multiplicity of zero. It cannot: zero copies of 0^E has nowhere
     * to record which operand it came from, so {@code 2−2} and {@code 1−1} would land on the same
     * object and the operand would be destroyed — the very thing the residue families exist to
     * prevent. The cancelling case goes to {@link AWind} instead, which keeps it.
     */
    private static Val plus(List<Val> args) {
        List<Val> a = new ArrayList<>(args);
        while (a.size() > 1 && (collect(a, Cott::plusPair)
                || substitute(a, Term.ZERO, true)
                || erase(a, true))) {
            // each helper makes at most one change, then the pass restarts
        }
        return a.size() == 1 ? a.get(0) : new Plus(sorted(a));
    }

    private static Val plusPair(Val x, Val y) {
        // the form: a plus its own negation, however it is spelled
        if (x instanceof Neg n && n.of().equals(y)) {
            return new AWind(y);
        }
        if (y instanceof Neg n && n.of().equals(x)) {
            return new AWind(x);
        }
        Pt p = asPoint(x);
        Pt q = asPoint(y);
        if (p == null || q == null) {
            return null;
        }
        if (p.exp().equals(q.exp())) {
            Rational sum = p.mult().add(q.mult());
            // opposite multiplicities on one exponent: cancelling, so keep the operand
            return sum.isZero()
                    ? new AWind(new Pt(p.mult().signum() > 0 ? p.mult() : q.mult(), p.exp()))
                    : new Pt(sum, p.exp());
        }
        // Exponents a WHOLE TWIST apart are negatives of each other, so they subtract. Ordering on
        // the smaller twist is what makes this deterministic under commutativity.
        Pt lo = twistBelow(p, q) ? p : (twistBelow(q, p) ? q : null);
        if (lo == null) {
            return null;   // unlike exponents have no definite sum and are not rewritten
        }
        Pt hi = lo == p ? q : p;
        return lo.mult().equals(hi.mult())
                ? new AWind(new Pt(lo.mult(), lo.exp()))
                : new Pt(lo.mult().subtract(hi.mult()), lo.exp());
    }

    /** Whether {@code lo} and {@code hi} share an exponent but for a twist, with lo's the smaller. */
    private static boolean twistBelow(Pt lo, Pt hi) {
        Xp a = lo.exp();
        Xp b = hi.exp();
        // The twist lives in [0, 2) by construction, so one whole twist on is +1 below 1 and −1 above.
        Rational turned = a.twist().compareTo(Rational.ONE) < 0
                ? a.twist().add(Rational.ONE)
                : a.twist().subtract(Rational.ONE);
        return a.grade().equals(b.grade())
                && a.torsion().equals(b.torsion())
                && a.twist().compareTo(b.twist()) < 0
                && b.twist().equals(turned);
    }

    // ---------------------------------------------------------------- products

    private static Val times(List<Val> args) {
        List<Val> a = new ArrayList<>(args);
        while (a.size() > 1 && (collect(a, Cott::timesPair)
                || substitute(a, Term.ONE, false)
                || erase(a, false))) {
            // as above: one change per pass, then start over
        }
        return a.size() == 1 ? a.get(0) : new Times(sorted(a));
    }

    private static Val timesPair(Val x, Val y) {
        // the form: a times its own inverse
        if (x instanceof Inv i && i.of().equals(y)) {
            return new Wind(y);
        }
        if (y instanceof Inv i && i.of().equals(x)) {
            return new Wind(x);
        }
        // The user law: 0·ω IS 0/0, an x/x form with no context-free value — so it is a residue and
        // not the 1 the exponential law would give. This is the finer reading outranking the law, and
        // checking it here is what the equational version had to express as a negative guard.
        if (x.equals(Term.ZERO) && y.equals(Term.OMEGA) || x.equals(Term.OMEGA) && y.equals(Term.ZERO)) {
            return new Wind(Term.ZERO);
        }
        Pt p = asPoint(x);
        Pt q = asPoint(y);
        // b^x times b^y is b^(x+y): multiplicities multiply and exponents add, for any base, with no
        // exclusion for zero.
        return p == null || q == null ? null : new Pt(p.mult().multiply(q.mult()), p.exp().add(q.exp()));
    }

    // ---------------------------------------------------------------- the shared machinery

    /** Try every pair; on the first that reduces, replace both with the result and report a change. */
    private static boolean collect(List<Val> a, java.util.function.BinaryOperator<Val> pair) {
        for (int i = 0; i < a.size(); i++) {
            for (int j = i + 1; j < a.size(); j++) {
                Val merged = pair.apply(a.get(i), a.get(j));
                if (merged != null) {
                    a.remove(j);
                    a.set(i, eval(merged));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The PLAIN residue, at zero only: an identity from the foreign family materialises as the value
     * its own family starts from. In a sum, {@code 1^0} becomes 1; in a product, {@code 0^0} becomes 0.
     */
    private static boolean substitute(List<Val> a, Val residue, boolean sum) {
        for (int i = 0; i < a.size(); i++) {
            boolean foreign = sum
                    ? a.get(i) instanceof Wind mw && mw.of().equals(Term.ZERO)
                    : a.get(i) instanceof AWind w && w.of().equals(Term.ZERO);
            if (foreign) {
                a.set(i, sum ? Term.ONE : Term.ZERO);
                return true;
            }
        }
        return false;
    }

    /**
     * ERASURE: the closure set vanishes in the home context of its family. The operand is removed
     * from the list, not replaced — an erasure is the operand disappearing, and a list is the only
     * representation in which "nothing" is expressible without inventing a value for it.
     */
    private static boolean erase(List<Val> a, boolean sum) {
        for (int i = 0; i < a.size(); i++) {
            Val at = a.get(i);
            boolean home = sum
                    ? at instanceof AWind aw && isClosure(aw.of())
                    : at instanceof Wind w && isClosure(w.of());
            if (home) {
                a.remove(i);
                return true;
            }
        }
        return false;
    }

    private static boolean isClosure(Val v) {
        return Term.CLOSURE.contains(v);
    }

    // ---------------------------------------------------------------- canonical order

    /**
     * A deterministic order for the arguments of an AC operator. Nothing in the theory depends on it
     * — the operators carry no order to lose — but the rendering does, and so does every test: the
     * coefficient belongs at the front of a product, not buried in the middle of one.
     */
    private static List<Val> sorted(List<Val> args) {
        List<Val> out = new ArrayList<>(args);
        out.sort(Comparator.<Val>comparingInt(Cott::order).thenComparing(Object::toString));
        return out;
    }

    /** Plain numbers, then named points, then other points, then atoms, then anything built. */
    static int order(Val v) {
        return switch (v) {
            case Pt p when p.exp().isUnit() -> 0;
            case Pt p -> Render.name(p) != null ? 1 : 2;
            case Atom a -> 3;
            default -> 4;
        };
    }
}
