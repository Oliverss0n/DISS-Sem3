package Distributions;

import java.util.Random;

public class ErlangDist {

    //vygenerovane pomocou AI

    private Random[] randoms;
    private double phaseMean;
    private int k;
    private double offset;

    public ErlangDist(int k, double mean, Random genSeed) {
        this(k, mean, 0.0, genSeed);
    }

    public ErlangDist(int k, double mean, double offset, Random genSeed) {
        this.k = k;
        this.phaseMean = mean / k;
        this.offset = offset;
        this.randoms = new Random[k];
        for (int i = 0; i < k; i++) {
            this.randoms[i] = new Random(genSeed.nextLong());
        }
    }

    public double sample() {
        double result = 0;
        for (int i = 0; i < k; i++) {
            result += -Math.log(randoms[i].nextDouble()) * phaseMean;
        }
        return result + offset;
    }

}