package sibarum.cott;

/**
 * The surface notation: which characters end an operand, which begin one, and therefore where
 * juxtaposition alone means multiplication.
 *
 * <p>This exists as one class because printing and parsing have to be <em>inverse</em>, not merely
 * similar. {@link Render} drops the × wherever {@link #implied} says it can, and {@link #adjacency}
 * puts it back on the way in from exactly the same two character sets. Deriving both from one place
 * is the invariant; the keypad reads them too, so a clicked expression and a typed one agree.
 */
public final class Notation {

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
                .replace('*', '×').replace('/', '÷').replace('-', '−').replace('w', 'ω'));
    }

    /**
     * Make juxtaposition multiply: {@code 2ω}, {@code 3(x+1)}, {@code xy}. The keypad has always
     * inserted this × as you press, but a typed expression never got it — so {@code 2ω} was a syntax
     * error. Doing it here rather than in the parser keeps typed input and the keypad agreeing.
     */
    static String adjacency(String s) {
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i > 0 && implied(s.charAt(i - 1), c)) {
                out.append('×');
            }
            out.append(c);
        }
        return out.toString();
    }

    /**
     * Whether juxtaposition alone multiplies. The left side has to be something an operand can
     * follow, the right has to be something an operand can start with, and a digit may never lead —
     * mid-numeral the digits belong to one operand, since {@code 2×3} is not {@code 23}.
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
