package sibarum.cott;

import org.junit.jupiter.api.Test;
import sibarum.cott.projection.Projection;
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
 * Every citation names a declaration cott-lean has, and nothing that computes a value goes uncited.
 *
 * <p>The declarations are cott-lean's {@code declarations.txt}, found through the system property
 * {@code cott.lean.declarations}, which the build points at the sibling checkout.
 */
class CitationTest {

    private static final Set<String> EXEMPT = Set.of("equals", "hashCode", "toString", "values", "valueOf");

    /** Packages whose tests state theorems. */
    private static final Set<String> THEORY = Set.of("sibarum.cott.traction", "sibarum.cott.projection");

    @Test
    void everyCitationIsALeanDeclaration() throws Exception {
        Set<String> declarations = declarations();
        List<String> dangling = new ArrayList<>();
        int citations = 0;
        for (Class<?> c : concat(classes(T.class), classes(CitationTest.class))) {
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
    void everythingThatComputesAValueIsCited() throws Exception {
        List<String> uncited = new ArrayList<>();
        for (Class<?> c : classes(T.class)) {
            if (c.isInterface() || c.isAnnotation()) continue;
            boolean valueLayer = c.getPackageName().equals(T.class.getPackageName());
            boolean projection = Projection.class.isAssignableFrom(c);
            for (Method m : c.getDeclaredMethods()) {
                if (m.isSynthetic() || m.isBridge() || EXEMPT.contains(m.getName()) || m.isAnnotationPresent(Lean.class))
                    continue;
                if (valueLayer && Modifier.isPublic(m.getModifiers()) && Modifier.isPublic(c.getModifiers()))
                    uncited.add(c.getSimpleName() + "." + m.getName() + " cites no definition");
                if (projection && m.getName().equals("apply"))
                    uncited.add(c.getSimpleName() + ".apply cites no map");
            }
        }
        for (Class<?> c : classes(CitationTest.class)) {
            if (!THEORY.contains(c.getPackageName()) || !c.getSimpleName().endsWith("Test")) continue;
            for (Method m : c.getDeclaredMethods())
                if (m.isAnnotationPresent(Test.class) && !m.isAnnotationPresent(Proves.class))
                    uncited.add(c.getSimpleName() + "." + m.getName() + " states no theorem");
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

    private static List<Class<?>> concat(List<Class<?>> a, List<Class<?>> b) {
        List<Class<?>> out = new ArrayList<>(a);
        out.addAll(b);
        return out;
    }

    /** Every class compiled into the same output directory as {@code anchor}: main or test. */
    private static List<Class<?>> classes(Class<?> anchor) throws IOException, URISyntaxException, ClassNotFoundException {
        List<Class<?>> out = new ArrayList<>();
        Path root = Path.of(anchor.getProtectionDomain().getCodeSource().getLocation().toURI());
        try (Stream<Path> files = Files.walk(root)) {
            for (Path f : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                String name = root.relativize(f).toString().replace('\\', '.').replace('/', '.');
                out.add(Class.forName(name.substring(0, name.length() - ".class".length()), false,
                        CitationTest.class.getClassLoader()));
            }
        }
        return out;
    }
}
