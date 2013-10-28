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

- The first arg (10000000) basically controls how long the application will run.
- The second controls how much time is spent in `HardWork.work` &mdash; you want this to be low enough that the application is responsive (remember, there are no yield points within that method, so while it's there the application will seem "stuck" in a profiler) yet high enough that time is actually spent there.
- The last number is the size of the `int[]`s on which we operate; if this is too large, the profile is mostly the time spent in populating these arrays, which isn't the intended focus of the app.

The results
===========

If you're using something like VisualVM, you should see `SpinWork.work` appear most in the profiler, while `HardWork.work` barely shows up, or doesn't show up at all.

In fact, `SpinWork.work` takes almost no time: the JIT is able to optimize away all those empty spin loops. It still does have to update the count 1000 times up and 1000 times back down, since `updateCount` is synchronized (escape analysis could one day remove even this, but it doesn't as of late 2013). Those 1000 increments and decrements take very little time; but they're _all_ yield points, because of the synchronization.

Meanwhile, `HardWork.work` takes a lot of time, but doesn't contain any yield points. So, even though that's where a lot (possibly even most, depending on your args!) of the time is spent, most profilers will competely miss it. The only profiler I know of that does catch it is Solaris Studio Performance Analyzer.
