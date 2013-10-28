java-profiling-breaker
======================

A simple program that demonstrates how most of the standard Java CPU samplers are broken. This creates a `Callable` whose run method invokes two methods: a pretty quick one that initializes an array of ints, and a slower one that operates on this ints. On my machine, the slow method takes about 10x as long as the quick one -- and yet doesn't show up at all using JVisualVM.

For information as to why this happens: http://www-plan.cs.colorado.edu/klipto/mytkowicz-pldi10.pdf

compiling and running
=====================

    javac *.java
    java Main <number of tasks> <iterations per task> <size of int arrays> <verbose (true|false)>

I get good results with something like:

    java Main 10000000 1000 100 false

The first arg (10000000) basically controls how long the application will run. The second controls how much time is spent in `HardWork.work` -- you want this to be low enough that the application is responsive (remember, there are no yield points within that method, so while it's there the application will seem "stuck" in a profiler) yet high enough that time is actually spent there. The last number is the size of the `int[]`s on which we operate; if this is too large, the profile is largley the time spent in populating these arrays.

How do we get `HardWork.work` to show up?
=======================================

JVisualVM won't find it, because `HardWork.work` doesn't contain any yield points. The only profiler I know of that does catch it is Solaris Studio Performance Analyzer.
