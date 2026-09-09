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
E2   -- not a primitive --        (0^u)^v = 0^(vu) at nonzero RATIONAL v is a theorem of
                                  E1 + E6, needing E3 at negative v. Reinstated under Proven
                                  as E2'. At v with a w-component there is no rule.
E3   0^(-a) = 1/0^a               NOT derived from E1: that route needs a - a = 0, an erasure
E4   0^1 = 0
E5   0^0 = 1
E6   0^ is injective              reversibility -- and this is what supplies uniqueness of roots
E7   0^ is closed on {0,1,-1,w}   this is what "the type is a closure" means
E8   log_0 inverts 0^             a primitive in earnest: log to a general base went with E2. WIRED
E10  -- no longer a primitive --  0^a - 0^b = 0^(a/b) follows from E1 + E3 + E8 plus the
                                  general involution, so it inherits that involution's status.
                                  Filed under Chosen.
E9   w := 1/0
```

Both vacated slots keep their numbers rather than being renumbered. E1 and E3-E10 are cited by
number in the other docs and in the commit history, and renumbering would silently rewrite all
of it. E10 is listed out of order, after E9, so that the eight remaining primitives read without
a gap in the middle.

**The E10 protection is withdrawn.** This file used to say that E10 and its totality were kept
because that is the branch the Q model satisfies, so it is the branch known to be consistent,
and that where a conflict had to break somewhere it no longer broke at E10. Neither half stands.
There is no Q model in this repository -- five prose references and no construction -- so the
protection rested on an artifact nobody has. And E10 is now derived rather than primitive, so it
cannot outrank the assumption it depends on. Conflicts that were arbitrated by E10 need
re-examining; the one that mattered has already moved, and is under Chosen at `-0 != w`.

Throughout, `x = 0^u`. Every traction value has such a u, and E6 makes it unique.

### What E2 cost, and what the restriction saves

E2 said `(0^u)^v = 0^(uv)` for every v, and was withdrawn on 2026-09-05 on two grounds: that it
was only ever proved for integer v, and that the branch without it is the one the Q model
satisfies. The withdrawal over-corrected. What the proof reaches is every **rational** v, and the
second ground has since gone -- see the protection note above.

**The one objection that mattered, answered.** This section used to say: `(0^(u/n))^n = 0^u`
makes `0^(u/n)` AN n-th root of `0^u`, and identifying it with THE root `(0^u)^(1/n)` needs
uniqueness of roots, "and nothing here supplies that." Something does, and it is E6.

```
Define a rational power the ordinary way: x^(p/q) is the y with y^q = x^p.

    existence     y = 0^(pu/q) works, by E1 at integer q
    uniqueness    y = 0^t with 0^(tq) = 0^(pu) gives tq = pu by E6, so t = pu/q

So E2' at POSITIVE rational v is a theorem of E1 + E6. Injectivity IS root uniqueness:
0^t is determined by t, so tq = pu has one solution and the q-th root is unique.
Negative rational v additionally needs E3 for the reciprocal, which is where E3 earns
its independence.
```

**What stays out, and why each is excluded for its own reason.**

```
v = 0                 Zero copies is the empty product, and calling that 1 is x^0 = 1,
                      which this theory does not have. This exclusion is load-bearing and
                      not merely inherited: without it E2' gives 1^0 = (0^0)^0 = 0^0 = 1,
                      against the cycle's 1^0 = w. The cycle keeps x^0.

v with a w-component  x^w needs a PRODUCT of two exponents. Exponents are Q·1 + Q·w, which
                      has addition and rational scaling and no product: w·w is (i·pi)^2,
                      which is -pi^2 and not of the form a + bw. So this is not a rule the
                      theory declines to state -- there is nothing there to state.

v irrational          The same reason 0^pi was never on the table.
```

**What comes back.** `0^(u/n)` is THE unique n-th root of `0^u`. `(0^a)^(6/3)` is licensed, so
the "integer as a term and not as a value" guard relaxes to "rational, and no w-component".
`sqrt(-1)` names itself again by a shorter route than the withdrawn one: `(-1)^(1/2)` is
`(0^w)^(1/2)` = `0^(w/2)`, where the base carries the w and the exponent is rational, so it is
inside the fence -- see derivations.md. A logarithm to a general base returns on the same-line
case, `log_(0^a)(0^b) = b/a` wherever `b/a` is rational, i.e. wherever a and b are parallel as
exponents. And the reciprocal law is Proven at rational c, since `(b^c)^(1/c) = b` is now
available there.

**What does not.** `1^x = 1 + u`, `(-1)^x = 0^(wx)` and `0x = 0^(1^x)` all needed v general
rather than rational, and stay withdrawn. `x^0 = 1^x` and `x^w = (-1)^x` stay Open: E2' excludes
both points, so neither old refutation returns. And the `x^0` cycle is NOT promoted -- it uses
the reciprocal law at `c = 0`, where `1/c` is w, which is exactly the case E2' cannot reach. E2'
proves the reciprocal law everywhere the cycle does not need it.

What is left of problem 3 is narrower than it was: not "no power rule off the integers" but no
power rule off the rationals. The remaining hole is the w-direction, and that hole is now
explained rather than merely reported -- there is no product on the exponents to write the rule
with. See theory-problems.md, problem 3.

**It cannot be wired yet.** `(-1)^2` is `(0^w)^2` = `0^(2w)`, and the coordinates put `w + w` at
1 -- the same defect that keeps the leap unwired. E2' at integer v on base `-1` walks straight
into it, so this waits on the exponent carrier along with the leap.

### Proven

Locked in and safe. Follows from the primitives alone.

```
w = 0^-1 = 1/0
    E3 at a=1: 0^-1 = 1/0^1 = 1/0 = w, by E4 and E9.

0 = 0^1 = 1/w
    E4; and 1/w = 1/(1/0) = 0.

1 = 0^0 = 1^1
    E5; and 1^1 is one copy of 0^0, so 1^1 = 0^0 = 1 by the integer power rule.

0^x = w^(-x)   and   w^x = 0^(-x),   for nonzero rational x
    w^(-x) = (0^-1)^(-x) = 0^x by E2', which reaches rational exponents and no further. This
    used to read "integer x only", because the integer power rule was all there was; E2'
    widens it to the rationals. At an exponent with a w-component both lines still have no
    rule. x = 0 is excluded with the rest of E2': it would give w^0 = 0^0 = 1, against the
    cycle's w^0 = -1.

E2'  (0^u)^v = 0^(vu),  for nonzero rational v
    Positive v: x^(p/q) is the y with y^q = x^p; y = 0^(pu/q) exists by E1 at integer q, and
    is unique because 0^(tq) = 0^(pu) gives tq = pu by E6. Negative v needs E3 as well.
    Integer v is the special case that was never in doubt: repeated multiplication, so E1
    gives it directly.
    Excludes v = 0, v with a w-component, and irrational v, each for its own reason. See
    "What E2 cost, and what the restriction saves".

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

there are no nilpotents, and the word is withdrawn
    This file used to claim, under Proven, that 0^(1/n) is nilpotent: n copies of it multiply
    to 0^(1/n + ... + 1/n) = 0^1 = 0, and 0^(1/n) != 0 by E6, so traction was said to have
    nilpotents of every order.

    WITHDRAWN. Eliminating nilpotency is the whole basis of the theory, and the word imports
    the one assumption traction rejects: a nilpotent is a nonzero element whose power reaches
    an ANNIHILATOR. Zero annihilates nothing here -- it has a reciprocal, w, and 0^1 is one
    rung of the traction axis rather than a floor. Reaching it is not dying.

    And the axis does not stop there. Even granting 0^(n/n) = 0^1, the value 0^(4/3) is still
    up the axis and no power of 0^(1/3) is going to account for it. Nothing has been killed,
    nothing has been lost, and 0^ is still injective. A word that says otherwise was never
    describing this type.

    What survives is the observation and not the label: 0^(1/n) is a value distinct from 0
    whose n-th power is 0, and that puts traction outside the commutative rings wheels and
    meadows are built on -- because in those, reaching zero is terminal, and here it is not.
```

### Chosen

Choices will be kept until disproven or all options exhausted.
Everything derived from them is listed here too, not under Proven.

```
E10  0^a - 0^b = 0^(a/b),  and it is E1 seen through the involution
    Was a primitive. It is derived, and what it needs is the general involution:

        0^(a/b) = log_0(a/b)               the general involution
                = log_0(a · 1/b)
                = log_0(a) + log_0(1/b)    E1 + E8
                = log_0(a) - log_0(b)      E3 + E8
                = 0^a - 0^b                the general involution, twice more

    Everything but line 1 and line 5 is Proven. Those two are log_0(x) = 0^x, which is
    forced on {0,1,-1,w} and asserted beyond -- theory-problems.md, problem 4. So E10's
    status is exactly that involution's status: Proven on the four points, Chosen off them.
    It needs no E2: the step from log_0(1/b) to -log_0(b) is E3 + E8 -- put x = 0^p, then
    1/x = 0^(-p) by E3, so log_0(1/x) = -p = -log_0(x) -- and NOT the log power law.

    THIS IS WHY E1 AND E10 MIRROR EACH OTHER. The docs have observed throughout that 0^
    exchanges the floors "in both directions" -- · with +, / with -. That is not two
    axioms that happen to be mirrors. It is one axiom seen through a map that swaps the
    floors, and E10 is the image. E10 is not independent of E1 + E3 + E8 given the
    involution. It does NOT collapse E3, which the derivation uses as a separate input.

    WHAT THIS COSTS. E10 can no longer arbitrate against problem 4, since it depends on it,
    and the protection it used to carry is withdrawn (see Primitives). What it buys is that
    two open items become one decision: adopt the general involution and E10 comes free with
    E1's own status; decline it and E10 is a four-point fact. There is no third branch where
    E10 stands and the involution does not.

    Totality at a = b is unaffected by the restatement: the multiplicative erasure a/b
    materialises as 1, so y - y = 0^1 = 0 rather than discharging. That reading is about
    the erasure and does not go through the involution.

    One caveat on the derivation: it needs a and b to be powers of zero for E1 + E8 to reach
    them, and that is an EXISTENCE assumption. E6 does not supply it -- E6 gives uniqueness.
    It is the assumption stated at the head of this file, "every traction value has such a u",
    and REVIEW.md P0-5 disputes it. Nothing in the derivation turns on it beyond what every
    other rule here already assumes.

-0 != w
    MOVED FROM PROVEN, because its proof was E10 and its totality:
        0 - 0 = 0^1 - 0^1 = 0^(1/1) = 0^1 = 0, and -0 = w would require 1/1 = -1.
    E10 is Chosen now, so this is too. There is no Proven route to it and there never could
    have been: -0 is DEFINED by the negation rule, which is Chosen, so a claim about what -0
    is not cannot outrank it. The cleaner proof is the negation rule's own -- -0 = 0^(1+w)
    and w = 0^-1, so -0 != w requires 1 + w != -1, i.e. w != -2, which holds because w is
    not rational.

    It still rules out w and nothing more. It does NOT reach -0 = 0: reading the expression
    0 - 0 as -0 needs the value 0 to be the additive identity, and universal invariance does
    not say that -- it says the erasure FORM adds without effect.

0^w = -1
    Forced by E6 and E7 plus the other three values: 0^ already 2-cycles {0,1},
    so it must permute {-1,w}, and 0^-1 = w leaves only 0^w = -1.
    Not independent. This is the leap. It never used E2 and the deletion does not touch it.

log_0(x) = 0^x  and  0^(0^x) = x
    Forced on {0,1,-1,w} by the above: once 0^w = -1, the map is an involution.
    Extending it to all x is a SEPARATE and much larger assumption. Untouched by the
    deletion, but it has lost most of its customers: the results that were built on the
    general form went with E2.

-0 = 0·(-1), and the canonical form stops there
    Negation is multiplication by -1. So -0 is 0·(-1), and it is NOT 0 - 0: that is the
    additive erasure, which discharges to the additive identity and is a different question
    entirely. Conflating the two is what made -0 look ambiguous.
    Pushing further is available and is not the canonical form: 0·(-1) = 0^1 · 0^w = 0^(1+w)
    by E1 and the leap, and 0^(1+w) != 0^1 by E6 since w != 0. So -0 is a magnitude-zero point
    with the opposite orientation, the way omega is, and not 0 itself -- consistent with the
    Proven -0 != w. But the form to write is 0(-1).
    In the coordinates it has a literal of its own: (0,-1), where the sign sits on the
    denominator because a zero numerator has none to carry. Negating the numerator returned the
    same pair, so the engine used to answer -0 = 0; both negation and multiplication now put
    the sign where it fits, and 0·(-1) and -0 land on the same literal.

    A consequence, observed and KEPT rather than filed as a defect: 1 + 1÷(-1) is -0 and not 0.
    1÷(-1) is (1,-1) -- minus one with the sign on the denominator, which is what the reciprocal
    has always produced -- and the sum cross-multiplies like this: the numerators
    cancel, 1·(-1) + 1·1 = 0, and the -1 in the denominator does not. So what survives the
    cancellation is the ORIENTATION: the magnitude goes and the sign stays, which is the kind of
    conservation this theory is built on rather than an accident to be normalised away.
    It does mean (-1,1) and (1,-1) are both minus one and behave differently in a sum. Left as
    it is until it produces a contradiction; do not "fix" it by moving signs between the slots,
    which is the one thing this carrier does not do.

-(0^a) = 0^(a+w)
    E1 and 0^w = -1, for a traction rather than for the point. Inherits the leap, and nothing
    above it is stronger.
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
    It needs the power law to PROVE: a = b^c gives b = a^(1÷c) only through (b^c)^(1÷c) = b.
    E2' now supplies that at nonzero rational c, so the law is PROVEN there and this entry is
    only Chosen at the cases E2' cannot reach.
    And that is exactly where the cycle uses it. Note the law survives c = 0, where the
    classical version dies: 1÷0 is w, a value this type has, so log_a(b) = w rather than
    undefined -- and c = 0 is the one point E2' excludes. So E2' proves the reciprocal law
    everywhere the next item does not need it, and the cycle stays Chosen.

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

    THE SQUARE OF IT IS 0 <-> w, 1 <-> -1. Applying ^0 twice gives 0 -> w, 1 -> -1, w -> 0,
    -1 -> 1, and there is nothing to tune about THAT: it is the cycle, squared.

    CALLING IT x -> -1÷x IS A FURTHER STEP, AND IT IS NOT FREE. Worked in this theory's own
    arithmetic, using -1 = 0^w and E1+E3, it holds at two of the four and not at the other two:

        (1^0)^0    = w^0    = -1    -1÷1  = 0^w ÷ 0^0 = 0^w      = -1     holds
        ((-1)^0)^0 = 0^0    = 1     -1÷-1 = 0^w ÷ 0^w = 0^0      = 1      holds
        (w^0)^0    = (-1)^0 = 0     -1÷w  = 0^w ÷ 0^-1 = 0^(w+1) = -0     only if -0 = 0
        (0^0)^0    = 1^0    = w     -1÷0  = 0^w ÷ 0^1 = 0^(w-1)  = -w     only if -w = w

    The last two rows are true on the rational shadow, where -0 and 0 have the same projection,
    and false upstairs, where -0 != 0 by E6. This said "it holds at all four" and "one
    disagreement anywhere would end the cycle, and there is none". Two of the four were being
    read off the shadow, and there was nothing upstairs at those points to disagree with.

    Structurally: order the four as the cycle does -- 0, 1, w, -1 -- as the corners of a
    square. Then 1÷x swaps the opposite pair 0 and w and is a diagonal reflection; 0^ swaps
    adjacent pairs and is an edge reflection; and the cycle squared swaps both diagonal pairs
    at once, which is the half-turn. Every operation the closure set has is a symmetry of that
    square, and ^0 is the quarter-turn that generates the group. All of them together are D4,
    of order 8. Naming the other diagonal "-x" and the half-turn "-1÷x" is the shadow-level
    reading above, and it is not carried upstairs.
    The permutations above are exact. The GEOMETRY is not established: 0 and w share a rational
    shadow, so the four are not at compass points of anything yet, and the square is
    combinatorial until something says otherwise.

    What it costs. It rests on the reciprocal law and on the leap, which is why it is filed here
    and not under Proven. 1^0 = w is the value to check against the Q model before this is
    leaned on.

    WHAT IT WAS THOUGHT TO BUY, WITHDRAWN. This was filed as evidence on problem 2: naming the
    square of the cycle "-1÷x" requires -0 and -w to be defined at all, and if that
    identification holds it FORCES -0 = 0 and -w = w, against the negation rule above. The
    forcing is real, but it happens on the shadow -- which is the only level where the
    identification holds at those two points, and where -0 = 0 already, so nothing is at stake
    there. Upstairs the identification simply fails at 0 and w instead of forcing anything. It
    never reached the negation rule, and it is no longer evidence on problem 2.
```

### Maybe

Not 100% sure yet, but the evidence is strong.

```
0^a + 0^b = 0^(a·b)
    The mirror of E10: if subtraction of values is division of exponents, then
    addition of values is multiplication of exponents.
    Also follows from E1 plus the general involution, since an involution carrying
    + to · necessarily carries · to +.
    That second route is now the interesting one, because it is the same move that derives
    E10 under Chosen -- E1 conjugated by the involution. If the involution is adopted, this
    law and E10 come from one place, and holding one while rejecting the other stops being
    available. Worth checking before adopting either: E10 falls out of the conjugation as a
    log identity, and this does not, so they may not be equally cheap.
    Never stated independently, so it is held one rank below the primitives.
    Never used E2, so the deletion leaves it where it was.

    It gives x + 0 = x, since 0^u + 0^1 = 0^(u·1) = 0^u -- but that is no longer news: 0 is
    invariant under addition anyway, and 1 under multiplication. Each identity is invariant
    under its own operation, and -1 and w under neither, which is why 1 + w stands.
    What is left to settle is narrower and is the law itself: it says a sum of two traction
    parts is a single traction part, and the canonical form a + 0^b holds one. Its erasure cell
    has no recognisable value-level form either. Both are reasons it is still Maybe.
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
log_(0^a)(0^b) = 0^(b/a)    PARTLY BACK. Was E2 + E6, and E2' + E6 gives it wherever b/a is
                            rational -- i.e. wherever a and b are parallel as exponents. Off
                            that line it is still undefined, so E8 remains a primitive in
                            earnest rather than a special case of a definable family
(0^u)^v, v with a w-part    no rule, and none available: there is no product on the exponents
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
