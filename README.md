# cott-engine

Traction Theory as a direct evaluator. Pure Java: no subprocess, no native code, no reflection.

**This repository is mid-rewrite.** The carrier, the term language and the evaluator have been
deleted and are being replaced. The syntax layer survives and still needs retargeting. It does
not currently compile. See [docs/IMPLEMENTATION-PLAN.md](docs/IMPLEMENTATION-PLAN.md).

## What changed and why

The previous engine represented a value as `pt(k, xp(grade, twist, torsion))` — a multiplicity
over a three-slot exponent, with both slots closed in their canonical constructors. Two of those
decisions turned out to be load-bearing in the wrong direction:

- **Rationals reduced.** The torsion slot existed only to work around it: `0/2` is a root of the
  residue zero, but as a reduced rational it collapses to plain `0`, so it needed a slot of its
  own. A non-reducing coordinate pair makes `(0,2)` and `(0,1)` different objects and deletes the
  slot and its machinery outright.
- **The exponent was bounded.** An exponent that fell outside the triple was "not merely
  unreduced, it is unrepresentable." But `0·x = 0^(1+u)` puts arbitrary traction values in
  exponents, so exponents have to nest.

The replacement carrier is a projective rational — a numerator/denominator pair kept exactly as
it arises, never reduced — with a traction being a `base^exponent` pair over expressions.

## The theory

Traction Theory is an implementation of COTT: a type is the closure over total, reversible
operations. The docs are the specification, and they are honest about status — every claim is
filed as Proven, Chosen, Maybe, or False, and the open problems are listed rather than papered
over.

- [what-is-traction.md](docs/what-is-traction.md) — the theory, and what omega is
- [what-is-a-supertype.md](docs/what-is-a-supertype.md) — why the carrier is subtracted from, not built up
- [notation-and-terminology.md](docs/notation-and-terminology.md) — erasure, universal invariance, the relations
- [equivalence-classes.md](docs/equivalence-classes.md) — the primitives, and every claim with its proof and status
- [rule-combinations.md](docs/rule-combinations.md) — the full operation table; this is the spec for `simplify()`
- [derivations.md](docs/derivations.md) — worked derivations
- [theory-problems.md](docs/theory-problems.md) — what is open, and what each open question blocks

The central unresolved term is `0·w`. It is the one product with zero whose exponent sum is an
erasure, and `0^(0w)` is the same question wearing different clothes.

The power law `(0^u)^v = 0^(uv)` was withdrawn on 2026-09-05, keeping E10 and total subtraction
instead — the branch the author's ℚ model satisfies, and so the branch known to be consistent.
What survives is the integer case, which is a theorem of E1 rather than a law of its own. The
cost is that `x^v` has no rule at non-integer `v`, and that `1^w`, `(-1)^0` and `x^0` are no
longer the same question as `0·w`. What it buys is that negation is not multiplication by −1.

## Build

```bash
mvn test
```
