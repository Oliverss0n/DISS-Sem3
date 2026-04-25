package Distributions;

import java.util.Random;

public class DiscreteUniformDist {
    private final int min;
    private final int max;
    private final Random random;

    public DiscreteUniformDist(int min, int max, Random genSeed) {
        this.min = min;
        this.max = max;
        this.random = new Random(genSeed.nextLong());
    }

    public int sample() {
        return random.nextInt(min,max + 1);
    }
}
