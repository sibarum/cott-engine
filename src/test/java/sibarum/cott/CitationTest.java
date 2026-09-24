package sibarum.cott;

import org.junit.jupiter.api.Test;
import sibarum.cott.traction.Lean;
import sibarum.cott.traction.Proves;
import sibarum.cott.traction.T;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Every citation names a declaration cott-lean has, and nothing in the value layer goes uncited.
 *
 * <p>The declarations are cott-lean's {@code declarations.txt}, found through the system property
 * {@code cott.lean.declarations}, which the build points at the sibling checkout.
 */
class CitationTest {

    private static final Set<String> EXEMPT = Set.of("equals", "hashCode", "toString", "values", "valueOf");

    @Test
    void everyCitationIsALeanDeclaration() throws Exception {
        Set<String> declarations = declarations();
        List<String> dangling = new ArrayList<>();
        int citations = 0;
        for (Class<?> c : classes()) {
            for (AnnotatedElement e : elements(c)) {
                for (String name : cited(e)) {
                    citations++;
                    if (!declarations.contains(name)) dangling.add(name + "  (cited by " + describe(c, e) + ")");
                }
            }
        }
        assertTrue(citations > 50, "only " + citations + " citations found: is the class scan working?");
        if (!dangling.isEmpty())
            fail("cited, but not declared in cott-lean:\n  " + String.join("\n  ", dangling));
    }

    @Test
    void theValueLayerIsCitedThroughout() throws Exception {
        List<String> uncited = new ArrayList<>();
        for (Class<?> c : classes()) {
            if (!c.getPackageName().equals(T.class.getPackageName()) || c.isAnnotation()) continue;
            boolean test = c.getSimpleName().endsWith("Test");
            for (Method m : c.getDeclaredMethods()) {
                if (m.isSynthetic() || EXEMPT.contains(m.getName())) continue;
                if (test) {
                    if (m.isAnnotationPresent(Test.class) && !m.isAnnotationPresent(Proves.class))
                        uncited.add(c.getSimpleName() + "." + m.getName() + " states no theorem");
                } else if (Modifier.isPublic(m.getModifiers()) && Modifier.isPublic(c.getModifiers())
                        && !m.isAnnotationPresent(Lean.class)) {
                    uncited.add(c.getSimpleName() + "." + m.getName() + " cites no definition");
                }
            }
        }
        if (!uncited.isEmpty()) fail(String.join("\n", uncited));
    }

    // ----

    private static Set<String> declarations() throws IOException {
        String prop = System.getProperty("cott.lean.declarations");
        Path path = Path.of(prop != null ? prop : "../cott-lean/declarations.txt");
        if (!Files.exists(path))
            fail("cott-lean's declarations.txt is not at " + path.toAbsolutePath()
                    + ". Check out cott-lean beside this repository, or set -Dcott.lean.declarations.");
        return new HashSet<>(Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                .map(String::strip).filter(s -> !s.isEmpty()).toList());
    }

    private static List<String> cited(AnnotatedElement e) {
        List<String> out = new ArrayList<>();
        Lean lean = e.getAnnotation(Lean.class);
        if (lean != null) out.addAll(Arrays.asList(lean.value()));
        Proves proves = e.getAnnotation(Proves.class);
        if (proves != null) out.addAll(Arrays.asList(proves.value()));
        return out;
    }

    private static List<AnnotatedElement> elements(Class<?> c) {
        List<AnnotatedElement> out = new ArrayList<>();
        out.add(c);
        out.addAll(Arrays.asList(c.getDeclaredFields()));
        out.addAll(Arrays.asList(c.getDeclaredMethods()));
        if (c.isRecord()) out.addAll(Arrays.asList(c.getRecordComponents()));
        return out;
    }

    private static String describe(Class<?> c, AnnotatedElement e) {
        return e instanceof Class<?> ? c.getName() : c.getName() + "." + e;
    }

    /** Every class compiled from main and test sources. */
    private static List<Class<?>> classes() throws IOException, URISyntaxException, ClassNotFoundException {
        List<Class<?>> out = new ArrayList<>();
        for (Class<?> anchor : List.of(T.class, CitationTest.class)) {
            Path root = Path.of(anchor.getProtectionDomain().getCodeSource().getLocation().toURI());
            try (Stream<Path> files = Files.walk(root)) {
                for (Path f : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                    String name = root.relativize(f).toString().replace('\\', '.').replace('/', '.');
                    out.add(Class.forName(name.substring(0, name.length() - ".class".length()), false,
                            CitationTest.class.getClassLoader()));
                }
            }
        }
        return out;
    }
}
