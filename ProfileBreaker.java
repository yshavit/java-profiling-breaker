import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ProfileBreaker implements Callable<Result> {
    private final int iters;
    private final int arraySize;
    private long timeSpent;
    private final SimpleRand rand;
    private volatile Result result;
    private int count;

    public ProfileBreaker(int seed, int iters, int arraySize) {
        this.iters = iters;
        this.arraySize = arraySize;
        rand = new SimpleRand(seed);
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

        return new Result(resultInt + getCount(), lap1-start, lap2-lap1, lap3-lap2);
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
        for (int i = 0; i < 1000; ++i) {
            updateCount(1);
        }
        for (int i = 0; i < 10000000; ++i) {
            spin0();
        }
        for (int i = 0; i < 1000; ++i) {
            updateCount(-1);
        }
    }

    private void spin0() {
        for (int i = 0; i < 10000000; ++i) {
            spin1();
        }
    }

    private void spin1() {
        for (int i = 0; i < 10000000; ++i) {
            spin2();
        }
    }

    private int spin2() {
        int r = 0;
        for (int i = 0; i < 10000000; ++i) {
            r += 1;
        }
        return r;
    }

    private synchronized void updateCount(int delta) {
        count += delta;
    }

    private synchronized int getCount() {
        return count;
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

    public static class Handler implements Runnable {
        private final BlockingQueue<ProfileBreaker> workQueue;
        private final AtomicInteger resultInt;
        private final CountDownLatch doneLatch;
        private final boolean verbose;

        public Handler(BlockingQueue<ProfileBreaker> workQueue, AtomicInteger resultInt, CountDownLatch doneLatch, boolean verbose) {
            this.workQueue = workQueue;
            this.resultInt = resultInt;
            this.doneLatch = doneLatch;
            this.verbose = verbose;
        }
        
        @Override
        public void run() {
            while(true) {
                ProfileBreaker breaker;
                try {
                    breaker = workQueue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                Result result = breaker.call();
                if (verbose) {
                    message(result);
                }
                resultInt.addAndGet(result.value);
                doneLatch.countDown();
            }
        }
    }

    public static class Producer implements Runnable {
        private final BlockingQueue<ProfileBreaker> workQueue;
        private final int iters;
        private final int arraySize;

        public Producer(BlockingQueue<ProfileBreaker> workQueue, int iters, int arraySize) {
            this.workQueue = workQueue;
            this.iters = iters;
            this.arraySize = arraySize;
        }
        
        @Override
        public void run() {
            SimpleRand rand = new SimpleRand((int)System.nanoTime() + hashCode());
            while (true) {
                try {
                    workQueue.put(new ProfileBreaker(rand.nextInt(), iters, arraySize));
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return; // Done
                }
            }
        }
    }
        
    public static synchronized void message(Object message) {
        System.out.println(String.valueOf(message));
    }

    public static void main(String[] args) throws Exception {
        final int nTasks = Integer.parseInt(args[0]);
        final int iters = Integer.parseInt(args[1]);
        final int arraySize = Integer.parseInt(args[2]);
        final boolean verbose = Boolean.parseBoolean(args[3]);
        
        int nThreads = Integer.parseInt(System.getProperty("threads", "2"));

        final BlockingQueue<ProfileBreaker> breakers = new ArrayBlockingQueue<ProfileBreaker>(nThreads * 10);
        final AtomicInteger resultSum = new AtomicInteger(0);
        final CountDownLatch doneLatch = new CountDownLatch(nTasks);
        for (int i = 0; i < nThreads; ++i) {
            startThread(new Producer(breakers, iters, arraySize));
            startThread(new Handler(breakers, resultSum, doneLatch, verbose));
        }
        
        doneLatch.await();
        System.out.println(resultSum);
    }

    private static void startThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        t.start();
    }
}
