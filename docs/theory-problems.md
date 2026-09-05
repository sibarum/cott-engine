Theory Problems
===

Open problems, in the order they block things. Rule labels E1-E10 are from
equivalence-classes.md.

### 1) 0·w

**The clean statement.** For any `x = 0^u`, multiplication by zero shifts the exponent by one:

```
0·x = 0^1 · 0^u = 0^(1+u)        E1, Proven
```

Total and reversible — u comes straight back. Put `x = w`, so `u = -1`:

```
0·w = 0^(1 + -1)
```

That is the ONLY value of x whose exponent sum is an erasure. Every other product with zero is
fine. So this is not "multiplication by zero is broken". It is one term, and it is the term where
the exponent addition discharges.

`0^(0w)` inherits the problem and adds nothing: `0^` is injective, so `0^(0w)` has exactly as many
values as `0w` does. `1^w` and `(-1)^0` are the same term again — all three are one question.

**Status of the arguments.** Six derivations were tried. Four are dead:

```
(0^w)^0 = 1, so 0w = 0
    Uses y^0 = 1. But x^0 = 1^x is False (equivalence-classes.md), and x^0 is not 1.
    REJECTED.

x^0 = 1^x  forces  0w = 0
x^w = (-1)^x  forces  0w = w
    Both lines are False. They hold only at fixed points of 0^, and the closure set has
    none — it is two clean 2-cycles. REJECTED.

log0(?) = 0w, divide by 0, so ? = 0 and 0w = 1
    Dividing by 0 is multiplying by w, so 0w/0 = 0w·w = 0w². Stripping the 0w off the
    left requires 0w = 1. CIRCULAR.

0·log0(0^w) = 0w, therefore log0(0^w) = 0w²
    Same circularity. With log0(0^w) = w from the involution, it restates 0·w = w·0.
    VACUOUS.
```

One argument survives, in three costumes:

```
0w = 0/0 = 0^1/0^1 = 0^(1-1) ~= 0^0 = 1
log0(0w) = log0(0) - log0(0) = 1 - 1 ~= 0,  so 0w = 1
log0(0^1 · 0^-1) = 1 + -1 ~= 0,             so 0w = 1
```

All three end at the same step: materialising the additive erasure `1 + -1` as the value `0`.

**So the whole problem reduces to one question:**

> May an additive erasure be materialised as the value 0 inside an exponent?

- **Yes** → `0w = 1`, and therefore `0^(0w) = 0^1 = 0`, and therefore `(-1)^0 = 0`.
- **No** → `0w` is the erasure, `0^(0w)` is an erasure in a parameter slot, and the term is
  ill-formed rather than multi-valued.

notation-and-terminology.md says erasure "is always the result of an operation, never the
parameter for one," which reads as **no**. Against that, `(-1)^0 = 0` is the consequence that
has been rejected on sight each time it appeared, and it is what **yes** costs.

**A proposed rule, not yet adopted.** The exponent slot of `0^` is additive, since `0^(a+b) =
0^a · 0^b`. So an erasure landing there is at home or foreign depending on its kind:

| in the exponent of `0^` | kind | context | behaviour |
|---|---|---|---|
| `1 - 1` | additive | home | erases — the term discharges |
| `1 / 1` | multiplicative | foreign | materialises as `1` |

This keeps the `-0` derivation (`0^(1/1) = 0^1 = 0`, foreign, materialises) and discharges
`0w` (home). It is consistent with both, but it has not been derived from anything.

### 2) Negation

Two routes to `-0` disagree:

```
(i)  0 - 0 = 0^1 - 0^1 = 0^(1÷1) = 0^1 = 0        E10, then 0 as additive identity
(ii) -0 = 0·(-1) = 0^1 · 0^w = 0^(1+w)            E1, then -1 = 0^w
```

These agree only if `w = 0`. One of these must go: E10, `-1 = 0^w`, `-y = y·(-1)`, or "0 is the
additive identity". The third is the cheapest — negation being multiplication by −1 is nowhere
written down as an assumption, it is inherited from ring habits.

Note what is actually proved today: `-0 != w`. The value of `-0` is open.

### 3) T(0) and T(w)

`(0^u)^v = 0^(uv)` admits no translation on nonzero rationals — repeated multiplication forces
the identity. It is unconstrained at `v = 0` and `v = w`, because neither is reachable that way
and the route to `v = 0` runs through `0^(u-u)`, an erasure.

Those two points are exactly `x^0` and `x^w`, the two False lines. A translation is permitted
there and nowhere else. Nothing yet says what it is.

### 4) The involution off the closure set

`0^(0^x) = x` is forced on `{0,1,-1,w}` by injectivity and closure. Extending it to all x is a
separate and much larger assumption, and most of the useful results use the general form —
`1^x = 1 + 0^x`, `0x = 0^(1^x)`, `wx = 0^((-1)^x)` all inherit it.

This is the largest unpaid assumption in the theory.

### 5) Nilpotents

`0^(1/n)` is nonzero by injectivity but `(0^(1/n))^n = 0`. So traction has nilpotents of every
order. This is not a problem, but nothing in the theory accounts for them yet, and it puts
traction outside the commutative rings that wheels and meadows are built on.
