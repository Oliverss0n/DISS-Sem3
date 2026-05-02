package Statistics;

public class Stat {

    private double sum = 0;
    private double sumOfPowers = 0;
    private int count = 0;

    private static final double[] T_VALUES = {
            0, 12.706, 4.303, 3.182, 2.776, 2.571,
            2.447, 2.365, 2.306, 2.262, 2.228,
            2.201, 2.179, 2.160, 2.145, 2.131,
            2.120, 2.110, 2.101, 2.093, 2.086,
            2.080, 2.074, 2.069, 2.064, 2.060,
            2.056, 2.052, 2.048, 2.045, 2.042
    };

    public void add(double value) {
        this.sum += value;
        this.sumOfPowers += value * value;
        this.count++;
    }

    public double getMean() {
        if (count == 0) return 0;
        return sum / count;
    }

    public double getVariance() {
        if (count < 2) return 0;
        return (sumOfPowers - (sum * sum) / count) / (count - 1);
    }

    public double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public int getCount() {
        return count;
    }

    private double getTValue() {
        if (count <= 1) return 0;

        int df = count - 1;

        if (df < T_VALUES.length) {
            return T_VALUES[df];
        } else {
            return T_VALUES[T_VALUES.length - 1];
        }
    }

    public double getConfidenceIntervalHalfWidth() {
        if (count < 2) return 0;
        double t = getTValue();
        return t * getStdDev() / Math.sqrt(count);
    }

    public double getConfidenceIntervalLower() {
        return getMean() - getConfidenceIntervalHalfWidth();
    }

    public double getConfidenceIntervalUpper() {
        return getMean() + getConfidenceIntervalHalfWidth();
    }

    public void clear() {
        this.sum = 0;
        this.sumOfPowers = 0;
        this.count = 0;
    }
}