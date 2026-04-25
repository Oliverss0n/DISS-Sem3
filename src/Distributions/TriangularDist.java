package Distributions;

import java.util.Random;

public class TriangularDist {

    private Random random;
    private double min;
    private double max;
    private double mode;


    public TriangularDist(double min, double max, double modus, Random genSeed) {
        this.random = new Random(genSeed.nextLong());
        this.min = min;
        this.max = max;
        this.mode = modus;
    }

    // vygenerovane pomocou AI
    public double sample() {
        double u = random.nextDouble();
        if (u < (mode - min) / (max - min)) {
            return min + Math.sqrt(u * (max - min) * (mode - min));
        } else {
            return max - Math.sqrt((1 - u) * (max - min) * (max - mode));
        }
    }

}
