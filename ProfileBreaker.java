public class ProfileBreaker {
    private final int iters;
    private final int arraySize;
    private final int spins;
    private long timeSpent;
    private final SimpleRand rand;
    private volatile Result result;
    private int count;

    public ProfileBreaker(int seed, int iters, int arraySize, int spins) {
        this.iters = iters;
        this.arraySize = arraySize;
        this.spins = spins;
        rand = new SimpleRand(seed);
        timeSpent = 0;
    }

    public Result call() {
        long start = System.nanoTime();

        int[] ints = randomInts();
        long lap1 = System.nanoTime();

        int resultInt = HardWork.work(ints, iters);
        long lap2 = System.nanoTime();

        int spinCount = new SpinWork().work(spins);
        long lap3 = System.nanoTime();

        return new Result(resultInt + spinCount, lap1-start, lap2-lap1, lap3-lap2);
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
}
