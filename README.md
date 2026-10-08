# cott-engine

[![Build and check citations](https://github.com/sibarum/cott-engine/actions/workflows/build.yml/badge.svg)](https://github.com/sibarum/cott-engine/actions/workflows/build.yml)

The traction calculator: an ordinary-looking calculator that divides by zero and gives indeterminate
forms a value, without error or contradiction. Under it is traction, the number model formalized and
proven in [cott-lean](https://github.com/sibarum/cott-lean).

cott-lean is the specification. This engine implements only what it proves: every operation cites the
Lean definition it implements, and every test of the value layer names the theorem it states. What the
Lean has not proven, the engine does not do.

A value is an **unquotiented pair** `T(p, q)`, and that pair is the answer, always kept. Every number it
stands for is a reading of it, and cott-lean names six (`T.Unquotiented`):

```
C(p,q) = q + i·p             as a complex number
D(p,q) = q − p               as a difference
S(p,q) = p + q               as a sum
Q(p,q) = p / q               as a ratio
P(p,q) = p · q               as a product
L(p,q) = (log_b N, turn)     as a logarithm: a scale in any base b, and a turn     N = p² + q²
```

None of them needs π. The base of `L` is chosen only when converting, and its turn is the direction as a
fraction of a whole turn. The calculator shows its [projections](#the-projections), and each is one of
these readings or is read from one.

```
> 1/0
= ω
> 0/0
= 0ω
> 2x^2+4x+2
2x^2 + 4x + 2
> x = 1
x = 1
> ω(2x)-(0(x+1)+0^2(2x-1))/2
= 4/0
    ray        ω
    ratio      ω
    classical  undefined
    angle      90°
    point      4i
```

Build it with `mvn package` and run it with `java -jar target/cott.jar`. On Windows it puts the console in
UTF-8 while it runs and restores it after, so PowerShell needs no `chcp` first. `mvn -q compile exec:exec`
runs it without building the jar.

At the prompt, symbols are typed as escapes and printed as themselves: `\o` is `ω`, `\.` is `·`, `\x` is `×`,
`\/` is `÷`, `\-` is `−`. The symbols themselves also work. `:symbols` lists the ones the current arithmetic
can use (`ω` only where it is a value), with their escapes. `:help` lists them too, `:mode` lists the modes,
`:mode ieee` changes one, and `:quit` leaves.

## Modes

The calculator is in one mode of each modeset at a time. The first modeset is **Arithmetic**: what a
value is, and how the operations act on it.

| key | arithmetic | `1/0` | `0/0` | `ω` | decimals |
|---|---|---|---|---|---|
| `ieee` | IEEE Floating Point: binary64, round to nearest even | `∞` | `NaN` | no | yes |
| `compound` | T(T,T) Compound Ratio: `T2(A, B)` as the ratio `A / B` of two ratios, flattened (`Nested/Basic`) | `ω` | `0ω` | yes | not yet |
| `point` | C(T,T) Point: `T2(A, B)` as the point `B + A·i` over ratios (`Nested/Point`) | `0ω + 0ω·i` | `0ω + 0ω·i` | yes, as `i` | not yet |
| `complex` | T(C,C) Complex Ratio: a ratio `z/w` of two Gaussian integers (`Nested/RatioPoint`) | `1/0` | `0/0` | yes, as `1/0` | not yet |
| `bicomplex` | C(C,C) Bicomplex: the point `B + A·j` with Gaussian-integer coordinates (`Nested/Bicomplex`) | no `/` | no `/` | no | no |
| `bicomplex-ratio` | C(T(C,C),T(C,C)) Bicomplex Ratio: the point `B + A·j` over ratios of Gaussian integers (`Nested/BicomplexRatio`) | `0ω + 0ω·j` | `0ω + 0ω·j` | yes, as `1/0` | not yet |

A traction mode is named by its construction. The outer letter is how the pair is read, `T` as a ratio and
`C` as a point, and the inner letters are what its coordinates are, `T` ratios and `C` Gaussian integers.
The four corners are T(T,T) Compound Ratio, C(T,T) Point, T(C,C) Complex Ratio and C(C,C) Bicomplex.

In the two `C`-coordinate modes a Gaussian integer is held as the flat point it already is, `T(p, q) = q + p·i`
(`T.toGaussian`), with `⊕` as its `+` and `⊗` as its `·`. In T(C,C) Complex Ratio, `+`, `·`, `-` and the
reciprocal are `T`'s formulas over the Gaussian integers (`TOver`), and `/` is `a · reciprocal(b)`, a swap. So
`3/0` stays `3/0`, keeping its numerator, and `(3/0)·0` is `0/0`. The readings are the complex `value` and the
`point` the ratio rationalizes to, `(z·w̄)/N(w)`, where every `z/0` becomes `0ω + 0ω·i` (`TC.rationalize_infinite`).
`ω` enters as `1/0`, so for now every coordinate typed is a plain integer.

In C(C,C) Bicomplex an integer enters on the outer unit, `n + 0·j`. cott-lean gives it no inverse, so there is no
`/` and no `ω`, and for now nothing typed reaches `i` or `j`. The readings are the `norm` `B² + A²`, a Gaussian
integer, and the point with `j` read as `i` and as `−i` (`CC.evPlus`, `CC.evMinus`).

C(T(C,C),T(C,C)) Bicomplex Ratio gives C(C,C) its inverse, the way C(T,T) gives the integer points theirs:
each coordinate is a T(C,C) ratio, and `/` multiplies by `(B − A·j)/(A² + B²)`. These are Point's formulas,
and on real ratio coordinates the inverse is Point's exactly (`CTC.inv_ofT2`), so `1/2` is
`8/16 + (0/16)·j`. It fails only on the light lines `B = ±i·A`, where the value is a zero divisor with no
inverse at all (`CTC.isUnit_val_iff`). The readings are the bicomplex `value`, the `norm` and whether it is a
`unit`. `ω` enters as `1/0` evaluated here, which collapses to `0ω + 0ω·j`.

In C(T,T) Point, `+` is the sum of the points, `·` their product, and `/` multiplies by the point's exact
inverse `(B − A·i)/(A² + B²)`. `-a` is `(-1)·a`, since cott-lean defines no point negation, and `x^0` is not
defined. The answer is the point unreduced, so `1/2` is `8/16 + (0/16)·i`: the inverse returns to `1` only up to
its own squared length (`T2.otimes_pointInv`). Its readings are the complex `value` and the `length²`.

A new calculator starts in T(T,T) Compound Ratio. The notation, definitions and substitution are shared by every
arithmetic, and a definition is kept as written, not as a value. So `x = 1/10` answers `1/10` in one
arithmetic and `0.1` in another, and changing the arithmetic changes what every later line evaluates to.

In IEEE Floating Point, each operation is the IEEE operation itself: `3/5` is `0.6`, while `3·(1/5)` is
`0.6000000000000001`. A literal is rounded once, `^` is `StrictMath.pow`, which gives the same answer on
every machine, and `-0` is kept apart from `0`. A value is written with the fewest digits that read back as
the same double, and its readings are the `exact` value the double holds, its `hex` form and its `bits`.

The second modeset is **Recursion limits**: how far a recursion may go before it is stopped. For now that is the
mediant descent behind `cos` and `sin` (see [Recursion limits](#recursion-limits)). A new calculator starts in
`standard`.

## For a front end

The engine does all the evaluating, so a GUI only has to draw it:

```java
Calculator calc = new Calculator();          // or new Calculator(Arithmetic.IEEE_FLOATING_POINT)

// One control per modeset, listing each mode's label(), with calc.mode(set) selected.
for (Modeset set : Modeset.values())
    addDropdown(set.label(), set.modes(), calc.mode(set), calc::set);

// A line in; what to show out. Bad input throws SyntaxException or CalculatorException.
switch (calc.enter("0.1 + 0.2")) {
    case Result.Value v       -> show(v.text(), v.readings());  // "0.30000000000000004", {exact, hex, bits}
    case Result.Defined d     -> show(d.text());                // "x = 0.1"
    case Result.Unevaluated u -> show(u.text());                // "y^2", while y is free
}
```

`Arithmetic.hasOmega()` and `hasDecimals()` say which keys a mode can use. `Result.RatioValue` and
`Result.IeeeValue` also carry the value itself (the pair, or the `double`) for anything a front end needs
beyond the written text and readings. A new arithmetic is one more `Arithmetic` constant with an
evaluator, and a new modeset is one more `Modeset` constant.

## What happens to a line

1. **Parse** the universal notation into a tree (`notation`). This layer is syntax only.
2. **Substitute** variables and inline functions. If a variable is still free, the expression is
   returned as it is.
3. **Evaluate** in the current arithmetic. The rest of this section is T(T,T) Compound Ratio.
4. Evaluate at Level 2, in `T2`: a literal `n` enters as `T2.of(T(n,1))`, and `+`, `·`, `-`, the
   reciprocal and `^` are `T2`'s own operations. `a - b` is `a + (-b)`, and `a / b` is `a · reciprocal(b)`.
5. **Flatten** the result to a flat pair, `T2(A, B) ↦ A / B`. That pair, with no quotient, is the answer:
   the ground truth, always kept. It is written as one of the nine named values by its name, otherwise
   as `p` or `p/q`, unreduced.
6. **Project** it. Every other reading is a `Projection` of the ground truth, taken on demand and never
   stored in its place, so any number of them can be read from one value.

## The projections

| name | of | reading | Lean |
|---|---|---|---|
| `ray` | turn | the class of positive multiples, by its representative | `T.Rel`, positive integers |
| `ratio` | `Q` | the class of non-zero multiples, with `0ω` kept apart | `T.Rel`, `T.ratioRel_iff` |
| `classical` | `Q` | what a classical calculator says: `p/q`, or `undefined` where `q = 0` | `T.toQa` |
| `angle` | turn | `θ = arg(q + p·i)`, in degrees, which is `360 · turn`; the one reading that is not exact | `T.theta` |
| `point` | `C` | `q + p·i` | `T.toC` |
| `rotation` | `1^(2·turn)` | the rotation the pair squares to, `(q² − p²)/N + (2pq/N)·i`: exact, and by **twice** `angle` | `T.rot` |

The `of` column names the reading each projection shows. `ray` and `angle` both show the turn of `L`, the
direction as a ray: on the integers it is `T.turn` (`T.Unquotiented.turn_toT`), and off `0ω` two pairs have
one turn exactly when one is a positive multiple of the other (`T.Unquotiented.turn_eq_turn_iff`). `angle`
writes it in degrees, and `ray` writes the class it names. `ratio` keeps less, the direction as a line,
since `Q` does not change when the pair is scaled by any `t ≠ 0`, `−1` included (`T.Unquotiented.Q_smul`).
`classical` is `Q` taken as a number. `rotation` is `1^(2·turn)`, `C²` over its norm, and since
`C² = S·D + 2i·P` (`T.Unquotiented.C_sq`), its entries are `S·D/N` and `2P/N`.

`D`, `S`, `P` and the scale `log_b N` have no projection yet.

`rotation` is the one reading that turns by `2θ`, not `θ`: `1` reads `i`, a quarter turn, and `ω` reads `-1`. Its
entries are rationals with no π (`T.rot_mem_specialOrthogonalGroup`), two pairs read the same exactly when they lie on
one line through the origin (`T.rot_eq_rot_iff`), and every rotation with rational entries is some pair's
(`T.rot_surjective`).

A projection is `Projection<R>`: a name, `apply(T)`, which cites the Lean map it implements, and
`display(R)`. `Result.RatioValue.read(projection)` gives the reading itself, and `readings()` all of them,
written. A new reading is one more class and one line in `Projections.ALL`.

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
`sin²(θR − θL) = 1/(N(L)·N(R))` (`T.sin_sq_dial`). `RationalTrig.dialed` can stop at the first depth where that
denominator reaches a bound, since every depth is one the theorems cover.

In T(T,T) Compound Ratio these are the built-in functions `cos(t)` and `sin(t)`, with `t` in turns:

```
> cos(1/6)
= 1761923520/3523847042
    classical  880961760/1761923521
    ...
    cos(1/6)   depth 26, between T(40545,70226) and T(29681,51409), sin² of the gap 1/23171366679119247242
> cos(1/6)^2 + sin(1/6)^2
= 154194255969364838344903323227965255696/154194255969364838344903323227965255696
```

- The turn is the argument's flat pair `T(a, b)`, as it is. `b` must be positive and at most the Recursion limits
  mode's bound. Any positive multiple of the turn gives the same answer, so `cos(2/12)` is `cos(1/6)`.
- The answer is the unreduced pair `T(q² − p², N)` (or `T(2pq, N)`) of the pair the descent ends on. `cos(1/4)`
  is `0/2`.
- With one argument the descent goes until a recursion limit stops it. When the target is exactly a pair's turn
  (a multiple of a quarter turn, after halving), the width grows too slowly, and it is the step limit that stops it.
  `cos(t, n)` dials to depth `n`, the Lean's own `n`; it is refused, not cut short, if a limit would stop it first.
- The reading named after the call is its certificate: the depth, which limit stopped it if not the precision, the
  bracket `L, R` of the halved turn's remainder, and `sin²` of the angle between them.

### Recursion limits

The second modeset bounds the descent. Every bound is a count, never a time, so a line answers the same on every
machine. The descent stops at the first of: the precision, `N(L)·N(R) ≥ 2^precision`; the step limit; or a
comparison that would walk a power larger than the size limit. Its size is about `8b` times the mediant's bits,
and that is what each comparison's cost grows with.

| key | precision | steps | `b` up to | power size | slowest measured |
|---|---|---|---|---|---|
| `shallow` | `2^-32` | 256 | 360 | 2^16 bits | under 0.1 s |
| `standard` (initial) | `2^-64` | 1024 | 1000 | 2^19 bits | 1.8 s, `cos(7/999, 1024)` before it is refused |
| `deep` | `2^-128` | 16384 | 10000 | 2^24 bits | 57 s `cos(500/2000)`, 94 s `cos(1/10000)`; `cos(333/999, 4096)` over 5 min; `cos(2500/10000)` and the largest explicit depths are estimated at tens of minutes to hours |

The slowest one-argument case in each mode is an exact quarter turn with the largest denominator, such as
`cos(250/1000)`, which runs to the step limit. With an explicit depth and a large `b`, the size limit is usually
what refuses it: in `standard`, `cos(333/999, n)` is refused past depth 102.
- `cos` and `sin` are built in and cannot be redefined. The other arithmetics refuse them for now.

## The notation

- Juxtaposition is multiplication at the precedence of `·` and `/`, left to right: `2x`, `ω(2x)`,
  `0(x+1)`, and `1/2x` is `(1/2)·x`. It binds looser than `^`, so `2x^2` is `2·(x^2)`.
- `^` is right-associative and binds tighter than a sign: `-2^2` is `-(2^2)`. `x²` is `x^2`.
- `·`, `*`, `×` all multiply; `/` and `÷` divide; `−` is minus.
- A run of letters is split into names: a defined name is taken whole, longest first, and any other letter
  stands alone. So `xy` is `x·y` unless `xy` has been defined.
- `name(…)` is a call only when `name` is a function; otherwise it is multiplication.
- `x = …` defines a variable and `f(x, y) = …` a function. Definitions are substituted when used, so a
  later change to a variable is seen by everything that uses it.

- A decimal literal is digits, a point and digits: `0.5`, not `.5` or `5.`. It is read as written, and the
  arithmetic decides whether it is a value.

**Not yet in T(T,T) Compound Ratio:** decimal literals, and exponents other than a whole-number literal (or a
variable bound to one). `T2.power` is proven for natural exponents only. Reading a decimal as a pair is a
choice (`0.5` as `T(5,10)` or `T(1,2)`) that the model has not made. IEEE Floating Point takes both.

## Citations

`@Lean("T2.flatten")` on an operation names the cott-lean declaration it implements, and
`@Proves("T2.flatten_plus")` on a test names the theorem it states. The test checks the same fact on the
nine named values and on many other pairs; the proof is what makes it hold for all of them.

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
