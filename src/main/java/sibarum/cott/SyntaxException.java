package sibarum.cott;

/** An expression the parser could not read. The message is written to be shown to a user. */
public class SyntaxException extends RuntimeException {

    public SyntaxException(String message) {
        super(message);
    }
}
