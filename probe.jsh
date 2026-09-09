import sibarum.cott.*;
import sibarum.cott.engine.base.expr.*;
import sibarum.cott.engine.operation.binary.*;
import sibarum.cott.engine.operation.unary.*;
import sibarum.cott.engine.rational.expr.*;
import sibarum.cott.engine.traction.expr.*;
import sibarum.cott.engine.derivation.*;
import static sibarum.cott.engine.rational.expr.RationalLiteral.*;
/** Type an expression the way the display would. */
String ev(String entry) { return Cott.evaluate(entry); }
/** What it parses to, before anything answers it. */
String term(String entry) { return Parser.parse(Notation.normalize(entry)).toString(); }
System.out.println("ev(\"...\") to evaluate, why(\"...\") for the derivation, term(\"...\") for the parsed shape.");

/** The answer with its proof: every rewrite, and what licensed each. */
void why(String entry) {
    var d = Cott.derive(entry);
    System.out.println(entry + "  =>  " + Render.show(d.to())
            + (d.isProven() ? "   [proven]" : "   [assumes " + d.assumptions() + "]"));
    for (var s : d.steps()) {
        System.out.println("    = " + Render.show(s.after()) + "        " + s.rule());
    }
}
