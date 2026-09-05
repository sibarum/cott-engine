Theory Problems
===

Open problems, in the order they block things. Rule labels E1-E10 are from
equivalence-classes.md. E2 is withdrawn, and problem 3 is now the largest of these.

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
values as `0w` does.

**This problem got smaller when E2 went.** `1^w` and `(-1)^0` used to be the same term as
`0^(0w)`, by `1^w = (0^0)^w = 0^(0·w)` and `(-1)^0 = (0^w)^0 = 0^(w·0)`. Both readings were E2
at the two points E2 never reached. So the four spellings are no longer one question: `0·w` and
`0^(0w)` are this problem, and `1^w` and `(-1)^0` are now instances of problem 3 — terms with no
rule at all.

**Status of the arguments.** Of the six derivations tried, the four dead ones are dead for
different reasons than before:

```
(0^w)^0 = 1, so 0w = 0
    Used y^0 = 1. It now fails one step earlier: nothing flattens (0^w)^0 at all, because the
    integer power rule does not reach n = 0. DEAD, and more thoroughly than before.

x^0 = 1^x  forces  0w = 0
x^w = (-1)^x  forces  0w = w
    These were rejected because both lines were False. Both refutations were E2 arguments and
    are withdrawn, so these two are no longer rejected -- they are CONDITIONAL on two
    statements that are now open (problem 3). They cannot be used, but they are no longer
    evidence against anything either.

log0(?) = 0w, divide by 0, so ? = 0 and 0w = 1
    Dividing by 0 is multiplying by w, so 0w/0 = 0w·w = 0w². Stripping the 0w off the
    left requires 0w = 1. CIRCULAR. Unaffected.

0·log0(0^w) = 0w, therefore log0(0^w) = 0w²
    Same circularity. With log0(0^w) = w from the involution, it restates 0·w = w·0.
    VACUOUS. Unaffected.
```

One argument survives, in three costumes, and all three are E1/E3/E8 arguments that never
touched E2:

```
0w = 0/0 = 0^1/0^1 = 0^(1-1) ~= 0^0 = 1
log0(0w) = log0(0) - log0(0) = 1 - 1 ~= 0,  so 0w = 1
log0(0^1 · 0^-1) = 1 + -1 ~= 0,             so 0w = 1
```

All three end at the same step: materialising the additive erasure `1 + -1` as the value `0`.

**So the whole problem reduces to one question:**

> May an additive erasure be materialised as the value 0 inside an exponent?

- **Yes** → `0w = 1`, and therefore `0^(0w) = 0^1 = 0`.
- **No** → `0w` is the erasure, `0^(0w)` is an erasure in a parameter slot, and the term is
  ill-formed rather than multi-valued.

notation-and-terminology.md says erasure "is always the result of an operation, never the
parameter for one," which reads as **no**. What **yes** costs is no longer `(-1)^0 = 0`: that
consequence went with E2, since `(-1)^0` is no longer this term. So the consequence that was
rejected on sight each time it appeared is no longer attached to this question, and **yes** is
cheaper than it was.

**Half a proposed rule, now adopted.** The exponent slot of `0^` is additive, since
`0^(a+b) = 0^a · 0^b`. So an erasure landing there is at home or foreign depending on its kind:

| in the exponent of `0^` | kind | context | behaviour | status |
|---|---|---|---|---|
| `1 - 1` | additive | home | erases — the term discharges | still open: this problem |
| `1 / 1` | multiplicative | foreign | materialises as `1` | ADOPTED with total subtraction |

The foreign row is no longer a proposal: the totality of E10 is exactly that row, and it is
what makes `0 - 0 = 0^(1/1) = 0^1 = 0`. The home row was always the other half, and it is
still the open one. The two halves were consistent as a pair, which is mild evidence for the
home row — but it has still not been derived from anything.

### 2) What negation does

The old conflict here is resolved, and it left a hole.

E10 with its totality gives `0 - 0 = 0^(1/1) = 0^1 = 0`. Reading `+(x-x) = ∅` as "0 is the
additive identity", the additive inverse of the identity is the identity, so `-0 = 0`. The
other route gives `-0 = 0·(-1) = 0^1 · 0^w = 0^(1+w)`, and those agree only if `w = 0`, which
E6 forbids. E10 is protected by this branch and `-1 = 0^w` is forced by E6 and E7, so the
casualty is `-y = y·(-1)`:

```
negation is not multiplication by -1
```

That was the cheapest of the four candidates and the only one never written down as an
assumption, so this is the expected outcome. But it removes the only exponent rule negation
had. `-(0^a) = 0^(a+w)` was that rule, and it *was* multiplication by −1.

**So the open question is now: what does negation do to an exponent?** Nothing in the
primitives says. E10 gives differences of two values, `0^a - 0^b = 0^(a/b)`, and `-0 = 0`, and
that is the whole of what is known. Note that `-(0^a)` cannot be recovered from E10 as
`0^0 - 0^a`, because that needs `0` to be the additive identity *and* a value-level rule for
unary minus, which is the thing being asked for.

One step to confirm before this is load-bearing: reading `+(x-x) = ∅` as "0 is the additive
identity". Everything else in the chain is E1, E6 and E10.

### 3) There is no power rule off the integers

This replaces the old "T(0) and T(w)", and it is larger than that problem was.

With E2 withdrawn, what remains of the power rule is a theorem of E1: an integer power is
repeated multiplication, so `(0^u)^n = 0^(nu)` for positive integer n, and for negative n
wherever `y^(-n) = 1/y^n` is granted. Nothing reaches:

```
v = 0            x^0        was False on E2's authority; now Open
v = w            x^w        was False on E2's authority; now Open
v non-integer    (0^u)^v    (0^(u/n))^n = 0^u makes 0^(u/n) AN n-th root of 0^u; calling it
                            THE root needs uniqueness of roots, which nothing supplies
```

Three things follow.

**Totality of `^` is lost.** what-is-traction.md says exponentiation is a primitive operation
of the type, as primitive as addition and multiplication, extending to any base. A primitive
operation of a COTT type is supposed to be total, and `^` now has a rule on a sparse subset of
its right operand. Either a general power law comes back in some form, or `^` is not primitive
in the sense the theory claims.

**`i` is no longer derived through logs.** The old derivation ran
`(-1)^(1/2) = 0^((1/2)·log_0(-1)) = (0^(1/2))^w`, which is E2 twice over. What survives is
weaker and still useful: `0^(w/2) · 0^(w/2) = 0^(w/2 + w/2) = 0^w = -1` by E1 and `0^w = -1`,
so `0^(w/2)` squares to −1 without any appeal to E2. So a square root of −1 is still exhibited
inside the type and needs no adjoining, but the half-power `(-1)^(1/2)` that names it is
undefined. See derivations.md.

**Two "False" entries became "Open".** They were refuted using E2 at exactly the two points
E2 was unconstrained, which was already flagged as suspicious when it was the old problem 3.
Nothing now argues for or against `x^0 = 1^x` or `x^w = (-1)^x`.

### 4) The involution off the closure set

`0^(0^x) = x` is forced on `{0,1,-1,w}` by injectivity and closure. Extending it to all x is a
separate and much larger assumption.

This was the largest unpaid assumption in the theory. It is still unpaid, but it is no longer
the largest, because almost everything that used it was built on E2 as well and went with E2:
`1^x = 1 + 0^x`, `0x = 0^(1^x)` and `wx = 0^((-1)^x)` are all withdrawn.

Its one remaining customer is the Maybe addition law `0^a + 0^b = 0^(a·b)`, which has a second
justification anyway (the mirror of E10). So adopting or rejecting the general involution now
costs much less than it did — and buys much less.

### 5) Nilpotents

`0^(1/n)` is nonzero by injectivity but n copies of it multiply to `0^1 = 0`. So traction has
nilpotents of every order. This survived E2's deletion intact, because repeated multiplication
is E1 and never needed E2.

This is not a problem, but nothing in the theory accounts for them yet, and it puts traction
outside the commutative rings that wheels and meadows are built on.
