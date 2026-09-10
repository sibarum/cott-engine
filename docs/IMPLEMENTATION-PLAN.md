Implementation Plan
===

### Where the repository stands

Deleted: `Term.java`, `Cott.java`, `Rational.java`, `CottTest.java`, `DisplayTest.java`,
`docs/for-lean-users.md`. All of it encoded the bounded three-slot exponent, the reducing
rational, or reduction rules the current docs mark False.

Surviving, with the number of references each still makes to the deleted carrier:

```
Notation.java          201 lines    0   pure syntax, drop-in
SyntaxException.java     9 lines    0
Real.java              160 lines    7   trig catalogue
Bindings.java          511 lines   23   k = 3, f(x) = ..., expansion not environment
Render.java            306 lines   25
Parser.java            268 lines   29
RealTest.java          169 lines    1
BindingsTest.java      227 lines    2
```

The whole of it compiles again as of phase 3, and the exclusion that kept the syntax layer out
of the build is gone from the pom. `mvn test` is 114 green and one skipped; `mvn install` puts
`sibarum.cott` back in the jar, which is what a client needs.

The deletion is committed (`dbe1ec1`), together with these docs.

### Phase 1 — carrier

Bring the expression tree over from cott-engine-2 under `sibarum.cott.engine.*`. Keep its package
structure; it can coexist with `sibarum.cott` in one artifact.

```
base/expr/          IExpr, IPromotionRule
operation/binary/   IBinaryOperationExpr, Addition, Multiplication, Exponential, Logarithm
operation/unary/    IUnaryOperationExpr, Negation, Reciprocal
projective/expr/    ProjectiveRationalLiteral
traction/expr/      TractionLiteral, ProjRationalToTractionPromotionRule
```

Two changes on arrival:

1. **`TractionLiteral(IExpr base, IExpr exp)`**, not two `ProjectiveRationalLiteral`s. The
   exponent has to nest — `1^0` routes through `0^(0^2)`, and `0·x = 0^(1+u)` puts arbitrary
   values in exponents. This is the same defect the old `Xp` had; do not reintroduce it.
2. **Integer overflow.** Coordinates are `int` and nothing reduces, so denominators only grow.
   `Math.multiplyExact`, `long`, or `BigInteger` — pick one before anything depends on the
   numeric contract.

Carry the 25 existing tests. **Done when:** engine compiles standalone and `mvn test -Dtest='*Expr*'`
is green. Syntax layer still broken.

**Landed in `1d5593a`.** `BigInteger` was the choice for point 2: nothing reduces, so growth is
the design, and a fixed-width coordinate would decide how long the engine stays exact and decide
it silently. `ProjectiveRationalLiteral.of(n, d)` keeps the ordinary spellings short. The
projection divides the coordinates rather than their doubles, since coordinates outgrow a double
long before the value they name does. 33 tests green — the 25 carried over, plus eight for the
two arrival changes.

### Phase 2 — rules

`rule-combinations.md` is the spec. Each row is a `simplify()` case.

Implementable now, no open questions:

```
0^a · 0^b   -> 0^(a+b)      E1
0^a ÷ 0^b   -> 0^(a-b)      E1 + E3
0^a - 0^b   -> 0^(a÷b)      E10, and total: at a = b this is 0^1, not a discharge
(0^a)^n     -> 0^(a·n)      E1, for integer n ONLY — see below
```

Behind a flag, or marked provisional in tests:

```
0^a + 0^b   -> 0^(a·b)      Maybe — inferred, never stated independently
```

Blocked — leave the term standing, do not guess:

```
x^w             theory-problems.md #3 — no rule; was False on E2's authority
(0^a)^v, v ∉ ℤ  theory-problems.md #3 — same hole
log_(0^a)(0^b)  went with E2; only log_0 (E8) is available
```

Since withdrawn from that list: `0·w` is 1 (problem 1, the multiplicative erasure), `x^0` is the
4-cycle on the closure set, and `-(0^a) = 0^(a+w)` is Chosen and uncontested — problem 2 turned
out not to be a problem.

**E2 was withdrawn on 2026-09-05**, keeping E10 and total subtraction: that is the branch the
ℚ model satisfies. The engine is affected in three places.

The integer power rule is a rule about the *right operand's shape*, not just its value. `n` has
to be an integer literal, and `(0^a)^n` where `n` is any other term must stand — including where
`n` happens to evaluate to an integer through an unreduced coordinate, since `(6,3)` is a
coordinate pair and not the integer 2. Deciding integrality by projection would reintroduce E2
by the back door at exactly the points the theory withdrew it.

`n = 0` is not in the rule. Zero copies is the empty product, and calling it 1 is `x^0 = 1`,
which the theory does not have.

Negation keeps its rule. `-(0^a) -> 0^(a+w)` is Chosen, and nothing conflicts with it -- problem 2 turned
out not to be a problem. It is unwired because it inherits the leap, not because of a collision.

Promotion finally gets a consumer here: the exponent rules need both operands as tractions, so
`ProjRationalToTractionPromotionRule` needs a registry that runs before the binary rules.

**Two constraints that are easy to violate:**

- **Match on terms, not values.** `1·1` is the value 1; `1·(1÷1)` is an erasure. Same class,
  different terms. Any canonicalisation before the match destroys what the match needs.
- **Never collapse `0^(0^y)` to `y` outside `{0,1,-1,w}`** without recording that it used the
  general involution — that is theory-problems.md #4, the largest unpaid assumption here.

**Done when:** every row of the table is either implemented or has a test asserting the term
stands, with a comment naming the problem that blocks it.

**Landed.** `TractionRules`, one method per row, consulted by the operation nodes before the
projective layer gets them — that order is "the finer reading wins". 132 tests green, none
skipped. Four things worth carrying forward:

*Standing has to be said, not merely not-said.* A rule that declines lets the projective layer
answer, and the projective layer is perfectly willing to multiply `0` by `ω`. The undecided
cells return the term unchanged instead of returning nothing.

*The erasure is read off the term.* `y - y` materialises as the multiplicative **identity**, not
as whatever the coordinates compute: `a÷a` at `a = (2,1)` is the pair `(2,2)`, which is the value
one at coordinates that are not one, and `0^(2,2)` is not the point zero. Reading it off the term
is what makes subtraction total.

*The integer power rule is repeated multiplication, and that is all it is.* `2^3` is 8 by
multiplying three copies, `(0^2)^3` is `0^6` because the product rule turns the copies into an
exponent sum. One mechanism, not two — which is exactly the proof in the docs. It is only applied
where the copies join back up, since `x^2` is not improved by becoming `x·x`.

*The leap is not wired.* `-1 = 0^w` is Chosen, but reading it makes `(-1)·(-1)` into `0^(w+w)`,
and the coordinates put `w+w` at 1, so the engine would answer that the square of minus one is
zero. Three of the four points read; the fourth is kept where the two layers cannot meet, with
`provisionalMinusOne` and a test recording what it would have done. This is problem 5, which the
implementation found.

### Phase 3 — syntax layer

Retarget the survivors, ~84 sites total. Order by coupling, lowest first:

1. `Real.java` (7) — mostly independent
2. `Bindings.java` (23) — expansion is structural; should be near-mechanical
3. `Render.java` (25) — printer follows the new term shapes
4. `Parser.java` (29) — builds `IExpr` instead of `Term`

`Notation.java` and `SyntaxException.java` need nothing. Keep printing and parsing inverse —
that property came from both deriving from `Notation`, and it is worth not losing.

`BindingsTest` and `RealTest` touch the carrier 2 and 1 times; they should survive the retarget
nearly intact.

**Done when:** `mvn test` is green and a string round-trips: parse, simplify, render.

**Landed.** 114 tests green, one skipped. The exclusion is gone from the pom, so `sibarum.cott`
is in the jar again. Four things the estimate above missed:

*The carrier had no node for a name or a call*, so the parser had nothing to build `x`, `π` or
`sin(x)` from. `AtomExpr` and `CallExpr` were added under `base/expr`. Neither is theory — an
atom is what a name has always parsed to, and nothing in the carrier answers a call.

*Nothing answered a `Real` call either.* That was `Cott.reduce`'s job and `Cott` had been
deleted, so `Cott` is back as a front end and not an evaluator: normalize, parse, expand, answer
the real calls, simplify, render. The real *reading* lives there too, because it has to know π
and e, and the carrier must not.

*The reading of omega is not the carrier's projection of it.* `ProjectiveRationalLiteral`
projects ω onto zero's shadow, which is right for a projection and wrong as an argument to sin —
it made `sin(ω)` answer 0. A zero denominator now has no real reading, and the call stands.

*The printer names nothing.* `0^0` prints as `0^0`, not `1`: E5 belongs to `simplify()`. The old
printer could name points because what it printed was already a normal form. The decimal spelling
had to be narrowed for a related reason: coordinates do not reduce, so `0.5` is the pair (5, 10)
and nothing else, and printing (1, 2) that way would hand back a different literal. A decimal is
used only where the denominator is already a power of ten, which is exactly the case it exists
for — `Real` rounds to a denominator of 10^15.

Consequences visible in the tests, all of them non-reduction showing through: `cos(π÷3)` is
`0.5` where it used to be `1÷2`, a typed `2.5` comes back as `2.5` rather than `5÷2`, and
`1÷2w` is `ω` — halving omega does not move it, since `(1,2)·(1,0) = (1,0)`. That last one is
also why `i` has no coordinate form: `0^(w/2)` would say `-1`. `i` parses as an atom and stands.

Five assertions now wait on phase 2 and say so where they stand: three integer powers, the
guard-digit test (skipped, since it cannot square anything), and one canonical product ordering.

### Phase 4 — calculator

**Withdrawn. `calculator-vexel-demo` has been deleted, and a client for this engine will be
written from scratch rather than ported.**

What the audit found before it went, kept because a rewrite should not walk back into it. The
187 references were two different jobs sharing one name: `Rational` was doing plot geometry —
sample points across a range, axis bounds — where an ordinary reducing rational is what is
wanted, while the projective coordinate never reduces and carries ω and `0÷0 = 1` with it. A
client wants both, and should say which it means at each site rather than inheriting one class
for both.

The bridge was worse than a rename. `Traction.java` read the carrier structurally — a
multiplicity over a three-slot exponent, with `Wind`/`AWind` for erasure — so it encoded the
abandoned theory in its shape, not just its imports. And two of its tests asserted the opposite
of what this branch says: `1-1` read as erasure rather than as a value, which total subtraction
overturns, and `0w` read as erasure, which the carrier answers as 1. Neither was a porting
problem.

The lesson for whatever replaces it: a client reads what the engine says and does not re-derive
it. Anything that pattern-matches the carrier's shape will have to be rewritten every time the
carrier moves, and the carrier is going to move again.

### Derivations — the answer with its proof

Added after phase 2, because information conservation is the theory's founding commitment and an evaluator
that returns only an answer discards the derivation that produced it.

`Cott.derive(entry)` returns a `Derivation`: the expression as it arrived, the expression it settled on, and
every rewrite between, each carrying the `Rule` that licensed it — the rule as the docs write it, where it is
justified, and its status. So an answer can be asked whether it depends on anything the theory has not
settled, per answer rather than per document:

```
cos(π÷3)·2  =>  1   [assumes: cos of a real reading, rounded to 15 places  [APPROXIMATE]]
    = 0.5·2        cos of a real reading, rounded to 15 places
    = 1            the coordinates combine  [projective arithmetic, PROVEN]
```

Four decisions in the shape of it:

**There is no other evaluator.** `Deriver.derive` *is* simplification, and `IExpr.simplify()` is that walk
with the reasons dropped. No node has a `simplify()` of its own any more; they were deleted. The first version
kept a fast big-step path beside the derivation and pinned the two together with a test, which is the
arrangement this replaces — pinning by test only covers the inputs somebody thought to write down, and the
ways two paths drift are exactly the ways nobody thinks of: a rule consulted in a different order, a result
one path re-reduces and the other does not, an optimisation applied to one. There is now nothing to keep in
step. The cost is that `simplify()` allocates a step list it throws away; take it back with a flag on the
driver if a plotter ever needs it, not with a second walk.

**Small steps, whole terms.** One rewrite per line, the entire expression each time, so it reads as a chain of
equalities the way the docs argue rather than as a log of what the evaluator did to itself.

**Rules build; the driver reduces.** `TractionRules` returns `0^(1+1)` rather than `0^2` — a rule that
finished its own exponent arithmetic would be doing work no derivation could show. That is also what turned
the four-point folding (`0^1 = 0`) into a rewrite of its own instead of something a rule did on its way past.

**The projective layer reports itself.** Every defect found in this engine has been coordinate arithmetic
rather than a traction rule — `w+w` landing on 1, `0·w` answered by two pairs multiplying before any rule was
consulted, `(-1)·(-1)` reaching zero. A derivation that recorded only the interesting-looking layer would have
missed all three.

```
2^3  =>  8   [proven]
    = 2·2·2        (0^a)^n = 0^(a·n), integer n  [E1, repeated multiplication]
    = 4·2          the coordinates combine
    = 8            the coordinates combine
```

### Phase 5 — the Traction-Theory carrier

`docs/Traction-Theory.md` states the theory with a carrier of its own: a traction is `(n, t) = n·0^t`,
a real part and a traction part, with four total operations given in closed form. This phase is that
carrier, in place of the `base^exponent` pair over a projective rational.

**What moved.** `ProjectiveRationalLiteral` became `RationalLiteral` — an ordinary non-reducing
rational whose **denominator is never zero**; `TractionLiteral(base, exp)` became
`TractionLiteral(real, exponent)` with the base fixed at zero; `TractionRules` was rewritten one
method per operation, plus the two unit tables; the syntax layer was retargeted at both. 196 green.

**The zero denominator was the whole of it.** Omega lived in the coordinates as `(1, 0)` only because
a coordinate pair was the only thing in the carrier that could hold `1÷0`. It does not need to be
there — `ω` is `0^-1`, which the traction pair spells — and `1÷0` becomes E9, an axiom applied in
view of a derivation rather than arithmetic done under one. Four things the docs record as defects
went with it:

- `ω+ω` is `2ω` and `(-1)·(-1)` is `1` without either being special-cased. There is no
  cross-multiplication across a zero denominator because there is no zero denominator.
- `ω÷2` is half of omega. `1÷2w` used to answer `ω`, since the zero denominator absorbed the factor;
  `i = 0^(ω÷2)` is therefore spellable now, and what it still needs is uniqueness of roots — its
  square lands on `0^((2÷4)ω)`, beside `0^ω` rather than on it.
- `-0` is not a literal. It is `-1·0`, the pair `(-1, 1)`, so the sign lives in the real coordinate
  instead of on a denominator, and negation is an involution on the pair without the leap. The
  three-way disagreement about `-0` collapses to one answer: the product, with no rule for it.
- `2·0` keeps the 2. A rational zero annihilates if it is computed with, so the absence marker is
  skipped instead, and `0·0^2` is `0^3`. Without that, dividing by zero proves 2 = 1.

**The absence marker was got wrong first, and the author corrected it.** This phase landed with the
real part respelled — `0 = (1,1)`, `ω = (1,-1)`, a real part that may never be zero, and a zero
arriving there rolling into the exponent. The reason given was the annihilation above. That was the
right defect and the wrong fix, and the marker is zero after all:

*One is a coefficient a term could actually have.* A marker of one cannot be told from a real part
that happens to be one, and a marker that is a value takes part in arithmetic and gets absorbed —
`1·1^1` can be argued into `1^2`. Zero cannot be a coefficient here, because a coefficient of zero
would annihilate and this theory has no annihilator, so zero is the only number that can mean
absence. Uniformly in both slots, and `(0,0)` is then erasure itself, which is the `∅` at the centre
of the diagram. The respelling had made that origin unreachable.

*Annihilation is a defect in the operation, not in the marker.* `a·c` with an absent operand is
skipped, not multiplied: `2·0 = (2·∅, 0+1) = (2, 1)`. Negation has nothing to turn in an absent real
part, so it materialises the `-1` it is multiplying by, and `-0` is `(-1, 1)` — the same pair the
respelling reached, so nothing about `-0` depended on the marker.

*One rule was needed to keep one spelling per value.* A real part of exactly one is the
multiplicative identity and collapses back to the marker by `x·1 = x`, because `1÷0` arrives as
`(1, -1)` where omega is `(0, -1)`. That collapse is a rule with a name, in view of the derivation,
rather than the marker and the coefficient being the same thing by fiat.

**Four things in the doc are not wired, and two of them disagree with the doc's own other pages.**

*The carrier's negation, `-(a, b) = (a, b-1)`.* It sends `1` to `1·0^-1`, which is `ω`, against the
four-unit table's `-1 = (-1, 0)`. The traction form beside it, `a·0^(b+ω)`, is a third answer. What
is wired is the table's: negation turns the real coordinate, which follows from negation
distributing over a product and needs neither the leap nor an ω in the exponent.

*The traction addition law.* Left out at the author's instruction. Four edges of it were reported as
disagreements and three were the marker's fault rather than the law's: a factor mentioning an erased
coordinate never got generated, because the four factors are cross-terms of a distribution and a
cross-term with an erased part in it was never there to distribute. Dropped rather than evaluated,
they give `0 + 1 = 1`, `1 + 1 = 2`, and `0^2 + 0^3 = 0^6`, which is the older docs' mirror law — so
the two agree on bare powers. Written with explicit coefficients, `1·0 + 1·0^0` is 2 where `0 + 1`
is 1, which is the marker earning its keep.

What survives is one disagreement and the two holes. At `b = d` only `0^(bd)` survives, so the law
makes `ω + ω` the point zero where distributivity makes it `2ω`: the real part is a multiplicative
slot, so its absence is a multiplicative erasure, and by the residue rules a multiplicative erasure
landing in a sum leaves a residue of one rather than nothing. The law drops it; the residue rule
keeps it as one copy. Taking the law makes the value zero an additive identity, which is the
collision the older docs already record against the mirror law. The holes are the general involution
and a power rule in the ω-direction. What answers a sum meanwhile is distributivity where the two
terms are alike, and the identity otherwise.

*The general involution, `0^(0^n) = n`.* Stated in the doc as E6 being involutive generally. On the
four points the tables have it and there it is forced; off them it is problem 4, so it is
`provisionalInvolution` and `2^3` still stands rather than becoming a traction.

*`-1·0 = ω`.* Conjectured, and refuted from the primitives: with E1 and the leap it makes `ω = -2`
as an exponent, then `-1 = 0^-2` whose reciprocal is `0^2`, and `-1` is its own reciprocal, so
`0^2 = 0^-2` and E6 gives `2 = -2`. Recorded as `provisionalMinusZero` with that derivation, and the
pair stands.

**What the tables added.** The unit exponentiation and logarithm tables are new — sixteen cells each,
filed Chosen. They subsume the `x^0` cycle, and they wire two cells the engine previously left
standing: `log(-1, 0) = ω`, and the leap under a real part. The second was necessary rather than
optional: `0^ω · 0^ω` carries the real part in by E1 before the fold can see the term, so a fold
that only matched a bare pair let the square of minus one stand.

**Three defects this phase found in the engine rather than in the theory.**

*The identity ran too early.* `x + 0 = x` fired before its operand had settled, so `2·0 + 0`
answered `2·0` where distributivity answers `3·0`. The fix is a third phase in the driver: shape
rules, then the operands, then the identities and the coordinates. The identity also gained the
condition REVIEW.md's P1-1 asks for — it does not apply where x is itself at the point zero's order,
which is what leaves `0 + 0 = 2·0` to distributivity.

*A log's operands were never reduced.* `inChildren` had no case for it, which went unnoticed while
every log rule read a literal the parser produces directly. `log(-1, 0)` arrives as
`log(Negation(1), 0)`, so the cell could not be reached until the case existed.

*Two spellings of the same value.* A zero numerator away from `(0, 1)` — `1 + 1÷(-1)` lands on
`(0, -1)` — is a multiple of the point zero and belongs in the pair, so `0÷d` is now `(1÷d)·0`.
Without it the display did not round-trip: it printed `0÷-1` and re-read it as `1÷-1·0`. The
converse case is the one to leave alone: `2÷2` stays `(2,2)`, one at coordinates that are not one,
because that is the same fact the exponent erasure is read off the term to avoid.

*The erasure was found at one end of E1 and not the other.* `0^(2÷2) · 0^(-2÷2)` answered
`0^(1÷2·0)`, because E1 builds the exponent sum while the exponents are still terms — so the
product's own erasure check was comparing `(2,2)` against an unreduced `-2÷2` and missing it.
Delaying E1 is not the fix, since the shape it matches can be reduced away. What was missing is the
same erasure at the other end: a sum of two coordinates where one is the other with its sign turned
is `z - z` with no negation node left to recognise it by. Found by checking a claim written into
`Traction-Theory.md`, not by a test.

**Five more, found by checking the laws rather than the cases.** Commutativity, associativity and
reversibility over a corpus of ten values, 2400 pairs, with the failures split into *unsound* (both
sides answered and disagreed) and *incomplete* (a side stands, so no rule reaches). Two of the last
five commits before this rewrite were one-sided matching bugs, so this is where to look.

- *The identity dropped a zero it could not absorb.* `x + 0 = x` asks x to dominate the point zero,
  and a term that has not settled has to be asked the same question of its parts: `0 + (0 + 0^2)`
  answered `0 + 0^2`, a zero gone, because the standing sum on the right is not a literal and so
  looked like it dominated.
- *The additive erasure materialised instead of vanishing.* `x + (z-z) = x` says it leaves nothing;
  discharging it to the point zero first and then adding made `0 + (1-1)` into `2·0`.
- *The transient `(0,0)` was multiplied before it discharged.* Left standing for one turn, an
  enclosing product got it: `(0·ω)·1` answered `0^(2·0)`, the two exponent zeros having been added
  as though they were point zeros. It discharges where it is made now, and exponent arithmetic skips
  an absent exponent.
- *Two spellings of `-1` took different paths.* `1 + (-1)` at the literal and `1 - 1` at a negation
  node are the same erasure, and only the node was recognised. Likewise `-1÷-1`: as negation nodes
  it was the whole-term erasure and answered 1, as literals it went to the coordinates and answered
  `(-1,-1)` -- so a printed answer re-read as something else. Whether a rule fires may not depend on
  how far an operand happens to have reduced.

What the sweep left was six disagreements out of 2400, all one shape: `x + 0 = x` discarding a
magnitude-zero term that a later erasure needed, so addition was not associative. That is REVIEW.md's
P1-1, and the author settled it: **`x + 0` is a projection, not a traction rule.** Adding the point
zero does not move a value's shadow, and in the type both terms are still there.

Removing the rule took three steps, and the middle one is the interesting one.

- *The rule goes.* `PLUS_ZERO`, and with it the dominance condition and its two helpers.
- *So does the coordinate version.* Measured on its own, deleting only the rule made the sweep
  **worse** -- eighteen disagreements -- because `(1,1) + (0,1)` is `(1,1)`, so the model went on
  absorbing what the theory had stopped absorbing, and with no condition at all.
- *Which forces the sorts apart.* In an exponent the rational zero IS the absence marker and
  `1 + 0` is 1; as a value it is the point zero and the sum stands. The walk now carries which slot
  it is in, and the rules that lift a zero take it with them. That is the narrow form of REVIEW.md's
  P0-4 -- by position, not by type -- and it is the only place the engine needs it.

Six disagreements down to **two**, and those two are a different question: a cancellation between
coordinates gives the point zero read as an erasure and `0÷2` computed as coordinates, which are
different values here. The visible cost is that `1 + 0`, `0 - 1` and `ω + 0` now stand, with the
shadow carrying the old answer.

**A fuzz of 20000 random expressions**: no crashes and no non-termination, and every answer settles
when re-entered. 857 of them need exactly one extra round, never more and never oscillating: a
coordinate that does not reduce is spelled as a division, and re-reading a division takes one rewrite
to fold back into the coordinate. The value never changes.

**Checked by 196 tests and by a stability sweep**: 82 expressions, each answer re-entered and
required to answer itself. That is the invariant the printer exists for — the result of one
evaluation is the entry for the next — and it is stricter than the round-trip test, which does not
simplify what it re-reads.

### What this plan deliberately does not do

It does not resolve `0·w`, supply a power rule off the integers, choose between the Maybe
addition law and negation-as-multiplication-by-−1, or adopt the general involution. Every one of
those is a theory decision, and the engine is built so each leaves a standing term rather than
a wrong answer.
