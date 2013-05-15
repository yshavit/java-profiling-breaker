public class HardWork {
    private HardWork() {}

    public static int work(int[] ints, int iters) {
        // This method does a fair amount of work -- for multiple iterations it
        // loops over the array, doing some mod operations on each element.
        // However, it contains no yield points -- no memory allocations,
        // synchronization, field accesses (especially no volatile fields), etc.
        // As such, the JVM won't ever sample in this method.
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

}
