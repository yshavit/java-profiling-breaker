public class NoSyncRand {
    private int v;
    
    public NoSyncRand(int seed) {
        v = seed;
        nextInt();
    }

    public int nextInt() {
        // Taken from JCIP 12.1.3 (listing 12.4): http://jcip.net 
        v ^= v << 6;
        v ^= v >>> 21;
        v ^= (v << 7);
        return v;
    }
}
