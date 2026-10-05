package sibarum.cott.calculator;

import sibarum.cott.notation.SyntaxException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The prompt's way of typing symbols a keyboard lacks: a backslash and one character, {@code \o} for
 * {@code ω}. It is a convenience of the command line and lives here, not in the notation, which reads the
 * symbols themselves and is unchanged. A symbol typed directly still works.
 */
public final class Escapes {

    private Escapes() {}

    /** Each escape's character and the symbol it stands for, in the order {@code :symbols} lists them. */
    public static final Map<Character, String> ALL = new LinkedHashMap<>();

    private static final Map<String, String> MEANING = Map.of(
            "ω", "omega", "·", "times", "×", "times", "÷", "divided by", "−", "minus");

    static {
        ALL.put('o', "ω");
        ALL.put('.', "·");
        ALL.put('x', "×");
        ALL.put('/', "÷");
        ALL.put('-', "−");
    }

    /** The escapes whose symbol the arithmetic can use: {@code ω} only where it is a value. */
    public static Map<Character, String> in(Arithmetic arithmetic) {
        Map<Character, String> out = new LinkedHashMap<>(ALL);
        if (!arithmetic.hasOmega()) out.values().remove("ω");
        return out;
    }

    /** What a symbol is, in words: {@code ÷} is "divided by". */
    public static String meaning(String symbol) {
        return MEANING.getOrDefault(symbol, "");
    }

    /** The line with every escape replaced by its symbol. */
    public static String expand(String line) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c != '\\') { out.append(c); continue; }
            if (i + 1 == line.length()) throw new SyntaxException("a backslash needs a character after it", i);
            String symbol = ALL.get(line.charAt(++i));
            if (symbol == null) throw new SyntaxException("unknown escape '\\" + line.charAt(i) + "'", i - 1);
            out.append(symbol);
        }
        return out.toString();
    }
}
