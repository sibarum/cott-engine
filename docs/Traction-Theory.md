Traction Theory
===

A novel algebraic structure (Traction Theory) that totalizes the reciprocal of zero
via a two-component carrier and structural involutions, distinct from wheels and meadows.

```
               ^                   t
               |                 
              0^2               ·  2
               |  
              0^1=0             ·  1
             /   \
 <-1__-1=0^ω<  ∅  >0^0=1___2_>  ·  0
             \   /  
              ω^1               · -1
               |
              ω^2               · -2
               |
               v
n  ·     ·     ·     ·     · 
  -2    -1     0     1     2 
```

## Four Fundamental Units

{1, 0, -1, ω}

`ω = 1/0`

## Carrier

A traction is two rationals, of the form:

`(n, t) = n·0^t`

Which has a "real" part n and a traction part 0^t.

```
 1 = ( 1, 0) =      1·∅  =  1
 0 = ( 0, 1) =  ∅·(0^1)  =  0^1  =  0
 ω = ( 0,-1) =  ∅·(0^-1) =  0^-1 =  ω
-1 = (-1, 0) =     -1·∅  = -1
```

A zero in either coordinate means that axis contributes nothing. A zero exponent is an absent traction
part, since 0^0 is 1; a zero real part is an absent real part. Zero is the only number that can say
so. One is a coefficient a term could actually have, so a marker of one could not be told from a real
part that happens to be one — and a marker that is a value takes part in arithmetic and gets
absorbed, the way `1·1^1` can be argued into `1^2`. Zero cannot be a coefficient here, because a
coefficient of zero would annihilate and this theory has no annihilator.

So at most one coordinate is ever zero, and one of the two is always defined.

The coordinate (0,0) is not a member of the type, but can be used as a transient value denoting erasure.
It should be immediately translated to (1,0) or (0,1) as per the rules of discharge.

**An absent coordinate is skipped, not computed with.** In a product the real parts combine as `a·c`
with an absent one skipped, which is what ∅ means multiplicatively:

`2·0 = (2·∅, 0+1) = (2, 1)`, keeping its 2, where computing it would annihilate and dividing by zero
would then prove 2 = 1.

`0·0 = (∅·∅, 1+1) = (0, 2) = 0^2`

Negation has nothing to turn in an absent real part, so it materialises the -1 it is multiplying by:
`-0` is `(-1, 1)`, which is `-1·0`. And a real part of exactly one is the multiplicative identity,
which says nothing the marker does not, so it collapses back to the marker by `x·1 = x`: `1÷0` arrives
as `(1, -1)` and is omega, `(0, -1)`, rather than a second pair beside it.

A zero numerator away from `(0,1)` is not an absence but a multiple of the point zero: `0÷d` is
`(1÷d)·0`, and dropping the d would lose something multiplicative — a root, an orientation — that the
type is supposed to conserve.

Neither coordinate has a zero denominator, and neither needs one. ω is 0^-1, which the traction part
already spells, so `1/0` never has to be held as a pair of coordinates — it is E9, applied. What that
buys is that the coordinates stop disagreeing with the algebra at the one place they used to:

```
ω + ω = 2ω        not 1
ω ÷ 2 = (1÷2)·0^-1    not ω, so 0^(ω÷2) is writable
(-1)·(-1) = 1     not 0
```

Either coordinate may hold an expression rather than a number, and t has to. It nests — `0^(0^2)` —
it holds values that are not rationals — `0^ω`, and `0^(1+ω)`, which stands — and a term the theory
has not resolved has to be writable at all: `-1·0` is `(-1, 1)` and there is no rule for it. A
carrier closed over two number coordinates cannot hold that; it would have to answer.

## Axioms

E(x) = 0^x

```
E1    E(a + b) = E(a) · E(b)         Product Rule: E is a homomorphism (T,+) → (T,·)
E2    E(a ÷ b) = E(a) − E(b)         Quotient Rule: and carries ÷ to −
E3    E(−a) = 1 / E(a)
E4    E(1) = 0
E5    E(0) = 1
E6    E is involutive
E7    E maps {0, 1, -1, ω} onto itself
E8    log_0 = E⁻¹
E9    ω := 1/0
```

E2 here is the rule the older docs number E10. The power law those docs call E2 is not an axiom:
`(0^a)^n = 0^(a·n)` is a theorem of E1, by repeated multiplication — see Powers.

## Operations

The traction type is defined as the closure over these total, reversible operations:


### Operational Identity

The operational identity is ∅, erasure, which is not a member of the type.
Terms may be invented or erased by the operational identity.
Erasure occurs only when the operation matches the discharge.
If the operation doesn't match, a residue is left behind.
Additive erasure leaves a residue of zero.
Multiplicative erasure leaves a residue of one.

`x + (z-z) = x + ∅ = x`

`x · (z-z) = x · 0 = 0x`

`x + (z/z) = x + 1 = x+1`

`x · (z/z) = x · ∅ = x`

These four rules are summarized as:

`z-z=∅, additively`
`z/z=∅, multiplicatively`

Standing alone, an erasure discharges to the identity of its own operation:

`z/z = 1`, `z-z = 0`

Between two rationals the coordinates answer instead, and what they answer is `2÷2 = (2,2)`: one, at
coordinates that are not one. That is kept rather than overruled — it is the same fact that makes the
erasure something to read off the term, and nothing here reduces.

The kind of an erasure is fixed by the operation it came FROM, not the one it lands in. `0·ω` is a
product, so its erasure is multiplicative and discharges to 1, even though under E1 it presents as
`0^(1 + -1)`, which reads additive. The lift changes the kind.

### Unit Exponentiation

Exponentiation and logarithm are bijective over the units.

```
1  = 0^0  = 1^1   = (-1)^(-1) = ω^ω
0  = 0^1  = 1^ω   = (-1)^0    = ω^(-1)
ω  = 0^-1 = 1^0   = (-1)^ω    = ω^1
-1 = 0^ω  = 1^-1  = (-1)^1    = ω^0
```

The base-0 row is not a lookup. `0^0 = 1` is E5, `0^1 = 0` is E4, `0^-1 = ω` is E3 with E9, and
`0^ω = -1` is then the only value left if that row is a permutation — so the leap is forced here
rather than chosen.

**The other twelve cells are forced too, and that is what checks the table.** A table cannot be
checked by reading it, but it can be searched. Of all 4^12 tables with the base-0 row above, 216
keep `x^1 = x` and a logarithm at every base; four of those also have every column a permutation,
and all four compose; two of those four keep `0^x = ω^(-x)`; and one of those two keeps the
reciprocal law `log(b,a)·log(a,b) = 1`. This one. Nothing else fits.

The near-miss is worth recording, because it is four cells away — `^0` and `^ω` interchanged at the
two additive-unit bases:

```
                 ^0   ^1   ^w   ^-1
        0       1    0   -1    w
        1       0    1    w   -1
        w      -1    w    1    0
        -1      w   -1    0    1
```

It keeps everything above except the reciprocal law, and in exchange **every** unit exponent becomes
an involution — `(x^a)^a = x` for all four, the exponents acting as the Klein group rather than as
ℤ/4. The cost is that `1^ω` is ω, so `log(0,1)·log(1,0)` is `0·0` and not 1. The two readings are the
same choice seen twice: the `x^0` 4-cycle is what the reciprocal law forces, and the Klein group is
what all-involutions forces. Reading `x^0` as the Cayley transform `(1+x)÷(1−x)` on the four points
agrees with the cycle, and would make `2^0 = -3`; the near-miss is the other Möbius map of that
quadruple, `(1−x)÷(1+x)`, and would make `2^0 = -1÷3`.

The `x^0` row is the 4-cycle `0 -> 1 -> ω -> -1 -> 0`. It has no fixed point, so not `x^0 = 1`, and
it reaches nothing off the four units: `2^0` stands.

The cells with ω in the exponent are asserted, not derived. There is no product on the exponents to
write a power rule with in the ω-direction, so `ω^ω`, `1^ω` and `(-1)^ω` come from the table closing
and from nowhere else.

**`x^-1` is not `1÷x`.** They are two different operations, and this table is what says so:

```
x        0    1    ω   -1
x^-1     ω   -1    0    1        the fourth column above
1÷x      ω    1    0   -1        the reciprocal
```

They agree on the multiplicative axis and swap on the additive one. The reciprocal reading of `x^-1`
is refuted by the bijectivity above: if `1^-1` were `1÷1 = 1`, the base-1 row would read
`{ω, 1, 0, 1}` — one twice and -1 missing — and base -1 would read `{0, -1, ω, -1}`.

**And no rearrangement of the table fixes that.** The collision is between two cells that are both
forced, inside one row: `x^1 = x` gives `1^1 = 1`, and `x^-1 = 1÷x` gives `1^-1 = 1`. Two distinct
exponents, one value, so `1^·` is not injective and `log_1` does not exist. The same at -1, where
`(-1)·(-1) = 1` makes `1÷(-1) = -1 = (-1)^1`. No other cell takes part, so relaxing the leap or the
base-0 row changes nothing — searched exhaustively over all 4x4 tables, and the three conditions
together have no solutions at all.

What it costs to take `x^-1 = 1÷x` anyway is therefore fixed: `log_1` and `log_(-1)`, and with them
the composition law — of the sixteen pairs `(x^a)^b`, between four and eight stop being any `x^c`.
That is information being lost at two of the four units, in the way `1^n = 1` loses it classically.
The table as it stands is the reversible reading, which is why it is the one here.

What `^-1` is instead is the half-turn: `x^-1 = (x^0)^0` at all four points. And the four unit
exponents compose as ℤ/4, generated by `^0`:

```
^1  = the identity                 ^-1 = the half turn,      (^0)²:  0 <-> ω,  1 <-> -1
^0  = the quarter turn             ^ω  = three quarters,     (^0)³
      0 -> 1 -> ω -> -1 -> 0
```

`(x^a)^b = x^(a∘b)` for all sixteen pairs, where ∘ is ℤ/4 under `1 -> 0, 0 -> 1, -1 -> 2, ω -> 3`.
Note that ∘ is not the product of the exponents: `(x^0)^0` is `x^-1` and not `x^(0·0) = x^0`, which
is why a power rule at general exponents has to exclude `v = 0`. Read off the table, so it stands or
falls with it.

E3 is why the two ever looked interchangeable. It says `0^(-a) = 1÷0^a` — at base zero, where they
do agree, and every derivation here that writes a negative power is at that base. Off it, write
`1÷x` when the reciprocal is meant, which is what the operations below are stated in.

**The leap is a fold, not a reading.** `0^ω` becomes `-1`, and `-1` is not read back as `0^ω`.
Reading it back turns `(-1)·(-1)` into `0^(ω+ω)`, which is `0^(2ω)`, and no rule finishes that — so
the square of minus one would stand where the real coordinate answers 1. Folding under a real part
is needed as well as folding bare: E1 carries the real part in first, so `0^ω · 0^ω` reaches
`(-1)·0^ω` before anything can fold it, and `a·0^ω = -a`.

### Unit Logarithm

```
 1 = log_0  0 = log_1  1  = log_-1 -1   = log_ω  ω
 0 = log_0  1 = log_1  ω  = log_-1  0   = log_ω -1
 ω = log_0 -1 = log_1  0  = log_-1  ω   = log_ω  1
-1 = log_0  ω = log_1 -1  = log_-1  1   = log_ω  0
```

This is the table above read backwards, and that is how it is checked: `log_b(b^x) = x` for all
sixteen. `log_0` is E8 and is a primitive; the other three bases are the table's.

`log_0(-1) = ω` is the leap read backwards, and it is safe where the arithmetic reading is not.
Inverting is not arithmetic — no addition law is involved, the answer is an exponent rather than
something built out of points, and nothing anywhere produces a logarithm for it to feed.

### The Involution

Powers of zero are generally involutive:

`0^n=log_0(n)`, `0^(0^n) = n`

The carrier can't distinguish between these pairs.

On the four units this is forced, and the tables above have it. Off them it is an assumption, and it
is the load-bearing one: E2 depends on it, and so does the addition law below, by the same
conjugation. So it is not applied off the four — `0^(0^2)` stands, and 2 is not read as a power of
zero, which is why `2^3` is 8 by multiplying three copies rather than by an exponent sum.

### Addition

Traction addition:

`a·0^b + c·0^d = (a+c) · a^d · c^b · 0^(bd)`

Carrier addition:

`(a, b) + (c, d) = ((a+c)·a^d·c^b, bd)`

Note: `a`, `c`, `a^d` and/or `c^b` should roll into coordinate `t` if any evaluate to some power of {0,ω}.

Derivation:

```
a·0^b = 0^0^a·0^b                                       E6 Involution 0^0^x=x
        0^(0^a + b)                                     E1 Product Rule
        
a·0^b + c·0^d = 0^(0^a + b) + 0^(0^c + d)
                log_0(0^a + b) + log_0(0^c + d)                 E6 Involution 0^x=log_0(x)
                log_0( (0^a + b) · (0^c + d) )                  E1
                log_0( 0^a·0^c + d·0^a + b·0^c + bd )           Distributivity
                0^( 0^a·0^c + d·0^a + b·0^c + bd )              E6
                0^(0^(a+c)) · 0^(d·0^a) · 0^(b·0^c) · 0^(bd)    E1
                (a + c) · 0^(d·0^a) · 0^(b·0^c) · 0^(bd)        E6
                (a + c) · 0^(0^a)^d · 0^(0^c)^b · 0^(bd)        Power Rule
                (a + c) · a^d · c^b · 0^(bd)                    E6
```

**A factor mentioning an erased coordinate never got generated.** The four factors are the cross-terms
of the distribution above, and a cross-term with an erased part in it was never there to distribute.
So it is dropped rather than evaluated, and that is what makes the law come out right where reading
those factors as values does not:

```
0 + 1      a absent, d absent    (a+c) is c, and only c^b survives      = 1·1^1 = 1
1 + 1      b absent, d absent    only (a+c) survives                    = 2
0^2 + 0^3  a and c absent        only 0^(bd) survives                   = 0^6
```

The third is the mirror law in the older docs, `0^a + 0^b = 0^(a·b)`, so the two agree on bare powers.
Evaluating the dropped factors instead gives the wrong answers, and each one shows why the marker
cannot be a value: `a^d` at `d = 0` becomes `1^0`, which the cycle says is ω, and `0^(bd)` at
`b = d = 0` becomes `0^(0·0)`, which is `0^(0^2)` and stands. Written with explicit coefficients,
`1·0 + 1·0^0` is 2 where `0 + 1` is 1.

**Not adopted, and two holes and one disagreement stand in the way.** The derivation needs the general
involution off the four units, and it needs the Power Rule at exponents that are not integers — the
ω-direction included, where there is no rule at all. Those are the holes. The disagreement is at
`b = d`:

```
ω + ω     only 0^(bd) survives, so the law gives 0^((-1)(-1)) = 0^1 = 0.  Two omegas are 2ω.
0 + 0     the same cell: the law gives 0^(1·1) = 0.  Two zeros are 2·0.
```

The real part is a multiplicative slot, so its absence is a multiplicative erasure — and by the
residue rules above, a multiplicative erasure landing in a sum leaves a residue of one:
`x + (z/z) = x + 1`. The law drops it; the residue rule keeps it as one copy. Taking the law makes the
value 0 an additive identity, which is the collision the older docs already record against the mirror
law, so this is a choice rather than a defect.

**What holds instead.** Two terms alike in their traction part add by distributivity, which is the
residue rule applied to the real parts:

`a·0^b + c·0^b = (a+c)·0^b`, an absent real part counting as one copy

so `0 + 0 = 2·0`, `2·0 + 0 = 3·0`, `ω + ω = 2ω`, and `0^2 + 0^2 = 2·0^2`. Where the real parts erase
each other the sum is the additive erasure: `1·0 + (-1)·0 = 0`.

**`x + 0 = x` is not one of the rules. It is a fact about the projection.** Adding the point zero does
not move a value's shadow, and that is the whole of what it says; in the type both terms are still
there, and discarding one loses what the type exists to conserve. So `1 + 0` stands, and its shadow
is 1.

As a rewrite it also cannot be made to work. Any version of it breaks associativity, because it
discards a magnitude-zero term that a later erasure needed:

```
(1 + -1) + 2·0  =  2·0     the erasure vanishes and the multiple of zero survives
1 + (-1 + 2·0)  =  0       if 2·0 is absorbed into the -1 first, the 1 and the -1 then erase
```

Six pairs in a 2400-pair sweep of commutativity, associativity and reversibility behaved that way,
all of that shape, and all six are gone with the rule. What is left is two, and they are a different
question: a cancellation between coordinates gives the point zero when it is read as an erasure and
`0÷2` when the coordinates compute it, and those are different values here.

The same goes for the coordinates: `(1,1) + (0,1)` is `(1,1)`, so the model absorbs a point zero the
theory declines to, and it may not do that in a value either. In an EXPONENT it must — there the zero
is the absence marker and `1 + 0` is 1 — so the two sorts have to be told apart by position. That is
the narrow form of the exponents-and-values distinction, and it is the only place the engine needs it.

At unlike orders a sum stands: `1 + ω`, `0^2 + 0^3`, `1 + 0`, `0 - 1`.

### Multiplication

Traction multiplication:

`a·0^b · c·0^d = (a·c)·0^(b+d)`

Carrier multiplication:

`(a, b) · (c, d) = (a·c, b+d)`

Note: an absent real part is skipped rather than multiplied, and `a` and/or `c` roll into coordinate
`t` if either is a power of {0,ω} — see the Carrier.

Derivation:

```
a·0^b · c·0^d
  = (a·c) · (0^b · 0^d)        commutativity and associativity
  = (a·c) · 0^(b+d)            E1
```

At `d = -b` the exponent sum is an erasure. The operation is a product, so it is multiplicative and
discharges to 1:

`0·ω = 0^1 · 0^-1 = 1`, and `0^a · 0^-a = 1`

read off the terms and not off the coordinates — `0^(2÷2) · 0^(-2÷2)` is 1, though the exponents sum
to `(0,2)` rather than to the exponent zero — that sum is the additive erasure too, and discharges.

### Division

`(a, b) ÷ (c, d) = (a÷c, b-d)`, which is E1 with E3, and `z÷z = 1` for the same reason as above.

### Subtraction

E2 as a rule: `0^a - 0^b = 0^(a÷b)`, and it is total. At `a = b` the exponent is the multiplicative
erasure and materialises as the multiplicative identity, so `z - z` is `0^1`, which is the point
zero — for every z, read off the term. Reading it off the coordinates instead is what would break
it: `a÷a` at `a = (2,1)` is `(2,2)`, and `0^(2,2)` is not the point zero.

Both operands have to be bare powers of zero. 1 and -1 are the additive units and are not read as
powers of zero, so `0 - 1` is not an E2 case; it answers -1 because 0 is invariant under addition.
`2·0^a - 3·0^b` has no rule and stands.

### Powers

`(0^a)^n = 0^(a·n)` for nonzero integer n. This is E1 and nothing else: n copies of `0^a`, whose
exponents add. One mechanism for both readings — `2^3` is 8 by multiplying three copies, and
`(0^2)^3` is `0^6` because the product rule turns those copies into an exponent sum.

`n = 0` is not in it. Zero copies is the empty product, and calling that 1 is `x^0 = 1`, which the
`x^0` cycle refutes. Nor is n off the integers: the older docs widen it to rational n as a theorem
of E1 and E6, and in the ω-direction there is no rule to widen.

n has to be an integer AS WRITTEN. `(6,3)` is a coordinate pair, not the integer 2, so `0^(6÷3)`
stands where `0^2` would have flattened — asking what the pair projects to would be reducing.

**A negative n is a different claim, and it is not E3.** `(0^a)^-n = 0^(-a·n)` is usually cited to E3,
but E3 relates a negated *exponent* to the reciprocal — `0^(-a) = 1÷0^a` — and getting from there to a
negative *outer power* needs `x^-1 = 1÷x` at `x = 0^a` first. That is the identification the unit
table refutes, so this is Chosen rather than proven, and it holds only where `0^a` is on the traction
axis:

```
(0^2)^-1 = 0^-2     a = 2, on the traction axis
(0^0)^-1            a = 0, and 0^0 is 1: the table says 1^-1 = -1, not 0^(-0)
(0^ω)^-1            a = ω, and 0^ω is -1: the table says (-1)^-1 = 1, not 0^(-ω)
```

The same step is what a power rule at negative rational exponents needs, and what `0^x = ω^(-x)`
needs — both are stated as proven in the older docs, with "+ E3 at negative v" carrying the weight.
The weight it carries is this identification, not E3.

### Negation

Carrier negation:

```
-(a, b) = (-a, b),  and  -(∅, b) = (-1, b)
```

Negation turns the real part and leaves the traction part alone. Derivation:

```
-(a·0^b) = (-1)·(a·0^b) = (-1·a)·0^b = (-a)·0^b
                              negation distributes over the product
```

An absent real part has nothing to turn, so the -1 being multiplied by materialises there — which is
the same rule, since `(-1, 0)·(∅, b)` is `(-1, b)` once the absent coordinate is skipped.

It is an involution on the pair either way, it needs neither the leap nor an ω in an exponent, and it
agrees with the four units as the Carrier spells them: `-1` is `(-1, 0)`.

The route through the leap is the same claim written the other way round:

```
-(a·0^b) = (-1)·(a·0^b) = a · (0^ω · 0^b) = a·0^(b+ω)
                              the leap, then E1 + commutativity
```

The two agree at `-1`, where `0^ω` folds. Elsewhere the second needs `b+ω` in an exponent, which
stands, so the first is the one that answers.

`-(a, b) = (a, b-1)` does not hold. It sends 1 to `1·0^-1`, which is ω, against `-1 = (-1, 0)` —
and `b+ω` is not `b-1` unless `ω = -1`.

**-0.** There is no `-0` among the units. It is `-1·0`, the pair `(-1, 1)`, and no rule reduces it:

```
-0 = -1·0 = (-1, 1)          and it stands
```

`-1·0 = ω` is refutable, so the pair is not omega either:

```
-1·0 = 0^ω · 0^1 = 0^(ω+1)   the leap, then E1
0^(ω+1) = 0^-1               the conjecture, since ω = 0^-1
ω + 1 = -1                   E6, so ω = -2 as an exponent
-1 = 0^ω = 0^-2              substituting
1÷(-1) = 1÷0^-2 = 0^2        E3, and -1 is its own reciprocal
0^2 = 0^-2                   so 2 = -2 by E6
```

### Reciprocation

Traction reciprocation:

```
1/(a·0^b) = (1/a)·0^(-1·b)
```

Carrier reciprocation:

```
1/(a, b) = (1/a, -b),  and  1/(∅, b) = (∅, -b)
```

Derivation:

```
1/(a·0^b)  =  1/a · 1/(0^b)  =  (1/a)·0^(-b)
                                        E3
```

An absent real part stays absent, since ∅ is one multiplicatively.

This is the reciprocal, and it is not `x^-1`: see Unit Exponentiation. The two agree at 0 and at ω,
which is E3's axis, and differ at 1 and -1.

`1/0` is not this rule, and not arithmetic on coordinates: it is E9. The point zero is `(0, 1)`, and
`1/0 = ω = (0, -1)` — which is what the rule gives once 0 is read as `0^1`, so the two agree; but E9
is what licenses it, and a coordinate pair could not have held it without a zero denominator.

The reciprocal of 0 and of ω are each other, and neither is a special case:

`1/0 = ω`, `1/ω = 0`

# Roots of Unity

```
                  0^1
      0^{(ω+1)/2} ||| 0^{1/2}
             |\ |\/|\/| /|
             ||//\\|//\\||
          (0^ω)<--[∅]-->(0^0)
             ||\\//|\\//||
             |/ |/\|/\| \|
      0^{(ω-1)/2} ||| 0^{-1/2}
                  0^-1
```

Traction syntax enables construction of a variety of useful elements,
and provides a framework for performing operations on these elements
with a unified process.

## Imaginary Numbers

`i = 0^(ω/2)`

The following is one of the first derivations that led to the development of this theory:

```
i = (-1)^(1/2)
log_0(i) = (1/2) * log_0(-1)    Power Rule
log_0(i) = (1/2) * ω            E6, log_0(-1)=0^-1
i = 0^(ω/2)
```

Squaring:

```
i^2 = (0^(ω/2))^2 = 0^(2*ω/2) = 0^(1^2*ω) ~= 0^ω = -1
```

The 1^n term tracks sheet number. If sheet number is an invariant, it may further be simplified to -1.

### Infinitesimals

`ε = 0^(1/2)`

It's literally the square root of zero, and unlike dual numbers, it's not nilpotent,
nor does it square to an absorbing zero.

```
ε^2 = (0^(1/2))^2 = 0^(2/2) = 0^(1^2) ~= 0^1 = 0
```

### Split Complex

`j = 0^(0/2)`

Squares to 1.

```
j^2 = (0^(0/2))^2 = 0^((0*2)/2) = 0^(0 * 1^2) ~= 0^0 = 1
```

### Root of Undefined

1/0 is classically undefined. Traction doesn't have an undefined, so instead it has structure.

`k = 0^(-1/2)`

Squares to ω.

```
k^2 = (0^(-1/2))^2 = 0^((-2/2)) = 0^(-1*(1^2)) ~= 0^-1 = ω
```

## Prior Art

Wheels and meadows are two well-known theories that attempt to totalize division.
They do so by amputating the emergent structure.
Wheels collapse upon the arrival of a bottom element, meadows calls "0/0=0" and ends the party early.
Traction strives not only for totality, but information-conservation (reversibility) also.
