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
 * multiple of it gives the same answer. With one argument the descent goes until one of the {@link Limits}
 * stops it, and the certificate says which. A second argument, a whole number, is the depth itself, as the
 * Lean's {@code n}; it is refused if the limits would stop the descent before it.
 */
final class Trig {

    private Trig() {}

    static final Set<String> NAMES = Set.of("cos", "sin", "exp");

    static boolean isBuiltIn(String name) {
        return NAMES.contains(name);
    }

    static void checkArity(Expr.Call c) {
        if (c.args().size() != 1 && c.args().size() != 2)
            throw new CalculatorException(c.name() + " takes a turn, and optionally a depth, not "
                    + c.args().size() + " arguments");
    }

    /**
     * The value of {@code cos(t)} or {@code sin(t)} for the turn {@code turn}, within {@code limits}, with the
     * descent's certificate put in {@code certificates} under the call as written.
     */
    @Lean({"T.cosTurn", "T.sinTurn", "T.spinTurn", "T.dial", "T.sin_sq_dial"})
    static T evaluate(Expr.Call c, T turn, Limits limits, Map<String, String> certificates) {
        checkArity(c);
        String call = Printer.print(c);
        if (turn.q().signum() <= 0)
            throw new CalculatorException(c.name() + " takes a turn a/b with b > 0, and " + Printer.print(c.args().getFirst())
                    + " is " + turn);
        if (turn.q().compareTo(BigInteger.valueOf(limits.maxDenominator())) > 0)
            throw new CalculatorException(c.name() + " takes a turn whose denominator is at most " + limits.maxDenominator()
                    + " in " + limits.key() + " limits, and " + Printer.print(c.args().getFirst()) + " is " + turn);
        long b = turn.q().longValueExact();
        RationalTrig.Dialed d;
        if (c.args().size() == 2) {
            if (!(c.args().get(1) instanceof Expr.Num depth) || depth.value().signum() < 0
                    || depth.value().compareTo(BigInteger.valueOf(limits.maxSteps())) > 0)
                throw new CalculatorException("the depth is a whole number up to " + limits.maxSteps() + " in "
                        + limits.key() + " limits, not " + Printer.print(c.args().get(1)));
            int n = depth.value().intValueExact();
            d = RationalTrig.dialed(turn.p(), b, n, null, limits.maxPowerBits());
            if (d.depth() < n)
                throw new CalculatorException(call + " would compare powers of more than " + limits.maxPowerBits()
                        + " bits after depth " + d.depth() + ", past " + limits.key() + " limits");
        } else {
            d = RationalTrig.dialed(turn.p(), b, limits.maxSteps(), limits.width(), limits.maxPowerBits());
        }
        String why = switch (d.stop()) {
            case WIDTH -> "";
            case DEPTH -> c.args().size() == 2 ? "" : " (step limit)";
            case SIZE -> " (power size limit)";
        };
        certificates.put(call, "depth " + d.depth() + why + ", between " + d.bracket().lower() + " and "
                + d.bracket().upper() + ", sin² of the gap 1/" + d.bracket().widthDenominator());
        return c.name().equals("cos") ? Spin.rotCos(d.spin()) : Spin.rotSin(d.spin());
    }
}
