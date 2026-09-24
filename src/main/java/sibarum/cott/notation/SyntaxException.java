package sibarum.cott.notation;

/** Input that is not an expression or a definition, with the offset where reading stopped. */
public class SyntaxException extends RuntimeException {

    private final int position;

    public SyntaxException(String message, int position) {
        super(message + " (at " + position + ")");
        this.position = position;
    }

    public int position() {
        return position;
    }
}
