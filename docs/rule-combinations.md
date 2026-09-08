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
| `0^a ÷ 0^b`     | `a - b`     | E1 + E3 | Proven  | wired |
| `0^a + 0^b`     | `a · b`     | —       | Maybe   | provisional, unwired |
| `0^a - 0^b`     | `a ÷ b`     | E10     | Proven, and total | wired |
| `(0^a)^n`       | `a · n`     | E1      | Proven for integer n only | wired |

The first four are one statement: `0^` exchanges the additive and multiplicative floors,
and the inverses follow. `·` swaps with `+`, `÷` swaps with `-`.

Row 4 is total. `0^a - 0^a = 0^(a÷a)` materialises as `0^1 = 0` rather than discharging, so
subtraction always has an answer. This is the branch the Q model satisfies.

Row 5 is what is left of E2, and it is a theorem of E1 rather than a rule of its own: an
integer power is repeated multiplication, so row 1 applied n times gives it. It is the odd row
out in the same way E2 was — every other row lifts both operands, and this one lifts the left
operand and takes the right one raw, because `a` is an exponent and `n` is a count. But it no
longer reaches a general `v`, so the floor-mixing it used to license is gone with it.

### The exponent zero, on the closure set

| value op | value | rule | status | in the engine |
|----------|-------|------|--------|---------------|
| `0^0` | `1` | E5 | Proven | wired |
| `1^0` | `w` | the 4-cycle | Chosen | not wired |
| `w^0` | `-1` | the 4-cycle | Chosen | not wired |
| `(-1)^0` | `0` | the 4-cycle | Chosen | not wired |

`x^0` is a 4-cycle `0 -> 1 -> w -> -1 -> 0` on the closure set, forced by the reciprocal law
together with E6 and E7 — see equivalence-classes.md, Chosen. It is the first map on those four
points that is not an involution, and with `0^` it generates D4, with `^0` as the quarter-turn.

Off the closure set there is still nothing: `2^0` stands, and no counting argument reaches it.
The rule is a lookup on four values, so wiring it is a table rather than a derivation — which is
also a warning, since a table is exactly the kind of rule that cannot be checked by reading it.

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
problem 2, and it is a conflict between two items, not a hole.

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

4) The negation conflict is NOT resolved by E10, and an earlier version of this file said it
   was. E10 gives what `0 - 0` is worth; it does not make the value `0` an additive identity,
   and universal invariance does not either. The premise that does is the Maybe addition law,
   via `x + 0 = 0^(u·1) = x`. So the conflict is that law against `-(0^a) = 0^(a+w)`, which is
   Maybe against Chosen. See theory-problems.md, problem 2.

5) Degenerate cells cannot be recognised by value, only by term shape. 1·1 is the value 1;
   1·(1÷1) is an erasure. Same class, different terms. So simplify() has to match on the
   term, and canonicalising early destroys the information it needs.
```
