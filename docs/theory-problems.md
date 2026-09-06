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

**Universal invariance does not reach this term, and here is why.** Invariance is indexed by
the operation: `+(z-z)` may be added anywhere, `*(z/z)` multiplied anywhere, and both hold
inside an exponent as readily as outside one — `1+(z-z)` and `1·(z/z)` are both still `1`. So
the exponent of `0^` is not a slot of one privileged kind, and an earlier version of this
section was wrong to call it additive and to sort erasures into "home" and "foreign" by that.

What separates this term from every invariance example is a **host**. In `x + (z-z)` the
erasure form is added *to* something, and that something is what survives. In `0^(1 + -1)` the
erasure form is the entire exponent; there is nothing for it to be invariant with respect to.
Writing `1 - 1 = 0 + (1 - 1)` to supply a host is circular — it assumes the value `0` is an
additive host, which is problem 2.

So the question is not which kind of erasure lands where. It is what a hostless erasure means
in a parameter slot, and the answer is either the value it would materialise as, or nothing at
all.

**One thing the branch does settle.** The totality of E10 is exactly the statement that
`0^(a÷a)` materialises as `0^1` rather than discharging, in the one case where the erasure is
hostless and multiplicative. That is `y - y = 0`, and it stands. It is evidence by analogy for
materialising the additive one too — the same situation, the other operation — but only
analogy: `0^(1 + -1)` has not been derived from it, and the two are not forced to agree, since
the whole point of matching on terms is that context is allowed to decide.

### 2) Negation, and which premise makes 0 the additive identity

**A retraction first.** This section previously concluded that negation is not multiplication
by −1, deriving `-0 = 0` from the totality of E10 plus `+(x-x) = ∅` read as "0 is the additive
identity". That reading is wrong. Universal invariance is indexed by the operation, not by a
position, and it is a statement about the erasure **form**: adding `x-x` to anything changes
nothing. The additive identity is deliberately kept separate from magnitude-zero — the value
`0 = 0^1` is a point of the type with an orientation and a reciprocal, and separating those two
jobs is the whole bookkeeping difference from the classic treatment. See
notation-and-terminology.md.

So `0 - 0 = 0^(1/1) = 0^1 = 0` says what the expression `0 - 0` is worth. Reading it as `-0`
needs the value `0` to be the additive identity, and E10 does not supply that.

**What the conflict actually is.** Two routes to `-0`:

```
(i)  0 - 0 = 0^(1÷1) = 0^1 = 0                   E10 with totality, THEN "0 is the additive
                                                 identity", which E10 does not give
(ii) -0 = 0·(-1) = 0^1 · 0^w = 0^(1+w)           E1, then -1 = 0^w
```

They agree only if `w = 0`, which E6 forbids. Route (ii) is E1 plus a Chosen item that E6 and
E7 very nearly force. Route (i) needs a premise from somewhere, and there is exactly one place
in the theory that supplies it:

```
the Maybe addition law makes the value 0 the additive identity
    x + 0 = 0^u + 0^1 = 0^(u·1) = 0^u = x,  since 1 is the multiplicative identity
    in the exponent.
```

That was not noticed while `0^a + 0^b = 0^(a·b)` was read only as E10's mirror. So the conflict
is **the Maybe addition law against negation-as-multiplication-by-−1**, and the addition law is
the lower-ranked of the two. Nothing here forces the choice, but the ranking points one way.

**Where this leaves the values.** Taking route (ii), which is what the docs now record:

```
-0 = 0^(1+w),  and  0^(1+w) != 0^1 by E6
```

So `-0` is a magnitude-zero point distinct from `0`, with the opposite orientation, the way
omega is. Traction has more than one of these, which is the same feature that separates the
additive identity from magnitude-zero in the first place. The old `-0 != w` stays Proven and is
now the weaker statement of the two.

**What would settle it.** Whether `x + 0 = x` for the value `0`. If yes, the addition law
survives and negation is not multiplication by −1, and negation is then left with no exponent
rule at all. If no, negation keeps `a + w` and the addition law needs restating so that it does
not quietly make `0` an identity — for instance by being a rule about terms rather than values,
which is the distinction finding 5 in rule-combinations.md already relies on.

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

### 5) The two layers disagree at the four points

Found by implementing the rules, and it is the sharpest form of the `0·w` question rather than a separate
one. The engine has a projective coordinate layer — the rationals with omega, where a value is a pair — and
a traction layer, where a value is `0^a`. Both can answer some of the same expressions, and they do not
always agree.

```
w + w        coordinates: (1,0) + (1,0) = (0,0), which is 1
0·w          coordinates: (0,1) · (1,0) = (0,0), which is 1
             tractions:   0^(1 + -1), an erasure -- Problem 1, undecided
(-1)·(-1)    coordinates: (-1,1) · (-1,1) = (1,1), which is 1
             tractions:   0^w · 0^w = 0^(w+w) = 0^1 = 0, by E1 and the leap
1 + w        coordinates: (1,1) + (1,0) = (1,0), which is w
```

The last line is why the negation rule cannot simply be switched on: `-0 = 0^(1+w)` becomes `0^w`, which is
`-1`. And the third is why `-1 = 0^w` is not used as a reading in the engine at all — wiring it makes the
square of minus one zero.

So the coordinate layer has already answered Problem 1, in the materialise direction, by the
`(0,0) -> (1,1)` normalisation in its constructor. That normalisation is deliberate and has a test on it.
Either it is the answer, in which case Problem 1 is settled and the docs should say so; or the coordinates
are a model that is wrong about omega, in which case `w + w = 1` needs to go and `0·w` stays open.

Nothing here is decided. What the engine does meanwhile: the traction rules run first and leave the
undecided cells standing, three of the four points read as powers of zero, and the fourth — the leap — is
kept where the two layers cannot meet.

### 6) Nilpotents

`0^(1/n)` is nonzero by injectivity but n copies of it multiply to `0^1 = 0`. So traction has
nilpotents of every order. This survived E2's deletion intact, because repeated multiplication
is E1 and never needed E2.

This is not a problem, but nothing in the theory accounts for them yet, and it puts traction
outside the commutative rings that wheels and meadows are built on.
