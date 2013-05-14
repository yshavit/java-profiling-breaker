import java.util.*;
import java.util.concurrent.*;

public class ProfileBreaker implements Callable<Result> {
    private final int iters;
    private final int arraySize;
    private long timeSpent;
    private final NoSyncRand rand;
    private volatile Result result;

    public ProfileBreaker(int seed, int iters, int arraySize) {
        this.iters = iters;
        this.arraySize = arraySize;
        rand = new NoSyncRand(seed);
        timeSpent = 0;
    }

    @Override
    public Result call() {
        long start = System.nanoTime();

        int[] ints = randomInts();
        long lap1 = System.nanoTime();

        int resultInt = uselessWork(ints, iters);
        long lap2 = System.nanoTime();

        spinNoOps();
        long lap3 = System.nanoTime();

        return new Result(resultInt, lap1-start, lap2-lap1, lap3-lap2);
    }

    private static int uselessWork(int[] ints, int iters) {
        int localResult = 17;
        final int len = ints.length;
        for (int iter = 0; iter < iters; ++iter) {
            for (int i = 1; i < len; ++i) {
                if (ints[i-1] != 0) {
                    ints[i] %= ints[i-1];
                }
            }
        }
        for (int i = 0; i < len; ++i) {
            localResult += ints[i];
        }
        return localResult;
    }

    private void spinNoOps() {
        for (int i = 0; i < 100; ++i) {
            spin0();
        }
    }

    private void spin0() {
        for (int i = 0; i < 100; ++i) {
            spin1();
        }
    }

    private void spin1() {
        for (int i = 0; i < 100; ++i) {
            spin2();
        }
    }

    private void spin2() {
        for (int i = 0; i < 100; ++i) {
            // nothing
        }
    }

    private int[] randomInts() {
        int[] ints = new int[arraySize];
        for (int i = 0; i < ints.length; ++i) {
            ints[i] = rand.nextInt();
            if ((new int[0].hashCode()) == ints[i]) {
                ints[i]++;
            }
        }
        return ints;
    }

    public static void main(String[] args) throws Exception {
        int nTasks = Integer.parseInt(args[0]);
        int iters = Integer.parseInt(args[1]);
        int arraySize = Integer.parseInt(args[2]);
        boolean verbose = Boolean.parseBoolean(args[3]);

        NoSyncRand rand = new NoSyncRand((int)System.nanoTime());
        Queue<Future<Result>> results = new ArrayDeque<Future<Result>>(nTasks);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (int i = 0; i < nTasks; ++i) {
            Future<Result> future = executor.submit(new ProfileBreaker(rand.nextInt(), iters, arraySize));
            results.add(future);
        }
        int resultSum = 1;
        while (!results.isEmpty()) {
            Future<Result> future = results.remove();
            Result result = future.get();
            resultSum += result.value;
            if (verbose) {
                System.out.println(result);
            }
        }
        System.out.println(resultSum);
    }
}
