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
E2   (0^u)^v = 0^(uv)
E3   0^(-a) = 1/0^a               NOT derived from E1: that route needs a - a = 0, an erasure
E4   0^1 = 0
E5   0^0 = 1
E6   0^ is injective              this is reversibility
E7   0^ is closed on {0,1,-1,w}   this is what "the type is a closure" means
E8   log_0 inverts 0^
E9   w := 1/0
E10  0^a - 0^b = 0^(a/b)          exponent division is value subtraction
```

Throughout, `x = 0^u`. Every traction value has such a u, and E6 makes it unique.

### Proven

Locked in and safe. Follows from the primitives alone.

```
w = 0^-1 = 1/0
    E3 at a=1: 0^-1 = 1/0^1 = 1/0 = w, by E4 and E9.

0 = 0^1 = 1/w
    E4; and 1/w = 1/(1/0) = 0.

1 = 0^0 = 1^1
    E5; and 1^1 = (0^0)^1 = 0^(0·1) = 0^0 = 1, by E2.

-1 = (-1)^1
    Trivial.

0^x = w^(-x)
    w^(-x) = (0^-1)^(-x) = 0^((-1)(-x)) = 0^x, by E2 and w = 0^-1.

w^x = 0^(-x)
    Same move.

-0 != w
    0 - 0 = 0^1 - 0^1 = 0^(1/1) = 0^1 = 0, by E10.
    -0 = w would require 1/1 = -1.
    This rules out w. It does NOT establish -0 = 0: that step also needs "0 is the
    additive identity", and the result then CONFLICTS with -0 = 0·(-1) = 0^(1+w).
    See rule-combinations.md, finding 4. The value of -0 is currently open.

0·x = 0^(1+u)
    0^1 · 0^u = 0^(1+u), by E1 and E4.
    Multiplication by zero shifts the exponent by one. Total, and reversible: u comes back.

0·0 = 0^2, so 0² != 0
    The above at u=1; and 0^2 != 0^1 by E6.

0·w = 0^(1 + -1)
    The above at u=-1. The ONLY product with zero whose exponent is an erasure.
    This is Problem 1 in one line. See theory-problems.md.

0^(1/n) is nilpotent
    (0^(1/n))^n = 0^(n · 1/n) = 0^1 = 0, by E2 and E4.
    But 0^(1/n) != 0 = 0^1 by E6, since 1/n != 1.
    So traction has nilpotents of every order. Note this puts it outside the
    commutative rings that wheels and meadows are built on.

1^w = 0^(0w)
    1^w = (0^0)^w = 0^(0·w), by E2.
    So 1^w and 0^(0w) are the same term, not two problems.

E2 admits no translation on nonzero rationals
    (0^u)^n = 0^u · ... · 0^u = 0^(nu) by E1, so the exponent product cannot be
    rewritten as 0^(u·T(n)) for any T other than the identity. Same for 1/n, via
    (0^(u/n))^n = 0^u, and for negatives via E3.
    v = 0 and v = w are NOT reachable this way: the route to v=0 runs through
    (0^u)^(1 + -1) = 0^(u - u), an erasure. So a translation is permitted at exactly
    those two points and nowhere else - which is exactly where the False items below break.
```

### Chosen

Choices will be kept until disproven or all options exhausted.
Everything derived from them is listed here too, not under Proven.

```
0^w = -1
    Forced by E6 and E7 plus the other three values: 0^ already 2-cycles {0,1},
    so it must permute {-1,w}, and 0^-1 = w leaves only 0^w = -1.
    Not independent. This is the leap.

log_0(x) = 0^x  and  0^(0^x) = x
    Forced on {0,1,-1,w} by the above: once 0^w = -1, the map is an involution.
    Extending it to all x is a SEPARATE and much larger assumption. Everything
    below uses the general form.

(-1)^x = 0^(wx)
    (-1)^x = 0^(x · log_0(-1)), and log_0(-1) = w is 0^w = -1. Inherits the leap.

(-1)^0 = 0^(0w) = 1^w
    Same, via (0^w)^0 = 0^(w·0). One term with three spellings.

1^x = 1 + u
    1^x = (0^0)^x = 0^(0·x) = 0^(0^(1+u)) = 1 + u, by E2, the Proven 0·x rule,
    and the general involution.
    Gives 1^1 = 1, 1^0 = 2, 1^(-1) = 1+w, and 1^w = 1 + -1, the erasure.
    Note this is a definition of 1^x, which the theory otherwise lacked.

0x = 0^(1^x)
    Proven 0·x gives 0x = 0^(1+u), and 1^x = 1+u above. So the two agree.
    Was listed as unproven; it follows once 1^x is defined.

wx = 0^((-1)^x)
    w·x = 0^(-1) · 0^u = 0^(u-1) by E1.
    (-1)^x = 0^(x·w) = 0^(0^(u-1)) = u-1 by the general involution.
    So 0^((-1)^x) = 0^(u-1) = wx.
    Was listed as unproven; same story.
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
```

### False

Can't work. Both fail the same way: each demands that multiplication by 0 (resp. by w)
be blind to the difference between u and 0^u. It cannot be - 0· shifts the exponent by
one and 0^ is an involution, and those do not agree.

```
x^0 = 1^x
    x^0 = (0^u)^0 = 0^(0·u) and 1^x = (0^0)^x = 0^(0·x), so by E6 it demands 0·u = 0·0^u.
    At u=0 that is 0·0 = 0·1 = 0, i.e. 0² = 0. But 0² = 0^2 (Proven).
    Independently: 1^0 = 2 (Chosen), not 1.

x^w = (-1)^x
    Same shape: (0^u)^w = 0^(uw) against 0^(wx) = 0^(w·0^u), so it demands u = 0^u.
    False at u = 1/2: 0^(1/2) is nilpotent, and (1/2)² = 1/4.
```

### Notation still open

```
0wx
    Under the product reading 0·w·x it is undefined, because 0·w is (Proven, above).
    Under the reading (0·w)^x it is undefined for the same reason.
    Either way it inherits Problem 1 and cannot be used until that is settled.
```
