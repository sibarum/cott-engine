# cott-engine

[![Build and check citations](https://github.com/sibarum/cott-engine/actions/workflows/build.yml/badge.svg)](https://github.com/sibarum/cott-engine/actions/workflows/build.yml)

The traction calculator: an ordinary-looking calculator that divides by zero and gives indeterminate
forms a value, without error or contradiction. Under it is traction, the number model formalized and
proven in [cott-lean](https://github.com/sibarum/cott-lean).

cott-lean is the specification. This engine implements only what it proves: every operation cites the
Lean definition it implements, and every test of the value layer names the theorem it states. What the
Lean has not proven, the engine does not do.

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

| name | reading | Lean |
|---|---|---|
| `ray` | the class of positive multiples, by its representative | `T.Rel`, positive integers |
| `ratio` | the class of non-zero multiples, with `0ω` kept apart | `T.Rel`, `T.ratioRel_iff` |
| `classical` | what a classical calculator says: `p/q`, or `undefined` where `q = 0` | `T.toQa` |
| `angle` | `θ = arg(q + p·i)`, in degrees; the one reading that is not exact | `T.theta` |
| `point` | `q + p·i` | `T.toC` |

A projection is `Projection<R>`: a name, `apply(T)`, which cites the Lean map it implements, and
`display(R)`. `Result.RatioValue.read(projection)` gives the reading itself, and `readings()` all of them,
written. A new reading is one more class and one line in `Projections.ALL`.

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
