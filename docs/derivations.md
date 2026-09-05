Derivations
===

Here I will keep all the official traction-approved derivations, both as a source of
documentation, and to illustrate how traction algebra works.

Note: w is supposed to be omega.

### i

The route through logs is withdrawn. It was:

```
sqrt(-1) = (-1)^(1/2)
=0^(log_0( (-1)^(1/2) ))
=0^((1/2)*log_0(-1))
=(0^(1/2))^w
```

Step three is `log_0(y^v) = v·log_0(y)` and step four is `0^(uv) = (0^u)^v`. Both are E2, which
is withdrawn, so this derivation no longer stands.

What survives says less but needs only E1 and `0^w = -1`:

```
0^(w/2) · 0^(w/2) = 0^(w/2 + w/2)     E1
                  = 0^w               exponent arithmetic
                  = -1                Chosen
```

So `0^(w/2)` squares to −1. A square root of −1 is exhibited inside the type and still needs no
adjoining — that much is unchanged. What is gone is the half-power `(-1)^(1/2)` that used to name
it: `x^v` has no rule at non-integer `v`, so the notation `sqrt(-1)` cannot currently be cashed
out into the value that answers it.

Note this is a square root, not "the" square root. Uniqueness of roots is exactly what the
theory does not supply (theory-problems.md, problem 3), and `0^(-w/2)` squares to −1 as well.
