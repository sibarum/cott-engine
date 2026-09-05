Rule Combinations
===

To ensure all possible scenarios have been accounted for.

Every traction value is `0^a` for exactly one a (E6). So operand *shape* is not a variable —
there is only one shape. What varies is the operation, and which degenerate exponent it lands on.

Throughout, `x = 0^u` and `u = 0^x`. Rule labels E1-E10 are from equivalence-classes.md.

### Binary operations

Both operands lifted, result given as a power of 0.

| value op        | exponent op | rule    | status  |
|-----------------|-------------|---------|---------|
| `0^a · 0^b`     | `a + b`     | E1      | Proven  |
| `0^a ÷ 0^b`     | `a - b`     | E1 + E3 | Proven  |
| `0^a + 0^b`     | `a · b`     | —       | Maybe   |
| `0^a - 0^b`     | `a ÷ b`     | E10     | Proven  |
| `(0^a)^v`       | `a · v`     | E2      | Proven  |
| `log_(0^a)(0^b)`| `b ÷ a`     | E2 + E6 | Proven  |

The first four are one statement: `0^` exchanges the additive and multiplicative floors,
and the inverses follow. `·` swaps with `+`, `÷` swaps with `-`.

Row 5 is the odd one out. Every other row lifts both operands; E2 lifts the left operand
and takes the right one raw — `a` is an exponent, `v` is a value. The swap has nowhere to
send `^`, because it exchanges two floors and `^` sits on a third. That is the structural
reason for the asymmetry, not an oversight.

Row 6 is derived, not primitive: `(0^a)^z = 0^(az) = 0^b` gives `az = b` by E6, so `z = b/a`.
Note this contradicts what-is-traction.md, which calls log/exp primitive. Exp is primitive;
log_base is definable from it. Only `log_0` is special, and only because a=1 makes it E8.

### Unary operations

| value op    | exponent op | rule           | status |
|-------------|-------------|----------------|--------|
| `1 ÷ 0^a`   | `-a`        | E3             | Proven |
| `-(0^a)`    | `a + w`     | E1, `-1 = 0^w` | Chosen |

Negation is "add w to the exponent". It is an involution exactly when `2w = 0`, which the
Maybe addition law supplies: `w + w = 0^-1 + 0^-1 = 0^((-1)(-1)) = 0^1 = 0`.

### Degenerate cells

A cell is degenerate when the exponent-level operation is an erasure form — `x - x` or `x ÷ x`.

| value op | exponent op | erases when | value-level form |
|----------|-------------|-------------|------------------|
| `·`      | `a + b`     | `b = -a`    | `y · (1÷y)`      |
| `÷`      | `a - b`     | `a = b`     | `y ÷ y`          |
| `+`      | `a · b`     | `b = 1÷a`   | no standard form |
| `-`      | `a ÷ b`     | `a = b`     | `y - y`          |

Rows 1, 2 and 4 are the erasure changing kind: a multiplicative erasure at the value level
lands as an additive erasure in the exponent, and the other way round. Row 3 has no
recognisable value-level form, which is one more reason the addition law is only Maybe.

`0·w` is row 1 at `a = 1, b = -1`. That is the whole of Problem 1.

### What these tables expose

```
1) log_base is derivable, so "log/exp are both primitive" is one primitive too many.

2) Row 5 is the only floor-mixing rule, and the only place a translation is permitted
   (equivalence-classes.md, "E2 admits no translation on nonzero rationals").
   Those two facts are the same fact.

3) Discharge and materialise are BOTH in use, in the same table, with no rule saying which:
     - row 1 discharges. That is 0·w, left undefined.
     - row 4 materialises. That is the -0 = 0 proof, where 0^(a/a) is read as 0^1.
   Until a rule decides this, half the table is convention.

4) CONFLICT — negation. Two routes to -0 disagree:
     (i)  0 - 0 = 0^1 - 0^1 = 0^(1÷1) = 0^1 = 0, by E10; and 0 is the additive
          identity, so -0 = 0.
     (ii) -0 = 0·(-1) = 0^1 · 0^w = 0^(1+w), by E1 and -1 = 0^w.
   These agree only if w = 0. So one of these must go:
     - E10,
     - -1 = 0^w (the leap),
     - -y = y·(-1), i.e. negation is not multiplication by -1,
     - or "0 is the additive identity", which is what turns 0-0 into -0.
   The third is the cheapest and is not currently written down anywhere as an assumption.

5) Degenerate cells cannot be recognised by value, only by term shape. 1·1 is the value 1;
   1·(1÷1) is an erasure. Same class, different terms. So simplify() has to match on the
   term, and canonicalising early destroys the information it needs.
```
