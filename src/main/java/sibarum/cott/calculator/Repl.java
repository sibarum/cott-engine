package sibarum.cott.calculator;

import sibarum.cott.notation.SyntaxException;
import sibarum.cott.traction.Quotient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * The calculator at a prompt. A line is an expression or a definition; {@code :quotient none|ray|ratio}
 * chooses the invariant results are read under, and {@code :quit} leaves.
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
            if (line.startsWith(":quotient")) {
                String name = line.substring(":quotient".length()).strip();
                if (name.isEmpty()) {
                    out.println(calc.quotient().name().toLowerCase(Locale.ROOT));
                    continue;
                }
                try {
                    calc.quotient(Quotient.valueOf(name.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    out.println("quotients: none, ray, ratio");
                }
                continue;
            }
            try {
                Result r = calc.enter(line);
                out.println(r instanceof Result.Value ? "= " + r.text() : r.text());
            } catch (SyntaxException | CalculatorException e) {
                out.println("! " + e.getMessage());
            }
        }
    }
}
