import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Main {

    public static void main(String[] args) throws Exception {
        final int nTasks = Integer.parseInt(args[0]);
        final int iters = Integer.parseInt(args[1]);
        final int arraySize = Integer.parseInt(args[2]);
        final int spins = Integer.parseInt(args[3]);
        final boolean verbose = Boolean.parseBoolean(args[4]);
        
        int nThreads = Integer.parseInt(System.getProperty("threads", "2"));

        final BlockingQueue<ProfileBreaker> breakers = new ArrayBlockingQueue<ProfileBreaker>(nThreads * 10);
        final AtomicInteger resultSum = new AtomicInteger(0);
        final CountDownLatch doneLatch = new CountDownLatch(nTasks);
        for (int i = 0; i < nThreads; ++i) {
            startThread(new Producer(breakers, iters, arraySize, spins));
            startThread(new Handler(breakers, resultSum, doneLatch, verbose));
        }
        
        doneLatch.await();
        System.out.println(resultSum);
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
        private final int spins;

        public Producer(BlockingQueue<ProfileBreaker> workQueue, int iters, int arraySize, int spins) {
            this.workQueue = workQueue;
            this.iters = iters;
            this.arraySize = arraySize;
            this.spins = spins;
        }
        
        @Override
        public void run() {
            SimpleRand rand = new SimpleRand((int)System.nanoTime() + hashCode());
            while (true) {
                try {
                    workQueue.put(new ProfileBreaker(rand.nextInt(), iters, arraySize, spins));
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

    private static void startThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        t.start();
    }
}
