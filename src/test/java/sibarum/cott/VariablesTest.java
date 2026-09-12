package sibarum.cott;

import org.junit.jupiter.api.Test;
import sibarum.cott.engine.base.expr.IExpr;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariablesTest {

    private static Set<String> in(String entry) {
        return Variables.of(Cott.derive(entry).to());
    }

    @Test
    void aClosedTermHasNoFreeNames() {
        assertTrue(Variables.isClosed(Cott.derive("2+2").to()));
        assertTrue(Variables.isClosed(Cott.derive("0·w").to()));
        assertTrue(Variables.isClosed(Cott.derive("0^(w÷2)").to()));
    }

    @Test
    void aVariableIsFound() {
        assertEquals(Set.of("x"), in("x^2+1"));
        assertEquals(List.of("x", "y"), List.copyOf(in("x+y")));
        assertEquals(List.of("y", "x"), List.copyOf(in("y+x")), "in the order the user wrote them");
    }

    /** A constant is a name the theory does not construct, not a name the user still owes. */
    @Test
    void aConstantIsNotAVariable() {
        assertTrue(Variables.isClosed(Cott.derive("π").to()));
        assertTrue(Variables.isClosed(Cott.derive("e").to()));
        assertEquals(Set.of("x"), in("π·x"));
    }

    /** Names are found inside a traction's coordinates, not only in the operations above it. */
    @Test
    void aNameInsideACoordinateIsFound() {
        assertEquals(Set.of("x"), in("0^x"));
        assertEquals(Set.of("x"), in("x·0"));
    }

    /** Expand first: what a plot draws is the body, so what it reports is the body's names. */
    @Test
    void aDefinitionReportsTheNamesItsBodyUses() {
        Bindings session = Bindings.EMPTY.define("f(t) = t^2+1");
        IExpr expanded = Cott.derive("f(x)", session).to();
        assertEquals(Set.of("x"), Variables.of(expanded));
        assertFalse(Variables.of(expanded).contains("f"));
    }

    /** A defined value is substituted away, so it is not free. */
    @Test
    void aDefinedValueIsNotFree() {
        Bindings session = Bindings.EMPTY.define("k = 3");
        assertTrue(Variables.isClosed(Cott.derive("k+1", session).to()));
    }
}
