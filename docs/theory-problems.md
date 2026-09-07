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

**A second line of evidence, from somewhere else entirely.** The `x^0` 4-cycle squares to the
permutation `0 <-> w, 1 <-> -1`. Calling that permutation `-1÷x` — which is what it looks like,
and what makes it the half-turn of the square the four points form — requires `-0` and `-w` to
be defined, and it *forces* `-0 = 0` and `-w = w`. That is against the negation rule, and so
against route (ii).

This is not the retracted argument returning. That one read `+(x-x) = ∅` as "0 is the additive
identity" and was wrong about it; this one is group structure on four points and does not touch
the additive identity at all. Two independent routes now point the same way, and neither is
strong enough on its own: the 4-cycle is Chosen, and the identification of its square with
`-1÷x` is exactly the thing at issue. But it is no longer only the Maybe addition law pulling
against the negation rule.

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
Nothing now argues for or against `x^0 = 1^x` or `x^w = (-1)^x` — except the 4-cycle below,
which refutes both if it is kept.

**`v = 0` is settled on the closure set, and only there.** `x^0` is the 4-cycle
`0 -> 1 -> w -> -1 -> 0`, forced by the reciprocal law plus E6 and E7 — see
equivalence-classes.md, Chosen. That reaches exactly four points, by a counting argument that
is closure's and cannot extend past it. So `2^0` is untouched, and this problem is now about
`v = w` and about everything off the closure set rather than about `v = 0` as such.

Worth noting what the 4-cycle does *not* need: a power law. It reaches `v = 0` by inverting
the log rather than by raising anything, which is why it can settle a point E2 never
constrained even though E2 is gone.

### 4) The involution off the closure set

`0^(0^x) = x` is forced on `{0,1,-1,w}` by injectivity and closure. Extending it to all x is a
separate and much larger assumption.

This was the largest unpaid assumption in the theory. It is still unpaid, but it is no longer
the largest, because almost everything that used it was built on E2 as well and went with E2:
`1^x = 1 + 0^x`, `0x = 0^(1^x)` and `wx = 0^((-1)^x)` are all withdrawn.

Its one remaining customer is the Maybe addition law `0^a + 0^b = 0^(a·b)`, which has a second
justification anyway (the mirror of E10). So adopting or rejecting the general involution now
costs much less than it did — and buys much less.

### 5) Two readings of the same expression, and which one is in force

Not a contradiction to be resolved by killing one side. The engine has a projective coordinate layer — the
rationals with omega, where a value is a pair — and a traction layer, where a value is `0^a`. Some
expressions have an answer in both, and the two answers are answers to different questions.

`0·w` is the case that matters. On the real line it behaves as `0·w = 1`: two coordinate pairs multiply to
`(0,0)`, which is one. In the lift it is `0^(1 + -1)`, an additive erasure with no host, and that is
Problem 1. Both are right where they stand. What an engine needs is not a winner but a record of which
reading it is in, and the current one runs the traction rules first — "the finer reading wins" — so the
lifted reading is what a user sees, and the term stands.

**This section previously claimed a contradiction, on the strength of a defect.** The coordinate addition
cross-multiplied unconditionally, so `w + w` came out as `(0,0)` — one — while `2·w` was `(2,0)`. Adding a
value to itself and doubling it disagreed, and not only at omega: `1/2 + 1/2` was `(4,4)` against `(2,2)`
for `2·(1/2)`, and three thirds cubed the denominator to `(27,27)`. Cross-multiplication is how a common
denominator is *made*, and it was being applied when one was already there. A common denominator is now used
where there is one; nothing is reduced by it, and `1/2 + 1/3` is untouched.

What survives of the finding, with the arithmetic right:

```
0·w          coordinates: 1                 tractions: 0^(1 + -1), Problem 1
(-1)·(-1)    coordinates: 1                 tractions: 0^(w+w) = 0^(2w), standing
1 + w        coordinates: w
```

So the leap can be wired without producing a falsehood — it was `0^1 = 0` before the fix, and is `0^(2w)`
now, which is a term standing rather than a wrong answer. What it costs is that `(-1)·(-1)` stops answering
1, because the traction reading runs first and cannot finish. It would finish under `2w = 0`, which is the
same condition rule-combinations.md names for negation to be an involution, and which E6 gives from
`0^(2w) = 1 = 0^0`. That is one decision covering the leap, the involution and the square of minus one.

The engine meanwhile: traction rules first, undecided cells standing, three of the four points read as
powers of zero, and the leap kept out of the reading so ordinary arithmetic keeps answering.

### 6) When may a point be re-read as a power of zero? — RESOLVED

The answer: only the multiplicative units. 1 and -1 are the additive units, 0 and w the multiplicative
ones, and a value is `a + 0^b` with one of each. Reading an additive unit as a power of zero mixes the two
axes, and nothing needs it -- the operands that are on the traction axis are already written that way. So
`exponentOfZero` reads 0 and w and no longer reads 1, and the traversal now tries a shape before descending
into it. Both are in the engine.

What follows is the finding that led there, kept because the failure mode is instructive.

**The symptom.** E10 matches the shape `Addition(x, Negation(y))`. The engine reduces innermost-first, so it
turns `Negation(1)` into the literal `-1` before it ever looks at the sum — and by then the shape E10 needs
is gone. So E10 fires between tractions, where `Negation(0^3)` has no coordinate negation to collapse into,
and never between the four points, where it does. `0^2 - 0^3` answers by E10; `w - 1` answers by
coordinates, and the two layers do not agree there.

**The fix that is not a fix.** Try the shape before descending, and E10 fires — and then nothing
terminates:

```
0 - 1                       the shape E10 wants
  = 0^(1÷0)                 E10, reading 0 = 0^1 and 1 = 0^0
  = 0^(0^0 ÷ 0^1)           the exponent is a quotient of two points, so E1+E3 can read it too
  = 0^(0^(0-1))             and 0-1 is where this started
```

`0 - 1` and `1÷0` rewrite into each other, without end. Every one of the four points can always be re-read
as a power of zero — that is what E4, E5 and `w = 0^-1` say — so any rule that consumes points and produces
an exponent built out of points can go round again. It is the first paragraph of equivalence-classes.md
happening in the engine: rewrites are two-way, and a value is either terminating or belongs to an
equivalence class.

Innermost-first was not avoiding this. It was hiding it, by letting the coordinates reduce `1÷0` to omega
before any rule could see it as a quotient of tractions.

**So the question is not the traversal.** It is when a point may be re-read as a power of zero, and there
are at least two ways to answer it:

- **E9 outranks the derived readings.** `w := 1÷0` is a definition, so `1÷0` is omega and the chain stops
  at `0^w` — which is `-1` by the leap, agreeing with what the coordinates said all along. A definition
  beating a derived reading is principled, and it terminates.
- **The traction rules apply only where an operand is already a traction.** Also terminates, and gives up
  something: `0·w` would go to the coordinates and answer 1, undoing the one cell phase 2 was careful to
  leave standing.

Neither was needed. Not mixing the axes is cheaper than both and settles it without ranking one rule over
another: the cycle needed `1` read as `0^0`, and that reading was never legitimate.

What it corrected, beyond terminating: `w - w` was answering 1, because the coordinates sent `(1,0)+(-1,0)`
to `(0,0)` and the constructor sends that to one. E10 now reaches it and gives `0^(-1÷-1)`, whose exponent
is the multiplicative erasure, so it materialises to `0^1` and the answer is 0. Subtracting a thing from
itself gives zero at omega too, which is what totality was supposed to mean.

### 7) Nilpotents

`0^(1/n)` is nonzero by injectivity but n copies of it multiply to `0^1 = 0`. So traction has
nilpotents of every order. This survived E2's deletion intact, because repeated multiplication
is E1 and never needed E2.

This is not a problem, but nothing in the theory accounts for them yet, and it puts traction
outside the commutative rings that wheels and meadows are built on.
