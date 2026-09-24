# cott-engine

An evaluator for traction, the number model formalized and proven in
[cott-lean](https://github.com/sibarum/cott-lean).

cott-lean is the specification. This engine implements only what it proves: every operation here
cites the Lean theorem that justifies it, and its tests state the same facts. What the Lean has not
proven, the engine does not do.

The engine's history before this fresh start, under an earlier and different theory, is kept at the
tag `archive/cott-pre-traction`. Nothing here is carried over from it.
