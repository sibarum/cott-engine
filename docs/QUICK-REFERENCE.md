Traction Theory — Quick Reference
===

Distilled from the working docs. Every line carries its status. Where `REVIEW.md` would change a line and
the change has not been made, the line is marked `†` and the entry is in the ledger at the end.

**Current as of 2026-09-08, after two changes to the primitives:** E2 is reinstated as the theorem E2′ at
rational exponents, and E10 is derived rather than primitive. Both are recorded in
`equivalence-classes.md`; the other docs have not caught up — see the end of this file.

**Status labels.** `P` Proven from the primitives. `C` Chosen, kept until disproven. `M` Maybe, evidence
but no proof. `O` Open, no rule. Anything resting on a `C` or `M` inherits it.

---

## Notation

```
·  ÷  ^          times, divide, power        typed *  /  ^
−                minus and negation          typed -
ω  π  e  i       omega, pi, e, i             ω typed w
0^a              a traction: zero to an exponent
log(x, b)        the log OF x TO base b      log(x, 0) is the distinguished one
2ω  xy  3(x+1)   juxtaposition multiplies
∅                erasure
```

```
A = B            two-way equality, up to universal invariance
A --> B          A implies B
A ~= B           same projection (informal, illustrative only)
A != B           not equal
```

Throughout: `x = 0^u`, `y = 0^v`. A value is `a + 0^b` — one additive part, one exponential part.

---

## The primitives

```
E1    0^a · 0^b = 0^(a+b)        exponent addition is value multiplication
E2    -- not a primitive --      (0^u)^v = 0^(v·u) at nonzero rational v is a THEOREM of
                                 E1 + E6 (+ E3 at negative v). Proven, as E2′
E3    0^(−a) = 1÷0^a             not derived from E1 — that route needs a − a = 0, an erasure
E4    0^1 = 0
E5    0^0 = 1
E6    0^ is injective            reversibility — and it supplies uniqueness of roots
E7    0^ is closed on {0, 1, −1, ω}
E8    log(·, 0) inverts 0^       primitive in earnest; log to a general base went with E2
E9    ω := 1÷0
E10   -- not a primitive --      0^a − 0^b = 0^(a÷b) is E1 seen through the involution, so it
                                 inherits problem 4's status. Chosen
```

Eight primitives. Both vacated slots keep their numbers — E1 and E3–E10 are cited by number
throughout the docs and the commit history.

**E1 and E10 are one axiom, not two.** `0^` carries `+` to `·` and `÷` to `−` because E10 *is* E1
conjugated by the involution — see the derivation below. E4 and E5 say `0^` swaps the two identities.

---

## Rewrite rules

### Binary — both operands lifted

| term | exponent | rule | status | engine |
|---|---|---|---|---|
| `0^a · 0^b` | `a + b` | E1 | P | wired |
| `0^a ÷ 0^b` | `a − b` | E1 + E3 | P | wired, either side |
| `0^a − 0^b` | `a ÷ b` | E10 | **C** — problem 4; total | wired, status label stale |
| `0^a + 0^b` | `a · b` | — | M | unwired |
| `(0^a)^n` | `a · n` | E1 | P, nonzero integer `n` | wired |
| `(0^a)^v` | `v · a` | E2′ | P, nonzero rational `v` | not wired — needs the exponent sort |

Either side matters: `(1÷y)·x` is `x÷y` and `−x + y` is `y − x`. Matching one order only made
multiplication and addition non-commutative on those terms — both defects are fixed.

`v` must be rational **as a term**, with no ω-component. The old guard was "integer as a term, not as a
value" — the pair `(6,3)` is a coordinate pair, not the integer 2 — and E2′ relaxes it, since `(6,3)` is a
rational exponent and now licensed. `v = 0` stays out: zero copies is the empty product, and without the
exclusion E2′ gives `1^0 = (0^0)^0 = 1` against the cycle's `1^0 = ω`.

### Unary

| term | exponent | rule | status |
|---|---|---|---|
| `1 ÷ 0^a` | `−a` | E3 | P |
| `−(0^a)` | `a + ω` | E1, `−1 = 0^ω` | C |

Negation is "add ω to the exponent". A negative power at a general base — `2^−1` — is a convention, not
a consequence: it stands.

### The exponent zero, on the closure set

| term | value | rule | status |
|---|---|---|---|
| `0^0` | `1` | E5 | P |
| `1^0` | `ω` | the 4-cycle | C |
| `ω^0` | `−1` | the 4-cycle | C |
| `(−1)^0` | `0` | the 4-cycle | C |

Off the closure set there is nothing. `2^0` stands. A lookup on four values is a table, and a table
cannot be checked by reading it — what checks it is the cycle closing.

### The logarithm

| term | value | rule | status | engine |
|---|---|---|---|---|
| `log(0^a, 0)` | `a` | E8 | P | wired |
| `log(0, 0)` | `1` | E8, E4 | P | wired |
| `log(1, 0)` | `0` | E8, E5 | P | wired |
| `log(ω, 0)` | `−1` | E8, E9 | P | wired |
| `log(−1, 0)` | `ω` | the leap | C | not wired |
| `log(x, b)`, `b != 0` | — | went with E2 | O | stands |

E8 reads the four points where the arithmetic rules will not. That is not the axis rule being bent:
inverting is not arithmetic, and nothing anywhere produces a log for this to feed.

---

## Erasure and the identities

```
·(x ÷ x) = ∅          multiplying by x÷x changes nothing
+(x − x) = ∅          adding x−x changes nothing
```

**An erasure discharges to the identity of its own operation:** `x ÷ x = 1`, `x − x = 0`.

**The kind of an erasure is fixed by the operation it came FROM, not the one it lands in.** This is the
column that kept problem 1 open. `0·ω` is a product, so its erasure is multiplicative and discharges to
`1`, even though under E1 it presents as `0^(1 + −1)`, which reads additive. The lift changes the kind.

| term | exponent | erasure at | kind at value level | behaviour |
|---|---|---|---|---|
| `·` | `a + b` | `b = −a` | multiplicative | discharges to `1` |
| `÷` | `a − b` | `a = b` | multiplicative | discharges to `1` |
| `−` | `a ÷ b` | `a = b` | additive | discharges to `0` |
| `+` | `a · b` | `b = 1÷a` | additive | no recognisable value form |

Invariance is indexed **by which operation**, never by position. An exponent is not a privileged slot:
`1+(z−z)` and `1·(z÷z)` are both still `1`.

**Each identity is invariant under its own operation and nothing else.** `x + 0 = x`, `x · 1 = x`. †
`−1` and `ω` are invariant under neither — which is why `1 + ω` has no single value and stands as the
canonical form `a + 0^b` at `a = 1, b = −1`.

**The additive identity is not magnitude-zero.** The erasure form `x − x` adds without effect; the value
`0 = 0^1` is a point of the type with an orientation and a reciprocal. `+(x−x) = ∅` does **not** say the
value `0` is the additive identity, and cannot be used as that. Separating the two jobs is the whole
bookkeeping difference from the classic treatment — it is what epsilon and `dx` are for.

**The two axes do not mix.** `1` and `−1` are the additive units; `0` and `ω` the multiplicative ones. An
additive unit may not be re-read as a power of zero. `1 + ω` stands, `1 + 0` stands. † Cost: `1 + 0` used
to answer `1`.

---

## Derived results — Proven

```
ω  = 0^−1 = 1÷0                E3 at a=1, with E4 and E9
0  = 0^1 = 1÷ω                 E4
1  = 0^0 = 1^1                 E5; and 1^1 is one copy of 0^0
0·x = 0^(1+u)                  E1 + E4. Multiplying by zero shifts the exponent up by one
ω·x = 0^(u−1)                  E1 + E9. Multiplying by omega shifts it down by one
0·0 = 0^2,  so 0^2 != 0        0·x at u=1, then E6
0·ω = 1                        0·x at u=−1: the multiplicative erasure, discharged
1÷0^a = 0^−a                   E3
(0^a)^n = 0^(a·n)              nonzero integer n, by repeated multiplication and E1
(0^a)^v = 0^(a·v)              nonzero rational v — E2′, from E1 + E6 (+ E3 if v < 0)
0^(u÷n)                        THE unique n-th root of 0^u, uniqueness by E6
0^x = ω^(−x),  ω^x = 0^(−x)    nonzero rational x — widened from integer by E2′
```

`−0 != ω` is **not** here any more. Its proof was E10 and totality, which is Chosen now; and `−0` is
defined by the negation rule, which is Chosen too, so no claim about `−0` can be Proven. It sits under
Chosen, where the cleaner proof is `−0 = 0^(1+ω)` against `ω = 0^−1`, needing only `ω != −2`.

**No annihilator, no nilpotents.** Zero annihilates nothing — it has a reciprocal, and `0^1` is one rung
of the traction axis rather than a floor. Reaching it is not dying, and `0^(4÷3)` is still further along.
The word "nilpotent" is withdrawn; what survives is the observation: `0^(1÷n)` is distinct from `0` and
its n-th power is `0`, which puts traction outside the commutative rings wheels and meadows are built on.

---

## The closure set

`0^` on `{0, 1, ω, −1}` is the permutation `(0 1)(−1 ω)` — an involution, and by E8 its own inverse
there. This is what "the log/exp involution" means.

```
x        0    1    ω    −1
0^x      1    0    −1    ω        E5, E4, the leap, E3+E4+E9
x^0      1    ω    −1    0        E5, then the 4-cycle
```

`x^0` is the 4-cycle `0 → 1 → ω → −1 → 0`. It is the first map on those four points that is **not** an
involution, and it has no fixed point — so not `x^0 = 1`.

**⟨`^0`, `0^`⟩ ≅ D₄.** Order the four as the cycle does — `0, 1, ω, −1` — as the corners of a square:

```
1÷x           diagonal reflection, fixes 1 and −1, swaps 0 and ω
0^            edge reflection
^0            quarter-turn — generates the group
(^0)²         half-turn: 0 ↔ ω, 1 ↔ −1
```

Every operation the theory has on the closure set is a symmetry of that square. The permutations are
exact; the **geometry is not established** — `0` and `ω` share a rational shadow, so nothing yet places
the four at the corners of anything. †

---

## Derivations worth having to hand

**ω, and zero's invertibility.**
```
0^−1 = 1÷0^1        E3 at a=1
     = 1÷0          E4
     = ω            E9
```

**0·ω = 1.** The oldest open problem, closed.
```
0·ω = 0^1 · 0^−1    E4 and ω = 0^−1
    = 0^(1 + −1)    E1 — the exponent is an additive erasure
    = 1             but the OPERATION was a product: y·(1÷y), multiplicative, discharges to 1
```

**The leap.** `0^` already 2-cycles `{0,1}`, so by E7 it must permute `{−1, ω}`; `0^−1 = ω` leaves only
```
0^ω = −1            C — forced by E6 + E7 given the other three
```

**E10, which is E1 seen through the involution.** The reason the two mirror each other.
```
0^(a÷b) = log_0(a÷b)              the general involution       C — problem 4
        = log_0(a · 1÷b)
        = log_0(a) + log_0(1÷b)    E1 + E8                     P
        = log_0(a) − log_0(b)      E3 + E8                     P
        = 0^a − 0^b                the involution, twice more   C — problem 4
```
Everything but the involution is Proven, and no E2 is needed — line 4 is E3 + E8, not the log power
law. So E10 is not independent of E1 + E3 + E8, and its status is exactly the involution's: Proven on
the four points, Chosen off them. It does **not** collapse E3, which the derivation uses as an input.

Two open items become one decision. Adopt the general involution and E10 comes free with E1's own
status; decline it and E10 is a four-point fact. There is no third branch. And E10 can no longer
arbitrate against problem 4, since it now depends on it.

**i.** Needs only E1 and the leap. The route through logs is withdrawn with E2.
```
0^(ω÷2) · 0^(ω÷2) = 0^(ω÷2 + ω÷2)     E1
                  = 0^ω               exponent arithmetic
                  = −1                the leap
```
A square root, not *the* square root — `0^(−ω÷2)` squares to `−1` as well. Uniqueness of roots is
exactly what the theory does not supply.

**The x^0 cycle.** From the reciprocal law `log(b, a)·log(a, b) = 1` (C):
```
a = b^0  -->  log(a, b) = 0  -->  log(b, a) = 1÷0 = ω  -->  a^ω = b

a -> a^ω is a permutation of {0,1,−1,ω}: injective by E6, closed by E7 †
    1 -> 0      E5 inverted
    0 -> −1     the leap
    ω -> 1      forced: ω -> ω gives log(ω, ω) = ω against log(x, x) = 1
    −1 -> ω     forced by counting

invert:  0^0 = 1,  1^0 = ω,  ω^0 = −1,  (−1)^0 = 0
```
Note the reciprocal law survives `c = 0`, where the classical version dies: `1÷0` is `ω`, a value this
type has. That is the whole of what the cycle uses.

**−0.** Negation is multiplication by `−1` (C).
```
−0 = 0·(−1)              the canonical form — stop here
   = 0^1 · 0^ω           E1 and the leap
   = 0^(1+ω)  != 0^1     by E6, since ω != 0
```
So `−0` is a magnitude-zero point with the opposite orientation, the way `ω` is. It is **not** `0 − 0` —
that is the additive erasure and a different question entirely. Conflating the two is what made `−0` look
ambiguous.

---

## Status ledger

**Chosen.**
```
log(x, 0) = 0^x, i.e. 0^(0^x) = x forced on four points, asserted everywhere — problem 4.
                                  NOW THE LOAD-BEARING ONE: E10 depends on it
E10  0^a − 0^b = 0^(a÷b)          E1 through the involution; inherits problem 4
−0 != ω                           moved from Proven — its proof was E10
0^ω = −1                          the leap
−y = y·(−1)                       negation is multiplication by −1; gives −0 = 0^(1+ω)
log(b,a) · log(a,b) = 1           the reciprocal law — Proven at rational c by E2′, Chosen
                                  only at c = 0, which is the case the cycle uses
x^0 on {0,1,ω,−1} is the 4-cycle  not promoted by E2′: it needs c = 0
the two axes do not mix
```

**Maybe.** `0^a + 0^b = 0^(a·b)` — the mirror of E10. If adopted, `0^` carries `·` to `+` as well, and
the exchange of floors is complete. Its trouble: the canonical form `a + 0^b` holds *one* exponential
part and this law says a sum of two is one; its erasure cell has no value-level form; and it quietly
makes the value `0` an additive identity (`0^u + 0^1 = 0^(u·1) = 0^u`), which collides with negation.
Maybe against Chosen — the ranking points one way, but nothing forces it.

**Open.**
```
problem 3    no power rule off the RATIONALS — narrowed by E2′. What is left is the
             ω-direction, and that hole is now explained rather than reported: there is no
             product on the exponents to write the rule with. Still a totality gap.
problem 4    0^(0^x) = x off the closure set. THE load-bearing open question now — E10
             depends on it, and so does the Maybe addition law by the same conjugation.
problem 5    two readings — 0·ω is 1 downstairs and an erasure in the lift. Standing, not a
             contradiction: an implementation records which reading is in force.
x^0 = 1^x    refuted conditionally by the cycle (0^0 = 1 against 1^0 = ω), so one rank
x^ω = (−1)^x weaker than the old E2 refutations. Filed Open, not False.
§1           is `=` coordinate equality or value equality? Unanswered, and load-bearing.
```

**Withdrawn with E2, and still withdrawn under E2′.** `1^x = 1 + u`; `0x = 0^(1^x)`;
`(−1)^x = 0^(ω·x)`; `ω·x = 0^((−1)^x)` (its E1 half survives as `ω·x = 0^(u−1)`). All needed `v` general
rather than rational. **Partly back:** `log(0^b, 0^a) = b÷a` wherever `b÷a` is rational — i.e. wherever
`a` and `b` are parallel as exponents. **Not available at all:** `(0^u)^v` at `v` with an ω-component,
and not for want of a rule — there is no product on the exponents.

---

## Traps

- **Match on terms, never on values.** `1·1` is the value 1; `1·(1÷1)` is an erasure. Same class,
  different terms. Canonicalising early destroys what the match needs.
- **Never collapse `0^(0^y)` to `y`** off `{0,1,ω,−1}` without recording the general involution.
- **`0 − 1` does not terminate** if additive units may be read as powers of zero: E10 sends it to
  `0^(1÷0)`, then E1+E3 read that exponent as `0^(0^0 ÷ 0^1)` = `0^(0^(0−1))`, and round again. The fix
  is the axis restriction, not the traversal order.
- **`(−1)·(−1)` and `ω+ω`** are where the coordinate model has bitten hardest: cross-multiplication
  breaks at a zero denominator, sending `ω+ω` to `1` and hence `(−1)²` to `0`. Defects in a model, not
  in the theory — but every one was found there and not in the axioms.
- **`1 + 1÷(−1)` is `−0`, not `0`.** `1÷(−1)` is `(1,−1)`; the numerators cancel and the orientation
  survives. Kept deliberately: the magnitude goes and the sign stays. Do not "fix" it by moving signs
  between the coordinate slots.
- **Retracted twice, do not repeat:** deriving `−0 = 0` from E10 plus "`0` is the additive identity".
  Universal invariance does not supply that premise.

---

## REVIEW.md ledger

Entries marked `†` above are still contested and not applied. The rest are recorded here as
settled, so the two documents can be read against each other.

```
E10                 SETTLED, and not by applying P0-6. E10 is now derived (see above) and
                    filed Chosen, and the derivation EXPLAINS P0-6 rather than conceding it:
                    E10's evidence is four-point because it inherits the involution's domain,
                    and the involution is forced only on four points. The model's refutation
                    relocates to `0^(0^x) = x` off the closure set, which the model refutes
                    too — so the model is consistent with itself about both. The "branch known
                    to be consistent" protection is withdrawn (P1-3: no ℚ model in the repo).
x + 0 = x           P1-1: conditionally true — holds when x dominates 0, fails at x = 0 and at
                    0^a for a >= 1. Asserted in two files, forbidden in a third.
1 + 0 stands        P2-5: over-restrictive. 1 + ω should stand (it is an exponent, not a value);
                    1 + 0 should not.
−0 != ω             APPLIED. Moved to Chosen, with P1-2's proof: −0 = 0^(1+ω) and ω = 0^−1,
                    so it needs 1 + ω != −1, i.e. ω != −2, which holds as ω is not rational.
0^x = ω^(−x)        APPLIED, and widened. "Nonzero" is now said out loud — at x = 0 it would
                    give ω^0 = 1 against the cycle's ω^0 = −1 — and E2′ takes it from integer
                    x to rational x.
the D4 geometry     P0-2: established after all. x^0 on the closure set is the Cayley transform
                    (1+x)÷(1−x) on ℚP¹ with ω = ∞; {0,1,−1,∞} is a harmonic quadruple and D4 is
                    its PGL₂ stabiliser. Gives a unique total extension — prediction 2^0 = −3,
                    orbit {2, −3, −1÷2, 1÷3} — and makes E7 and "no fixed point" theorems.
(^0)² = −1÷x        P0-1: a shadow-level identity. Upstairs it holds at 1 and −1 and fails at
                    0 and ω. Not "checked at all four points".
a -> a^ω            P1-5: its injectivity and closure do not follow from E6 and E7, which are
                    about 0^(·). This is the load-bearing step of the cycle.
every value is 0^a  P0-5: E6 gives uniqueness, not existence. In the model 2 is a value and is
                    not a power of zero, so the rule tables have gaps rather than being
                    exhaustive.
one sort            P0-4: the model needs two — exponents G and values V, with 0^ the iso and G
                    not a subset of V. Then "the axes do not mix" is a typing rule, and the two
                    different things now both written ω (P0-3: the value 1÷0, and the phase
                    direction whose exponential is −1) become impossible to conflate.
ω shares 0's shadow P1-9: it does not. Restate as the reciprocal symmetry of 0 and ∞ on ℚP¹.
```

---

## Not yet caught up

The two changes above are recorded in `equivalence-classes.md` only. These sites still state the
old position:

```
FORMALIZATION.md  §3 lists E10 among the axioms and E2 as withdrawn; §3's "E1 and E10 together
                  are the substance" is now one axiom seen twice; §6 gives `-0 != ω` as "E10 and
                  totality", which is Chosen now; §8 q3 (independence) is partly answered — E10
                  is not independent, E3 still is; §8 q4 (total `^`) narrows to the ω-direction
rule-combinations Row 3 of the binary table reads "Proven, and total" and should read Chosen;
.md               row 5 needs an E2′ row beside it; "Gone with E2" needs the same split as
                  equivalence-classes.md's rewritten section
theory-problems   problem 3 narrows to the rationals; problem 4 should say it is now
.md               load-bearing for E10; problem 2's route (i) is Chosen throughout
derivations.md    sqrt(-1) can be cashed out again: (-1)^(1÷2) = (0^ω)^(1÷2) = 0^(ω÷2). The note
                  saying it cannot is out of date
TractionRules     DIFFERENCE is declared PROVEN with reference "E10, total". Both are now wrong,
.java:50          and this one matters: every answer using subtraction currently reports a
                  provenance it does not have. Changing it to CHOSEN flips
                  DerivationTest.anAnswerFromThePrimitivesAloneSaysSo and any assertion that a
                  subtraction is proven — a small change with a test tail
```
