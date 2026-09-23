Traction Theory
===

A traction is an oriented projective rational.

Let `T` represent the ratio of two rationals (or subexpressions) p and q.

```
T(p,q) = p/q ≈ tan(arg(q + i*p))
```

# Values

Tangent is the "Rosetta Stone" of this model.
The classic limitation of IEEE floating point that necessitates the atan2(,) function in computing
is the same reason why traction adopts its unique top & bottom sign, and why the 0/0 element is
in the set, but behaves like a recoverable singularity.

*The four signed seeds are the unit group of ℤ\[i].*
They are exactly the four pairs `⊗` can undo.
Each `p` and `q` is independently negatable.
This leads to `-1/1≠1/-1` and `1/1≠-1/-1`.
For compactness, `-1 = -1/1` and `_1 = 1/-1`, where underscore represents a bottom negative,
and `-_1 = -1/-1`.
Zero can only be bottom-negative `_0 = 0/-1` but not top-negative,
and omega can be top-negative `-ω = -1/0` but not bottom-negative.

Every pair is distinct: not because invariants are removed, but because no invariant is specified by
default. Equality is coordinate equality, so `T(1,2) ≠ T(2,4)`.
The ratio, the ray (a positive multiple of both coordinates), the angle and the norm are invariants
a use may specify. Where a law below holds only under one of them, it says which.

| T    | p/q   | T(p,q)   | θ     | tan(θ)    | IEEE 64 | Notes                                                  |
|------|-------|----------|-------|-----------|---------|--------------------------------------------------------|
| =  0 | 0/1   | T(0, 1)  | 0     | 0         | +0      | Zero has a unit denominator by default                 |
| =  1 | 1/1   | T(1, 1)  | π/4   | 1         | 1       | The common unit                                        |
| =  ω | 1/0   | T(1, 0)  | π/2   | +inf      | +inf    | Reciprocal of Zero has a unit numerator by default     |
| = _1 | 1/-1  | T(1,-1)  | 3π/4  | -1        | -1      | Bottom negative                                        |
| = _0 | 0/-1  | T(0,-1)  | π     | 0         | -0      | Zero can only be bottom-negative                       |
| =-_1 | -1/-1 | T(-1,-1) | -3π/4 | 1         | 1       | Double negative, distinct from positive                |
| = -ω | -1/0  | T(-1,0)  | -π/2  | -inf      | -inf    | Omega can only be top-negative                         |
| = -1 | -1/1  | T(-1,1)  | -π/4  | -1        | -1      | Top negative                                           |
| = 0ω | 0/0   | T(0, 0)  | none  | undefined | NaN     | The identity of ⊕; absorbing under +, *, ⊗             |


# Operations

The primitive operations for a traction include addition, multiplication, inverse, and reciprocal.
Subtraction and division are derived.
Exponentiation is achieved by angle scaling.

```
T(a,b) + T(c,d) = T(ad+bc, bd)         Normal fraction addition
T(a,b) ⊕ T(c,d) = T(a+c, b+d)          Addition of (b+a*i) + (d+c*i)
T(a,b) * T(c,d) = T(ac, bd)            Normal fraction multiplication
T(a,b) ⊗ T(c,d) = T(ad+bc, bd-ac)      Angle addition, arg(b+a*i) + arg(d+c*i)
```

Units:

```
1 = (1,1)  for *
0ω = (0,0) for ⊕
0 = (0,1)  for +, ⊗
```

`⊗` distributes over `⊕` exactly, on the coordinates.
`*` distributes over `+` up to a scale: with `z = T(c,d)`, the right side is the left side
with both coordinates multiplied by `d`.

```
(x ⊕ y) ⊗ z = (x ⊗ z) ⊕ (y ⊗ z)
(x * z) + (y * z) = d·((x + y) * z)
```

So `(x + y) * z = (x * z) + (y * z)` holds as written where `d = 1`,
holds for the ratio where `d ≠ 0` (for the ray where `d > 0`),
and where `d = 0` the right side is `0ω`: `ω * (0 + 1) = ω`, but `ω * 0 + ω * 1 = 0ω`.

Exponentiation is achieved by angle scaling.

```
T(a,b)^T(c,d) = tan((c/d)·arctan(a/b))
T^(x+y) = T^x ⊗ T^y
(T^x)^y = T^(x * y)
```

At integer exponents, where `T^n` is `⊗` taken `n` times (through `-T` when `n < 0`):
`(T^x)^y = T^(x * y)` holds exactly, for both signs.
`T^(x+y) = T^x ⊗ T^y` holds exactly when `x` and `y` have the same sign.
When they don't, `T^x ⊗ T^y` is `T^(x+y)` with both coordinates multiplied by `(a²+b²)^min(|x|,|y|)`:
one norm for each turn that went out and came back.
It is therefore exact at every exponent for the four seeds, whose norm is 1.
For example `T(1,2)^1 ⊗ T(1,2)^-1 = T(0,5)`.

Each operation has its own inverse:

```
1/T(a,b)  = T(b,a) for *
-T(a,b) = T(-a,b) for +, ⊗
-_T(a,b) = T(-a,-b) for ⊕
```

What each lands on, against its own inverse:

```
T(a,b) ⊕ T(-a,-b) = T(0, 0)            0ω exactly
T(a,b) ⊗ T(-a,b)  = T(0, a²+b²)        0 exactly for the four seeds; the ray of 0 otherwise, but 0ω at 0ω
T(a,b) + T(-a,b)  = T(0, b²)           0 exactly where b² = 1; the ray of 0 where b ≠ 0; 0ω where b = 0
T(a,b) * T(b,a)   = T(ab, ab)          1 exactly where ab = 1; the ray of 1 where ab > 0,
                                       of -_1 where ab < 0, and 0ω where ab = 0
```

So `-1 * 1/-1 = T(-1,1) * T(1,-1) = -_1`.

Knowing the result and one operand, the other comes back exactly from
`⊕` always, from `⊗` unless the known operand is `0ω`,
from `+` unless its denominator is 0, and from `*` unless either of its coordinates is 0.
From `⊚` (below) it comes back unless the known operand is on a light line, `a² = b²`,
and from `∥` unless its numerator is 0.
All of these are one condition. With `k` fixed, each product is a linear map of the other operand,
and its determinant is `k`'s norm in that product's ring (see Structure):
`a² + b²` for `⊗`, `b²` for `+`, `b² − a²` for `⊚`, `ab` for `*` and `a²` for `∥`.
The other operand comes back exactly when the norm is not 0.
`k` has an inverse, so that every result has a preimage, exactly when the norm is `±1`.
Under `⊗` and `⊚` those are the four seeds, under `*` they are `1, _1, -_1, -1`,
under `+` they are every `T(a, ±1)`, and under `∥` every `T(±1, b)`.

Under `*`, `ω` and `0` split a pair into its two coordinates, and `⊕` puts them back:

```
(x * ω) ⊕ (x * 0) = T(a, 0) ⊕ T(0, b) = x
```

So what `*` loses against a pair on an axis is exactly the other projection.
Against `T(a, 0)` with `a ≠ 0`, two operands give the same result exactly when their `x * ω` agree,
so keeping `x * 0` alongside the result gives the operand back.

`*` is the only product with projections like these.
A projection is a pair that is its own square, other than the ring's zero and unit.
In the family `ω² = a + b·ω` of Structure, one exists exactly when `b² + 4a = 1`, which is `*`'s ring.
Under `⊗`, `+` and `⊚` the only pairs that are their own square are `0ω` and `0`,
under `∥` they are `0ω` and `ω`, and under `*` they are `0ω`, `0`, `ω` and `1`.

Every product loses at most one integer. Against a `k` other than `0ω` whose norm is 0,
the result is fixed by its own numerator, one linear combination of the other operand's coordinates,
and keeping that operand's numerator as well gives it back.
Against `T(c, 0)`, `+` keeps `b` and loses `a`, and `*` keeps `a` and loses `b`.
Against `T(0, c)`, `*` and `∥` keep `b` and `a` respectively.
Against `T(c, c)`, `⊚` keeps `b + a`, and against `T(c, −c)` it keeps `b − a`: each light line keeps the
light-cone coordinate the other loses. `⊗` loses nothing except against `0ω`,
and against `0ω` every product loses both integers.
Where it does not come back, the operation is ambiguous there:
every operand has a distinct partner giving the same result.

# Structure

The value position is a wheel.
`(T, 0, 1, +, *, /)`, with `/x = 1/x = T(b,a)`, satisfies every wheel axiom at coordinate equality:

```
(x + y)z + 0z = xz + yz
(x + yz)/y = x/y + z + 0y
0·0 = 0
(x + 0y)z = xz + 0y
/(x + 0y) = /x + 0y
0/0 + x = 0/0
```

along with `+` and `*` being commutative monoids and `/` a multiplicative involution.
It is the wheel of fractions over ℤ with nothing identified.
The distributivity above, up to `d`, is the first of these laws.
The wheel's bottom element is `0/0 = 0 * ω = 0ω`.

In wheel theory, a wheel of fractions takes a quotient by a set `S` of multipliers,
and that is where an invariant would enter: `S = {1}` identifies nothing, which is `T`;
the positive integers give the ray; the nonzero integers give the ratio.
For every multiplicative `S`, `x ~ y` when `s·x = t·y` for some `s, t` in `S` is an equivalence,
and the quotient is a wheel. The ratio this gives keeps `0ω` apart from everything else.
`+`, `*`, `/`, `-`, `-_` and `⊗` survive every such quotient.
`⊕` survives only `S = {1}`, which identifies nothing, and `0 ∈ S`, which identifies everything.
So under the ray or the ratio the wheel and `⊗` remain, and the mediant and the rings below do not.

The value position also sits next to two structures from the division-by-zero literature.
`T(p,q) ↦ p/q`, with every `T(p,0)` going to the error element, maps T onto the rational common meadow.
It respects `+`, `*` and `-` exactly, and the reciprocal everywhere but at the quarter turns,
and no map onto the common meadow respects all four.
Bergstra and Ponse's fracpairs are T, with the reciprocal multiplied by the denominator.
Every law above that differs from its written form differs by one added residue `T(0,k)`,
and `x + T(0,k)` is `x` with both coordinates multiplied by `k`.

The exponent position is ℤ\[i]: `⊕` is `+`, `⊗` is `×`, `-x` is the conjugate, and `-_x` is the negative.

The two positions share `⊕`. Every product here distributes over it exactly, and makes a ring with it.
Two more products complete the family:

```
T(a,b) ⊚ T(c,d) = T(ad + bc, bd + ac)   Split-complex product, (b + a·j)(d + c·j) with j² = 1
T(a,b) ∥ T(c,d) = T(ac, ad + bc)        Parallel sum, 1/(1/x + 1/y)
```

What `ω` is under each:

| product | `ω` squared | `ω` in that ring    | the ring with `⊕`           |
|---------|-------------|---------------------|-----------------------------|
| `⊗`     | `_0`        | a square root of −1 | ℤ\[i], unit `0`             |
| `+`     | `0ω`        | the nilpotent ε     | ℤ\[ε], unit `0`             |
| `⊚`     | `0`         | a square root of +1 | ℤ\[j], unit `0`             |
| `*`     | `ω`         | an idempotent       | ℤ × ℤ, unit `1`             |
| `∥`     | `ω`         | the unit            | ℤ\[ε], with `0` as ε        |

`⊗`, `+` and `⊚` are one formula. Read `T(p,q)` as `q + p·ω` with `ω² = a + b·ω`:

```
T(p,q) · T(r,s) = T(ps + rq + b·pr, qs + a·pr)
```

This is every ring free of rank two over ℤ, by the same reading each time. `⊗` is `ω² = −1`, `+` is
`ω² = 0` and `⊚` is `ω² = 1`, the complex, dual and split-complex numbers. `ω² = −1 − ω` gives the
Eisenstein integers. `*` is `ω² = ω`, read as `q + (p − q)·ω` because its unit is `1`. It is not `⊚`:
the split-complex integers have no idempotent but `0` and `1`, so over ℤ the two rings differ, and
they agree only once 2 is invertible. `∥` is `+` through the reciprocal, which exchanges `0` and `ω` and
fixes `0ω`, so it is ℤ\[ε] again with those two roles exchanged.

`0ω` across both:

```
+, *, ⊗      absorbing       0ω + x = 0ω * x = 0ω ⊗ x = 0ω, and it is the zero of every ring above
⊕            the identity    0ω ⊕ x = x
/, -, -_     fixed           and 0ω is the only pair all three fix
```

The two halves meet in tangent addition.
Written with the wheel's operations, `tan(α + β) = (tan α + tan β)/(1 − tan α · tan β)` is `⊗`,
with both coordinates multiplied by the product of the denominators:

```
(x + y)/(1 − x * y) = (b·d)·(x ⊗ y)          x = T(a,b), y = T(c,d)
```

The wheel's side is `0ω` exactly when `b = 0` or `d = 0`, whatever the other angle was.
`x ⊗ y` is `0ω` only when `x` or `y` is, and at a quarter turn it loses nothing:
`ω ⊗ 1 = _1`, where the wheel's formula gives `0ω`.
A quarter turn in the result is not a collapse: `tan(45° + 45°)` is `T(2,0)` both ways.
Where `b·d < 0`, the wheel's side is on the opposite ray:
`_1 ⊗ 1 = T(0,-2)`, at 180°, where the wheel's formula gives `T(0,2)`, at 0°.

`⊚` is relativistic velocity addition in the same way, with `1` as the speed of light:
it adds rapidities, as `⊗` adds angles, and `T(p,q)` reads as `tanh` where `⊗` reads it as `tan`.

```
(x + y)/(1 + x * y) = (b·d)·(x ⊚ y)
```

The formula divides by zero in three places:
- `x * y = -1` is a pole, not a collapse. Both sides answer `T(k,0)`: `T(2,1) ⊚ T(-1,2) = T(3,0)`.
- `b = 0` or `d = 0` collapses the wheel's side to `0ω`, and `⊚` loses nothing, as with `⊗`.
- `⊚`'s own zero divisors are the two light lines. In light-cone coordinates `(q + p, q − p)`, `⊚`
  multiplies each coordinate separately. So `x ⊚ y = 0ω` exactly when each light-cone coordinate is zero
  in `x` or in `y`: `1 ⊚ -1 = 0ω` is `c` plus `−c`. `⊗` has nothing like this.

`1 ⊚ y` is a multiple of `1` for every `y`: light plus any velocity is light.
`-x` is the split conjugate, and `x ⊚ -x = T(0, b² − a²)`.

`∥` is the power sum `(xⁿ + yⁿ)^(1/n)` at `n = −1`. `n = 1` is `+`, and no other `n` stays on the
integer pairs. Classically `1/(1/x + 1/y)` and `x·y/(x + y)` agree. In the value position the first is `∥`
exactly, and the second is `(b·d)·(x ∥ y)`. So the second collapses against an open circuit `ω`,
where `ω ∥ y = y`. `ω` is the unit of `∥`, and `0 ∥ 0 = 0ω`.

# Extra utilities

Tangent half-angle substitution: returns a unit vector at double the angle.

```
z(T(a, b))  =  T( 2ab , b^2 − a^2 )
```

`z(T) = T ⊗ T = T^2`: the point at double the angle. Its norm is `(a²+b²)²`,
so the unit vector is `z(T)/(a²+b²)`, and that is `E(T)` below.

The Mobius transform.

```
E(T) = (1 + iT)/(1 − iT)           traction → unit circle
E^{-1}(v) = Im(v)/(1 + Re(v))      unit circle → traction
```

`E(T(a,b)) = (b + ai)/(b − ai) = z(T)/(a²+b²)`.
Read back as a pair, `T(Im v, 1 + Re v)` with `a²+b²` cleared from both coordinates,
`E^{-1}(E(T(a,b)))` is `T(2ab, 2b²)`: `T(a,b)` with both coordinates multiplied by `2b`.
At `ω` and `-ω`, where `b = 0`, it is `0ω`.

# Etc

Evaluation is depth-first.
Choosing one set of semantics at the time of construction can be helpful
to prevent +,* and ⊕,⊗ semantics from getting mixed up accidentally.

Iterated mediant from the four seeds: insert `⊕` between each pair of neighbours, starting from
`0, ω, _0, -ω` around the circle, and repeat. The first round gives `1, _1, -_1, -1`.
This reaches each `T(p,q)` with `gcd(p,q) = 1` exactly once, and reaches no other pair.
So every traction other than 0ω is a positive multiple of exactly one reached traction (the same ray),
while `T(2,4)`, `T(2,2)` and `T(0,2)` are not themselves reached.

# Formalized

Proved in Lean 4 with Mathlib, in `cott-lean`, for every pair, at coordinate equality:

| claim | file | theorem |
|---|---|---|
| `⊕`, `⊗` are ℤ\[i\] | `T/Gaussian.lean` | `ExponentPosition.ringEquiv` |
| the seeds are what `⊗` can undo | `T/Gaussian.lean` | `exists_otimes_eq_zero_iff` |
| distributivity, up to `d` | `T/ValuePosition.lean` | `distrib_scaled` |
| what each inverse lands on | `T/Gaussian.lean`, `T/ValuePosition.lean` | `otimes_neg_self`, `plus_neg_self`, `times_reciprocal_self` |
| when an operand comes back | `T/Recovery.lean` | `oplus_recoverable`, `otimes_recoverable_iff`, `plus_recoverable_iff`, `times_recoverable_iff` |
| the exponent laws | `T/Powers.lean` | `otimesPower_otimesPower`, `otimesPower_add` |
| `z` and `E` | `T/Mobius.lean` | `mobius_eq`, `doubleAngle_norm`, `invMobius_mobius` |
| the mediant from the seeds | `T/MediantTree.lean` | `reach_injective`, `range_reach`, `exists_unique_reached_ray` |
| the value position is a wheel | `T/Wheel.lean` | `isWheel`, `bottom_eq`, `zero_times_omega` |
| `0ω` across both | `T/Wheel.lean` | `zeroOmega_plus`, `zeroOmega_times`, `zeroOmega_otimes`, `zeroOmega_oplus`, `fixed_by_all_inverses_iff` |
| tangent addition, and where the wheel collapses | `T/TangentAddition.lean` | `tanAdd_eq`, `tanAdd_eq_zeroOmega_iff`, `otimes_eq_zeroOmega_iff`, `quarterTurn_contrast` |
| the quotient by `S` is a wheel; `⊕` does not survive it | `T/Quotient.lean` | `setoid`, `Q.isWheel`, `oplus_respects_iff`, `ratioRel_iff` |
| T and the rational common meadow | `T/CommonMeadow.lean` | `toQa_surjective`, `toQa_plus`, `toQa_times`, `toQa_neg`, `toQa_reciprocal`, `no_surjective_hom_Qa` |
| fracpairs | `T/Fracpair.lean` | `finv_eq_scale`, `toQa_finv`, `toQa_of_fracRel` |
| each discrepancy is one residue | `T/Residue.lean` | `plus_residue`, `distrib_residue`, `tanAdd_residue` |
| `⊕` with `+` is ℤ\[ε\]; `⊕` with `*` is ℤ × ℤ | `T/Dual.lean` | `DualPosition.ringEquiv`, `ProdPosition.ringEquiv`, `oplus_plus` |
| every quadratic ring, by one formula | `T/Quadratic.lean` | `QuadPosition.ringEquiv`, `otimes_eq_qtimes`, `plus_eq_qtimes`, `ProdPosition.quadEquiv`, `split_not_prod` |
| velocity addition, and the light cone | `T/Velocity.lean` | `velAdd_eq`, `lightCone_splitTimes`, `splitTimes_eq_zeroOmega_iff`, `splitTimes_recoverable_iff`, `infinity_contrast`, `one_splitTimes` |
| the parallel sum | `T/Parallel.lean` | `reciprocal_par`, `ParPosition.ringEquiv`, `ParPosition.reciprocalEquiv`, `par_recoverable_iff`, `parAdd_eq`, `open_contrast` |
| one norm decides recovery and inverses; the two projections | `T/Norm.lean` | `recoverable_iff_norm`, `exists_inverse_iff_norm`, `exists_plus_eq_zero_iff`, `exists_splitTimes_eq_zero_iff`, `exists_times_eq_one_iff`, `exists_par_eq_omega_iff`, `times_omega_oplus_times_zero` |
| each product's idempotents; `*` alone has projections | `T/Projection.lean` | `qtimes_idempotent_iff`, `times_idempotent_iff`, `par_idempotent_iff`, `times_eq_times_iff_of_q_eq_zero`, `times_recover_with_complement` |
| each product loses at most one integer | `T/Loss.lean` | `numerator_determines`, `qtimes_eq_qtimes_iff`, `qtimes_recover_with_numerator`, `plus_eq_plus_iff_of_q_eq_zero`, `splitTimes_eq_splitTimes_iff_of_p_eq_q`, `splitTimes_eq_splitTimes_iff_of_p_eq_neg_q`, `par_eq_par_iff_of_p_eq_zero` |

Not formalized: the angle column, `T(a,b)^T(c,d)` off the integers,
and that the power sum leaves the integer pairs for `n ∉ {1, −1}`.
The opposite ray where `b·d < 0` follows directly from `tanAdd_eq` and is not a separate theorem.
The wheel axioms are checked against the statements on Wikipedia and nLab.

