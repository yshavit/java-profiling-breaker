public class SpinWork {
    private int count;

    public int work() {
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
