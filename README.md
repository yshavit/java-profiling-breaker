java-profiling-breaker
======================

A simple program that demonstrates how most of the standard Java CPU samplers are broken. This creates a `Callable` whose run method invokes two methods: a pretty quick one that initializes an array of ints, and a slower one that operates on this ints. On my machine, the slow method takes about 10x as long as the quick one -- and yet doesn't show up at all using JVisualVM.

For information as to why this happens: http://www-plan.cs.colorado.edu/klipto/mytkowicz-pldi10.pdf

compiling and running
=====================

    javac *.java
    java ProfileBreaker <number of tasks> <iterations per task> <size of int arrays> <verbose (true|false)>

How do we get `uselessWork` to show up?
=======================================

JVisualVM won't find it, because `uselessWork` doesn't contain any yield points. The only profiler I know of that does catch it is Solaris Studio Performance Analyzer.
