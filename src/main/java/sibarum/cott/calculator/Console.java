package sibarum.cott.calculator;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * On Windows a console reads and writes in its code page, 437 or 850 by default, neither of which has
 * {@code ω}. So the calculator puts the console in UTF-8 for as long as it runs, and puts it back after,
 * which spares the user a {@code chcp 65001} first. Anywhere else, or if the calls fail, it does nothing
 * and the output is UTF-8 all the same.
 */
final class Console implements AutoCloseable {

    private static final int UTF8 = 65001;

    private final MethodHandle setInput, setOutput;
    private final int inputBefore, outputBefore;

    private Console(MethodHandle getInput, MethodHandle getOutput, MethodHandle setInput, MethodHandle setOutput)
            throws Throwable {
        this.setInput = setInput;
        this.setOutput = setOutput;
        inputBefore = (int) getInput.invokeExact();
        outputBefore = (int) getOutput.invokeExact();
        int a = (int) setInput.invokeExact(UTF8);
        int b = (int) setOutput.invokeExact(UTF8);
    }

    /** The console in UTF-8, or an object that changes nothing where that cannot be done. */
    static Console utf8() {
        if (!System.getProperty("os.name", "").startsWith("Windows")) return null;
        try {
            Linker linker = Linker.nativeLinker();
            SymbolLookup kernel32 = SymbolLookup.libraryLookup("kernel32", Arena.global());
            FunctionDescriptor get = FunctionDescriptor.of(ValueLayout.JAVA_INT);
            FunctionDescriptor set = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
            return new Console(
                    linker.downcallHandle(kernel32.find("GetConsoleCP").orElseThrow(), get),
                    linker.downcallHandle(kernel32.find("GetConsoleOutputCP").orElseThrow(), get),
                    linker.downcallHandle(kernel32.find("SetConsoleCP").orElseThrow(), set),
                    linker.downcallHandle(kernel32.find("SetConsoleOutputCP").orElseThrow(), set));
        } catch (Throwable unavailable) {
            return null;
        }
    }

    @Override
    public void close() {
        try {
            // a code page of 0 means there is no console to restore
            if (inputBefore != 0) { int a = (int) setInput.invokeExact(inputBefore); }
            if (outputBefore != 0) { int b = (int) setOutput.invokeExact(outputBefore); }
        } catch (Throwable ignored) {
            // nothing more can be done
        }
    }
}
