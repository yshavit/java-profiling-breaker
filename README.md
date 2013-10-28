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

If `verbose` is enabled, you'll get an output of each iteration that summarizes how much time is spent in setting the iteration up (ie populating the `int[]`, `HardWork.work` and `SpinWork.work`. It also outputs a value that you can ignore; it's just there so that the JIT can't optimize away the loops as doing no work. The output will look something like this:

    ...
    setup: 0.010	work: 0.704	spin: 0.034	value: 953606947
    setup: 0.009	work: 0.610	spin: 0.037	value: -144282825
    setup: 0.008	work: 0.626	spin: 0.036	value: -1096571981
    setup: 0.009	work: 0.672	spin: 0.035	value: -2101465172
    ...

The above is with the arguments I mentioned earlier. Notice that we spend about 20x as much time in `HardWork.work` as we do in `SpinWork.work`. **When I profiled this with VisualVM, it said that 50.1% of the time was spent in `SpinWork.work`, while `HardWork.work` didn't show up in even a single sample.**

The results
===========

If you're using something like VisualVM, you should see `SpinWork.work` appear most in the profiler, while `HardWork.work` barely shows up, or doesn't show up at all.

In fact, `SpinWork.work` takes almost no time: the JIT is able to optimize away all those empty spin loops. It still does have to update the count 1000 times up and 1000 times back down, since `updateCount` is synchronized (escape analysis could one day remove even this, but it doesn't as of late 2013). Those 1000 increments and decrements take very little time; but they're _all_ yield points, because of the synchronization.

Meanwhile, `HardWork.work` takes a lot of time, but doesn't contain any yield points. So, even though that's where most (depending on your args!) of the time is spent, most profilers will competely miss it. The only profiler I know of that does catch it is Solaris Studio Performance Analyzer.

These results seem contrived
============================

They are. I purposely created two extreme methods: one that does almost no work but contains lots of yield points, and another that does a lot of work but contains not a single yield point. Most methods aren't quite so polar in either direcion.

That said, it definitely demonstrates the potential for biases in Java profilers.

Next time you see a method that "should" be cheap appear a lot in a CPU sample &mdash; for instance, a trivial getter that just returns a `volatile` or is `synchronized` &mdash; consider that it could be that it _is_ cheap, but appears in a lot of samples because of bias towards yield points. If you really need accurate results, I'd recommend something like Solaris Studio Performance Analyzer.
