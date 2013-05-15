public class SpinWork {
    private int count;

    public int work() {
        // This method spin0's, which spin1's, which spin2's,
        // which does some mod operations. It looks like it should
        // take a long time, but the JIT will realize that the whole
        // operation is a noop and just take it out entirely.
        // What's left is a quick count up to 1000 and back down,
        // on an uncontended lock. This is very quick, but
        // each count is a yield point, so they'll appear in
        // a lot of JVM samples.
        for (int i = 0; i < 1000; ++i) {
            updateCount(1);
        }
        for (int i = 0; i < 10000000; ++i) {
            spin0();
        }
        for (int i = 0; i < 1000; ++i) {
            updateCount(-1);
        }
        return getCount();
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
        for (int i = 0; i < 10; ++i) {
            r += r % (i+1);
        }
        return r;
    }

    private synchronized void updateCount(int delta) {
        count += delta;
    }

    private synchronized int getCount() {
        return count;
    }
}
