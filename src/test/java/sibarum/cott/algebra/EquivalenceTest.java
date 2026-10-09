package sibarum.cott.algebra;

import org.junit.jupiter.api.Test;
import sibarum.cott.traction.CC;
import sibarum.cott.traction.CTC;
import sibarum.cott.traction.T;
import sibarum.cott.traction.T2;
import sibarum.cott.traction.TC;

import java.math.BigInteger;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sibarum.cott.algebra.TractionAlgebra.C;
import static sibarum.cott.algebra.TractionAlgebra.D;
import static sibarum.cott.algebra.TractionAlgebra.P;
import static sibarum.cott.algebra.TractionAlgebra.Q;
import static sibarum.cott.algebra.TractionAlgebra.S;

/**
 * The evaluator's operations, on pairs whose coordinates are all of one kind, against the hand-written algebras they
 * replace, pair for pair; and D, S and P against the readings they are the algebras of.
 */
class EquivalenceTest {

    private static final Evaluator EV = new Evaluator(NumberType.INTEGER, SizeLimit.MEDIUM, Form.SUM_OF_PRODUCTS, null);
    private static final Random RANDOM = new Random(20261009);

    private static T t() {
        return T.of(RANDOM.nextInt(7) - 3, RANDOM.nextInt(7) - 3);
    }

    private static Pair<BigInteger> q(T t) {
        return new Pair<>(Q, t.p(), t.q());
    }

    private static Pair<BigInteger> c(T t) {
        return new Pair<>(C, t.p(), t.q());
    }

    private static Pair<Pair<BigInteger>> qq(T2 x) {
        return new Pair<>(Q, q(x.p()), q(x.q()));
    }

    private static Pair<Pair<BigInteger>> cq(T2 x) {
        return new Pair<>(C, q(x.p()), q(x.q()));
    }

    private static Pair<Pair<BigInteger>> qc(TC x) {
        return new Pair<>(Q, c(x.p()), c(x.q()));
    }

    private static Pair<Pair<BigInteger>> cc(CC x) {
        return new Pair<>(C, c(x.p()), c(x.q()));
    }

    private static Pair<Pair<Pair<BigInteger>>> cqc(CTC x) {
        return new Pair<>(C, qc(x.p()), qc(x.q()));
    }

    private static void times(int n, Runnable r) {
        for (int k = 0; k < n; k++) r.run();
    }

    private static Object cube(Object x) {
        return EV.pow(x, BigInteger.valueOf(3), null);
    }

    @Test
    void qOverIntegersIsTheFlatRatio() {
        times(500, () -> {
            T x = t(), y = t();
            assertEquals(q(x.plus(y)), EV.add(q(x), q(y)));
            assertEquals(q(x.times(y)), EV.mul(q(x), q(y)));
            assertEquals(q(x.neg()), EV.neg(q(x)));
            assertEquals(q(x.times(y.reciprocal())), EV.div(q(x), q(y)));
        });
    }

    @Test
    void cOverIntegersIsTheGaussianIntegers() {
        times(500, () -> {
            T x = t(), y = t();
            assertEquals(c(x.oplus(y)), EV.add(c(x), c(y)));
            assertEquals(c(x.otimes(y)), EV.mul(c(x), c(y)));
            assertEquals(c(x.oplusInverse()), EV.neg(c(x)));
        });
    }

    @Test
    void qOverQIsTheCompoundRatio() {
        times(500, () -> {
            T2 x = new T2(t(), t()), y = new T2(t(), t());
            assertEquals(qq(x.plus(y)), EV.add(qq(x), qq(y)));
            assertEquals(qq(x.times(y)), EV.mul(qq(x), qq(y)));
            assertEquals(qq(x.neg()), EV.neg(qq(x)));
            assertEquals(qq(x.times(y.reciprocal())), EV.div(qq(x), qq(y)));
            assertEquals(qq(x.power(3)), cube(qq(x)));
        });
    }

    @Test
    void cOverQIsThePoint() {
        times(500, () -> {
            T2 x = new T2(t(), t()), y = new T2(t(), t());
            assertEquals(cq(x.oplus(y)), EV.add(cq(x), cq(y)));
            assertEquals(cq(x.otimes(y)), EV.mul(cq(x), cq(y)));
            assertEquals(cq(x.otimes(y.pointInv())), EV.div(cq(x), cq(y)));
            assertEquals(cq(x.otimes(x).otimes(x)), cube(cq(x)));
        });
    }

    @Test
    void qOverCIsTheComplexRatio() {
        times(500, () -> {
            TC x = new TC(t(), t()), y = new TC(t(), t());
            assertEquals(qc(x.plus(y)), EV.add(qc(x), qc(y)));
            assertEquals(qc(x.times(y)), EV.mul(qc(x), qc(y)));
            assertEquals(qc(x.neg()), EV.neg(qc(x)));
            assertEquals(qc(x.times(y.reciprocal())), EV.div(qc(x), qc(y)));
        });
    }

    @Test
    void cOverCIsTheBicomplexIntegers() {
        times(500, () -> {
            CC x = new CC(t(), t()), y = new CC(t(), t());
            assertEquals(cc(x.oplus(y)), EV.add(cc(x), cc(y)));
            assertEquals(cc(x.otimes(y)), EV.mul(cc(x), cc(y)));
        });
    }

    @Test
    void cOverQOverCIsTheBicomplexRatio() {
        Supplier<TC> tc = () -> new TC(t(), t());
        times(300, () -> {
            CTC x = new CTC(tc.get(), tc.get()), y = new CTC(tc.get(), tc.get());
            assertEquals(cqc(x.oplus(y)), EV.add(cqc(x), cqc(y)));
            assertEquals(cqc(x.otimes(y)), EV.mul(cqc(x), cqc(y)));
            assertEquals(cqc(x.otimes(y.inv())), EV.div(cqc(x), cqc(y)));
        });
    }

    // ---- each algebra is the algebra of its reading ----

    private static BigInteger reading(Object v) {
        Pair<?> x = (Pair<?>) v;
        BigInteger p = (BigInteger) x.p(), q = (BigInteger) x.q();
        return switch (x.algebra()) {
            case D -> q.subtract(p);
            case S -> p.add(q);
            case P -> p.multiply(q);
            default -> throw new IllegalArgumentException(x.algebra().name());
        };
    }

    @Test
    void dSAndPAddAndMultiplyTheirReadings() {
        for (TractionAlgebra a : new TractionAlgebra[]{D, S, P}) {
            times(300, () -> {
                T tx = t(), ty = t();
                Pair<BigInteger> x = new Pair<>(a, tx.p(), tx.q()), y = new Pair<>(a, ty.p(), ty.q());
                if (a != P) assertEquals(reading(x).add(reading(y)), reading(EV.add(x, y)));
                assertEquals(reading(x).multiply(reading(y)), reading(EV.mul(x, y)));
                assertEquals(reading(x).negate(), reading(EV.neg(x)));
            });
        }
    }
}
