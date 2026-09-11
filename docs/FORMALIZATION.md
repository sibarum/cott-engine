Traction Theory — a statement for formalization
===

**What this is.** A precise statement of Traction Theory as it currently stands, written for someone who
formalizes mathematics for a living. It is not an introduction and not an advocacy document. Every claim
carries its status, and the open questions are listed rather than smoothed over.

**What is being asked.** Whether the axiom set is consistent; whether it has a model; whether the axioms are
independent; and whether the four questions in §8 have standard answers we have missed. Criticism of the
framing is at least as useful as criticism of the results.

**Context.** The theory is under active development, has an executable implementation (Java, 179 tests), and
has never been formalized. The implementation is evidence of a weak kind — it has not produced a
contradiction — and is described in §10 so that its evidential weight can be judged rather than trusted.

---

## 1. The carrier, and the equality question

The intended carrier `T` contains the rationals and one further element `ω`, characterised by `ω = 1/0`.
Zero is invertible; that is the founding commitment, and it is what puts the theory outside fields, wheels
and meadows (§9).

`T` is currently presented by **coordinates**: a pair `(n, d) ∈ ℤ × ℤ`, with `(0,0)` identified to `(1,1)`
and **no other identification**. That is the whole of the normalization protocol: it does not reduce by a
common factor and it does not move a sign between the slots. So `(1,2)` and `(2,4)` are distinct objects
that both project to `1/2`, and `(0,1)` and `(0,-1)` are distinct objects both of magnitude zero (the second
is `-0`; see §6).

This is a statement about which pairs are *identified*, not about which pairs the operations may *produce*.
An operation is free to put a sign wherever it has one to put — negation at a zero numerator puts it on the
denominator, because there is nowhere else — and that is not the protocol moving it.

**This is the first thing we would like an opinion on.** The axioms in §3 are written with `=`, and there are
two readings:

- **(a) `=` is coordinate equality.** Then `T` is `ℤ × ℤ` with one identification, and several axioms as
  written are false: E10 at `a = b` gives `E(a/a)`, and `a/a` at `a = (2,1)` is `(2,2)`, not `(1,1)`.
- **(b) `=` is value equality**, i.e. `T` is the quotient by `(n,d) ~ (kn,kd)`. Then the axioms read
  correctly, but the non-reduction that the theory insists on is an implementation detail with no
  mathematical content — and the author does not believe that, because the distinction between `(0,1)` and
  `(0,-1)` is meant to carry the orientation of zero.

The implementation takes (a), and the visible consequence is that `(0^(1/3))^3` reduces to `0^(3/3)` and
stops rather than reaching `0^1`. Our suspicion is that the theory wants a setoid — coordinate equality for
matching, value equality for the axioms — and that the two have been conflated throughout. **Is that
coherent, and is it the standard way to present such a thing?**

## 2. Signature

One sort. Operations, with the arity and the domain on which the theory currently defines them:

| operation | arity | total? |
|---|---|---|
| `+` addition | 2 | yes |
| `·` multiplication | 2 | yes |
| `−` negation | 1 | yes |
| `⁻¹` reciprocal | 1 | **yes, including at 0** — this is the point of the theory |
| `^` power | 2 | **no** — see below |
| `log_b` | 2 | **no** — defined only at `b = 0` |

`^` is defined in three disjoint cases and nowhere else:

- `0^a` for any `a` — this is the unary operator `E` of §3, and is where all the content is;
- `x^n` for `n` a nonzero integer — repeated multiplication, and for negative `n` only where
  `x^(-n) = 1/x^n` is granted (at base 0 this is E3; at a general base it is a convention, flagged as such);
- `x^0` for `x ∈ {0, 1, ω, -1}` — a four-element lookup, status Chosen (§7).

So `2^0`, `x^ω`, `2^(1/2)` and `log_2(8)` are all undefined. In a theory whose founding requirement is that
operations be total, this is the largest defect and is open problem 3.

## 3. The exponential operator

The axioms are more legible if `0^(·)` is read not as a binary power but as a distinguished **unary
operator** `E : T → T`, written `E(a) = 0^a`. Then:

```
E1    E(a + b) = E(a) · E(b)         E is a homomorphism (T,+) → (T,·)
E2    E(a ÷ b) = E(a) − E(b)         and carries ÷ to −
E3    E(−a) = 1 / E(a)
E4    E(1) = 0
E5    E(0) = 1
E6    E is injective
E7    E maps {0, 1, -1, ω} onto itself
E8    log_0 = E⁻¹
E9    ω := 1/0
```

# TODO: Refactor E10 --> E2.

**E1 and E10 together are the substance.** `E` carries addition to multiplication and division to
subtraction — it exchanges the additive and multiplicative structure in both directions. E4 and E5 say `E`
swaps the two identities: `E(0) = 1` and `E(1) = 0`.

E10 is asserted **total**, including at `a = b`, where the theory reads `a/a` as the multiplicative identity
and concludes `y − y = E(1) = 0`. (This is where reading (a) of §1 bites.)

**Withdrawn.** An earlier axiom `E2 : (0^u)^v = 0^(uv)` was withdrawn on 2026-09-05, on the grounds that it
was only ever proved for integer `v` — where it is a consequence of E1, since an integer power is repeated
multiplication — and that the branch without it is the one the author's rational model satisfies. Its label
is left vacant to keep the numbering stable.

## 4. Consequences of E6 and E7

`E` restricted to `{0, 1, -1, ω}` is a permutation of a four-element set. Three of its values are fixed:
`E(0) = 1` (E5), `E(1) = 0` (E4), and `E(-1) = 1/E(1) = 1/0 = ω` (E3, E4, E9). The fourth is then forced:

```
E(ω) = -1
```

The theory calls this "the leap" and files it as Chosen rather than Proven, because it is forced only given
E7, which is itself a strong assumption about which four elements are closed. So `E` on those four is the
permutation `(0 1)(-1 ω)` — an involution, and by E8 it is its own inverse there, which is what the theory
means by "the log/exp involution".

## 5. Erasure

The theory's own term for the identities, and a place where a formalizer's opinion would help. Two "universal
invariants" are stated:

```
*(x ÷ x) = ∅        multiplying by x ÷ x changes nothing
+(x − x) = ∅        adding x − x changes nothing
```

Earlier presentations treated `∅` as a metalevel rewriting notation — "erasure is always the result of an
operation, never the parameter for one" — and this caused persistent trouble. The current position is
simpler and we believe it is now just ordinary algebra:

> An erasure discharges to the identity of **its own operation**: `x ÷ x = 1`, `x − x = 0`.

with the rider that the *kind* of an erasure is fixed by the operation it came from and not by the operation
it lands in. This matters because `E` exchanges the two floors: `0 · ω` is a product, so its erasure is
multiplicative and discharges to 1 — even though under E1 it presents as `E(1 + (-1))`, which reads
additive. That reading closed what had been the theory's oldest open problem.

**The identities are invariant under their own operation and nothing else.** `x + 0 = x` and `x · 1 = x`;
`-1` and `ω` are invariant under neither, which is why `1 + ω` has no single value and is retained as a pair.

## 6. Derived results

Each of these follows from §3 alone. `x = E(u)` throughout.

```
ω = E(-1) = 1/0                      E3 at a = 1, with E4 and E9
0 = E(1) = 1/ω
E(u) · 0 = E(1 + u)                  E1 and E4: multiplying by zero shifts the exponent by one
E(u) · ω = E(u − 1)                  E1 and E9
0 · 0 = E(2),  hence 0² ≠ 0          E1 and E6
0 · ω = 1                            §5: y·(1/y) at y = 0
(0^u)^n = E(nu)                      integer n, by repeated multiplication and E1
-0 = 0·(-1) ≠ 0                      §7; distinct from 0 by E6
-0 ≠ ω                               E10 and totality
```

Two of these are worth flagging to a reader as unusual rather than merely unfamiliar:

**`0 · ω = 1`.** Zero times its own reciprocal is one. In a wheel this is `⊥`; here it is the multiplicative
erasure and discharges.

**`0² ≠ 0`.** Squaring zero moves it up the traction axis rather than fixing it. Zero is not idempotent and
not an annihilator, and the theory has no annihilator at all — which is why it also has **no nilpotents**,
despite `E(1/n)^n = E(n/n)`: reaching `E(1)` is not reaching a floor, and `E(4/3)` is still further along.

## 7. Chosen — decided, not proven

Kept until disproven. Anything resting on these inherits their status, and the implementation reports which
of them any given answer used.

```
E(ω) = -1                            the leap; forced by E6 + E7 given the other three (§4)
log_0(x) = E(x), i.e. E(E(x)) = x    forced on the four points; asserted for all x, which is much stronger
-y = y · (-1)                        negation is multiplication by -1; gives -0 = E(1 + ω) ≠ 0
log_a(b) · log_b(a) = 1              the reciprocal law
x^0 on {0,1,ω,-1} is the 4-cycle     0 → 1 → ω → -1 → 0
```

The last is the theory's most recent result and the one most likely to interest a reader, because it is the
first map on those four points that is not an involution. Writing `P(x) = x^0`:

```
x       0     1     ω     -1
P(x)    1     ω     -1     0
```

It is derived from the reciprocal law: `a = b^0` gives `log_b(a) = 0`, hence `log_a(b) = 1/0 = ω`, hence
`a^ω = b`; the map `a ↦ a^ω` is a permutation of the four by E6 and E7; three of its values are known and
the fourth is forced, with `ω ↦ ω` excluded because it would give `log_ω(ω) = ω` against `log_x(x) = 1`.

One consequence with no free parameters:

- `⟨P, E⟩ ≅ D₄`. Ordering the four as `0, 1, ω, -1` — the cycle's own order — as the corners of a square:
  `1/x` is the diagonal reflection fixing `1` and `-1`, `E` an edge reflection, `P` the quarter-turn that
  generates the group, and `P²` the half-turn, since it swaps both diagonal pairs at once. Every operation
  the theory has on the closure set is a symmetry of that square.

Naming the other diagonal `-x`, and the half-turn `-1/x`, is a statement about the projection and not about
`T`. Both need `-0 = 0` and `-ω = ω`: in the theory's own arithmetic `-1/ω = 0^(ω+1) = -0` where `P²(ω) = 0`,
and `-1/0 = 0^(ω-1) = -ω` where `P²(0) = ω`. Downstairs those are equal and the naming is exact; upstairs
`-0 ≠ 0` by E6, and it fails at both. The `1` and `-1` cases hold at either level.

The permutation facts are exact. The geometry is *not* established: `0` and `ω` share a rational projection,
so nothing yet places those four at the corners of anything.

## 8. Open, and the questions we would like answered

**Maybe — the addition law.** `E(a) + E(b) = E(a · b)`, the mirror of E10. If adopted, `E` carries `·` to
`+` as well as `+` to `·`, which would make the exchange of floors complete. Its trouble: the theory's
canonical form for a value is `a + E(b)` with one additive part and one exponential part, and this law
asserts that a sum of *two* exponential parts is a single one. Never stated independently; held one rank
below the axioms.

**Problem 3 — totality of `^`.** §2. Either a general power law returns in some form, or `^` is not a
primitive operation in the sense the theory claims. This is the largest gap.

**Problem 4 — `E(E(x)) = x` off the closure set.** Forced on the four points; asserted everywhere. The
largest single unpaid assumption, though it now has few consumers.

**Problem 5 — two readings.** `0 · ω` is `1` under the rational projection and an erasure in the lift. Both
are correct in their own place, and the theory's position is that an implementation must record which
reading is in force rather than pick one. This is not thought to be a contradiction.

The four questions, in the order we would find answers most useful:

1. **Is `=` coordinate or value equality (§1)?** Does the theory need a setoid, and if so is there a
   standard presentation of "matching is finer than equality" that we should be using?
2. **Is E1–E10 consistent, and is there a model?** We do not have one. The coordinate carrier of §1
   interprets `+ · − ⁻¹` but says nothing about `E`, so it is not a model of the axioms — only of the
   rational part. A model, or a proof that none exists, would settle more than anything else here.
3. **Are the axioms independent?** E3 is asserted separately from E1 on the grounds that deriving it needs
   `a − a = 0`. Is that right, and does anything else collapse?
4. **Is a total `^` achievable** given E1, E10 and E6, or does injectivity together with the exchange of
   floors force a partial power?

## 9. Relation to known structures

Stated so a reader can locate the theory quickly, and so that we can be told if it is a known object under
another name.

**Not a field, and not a ring.** `0` is invertible, so no.

**Not a wheel** (Carlström). Wheels adjoin `⊥ = 0/0` and keep `0 · x ≠ 0`; traction has `0/0 = 1` rather
than a bottom element, and `0 · ω = 1`.

**Not a meadow** (Bergstra–Tucker). Meadows are commutative rings with a total inverse satisfying
`0⁻¹ = 0`; traction has `0⁻¹ = ω ≠ 0`.

**Not the projective line over ℚ.** Coordinates are not taken up to scaling, so `(1,2)` and `(2,4)` differ.
The coordinate carrier is a cover of `ℚP¹` rather than `ℚP¹` itself. And `ω` is not the point at infinity in
the usual sense: the theory insists it has *zero* magnitude and shares zero's projection, differing from
zero in orientation.

**The distinctive claims**, for quick comparison: `0` is invertible; `0² ≠ 0`; `0 · ω = 1`; there are two
distinct elements of magnitude zero (`0` and `-0`); there is no annihilator and no nilpotent; and `x ↦ x^0`
is a 4-cycle on `{0,1,ω,-1}`.

## 10. The implementation, and what it is evidence of

A Java implementation exists (`sibarum.cott:cott-engine`), currently 179 tests green. Its evidential value
should be judged narrowly:

- **It is a rewriting system, not a decision procedure.** Terms reduce by one rule at a time until nothing
  applies; where no rule applies the term stands. "Stands" is the engine's report that the theory has no
  answer, and it is used deliberately rather than as a failure.
- **Every rewrite carries the axiom or the choice that licensed it**, with a status of Proven, Chosen,
  Maybe, Approximate or Open. An answer can therefore be asked whether it depends on anything unproven, and
  answers do report this. There is only one evaluator: the derivation *is* the reduction, so an answer and
  its account of itself cannot disagree.
- **What it has not found:** no contradiction, and no non-terminating reduction since the one described in
  the next point was closed.
- **What it did find**, and this is the honest part: five defects in the *coordinate arithmetic* rather than
  in the axioms. `ω + ω` computed to `1` instead of `2ω` because cross-multiplication breaks at a zero
  denominator; the square of minus one computed to `0` as a consequence; E10 never matched at the four
  points because of a traversal order; `1 + 0` silently answered a question the theory had open; and
  `-0` computed to `0`. Each was a defect in a model, not in the theory — which is the pattern a formalizer
  would expect, and is a reason to want the model of question 2.
- **One reduction cycle was found and closed.** Reading an additive unit as a power of zero made `0 − 1`
  non-terminating: E10 sends it to `E(1/0)`, and E1+E3 read that exponent back as `E(0)/E(1)`, which is
  `E(0 − 1)` again. The resolution was that the additive units `{1, -1}` are not to be read through `E` at
  all, only the multiplicative ones `{0, ω}`. We would like to know whether that restriction is principled
  or whether it is patching over something.

The remaining engineering gap worth declaring: the status labels are hand-written prose that nothing
verifies against behaviour, so a rewrite could in principle cite the wrong justification. Nothing suggests
one does.

## 11. Provenance

The theory is the author's. This document was assembled from `equivalence-classes.md`,
`rule-combinations.md`, `theory-problems.md`, `notation-and-terminology.md` and `what-is-traction.md` in the
same repository, which remain the working record and carry the derivations in full, including several that
were withdrawn. Where this document and those disagree, those are newer.
