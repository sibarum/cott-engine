# cott-engine

[![Build and check citations](https://github.com/sibarum/cott-engine/actions/workflows/build.yml/badge.svg)](https://github.com/sibarum/cott-engine/actions/workflows/build.yml)

The traction calculator: an ordinary-looking calculator that divides by zero and gives indeterminate
forms a value, without error or contradiction. Under it is traction, the number model formalized and
proven in [cott-lean](https://github.com/sibarum/cott-lean).

cott-lean is the specification. Every operation cites the Lean definition it implements, every test of the
value layer names the theorem it states, and every answer says which cott-lean file covers it, or that none
does.

## Unquotiented pairs

A value is a pair `(p, q)` that is never reduced: `Q(1, 2)` and `Q(2, 4)` are different values, `q = 0` is
allowed, and `0/0` is a value like any other. The pair is the answer, always kept.

What a pair means depends on its type. The same two numbers are a different value in each, as a vector is not
a bivector, and the type decides what `+` and `·` do. These are the **traction algebras**, each the algebra its
reading turns into `+` and `×` (`T.Unquotiented`, `T/PairAlgebras`):

| constructor | stands for | `+` | `·` | a number `v` is |
|---|---|---|---|---|
| `C(p, q)` | `q + ip` | the sum of the points | `⊗`, `(ps + rq, qs − pr)` | `C(0, v)` |
| `D(p, q)` | `q − p` | the sum of the points | `⊚`, `(ps + rq, qs + pr)` | `D(0, v)` |
| `S(p, q)` | `p + q` | the sum of the points | `⊚` | `S(0, v)` |
| `Q(p, q)` | `p / q` | `(ps + rq, qs)` | `(pr, qs)` | `Q(v, 1)` |
| `P(p, q)` | `p · q` | none: no flat operation adds it | `(pr, qs)` | `P(1, v)` |

`C`, `D` and `S` are sums, and `Q` and `P` products. A number enters an algebra on one coordinate, its slot,
with the identity on the other. The slot is `q`, except in `Q`, where `q` is the denominator.

Pairs nest and mix. A coordinate is any value, and the two need not be alike: `C(0, Q(1, 2))` holds a number
beside a `Q`. Each algebra is written once, in terms of its coordinates' own `+` and `·`, so nesting follows
the same convention all the way down.

```
> 1/2 + (4 + 3i)
= C(3, Q(9, 2))
    rung       exact
    value      9/2 + 3i   (quotient)
    norm       117/4   (up to invariant)
    turn       0.0935835209   (up to error)
    cott-lean  Nested/Point: C of Q
> 1/0
= Q(1, 0)
    rung       exact
    ray        ω   (quotient)
    ratio      ω   (quotient)
    classical  undefined   (quotient)
    angle      90°   (up to error)
    cott-lean  T/Unquotiented and T/PairAlgebras: Q of two numbers
> P(2, 3) + P(1, 5)
= S(P(2, 3), P(1, 5))
    rung       exact
    value      11   (quotient)
    cott-lean  no file covers this nesting; computed by the engine's rules
```

Build it with `mvn package` and run it with `java -jar target/cott.jar`. On Windows it puts the console in
UTF-8 while it runs and restores it after, so PowerShell needs no `chcp` first. `mvn -q compile exec:exec`
runs it without building the jar.

At the prompt, symbols are typed as escapes and printed as themselves: `\o` is `ω`, `\.` is `·`, `\x` is `×`,
`\/` is `÷`, `\-` is `−`. The symbols themselves also work. `:symbols` lists the symbols and their escapes,
`:help` explains them and the base, `:mode` lists the modes, `:mode decimal` changes one, and `:quit` leaves.

## Evaluating

A bare number is **typeless**: `3 + 4` is `7`. It stays a number until an operation meets it with a pair, and
then it enters that pair's algebra on its slot, with a typeless identity on the other coordinate. Nothing is
embedded before it is needed, so nothing has to be undone after, and what is written is kept: `C(Q(0, 1), 2)`
stays as it is.

Two pairs of different algebras meet in one of them, the other entering it whole on its slot:

- **Two sums, or two products,** meet in the left one: `C(1, 2) + D(3, 4)` is `C(1, D(3, 6))`.
- **A sum and a product** meet as the **Form** mode says. In the sum-of-products form, the default, the sum is
  outside: `1/2 + (4 + 3i)` is `C(3, Q(9, 2))`, the `Q` entering as `C(0, Q(1, 2))`. In the product-of-sums form
  it is `Q(C(6, 9), 2)`. Both are `9/2 + 3i`, and pay for division in different places. Adding `C`s is the safer
  of the two, since it adds coordinate by coordinate, and `Q`'s `+` multiplies denominators, where a zero
  absorbs.

The operation is then the algebra's own formula, and the operations inside the formula meet in turn by the same
rules.

**An operation is never refused.** Where an algebra lacks one, the answer is the pair whose reading is that
operation: `a + b` is `S(a, b)`, `a − b` is `D(b, a)`, `a · b` is `P(a, b)`, and `a / b` is `Q(a, b)`. So
`P(2, 3) + P(1, 5)` is `S(P(2, 3), P(1, 5))`, a sum of products, and over the Integers `6/3` is `Q(6, 3)`.

**Division** in `Q` multiplies by the swap, `Q(s, r)`, defined for every divisor. `C` divides by the conjugate
over the norm, `(−r, s)/(r² + s²)`, and `D` and `S` by the `⊚` inverse `(−r, s)/(s² − r²)`. The divisions inside
are the evaluator's, so over the Integers the coordinates become `Q`s: `C(1, 2) / C(3, 4)` is
`C(Q(-50, 625), Q(275, 625))`, the point's exact inverse (`T2.pointInv`). In the product-of-sums form, a sum
divided by a sum is `Q(x, y)` instead.

**A power** has a number for its exponent. `Q` and `P` raise each coordinate (`T2.power`), the others multiply
the base by itself, and a negative power is `1` divided by the positive one: `2^-3` is `Q(1, 8)`. In IEEE 64-bit,
a number raised to a number is `StrictMath.pow`.

**Named values** are exact pairs: `ω = Q(1, 0)`, `_0 = Q(0, -1)`, `_1 = Q(1, -1)`, `0ω = Q(0, 0)`, and
`i = C(1, 0)`, so `4 + 3i` is `C(3, 4)`. `e` is the base, below.

## Modes

The calculator is in one mode of each modeset at a time. Definitions are kept as written, not as values, so
`x = 1/3` answers `Q(1, 3)` over the Integers and `0.3333333333333333` in IEEE 64-bit, and changing a mode
changes what every later line evaluates to.

**Number type**: the numbers at the bottom of every value.

| key | number type | `1/3` | `1/0` |
|---|---|---|---|
| `integer` (initial) | `BigInteger`, exact, with no division of its own | `Q(1, 3)` | `Q(1, 0)` |
| `decimal` | `BigDecimal`, exact until a result has more digits than the limit, then rounded half to even | `0.3333…3` | `Q(1, 0)` |
| `ieee64` | the `double`: each operation the IEEE one, so `3/5` is `0.6` and `3·(1/5)` is `0.6000000000000001` | `0.3333333333333333` | `∞` |

A Decimal can divide, so it can bring an expression down to one pair. Over the Integers a decimal literal is
refused: whether `0.5` is `Q(5, 10)` or `Q(1, 2)` is not chosen.

**Size limit**: how large an Integer or a Decimal may be. An Integer over its limit cannot be rounded without
changing the pair it is in, so it is refused; a Decimal is rounded, and says so.

| key | Integer | Decimal |
|---|---|---|
| `small` | 64 bits | 16 digits |
| `medium` (initial) | 4096 bits | 34 digits |
| `large` | 1 MiB | 1000 digits |

**Form**: `sum-of-products` (initial) or `product-of-sums`, as above.

**Recursion limits**: how far the descent behind `cos` and `sin` may go, [below](#recursion-limits).

## Readings and rungs

Every reading is taken from the value and never replaces it, and each says what it still holds. The rungs run
from all of it to none:

| rung | what is kept | example |
|---|---|---|
| exact | everything | the pair as computed |
| same value | the value, as another equation | `C(3, Q(9, 2))` written `Q(C(6, 9), 2)` |
| quotient | the algebra's reading: many pairs give it | `Q(6, 2)` and `Q(3, 1)` both read `3` |
| up to invariant | part of the information, as a direction without its scale | `norm`, a scale without a direction |
| up to error | within a bound: information may be lost | a rounded Decimal, a double, a dialed `cos` |
| error | nothing | a double's `NaN` |

The prompt tags each reading with its rung unless it is exact. The readings are:

- **`rung`**: the answer's own. Evaluation reduces nothing it cannot undo, so an answer is exact unless a double,
  a rounded Decimal or a dialed `cos` or `sin` is in it, and an error where a double is `NaN`.
- **`value`**: the number the pair stands for, each algebra read as its meaning all the way down, so
  `C(3, Q(9, 2))` is `9/2 + 3i`. A `C` of `C`s over numbers loses nothing by it (`C_injective`); any other value
  is a quotient. A zero denominator inside leaves it `undefined`.
- **`norm`** and **`turn`**: of a value off the real line, its squared length, and its direction as a fraction
  of a whole turn, in `(−1/2, 1/2]`. Neither needs π.
- **`lowest terms`**: each `Q` of two Integers inside a nested value reduced to its ratio's representative, where
  one is not already.
- **`cott-lean`**: the file proving this nesting's operations, or that none does. A `C`, `D`, `S`, `Q` or `P`
  of two numbers is `T/Unquotiented` and `T/PairAlgebras`; `Q` of `Q` is `Nested/Basic`, `C` of `Q`
  `Nested/Point`, `Q` of `C` `Nested/RatioPoint`, `C` of `C` `Nested/Bicomplex`, and `C` of `Q` of `C`
  `Nested/BicomplexRatio`. A number beside a pair counts as that pair's number on its slot (`T.ratioInt`,
  `T.complexInt`). A double is not a real number cott-lean speaks of.
- A double's **`exact`** value, **`hex`** form and **`bits`**.

A `Q` of two Integers also has the projections of the flat pair. A `Q` is not read as a point: that would take
a transform or a re-embedding.

| name | reading | rung | Lean |
|---|---|---|---|
| `ray` | the class of positive multiples, by its representative: the turn | quotient | `T.Rel`, positive integers |
| `ratio` | the class of non-zero multiples, with `0ω` kept apart | quotient | `T.Rel`, `T.ratioRel_iff` |
| `classical` | what a classical calculator says: `p/q`, or `undefined` where `q = 0` | quotient | `T.toQa` |
| `angle` | `θ = arg(q + p·i)`, in degrees, which is `360 · turn` | up to error | `T.theta` |

`ray` and `angle` both show the turn: off `0ω` two pairs have one turn exactly when one is a positive multiple
of the other (`T.Unquotiented.turn_eq_turn_iff`). `ratio` keeps less, the direction as a line, since `Q` does
not change when the pair is scaled by any `t ≠ 0` (`T.Unquotiented.Q_smul`).

## The base

`e` is the base of `e^x`, and of the turn `cos` and `sin` count in. `e = b` makes `e^x` be `b^x`. A base is held
by its units, as cott-lean holds it (`T/Bases.lean`), not by its value: the full-turn `1` and the identity `1`
are the same number with different units, and `e = 1` is the full turn.

| `e =` | one unit is | `e^x` | default in |
|---|---|---|---|
| `1` | a turn | the rotation by `x` turns | Integer, Decimal |
| `-1` | a half turn, the spinor | the rotation by `x` half turns | |
| `i` | a quarter turn | the rotation by `x` quarter turns | |
| `e` | a radian, which takes π | the real exponential | IEEE 64-bit |
| `0`, `ω` | none: the turn unit is `0 : 0` | not yet | |

Over the Integers `e^x` is `C(sin, cos)`, dialed exactly onto the circle: `e^(1/4)` is `C(Q(2, 2), Q(0, 2))`,
whose value is `i`, and after `e = -1`, `e^1` is `-1`. In IEEE 64-bit they are a double's. `e` on its own is the
base's value, `exp(x)` is `e^x`, and the base in force where a line is used is the one that counts, as with
definitions. `e` can only be set by `e = b`.

`0^x` follows the `zeroPow` rules (`T/Bases.lean`), whose results are logarithm pairs, a zero-magnitude rotation among them,
which no `C` value holds; it waits for `L` as a pair type. `ln` waits for an exact turn of a general point.

## Trigonometry in turns

The cosine and sine of a rational turn `a/b` come out as two rationals exactly on the unit circle, from integers
alone. Only their angle is approximate, and by a bracket that is certified, not rounded. Four parts of the value
layer, one per cott-lean file:

| class | what it does | Lean |
|---|---|---|
| `Spin` | `rotCos`, `rotSin`: the rotation a pair squares to, as the pairs `T(q² − p², N)` and `T(2pq, N)`, unreduced | `T/Spin` |
| `Winding` | `turnLt x a b`: whether `x`'s turn is under `a/b`, by counting how often the powers of `x` cross the positive real ray, in `O(log b)` products | `T/Winding` |
| `Dial` | the mediant descent from `(0, ω)` toward a turn in `(0, 1/4]`, keeping `turn L < a/b ≤ turn R` with `det L R = 1` at every depth | `T/Dial` |
| `RationalTrig` | `cosTurn`, `sinTurn`: halve the turn, split off quarter turns, dial the rest, and square | `T/RationalTrig` |

`turnLt` is meaningful for a pair in the upper half-plane or on the positive real ray (`T.turnLt_iff`); elsewhere it
computes, as the Lean's definition does, but nothing is proved of it. The bracket's width is exact:
`sin²(θR − θL) = 1/(N(L)·N(R))` (`T.sin_sq_dial`).

Over the Integers these are `cos(x)` and `sin(x)`, with `x` a whole number or a `Q` counted in the base's units:

```
> cos(1/6)
= Q(1761923520, 3523847042)
    rung       up to error: cos and sin are dialed to a bracket
    ...
    cos(1/6)   depth 26, between T(40545,70226) and T(29681,51409), sin² of the gap 1/23171366679119247242   (up to error)
```

- The turn is `x` times the base's unit, as a pair `T(a, b)`. `b` must be positive and at most the Recursion
  limits mode's bound. Any positive multiple of the turn gives the same answer, so `cos(2/12)` is `cos(1/6)`
  (`T.cosTurn_scale`), and with `e = -1`, `cos(1/3)` is it too.
- The answer is the unreduced pair `Q(q² − p², N)` (or `Q(2pq, N)`) of the pair the descent ends on. `cos(1/4)`
  is `Q(0, 2)`.
- With one argument the descent goes until a recursion limit stops it. When the target is exactly a pair's turn
  (a multiple of a quarter turn, after halving), the width grows too slowly, and it is the step limit that stops it.
  `cos(x, n)` dials to depth `n`, the Lean's own `n`; it is refused, not cut short, if a limit would stop it first.
- The reading named after the call is its certificate: the depth, which limit stopped it if not the precision, the
  bracket `L, R` of the halved turn's remainder, and `sin²` of the angle between them.
- `cos`, `sin` and `exp` are built in and cannot be redefined. Decimal refuses them.

### Recursion limits

Every bound is a count, never a time, so a line answers the same on every machine. The descent stops at the first
of: the precision, `N(L)·N(R) ≥ 2^precision`; the step limit; or a comparison that would walk a power larger than
the power size. Its size is about `8b` times the mediant's bits, and that is what each comparison's cost grows with.

| key | precision | steps | `b` up to | power size | slowest measured |
|---|---|---|---|---|---|
| `shallow` | `2^-32` | 256 | 360 | 2^16 bits | under 0.1 s |
| `standard` (initial) | `2^-64` | 1024 | 1000 | 2^19 bits | 1.8 s, `cos(7/999, 1024)` before it is refused |
| `deep` | `2^-128` | 16384 | 10000 | 2^24 bits | 57 s `cos(500/2000)`, 94 s `cos(1/10000)`; `cos(333/999, 4096)` over 5 min; `cos(2500/10000)` and the largest explicit depths are estimated at tens of minutes to hours |

The slowest one-argument case in each mode is an exact quarter turn with the largest denominator, such as
`cos(250/1000)`, which runs to the step limit. With an explicit depth and a large `b`, the power size is usually
what refuses it: in `standard`, `cos(333/999, n)` is refused past depth 102.

## For a front end

The engine does all the evaluating, so a GUI only has to draw it:

```java
Calculator calc = new Calculator();          // or new Calculator(NumberType.IEEE, Form.PRODUCT_OF_SUMS)

// One control per modeset, listing each mode's label(), with calc.mode(set) selected.
for (Modeset set : Modeset.values())
    addDropdown(set.label(), set.modes(), calc.mode(set), calc::set);

// A line in; what to show out. Bad input throws SyntaxException or CalculatorException.
switch (calc.enter("1/2 + (4 + 3i)")) {
    case Result.Value v       -> show(v.text(), v.readings(), v.rungs());  // "C(3, Q(9, 2))", {rung, value, ...}
    case Result.Defined d     -> show(d.text());                           // "x = 0.1", or what e = b set
    case Result.Unevaluated u -> show(u.text());                           // "y^2", while y is free
}
```

`Result.Value.value()` is the value itself: a number of the number type (`BigInteger`, `BigDecimal` or `Double`)
or a `Pair` of a `TractionAlgebra` whose coordinates are values. `text()` writes it by its constructors, and what it
writes reads back as the same value. `calc.base()` is the current base. A new modeset is one more `Modeset`
constant.

## What happens to a line

1. **Parse** the universal notation into a tree (`notation`). This layer is syntax only.
2. **Substitute** variables and inline functions, `e` by the base's value and `e^x` by `exp(x)`. If a variable is
   still free, the expression is returned as it is.
3. **Evaluate** in the traction algebras over the number type (`algebra.Evaluator`): numbers typeless, pairs
   meeting as above, and `cos`, `sin` and `exp` by the base.
4. **Read** it. Every reading is taken from the value on demand and never stored in its place.

## The notation

- `C`, `D`, `S`, `Q` and `P` construct pairs, `Q(1, 2)`, and nest: `C(Q(0, 1), Q(1, 2))`. They cannot be defined.
- `ω`, `_0`, `_1`, `i` and `e` are named values. `i` cannot be defined, and `e` only by `e = b`.
- Juxtaposition is multiplication at the precedence of `·` and `/`, left to right: `2x`, `ω(2x)`, `3i`,
  and `1/2x` is `(1/2)·x`. It binds looser than `^`, so `2x^2` is `2·(x^2)`.
- `^` is right-associative and binds tighter than a sign: `-2^2` is `-(2^2)`. `x²` is `x^2`.
- `·`, `*`, `×` all multiply; `/` and `÷` divide; `−` is minus.
- A run of letters is split into names: a defined name is taken whole, longest first, and any other letter
  stands alone. So `xy` is `x·y` unless `xy` has been defined.
- `name(…)` is a call only when `name` is a function; otherwise it is multiplication.
- `x = …` defines a variable and `f(x, y) = …` a function. Definitions are substituted when used, so a
  later change to a variable is seen by everything that uses it.
- A decimal literal is digits, a point and digits: `0.5`, not `.5` or `5.`. It is read as written, and the
  number type decides whether it is a value.
- A double or a Decimal too large or too small for positional notation is written `1.5·10^-7`, which reads back.

## Citations

`@Lean("T2.otimes")` on an operation names the cott-lean declaration it implements, and
`@Proves("T2.flatten_plus")` on a test names the theorem it states. The test checks the same fact on the
nine named values and on many other pairs; the proof is what makes it hold for all of them. `EquivalenceTest`
checks that the evaluator's operations, on pairs whose coordinates are all alike, give exactly the pairs of the
hand-written algebras in `sibarum.cott.traction` that cite the Lean.

`CitationTest` fails the build when:

- a citation names a declaration that is not in cott-lean's `declarations.txt`, so a definition renamed
  or dropped in the Lean cannot silently survive here;
- a public method of the value layer (`sibarum.cott.traction`), or a projection's `apply`, cites nothing;
- a test of the value layer states no theorem.

It reads `../cott-lean/declarations.txt`, so cott-lean must be checked out beside this repository. CI
checks out both, on every push and once a day, since cott-lean can change without this repository
changing. The path can be changed with `-Dcott.lean.declarations=…`.

## History

The engine's history before this fresh start, under an earlier and different theory, is kept at the tag
`archive/cott-pre-traction`. Nothing here is carried over from it.
