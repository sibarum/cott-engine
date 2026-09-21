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
Each `p` and `q` is independently negatable.
This leads to `-1/1≠1/-1` and `1/1≠-1/-1`.
For compactness, `-1 = -1/1` and `_1 = 1/-1`, where underscore represents a bottom negative,
and `-_1 = -1/-1`.
Zero can only be bottom-negative `_0 = 0/-1` but not top-negative,
and omega can be top-negative `-ω = -1/0` but not bottom-negative.

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
| = 0ω | 0/0   | T(0, 0)  | none  | undefined | NaN     | The additive identity                                  |


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

Both sets of addition and multiplication are distributive.

```
(x + y) * z = (x * z) + (y * z)
(x ⊕ y) ⊗ z = (x ⊗ z) ⊕ (y ⊗ z)
```

Exponentiation is achieved by angle scaling.

```
T(a,b)^T(c,d) = tan((c/d)·arctan(a/b))
T^(x+y) = T^x ⊗ T^y
(T^x)^y = T^(x * y)
```

Each operation is reversible by its own inverse:

```
1/T(a,b)  = T(b,a) for *
-T(a,b) = T(-a,b) for +, ⊗
-_T(a,b) = T(-a,-b) for ⊕
```

# Extra utilities

Tangent half-angle substitution: returns a unit vector at double the angle.

```
z(T(a, b))  =  T( 2ab , b^2 − a^2 )
```

The Mobius transform.

```
E(T) = (1 + iT)/(1 − iT)           traction → unit circle
E^{-1}(v) = Im(v)/(1 + Re(v))      unit circle → traction
```

# Etc

Evaluation is depth-first.
Choosing one set of semantics at the time of construction can be helpful
to prevent +,* and ⊕,⊗ semantics from getting mixed up accidentally.

Theory: Every traction other than 0ω is reached exactly once by iterated mediant from the four seeds.

