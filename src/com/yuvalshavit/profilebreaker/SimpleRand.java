package com.yuvalshavit.profilebreaker;

/**
 * Medium-quality randoms. This class provides randoms which are
 * just good enough to make sure the JIT can't optimize them out.
 * java.util.Random has synchronization internally, so it isn't
 * great for multithreaded performance tests, since the
 * synchronizations can interfere with the results.
 *
 * Taken from Java Concurrency in Practice,
 * section 12.1.3 (listing 12.4)
 * http://jcip.net
 */
public class SimpleRand {
    private int v;
    
    public SimpleRand(int seed) {
        v = seed;
        nextInt();
    }

    public int nextInt() {
        return (v = nextInt(v));
    }

    public static int nextInt(int v) {
        v ^= v << 6;
        v ^= v >>> 21;
        v ^= (v << 7);
        return v;
    }
}
