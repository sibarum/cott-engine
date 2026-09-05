Notation and Terminology
===

### Erasure (∅)

Erasure (∅) is a special rewrite where terms may be removed from an expression.
Information-conservation doesn't mean erasure is illegal,
it means erasure can only be performed under universal invariance.

If not for erasure, you could never solve an equation, because every time you attempt to rearrange
terms, you'd actually be duplicating values and gradually accumulating more residuals as you operate.
That would be duplication of information, which is not conservative.

Erasure is always reversible, although not uniquely so. Without this, algebra wouldn't be possible.
It's what allows you to perform the same operation on both sides of an equation to rearrange and solve.

Erasure is always the result of an operation, never the parameter for one.
It's not a member of the type, it's a rewrite rule notation for discharge.

### Universal Invariance

Universal invariance refers to operational identities.

- `*(x/x) = ∅`
- `+(x-x) = ∅`

See also: Erasure.

Note on `+(x-x) = ∅` under total subtraction. Subtraction is total (E10), so `x - x` is the value
`0` rather than nothing at all. The two are compatible: what `+(x-x) = ∅` says is that *adding*
`x - x` changes nothing, which is "0 is the additive identity" once `x - x = 0`. Read that way it
is load-bearing — it is the step that takes `0 - 0 = 0` to `-0 = 0`, and so the step that makes
negation not multiplication by −1. See equivalence-classes.md, Chosen, and theory-problems.md,
problem 2. If that reading is wrong, that whole conclusion comes back open.

### "A = B"

This is two-way equality, up to universal invariance.

### "A --> B"

A implies B.

### "A ~= B"

A and B have the same projection. This is informal and contextual, for illustrative purposes only.

### "A != B"

Not equal.
