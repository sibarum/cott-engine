# cott-engine

Traction Theory as a direct evaluator. Pure Java: no subprocess, no native code, no reflection.

The carrier is the traction pair of [docs/Traction-Theory.md](docs/Traction-Theory.md): a value is
`n·0^t`, a real part and a traction part. `mvn test` is 196 green.

## What the carrier is

```
1 = (1,  0)      0 = (0,  1)      ω = (0, -1)      -1 = (-1, 0)
```

A real part and an exponent, both of them **expressions** rather than coordinates. Two things force
that. The exponent has to nest — `0^(0^2)` is a term, and `0^(1+ω)` stands — and a term the theory
has not resolved has to be *representable*: `-1·0` is the pair `(-1, 1)` and there is no rule for
it, which a closed two-coordinate carrier could not express, because it would have to give that
pair some value.

**A zero coordinate is the absence marker**, in either slot: a zero exponent is an absent traction
part since `0^0` is 1, and a zero real part is an absent real part. Zero is the only number that can
say so — one is a coefficient a term could actually have, and a marker that is a value gets absorbed
into the arithmetic. So the operations skip an absent coordinate rather than computing with it, which
is what keeps `2·0 = (2, 1)` from annihilating its 2, and `(0,0)` is erasure, discharging at once to
`(1,0) = 1` multiplicatively or `(0,1) = 0` additively. That last one is `0·ω`.

The coordinates are ordinary rationals, kept exactly as they arise and never reduced, and **a
denominator is never zero**. That last one is the whole difference from the carrier before this
one. Omega used to live in the coordinates as the rational `(1, 0)`, because a coordinate pair was the
only thing that could hold `1÷0`; it does not need to, since `ω` is `0^-1` and the pair spells that
directly. What that bought:

- **`ω+ω` is `2ω`** and **`(-1)·(-1)` is `1`**. Both used to break on cross-multiplication across a
  zero denominator, which is the defect `docs/QUICK-REFERENCE.md` records as having bitten hardest.
- **`ω÷2` is half of omega.** It used to be omega — a zero denominator absorbed the factor — which
  is also why `i = 0^(ω÷2)` had no form. It has one now; what it still lacks is uniqueness of roots.
- **`-0` stops being a literal.** It is `-1·0`, the pair `(-1, 1)`, and the sign lives in the real
  coordinate rather than on a denominator. The three-way disagreement about `-0` is down to one
  answer: the product, unresolved.
- **`2·0` keeps the 2.** The absence marker is skipped rather than multiplied, so the 2 survives and
  `0·0^2` is `0^3` -- computing the marker instead would annihilate, and dividing by zero would then
  prove 2 = 1.

## What the engine answers

Every rewrite carries the rule that licensed it and that rule's status, so an answer can be asked
whether it depends on anything the theory has not settled:

```
0·ω     => 1              proven — the multiplicative erasure, discharged
0-1     => -1             proven
2·0+0   => 3·0            proven — distributivity, not the identity
0^2-0^3 => 0^(2÷3)        E2
w^w     => 1              the unit exponentiation table  [CHOSEN]
log(-1, 0) => ω           the unit logarithm table  [CHOSEN]
-1·0    => -0             stands: no rule, and the conjecture -1·0 = ω is refuted in TractionRules
0^(0^2) => 0^(0^2)        stands: the general involution is problem 4
2^0     => 2^0            stands: x^0 is a 4-cycle on the four points and reaches nothing else
```

## The theory

Traction Theory is an implementation of COTT: a type is the closure over total, reversible
operations. The docs are the specification, and they are honest about status — every claim is
filed as Proven, Chosen, Maybe, or False, and the open problems are listed rather than papered
over.

- [Traction-Theory.md](docs/Traction-Theory.md) — the statement of the theory this engine implements
- [QUICK-REFERENCE.md](docs/QUICK-REFERENCE.md) — every rule with its status, and the ledger
- [REVIEW.md](docs/REVIEW.md) — an outside reading, and the model behind it
- [what-is-traction.md](docs/what-is-traction.md) — the theory, and what omega is
- [what-is-a-supertype.md](docs/what-is-a-supertype.md) — why the carrier is subtracted from, not built up
- [notation-and-terminology.md](docs/notation-and-terminology.md) — erasure, universal invariance, the relations
- [equivalence-classes.md](docs/equivalence-classes.md) — the primitives, and every claim with its proof and status
- [rule-combinations.md](docs/rule-combinations.md) — the operation table
- [derivations.md](docs/derivations.md) — worked derivations
- [theory-problems.md](docs/theory-problems.md) — what is open, and what each open question blocks

Four things in `Traction-Theory.md` are not wired, and
[docs/IMPLEMENTATION-PLAN.md](docs/IMPLEMENTATION-PLAN.md) says why: the traction addition law, the
general involution, the carrier's negation rule (which contradicts the doc's own four-unit table),
and `-1·0`.

## Build

```bash
mvn test
```
