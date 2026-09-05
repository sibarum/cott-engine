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

### By which operation, not where

The invariance is indexed by the **operation**, not by a position in the expression. The question
is never "where may I put `z-z`", it is "by which operation may I apply it". Illustrated in the
reals, where `x + 3x` is the expression and the classic readings apply:

```
x·(z-z) + 3x^(z-z)                              both change the value: 0, and 1
x+(z-z) + (3+(z-z))·x^(1+(z-z))    = x + 3x     added anywhere, including into an exponent
x·(z/z) + (3÷(z/z))·x^(1·(z/z))    = x + 3x     multiplied anywhere, likewise
```

So an exponent is not a slot of one privileged kind. It holds a value, and a value takes both
invariants: `1+(z-z)` and `1·(z/z)` are both still `1`. What decides the outcome is which
operation carried the erasure form in, and whether that is the form's own operation.

And by reversibility, each of these runs backwards: erasure in reverse is how a term is
**injected** without disturbing equality. An identity that erases in one direction licenses the
injection in the other.

### The additive identity is not magnitude-zero

Practically, this treatment of erasure agrees with the classic one. The difference is
bookkeeping: it keeps the **additive identity** — the thing that adds without effect, which is
the erasure form `x-x` — separate from **magnitude-zero**, the point `0 = 0^1`, which is a value
of the type with an orientation and a reciprocal. Classically `0` does both jobs at once, and
separating them is precisely what epsilon and `dx` are for.

So `+(x-x) = ∅` is a statement about the erasure form. It does NOT say that the value `0` is the
additive identity, and it cannot be used as that. See theory-problems.md, problem 2, for what
does and does not follow — the two were conflated there once, and the retraction is recorded.

### "A = B"

This is two-way equality, up to universal invariance.

### "A --> B"

A implies B.

### "A ~= B"

A and B have the same projection. This is informal and contextual, for illustrative purposes only.

### "A != B"

Not equal.
