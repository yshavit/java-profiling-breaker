package com.yuvalshavit.profilebreaker;

public class SpinWork {
    private int count;

    public int work(int spins) {
        // This method spin0's, which spin1's, which spin2's,
        // which does some mod operations. It looks like it should
        // take a long time, but the JIT will realize that the whole
        // operation is a noop and just take it out entirely.
        // What's left is a quick count up to 1000 and back down,
        // on an uncontended lock. This is very quick, but
        // each count is a yield point, so they'll appear in
        // a lot of JVM samples.
        for (int i = 0; i < spins; ++i) {
            updateCount(i);
        }
        return getCount();
    }

    private synchronized void updateCount(int delta) {
        int randomizedCount = SimpleRand.nextInt(delta + count);
        String s = String.valueOf(randomizedCount);
        count = s.length();
    }

    private synchronized int getCount() {
        return count;
    }
}
