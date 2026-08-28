package sibarum.cott;

import java.math.BigInteger;
import java.util.List;

/**
 * The COTT term language. Two sorts, as the theory has always had them: a {@link Val} is a point,
 * an {@link Exp} is something that can sit in an exponent slot.
 *
 * <p>The whole language lives in one file on purpose — it is a specification, and the {@code sealed}
 * hierarchies are exhaustive only when the compiler can see every case at once. That exhaustiveness
 * is what makes {@link Cott}'s switches provably total: a new term form is a compile error in the
 * evaluator rather than a silently stuck expression at runtime.
 *
 * <h2>Everything is a point with a multiplicity</h2>
 *
 * {@code Pt(k, xp(g, t, r))} is k copies of 0^(g + tω + r), so a numeral needs no separate sort:
 *
 * <pre>
 *   1 = 0^0        0 = 0^1        ω = 0^-1       -1 = 0^ω       i = 0^(ω/2)
 *   2 = 2 copies of 0^0           2ω = 2 copies of 0^-1
 * </pre>
 *
 * <p>The named points are <em>spellings</em> of a {@code Pt} and nothing more — unlike the Maude
 * formulation, which had to keep them as irreducible constructors so the residue rules could match
 * them literally. Direct evaluation matches on the value, so the naming collapses to a rendering
 * concern and the whole {@code lift}/{@code drop} round trip disappears from the semantics.
 *
 * <h2>The two readings of a symbol</h2>
 *
 * The one real subtlety survives the port: ω read as a <em>point</em> is the grade −1, while ω read
 * as an <em>exponent</em> is the twist unit. {@link Cott#asPoint} and {@link Cott#asExponent} are
 * the two readings, and confusing them is what made {@code 0^(2ω)} unanswerable for a long time.
 */
public sealed interface Term {

    // ------------------------------------------------------------------ exponents

    /** Something an exponent slot can hold: a concrete triple, or a logarithm that did not resolve. */
    sealed interface Exp extends Term {
    }

    /**
     * An exponent: a rational grade, a twist closing at two, and a torsion closing at one.
     *
     * <p>The torsion slot exists because {@link Rational} reduces. {@code 0/2} is a root of the
     * residue zero — it is <em>not</em> one, and two of them sum back to zero — but as a rational it
     * reduces to plain {@code 0}, so it cannot live in the grade. The closures are enforced here, in
     * the canonical constructor, rather than by a rule that has to fire: an {@code Xp} that is out of
     * range is not merely unreduced, it is unrepresentable.
     */
    record Xp(Rational grade, Rational twist, Rational torsion) implements Exp {
        public Xp {
            twist = close(twist, Rational.TWO);
            torsion = close(torsion, Rational.ONE);
        }

        /** S − p·floor(S/p): the representative of S in [0, p). */
        private static Rational close(Rational s, Rational period) {
            Rational whole = Rational.of(s.divide(period).floor(), BigInteger.ONE);
            return s.subtract(period.multiply(whole));
        }

        public static final Xp UNIT = new Xp(Rational.ZERO, Rational.ZERO, Rational.ZERO);
        /** The twist unit — ω read as an exponent. */
        public static final Xp TWIST = new Xp(Rational.ZERO, Rational.ONE, Rational.ZERO);

        public static Xp grade(Rational g) {
            return new Xp(g, Rational.ZERO, Rational.ZERO);
        }

        public Xp add(Xp o) {
            return new Xp(grade.add(o.grade), twist.add(o.twist), torsion.add(o.torsion));
        }

        public Xp negate() {
            return new Xp(grade.negate(), twist.negate(), torsion.negate());
        }

        public Xp scale(Rational s) {
            return new Xp(grade.multiply(s), twist.multiply(s), torsion.multiply(s));
        }

        /** Whether this is a plain rational — no twist, no torsion. The guard the partial laws carry. */
        public boolean isPlain() {
            return twist.isZero() && torsion.isZero();
        }

        public boolean isUnit() {
            return grade.isZero() && isPlain();
        }

        @Override
        public String toString() {
            return "xp(" + grade + ", " + twist + ", " + torsion + ")";
        }
    }

    /** Log to base 0, unresolved. {@code lg} is its own inverse, so this is the stuck case only. */
    record Lg(Val of) implements Exp {
        @Override
        public String toString() {
            return "lg(" + of + ")";
        }
    }

    /** Log to a general base, unresolved. */
    record Logb(Val base, Val of) implements Exp {
        @Override
        public String toString() {
            return "logb(" + base + ", " + of + ")";
        }
    }

    // ------------------------------------------------------------------ points

    /** A point — anything the theory calls a value. */
    sealed interface Val extends Term {
    }

    /** {@code k} copies of 0^E. Every numeral, every named point, and everything reduced. */
    record Pt(Rational mult, Xp exp) implements Val {
        @Override
        public String toString() {
            return "pt(" + mult + ", " + exp + ")";
        }
    }

    /**
     * An opaque symbol. π and e are here because neither has a base-0 exponential form: π is the
     * normalisation that turns twist into angle, which needs an arc length the algebra does not
     * carry, and e is the base whose logarithm is the line rather than the circle. The variables sit
     * here too — an atom has no point reading and no exponent reading, so it stays opaque in both
     * slots, and that is all the theory needs of either kind.
     */
    record Atom(String name) implements Val {
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * A sum, associative and commutative, held flat and in canonical order.
     *
     * <p>Addition collects like terms and does nothing else — that is the free module on the
     * exponents, which is reversible, so it is admissible where tropical min was not. A sum of
     * unlike exponents has no definite answer and is left standing.
     */
    record Plus(List<Val> args) implements Val {
        public Plus {
            args = List.copyOf(args);
        }

        @Override
        public String toString() {
            return "plus(" + join(args) + ")";
        }
    }

    /** A product, associative and commutative, held flat and in canonical order. */
    record Times(List<Val> args) implements Val {
        public Times {
            args = List.copyOf(args);
        }

        @Override
        public String toString() {
            return "times(" + join(args) + ")";
        }
    }

    /**
     * Negation. It is <em>not</em> rewritten into a twist, even though that is what it means: the
     * additive residue is recognised as {@code plus(A, neg(A))}, so erasing the {@code neg} would
     * destroy the form before the rule could see it. The laws consult it through
     * {@link Cott#asPoint} instead.
     */
    record Neg(Val of) implements Val {
        @Override
        public String toString() {
            return "neg(" + of + ")";
        }
    }

    /** Inversion, kept for the same reason as {@link Neg}: {@code times(A, inv(A))} is a form. */
    record Inv(Val of) implements Val {
        @Override
        public String toString() {
            return "inv(" + of + ")";
        }
    }

    /**
     * Division — <em>primitive</em>, not sugar for multiplication by an inverse. On equal arguments
     * it is the multiplicative residue, and a residue has to keep which argument it came from.
     */
    record Div(Val of, Val by) implements Val {
        @Override
        public String toString() {
            return "div(" + of + ", " + by + ")";
        }
    }

    record Pow(Val base, Val exponent) implements Val {
        @Override
        public String toString() {
            return "pow(" + base + ", " + exponent + ")";
        }
    }

    /**
     * {@code 1^a} — the multiplicative residue family. The winding number is the operand, and it is
     * load-bearing: {@code x/x} is a <em>family</em>, not a single identity, and collapsing every
     * {@code a/a} to one object would destroy the a and break reversibility.
     */
    record Wind(Val of) implements Val {
        @Override
        public String toString() {
            return "wind(" + of + ")";
        }
    }

    /** {@code 0^a} — the additive residue family, the mirror of {@link Wind}. */
    record AWind(Val of) implements Val {
        @Override
        public String toString() {
            return "awind(" + of + ")";
        }
    }

    /**
     * The coarse projection. A projection and not a rewrite rule, so it cannot create critical
     * pairs: it is where {@code 1^a ≈ 1} and {@code 0^a ≈ 0} live without those becoming equations
     * that would collapse the families everywhere.
     */
    record Approx(Val of) implements Val {
        @Override
        public String toString() {
            return "approx(" + of + ")";
        }
    }

    /**
     * A call: one of {@link Real}'s functions, or a name a session has defined (see {@link Bindings}).
     *
     * <p>One node for both, because the difference is not the term's shape but who answers it. A built-in
     * reduces here, in {@link Cott}, when its arguments have a real reading; a defined one is expanded by
     * {@link Bindings} <em>before</em> the evaluator ever sees it, so the evaluator has no notion of an
     * environment and stays a function of its argument alone. A call to neither stands, like everything else
     * this theory has no answer for.
     */
    record Call(String name, List<Val> args) implements Val {
        public Call {
            args = List.copyOf(args);
        }

        @Override
        public String toString() {
            return name + "(" + join(args) + ")";
        }
    }

    // ------------------------------------------------------------------ the named points

    Pt ONE = new Pt(Rational.ONE, Xp.UNIT);
    Pt ZERO = new Pt(Rational.ONE, Xp.grade(Rational.ONE));
    Pt OMEGA = new Pt(Rational.ONE, Xp.grade(Rational.of(-1)));
    Pt MINUS_ONE = new Pt(Rational.ONE, Xp.TWIST);
    Pt IU = new Pt(Rational.ONE, new Xp(Rational.ZERO, Rational.of(1, 2), Rational.ZERO));

    /** The closure set: the three points that erase in the home context of their family. */
    List<Pt> CLOSURE = List.of(ZERO, ONE, OMEGA);

    static Pt number(Rational k) {
        return new Pt(k, Xp.UNIT);
    }

    private static String join(List<? extends Term> args) {
        return String.join(", ", args.stream().map(Object::toString).toList());
    }
}
