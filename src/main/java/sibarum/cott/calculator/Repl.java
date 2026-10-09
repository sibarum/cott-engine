package sibarum.cott.calculator;

import sibarum.cott.algebra.NumberType;
import sibarum.cott.notation.SyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The calculator at a prompt. A line is an expression or a definition, and a value is shown with every
 * reading of it beneath. Symbols are typed as {@link Escapes escapes}, {@code \o} for {@code ω}, and
 * always printed as themselves. {@code :symbols} lists the symbols and their escapes,
 * {@code :help} lists them and the commands, {@code :mode} lists and changes the
 * {@link Mode modes}, and {@code :quit} leaves.
 */
public final class Repl {

    public static void main(String[] args) throws IOException {
        Console console = Console.utf8();
        try {
            run(System.in, new PrintStream(System.out, true, StandardCharsets.UTF_8));
        } finally {
            if (console != null) console.close();
        }
    }

    static void run(InputStream input, PrintStream out) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        Calculator calc = new Calculator();
        out.println("The traction calculator, over " + calc.numberType().label() + " numbers.");
        out.println("Type :help for symbols, modes and commands, or :quit to leave.");
        out.print("> ");
        for (String line; (line = in.readLine()) != null; out.print("> ")) {
            line = line.replace("﻿", "").strip(); // a piped-in byte-order mark is not part of the line
            if (line.isEmpty()) continue;
            if (line.equals(":quit")) break;
            if (line.equals(":help")) { help(calc, out); continue; }
            if (line.equals(":symbols")) { symbols(calc, out); continue; }
            if (line.equals(":mode") || line.startsWith(":mode ")) { mode(calc, line.substring(5).strip(), out); continue; }
            try {
                Result r = calc.enter(Escapes.expand(line));
                if (r instanceof Result.Value v) {
                    out.println("= " + v.text());
                    int width = v.readings().keySet().stream().mapToInt(String::length).max().orElse(0);
                    for (Map.Entry<String, String> e : v.readings().entrySet())
                        out.println("    " + e.getKey() + " ".repeat(width - e.getKey().length() + 2) + e.getValue());
                } else {
                    out.println(r.text());
                }
            } catch (SyntaxException | CalculatorException e) {
                out.println("! " + e.getMessage());
            }
        }
    }

    private static void help(Calculator calc, PrintStream out) {
        out.println("Type an expression, or a definition: x = 2, f(x, y) = x^2 + y.");
        out.println("A pair is written by its algebra: Q(1, 2) is 1/2, C(3, 4) is 4 + 3i, and D, S and P read q − p, p + q and p·q.");
        out.println("Pairs nest and mix: C(0, Q(1, 2)). A bare number takes the algebra of the pair it meets.");
        if (calc.numberType() == NumberType.INTEGER)
            out.println("cos(t) and sin(t) take a turn, as in cos(1/6) for 60°; cos(t, n) dials to depth n.");
        symbols(calc, out);
        out.println(":symbols lists the symbols and their escapes.");
        out.println(":mode lists the modes, and :mode <key> changes one, as in :mode decimal.");
        out.println(":help shows this, :quit leaves.");
    }

    /** The symbols read from a plain keyboard, and what each is. */
    private static final Map<String, String> PLAIN = new LinkedHashMap<>();

    static {
        PLAIN.put("+", "plus");
        PLAIN.put("-", "minus, or a sign");
        PLAIN.put("*", "times; so does writing side by side, as in 2x");
        PLAIN.put("/", "divided by");
        PLAIN.put("^", "to the power of");
        PLAIN.put("( )", "grouping");
        PLAIN.put("=", "defines a variable or a function, as in f(x) = x^2");
        PLAIN.put(",", "separates a function's parameters and arguments, and a pair's coordinates");
        PLAIN.put("C D S Q P", "construct a pair, as in Q(1, 2)");
        PLAIN.put("i", "C(1, 0), the imaginary unit");
        PLAIN.put("_0 _1", "Q(0, -1) and Q(1, -1)");
    }

    /** The plain symbols, then the escaped ones with their escapes. */
    private static void symbols(Calculator calc, PrintStream out) {
        out.println("Symbols:");
        out.println("  typed as they are:");
        int width = PLAIN.keySet().stream().mapToInt(String::length).max().orElse(0);
        PLAIN.forEach((symbol, meaning) -> out.println("    " + symbol + " ".repeat(width + 2 - symbol.length()) + meaning));
        out.println("  typed as escapes, or as themselves:");
        Escapes.ALL.forEach((key, symbol) ->
                out.println("    \\" + key + "  " + symbol + "  " + Escapes.meaning(symbol)));
    }

    /** With no key, every modeset and its modes, the current one marked; with a key, a change of mode. */
    private static void mode(Calculator calc, String key, PrintStream out) {
        if (key.isEmpty()) {
            for (Modeset set : Modeset.values()) {
                out.println(set.label() + ":");
                int width = set.modes().stream().mapToInt(m -> m.key().length()).max().orElse(0);
                for (Mode m : set.modes())
                    out.println((m == calc.mode(set) ? "  * " : "    ") + m.key()
                            + " ".repeat(width - m.key().length() + 2) + m.label());
            }
            return;
        }
        for (Modeset set : Modeset.values()) {
            var found = set.mode(key);
            if (found.isPresent()) {
                calc.set(found.get());
                out.println(set.label() + ": " + found.get().label());
                return;
            }
        }
        out.println("! no mode '" + key + "'; :mode lists them");
    }
}
