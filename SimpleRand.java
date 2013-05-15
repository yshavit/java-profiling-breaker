public class SimpleRand {
    private int v;
    
    public SimpleRand(int seed) {
        v = seed;
        nextInt();
    }

    public int nextInt() {
        return (v = nextInt(v));
    }

    public static int nextInt(int v) {
        // Taken from JCIP 12.1.3 (listing 12.4): http://jcip.net 
        v ^= v << 6;
        v ^= v >>> 21;
        v ^= (v << 7);
        return v;
    }
}
