package sibarum.cott.traction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The cott-lean theorems a test states. The test checks the same fact the theorem proves, on the nine
 * named values and on many other pairs; the proof is what makes it hold for all of them.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Proves {
    String[] value();
}
