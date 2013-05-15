public class HardWork {
    private HardWork() {}

    public static int work(int[] ints, int iters) {
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
