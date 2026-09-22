# T(p,q) — Technical Design

## What this document is

This document has one central subject, and it is **§5: how multiplication can be made to stop
destroying coordinates.** Sections 1 to 4 are the setting for it — what the type is, what its two
arithmetics do, and exactly where and how information is lost — because a repair is only meaningful
against a precise account of the damage. §5 is designed and not built; everything before it is
implemented and under test.

Every factual claim below is either a formula from `docs/Traction-Model.md` or a measurement taken
against the code.

**Where the numbers come from.** Every count in §2, §3 and §4 is asserted in
`src/test/java/sibarum/cott/engine/TLawsTest.java` and runs with the suite. They are in the build
rather than in this file's history because a number in a document that cannot be re-run is worse
than no number — nobody can contradict it and it goes stale in silence. If a count here disagrees
with that test, the test is right. The counts are characterisations over the fixed samples named in
it, not universal theorems: `126 of 196` means 126 of the pairs drawn from a 14-element sample.

**§5 is the exception and is marked throughout.** Its claims were checked in a prototype written
during design and not kept, so they are *not* reproducible from this repository. Treat them as a
record of what was intended and apparently worked, to be re-derived before anything is built on
them. The one part of §5 that is a fact about the current type — the embedding, which §5.2 uses to
show why nesting cannot work and §5.6 uses to show why pruning is free — is in the test like
everything else.

It is deliberately *not* written in continuity with any earlier implementation in this project's
history. Concepts, names and laws from previous attempts are not imported here, including where they
look applicable — that inheritance is a known failure mode of this project, where one borrowed idea
quietly brings a dozen assumptions that no longer hold. Everything below is stated in terms of
`T(p,q)` and its own operations.

Three labels are used throughout and are load-bearing:

- **Implemented** — exists in the code and is covered by tests.
- **Designed** — specified and checked in simulation, not in the type.
- **Open** — genuinely undecided. Not a gap to be filled by inference.

Related documents: `docs/Traction-Model.md` is the model's own statement of the operations;
`docs/Traction-Theory.md` is the account of why. This one is the engineering view.

---

## 1. The carrier

**Implemented.** `sibarum.cott.engine.ratio.T` is a record of two `BigInteger` coordinates.

```
T(p, q)
```

Two readings of one object, neither derived from the other:

| reading | meaning |
|---|---|
| ratio | `p ÷ q`, which is `tan θ` |
| point | `q + pi` in the plane, whose argument is `θ` |

The type has **no reduction and no normalisation**. `T(1,2)` and `T(2,4)` are different values that
name the same ratio. `T(0,-1)` is different from `T(0,1)`. Equality is coordinate equality. `q` may
be zero — that is the point of the type rather than an edge case, and it is what makes `0` and `ω`
each other's reciprocal with neither special-cased.

`T(0,0)` is a member. It is the unit of `⊕` (§2), so it must be writable; nothing in the type turns
it into anything else. The reading `0÷0 = 1` by `x÷x` exists in the model's table but is **not
applied by the type** — it is a reading a caller may apply where the choice is visible.

### The nine named values

Eight directions and the centre. In ring order, from the top clockwise, which is also ascending `θ`
on the compass convention the charts use (`θ = 0` at twelve o'clock, growing clockwise):

| name | pair | θ | projection |
|---|---|---|---|
| `0` | `(0,1)` | 0° | `+0` |
| `1` | `(1,1)` | 45° | `1` |
| `ω` | `(1,0)` | 90° | `+inf` |
| `_1` | `(1,-1)` | 135° | `-1` |
| `-0` | `(0,-1)` | 180° | `-0` |
| `-_1` | `(-1,-1)` | −135° | `1` |
| `-ω` | `(-1,0)` | −90° | `-inf` |
| `-1` | `(-1,1)` | −45° | `-1` |
| `0ω` | `(0,0)` | none | `NaN` |

Two pairs of rows share a projection and differ by half a turn (`1`/`-_1`, `-1`/`_1`), and two more
share a tangent of zero (`0`/`-0`). The angle separates all of them; the projection does not. This
is why a client reports both.

---

## 2. Two arithmetics on one carrier

The model gives each position its own addition and multiplication. All four are **implemented**.

```
                value position            exponent position
  addition      T(ad+bc, bd)              T(a+c, b+d)          plus / oplus
  multiply      T(ac, bd)                 T(ad+bc, bd−ac)      times / otimes
  unit          1 = (1,1)  for ·          0ω = (0,0) for ⊕
                0 = (0,1)  for +          0  = (0,1) for ⊗
  inverse       T(b,a) reciprocal         T(-a,-b) for ⊕,  T(-a,b) for ⊗
```

Also implemented: `power(n)` = `T(a^n, b^n)` for `n ≥ 0`, which is `·` iterated, not the n-fold
angle. Negative `n` is refused rather than answered by swapping coordinates, since reaching `T(b,a)`
from `T(1/a, 1/b)` requires multiplying both coordinates by `ab`, and this type does not reduce.

Also implemented: `otimesPower(n)` = `⊗` iterated, which is the model's exponentiation where the
exponent is an integer — the angle scaled `n` times. It is total over negative `n`, unlike
`power(n)`, because `⊗`'s inverse is conjugation and costs no reduction. `doubleAngle()` is the
model's `z(T) = T(2ab, b²−a²)`, which is `otimesPower(2)` on the coordinates and not merely at that
ratio.

Not implemented: the general exponent `T(a,b)^T(c,d) = tan((c÷d)·arctan(a÷b))`. At a non-integer
`c÷d` the tangent it names is not the ratio of any two integers, so there is no pair to return and
none is invented. What should carry that case is the model's to say.

### 2.1 The exponent-position pair is exactly the Gaussian integers

**Measured.** Read the pair as the point `q + pi`. Then `⊕` is componentwise addition and `⊗` is

```
(b + ai)(d + ci) = (bd − ac) + (ad + bc)i
```

which is `T(ad+bc, bd−ac)` exactly. So `(T, ⊕, ⊗)` **is** `Z[i]`, with `(0,0)` as its zero and
`(0,1)` as its one. This is not an analogy; the formulas are equal term by term.

Everything good about those two operations follows from it and was confirmed independently:

- `⊗` distributes over `⊕` **exactly, on coordinates**: identical in 2197 of 2197 triples tested.
- `⊕` never loses information: given a result and one operand, the other is `P − c`, `Q − d`.
  Recovery succeeded in 196 of 196 cases.
- `⊗`'s inverse `T(-a,b)` **is** conjugation, and `x ⊗ conj(x)` is `(0, q²+p²)` — the norm, in the
  denominator slot. Recovery is `c ⊗ conj(b)` divided by that norm, and it is exact because `Z[i]`
  is a Euclidean domain: 182 of 196, failing only where `b` is `(0,0)`.
- The Gaussian units `±1, ±i` are the pairs `(0,1)`, `(0,-1)`, `(1,0)`, `(-1,0)` — which are exactly
  `0`, `-0`, `ω` and `-ω`. The invertible elements under `⊗` are the four points on the axes.

### 2.2 The value-position pair is unreduced fraction arithmetic, and is not a ring

`·` is componentwise, so it is the product monoid on the two coordinates. `+` is the fraction sum
with no common-denominator search — it cross-multiplies whether or not the denominators already
agree, so `1/2 + 1/2` lands on `(4,4)`.

**Measured: `·` does not distribute over `+`.** Over 2197 triples:

| | count |
|---|---|
| identical coordinates | 1141 |
| same value, different coordinates | 720 |
| **different value** | **336** |

The failures are where a zero erases. For example `ω·(0+1)` is `ω·1 = (1,0)`, while
`ω·0 + ω·1` is `(0,0) + (1,0) = (0,0)` — `ω` against the origin, two different values. The 720
middle cases are the non-reduction showing: same ratio reached at different coordinates depending on
the grouping.

So the value-position pair is not a ring on this carrier, and the failure is not an artifact of
representation — it is where information is destroyed.

### 2.3 Laws that do hold

**Measured**, over the nine named values plus a spread of ordinary fractions:

| law | result |
|---|---|
| commutativity, all four | holds (every operation table is symmetric) |
| associativity, all four | holds **exactly on coordinates** — 0 differences in 5832 triples |
| `x + 0 = x` | holds exactly, for every value including `ω`, `-ω`, `0ω` |
| `x · 1 = x` | holds exactly, same |
| `⊗` over `⊕` | holds exactly |
| `·` over `+` | fails, §2.2 |
| `(T^m)^n = T^(m·n)` | holds **exactly on coordinates**, both signs, every pair |
| `T^(m+n) = T^m ⊗ T^n` | holds exactly while the exponents agree in sign; where they cancel it is off by one norm factor per cancelled turn — 1234 of 1694 exact |

For `+`, associativity is identical on both sides algebraically: `(adf + bcf + bde, bdf)`.

### 2.4 Closure over the nine

**Measured.** How much of each operation's output over the nine named values lands back on them:

| | on the nine | distinct results |
|---|---|---|
| `·` | 81 / 81 | 9 |
| `+` | 73 / 81 | 13 |
| `⊗` | 65 / 81 | 13 |
| `⊕` | 49 / 81 | 25 |

`·` is closed because `{−1, 0, 1}` is closed under integer multiplication and the two coordinates
never interact: the product quantises to the 3×3 lattice. `⊗` is rotation, so it keeps results on
the eight spokes but lets them drift outward in magnitude.

---

## 3. Readings, zeros, and every other special case

**Implemented.** Three, deliberately different from each other.

| method | type | at a quarter turn | at `(0,0)` |
|---|---|---|---|
| `evaluate()` | `Optional<Double>` | empty | empty |
| `projection()` | `double` | `±inf` | `NaN` |
| `theta()` | `double` | `±π/2` | `NaN` |

`projection()` is the model table's column: total where `evaluate` is partial, and returning a
double specifically because IEEE has a signed zero and two infinities, which are exactly the four
places the model needs kept apart (`(0,1)→+0`, `(0,-1)→-0`, `(1,0)→+inf`, `(-1,0)→-inf`). It is
still a projection and still loses: `_1` and `-1` both come back `-1`.

Both `projection()` and `theta()` divide the coordinates rather than their doubles. Converting first
and dividing after gives `Infinity/Infinity` as soon as both coordinates outgrow a double — `atan2`
answers that at 45° regardless of the true angle, which made `T(10^400, 10^500)` and
`T(10^500, 10^400)` report the same eighth turn when they are nearly a quarter turn apart.

### 3.1 Every place a zero is treated specially

The arithmetic has **no** zero handling. Every special case is in a reading, and the rest of the
list is deliberate absences — places where one might expect a branch and there is none, which is
itself the design.

| where | at a zero | kind |
|---|---|---|
| the constructor | nothing. `(0,0)` is admitted, no sign is moved, nothing is reduced | absence |
| `+` `·` `⊕` `⊗` | nothing. The formulas run and zeros annihilate as they will | absence |
| `reciprocal()` | nothing. The coordinates swap, so `0 ↔ ω` and `(0,0) ↔ (0,0)` | absence |
| `power(0)` | `T(1,1)` for every pair, the origin included | falls out of `pow(0) = 1` |
| `evaluate()` | `q = 0` → empty, covering both the quarter turn and the origin | rule |
| `projection()` | `(0,0)` → `NaN` | rule |
| `projection()` | `q = 0, p ≠ 0` → `±inf` from p's sign | rule: `p÷q` is *undefined* there, not infinite |
| `projection()` | `p = 0` → `±0` from q's sign | rule: `p÷q` is *exactly 0* there, and unsigned |
| `theta()` | `p = 0, q < 0` → `+π`, not `−π` | tie-break: a zero numerator turns with the positive half |

Two consequences of that list worth stating outright, because both are easy to misread:

**The orientation in `projection()` is placed, not computed.** Three of its four branches are sign
rules. `p ÷ q` genuinely cannot tell `(0,1)` from `(0,-1)` — both are zero — nor `(1,0)` from
`(-1,0)`, where it is undefined. The `-0` and the two infinities are a sign bit carried by IEEE
because IEEE has somewhere to keep one. Off the axes the ratio has nowhere, which is why antipodes
merge there: `(1,1)` and `(-1,-1)` both project to `1.0`, and no rule rescues them.

**The four landmarks are not reserved.** Overflow and underflow land ordinary ratios on them:

```
T(1, 10^400)  -> 0.0         same value and same sign bit as the point zero
T(1, -10^400) -> -0.0        same as -0
T(10^400, 1)  -> Infinity    same as ω
```

So `projects as +inf` does not mean "this is `ω`", and `-0` does not mean "this is `-0`". The
projection is the ratio as an IEEE double with an orientation bit added on the axes — not a faithful
name for a point, and not reversible.

**`evaluate()` and `projection()` disagree at a signed zero.** `evaluate()` divides exactly and an
exact zero has no sign, so `T(0,-1)` evaluates to `0.0` and projects to `-0.0`. Deliberate, and the
reason there are two readings rather than one.

### 3.2 Every special case in the system, by kind

The complete list, from an audit of every branch in `T` and the parser. There are four kinds, and
which kind a case belongs to matters more than the case itself.

**Refusals — it throws.** Two, and both are about input rather than arithmetic.

- `T.power(n)`, `n < 0` — the result is not a pair of integers, and swapping the coordinates
  instead would be a reduction. `reciprocal()` is the operation that was wanted.
- `Parse.of` on text it cannot read — `SyntaxException`, with the character position.

**Declines — it returns nothing, or leaves the term standing.** The largest group, and the one the
system's behaviour mostly consists of.

- `evaluate()` where `q = 0`.
- `fold` on anything that is not a literal with a literal.
- `fold` on a `Power` whose exponent is not a whole number ≥ 0, or whose result would exceed
  `WIDEST` (2²⁴ bits).
- `fold` on a `Negation` under `Folding.STANDING`.
- `resolve` where the resolver declines a call, and `Functions.of` where a name is absent from the
  table or its entry returns null.
- `Parse` on `T(...)` whose arguments are not two whole literals — it stays a `Call`, which is why
  `T` is not a reserved word.
- `T.plus(IExpr)` and `T.times(IExpr)` against a non-`T` operand: a standing node, not an error.

**Placed rules — a value is chosen where the arithmetic supplies none.** These are the ones that
assert something, and so the ones to read sceptically.

- `projection()` at `(0,0)` → `NaN`; at `q = 0, p ≠ 0` → `±inf` from p's sign; at `p = 0` → `±0`
  from q's sign. Only the fourth branch divides. See §3.1 — the orientation here is placed, not
  computed.
- `theta()` at `p = 0, q < 0` → `+π` rather than `−π`.
- `Folding.ORDINARY` → `−T(a,b) = T(−a,b)`, the numerator turning rather than the denominator.
- `Parse.ratio()` → `3.25` becomes `T(325,100)`, the power of ten the digits asked for, unreduced.
- `Parse.whole()` accepts a negated literal, so `T(-1,0)` reads as a literal even though a number
  carries no sign.
- `power(0)` → `T(1,1)` at every pair, the origin included. Emergent rather than written — there is
  no branch, it falls out of `pow(0) = 1` on both coordinates — but it is a definite answer at a
  place where the other readings decline, so it belongs on this list rather than the last one.

**Deliberate absences — no branch where one might be expected, and that is the design.**

- The constructor normalises nothing: `(0,0)` is admitted, no sign is moved, nothing is reduced.
- The four operations have no zero handling at all.
- `reciprocal()` has no branch, so `0 ↔ ω` and `(0,0) ↔ (0,0)` fall out of the swap.
- `fold` applies no identities: `x·1`, `x+0` and `x/x` all stand.
- Nothing folds at parse time.
- The grammar has no juxtaposition rule.

**Shape rules in `Show`** are display only and carry no arithmetic: `Sum(l, Negation(r))` prints as
`l − r`, `Product(l, Reciprocal(r))` as `l ÷ r`, and brackets follow the precedence levels.

**Not in this system:** `Place` and `Projector` do not reference `T` at all. They read the other
carrier in `engine/traction`, and their zero handling — a zero coordinate as an absence marker — is
that carrier's, not this one's. Do not look for `T`'s special cases there or carry theirs back.

### 3.3 `atan2` is division here, and there is nothing left for it to do

**Measured**, in `TLawsTest.theQuotientIsThePairAndItsAngleIsAtan2`.

`atan2` exists because a classical `atan` is handed `y÷x` and the division has already destroyed
the quadrant: `1÷-1` and `-1÷1` are one number, and no function of that number can say which turn
it came from. So the two arguments are carried in separately and the quadrant is rebuilt from their
signs, in four cases plus the axes.

The division here does not destroy it. `T(1,-1)` and `T(-1,1)` are different values, the quotient
of two pairs keeps the placement of the sign it was given, and at whole arguments the quotient
**is** the pair:

```
T(y,1) · T(x,1)⁻¹  =  T(y, x)       for every y and x, including x = 0
```

Its angle agrees with `Math.atan2(y, x)` in 8 of the 9 sign combinations, to the last bit. The
ninth is `0÷0`, which is `0ω` and stands at no angle, where IEEE answers zero — the one place the
two part company, and it is the place `atan2` is inventing.

So `tan` and `atan` in `Standard` have no arithmetic in them at all. A pair is its own tangent, so
what is left of both is the branch — `T.principal()`, the half a classical tangent answers in, taken
by a half turn where the denominator was negative. Both names are that one map, which is the
model's first line stated as code. Applying it is how the quadrant gets lost, and it is opt-in:
nothing folds on the way past.

---

## 4. Where information is lost

This section is the one the extension in §5 exists to address.

**Measured.** Given a result and one operand, can the other be recovered exactly?

| | recovered | fails when |
|---|---|---|
| `⊕` | 196 / 196 | never |
| `⊗` | 182 / 196 | only against `(0,0)` |
| `+` | 154 / 196 | the known operand's **denominator** is zero |
| `·` | 126 / 196 | the known operand has **either** coordinate zero |

**All the irreversibility is in the value position.** `⊗` loses at exactly one point, the ring's own
zero, which no commutative ring avoids — `x · 0 = 0` forgets `x` everywhere in mathematics. `⊕`
loses nothing at all. So the exponent-position pair needs no repair, and everything the extension in
§5 exists for belongs to `·` and `+`.

The counts land exactly on the rule rather than approximately: 5 of the 14 sample values have a zero
coordinate and 5 × 14 = 70 = 196 − 126; 3 have a zero denominator and 3 × 14 = 42 = 196 − 154.

A recovery count on its own only measures the inverse formula that was used, so a companion test,
`everyRecoveryFailureIsGenuineAmbiguity`, constructs for every failing case a second operand giving
the same result. Constructed rather than searched for, because a search over the sample is not
evidence: `(2,1)·(1,0) = (2,0)` has no partner among the fourteen values and is ambiguous anyway,
since every `(2,k)` lands there. Searching would have reported 16 of the 70 as unexplained purely
from sample size.

**The accounting is exact: the number of zeroed slots in a result equals the number of coordinates
that became unrecoverable.** For `·` this is immediate, since slot *i* of the result is zero exactly
when it lost its factor. For `+`, loss happens only when the other operand's denominator is zero,
and then the result's denominator is zero too — one slot vacated, one coordinate lost. For `⊗`,
recovery fails only against `(0,0)`, and then the result is `(0,0)` — two vacated, two lost. So
there is always exactly enough room for what was lost, and never more.

### 4.1 Additive loss has no vacated slot to use

**Measured.** In general, a sum with one operand on the ω axis is

```
T(a,0) + T(c,d) = T(ad, 0)
```

— verified over every combination tried. The `bc` term and the whole denominator are killed by the
zero, so the result stays on the ω axis, keeps `a` scaled by the *other* operand's **denominator**,
and loses the other operand's **numerator** entirely.

```
ω  + 3/4 = (4,0)       the 3 is gone, the 4 survives
2ω + 5/2 = (4,0)
ω  + 0   = (1,0)       0 is still the identity, since d = 1
ω  + ω   = (0,0)       d = 0 as well, so both are lost
```

So `ω` absorbs additively against everything, not only against the additive units, and which operand
you hold decides whether you can invert: from `(c,d)` you recover `a = P ÷ d`, from `(a,0)` you
recover `d` but never `c`. That is §4's rule seen from the inside.
Under `·` the lost factor's own slot is free to hold it; under `+` that slot is occupied by `b·c`
built from the other operand. This is the structural difference between the two, and it is why the
extension below covers `·` and not `+`.

### 4.2 A sign detail that matters

**Measured.** `0 · (-1) = (0,1) = 0`, but `0 · _1 = (0,-1) = -0`.

A zero numerator has no sign to turn, so the sign of a zero arrives through the *denominator*. The
two values that both project to `-1` therefore act differently on zero: `-1 = T(-1,1)` leaves it
alone, `_1 = T(1,-1)` takes it to `-0`. The orientation is doing work here, not bookkeeping.

### 4.3 Non-uniqueness at the units

**Measured**, over `p, q ∈ [-2, 2]`:

```
0 x = 0    solutions:  (-2,1) (-1,1) (0,1) (1,1) (2,1)    — any T(n,1)
ω x = ω    solutions:  (1,-2) (1,-1) (1,0) (1,1) (1,2)    — any T(1,n)
1 x = 1    solutions:  (1,1)                               — unique
```

Only `1` pins its operand. `ω·ω = ω` is in that list.

---

## 5. The held product: how erasure is prevented

**This is the subject of the document.** It is also the one part that is **designed, not
implemented, and not reproducible from this repository** — nothing in `T` holds anything today, and
the traces in §5.7 come from a throwaway prototype that has not been kept. Re-derive before
building.

### 5.1 What has to be repaired, and why rewriting cannot do it

§4 located the damage exactly: `·` loses a coordinate at five of the nine named values, and the
number of coordinates lost always equals the number of slots zeroed. The loss is total rather than
obscured —

```
T(p,q) · T(0,1) = T(0,q)      for every p
```

— so after the multiply, `p` is not somewhere else in the expression waiting to be dug out. It is
not present at all. That is what makes the repair impossible to *derive*: rewriting can only move
information around, and there is none left to move. Any rule that keeps `p` has to be **added**, and
that is forced by the non-injectivity rather than being a failure of ingenuity. The only question a
derivation could settle is how much to add, and the answer runs from nothing to the whole operand
with no arithmetic picking a point on it.

### 5.2 Why nesting alone does not work

The natural first attempt is to push the zero down a level and let the lower level hold what the
upper one drops — write the zero as a nested coordinate and work it out normally:

```
T(p,q) · T(T(0,1), 1) = T( T(p,1)·T(0,1), q ) = T( T(0,1), q )
```

The `p` is gone again, one level down, and the same move is available again below that: an infinite
tower of passing `p` along, which never keeps it and is not computable either.

It fails for a structural reason worth stating, because it rules out the whole family of attempts:
**the nesting is self-similar**, so the same formula applies at every level and therefore the same
erasure applies at every level. That is measurable, and the measurement is already in this document
wearing the other face — the embedding `n ↦ T(n,1)` is a homomorphism, so a nested computation over
unit-denominator coordinates lands exactly where the flat one does. The property that makes pruning
free (§5.6) is the same property that makes nesting useless for preserving anything. A level that
mirrors the level above it cannot record what that level loses.

### 5.3 The rule

When a product would erase a coordinate, **hold** it rather than take it, in the form that does not
evaluate:

```
a · 0    held as    a / ω
```

The crucial point is that this is not an annotation about the product — **it is the product**.
Because `ω` is `0`'s reciprocal, `a/ω` *is* `a·0`; nothing has been invented, the multiplication has
simply been written the way round that keeps `a` visible. Two consequences follow immediately and
neither has to be stipulated:

- It survives pruning. Its denominator is `ω` rather than `1`, so the rule of §5.6.1 does not touch
  it. A mark holding the lost value *over zero* would have been an invention; this is not.
- It releases by ordinary cancellation: `(a/ω) · ω = a`. The thing that lifts a held slot is the
  reciprocal of what held it down, which is available in the model rather than added to it.

A coordinate may therefore be a pair rather than an integer, and the grade of a slot is how deep
that nesting runs.

### 5.4 When to hold

One criterion, not a list of cases:

> **hold the product unless it equals a value that already has a spelling.**

| product | equals | held? |
|---|---|---|
| `a · 1` | `a` | no |
| `1 · 0` | `0` — because `(1,ω)` and `(0,1)` are one value | no |
| `2 · 0` | nothing else in the system | yes → `(2,ω)` |
| `0 · 0` | `0²`, which is **not** `0` | yes → `(0,ω)` |

The last two rows are what make this one rule rather than a list. It is tempting to read the
criterion as "hold unless the swallowed factor was uninformative", which would lump `0` in with `1`
and collapse `(0,ω)` to `0`. That is wrong, and wrong in the direction that destroys the whole
scheme: `1·0` really is `0`, so collapsing it loses nothing, while `0·0` is `0²`, a value the system
does not otherwise have, so collapsing it erases exactly what holding exists to keep.

### 5.5 Powers of zero, and what the grade counts

Holding makes the powers of zero distinct values: `0`, `0²`, `0³` are three things, not one. A slot
at grade *k* is its own value times *k* zeroes, so the grade on a slot **is** the power of zero
standing on it:

```
x · 0 · 0 · 0   =   ((((3,ω),ω),ω), 4)   =   3·0³  over  4
```

Each `ω` cancels one of them. So "how many singularities this value met on its way here" is not an
interpretation laid over the grade — it is what the grade is, and reading it off costs nothing.

### 5.6 Pruning, and the switch

Three rewrites, all lossless, all reversible in both directions, and all optional:

1. `T(n,1) → n` — a coordinate over one is that integer.
2. `(1,ω) → 0` — a unit over ω carries nothing; `(0,1)` and `(1,ω)` are one value.
3. both slots held over the same thing → cancel it, since a common division is common to the ratio.

Rule 1 is safe for a reason that **is** checkable here and is in `TLawsTest`: the embedding
`n ↦ T(n,1)` preserves the only three things the four formulas do to coordinates — add, multiply,
negate — so a coordinate over one is indistinguishable from the integer under anything the
operations can do to it. Turning that pruning off changes how much is written down and never an
answer.

Rules 2 and 3 have that status only in the prototype. They have not been checked for confluence
against each other beyond the cases in §5.7, and rule 3 is a cancellation, which sits oddly beside
"nothing reduces" until someone says why a held division is structure rather than magnitude.

The switch matters as much as the rules. The grade is sometimes the answer — it says how many
singularities a path met — and sometimes noise, and which one it is depends on what is being asked
rather than on the algebra. So pruning is something a caller turns on, not something the type does.

### 5.7 What the prototype showed

```
x = (3,4)

x·0          = ((3,ω), 4)
x·0·ω        = ((3,ω), (4,ω))        prunes to (3,4)
x·ω·0        = ((3,ω), (4,ω))        prunes to (3,4)
x·(0·ω)      = (3,4)

x·0·0·0      = ((((3,ω),ω),ω), 4)    then ·ω·ω·ω  prunes to (3,4)

ω·1 = ω      0·1 = (0,1)      x·1 = x      0·2 = ((2,ω),1)      0·(-1) = ((-1,ω),1)
```

- Both routes reach the **same term**, not merely the same value.
- Grouping agrees.
- The grade rises one per zero met and falls one per `ω`, returning exactly.
- The units stay units: `1` is an identity, and `0` is still absorbed-into rather than absorbing.
- `0·(-1)` is distinct from `0`, so the sign that flat arithmetic drops survives in the mark.

### 5.8 What it does not cover

Three limits, and the first is the one that matters.

**`+` has no rule.** §4.1 is why: under `·` the slot that lost a factor is vacated and free to hold
it, but under `+` that slot is occupied by `b·c`, built from the other operand, so there is nowhere
to put the lost value without inventing a place. `T(a,0) + T(c,d) = T(ad,0)` loses `c` with no room
left. This is a different shape of problem from the one §5.3 solves, not a gap to be filled by
analogy with it.

**`⊕` and `⊗` are untested under holding.** `⊕` loses nothing anywhere, so it may need nothing at
all; `⊗` mixes both slots and will be at least as awkward as `+`.

**The `Z[i]` results in §2 do not automatically transfer.** They are theorems about the flat type
*because its coordinates are the integers*. Holding changes what a coordinate is — `0·0` becomes
`0²` rather than `0`, so the coordinates stop being `Z`. Pairs over a commutative ring inherit
distributivity for formal reasons, but the held coordinate structure is deliberately not a ring,
that being the whole point of not collapsing `0·0`. Distributivity, the absence of zero divisors,
and the Euclidean division that makes `⊗` recovery exact would each have to be re-measured on the
new coordinates. Running the §2 and §4 counts against a held prototype is the first thing to do, not
the last.

---

## 6. Open

Not gaps to be closed by inference. Each of these is a decision that has not been made.

1. **`+` under holding.** No rule. §4.1 is why: the slot that lost a coordinate is occupied by a
   product built from the other operand, so there is nowhere to hold the lost value without
   inventing a place.
2. **`⊕` and `⊗` under holding.** Untested. `⊕` loses nothing so it may need nothing; `⊗` mixes both
   slots and will be at least as awkward as `+`.
3. **Whether the grade relates to the `⊕`/`⊗` pairing.** Untested, and the two claims share only a
   word. Nothing measured connects nesting depth to the exponent position.
4. **What `0ω` is under the extension** — absorbing, the identity, or neither. It depends on rules
   §5.6.2 and §5.6.3 in ways the prototype showed but did not settle.
5. **Negation.** The model gives two inverses, `T(-a,-b)` and `T(-a,b)`, and does not say which one a
   minus sign is. The parser makes it a caller switch (`Folding.ORDINARY` / `STANDING`) rather than
   choosing. Under `ORDINARY`, `−T(0,1)` is `T(0,1)`, so `-0` is not reachable that way.
6. **`Show` and bare reciprocals.** `Product(Reciprocal(a), b)` prints `1÷a · b` and reads back with
   an extra `T(1,1)` factor, which `fold` never removes because it applies no identities. Left
   standing deliberately; it has theory implications rather than being a rendering bug.

---

## 7. Implementation map

| file | what it is |
|---|---|
| `engine/ratio/T.java` | the carrier, four operations, two powers, the doubled angle, the branch fold, three inverses, three readings |
| `parse/Traction.g4` | the grammar: precedence, associativity, accepted glyphs |
| `parse/Node.java` | the term: `Lit`, `Var`, `Call`, `Sum`, `Product`, `Power`, `Negation`, `Reciprocal`, and the four walks over it |
| `parse/Parse.java` | text to term, and `T(p,q)` read as a literal |
| `parse/Functions.java` | what a call means, supplied from outside |
| `parse/Catalogue.java` | the functions a session knows, each with an exact form, a projected one, both or neither |
| `parse/Standard.java` | the pre-installed entries: the model's operations, including the two with no glyph |
| `parse/Folding.java` | the negation switch |
| `parse/Show.java` | term to text, precedence-aware, round-trips |
| `engine/projection/Place.java` | where a settled carrier value lands, as coordinates |
| `engine/projection/Projector.java` | the same in three axes, declining what will not fit |

The parser is generated by ANTLR from the grammar file, which is the readable statement of what a
calculator here accepts.

---

## 8. Working principles

These are constraints the code already follows, recorded so they are not re-litigated.

- **Nothing reduces.** No common factors are cancelled, anywhere, ever.
- **Nothing normalises.** Not the sign, not `T(0,0)`, not a unit denominator.
- **Nothing folds at parse time.** `1+1` parses as a sum of two ones. The shape is what rules match
  on, and a parser that folded would hand everything downstream a term that had lost it.
- **`fold` applies no identities.** It combines a literal with a literal and stops. Not `x·1`, not
  `x+0`, not `x/x` — which invariants may be erased and which must be tracked is the caller's
  context, not the arithmetic's.
- **Erase-versus-track is a switch, not a ruling.** Where the model leaves a choice open, the code
  takes a policy object rather than picking. `Folding` is the existing example.
- **Declining is an answer.** An unresolved call, a term that is not one point, a power too wide to
  hold, a value with no angle: each stands or returns empty rather than being given an invented
  answer.
- **Do not inherit from earlier attempts.** Stated at the top, repeated here because it is the
  constraint most likely to be violated by accident.
