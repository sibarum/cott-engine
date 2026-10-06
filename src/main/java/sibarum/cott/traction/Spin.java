package sibarum.cott.traction;

/**
 * The rotation a pair squares to. {@code x ⊗ x} has norm {@code N²}, so it divides by {@code N} exactly, and
 * what is left is a rotation with rational entries:
 *
 * <pre>
 * rot T(p, q)  =  1/N · [ q² − p²   −2pq    ]        N = p² + q²
 *                       [ 2pq        q² − p² ]
 * </pre>
 *
 * <p>Off {@code 0ω} it is exactly a rotation, and it is the rotation by {@code 2θ}. Rotations compose as
 * pairs: {@code rot(x ⊗ y) = rot x · rot y}. Two pairs give the same rotation exactly when they lie on one
 * line through the origin, so {@code x} and {@code T(−p, −q)} share one: the double cover.
 *
 * <p>The Lean's entries are rationals. Here each is returned as the pair it is the ratio of, numerator over
 * {@code N}, unreduced: {@code rotCos T(1,2) = T(3, 5)}. At {@code 0ω} both are {@code 0ω}, where the Lean's
 * rationals are {@code 0}.
 */
public final class Spin {

    private Spin() {}

    /** {@code cos 2θ = (q² − p²)/N}, as the pair {@code T(q² − p², N)}. */
    @Lean({"T.rotCos", "T.rotCos_eq"})
    public static T rotCos(T x) {
        return new T(x.doubleAngle().q(), x.norm());
    }

    /** {@code sin 2θ = 2pq/N}, as the pair {@code T(2pq, N)}. */
    @Lean({"T.rotSin", "T.rotSin_eq"})
    public static T rotSin(T x) {
        return new T(x.doubleAngle().p(), x.norm());
    }
}
