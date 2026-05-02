package Statistics;

import OSPABA.Simulation;

public class TimeStat {


    private Simulation sim;

    private double lastChangeTime;
    private double sum;
    private double sumOfPowers;
    private double lastValue;
    private double startTime;

    public TimeStat(Simulation sim) {
        this.sim = sim;
        this.lastChangeTime = 0;
        this.sum = 0;
        this.sumOfPowers = 0;
        this.lastValue = 0;
        this.startTime = 0;
    }

    public void add(double newValue) {
        double currentTime = sim.currentTime();
        double deltaTime = currentTime - lastChangeTime;

        sum += deltaTime * lastValue;
        sumOfPowers += deltaTime * lastValue * lastValue;

        lastChangeTime = currentTime;
        lastValue = newValue;
    }

    public double getMean() {
        double currentTime = sim.currentTime();

        double activeTime = currentTime - startTime;
        if (activeTime <= 0) {
            return 0;
        }

        double totalSum = sum + lastValue * (currentTime - lastChangeTime);
        return totalSum / activeTime;
    }

    public double getVariance() {
        double currentTime = sim.currentTime();
        double activeTime = currentTime - startTime;
        if (activeTime <= 0) return 0;

        double totalSum = sum + lastValue * (currentTime - lastChangeTime);
        double totalSumSq = sumOfPowers + lastValue * lastValue * (currentTime - lastChangeTime);

        double mean = totalSum / activeTime;

        return (totalSumSq / activeTime) - (mean * mean);
    }

    public double getStdDev() {
        return Math.sqrt(Math.max(0, getVariance()));
    }

    public void clear(double currentTime) {
        this.sum = 0;
        this.sumOfPowers = 0;
        this.lastChangeTime = currentTime;
        this.startTime = currentTime;
    }
}