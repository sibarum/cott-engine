package sibarum.cott.calculator;

import sibarum.cott.notation.Expr;
import sibarum.cott.projection.Projection;
import sibarum.cott.projection.Projections;
import sibarum.cott.traction.CC;
import sibarum.cott.traction.CTC;
import sibarum.cott.traction.T;
import sibarum.cott.traction.T2;
import sibarum.cott.traction.TC;

import java.util.LinkedHashMap;
import java.util.Map;

/** What one line of input answers with. {@link #text} is what the user sees first. */
public sealed interface Result {

    String text();

    /** A variable or function was defined. */
    record Defined(String name, String text) implements Result {}

    /** A variable is still free after substitution, so the expression is left as it is. */
    record Unevaluated(Expr expr, String text) implements Result {}

    /**
     * A value, in the arithmetic it was computed in. {@link #text} writes it, and {@link #readings} gives
     * every other way that arithmetic offers of reading it, by name, written. A front end can show any
     * value from these alone; the records below carry the value itself for anything that needs more.
     */
    sealed interface Value extends Result permits RatioValue, PointValue, ComplexRatioValue, BicomplexValue, BicomplexRatioValue, IeeeValue {

        Arithmetic arithmetic();

        Map<String, String> readings();
    }

    /**
     * A {@link Arithmetic#TRACTION_RATIO} value: computed at Level 2 and flattened. {@code flat} is the
     * ground truth, with no quotient, and {@code text} writes it. Every other reading is a
     * {@link Projection} of it, taken on demand. {@code certificates} says, for each {@code cos} or
     * {@code sin} in the line, how far the descent went and the bracket it ended on.
     */
    record RatioValue(T2 level2, T flat, String text, Map<String, String> certificates) implements Value {

        public RatioValue {
            certificates = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(certificates));
        }

        public RatioValue(T2 level2, T flat, String text) {
            this(level2, flat, text, Map.of());
        }

        @Override
        public Arithmetic arithmetic() {
            return Arithmetic.TRACTION_RATIO;
        }

        public <R> R read(Projection<R> projection) {
            return projection.apply(flat);
        }

        /** Every projection the calculator offers, by name, as written, then the certificates. */
        @Override
        public Map<String, String> readings() {
            Map<String, String> out = new LinkedHashMap<>();
            for (Projection<?> p : Projections.ALL) out.put(p.name(), p.read(flat));
            out.putAll(certificates);
            return out;
        }
    }

    /**
     * A {@link Arithmetic#TRACTION_POINT} value: the point {@code B + A·i} with ratio coordinates, as computed.
     * {@code text} writes it unreduced; the readings are the complex number it is and its squared length.
     */
    record PointValue(T2 point) implements Value {

        @Override
        public Arithmetic arithmetic() {
            return Arithmetic.TRACTION_POINT;
        }

        @Override
        public String text() {
            return TractionPoint.write(point);
        }

        @Override
        public Map<String, String> readings() {
            Map<String, String> out = new LinkedHashMap<>();
            out.put("value", TractionPoint.value(point));
            out.put("length²", TractionPoint.lengthSquared(point));
            return out;
        }
    }

    /**
     * A {@link Arithmetic#COMPLEX_RATIO} value: the ratio {@code z / w} of two Gaussian integers, as computed.
     * {@code text} writes it unreduced; the readings are the complex number it is and the point it
     * rationalizes to.
     */
    record ComplexRatioValue(TC ratio) implements Value {

        @Override
        public Arithmetic arithmetic() {
            return Arithmetic.COMPLEX_RATIO;
        }

        @Override
        public String text() {
            return ComplexRatio.write(ratio);
        }

        @Override
        public Map<String, String> readings() {
            Map<String, String> out = new LinkedHashMap<>();
            out.put("value", ComplexRatio.complexValue(ratio));
            out.put("point", ComplexRatio.point(ratio));
            return out;
        }
    }

    /**
     * A {@link Arithmetic#BICOMPLEX} value: the point {@code B + A·j} with Gaussian-integer coordinates. The
     * readings are its norm, a Gaussian integer, and the two Gaussian integers it is with {@code j} read as
     * {@code i} and as {@code −i}.
     */
    record BicomplexValue(CC point) implements Value {

        @Override
        public Arithmetic arithmetic() {
            return Arithmetic.BICOMPLEX;
        }

        @Override
        public String text() {
            return Bicomplex.write(point);
        }

        @Override
        public Map<String, String> readings() {
            Map<String, String> out = new LinkedHashMap<>();
            out.put("norm", ComplexRatio.gaussian(point.nrm()));
            out.put("j → i", ComplexRatio.gaussian(point.evPlus()));
            out.put("j → −i", ComplexRatio.gaussian(point.evMinus()));
            return out;
        }
    }

    /**
     * A {@link Arithmetic#BICOMPLEX_RATIO} value: the point {@code B + A·j} whose coordinates are ratios of
     * Gaussian integers, as computed. The readings are the bicomplex number it is, its norm, and whether it
     * has an inverse.
     */
    record BicomplexRatioValue(CTC point) implements Value {

        @Override
        public Arithmetic arithmetic() {
            return Arithmetic.BICOMPLEX_RATIO;
        }

        @Override
        public String text() {
            return BicomplexRatio.write(point);
        }

        @Override
        public Map<String, String> readings() {
            Map<String, String> out = new LinkedHashMap<>();
            out.put("value", BicomplexRatio.bicomplexValue(point));
            out.put("norm", BicomplexRatio.norm(point));
            out.put("unit", BicomplexRatio.unit(point));
            return out;
        }
    }

    /**
     * An {@link Arithmetic#IEEE_FLOATING_POINT} value. {@code text} is the shortest decimal that reads back
     * as {@code value}; the readings are the exact value it holds, its hexadecimal form and its bits.
     */
    record IeeeValue(double value, String text) implements Value {

        @Override
        public Arithmetic arithmetic() {
            return Arithmetic.IEEE_FLOATING_POINT;
        }

        @Override
        public Map<String, String> readings() {
            Map<String, String> out = new LinkedHashMap<>();
            out.put("exact", IeeeFloatingPoint.exact(value));
            out.put("hex", Double.toHexString(value));
            out.put("bits", IeeeFloatingPoint.bits(value));
            return out;
        }
    }
}
