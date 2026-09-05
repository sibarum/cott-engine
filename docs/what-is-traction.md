Traction Theory
===

The name traction comes from the phrase "maximally tractable",
because traction was intended to be an algebra that "never runs out of options".
It's also discrete/rational, and "traction" sounds like "fraction" and "rational".
And, in the eyes of the author, it is a superior system to Wheel Theory, and
"wheels are useless if you have no traction".

Wordplay aside, Traction Theory is an implementation of Constructive Operational Type Theory (COTT).
A type is defined as the closure over total, reversible operations.
This is an inversion of classic type theory,
where types are defined by describing the properties of its members.
Instead, COTT identifies the most foundational properties of algebra as totality and reversibility,
and flatly refuses to accept a deficiency in either.
In practice, it is not yet completely total and completely reversible in all situations,
but as the theory develops it has grown more and more complete.
Traction is intended to be the simplest practical implementation of COTT over discrete arithmetic.

### Zero's Reciprocal, Omega

The traction type contains values, and not all values are numbers.
Traction contains the rational numbers, but it also contains a reciprocal for zero, omega.
Normally we think of zero's reciprocal as being infinity, because that's the analytical `lim x->Infinity of 1/x`.
But it's more nuanced than that.
Zero is the bottom of the positive numbers, and omega is the top of the negative numbers.
So omega is infinity-like, but it also has a zero magnitude like zero does.
Omega has an opposite orientation to zero, but they both have the same rational shadow,
occupying the same coordinate on the "real number line".

### The Log/Exp Involution

Traction Theory treats exponentiation as a primitive operation of the type, as primitive as addition
and multiplication, and it extends to any base, including zero and omega.

Log is not a second primitive. `log_(0^a)(0^b) = b/a` falls straight out of `(0^u)^v = 0^(uv)` plus
injectivity, so a logarithm to any base is definable rather than assumed. Only `log_0` is
distinguished, and only because `a = 1` makes it the inverse of the base-0 exponential — which, on
the closure set, is that same exponential. That is the involution. See rule-combinations.md.

### Information Conservation

Traction Theory is an information-conservative algebra and term rewriting system.
Given any binary operation, if the operation, the result of an operation, and one of the operands is known,
then the other operand is fully recoverable with no additional information.
