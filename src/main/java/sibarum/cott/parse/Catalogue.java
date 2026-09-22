package sibarum.cott.parse;

import sibarum.cott.engine.ratio.T;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * The functions a session knows, each declaring which of its two forms it has.
 * <p>
 * {@link Functions} is the seam a resolver plugs into and says nothing about what a name can do. This says
 * it: an entry carries an <b>exact</b> form, a <b>projected</b> form, both, or neither, and those four
 * states are the four ways a call can behave rather than four kinds of object.
 *
 * <pre>
 *  exact  projected   what the call does
 *  ----------------------------------------------------------------------------
 *   yes      no       simplifies to a pair, and evaluates through it where that
 *                     pair has a reading at all
 *   no       yes      does not simplify; the call stands and answers at evaluation
 *   yes      yes      the exact form for simplifying, the projected one for
 *                     evaluating -- which is for a function whose exact answer is
 *                     not always available or not always worth building
 *   no       no       deferred at every step: a declared name that always stands,
 *                     for a caller that substitutes it itself
 * </pre>
 *
 * <h2>Pre-installed, and expanded by the caller</h2>
 * {@link Standard#CATALOGUE} is what the engine installs: the model's own operations, including the two the
 * grammar has no glyph for. An application adds to it with {@link #or}, and the side it puts first is the
 * side that wins a shared name:
 * <pre>
 *  Catalogue session = mine.or(Standard.CATALOGUE);   // mine may override an installed name
 *  Catalogue session = Standard.CATALOGUE.or(mine);   // the installed names are fixed
 * </pre>
 * Neither is the default, because which one is right is a policy about the session and not about the
 * arithmetic.
 *
 * <h2>It is a {@link Functions}, so it needs no adapter</h2>
 * A catalogue answers {@link #apply} out of the exact forms alone, which is what {@link Node#resolve} wants
 * -- so {@code term.resolve(catalogue)} simplifies by the entries that have an exact form and leaves every
 * other call standing. The projected forms are read by {@link Node#projection(Catalogue)}, the one walk here
 * that approximates. A caller that wants only the exact stage never touches them.
 *
 * <h2>Arity is declared, and a call written at another arity stands</h2>
 * The grammar accepts any number of arguments, so {@code tan(1,2)} parses. It does not resolve and it does
 * not project: an entry answers at its own arity and declines at every other, which is the same answer an
 * undefined name gets. Declining is an answer, and it is the one that keeps a typo out of the arithmetic
 * without taking the window down.
 */
public interface Catalogue extends Functions {

    /** The entry for this name, or empty where the name is not one of ours. */
    Optional<Entry> lookup(String name);

    /**
     * Every name this catalogue answers for.
     * <p>
     * For a keypad, a printer, or a notation pass that has to tell a name from a run of letters: they read
     * the names from here rather than each keeping a list, so a key that types {@code oplus(} and a
     * catalogue that answers it cannot drift apart.
     */
    Set<String> names();

    /** Nothing is installed, so every call stands. */
    Catalogue EMPTY = of();

    /**
     * What a name means, and in which of the two stages it means it.
     *
     * @param name      the name as it is written in a call
     * @param arity     how many arguments it answers at, and it declines at any other
     * @param exact     the form that answers with a term, or empty where there is none
     * @param projected the form that answers with a number, or empty where there is none
     */
    record Entry(String name, int arity, Optional<Exact> exact, Optional<Projected> projected) {

        public Entry {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("an entry needs a name");
            }
            if (arity < 0) {
                throw new IllegalArgumentException(name + " cannot take " + arity + " arguments");
            }
        }

        /** An exact form only: it simplifies, and it evaluates through the pair it simplifies to. */
        public static Entry exact(String name, int arity, Exact exact) {
            return new Entry(name, arity, Optional.of(exact), Optional.empty());
        }

        /** A projected form only: the call stands through simplification and answers at evaluation. */
        public static Entry projected(String name, int arity, Projected projected) {
            return new Entry(name, arity, Optional.empty(), Optional.of(projected));
        }

        /** Both forms: the exact one for simplifying, the projected one for evaluating. */
        public static Entry of(String name, int arity, Exact exact, Projected projected) {
            return new Entry(name, arity, Optional.of(exact), Optional.of(projected));
        }

        /**
         * Neither form: a name that is known and always stands.
         * <p>
         * Worth declaring rather than leaving out, because a declared name is one a keypad can offer and a
         * printer can spell, and because the caller doing its own substitution still wants
         * {@link #names()} to be the whole vocabulary.
         */
        public static Entry deferred(String name, int arity) {
            return new Entry(name, arity, Optional.empty(), Optional.empty());
        }

        /** Whether this has an exact form, which is whether a call to it can settle before evaluation. */
        public boolean simplifies() {
            return exact.isPresent();
        }

        /** Whether this has a projected form of its own. */
        public boolean projects() {
            return projected.isPresent();
        }

        /** Whether this many arguments is the number it answers at. */
        public boolean accepts(List<Node> arguments) {
            return arguments.size() == arity;
        }
    }

    /**
     * A form that answers with a term: the arguments as they stand, and empty to leave the call alone.
     * <p>
     * Empty is not a failure. It is the answer for an argument the form cannot use yet -- a variable, or a
     * pair whose coordinates are not whole numbers -- and the call stands so that a later substitution can
     * bring it back.
     */
    @FunctionalInterface
    interface Exact {
        Optional<Node> apply(List<Node> arguments);
    }

    /**
     * A form that answers with a number: the arguments as they stand, their projections beside them, and
     * empty to decline.
     * <p>
     * Both are given because a pair pushed onto the number line has lost which of the four signings it
     * stood in -- {@code T(1,-1)} and {@code T(-1,1)} both project to -1 and are half a turn apart. So a
     * form that needs the orientation reads the term ({@link #number} is the whole of it) and a form that
     * does not reads the number. {@code theta} is the first kind; the exponent of {@code otimesPower} is
     * the second, since a number of turns has no orientation to keep.
     *
     * <p>The projections are supplied rather than taken because projecting a term needs the catalogue that
     * is calling: they are already done by the time a form is offered, and an argument that would not
     * project means the form is not offered at all.
     *
     * <p>This is where an approximation is allowed to live. Declining is for a question with no real answer
     * at all rather than for one whose answer is awkward: {@code NaN} and the two infinities are answers the
     * projection column already gives, and a form that means one of them returns it.
     */
    @FunctionalInterface
    interface Projected {
        OptionalDouble apply(List<Node> arguments, double[] projections);
    }

    /**
     * The pair an argument is, with the ratio arithmetic done first -- what an exact form asks of an
     * argument it means to compute with.
     * <p>
     * {@code atan(1÷-1)} is a call whose argument is a quotient and not yet a literal, because
     * {@link Node#resolve} walks before {@link Node#fold} does and neither folds on anyone's behalf. So a
     * form that wants a number asks for one here. It costs nothing to ask: {@code fold} combines a literal
     * with a literal and applies no identity, so the pair that comes back is the one the argument already
     * was, and an argument that is not a number is still not one.
     */
    static Optional<T> number(Node argument) {
        return argument.fold().literal();
    }

    /**
     * A table of entries, by name.
     *
     * @throws IllegalArgumentException if two entries share a name, which is a caller's table being wrong
     *                                 rather than a precedence to be guessed at -- use {@link #or} to say
     *                                 which of two catalogues wins
     */
    static Catalogue of(Entry... entries) {
        Map<String, Entry> table = new LinkedHashMap<>();
        for (Entry entry : entries) {
            if (table.put(entry.name(), entry) != null) {
                throw new IllegalArgumentException("two entries named " + entry.name());
            }
        }
        Map<String, Entry> copy = Map.copyOf(table);
        Set<String> names = Set.copyOf(table.keySet());
        return new Catalogue() {
            @Override
            public Optional<Entry> lookup(String name) {
                return Optional.ofNullable(copy.get(name));
            }

            @Override
            public Set<String> names() {
                return names;
            }
        };
    }

    /** This catalogue, and for a name it does not have, {@code next}. */
    default Catalogue or(Catalogue next) {
        Catalogue mine = this;
        Set<String> names = new LinkedHashSet<>(mine.names());
        names.addAll(next.names());
        Set<String> all = Set.copyOf(names);
        return new Catalogue() {
            @Override
            public Optional<Entry> lookup(String name) {
                Optional<Entry> found = mine.lookup(name);
                return found.isPresent() ? found : next.lookup(name);
            }

            @Override
            public Set<String> names() {
                return all;
            }
        };
    }

    /**
     * The exact stage: the entry's exact form at its own arity, and empty everywhere else.
     * <p>
     * An entry with no exact form declines here on purpose -- that is what "does not simplify" means, and
     * the call standing is what carries it to {@link Node#projection(Catalogue)}.
     */
    @Override
    default Optional<Node> apply(String name, List<Node> arguments) {
        return lookup(name)
                .filter(entry -> entry.accepts(arguments))
                .flatMap(Entry::exact)
                .flatMap(exact -> exact.apply(arguments));
    }
}
