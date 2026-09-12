package sibarum.cott;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The surface notation: which characters end an operand, which begin one, and therefore where
 * juxtaposition alone means multiplication.
 *
 * <p>This exists as one class because printing and parsing have to be <em>inverse</em>, not merely
 * similar. {@link Render} drops the {@link #TIMES} wherever {@link #implied} says it can, and
 * {@link #adjacency} puts it back on the way in from exactly the same two character sets. Deriving
 * both from one place is the invariant; the keypad reads them too, so a clicked expression and a
 * typed one agree.
 *
 * <h2>Words</h2>
 * The rule above is a rule about single characters, and it has to stay one: {@code xy} is {@code x·y}, so a run
 * of letters cannot be read as a name by default. What breaks the tie is a <b>vocabulary</b> — the names of
 * {@link Real}'s functions, {@code log}, and whatever a session has defined in its {@link Bindings}. The scan
 * below matches those, longest first, and treats each match as one token; everything else is still a character.
 * So {@code sin(x)} is a call, {@code theta} is a name once something has defined it and five juxtaposed
 * variables until then, and {@code xy} never changes meaning.
 *
 * <p>A token that is a <em>function</em> name does not end an operand — its brackets follow it, and
 * {@code sin(x)} must not become {@code sin·(x)} — while a token that is a defined value does, so {@code 2k} is
 * {@code 2·k} exactly as {@code 2x} is.
 */
public final class Notation {

    /**
     * The multiplication sign, as it is printed and as the parser expects it: a middle dot.
     *
     * <p>A named constant rather than a character literal in four files, because it has now been
     * changed once and the sweep reached the printer, the parser, the adjacency pass and the keypad —
     * which is exactly the set of places this class exists to keep in agreement. It was {@code ×}, and
     * a cross is a poor neighbour for {@code x} in an expression whose subject is usually {@code x}.
     *
     * <p>{@code ÷}, {@code −} and {@code ^} are still literals below. They have never had to move, and
     * one constant standing for the one thing that did says more than four that would imply a
     * configurability nobody has asked for.
     */
    public static final char TIMES = '·';

    /** What this notation used to print, still read on the way in so older text is not a syntax error. */
    private static final char LEGACY_TIMES = '×';

    /** The base-0 logarithm's name. Not a {@link Real}: it returns an exponent, and it is COTT's own. */
    static final String LOG = "log";

    private Notation() {
    }

    /** Characters that end an operand — a following operand token implies multiplication. */
    public static final String OPERAND_TAIL = "0123456789.)eiπωxyz";

    /** Characters that begin an operand token. */
    public static final String OPERAND_HEAD = "0123456789.(eiπωxyzl";

    /** The words every session knows, longest first. A session's own names are matched ahead of these. */
    private static final List<String> BUILTIN = builtinWords();

    private static List<String> builtinWords() {
        List<String> words = new ArrayList<>(Real.NAMES);
        words.add(LOG);
        return longestFirst(words);
    }

    private static List<String> longestFirst(List<String> words) {
        List<String> out = new ArrayList<>(words);
        out.sort(Comparator.comparingInt(String::length).reversed().thenComparing(s -> s));
        return List.copyOf(out);
    }

    /**
     * Typed ASCII to the keypad's glyphs, whitespace dropped, juxtaposition made explicit.
     *
     * <p>Whitespace is dropped rather than tolerated token by token, because the parser has no notion
     * of it: {@code 1 + 1} was a syntax error, and a formal sum comes back joined with spaces, which
     * left the calculator unable to re-read its own output.
     */
    public static String normalize(String s) {
        return normalize(s, Bindings.EMPTY);
    }

    /** As {@link #normalize(String)}, reading {@code session}'s names as words rather than as juxtaposition. */
    public static String normalize(String s, Bindings session) {
        return adjacency(s.replaceAll("\\s+", "")
                .replace('*', TIMES).replace(LEGACY_TIMES, TIMES)
                .replace('/', '÷').replace('-', '−').replace('w', 'ω'), session);
    }

    /**
     * Make juxtaposition multiply: {@code 2ω}, {@code 3(x+1)}, {@code xy}, {@code 2sin(x)}. The keypad has always
     * inserted this sign as you press, but a typed expression never got it — so {@code 2ω} was a syntax error.
     * Doing it here rather than in the parser keeps typed input and the keypad agreeing.
     */
    static String adjacency(String s, Bindings session) {
        List<String> words = vocabulary(session);
        StringBuilder out = new StringBuilder(s.length() + 8);
        Token previous = null;
        int i = 0;
        while (i < s.length()) {
            Token token = tokenAt(s, i, words, session);
            if (previous != null && previous.ends() && token.starts() && !numeral(token.text().charAt(0))) {
                out.append(TIMES);
            }
            out.append(token.text());
            previous = token;
            i += token.text().length();
        }
        return out.toString();
    }

    /**
     * The built-in words and {@code session}'s names as one list, longest first.
     *
     * <p>Longest first over the <em>whole</em> list, not one group after the other: a session that defines
     * {@code a} must not shadow {@code atan}, and it would if its own names were tried first.
     *
     * <p>Always asked of the {@link Bindings}, never short-circuited on its being empty. It used to be, and that
     * was a bug waiting for the scope a definition's body is read in: that scope defines nothing and yet carries
     * the parameters, so "empty" and "has no words of its own" stopped being the same question.
     */
    private static List<String> vocabulary(Bindings session) {
        return session.vocabulary();
    }

    /** The built-in words, for {@link Bindings} to fold its own names into. */
    static List<String> builtins() {
        return BUILTIN;
    }

    /** {@code words} longest first, so a scan matching in order finds the longest match. */
    static List<String> vocabularyOf(List<String> words) {
        return longestFirst(words);
    }

    /** One token: a word from the vocabulary, or a single character. */
    private record Token(String text, boolean ends, boolean starts) {
    }

    private static Token tokenAt(String s, int i, List<String> words, Bindings session) {
        String w = longestAt(s, i, words);
        if (w != null) {
            boolean call = Real.of(w) != null || w.equals(LOG) || session.isFunction(w);
            return new Token(w, !call, true);
        }
        char c = s.charAt(i);
        return new Token(String.valueOf(c), endsOperand(c), startsOperand(c));
    }

    /**
     * The word standing at {@code i}, or null where there is none — the same scan {@link #adjacency} makes,
     * exposed so {@link Parser} reads a word exactly where the adjacency pass decided there was one. Two scans
     * that could disagree about where {@code sin} ends is precisely the failure this class exists to prevent.
     */
    static String wordAt(String s, int i, Bindings session) {
        return longestAt(s, i, vocabulary(session));
    }

    private static String longestAt(String s, int i, List<String> words) {
        for (String w : words) {
            if (s.startsWith(w, i)) {
                return w;
            }
        }
        return null;
    }

    /**
     * Whether juxtaposition alone multiplies. The left side has to be something an operand can
     * follow, the right has to be something an operand can start with, and a digit may never lead —
     * mid-numeral the digits belong to one operand, since {@code 2·3} is not {@code 23}.
     *
     * <p>Character by character, which is what {@link Render} needs: it joins two pieces it has already
     * rendered. A piece beginning with a letter used to keep the sign, because no letter but {@code x},
     * {@code y} and {@code z} started an operand; every letter does now, so {@code 2·sin(x)} prints as
     * {@code 2sin(x)} and the scan finds {@code sin} as its own token on the way back.
     *
     * <p>What that opens is a hazard this test cannot see, because it is character by character: two pieces
     * can concatenate into a <em>word</em>, and {@code c·o·s} printed as {@code cos} is not a product any
     * more. {@link Render} checks for that separately before dropping a sign.
     */
    public static boolean implied(char left, char right) {
        return endsOperand(left) && startsOperand(right) && !numeral(right);
    }

    public static boolean endsOperand(char c) {
        return OPERAND_TAIL.indexOf(c) >= 0 || variable(c);
    }

    public static boolean startsOperand(char c) {
        return OPERAND_HEAD.indexOf(c) >= 0 || variable(c);
    }

    /**
     * Whether this character is a variable standing on its own.
     *
     * <p>Any letter, which subsumes the {@code eiπωxyz} written into the two sets above — those are kept
     * because they say which letters are <em>reserved</em>, and a reader of this class wants to see them.
     *
     * <p>A letter only reaches this test when it is not part of a matched word: {@link #tokenAt} tries the
     * vocabulary first, so {@code sin} is one token and never three variables. The cost of that is real and
     * worth knowing — the vocabulary is matched longest-first, so {@code asin(2)} is the arc sine and not
     * {@code a·sin(2)}, and a variable named {@code a} is invisible inside every function name beginning
     * with one.
     */
    public static boolean variable(char c) {
        return Character.isLetter(c);
    }

    /** Digits and the dot continue a number rather than starting a new operand. */
    public static boolean numeral(char c) {
        return (c >= '0' && c <= '9') || c == '.';
    }

    /** Whether an inserted keypad token continues a number rather than starting an operand. */
    public static boolean digitLike(String token) {
        return !token.isEmpty() && numeral(token.charAt(0));
    }
}
