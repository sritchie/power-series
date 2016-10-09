# power-serious.clj

>”Multiply the arc by the square of the arc, and take the result of repeating that (any number of times). >Divide (each of the above numerators) by the squares of the successive even numbers increased by that >number and multiplied by the square of the radius. Place the arc and the successive results so obtained >one below the other, and subtract each from the one above. These together give the jiva, as collected >together in the verse beginning with 'vidvan' etc." 
- Madhava of Sangamagrama (c. 1350 – c. 1425), founder of the Kerala school of astronomy and mathematics


##History

Computing power series with lazy evaluation was canonized in [an unpublished paper](https://docs.google.com/viewer?url=http%3A%2F%2Fpdos.csail.mit.edu%2F~rsc%2Fkahn77parallel.pdf) on coroutines by Giles Kahn and David MacQueen in 1977, even though it primarily examined the Sieve of Eratosthenes method for finding primes and only mentioned having developed a power series application in the conclusion along with one for Fourier transforms and various sorting algorithms. Regardless, the power series and primes sieve examples quickly spread throughout the computer science world as practical examples in numerical computing of [Peter Landin's concept of streams](http://fi.ort.edu.uy/innovaportal/file/20124/1/22-landin_correspondence-between-algol-60-and-churchs-lambda-notation.pdf) that so heavily influenced Kahn and MacQueen.

They perhaps became most well known through their implementation in Scheme included in [Structure and Interpretation of Computer Programs](https://mitpress.mit.edu/sicp/). By now versions exist in numerous languages, even those without support for laziness such as Go.

In 1989, long before Go, Doug McIlroy wrote a lazy concurrent version on one of its [CSP](https://docs.google.com/viewer?url=http%3A%2F%2Fwww.usingcsp.com%2Fcspbook.pdf) predecessors, Newsqueak, and published a paper: [Squinting at Power Series](https://swtch.com/~rsc/thread/squint.pdf). McIlroy referred to [the subsequent version in Haskell](http://www.cs.dartmouth.edu/~doug/powser.html), which is more complete than my own in only ten lines, as "most beautiful code I've ever written."


##Implementation Details & Chrestomathy Analysis

McIlroy's movement towards Haskell as well as my own experience confirm that laziness is far more important than concurrency in efficient evaluation of infinite series. In fact, concurrent control structures can be a considerable bottleneck on well-implemented code. This is easy to see in Clojure as it has a full [CSP implementation](http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html) as well as built-in support for lazy sequences.

However, Clojure's lazy sequences are *not* streams. It's somewhat of a hybrid language in that it very much uses eager evaluation yet has built-in support for laziness across all its sequences without the need for a separate set of operators. In fact, the majority of its higher order functions return lazy sequences as a matter of efficiency when processing large persistent data structures. In one sense this greatly simplifies writing libraries such as this: the majority of it is composed of one highly abstracted function: map.

On the other hand, a hallmark of stream processing in languages such as Scheme is defining infinite series through recursion and making that work in Clojure, while possible, is far from idiomatic. For example, to generate an infinite series of the number one in Scheme one writes:

```
(define ones (cons-stream 1 ones))
```

whereas in Clojure using the built-in function `repeat`:

```
(def ones (repeat 1))`
```

Still, it seems when it comes to infinite convergent series that recursion is simply the correct choice: *especially* considering we're almost always working with examples like trigonmetric functions and the exponential function that can be defined in terms of one another. Clojure does have rich generative methods capable of defining sine and cosine independently of one another, but the stubborn Schemer in me chose instead to spend a couple days learning the hairy idiosyncrasies of (often mutually) recursive functions that generate lazy sequences on the JVM. 

The one thing I'd like to improve (other than possibly constraining typecasting so as to avoid BigInts, which I don't think is feasible in Clojure without substituting Java arrays, which brings in too much overhead for this volume of data) is adding a composition function using Faà di Bruno's formula. So far McIlroy's is the only version I've personally seen that includes one, but it seems lacking considering I do have support for the compositional inverse.

A current list of functions follows:

+ Addition
+ Subtraction
+ Scaling 
+ Negation
+ Multiplication 
+ Division
+ Compositional Inversion
+ Differentiation
+ Integration


##Benchmarks

Tests were ran with OSX on a 1.7 GHz Intel Core i5 using [Criterium](https://github.com/hugoduncan/criterium) for cosine, the exponential function, and arctangent (all recursively defined) as well as tangent (calculated through division, or inversion of the Cauchy products) and reduction and scalar multiplication of the terms of arctangent to a BigDecimal approximation of π. Results were averaged over sixty trials with standard deviation included.

Infinite series are rather paradoxical benchmarks because although they grow linearly with number the rate is exponential with regards to storage due to the expansion of each term. However, because these particular infinite series converge what would be expected to be exponential turns out to be constant rather quickly: in the case of sine, cosine, and the exponential function only roughly three times the overhead. In that sense, it's quite a different metric for lazy evaluation than the other common example: the [Sieve of Eratosthenes](https://github.com/clojure/core.async/wiki/Sieve-of-Eratosthenes) where computation takes longer the further it progresses. Although somewhat skeptical of benchmarking, I would be interested to see comparisons between languages for both types of lazy numeric computation: particularly with the Haskell versions. 