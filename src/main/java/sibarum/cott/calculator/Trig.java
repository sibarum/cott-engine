package sibarum.cott.calculator;

import sibarum.cott.notation.Expr;
import sibarum.cott.notation.Printer;
import sibarum.cott.traction.Lean;
import sibarum.cott.traction.RationalTrig;
import sibarum.cott.traction.Spin;
import sibarum.cott.traction.T;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

/**
 * The built-in functions {@code cos(t)} and {@code sin(t)}, with {@code t} in turns: {@code cos(1/6)} is the
 * cosine of a sixth of a turn, 60°. The answer is the unreduced pair {@code T(q² − p², N)} or
 * {@code T(2pq, N)} of the pair the descent dials in, exactly on the unit circle.
 *
 * <p>The turn is the argument's flat pair {@code T(a, b)}, taken as it is when {@code b > 0}; any positive
 * multiple of it gives the same answer. With one argument the descent stops at the first depth where the
 * bracket's {@code N(L)·N(R)} reaches {@code 2^64}, so the angle between its ends has a squared sine of at
 * most {@code 2^-64}. A second argument, a whole number, is the depth itself, as the Lean's {@code n}.
 */
final class Trig {

    private Trig() {}

    static final Set<String> NAMES = Set.of("cos", "sin");

    /** Larger denominators make the powers the comparison walks too long to compute at the prompt. */
    static final long MAX_DENOMINATOR = 2000;
    static final int MAX_DEPTH = 4096;
    static final BigInteger WIDTH = BigInteger.ONE.shiftLeft(64);

    static boolean isBuiltIn(String name) {
        return NAMES.contains(name);
    }

    /** What an arithmetic without {@code cos} and {@code sin} throws at a call left after substitution. */
    static RuntimeException notHere(Expr.Call c) {
        if (isBuiltIn(c.name()))
            return new CalculatorException(c.name() + " is only in " + Arithmetic.TRACTION_RATIO.label() + " for now");
        return new IllegalStateException("unexpanded call " + c.name());
    }

    static void checkArity(Expr.Call c) {
        if (c.args().size() != 1 && c.args().size() != 2)
            throw new CalculatorException(c.name() + " takes a turn, and optionally a depth, not "
                    + c.args().size() + " arguments");
    }

    /**
     * The value of {@code cos(t)} or {@code sin(t)} for the turn {@code turn}, with the descent's certificate
     * put in {@code certificates} under the call as written.
     */
    @Lean({"T.cosTurn", "T.sinTurn", "T.spinTurn", "T.dial", "T.sin_sq_dial"})
    static T evaluate(Expr.Call c, T turn, Map<String, String> certificates) {
        checkArity(c);
        if (turn.q().signum() <= 0)
            throw new CalculatorException(c.name() + " takes a turn a/b with b > 0, and " + Printer.print(c.args().getFirst())
                    + " is " + turn);
        if (turn.q().compareTo(BigInteger.valueOf(MAX_DENOMINATOR)) > 0)
            throw new CalculatorException(c.name() + " takes a turn whose denominator is at most " + MAX_DENOMINATOR
                    + " for now, and " + Printer.print(c.args().getFirst()) + " is " + turn);
        RationalTrig.Dialed d;
        if (c.args().size() == 2) {
            if (!(c.args().get(1) instanceof Expr.Num depth) || depth.value().signum() < 0
                    || depth.value().compareTo(BigInteger.valueOf(MAX_DEPTH)) > 0)
                throw new CalculatorException("the depth is a whole number up to " + MAX_DEPTH + ", not "
                        + Printer.print(c.args().get(1)));
            int n = depth.value().intValueExact();
            d = RationalTrig.dialed(turn.p(), turn.q().longValueExact(), n, null);
        } else {
            d = RationalTrig.dialed(turn.p(), turn.q().longValueExact(), MAX_DEPTH, WIDTH);
        }
        certificates.put(Printer.print(c), "depth " + d.depth() + ", between " + d.bracket().lower() + " and "
                + d.bracket().upper() + ", sin² of the gap 1/" + d.bracket().widthDenominator());
        return c.name().equals("cos") ? Spin.rotCos(d.spin()) : Spin.rotSin(d.spin());
    }
}
