Traction Theory
===

A novel algebraic structure (Traction Theory) that totalizes the reciprocal of zero
via a two-component carrier and structural involutions, distinct from wheels and meadows.

```
              ^ 
              | 
             0^2               - 0^2
              |  
             0^1=0             - 0^1
            /   \
<-1__-1=0^ω<  ∅  >0^0=1___2_>  - 0^0
            \   /  
             0^-1=ω            - ω^1
              |
             0^-2=ω^2          - ω^2
              |
              v
  '     '     '     '     '
 -2    -1     0     1     2
```

## Four Fundamental Units

{1, 0, -1, ω}

`ω = 1/0`

## Carrier

A traction is two projective rationals, of the form:

`(n, t) = n·0^t`

Which has a "real" part n and a traction part 0^t.

```
 1 = ( 1, 0) =      1·∅  =  1
 0 = ( 0, 1) =  ∅·(0^1)  =  0^1  =  0
 ω = ( 0,-1) =  ∅·(0^-1) =  0^-1 =  ω
-1 = (-1, 0) =     -1·∅  = -1
```

The coordinate (0,0) is not a member of the type, but can be used as a transient value denoting erasure.
It should be immediately translated to (1,0) or (0,1) as per the rules of discharge.

## Axioms

E(x) = 0^x

```
E1    E(a + b) = E(a) · E(b)         Product Rule: E is a homomorphism (T,+) → (T,·)
E2    E(a ÷ b) = E(a) − E(b)         Quotient Rule: and carries ÷ to −
E3    E(−a) = 1 / E(a)
E4    E(1) = 0
E5    E(0) = 1
E6    E is involutive
E7    E maps {0, 1, -1, ω} onto itself
E8    log_0 = E⁻¹
E9    ω := 1/0
```

## Operations

The traction type is defined as the closure over these total, reversible operations:


### Operational Identity

The operational identity is ∅, erasure, which is not a member of the type.
Terms may be invented or erased by the operational identity.
Erasure occurs only when the operation matches the discharge.
If the operation doesn't match, a residue is left behind.
Additive erasure leaves a residue of zero.
Multiplicative erasure leaves a residue of one.

`x + (z-z) = x + ∅ = x`

`x · (z-z) = x · 0 = 0x`

`x + (z/z) = x + 1 = x+1`

`x · (z/z) = x · ∅ = x`

These four rules are summarized as:

`z-z=∅, additively`
`z/z=∅, multiplicatively`

### Unit Exponentiation

Exponentiation and logarithm are bijective over the units.

```
1  = 0^0  = 1^1   = (-1)^(-1) = ω^ω
0  = 0^1  = 1^ω   = (-1)^0    = ω^(-1)
ω  = 0^-1 = 1^0   = (-1)^ω    = ω^1
-1 = 0^ω  = 1^-1  = (-1)^1    = ω^0
```

### Unit Logarithm

```
 1 = log_0  0 = log_1  1  = log_-1 -1   = log_ω  ω
 0 = log_0  1 = log_1  ω  = log_-1  0   = log_ω -1
 ω = log_0 -1 = log_1  0  = log_-1  ω   = log_ω  1
-1 = log_0  ω = log_1 -1  = log_-1  1   = log_ω  0
```

### The Involution

Powers of zero are generally involutive:

`0^n=log_0(n)`, `0^(0^n) = n`

The carrier can't distinguish between these pairs.

### Addition

Traction addition:

`a·0^b + c·0^d = (a+c) · a^d · c^b · 0^(bd)`

Carrier addition:

`(a, b) + (c, d) = ((a+c)·a^d·c^b, bd)`

Note: `a`, `c`, `a^d` and/or `c^b` should roll into coordinate `t` if any evaluate to some power of {0,ω}.

Derivation:

```
a·0^b = 0^0^a·0^b                                       E6 Involution 0^0^x=x
        0^(0^a + b)                                     E1 Product Rule
        
a·0^b + c·0^d = 0^(0^a + b) + 0^(0^c + d)
                log_0(0^a + b) + log_0(0^c + d)                 E6 Involution 0^x=log_0(x)
                log_0( (0^a + b) · (0^c + d) )                  E1
                log_0( 0^a·0^c + d·0^a + b·0^c + bd )           Distributivity
                0^( 0^a·0^c + d·0^a + b·0^c + bd )              E6
                0^(0^(a+c)) · 0^(d·0^a) · 0^(b·0^c) · 0^(bd)    E1
                (a + c) · 0^(d·0^a) · 0^(b·0^c) · 0^(bd)        E6
                (a + c) · 0^(0^a)^d · 0^(0^c)^b · 0^(bd)        Power Rule
                (a + c) · a^d · c^b · 0^(bd)                    E6
```

### Multiplication

Traction multiplication:

`a·0^b · c·0^d = (a·c)·0^(b+d)`

Carrier multiplication:

`(a, b) · (c, d) = (a·c, b+d)`

Note: `a` and/or `c` should roll into coordinate `t` if either evaluate to some power of {0,ω}.

Derivation:

```
a·0^b · c·0^d
  = (a·c) · (0^b · 0^d)        commutativity and associativity
  = (a·c) · 0^(b+d)            E1
```

### Negation

Traction negation:

```
-(a·0^b) = a·0^(b+ω)
```

Carrier negation:

```
-(a, b) = (a, b-1)
```

Derivation:

```
-(a·0^b) = (-1)·(a·0^b) = a · (0^w · 0^b) = a·0^(b+w)
                              the leap, then E1 + commutativity
```

### Reciprocation

Traction reciprocation:

```
1/(a·0^b) = (1/a)·0^(-1·b)
```

Carrier reciprocation:

```
1/(a, b) = (1/a, -b)
```

Derivation:

```
1/(a·0^b)  =  1/a · 1/(0^b)  =  (1/a)·0^(-b)
                                        E3
```

## Prior Art

Wheels and meadows are two well-known theories that attempt to totalize division.
They do so by amputating the emergent structure.
Wheels collapse upon the arrival of a bottom element, meadows calls "0/0=0" and ends the party early.
Traction strives not only for totality, but information-conservation (reversibility) also.
