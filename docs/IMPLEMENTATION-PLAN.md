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
0·w             theory-problems.md #1
-(0^a)          theory-problems.md #2 — no exponent rule at all now, not a disagreement
x^0, x^w        theory-problems.md #3 — no rule; both were False on E2's authority
(0^a)^v, v ∉ ℤ  theory-problems.md #3 — same hole
log_(0^a)(0^b)  went with E2; only log_0 (E8) is available
```

**E2 was withdrawn on 2026-09-05**, keeping E10 and total subtraction: that is the branch the
ℚ model satisfies. The engine is affected in three places.

The integer power rule is a rule about the *right operand's shape*, not just its value. `n` has
to be an integer literal, and `(0^a)^n` where `n` is any other term must stand — including where
`n` happens to evaluate to an integer through an unreduced coordinate, since `(6,3)` is a
coordinate pair and not the integer 2. Deciding integrality by projection would reintroduce E2
by the back door at exactly the points the theory withdrew it.

`n = 0` is not in the rule. Zero copies is the empty product, and calling it 1 is `x^0 = 1`,
which the theory does not have.

Negation keeps its rule. `-(0^a) -> 0^(a+w)` is Chosen, as it was — a retraction inside the E2
commit had briefly called it false. It stays out of the implementable list only because it
conflicts with the Maybe addition law (problem 2), so whichever of the two is implemented has
to be the one behind the flag.

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

### What this plan deliberately does not do

It does not resolve `0·w`, supply a power rule off the integers, choose between the Maybe
addition law and negation-as-multiplication-by-−1, or adopt the general involution. Every one of
those is a theory decision, and the engine is built so each leaves a standing term rather than
a wrong answer.
