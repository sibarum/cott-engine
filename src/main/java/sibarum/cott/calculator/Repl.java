package sibarum.cott.calculator;

import sibarum.cott.notation.SyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * The calculator at a prompt. A line is an expression or a definition, and a value is shown with every
 * projection of it beneath. {@code :quit} leaves.
 */
public final class Repl {

    public static void main(String[] args) throws IOException {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Calculator calc = new Calculator();
        out.print("> ");
        for (String line; (line = in.readLine()) != null; out.print("> ")) {
            line = line.strip();
            if (line.isEmpty()) continue;
            if (line.equals(":quit")) break;
            try {
                Result r = calc.enter(line);
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
}
