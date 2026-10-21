package sibarum.cott.calculator;

/** Input the calculator reads but cannot answer: an unknown function, a cycle, or notation not yet proven. */
public class CalculatorException extends RuntimeException {
    public CalculatorException(String message) {
        super(message);
    }
}
