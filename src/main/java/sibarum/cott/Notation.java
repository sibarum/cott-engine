package sibarum.cott;

/**
 * The surface notation: which characters end an operand, which begin one, and therefore where
 * juxtaposition alone means multiplication.
 *
 * <p>This exists as one class because printing and parsing have to be <em>inverse</em>, not merely
 * similar. {@link Render} drops the {@link #TIMES} wherever {@link #implied} says it can, and
 * {@link #adjacency} puts it back on the way in from exactly the same two character sets. Deriving
 * both from one place is the invariant; the keypad reads them too, so a clicked expression and a
 * typed one agree.
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

    private Notation() {
    }

    /** Characters that end an operand — a following operand token implies multiplication. */
    public static final String OPERAND_TAIL = "0123456789.)eiπωxyz";

    /** Characters that begin an operand token. */
    public static final String OPERAND_HEAD = "0123456789.(eiπωxyzl";

    /**
     * Typed ASCII to the keypad's glyphs, whitespace dropped, juxtaposition made explicit.
     *
     * <p>Whitespace is dropped rather than tolerated token by token, because the parser has no notion
     * of it: {@code 1 + 1} was a syntax error, and a formal sum comes back joined with spaces, which
     * left the calculator unable to re-read its own output.
     */
    public static String normalize(String s) {
        return adjacency(s.replaceAll("\\s+", "")
                .replace('*', TIMES).replace(LEGACY_TIMES, TIMES)
                .replace('/', '÷').replace('-', '−').replace('w', 'ω'));
    }

    /**
     * Make juxtaposition multiply: {@code 2ω}, {@code 3(x+1)}, {@code xy}. The keypad has always
     * inserted this sign as you press, but a typed expression never got it — so {@code 2ω} was a
     * syntax error. Doing it here rather than in the parser keeps typed input and the keypad agreeing.
     */
    static String adjacency(String s) {
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i > 0 && implied(s.charAt(i - 1), c)) {
                out.append(TIMES);
            }
            out.append(c);
        }
        return out.toString();
    }

    /**
     * Whether juxtaposition alone multiplies. The left side has to be something an operand can
     * follow, the right has to be something an operand can start with, and a digit may never lead —
     * mid-numeral the digits belong to one operand, since {@code 2·3} is not {@code 23}.
     */
    public static boolean implied(char left, char right) {
        return endsOperand(left) && startsOperand(right) && !numeral(right);
    }

    public static boolean endsOperand(char c) {
        return OPERAND_TAIL.indexOf(c) >= 0;
    }

    public static boolean startsOperand(char c) {
        return OPERAND_HEAD.indexOf(c) >= 0;
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
