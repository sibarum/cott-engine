package sibarum.cott.engine.base.rule;

/**
 * What licensed a rewrite.
 * <p>
 * The docs file every claim as Proven, Chosen or Maybe, and that filing is the point of this type: a rewrite
 * carries its justification, so a derivation carries the statuses of everything it leaned on. An answer can
 * then be asked the question the status column exists to answer — whether it depends on anything the theory
 * has not settled — instead of that being reconstructed by hand afterwards.
 *
 * @param name      the rewrite itself, as the docs write it
 * @param reference where it is justified: a primitive's label, or the section that argues for it
 * @param status    how far the theory stands behind it
 */
public record Rule(String name, String reference, Status status) {

    public enum Status {
        /** Follows from the primitives alone. */
        PROVEN,
        /** A choice, kept until disproven. Sound only if the choice is right. */
        CHOSEN,
        /** Evidence but no proof. Held one rank below the primitives. */
        MAYBE,
        /**
         * A number the theory does not construct: a real-valued call, rounded. The one place this engine
         * approximates, and a derivation that contains one is a different kind of answer from one that does
         * not -- so it says so rather than passing as proven.
         */
        APPROXIMATE,
        /**
         * Not settled, and not applied. Carried by the non-rewrite that leaves a term standing, which has to
         * be said out loud: declining silently would let the layer below answer instead.
         */
        OPEN
    }

    public boolean isProven() {
        return status == Status.PROVEN;
    }

    @Override
    public String toString() {
        return name + "  [" + reference + ", " + status + "]";
    }
}
