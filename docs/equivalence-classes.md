Traction Theory - Equivalence Classes
===

Traction Theory doesn't have normal forms - it has equivalence classes instead.
All fully simplified traction values belong to an equivalence class.
So, there may not be a unique normal form for every expression.
And since rewrites are always 2-way, it's technically neither confluent nor terminating,
but still has properties of both.
All values are either terminating and have a unique normal form (maybe),
or they are non-terminating and belong to an equivalence class (more likely).

### Primitives

Everything below is proved from these. Where a proof needs something outside this list,
it says so, and the item is filed under Chosen or Maybe rather than Proven.

```
E1   0^a · 0^b = 0^(a+b)          exponent addition is value multiplication
E2   -- withdrawn --              was (0^u)^v = 0^(uv). See "The cost of E2".
E3   0^(-a) = 1/0^a               NOT derived from E1: that route needs a - a = 0, an erasure
E4   0^1 = 0
E5   0^0 = 1
E6   0^ is injective              this is reversibility
E7   0^ is closed on {0,1,-1,w}   this is what "the type is a closure" means
E8   log_0 inverts 0^             a primitive in earnest now: log to a general base went with E2
E9   w := 1/0
E10  0^a - 0^b = 0^(a/b)          exponent division is value subtraction. TOTAL: defined at
                                  a = b as well, where the multiplicative erasure a/b
                                  materialises as 1, so y - y = 0^1 = 0 rather than discharging
```

The E2 slot is left empty rather than renumbered. E1 and E3-E10 are cited by number in the
other docs and in the commit history, and renumbering would silently rewrite all of it.

E10 and its totality are kept because this is the branch the Q model satisfies, so it is the
branch known to be consistent. That protection gets used below: where a conflict has to break
somewhere, it no longer breaks at E10.

Throughout, `x = 0^u`. Every traction value has such a u, and E6 makes it unique.

### The cost of E2

E2 said `(0^u)^v = 0^(uv)` for every v. What it was ever *proved* for is narrower than that,
and that part is a theorem of E1 rather than a casualty of the deletion:

```
Integer powers still flatten
    (0^u)^n = 0^u · ... · 0^u = 0^(u + ... + u) = 0^(nu), by E1, for positive integer n.
    A positive integer power is repeated multiplication, so E1 does this on its own.

    Negative n: E3 gives it at base 0. For a general base it needs y^(-n) = 1/y^n, which
    is a convention here and not a consequence -- flagged, not assumed.

    n = 0 is NOT included. Zero copies is the empty product, and calling that 1 is x^0 = 1,
    which this theory does not have.

    Non-integer v is NOT included. (0^(u/n))^n = 0^u makes 0^(u/n) AN n-th root of 0^u;
    identifying it with THE root (0^u)^(1/n) needs uniqueness of roots, and nothing here
    supplies that.
```

So the deletion removes the power rule at `v = 0`, at `v = w`, and at every non-integer
rational -- which is exactly where E2 was unconstrained anyway. The old note "E2 admits no
translation on nonzero rationals" is what remains, restated: on the integers there was never
a choice, and off them there was never a rule.

What this costs is totality of `^`, and in a theory whose premise is totality that is a real
cost rather than a tidy-up. See theory-problems.md, problem 3.

### Proven

Locked in and safe. Follows from the primitives alone.

```
w = 0^-1 = 1/0
    E3 at a=1: 0^-1 = 1/0^1 = 1/0 = w, by E4 and E9.

0 = 0^1 = 1/w
    E4; and 1/w = 1/(1/0) = 0.

1 = 0^0 = 1^1
    E5; and 1^1 is one copy of 0^0, so 1^1 = 0^0 = 1 by the integer power rule.

0^x = w^(-x)   and   w^x = 0^(-x),   for integer x only
    w^(-x) = (0^-1)^(-x) = 0^x by the integer power rule, which reaches integer exponents
    and no further. At a general traction exponent both lines went with E2.

-0 != w
    0 - 0 = 0^1 - 0^1 = 0^(1/1) = 0^1 = 0, by E10 and its totality.
    -0 = w would require 1/1 = -1.
    This rules out w and nothing more. It does NOT reach -0 = 0: reading the expression
    0 - 0 as -0 needs the value 0 to be the additive identity, and universal invariance does
    not say that -- it says the erasure FORM adds without effect. The value of -0 comes from
    the negation rule under Chosen instead.

0·x = 0^(1+u)
    0^1 · 0^u = 0^(1+u), by E1 and E4.
    Multiplication by zero shifts the exponent by one. Total, and reversible: u comes back.

w·x = 0^(u-1)
    0^-1 · 0^u = 0^(u-1), by E1 and w = 0^-1. Multiplication by omega shifts the exponent
    down by one. This is the half of the old `wx` item that never needed E2.

0·0 = 0^2, so 0² != 0
    The 0·x rule at u=1; and 0^2 != 0^1 by E6.

0·w = 1
    The 0·x rule at u=-1 gives 0^(1 + -1), the only product with zero whose exponent is an erasure.
    RESOLVED: w is 1÷0 by E9, so 0·w is y·(1÷y), the multiplicative erasure, and an erasure discharges
    to the identity of its own operation. The additive erasure in the exponent is what the lift MAKES
    of it; the kind that decides is the kind of the operation it came from. See theory-problems.md #1.

0^(1/n) is nilpotent
    n copies of 0^(1/n) multiply to 0^(1/n + ... + 1/n) = 0^1 = 0, by E1 and E4 -- the
    integer power rule, so this survives the deletion intact.
    But 0^(1/n) != 0 = 0^1 by E6, since 1/n != 1.
    So traction has nilpotents of every order. Note this puts it outside the
    commutative rings that wheels and meadows are built on.
```

### Chosen

Choices will be kept until disproven or all options exhausted.
Everything derived from them is listed here too, not under Proven.

```
0^w = -1
    Forced by E6 and E7 plus the other three values: 0^ already 2-cycles {0,1},
    so it must permute {-1,w}, and 0^-1 = w leaves only 0^w = -1.
    Not independent. This is the leap. It never used E2 and the deletion does not touch it.

log_0(x) = 0^x  and  0^(0^x) = x
    Forced on {0,1,-1,w} by the above: once 0^w = -1, the map is an involution.
    Extending it to all x is a SEPARATE and much larger assumption. Untouched by the
    deletion, but it has lost most of its customers: the results that were built on the
    general form went with E2.

-(0^a) = 0^(a+w),  so  -0 = 0^(1+w)
    E1 and 0^w = -1: negation is multiplication by -1, which adds w to the exponent.
    Inherits the leap, and nothing above it is stronger.
    Note 0^(1+w) != 0^1 by E6, since w != 0. So -0 is a magnitude-zero point distinct from
    0 -- an opposite orientation, the way omega is -- rather than 0 itself. That is consistent
    with the Proven -0 != w, and it is what settles the old value-of--0 question in this
    branch.
    An earlier version of this file claimed the opposite, that negation is NOT multiplication
    by -1, deriving -0 = 0 from E10 plus "0 is the additive identity". RETRACTED: universal
    invariance does not supply that premise. +(x-x) = ∅ is about the erasure form, and the
    additive identity is kept separate from magnitude-zero on purpose -- see
    notation-and-terminology.md. What does supply the premise is the Maybe addition law, one
    rank lower than this, so the conflict is Maybe against Chosen. See theory-problems.md,
    problem 2.

the two axes do not mix, and a value is a + 0^b
    1 and -1 are the additive units; 0 and w are the multiplicative ones. Additive units add to
    each other, multiplicative units multiply with each other, and a value is one of each --
    a multiple of 1, plus a power of 0. Mixing them does not combine: 1 + w is 1 + w, 1 + 0 is
    1 + 0, and neither is a third thing.

    This is not a convention. It is forced by what the mixed reading costs.

    Mixing means reading an additive unit as a power of zero, since that is the only way an
    addition law can reach it: 1 = 0^0 by E5, so 1 + w becomes 0^0 + 0^-1, and the Maybe
    addition law makes that 0^(0·-1) = 0^(-0).
    That -0 is LOAD-BEARING. The answer depends on it -- if -0 is 0 then 0^(-0) is 0^0 is 1, so
    w would be an additive identity; if -0 is anything else the sum is something else. So an
    ordinary sum of two of the four points comes out resting on problem 2. Written as 0^(0-0)
    it rests on problem 1 instead, a hostless additive erasure in an exponent. Every road out of
    1 + w runs into something already open, and that is the argument: mixing makes ordinary
    arithmetic depend on questions the theory has not answered, and not mixing does not.

    The same reading fails a second way, independently. 0 - 1 read across the axes does not
    terminate: E10 sends it to 0^(1÷0), E1+E3 reads that exponent as 0^0 ÷ 0^1, which is
    0^(0-1) again. See theory-problems.md #6.

    In the engine: exponentOfZero reads 0 and w and not 1 or -1, and the coordinate addition
    declines when one operand has a zero coordinate and the other does not. What this cost, and
    it is the point of it: 1 + 0 used to answer 1, which is the claim that the value 0 is an
    additive identity -- problem 2 -- being settled by a collapse rather than by a rule.

log_a(b) · log_b(a) = 1
    The reciprocal law. It is a consequence of the general-base log rule that went with E2 --
    log_(0^a)(0^b) = b÷a is antisymmetric in a and b, and this is exactly that antisymmetry --
    so it is strictly weaker than E2 and buys back one identity rather than the law.
    It still needs the power law to PROVE: a = b^c gives b = a^(1÷c) only through
    (b^c)^(1÷c) = b. Adopted here on its own, for what it settles below.
    Note it survives c = 0, where the classical version dies: 1÷0 is w, a value this type has,
    so log_a(b) = w rather than undefined. That is the whole of what the next item uses.

x^0 on the closure set is the 4-cycle 0 -> 1 -> w -> -1 -> 0
    a = b^0 gives log_b(a) = 0, so log_a(b) = 1÷0 = w by the reciprocal law, so a^w = b. The
    map a -> a^w is a permutation of {0,1,-1,w}: injective by E6, closed by E7. Three of its
    four values are already known, and the fourth is then forced -- the same counting that
    forced the leap.

        1 -> 0      E5. 0^0 = 1 is 0 -> 1 in b -> b^0, so 1 -> 0 in its inverse
        0 -> -1     the leap, 0^w = -1
        w -> 1      forced: the only alternative is w -> w, and w^w = w would give
                    log_w(w) = w against log_x(x) = 1, so w = 1
        -1 -> w     forced by counting, nothing left to choose

    Inverting gives x^0:

        x       0     1     w     -1
        x^0     1     w     -1     0

    So 0^0 = 1 (E5, and it is the anchor rather than a consequence), 1^0 = w, w^0 = -1 and
    (-1)^0 = 0. NOT x^0 = 1: the map has no fixed point, which agrees with this engine already
    refusing that reading -- zero copies is the empty product.

    THE SQUARE OF IT IS A FREE PREDICTION. Applying ^0 twice gives 0 -> w, 1 -> -1, w -> 0,
    -1 -> 1, which is x -> -1÷x. There is nothing to tune, and it holds at all four:
    (0^0)^0 = 1^0 = w and -1÷0 = w; (1^0)^0 = w^0 = -1 and -1÷1 = -1; (w^0)^0 = (-1)^0 = 0
    and -1÷w = 0; ((-1)^0)^0 = 0^0 = 1 and -1÷-1 = 1. One disagreement anywhere would end the
    cycle, and there is none.

    Structurally: order the four as the cycle does -- 0, 1, w, -1 -- as the corners of a
    square. Then 1÷x swaps the opposite pair 0 and w and is a diagonal reflection; -x is the
    other diagonal; -1÷x is the half-turn, which is why it is this cycle squared; 0^ swaps
    adjacent pairs and is an edge reflection. Every operation the closure set has is a symmetry
    of that square, and ^0 is the quarter-turn that generates the group. All of them together
    are D4, of order 8.
    The permutations above are exact. The GEOMETRY is not established: 0 and w share a rational
    shadow, so the four are not at compass points of anything yet, and the square is
    combinatorial until something says otherwise.

    What it costs, and one thing it may buy. It rests on the reciprocal law and on the leap,
    which is why it is filed here and not under Proven. 1^0 = w is the value to check against
    the Q model before this is leaned on. And naming the square of the cycle "-1÷x" requires
    -0 and -w to be defined at all: if that identification holds it FORCES -0 = 0 and -w = w,
    and therefore that negation is not multiplication by -1 -- against the negation rule above.
    That is evidence on problem 2 by a route that has nothing to do with the retracted argument
    there; it is group structure on four points, not a premise about the additive identity.
    Flagged, not adopted.
```

### Maybe

Not 100% sure yet, but the evidence is strong.

```
0^a + 0^b = 0^(a·b)
    The mirror of E10: if subtraction of values is division of exponents, then
    addition of values is multiplication of exponents.
    Also follows from E1 plus the general involution, since an involution carrying
    + to · necessarily carries · to +.
    Never stated independently, so it is held one rank below the primitives.
    Never used E2, so the deletion leaves it where it was.

    It carries a consequence that was not noticed while it was only a mirror: it makes the
    VALUE 0 the additive identity. x + 0 = 0^u + 0^1 = 0^(u·1) = 0^u = x, since 1 is the
    multiplicative identity in the exponent. That is the premise the retracted -0 = 0
    argument needed, and it collides with the negation rule above, which gives
    -0 = 0^(1+w) != 0. So this law and negation-as-multiplication-by--1 cannot both stand,
    and this one is the lower-ranked of the two. See theory-problems.md, problem 2.
```

### Open

Filed False while E2 stood. Both refutations ran *through* E2, at exactly the two points E2
was unconstrained, so both are withdrawn rather than upheld. Neither is now believed: there
is simply no longer an argument either way.

```
x^0 = 1^x
    The refutation read (0^u)^0 = 0^(0·u) and (0^0)^x = 0^(0·x) and compared them by E6.
    Both readings were E2 at v = 0. The second line of the old refutation, "1^0 = 2, not 1",
    rested on 1^x = 1 + u, which went with E2 as well.
    REFUTED AGAIN, conditionally: the 4-cycle above gives x^0 = 1 at x = 0 and 1^x = 1^0 = w,
    which differ. So this is False if the cycle stands, by a route with no E2 in it -- but the
    cycle is Chosen, so this is one rank weaker than the old refutation rather than a return
    to it.

x^w = (-1)^x
    The same shape at v = w, withdrawn for the same reason.
    REFUTED AGAIN on the same terms: at x = 1 the cycle gives 1^w = 0 while (-1)^1 = -1.
```

Both are still filed here rather than under False, because what refutes them is Chosen. Off
the closure set they are untouched either way -- the cycle reaches four points, and problem 3
is about everything else.

### Withdrawn with E2

Nothing here is disproved. Each item lost its proof, and none of them has another route.

```
1^x = 1 + u                 used (0^0)^x = 0^(0·x) at a general x
0x = 0^(1^x)                built on 1^x
(-1)^x = 0^(wx)             used log_0(-1) = w through E2
(-1)^0 = 0^(0w) = 1^w       E2 at v = 0
1^w = 0^(0w)                E2 at v = w
wx = 0^((-1)^x)             built on (-1)^x; its E1 half survives as w·x = 0^(u-1)
log_(0^a)(0^b) = 0^(b/a)    was E2 + E6. A logarithm to a general base is undefined again,
                            which is why E8 is back to being a primitive in earnest
(0^u)^v, v non-integer      no rule
```

The largest consequence: `1^x`, `(-1)^x` and `x^0` -- three of the four spellings that used to
be one question -- are no longer connected to each other or to `0·w`. Problem 1 is now `0·w`
and `0^(0w)` alone.

### Notation still open

```
0wx
    No longer blocked. 0·w is 1, so the product reading 0·w·x is x, and the reading (0·w)^x is 1^x.
    Those are different, so the notation still needs a choice of grouping -- but it is a notation
    question now rather than an open value.
```
