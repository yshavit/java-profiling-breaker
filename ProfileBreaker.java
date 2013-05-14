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
        long lap = System.nanoTime();
        int resultInt = uselessWork(ints, iters);
        long end = System.nanoTime();
        return new Result(resultInt, lap-start, end-lap);
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
