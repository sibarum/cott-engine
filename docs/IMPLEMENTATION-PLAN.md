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

The syntax layer does not compile. That is expected until phase 3, and the compiler plugin
excludes `sibarum/cott/*.java` so the engine below it can be built and tested meanwhile.
Deleting that exclusion is what "phase 3 is done" means.

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

Negation has lost its rule rather than gained one. `-(0^a) -> 0^(a+w)` must NOT be implemented;
it is now false, not merely unproven.

Promotion finally gets a consumer here: the exponent rules need both operands as tractions, so
`ProjRationalToTractionPromotionRule` needs a registry that runs before the binary rules.

**Two constraints that are easy to violate:**

- **Match on terms, not values.** `1·1` is the value 1; `1·(1÷1)` is an erasure. Same class,
  different terms. Any canonicalisation before the match destroys what the match needs.
- **Never collapse `0^(0^y)` to `y` outside `{0,1,-1,w}`** without recording that it used the
  general involution — that is theory-problems.md #4, the largest unpaid assumption here.

**Done when:** every row of the table is either implemented or has a test asserting the term
stands, with a comment naming the problem that blocks it.

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

### Phase 4 — calculator

`calculator-vexel-demo` has 66 sites touching `Term`, `Rational` or `Cott.reduce`. Maven
coordinates are unchanged (`sibarum.cott:cott-engine:0.1.0-SNAPSHOT`), so its pom needs nothing.

Mostly renames: `Term` -> `IExpr`, `Rational` -> `ProjectiveRationalLiteral`,
`Cott.reduce(t)` -> `expr.simplify()`. `Parser`, `Notation`, `Render` and `Real` calls survive.

**Not mechanical:** it has its own `Traction.java` and `TractionTest.java`. If those assert
`x^0 = 1` or `-0 = 0`, the docs now mark the first False and the second open. Read them before
counting this phase as renames.

### What this plan deliberately does not do

It does not resolve `0·w`, supply a power rule off the integers, say what negation does to an
exponent, or adopt the general involution. Every one of those is a theory decision, and the
engine is built so each leaves a standing term rather than a wrong answer.

`-0 = 0` is no longer on that list: the branch settles it. What is on the list instead is what
negation does in general, which is a bigger hole than the conflict it replaced.
