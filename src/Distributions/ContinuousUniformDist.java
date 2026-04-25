package Distributions;

import java.util.Random;

public class ContinuousUniformDist {
    private double min;
    private double max;
    private Random random;


    public ContinuousUniformDist(double min, double max, Random genSeed) {
        this.min = min;
        this.max = max;
        this.random = new Random(genSeed.nextLong());
    }

    public double sample() {
        return min + (random.nextDouble() * (max - min));
    }
}
