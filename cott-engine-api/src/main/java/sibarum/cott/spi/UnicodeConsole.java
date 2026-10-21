package sibarum.cott.spi;

/**
 * Putting the console the calculator runs in into UTF-8, where it is not already: provided by an OS module through
 * {@link java.util.ServiceLoader} only where there is something to do (Windows, whose consoles default to a code page
 * without {@code ω}). Where none is on the class path, the console is used as it is.
 */
public interface UnicodeConsole {

    /**
     * Put the console in UTF-8 until the result is closed, which puts it back as it was. Returns null when there is no
     * console to change, or it cannot be changed: the output is UTF-8 all the same.
     */
    AutoCloseable utf8();
}
