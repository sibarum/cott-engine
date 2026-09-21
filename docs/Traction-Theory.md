Traction Theory
===

The rational, total, and reversible algebra.

# What is ω?

In attempting to totalize algebra,
the first step I took was the step many before me have taken:
to grant zero a reciprocal:

```
1/0=ω
```

This was my first mistake: I obscured the truth.
So let's go back to the basics for a moment.
What does it mean for zero to have a reciprocal?
Is ω infinity, or something different?
The answer is yes, but the perspective means everything, and that's the real lesson.

What's the smallest possible integer? Well, there isn't one.
But there is a smallest number of apples you can put in a basket: zero.
You can't have negative apples in the basket.
But you could have 3 apples, with one owed to a friend, so you'll have 2 leftover.
You'd describe this mathematically as `3-1=2`.
The negative sign never changed the magnitude of the value, it only changed the direction.

By contrast, taking the reciprocal of some number can't change the sign,
it can only change the magnitude (except for i, we'll circle back to this).
So `1/n > -n, n>0` means only the direction can change, not the magnitude.

Then there was zero.
Meadows would have you believe zero's reciprocal is just: zero.
And that's not wrong, it's just incomplete.

Zero definitely has no magnitude - that's its defining property.
But why can't it have a direction?
Whenever I count, I begin with zero, and I always count in positive integers, not negative ones.
So clearly I have a preference for the positive integers over the negative ones.
Therefore, zero does have a direction, and it's positive.

Then you might be tempted to suggest: perhaps `ω=-0`, and since `-0=0`, meadows got it right, end of story.
But this isn't where the story ends.
In fact, it's only beginning.

# Reversibility

What does it mean to have a zero that points in the negative direction?
Still, `x-0=x` and `x*0=0`, right?
Well, yeah, of course.
But totality wasn't enough for me.
Wheel theory and meadows are total, but they're not reversible.
I wanted totality *and* reversibility.

So why these two properties? Just to challenge myself? No.
It's actually a very simple idea.
I asked myself: What makes algebra so useful?
And my answer was: Given any equation, I can perform the same operation on both sides without breaking equality.
And if every operation has an inverse, then the entire space of equal expressions is fully tractable.
So the perfect algebra would be both total and reversible.
I was thinking on the level of 8th-grade mathematics,
which is roughly the level where public education teaches us Americans that "infinity isn't a number, it's an idea".
As if calling it a "number" was some kind of crime.
It's not, and I've since discovered that mathematicians don't practice what they preach, but I digress.

Defining reversibility becomes a bit tricky.
There are two classic ways to define it: one is a bijection, the other is a reversible term rewrite.
At this point, I didn't care which one it was.
I *felt* what it meant to be reversible, and adopting a definition I didn't understand what a recipe for disaster.
So on this discussion, I punt.
I mean "reversible" colloquially, and that's all you need to know.
Yes, I don't know what I'm talking about. Sue me.
I'm sick of people sitting in ivory towers, occasionally yelling "well, technically..."
as if any of us cared. But I digress again.

```DIGRESSION COUNTER: 2```

Ok, fine, I'll say one more thing:
Information conservation is the most consistent observation ever made about our observable universe.
Every particle, every quanta of energy, from thermodynamic laws to entropy, all information is conserved, always.
Our knowledge may have gaps, but the universe always remembers.
So why, then, do we attempt to describe the universe with a framework that intentionally erases information?
My conclusion: we gave up too early in trying to define zero's reciprocal.
Ask any mathematician why it can't exist, and they'll give you a perfectly good explanation.
Go ahead - I won't put words in their mouth.
I also don't care what their answer is, because having an answer doesn't automatically mean the answer is correct.
And given the choice between something that makes sense versus something that doesn't, which one do you think
I'm going to choose?

# Tangent(theta) = p/q

I never liked the classic notions of infinity. Tangent is a good example of this.
Nowadays programmers avoid tangent because it has singularities - vertical asymptotes that cause IEEE floating point 
to cough up an "Infinity", "-Infinity", or if you're really unlucky, a "NaN".
These are "absorbing elements" hijacking the arithmetic and corrupting it, literally creating dead spots in the logic,
places where the world stops working.
We've all encountered these kinds of bugs before.

So, getting infinity right has major consequences.

I asked at the beginning what the smallest possible integer was.
Now I'll ask: What's the largest possible integer?
And the answer is the same, but from a different perspective.
Before, 