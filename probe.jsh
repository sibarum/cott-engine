import sibarum.cott.*;
import sibarum.cott.engine.base.expr.*;
import sibarum.cott.engine.operation.binary.*;
import sibarum.cott.engine.operation.unary.*;
import sibarum.cott.engine.projective.expr.*;
import sibarum.cott.engine.traction.expr.*;
import static sibarum.cott.engine.projective.expr.ProjectiveRationalLiteral.*;
/** Type an expression the way the display would. */
String ev(String entry) { return Cott.evaluate(entry); }
/** What it parses to, before anything answers it. */
String term(String entry) { return Parser.parse(Notation.normalize(entry)).toString(); }
System.out.println("ev(\"...\") to evaluate, term(\"...\") to see the parsed shape.");
