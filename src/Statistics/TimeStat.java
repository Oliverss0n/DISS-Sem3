/*package Statistics;

public class TimeStat {

    //dostane referenciu na vseobecne simulacny jadro

    //1. atr cas poslednej zmeny
    //2. atr suma hodnot
    // uzivatel si tam poslal napr. novu hodnota vo fronte = 5 takze poznaci si do tej statistiky: aktualny stav doteraz bol 4, ten si pamata
    // pamata si cas poslednej zmeny
    // zobere si aktualny simulacny cas, ktory si nacita priamo z sim. jadra urobi si diferenciu: akt sim cas - cas poslednej zmeny * doterajsi stav v statistike a pripocita do sumy
    // a ked si vyziadam metodou priemer tak to vydeli aktualnym simulacnym casom.

    private EventCore core;
    private double lastChangeTime;
    private double sum;
    private double sumOfPowers;
    private double lastValue;
    private double startTime;

    public TimeStat(EventCore core) {
        this.core = core;
        this.lastChangeTime = 0;
        this.sum = 0;
        this.sumOfPowers = 0;
        this.lastValue = 0;
        this.startTime = 0;
    }

    public void add(double newValue) {
        double currentTime = core.getCurrentTime();
        double deltaTime = currentTime - lastChangeTime;

        sum += deltaTime * lastValue;
        sumOfPowers += deltaTime * lastValue * lastValue;

        lastChangeTime = currentTime;
        lastValue = newValue;
    }

    public double getMean() {
        double currentTime = core.getCurrentTime();

        double activeTime = currentTime - startTime;
        if (activeTime <= 0) {
            return 0;
        }

        double totalSum = sum + lastValue * (currentTime - lastChangeTime);

        return totalSum / activeTime;
    }

    public double getVariance() {
        double currentTime = core.getCurrentTime();
        if (currentTime == 0) return 0;

        double totalSum = sum + lastValue * (currentTime - lastChangeTime);
        double totalSumSq = sumOfPowers + lastValue * lastValue * (currentTime - lastChangeTime);

        double mean = totalSum / currentTime;

        return (totalSumSq / currentTime) - (mean * mean);
    }

    public double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public void clear(double currentTime) {
        this.sum = 0;
        this.sumOfPowers = 0;

        this.lastChangeTime = currentTime;

        this.startTime = currentTime;
    }


}
*/