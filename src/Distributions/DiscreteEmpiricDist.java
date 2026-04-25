package Distributions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiscreteEmpiricDist {

    private double[] probSum;
    private int[] values;
    private int[] maxes;
    private int[] mins;
    private List<Random> generators;
    private Random randSelection;

    public DiscreteEmpiricDist(double[] probabilities, int[] maxes, int[] mins, Random genSeed) {
        if(probabilities.length != maxes.length || probabilities.length != mins.length) {
            throw new IllegalArgumentException("All input arrays must have the same length");
        }

        this.maxes = maxes;
        this.mins = mins;

        randSelection = new Random(genSeed.nextLong());
        generators = new ArrayList<>();
        probSum = new double[probabilities.length];

        double sum = 0;
        for(int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            probSum[i] = sum;
            generators.add(new Random(genSeed.nextLong()));
        }
    }

    public int sample() {
        double row = randSelection.nextDouble();

        for(int i = 0; i < probSum.length; i++) {
            if(row < probSum[i]) {
                return generators.get(i).nextInt(mins[i],maxes[i]);
            }
        }

        return 0;
    }
}