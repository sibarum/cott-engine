External Review — 2026-09-08
===

A review of Traction Theory as it stands in this repository, by an outside reader working from the
nine docs. Nothing here has been applied. Each item says what to change, where, and why.

**How to read this.** Part 1 is a model and is the only part that has to be read in one sitting —
most of the rest follows from it. Part 2 lists what the model confirms, which is more than it
refutes. Part 3 is the proposed changes, ordered. Part 4 lists objections the reviewer raised in
discussion and then withdrew; they are recorded so they are not chased. Part 5 is what is still
open. Part 6 is prior art.

**Status labels.** `P0` changes a stated result. `P1` is a contradiction between two documents.
`P2` is precision or wording. `MODEL` means the finding is relative to Part 1 and stands or falls
with it.

---

## Part 1 — A model

The theory has one, and it was reachable from `0^(w/2) = i`. §8 question 2 asks for exactly this.

### The construction

Let `G = Q·1 + Q·w` be a formal two-dimensional Q-space — the **exponents**. Let

```
V = Q x Q      the values, written (m, s), with multiplication being addition of pairs
```

and read `(m, s)` as the leading term `t^m · e^(i·pi·s)`, where `t` is a formal infinitesimal.
Define `0^(m + sw) = (m, s)`. Then:

```
0    = (1, 0)         1  = (0, 0)         w  = (-1, 0)
-1   = (0, 1)         i  = (0, 1/2)       -0 = (1, 1)
```

The exponent's `1`-direction carries **order of vanishing**; its `w`-direction carries **phase**.
`w` is `i·pi`.

The projection composed with `0^`, call it `f`, is then forced rather than chosen:

```
f(sw) · f(tw) = f((s+t)w),  f(w) = -1,  f(w/2) = i   ==>   f(sw) = e^(i·pi·s)

f(m + sw) =   0            m > 0
              e^(i·pi·s)   m = 0
              infinite     m < 0
```

### What the axioms become

- **E1** is the multiplication rule, by construction.
- **E6** is the statement that `s` is a rational rather than a residue mod 2 — that is, that `V` is
  the **universal cover** of the monomial group, never forgetting the winding number. This is
  information conservation stated exactly, rather than as a motivation.
- **E8** is the inverse of the iso `G -> V`.
- **E9** and the leap concern different objects. See P0-3.

### Two levels, and which operations live where

```
upstairs    V = Q x Q, the cover              multiplication: total group.  addition: none.
downstairs  monomials c·t^m in a Hahn field   multiplication: total.        addition: partial.
projection  (m, s) |-> t^m e^(i·pi·s)         collapses s mod 2, and every m > 0 to 0
```

`G` is **not** a subset of `V`. That is a sort distinction, and it is the whole content of "the two
axes do not mix" (Part 2).

Downstairs, addition is inherited from the Hahn field and is defined **except at exact
cancellation**. Cancellation produces the field's own zero, which is not a monomial and so is not a
value. That is the erasure, and notation-and-terminology.md's "it's not a member of the type" is a
theorem here rather than a stipulation.

### The one-line diagnosis

> Everything the docs have resolved is multiplicative. Everything they have open is additive.

```
resolved   0·w = 1, 0·x, w·x, no nilpotents, i, the axes          all multiplicative
open       x + 0 = x, the addition law, 1 + w, 1 + 0, E10,
           the involution off the closure set                     all additive
```

`V` is a group under multiplication and has no addition of its own. One explanation, eight
problems — and it predicts that work on the additive side will not close them without either
restricting `+` to `G` or giving up E6.

---

## Part 2 — What the model confirms

Listed first because it is the larger list, and because several of these are items the docs hold
under Chosen or Maybe that the model makes theorems.

| claim | in the model |
|---|---|
| `0^2 != 0` | `(2,0) != (1,0)` — order of vanishing 2 against 1 |
| `0·w = 1` | `(1,0) + (-1,0) = (0,0)` |
| `0·x = 0^(1+u)`, `w·x = 0^(u-1)` | pair addition |
| no annihilator, no nilpotents | every pair is invertible; negate it |
| `-0 != 0`, same shadow | `(1,1)` against `(1,0)`; both have modulus zero |
| `-0 != w` | `(1,1)` against `(-1,0)` |
| `-0` is `0·(-1)` and **not** `0 - 0` | `-t` against exact cancellation. Problem 2 is right |
| the additive identity is not magnitude-zero | two distinct zeros. notation-and-terminology is right |
| `0^(1/n)` is an n-th root of `0` | `(1/n, 0)`; n copies give `(1,0)` |
| `i` needs no adjoining | `(0, 1/2)`. It was always on the phase axis |
| `0^(-w/2)` also squares to `-1` | `-i`. derivations.md is right |
| every root of unity is present | `0^(2kw/n) = e^(2·pi·i·k/n)` |
| `log_0(-1) = w` | `log(-1) = i·pi` |
| erasure is not a member of the type | exact cancellation leaves the monomial set |
| the two axes do not mix | `G` is not a subset of `V`. A sort fact |
| four-point closure, `D4`, `1/x` as a reflection | downstairs only — see P0-1 |

Two of these deserve saying out loud, because the docs are less sure of them than they need to be.

**"The two axes do not mix" is a theorem, not a policy.** equivalence-classes.md argues for it from
cost — "It is forced by what the mixed reading costs." It does not need that argument. `1 + w` is a
well-formed **exponent** and simply not a value, because `G` is not a subset of `V`. The engine
already enforces the same distinction and the docs call it an inherited wart: rule-combinations.md,
"`0^log_0(x)` is a sort error, because the `^` slot is checked for a value and a log returns an
exponent." That is the sort discipline being correct.

**Problem 2's older position was the right one.** The model has two zeros: the field zero, which is
the additive identity and is not a value, and `t = 0^1`, which is magnitude-zero, invertible, and is
a value. So notation-and-terminology.md's central distinction holds, and `0 - 0` really is a
different question from `-0`. What does not hold is problem 2's resolution in its unrestricted
form — see P1-1.

---

## Part 3 — Proposed changes

### P0-1 — `P^2 = -1/x` is a shadow-level identity, not a fact about T

**Where.** FORMALIZATION.md §7 ("Two consequences with no free parameters, both of which hold");
equivalence-classes.md, Chosen, "THE SQUARE OF IT IS A FREE PREDICTION … it holds at all four".

**The problem.** Checked in the theory's own arithmetic, using E1 + E3 and the negation rule:

| x | `P^2(x)` | `-1/x` upstairs | verdict |
|---|---|---|---|
| `1` | `-1` | `0^(w-0) = 0^w = -1` | holds |
| `-1` | `1` | `0^(w-w) = 0^0 = 1` | holds |
| `w` | `0` | `0^(w+1) = -0` | fails upstairs; holds under the projection |
| `0` | `w` | `0^(w-1) = -w` | fails upstairs; holds in QP^1, where `-inf = inf` |

So the identification holds at two of four points upstairs. FORMALIZATION.md §7 ships it as
"Checked at all four points," which it is not, and equivalence-classes.md asserts both that it
"holds at all four" and, twenty lines later, that it "FORCES `-0 = 0` and `-w = w` … against the
negation rule above." Those cannot both be true of the same level.

**Proposed.** State the level. `P^2 = -1/x` on the projection; upstairs it holds at `1` and `-1`
and fails at `0` and `w`. Drop "free prediction" and "One disagreement anywhere would end the
cycle, and there is none" — there is no disagreement at two of the points because there is nothing
there to disagree with.

**What it costs.** Less than it looks: the `x^0` cycle survives intact as shadow-level structure.
See P0-2.

**What it gains.** It dissolves problem 2's "second line of evidence" outright. The half-turn forces
`-0 = 0` **downstairs** (the projection of `0^(1+w)` is `0`); the negation rule says `-0 != 0`
**upstairs** (`0^(1+w) != 0^1` by E6). Both are true, at different levels, and were only ever in
conflict because there was no index to separate them. One of the two independent lines of evidence
problem 2 says "point the same way" turns out to point nowhere.

### P0-2 — the `x^0` cycle is the Cayley transform, and the result improves

**Where.** equivalence-classes.md, Chosen, the `x^0` block; FORMALIZATION.md §7;
rule-combinations.md, "The exponent zero, on the closure set".

**The finding.** `P(x) = x^0` on the closure set is the Mobius map `z |-> (1+z)/(1-z)` on QP^1,
with `w = inf`:

```
P(0) = 1/1 = 1      P(1) = 2/0 = w      P(w) = -1      P(-1) = 0/2 = 0
```

Its inverse `(z-1)/(z+1)` is the Cayley transform, and the standard references state the fact
directly: the Cayley transform permutes `{1, 0, -1, inf}` in sequence. `P^2 = -1/z` is then an
algebraic identity, not a prediction:

```
P(P(z)) = (1 + (1+z)/(1-z)) / (1 - (1+z)/(1-z)) = 2/(-2z) = -1/z
```

And `{0, 1, -1, inf}` has cross-ratio `-1` — it is a **harmonic quadruple** — whose setwise
stabiliser in PGL_2 is `D4` of order 8. A quadruple in general position gets only the Klein
four-group. So the `D4` in §7 is that stabiliser.

**Proposed additions, none of which weaken the result.**

1. **The geometry is established.** §7 says "nothing yet places those four at the corners of
   anything" and equivalence-classes.md says "the square is combinatorial until something says
   otherwise." Something does: the four are the harmonic set on QP^1 and `D4` is its stabiliser.
2. **`x^0` has a unique total extension**, which answers problem 3 at `v = 0` everywhere rather
   than on four points: `x^0 = (1+x)/(1-x)`. Prediction: `2^0 = 3/(-1) = -3`, with orbit
   `{2, -3, -1/2, 1/3}` — check `P(-3) = -2/4 = -1/2`, `P(-1/2) = (1/2)/(3/2) = 1/3`,
   `P(1/3) = (4/3)/(2/3) = 2`. This is the highest-value test in this document.
3. **"`x^0` has no fixed point" becomes a theorem everywhere.** `P(x) = x` gives `x^2 + 1 = 0`,
   with no rational solution. The docs currently observe this on four points.
4. **E7 becomes a theorem.** `P` has order 4 with no fixed points, so it partitions QP^1 into free
   4-element orbits, and `{0, 1, w, -1}` is precisely the `P`-orbit of `0` — since `-1/0 = w` and
   `-1/1 = -1`. A much stronger reason for the closure set than "this is what 'the type is a
   closure' means."
5. **The cycle requires value equality.** In the non-reducing carrier, `1^0` goes through
   `(1+1) / (1-1) = (2,1)·(1,0) = (2,0)`, and `w = (1,0)`. Under coordinate equality the second
   step lands one scaling off `w` and `P^4` never returns to `0`. So the cycle and §1's reading (a)
   are not both available. This is a cleaner statement of §1 question 1 than the setoid framing.

**The open problem this leaves, well-posed for the first time.** Does the cycle lift? Is there an
`x^0` on `V` — total, respecting E6 — whose shadow is the Cayley transform? That is a better
problem 3 than "there is no power rule off the integers," because the four-point behaviour is
forced and the only question is whether a lift exists.

### P0-3 — `w` names two different objects `MODEL`

**Where.** E9 (`w := 1/0`) and the leap (`0^w = -1`), throughout.

**The problem.** In the model the value `1/0` is `(-1, 0)`, reached by the **exponent** `-1` — that
is the Proven `w = 0^-1`. The exponent direction whose exponential is `-1` is `(0, 1)`. Those are
different elements of `G`. So E9 defines a value and the leap uses an exponent direction, and the
theory writes both `w`.

**This is the source of problem 6's non-termination.** The loop

```
0 - 1 = 0^(1/0) = 0^(0^0 / 0^1) = 0^(0^(0-1)) = ...
```

is exactly the identification of exponent-`w` with value-`w`. Problem 6 concludes "So the question
is not the traversal. It is when a point may be re-read as a power of zero" and answers by banning
the reading of additive units. That is a patch over a sort confusion, which is why the file itself
asks whether the restriction "is principled or whether it is patching over something." It is
patching; the principled version is P0-4.

**Proposed.** Give the two different names — keep `w` for the value `1/0` and name the exponent
direction separately. E9 then stays about `1/0`, and `0^w = -1` reads as a statement about the
phase direction.

### P0-4 — §2's "One sort" is wrong; there are two `MODEL`

**Where.** FORMALIZATION.md §2, "One sort."

**The problem.** The model needs exponents (`G`) and values (`V`) as distinct sorts, with `0^` the
iso between them and `G` not a subset of `V`. The engine already implements this;
rule-combinations.md records it as a wart inherited from the old engine.

**Proposed.** Two sorts in §2, with `0^ : G -> V` and `log_0 : V -> G`. Then:

- "the two axes do not mix" is a typing rule, not a policy (Part 2)
- `1 + w` stands because it is an exponent, and `G` is where `+` is total
- the `0^log_0(x)` sort error is correct behaviour and should stop being apologised for
- P0-3's two `w`s become impossible to conflate

**And the README's motivation for the carrier rewrite does not hold.** "`0·x = 0^(1+u)` puts
arbitrary traction values in exponents, so exponents have to nest" — but `1 + u` there is
`G`-addition of two exponents. No value enters an exponent. The nesting requirement came from
reading exponent-`w` as value-`w`, which is P0-3.

### P0-5 — "every traction value is `0^a`" is an unstated assumption, and false in the model `MODEL`

**Where.** equivalence-classes.md, "Throughout, `x = 0^u`. Every traction value has such a u, and
E6 makes it unique." rule-combinations.md, "Every traction value is `0^a` for exactly one a (E6).
So operand *shape* is not a variable — there is only one shape."

**The problem.** E6 gives uniqueness, not existence. In the model `0^G` is the subgroup
`{t^m e^(i·pi·s)}` — modulus-one coefficients only — so `2 = 2·t^0` is a value and is not a power
of zero, since `|e^(i·pi·s)| = 1 != 2`.

This matters because rule-combinations.md's entire table rests on there being one operand shape. If
some values are not powers of zero, the table has gaps rather than being exhaustive, and its
opening claim ("To ensure all possible scenarios have been accounted for") does not hold.

**Proposed.** Either state existence as a separate assumption and file it, or restrict the tables
to operands that are powers of zero and add rows for those that are not. The second is honest and
cheap; `2^0` standing is already an instance of it.

### P0-6 — E10 is false in the model, and its evidence is all four-point evidence `MODEL`

**Where.** equivalence-classes.md, E10, and "E10 and its totality are kept because this is the
branch the Q model satisfies, so it is the branch known to be consistent. That protection gets used
below."

**The problem.**

```
E10 at a=2, b=1:   0^2 - 0^1 = 0^(2/1) = 0^2
in the model:      e^2 - e^1 = 4.67,  e^2 = 7.39        false
```

E10 holds at the four points — `0^1 - 0^0 = 0 - 1 = -1 = 0^w` — and it holds under the projection,
since every `m > 0` collapses to `0`. So its evidence is four-point and shadow-level, which is
precisely where the Cayley/`D4` structure lives. That makes E10 look like shadow structure taken
for a lift, in the same way P0-1 does.

**Proposed.** Withdraw the "known to be consistent" protection until a model satisfying E10 is
exhibited (see P1-3), and re-examine which conflicts were arbitrated by it. The immediate question
worth checking: is there a restriction of E10 to `G`-exponents where `a/b` stays in `G` that is
true in the model, rather than only at the four points?

### P1-1 — `x + 0 = x` is asserted in two files and forbidden in a third

**Where.**

- notation-and-terminology.md: "It does **NOT** say that the value `0` is the additive identity,
  and it cannot be used as that."
- theory-problems.md problem 2, RESOLVED: "`x + 0 = x`".
- FORMALIZATION.md §5: "`x + 0 = x` and `x · 1 = x`", plus "`x - x = 0`", which is a one-line route
  to the forbidden premise.

Problem 2's resolution also takes both horns of the dilemma problem 2 itself states. Its own
retained text: "**What would settle it.** Whether `x + 0 = x` for the value `0`. If yes, the
addition law survives and negation is not multiplication by −1." The resolution asserts
`x + 0 = x` **and** that negation is multiplication by −1.

**What the model says, and it is a third answer neither branch anticipated.** `x + 0 = x` is
**conditionally** true — true exactly when `x` dominates `0`:

```
1 + 0 = 1 + t,  leading term 1      so it holds at 1, -1, w, and every rational != 0
0 + 0 = t + t = 2t != t             so it fails at x = 0 itself, and at 0^a for a >= 1
```

So `0` is not the additive identity — notation-and-terminology.md is right — and problem 2's
resolution overreaches by dropping the condition.

**Proposed.** State the conditional form. Keep notation-and-terminology.md's distinction, restate
problem 2's resolution as the restricted claim, and correct §5.

**Note for §5.** "`x - x = 0`" should read "`x - x` is the additive identity," which is a different
object from `0 = 0^1`. In the model, exact cancellation produces the field zero, which is not a
value — which is what notation-and-terminology.md means by erasure not being a member of the type.

### P1-2 — the Proven `-0 != w` is proved by the conflation the same entry retracts

**Where.** equivalence-classes.md, Proven. FORMALIZATION.md §6 repeats it as "`-0 != w` — E10 and
totality".

```
-0 != w
    0 - 0 = 0^1 - 0^1 = 0^(1/1) = 0^1 = 0, by E10 and its totality.
    -0 = w would require 1/1 = -1.
```

The derivation establishes a value for `0 - 0`, then applies it to `-0`. The next paragraph of the
same entry says: "reading the expression `0 - 0` as `-0` needs the value 0 to be the additive
identity, and universal invariance does not say that."

**Proposed.** The claim is true and the proof is not. Replace it: under the negation rule
`-0 = 0^(1+w)` and `w = 0^-1`, so `-0 != w` requires `1 + w != -1`, i.e. `w != -2`, which holds
because `w` is not rational. In the model, `(1,1) != (-1,0)`.

### P1-3 — "the branch known to be consistent" against "we do not have one"

**Where.** equivalence-classes.md, quoted in P0-6, against FORMALIZATION.md §8 question 2: "Is
E1–E10 consistent, and is there a model? **We do not have one.**" And §1: the coordinate carrier
"says nothing about `E`, so it is not a model of the axioms."

There is no Q model in the repository — five prose references, no construction. E10's immunity in
conflict arbitration therefore rests on an artifact that does not exist, and P0-6 suggests E10 is
where the difficulty is.

**Proposed.** Either commit the construction or drop the protection. If a Q model exists in a
notebook, it is the most load-bearing unverifiable claim in the docs and should be in the repository
before §8 goes to a formalizer.

### P1-4 — `log_b` is declared undefined for `b != 0`, and the reciprocal law uses it at three bases

**Where.** FORMALIZATION.md §2 table: `log_b` "defined only at `b = 0`". rule-combinations.md:
"`log_b(x)`, b != 0 — went with E2 — no rule". Against equivalence-classes.md's `x^0` derivation,
which runs `log_b(a) = 0, so log_a(b) = 1/0 = w` at `a, b` in `{0, 1, w, -1}`, and the reciprocal
law `log_a(b) · log_b(a) = 1` stated at general bases.

**A note in the theory's favour.** The reviewer expected `log_1` and `log_{-1}` to be fatal, since
`1^n = 1` for every integer `n`. They are not. The cycle consults each exactly where it is
single-valued: `log_1(w) = 0`, because `1^n = 1` for all nonzero integer `n` so `w` is hit only at
`n = 0`; and `log_{-1}(0) = 0`, because `(-1)^n` is `+/-1` for nonzero `n`, never `0`. The two bases
whose exponentials are non-injective are consulted exactly where injectivity is restored, and what
restores it is `x^0` refusing to be `1`. The derivation is tighter than it looks; the signature is
what is wrong.

**Proposed.** Extend the §2 signature to the partial `log_b` the cycle actually uses, with its
domain stated, or mark the reciprocal law as using an operation outside the signature.

### P1-5 — the `a |-> a^w` step attributes to E6 and E7 what they do not say

**Where.** equivalence-classes.md: "The map `a -> a^w` is a permutation of `{0,1,-1,w}`: injective
by E6, closed by E7."

E6 says `0^` is injective. E7 says `0^` is closed on the set. Both concern `0^(·)`. The map
`a |-> a^w` is `(0^u)^w`, which had a rule only through E2 — so with E2 withdrawn there is no rule
for it at all, and neither its injectivity nor its closure follows from anything.

This is the load-bearing step: it is what makes "three known, the fourth forced" work.

**Proposed.** State it as an assumption and file it under Chosen, or derive it. P0-2 offers a
derivation: if `x^0` is the Cayley transform then `a |-> a^w` is its inverse, a Mobius map, and both
properties are immediate.

### P1-6 — problem 1's corroboration is the leap restated

**Where.** theory-problems.md problem 1: "the `x^0` 4-cycle then derived `(-1)^0 = 0`
independently, from the reciprocal law, with no E2 and no `0·w` in it. **So the one objection became
a corroboration.**"

`(-1)^0 = 0` is `P(-1) = 0`, which is `Q(0) = -1`, which is `0^w = -1` — the leap, an input to the
cycle rather than an output of it. The cycle's four values come from E5 (`Q(1) = 0`), the leap
(`Q(0) = -1`), `log_x(x) = 1` (excluding `Q(w) = w`), and counting. Nothing was corroborated.

**A related note, in the theory's favour.** The four lines of the reciprocal-law derivation each
reduce to `P(b)^w = b`, i.e. to `Q = P^-1`. The genuine content of the reciprocal law here is
`log_b(a) = 0 ==> log_a(b) = w` — that the inverse of `x^0` is `x^w`, which lets a map be pinned
down by inverting rather than by raising. That is a real move, and problem 3 is right that it needs
no power law. It does not determine any value of `P` on its own.

**Proposed.** Restate problem 1's support. The resolution of `0·w = 1` rests on the erasure argument
and on the coordinate agreement, both of which stand; the corroboration claim should go.

### P1-7 — the nilpotents observation against §1's coordinate equality

**Where.** equivalence-classes.md: "What survives is the observation and not the label: `0^(1/n)`
is a value distinct from 0 **whose n-th power is 0**." Against FORMALIZATION.md §1:
"`(0^(1/3))^3` reduces to `0^(3/3)` and stops rather than reaching `0^1`."

Under the non-reducing coordinates the n-th power is `0^((n,n))`, not `0^1`. Same claim, two files,
opposite conclusions — another instance of the equality question rather than an independent problem.

**Proposed.** Say which equality the retained observation is under. It holds under value equality.

### P1-8 — §9's grounds for rejecting wheels are misdrawn

**Where.** FORMALIZATION.md §9: "Wheels adjoin `⊥ = 0/0` and keep `0 · x != 0`; traction has
`0/0 = 1` rather than a bottom element, and `0 · ω = 1`."

`0·x != 0` is listed as a wheel feature traction departs from. Traction has it too — `0·w = 1` is an
instance, and so is `0·0 = 0^2`. Wheels and traction also agree that `x - x != 0` in general. The
real differences are only what `0/0` and `0·w` evaluate to (`1` against `⊥`).

That makes wheels closer prior art than §9 says, which matters if wheels were ruled out on this
basis. Wheels also have what §8 question 2 asks for: for any commutative ring `R`, Carlstrom
constructs `Wh(R)`, so the far side of the seam is pinned down by a construction and not only by the
axioms describing it.

**Proposed.** Redraw the comparison around `0/0` and `0·w`, and note the construction as the thing
traction currently lacks.

### P1-9 — `w` does not share zero's projection

**Where.** what-is-traction.md: "they both have the same rational shadow, occupying the same
coordinate on the 'real number line'." FORMALIZATION.md §9: "the theory insists it has *zero*
magnitude and **shares zero's projection**."

In the carrier `w = (1,0)`, whose projection is `1/0`, not `0/1`. In the model `f(-1)` is infinite.
And if the projection respected multiplication with both `0` and `w` going to `0`, then `1 = 0·w`
would project to `0`.

**The intuition is right and the formalisation is not.** On the projective line `0` and `inf` are
the two points each adjacent to both the positives and the negatives, and `x |-> 1/x` exchanges
them. That symmetry is genuine, it is why `0` and `w` behave as mirror images throughout the tables,
and it is the same symmetry that makes `{0,1,w,-1}` harmonic with `1/x` as a diagonal reflection
(P0-2). It does not require a shared projection, and insisting on one costs the projection while
buying nothing the reflection did not already give.

**Proposed.** Restate as the reciprocal symmetry rather than as a shared shadow.

### P2-1 — the `^` cases are not disjoint

FORMALIZATION.md §2: "`^` is defined in three disjoint cases and nowhere else." `0^0` falls under
case 1 (`0^a`) and case 3 (the `x^0` lookup). They agree — both give `1` — so nothing breaks, but
the disjointness claim is false.

### P2-2 — `w^x = 0^(-x)` needs "nonzero integer"

equivalence-classes.md, Proven: "`0^x = w^(-x)` and `w^x = 0^(-x)`, for integer x only." At `x = 0`
this gives `w^0 = 0^0 = 1`, against the cycle's `w^0 = -1`. The integer power rule's exclusion of
`n = 0` is what saves it; the entry should say so.

### P2-3 — "contains the rationals" should say "covers"

FORMALIZATION.md §1 and what-is-traction.md say `T` contains the rationals. §9 already uses the
better word: "The coordinate carrier is a **cover** of QP^1 rather than QP^1 itself."

This matters for one reason, and it is a constraint on models rather than a contradiction:

```
E1 + E4 + E6 have no model in which Q is a subalgebra.
    0·0 = 0 in Q.  E4: 0 = 0^1.  E1: 0^1·0^1 = 0^2.  So 0^2 = 0^1, and E6 gives 2 = 1.
```

The Proven `0^2 != 0` is itself the proof that `T` does not contain `Q`. Saying "covers" makes the
projection the object of study, tells a formalizer the search space is valued fields and covers
rather than extensions of `Q`, and costs nothing.

### P2-4 — the projection needs writing down, and it must be strict

§1 should carry the projection explicitly: domain, codomain, and which operations it respects. Two
properties are forced, and both are counterintuitive enough to need stating:

- **It is strict, not absorbing.** In `Q`, `0` absorbs. If the projection inherited that, `0·w`
  would project to `0`, while `0·w = 1` projects to `1`. So undefinedness has to propagate through
  multiplication by `0`.
- **`f` is a second primitive.** A projection computed along a term that touches `w` gives nothing,
  so the projection on values must be given rather than computed. Note where `f` is interesting: on
  the ordinary axis it is exactly `lim_{t->0} t^a`, and the `w`-direction is where the limit reading
  stops and the theory starts. `f(w) = -1` **is** the leap — the stipulation of the projection in
  the new direction, which is a cleaner placement for it than "forced by E6 + E7".

### P2-5 — the axis restriction is doing two jobs, and only one is justified `MODEL`

Problem 6's fix — refusing to read additive units as powers of zero — buys termination and costs
`1 + 0`. The model separates the two cases:

```
1 + w    stands, and should       it is an exponent, not a value (P0-4)
1 + 0    stands, and should not   1 + t has leading term 1, so the value is 1
```

So the restriction is right about `1 + w` for a better reason than the one given, and
over-restrictive at `1 + 0`. The docs record the cost — "`1 + 0` used to answer 1" — as deliberate;
it is worth reopening now that the two cases can be told apart.

---

## Part 4 — Objections withdrawn

Raised in discussion and withdrawn. Recorded so they are not chased, and so the record of what the
theory survived is kept.

**"E1 + E10 + E5 + E9 + E6 is inconsistent."** The derivation ran through `(-1)·(-1) = 1`, which
required `Q` to be a subalgebra of `T`. That is not an axiom; it was the reviewer's import from §1's
prose. Withdrawn as a claim about the axioms, retained as the model constraint in P2-3.

**`0·0`, `w·w` and `(-1)·(-1)` as internal contradictions.** All three are fibres. `0^a` projects to
`0` for `a > 0`, and `0^(2w)` projects to `(-1)^2 = 1`. Both routes agree once the level is indexed.
Problem 5's "standing" is the correct status, and "the finer reading wins" is correct policy: the
finer reading gives the finer answer, and the finer answer projects correctly.

**"The axis restriction is a restriction on substitution of equals that no model can respect."**
Under operational semantics there is no substitution of equals, only rewriting along licensed steps,
so declining a step is normal. And the model makes the restriction a sort distinction (P0-4), i.e. a
theorem. Withdrawn entirely.

**"The projection is undefined exactly where the disagreements are."** That is what a seam is, and a
seam at `1/0` is the projection working rather than failing. Wheels have it in the same place.

**`f(w/2)` undefined.** Wrong; the reviewer had fixed the codomain as `Q`. With `i` in range, `f` is
total on the `w`-direction, and that is what produced Part 1.

**The `(F,+) != (F*,·)` argument.** The classical theorem — no field's additive and multiplicative
groups are isomorphic, the obstruction being that `-1` has multiplicative order 2 while nothing has
additive order 2 in characteristic 0 — is a constraint on models, not a refutation. The model of
Part 1 evades it because `V` has no addition of its own. Worth keeping as a warning: any attempt to
give `V` a compatible addition runs into it, and that is the same wall as the Maybe addition law.

---

## Part 5 — Open

**Does the `x^0` cycle lift?** P0-2. The obstruction to look at first: `P` has order 4 downstairs,
while E6 forbids any upstairs map from having finite order on the axis. That either forces the lift
to be non-periodic — four applications of `x^0` returning to a different sheet over the same
point, which would be consistent and rather elegant — or blocks it.

**Is there a `G`-restricted E10 that is true in the model?** P0-6. E10 is load-bearing everywhere
and is the one axiom the model refutes, so this is the highest-value check after `2^0 = -3`.

**Does the general involution survive anywhere off the four points?** In the model
`exp(exp(x)) != x`, so problem 4's answer looks like "do not extend it" — the four-point case is a
`D4` coincidence. Worth confirming, since the involution's remaining customer is the Maybe addition
law.

**Is there a canonical derivation history determined by the term alone, under which the licensed
rewrites are confluent?** If yes, that is the carrier, `=` gets a definition, E6 gets content, and
the axis restriction is a theorem about sheets. If no, the theory describes an evaluator rather than
a type, and equivalence-classes.md's opening line — "technically neither confluent nor terminating,
but still has properties of both" — is where a formalizer will put their finger. Problem 6 documents
one case where the history was determined by the traversal order rather than by the term.

---

## Part 6 — Prior art

For §9, which currently compares against three structures.

**The Cayley transform and the harmonic quadruple.** P0-2. The `x^0` result is a classical object:
`(1+z)/(1-z)` on QP^1, the harmonic set `{0,1,-1,inf}`, and `D4` as its PGL_2 stabiliser. Standard
references state the permutation fact directly. The most important entry here, because it gives the
newest result a check that does not depend on the theory.

**Exponential rings (E-rings).** E1 with E5 is verbatim the E-ring axiom: a map
`E : (R,+) -> (R^x,·)` with `E(x+y) = E(x)E(y)` and `E(0) = 1`. Literature: van den Dries;
Macintyre and Wilkie on exponential fields; nLab, "exponential ring." The one-line placement of
traction in that language: **E4 says `E(1) = 0`, i.e. the exponential's image contains zero, which
lands in the units.** That single axiom is what forces `0` invertible and the ring axioms out —
tighter than §9's "zero is invertible; that is the founding commitment."

**Transreal arithmetic (Anderson).** Not currently in §9 and it should be: total arithmetic closed
under all four operations, division by zero admitted, three new elements (`+inf`, `-inf`,
`Phi = 0/0`), an executable implementation, and a motivation about software failing at
singularities. Nearest neighbour in project shape. The differences are real — transreals have
`0·inf = Phi` where traction has `0·w = 1` — but the criticism transreal arithmetic drew will be
aimed at traction verbatim, and reading it is the cheapest way to pre-empt half of it.

**Bergstra, "Division by zero, a survey of options"** (Transmathematica, 2019). Classifies
totalisations by **extension type**, the number of non-real symbols introduced: meadows type 1,
wheels type 2, transreals type 3. Traction adds `w` and `-0`, so type 2, adjacent to wheels. The
document most likely to say whether the branch is charted, and the right thing for §9 to be
organised around.

**Also worth a line each.** Common meadows (Bergstra and Ponse, `1/0 = ⊥` absorbing);
non-involutive meadows; Saitoh and Okumura's division-by-zero calculus (`1/0 = 0`, the involutive
branch pursued much further analytically); and IEEE 754 signed zero, exact engineering prior art for
`(0,1)` against `(0,-1)` — two bit-distinct zeros, `1/(+0) = +inf`, `1/(-0) = -inf`, `+0 == -0`
under comparison — with forty years of accumulated experience about which identities break as a
result.

**On novelty.** Giving zero a reciprocal is thoroughly charted: wheels, transreals, meadows in three
flavours, Saitoh's calculus, and the projective line are five occupied positions with a survey to
map them. What the reviewer could not find anywhere is the specific combination `0·w = 1`,
`0^2 != 0`, no annihilator, two magnitude-zero elements. That is genuinely unclaimed. The model of
Part 1 says why it is available: it is the universal cover of the monomial group, where the winding
number is retained. That is a real object and the claims are true in it. What the model also says is
that the multiplicative half is where the theory lives, and that the additive half — E10, the
addition law, the identities — is open for a structural reason rather than for lack of work.
