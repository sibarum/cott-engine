package sibarum.cott.traction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The cott-lean declarations this code implements, by their full Lean names, such as {@code T.plus} or
 * {@code T2.flatten}.
 *
 * <p>Every citation must name a declaration in cott-lean's {@code declarations.txt}, and the build fails
 * on one that does not. So a definition renamed or dropped in the Lean cannot silently survive here.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.TYPE})
public @interface Lean {
    String[] value();
}
