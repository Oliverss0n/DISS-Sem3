package Distributions;

import java.util.Random;

public class ExponentialDist {

    private Random random;
    private double mean;


    public ExponentialDist(double mean, Random genSeed) {
        this.random = new Random(genSeed.nextLong());
        this.mean = mean;
    }

    //Vygenerovane pomocou AI
    public double sample() {
        return -Math.log(random.nextDouble()) * this.mean;
    }


}