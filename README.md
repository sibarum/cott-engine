# cott-engine

COTT — Constructive Operational Type Theory — as a direct evaluator. Pure Java: no subprocess, no
native code, no reflection.

Types are defined by *total reversible operations*, not by properties of numbers. An identity belongs
to an **operation**, not a value: `x−x` is the additive identity, `x/x` the multiplicative one. Each
one erases in its own context and materialises as a residue in the foreign one, which is how 0 and 1
are generated rather than postulated.

## The residue table

This is the specification the evaluator answers to.

| x | x·(x/x) | x+(x/x) | x·(x−x) | x+(x−x) |
|---|---------|---------|---------|---------|
| 0 | erases  | 1       | 0       | erases  |
| 1 | erases  | `1^1`   | `0^1`   | erases  |
| ω | erases  | `1^ω`   | `0^ω`   | erases  |
| n | `1^n`   | `1^n`   | `0^n`   | `0^n`   |

The closure set {0, 1, ω} erases in the home context of its family, leaves a plain residue at zero
only, and winds everywhere else. `1^a` and `0^a` are the two residue *families*: `x/x` is not a
single identity, and collapsing every `a/a` to one object would destroy the `a` and break
reversibility. An erasure is the operand **disappearing**, not a value standing for nothing.

## Everything is a point with a multiplicity

`pt(k, xp(g, t, r))` is k copies of 0^(g + tω + r), so a numeral needs no separate sort:

```
1 = 0^0      0 = 0^1      ω = 0^-1      -1 = 0^ω      i = 0^(ω/2)
2 = 2 copies of 0^0                     2ω = 2 copies of 0^-1
```

The exponent carries three rationals. The **twist** closes at two and holds ω; the **torsion**
closes at one and holds the roots of the residue zero — it needs a slot of its own because rationals
reduce, and `0/2` is a square root of one that is not one, which plain `0` cannot express. Both
closures are constructor invariants rather than rules that have to fire.

One subtlety runs through everything: ω read as a **point** is the grade −1, while ω read as an
**exponent** *is* the twist unit. `Cott.asPoint` and `Cott.asExponent` are the two readings.

## What it will not answer

Nothing here is total. A sum of unlike exponents, a product of atoms, `0^x`, `(−1)^ω` and `2^-1` all
come back unchanged — where the theory has no definite answer, the term stands. π and e are atoms:
neither has a base-0 exponential form and neither is derivable, so they never reduce. `i` is not
adjoined, it is derived, and it falls out of the exponent arithmetic.

## The real functions

Trigonometry has no base-0 exponential form — the same reason π and e are atoms — so it is not theory here, it
is a catalogue: `Real`, one enum, read by the parser, the printer, the adjacency pass and the keypad alike.

```java
Cott.evaluate("sin(π÷2)")     // "1"
Cott.evaluate("sin(2)")       // "0.909297426826"
Cott.evaluate("sin(x)")       // "sin(x)"   — no number to work on, so it stands, and stays plottable
Cott.evaluate("asin(2)")      // "asin(2)"  — not a finite real, so it stands too
Cott.evaluate("sin(deg(90))") // "1"
```

sin cos tan, their inverses, sec csc cot and theirs, the six hyperbolics, `atan2(x, y)` — the angle of the
point, in the order written — and `deg`/`rad`, which **construct** an angle rather than switching a mode: an
expression carrying `deg(90)` cannot be ambiguous about which measure it was written in, and one written under
a mode always is. `acot` takes the continuous branch, (0, π), rather than the one `atan(1/x)` tears at zero.

This is the one place the engine approximates. Answers are rounded to **fifteen decimal places** and shown to
**twelve** — rounded at all so that `sin(π)` is zero rather than the 1.22e-16 the floating point computes, and
shown three places narrower because everything downstream of a call is exact arithmetic on an approximation and
the error grows with the expression. Those three are guard digits, and they are why `sin(θ)²+cos(θ)²` is 1 and
not 1.000000000001.

A multiplicity now prints as a decimal wherever that spelling is the shorter one — the same exact rational
either way, but `0.909297426826` rather than `454648713413÷500000000000`. Nothing that read well as a fraction
changed: `5÷2` and `1÷2` are no longer than `2.5` and `0.5`, ties go to the fraction, and a rounding that would
turn a small number into zero is never used.

`log` is untouched and is still not the logarithm: it is the base-0 exponent reading, it returns an **exponent**,
and an exponent is not an operand — so `2+log(8, 2)` is a sort error, and the message now says which of those two
surprises is happening rather than assuming you knew the first.

## Names a session gives

`Bindings` is what `k = 3` and `f(x) = x^2+1` mean. It is expansion and not an environment: the definitions go
into the term before `Cott` sees it, so the evaluator stays a function of its argument alone.

```java
Bindings s = Bindings.EMPTY.define("k = 3").define("f(t) = k·t");
s.expand(Parser.parse(Notation.normalize("f(4)", s), s))   // 12
```

A name is only a name once the vocabulary knows it: `xy` is `x·y` and has to stay so, so `Notation` and `Parser`
both scan words out of `Real`'s names plus the session's, longest first, and everything else is still a single
character. A body keeps the names it was written with and looks them up when it is used, so correcting `k`
corrects everything that mentions it. A ring of definitions is refused rather than run until the stack ends.

## Use

```java
Cott.evaluate("x+0÷0")     // "1+x"    — the residue materialises
Cott.evaluate("x·(0÷0)")   // "x"      — the operand erases
Cott.evaluate("2÷0")       // "2ω"
Cott.evaluate("w^w")       // "-1"
```

`Cott.reduce(Term)` works on terms directly and returns a normal form. `Parser`, `Render` and
`Notation` are separable: `Notation` owns the character sets that decide where juxtaposition
multiplies, and both the printer and the parser derive from it so that printing and parsing are
inverse rather than merely similar.

## Run

```bash
mvn test
```

## History

This replaces an equational specification (`cott-one.maude`) driven through a bundled Maude
interpreter over a stdin/stdout REPL. The theory is unchanged and the test suite is ported case for
case; two things are deliberately different, neither semantic:

- **Points are always `pt` forms.** The named constants existed only so that equational matching
  could see them literally. Direct evaluation matches on the value, so the naming became a rendering
  concern and the whole `lift`/`drop` round trip disappeared.
- **Rule order is explicit.** "The finer reading wins" — `1/1` is `1^1` and not 1, `0·ω` is `0/0` and
  not 1 — used to be smuggled into the exponential law as a negative guard. Here the residue is
  simply checked first and the law only sees the pairs the residue did not claim.

## Known gaps

- The residue families are spelled `1^a` and `0^a`, which is also how a literal power of 1 or 0 is
  written, and the two are different objects. A displayed residue therefore does not read back as
  itself. Recorded in `DisplayTest.residueSpellingCollidesWithAPower`; closing it needs a spelling
  the parser can tell apart.
- `ex(0) = 0` — the point 0 read as an exponent is 0, which is what makes `0^0 = 1` — is a standing
  axiom about the single point 0, not an instance of any pattern. `2·0` and `0²` have no exponent
  reading at all. The equational version hid this behind an irreducible constructor, where the named
  constant and its `pt` form could disagree about the same point.
- `2^-1` does not reduce. A multiplicity takes only a **natural** power, so a negative whole exponent
  on a multiplicity is left standing even though `1/2` is perfectly representable.
- `pt(-1, xp(0,0,0))` and `pt(1, xp(0,1,0))` are both −1 and behave alike under multiplication, but
  are distinct terms. Subtraction produces the first; `0^ω` produces the second.
