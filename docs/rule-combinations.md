Rule Combinations
===

To ensure all possible scenarios have been accounted for.

Every traction value is `0^a` for exactly one a (E6). So operand *shape* is not a variable —
there is only one shape. What varies is the operation, and which degenerate exponent it lands on.

Throughout, `x = 0^u` and `u = 0^x`. Rule labels E1-E10 are from equivalence-classes.md.
E2 is withdrawn; the rows that rested on it are listed under "Gone with E2" below.

### Binary operations

Both operands lifted, result given as a power of 0.

| value op        | exponent op | rule    | status  |
|-----------------|-------------|---------|---------|
| `0^a · 0^b`     | `a + b`     | E1      | Proven  |
| `0^a ÷ 0^b`     | `a - b`     | E1 + E3 | Proven  |
| `0^a + 0^b`     | `a · b`     | —       | Maybe   |
| `0^a - 0^b`     | `a ÷ b`     | E10     | Proven, and total |
| `(0^a)^n`       | `a · n`     | E1      | Proven for integer n only |

The first four are one statement: `0^` exchanges the additive and multiplicative floors,
and the inverses follow. `·` swaps with `+`, `÷` swaps with `-`.

Row 4 is total. `0^a - 0^a = 0^(a÷a)` materialises as `0^1 = 0` rather than discharging, so
subtraction always has an answer. This is the branch the Q model satisfies.

Row 5 is what is left of E2, and it is a theorem of E1 rather than a rule of its own: an
integer power is repeated multiplication, so row 1 applied n times gives it. It is the odd row
out in the same way E2 was — every other row lifts both operands, and this one lifts the left
operand and takes the right one raw, because `a` is an exponent and `n` is a count. But it no
longer reaches a general `v`, so the floor-mixing it used to license is gone with it.

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

| value op    | exponent op | rule | status |
|-------------|-------------|------|--------|
| `1 ÷ 0^a`   | `-a`        | E3   | Proven |
| `-(0^a)`    | unknown     | —    | Open   |

Negation used to be "add w to the exponent", by E1 and `-1 = 0^w`, which is negation read as
multiplication by −1. In this branch that reading is false: E10 with totality forces `-0 = 0`,
while `0·(-1) = 0^(1+w)` forces `-0 = 0^(1+w)`, and those agree only if `w = 0`. See
equivalence-classes.md, Chosen. So negation is an operation with no exponent rule at present —
this is now problem 2, and it is a bigger hole than the conflict it replaced.

### Degenerate cells

A cell is degenerate when the exponent-level operation is an erasure form — `x - x` or `x ÷ x`.

| value op | exponent op | erasure at | kind in the exponent | behaviour |
|----------|-------------|------------|----------------------|-----------|
| `·`      | `a + b`     | `b = -a`   | additive, home       | open — Problem 1 |
| `÷`      | `a - b`     | `a = b`    | additive, home       | open — same question |
| `+`      | `a · b`     | `b = 1÷a`  | multiplicative       | no standard value form |
| `-`      | `a ÷ b`     | `a = b`    | multiplicative, foreign | materialises as 1: `y - y = 0` |

Rows 1, 2 and 4 are the erasure changing kind: a multiplicative erasure at the value level
lands as an additive erasure in the exponent, and the other way round.

Row 4 is settled by the totality of E10 — the foreign erasure materialises. Rows 1 and 2 are
the home case and are still open; that is Problem 1. Row 3 has no recognisable value-level
form, which is one more reason the addition law is only Maybe.

`0·w` is row 1 at `a = 1, b = -1`. That is the whole of Problem 1.

### What these tables expose

```
1) log_base is NOT derivable. It was, through E2, and that was the argument for "log/exp are
   both primitive is one primitive too many". With E2 gone the count is right again: exp is
   primitive, log_0 is primitive, and log to any other base is undefined.

2) There is no floor-mixing rule left. Row 5 reaches integer powers only, and the two points
   where a translation was ever permitted -- v = 0 and v = w, i.e. x^0 and x^w -- now have no
   rule at all rather than an unconstrained one. Those two were filed False on E2's authority;
   they are Open again. See problem 3.

3) Discharge and materialise are no longer both in use without a rule. The foreign erasure
   materialises: that is the totality of E10, chosen. The home erasure is undecided, and that
   is Problem 1 -- but it is now one question rather than half a table.

4) The negation conflict is resolved, at a price. E10 is protected, `-1 = 0^w` is forced by E6
   and E7, so `-y = y·(-1)` is the casualty. What negation does to an exponent is now unknown.

5) Degenerate cells cannot be recognised by value, only by term shape. 1·1 is the value 1;
   1·(1÷1) is an erasure. Same class, different terms. So simplify() has to match on the
   term, and canonicalising early destroys the information it needs.
```
