Rule Combinations
===

To ensure all possible scenarios have been accounted for.

Every traction value is `0^a` for exactly one a (E6). So operand *shape* is not a variable —
there is only one shape. What varies is the operation, and which degenerate exponent it lands on.

Throughout, `x = 0^u` and `u = 0^x`. Rule labels E1-E10 are from equivalence-classes.md.
E2 is withdrawn; the rows that rested on it are listed under "Gone with E2" below.

### Binary operations

Both operands lifted, result given as a power of 0.

| value op        | exponent op | rule    | status  | in the engine |
|-----------------|-------------|---------|---------|---------------|
| `0^a · 0^b`     | `a + b`     | E1      | Proven  | wired |
| `0^a ÷ 0^b`     | `a - b`     | E1 + E3 | Proven  | wired, either side |
| `0^a + 0^b`     | `a · b`     | —       | Maybe   | provisional, unwired |
| `0^a - 0^b`     | `a ÷ b`     | E10     | Proven, and total | wired, either side |
| `(0^a)^n`       | `a · n`     | E1      | Proven for integer n only | wired |

The first four are one statement: `0^` exchanges the additive and multiplicative floors,
and the inverses follow. `·` swaps with `+`, `÷` swaps with `-`.

Row 4 is total. `0^a - 0^a = 0^(a÷a)` materialises as `0^1 = 0` rather than discharging, so
subtraction always has an answer. This is the branch the Q model satisfies.

It matches from either side. `-x + y` is `y - x`, and the pattern used to require the negation on the
right, so half of subtraction was invisible to E10: `0 + (-0)` matched and answered 0, the erasure, while
`(-0) + 0` fell through to the identity and answered `-0`. Addition was not commutative on those terms as a
result.

Row 2 matches from either side for the same reason, and it had the same defect until it was fixed:
`(1÷y)·x` is `x÷y`, and the pattern required the reciprocal on the right, so `0^5 ÷ 0^3` answered `0^2`
while `(1÷0^3)·0^5` stood, and `y÷y` discharged to 1 while `(1÷y)·y` stood. Multiplication was not
commutative on those terms as a result.

Row 5 is what is left of E2, and it is a theorem of E1 rather than a rule of its own: an
integer power is repeated multiplication, so row 1 applied n times gives it. It is the odd row
out in the same way E2 was — every other row lifts both operands, and this one lifts the left
operand and takes the right one raw, because `a` is an exponent and `n` is a count. But it no
longer reaches a general `v`, so the floor-mixing it used to license is gone with it.

### The exponent zero, on the closure set

| value op | value | rule | status | in the engine |
|----------|-------|------|--------|---------------|
| `0^0` | `1` | E5 | Proven | wired |
| `1^0` | `w` | the 4-cycle | Chosen | wired |
| `w^0` | `-1` | the 4-cycle | Chosen | wired |
| `(-1)^0` | `0` | the 4-cycle | Chosen | wired |

`x^0` is a 4-cycle `0 -> 1 -> w -> -1 -> 0` on the closure set, forced by the reciprocal law
together with E6 and E7 — see equivalence-classes.md, Chosen. It is the first map on those four
points that is not an involution, and with `0^` it generates D4, with `^0` as the quarter-turn.

Off the closure set there is still nothing: `2^0` stands, and no counting argument reaches it.
The rule is a lookup on four values, so it is a table rather than a derivation — which is a warning, since
a table cannot be checked by reading it. What checks it instead is the cycle closing: four applications of
`^0` return to where they started, and that is one assertion covering all four rows.

### The logarithm

| value op        | value | rule | status | in the engine |
|-----------------|-------|------|--------|---------------|
| `log_0(0^a)`    | `a`   | E8   | Proven | wired |
| `log_0(0)`      | `1`   | E8, E4 | Proven | wired |
| `log_0(1)`      | `0`   | E8, E5 | Proven | wired |
| `log_0(w)`      | `-1`  | E8, w = 1÷0 | Proven | wired |
| `log_0(-1)`     | `w`   | the leap | Chosen | not wired |
| `log_b(x)`, b != 0 | — | went with E2 | no rule | stands |

E8 is the inverse of `0^` and nothing more. It reads the points where the arithmetic rules will not,
and that is not the axis rule being bent: what the axis rule forbids is reading an additive unit as a
power of zero to make an ARITHMETIC rule fire, because the exponent that comes out can be read back and
the pair cycles. Inverting is not arithmetic, and no rule anywhere produces a log for this one to feed --
the parser is the only source of them -- so it cannot be part of a cycle.

Not wired, and it is the rule to watch if logs ever do loop: the Chosen `log_0(x) = 0^x`, which
manufactures a power of zero out of any x.

One thing the parser refuses that E8 arguably permits: `0^log_0(x)` is a sort error, because the `^` slot
is checked for a value and a log returns an exponent. But an exponent is exactly what that slot wants, and
by E8 the answer is `x`. The old engine refused it too, so this is inherited rather than introduced.

### Gone with E2

```
(0^a)^v        for non-integer v -- no rule
log_(0^a)(0^b) = 0^(b÷a)
    Was derived as (0^a)^z = 0^(az) = 0^b, giving az = b by E6, so z = b/a. That derivation
    needed E2 at a general z, and the surviving integer version only settles the cases where
    b/a is already an integer.
    So a logarithm to a general base is undefined again, and E8 (log_0 inverts 0^) is a
    primitive in earnest rather than a special case of a definable family.
    what-is-traction.md has been corrected: exp is primitive, and so is log_0.
```

### Unary operations

| value op    | exponent op | rule           | status |
|-------------|-------------|----------------|--------|
| `1 ÷ 0^a`   | `-a`        | E3             | Proven |
| `-(0^a)`    | `a + w`     | E1, `-1 = 0^w` | Chosen |

Negation is "add w to the exponent" — negation read as multiplication by −1. It gives
`-0 = 0^(1+w)`, which E6 keeps distinct from `0^1`, so `-0` is a magnitude-zero point with the
opposite orientation rather than `0` itself.

It is an involution exactly when `2w = 0`, which the Maybe addition law supplies:
`w + w = 0^-1 + 0^-1 = 0^((-1)(-1)) = 0^1 = 0`. Note the awkwardness there — that same addition
law makes the value `0` an additive identity, which contradicts this row. So the row that needs
the addition law to be an involution is the row the addition law collides with. That is
problem 2, which is resolved: 0 is invariant under addition and 1 under multiplication, each under its
own operation, and nothing was ever pulling against this row. What is left of the addition law is that it
claims a sum of two traction parts is a single one.

### Degenerate cells

A cell is degenerate when the exponent-level operation is an erasure form — `x - x` or `x ÷ x`.

| value op | exponent op | erasure at | kind at the VALUE level | behaviour |
|----------|-------------|------------|-------------------------|-----------|
| `·`      | `a + b`     | `b = -a`   | multiplicative          | discharges to 1 |
| `÷`      | `a - b`     | `a = b`    | multiplicative          | discharges to 1 |
| `+`      | `a · b`     | `b = 1÷a`  | additive                | no standard value form |
| `-`      | `a ÷ b`     | `a = b`    | additive                | discharges to 0 |

The column that matters is the fourth, and getting it wrong is what kept Problem 1 open. An erasure is
decided by the kind of the operation it came **from**, not the kind it arrives as. `0·w` is a product, so
the erasure is multiplicative and discharges to 1 — even though the exponent it lands in reads `1 + -1`.
`y - y` is a difference, so it discharges to 0, even though the exponent reads `a ÷ a`. The lift changes
the kind, and the earlier version of this table sorted the rows by the kind after the lift.

Every row is the erasure changing kind on the way into the exponent, which is why the fourth column and
the second disagree throughout. Row 3 is the only one with no recognisable value-level form, and that is
one more reason the addition law is only Maybe.

`0·w` is row 1 at `a = 1, b = -1`, and it discharges to 1. Problem 1 is closed; see
theory-problems.md #1.

An earlier version of this section argued that what distinguished the settled row from the open ones was a
**host** — that `0^(1 + -1)` is an erasure standing as the whole exponent with nothing to be added to,
which is why universal invariance said nothing about it. That was true and beside the point: invariance
says nothing about it either way, because the erasure was already decided at the value level before it was
lifted.

### What these tables expose

```
1) log_base is NOT derivable. It was, through E2, and that was the argument for "log/exp are
   both primitive is one primitive too many". With E2 gone the count is right again: exp is
   primitive, log_0 is primitive, and log to any other base is undefined.

2) There is no floor-mixing rule left. Row 5 reaches integer powers only, and the two points
   where a translation was ever permitted -- v = 0 and v = w, i.e. x^0 and x^w -- now have no
   rule at all rather than an unconstrained one. Those two were filed False on E2's authority;
   they are Open again. See problem 3.

3) Discharge and materialise are one rule now: an erasure resolves to the identity of the operation it
   came from. 1 for a product or a quotient, 0 for a sum or a difference. That closed Problem 1, and
   the totality of E10 turns out to be the same statement seen from the other side.

4) There is no negation conflict, and two earlier versions of this file said there was. 0 is invariant
   under addition and 1 under multiplication, each under its own operation; -1 and w under neither, which
   is why 1 + w stands. Negation is multiplication by -1 and nothing contradicts it. See
   theory-problems.md, problem 2.

5) Degenerate cells cannot be recognised by value, only by term shape. 1·1 is the value 1;
   1·(1÷1) is an erasure. Same class, different terms. So simplify() has to match on the
   term, and canonicalising early destroys the information it needs.
```
