package com.yuvalshavit.profilebreaker;

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
        
//        iterateOver(ints, iters);
        for (int i = 0; i < len; ++i) {
            localResult += ints[i];
        }
        return localResult;
    }

//    private static void iterateOver(int[] ints, int iters) {
//        for (int iter = 0; iter < iters; ++iter) {
//            mungeInts(ints);
//        }
//    }
//    
//    private static void mungeInts(int[] ints) {
//        final int len = ints.length;
//        for (int i = 1; i < len; ++i) {
//            if (ints[i-1] != 0) {
//                ints[i] %= ints[i-1];
//            }
//        }
//    }
//    
//    private static boolean shoudlMunge(int[] ints, int i) {
//        return ints[i - 1] != 0;
//    }
//    
//    private static void munge(int[] ints, int i) {
//        ints[i] %= ints[i-1];
//    }
}
