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
> :quotient ratio
> ω(2x)-(0(x+1)+0^2(2x-1))/2
= ω
```

Run it with `mvn compile` and then `java -cp target/classes sibarum.cott.calculator.Repl`.

## What happens to a line

1. **Parse** the universal notation into a tree (`notation`). This layer is syntax only.
2. **Substitute** variables and inline functions. If a variable is still free, the expression is
   returned as it is.
3. **Evaluate** at Level 2, in `T2`: a literal `n` enters as `T2.of(T(n,1))`, and `+`, `·`, `-`, the
   reciprocal and `^` are `T2`'s own operations. `a - b` is `a + (-b)`, and `a / b` is `a · reciprocal(b)`.
4. **Flatten** the result to a flat pair, `T2(A, B) ↦ A / B`.
5. **Read** it under the chosen quotient: `none` (the pairs as they are, the default), `ray` (positive
   multiples identified) or `ratio` (non-zero multiples identified, `0ω` kept apart).
6. **Display** it: one of the nine named values by its name, otherwise `p` or `p/q`.

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

**Not yet:** decimal literals, and exponents other than a whole-number literal (or a variable bound to
one). `T2.power` is proven for natural exponents only. Parsing a decimal as a pair is a choice
(`0.5` as `T(5,10)` or `T(1,2)`) that the model has not made.

## Citations

`@Lean("T2.flatten")` on an operation names the cott-lean declaration it implements, and
`@Proves("T2.flatten_plus")` on a test names the theorem it states. The test checks the same fact on the
nine named values and on many other pairs; the proof is what makes it hold for all of them.

`CitationTest` fails the build when:

- a citation names a declaration that is not in cott-lean's `declarations.txt`, so a definition renamed
  or dropped in the Lean cannot silently survive here;
- a public method of the value layer (`sibarum.cott.traction`) cites nothing;
- a test of the value layer states no theorem.

It reads `../cott-lean/declarations.txt`, so cott-lean must be checked out beside this repository. CI
checks out both, on every push and once a day, since cott-lean can change without this repository
changing. The path can be changed with `-Dcott.lean.declarations=…`.

## History

The engine's history before this fresh start, under an earlier and different theory, is kept at the tag
`archive/cott-pre-traction`. Nothing here is carried over from it.
