package entities;

/**
 * Represents a patient as a passive entity (data object).
 */
public class Patient {
    private static int idCounter = 0;
    private String stav = "Kráča k recepcii";

    private final int id;
    private final boolean isAmbulance;
    private int priority;

    private final double arrivalTimeBuilding;
    private double arrivalTimeQueueExam;
    private double startTimeExam;
    private double arrivalTimeQueueTreatment;
    private double startTimeTreatment;
    private double endTimeTreatment;

    public Patient(boolean isAmbulance, double currentTime) {
        this.id = ++idCounter;
        this.isAmbulance = isAmbulance;
        this.arrivalTimeBuilding = currentTime;
        this.priority = -1; // Not yet assigned
    }


    public int getId() {
        return id;
    }
    public boolean isAmbulance() {
        return isAmbulance;
    }

    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getArrivalTimeBuilding() {
        return arrivalTimeBuilding;
    }

    public void setArrivalTimeQueueExam(double time) {
        this.arrivalTimeQueueExam = time;
    }
    public double getArrivalTimeQueueExam() {
        return arrivalTimeQueueExam;
    }

    public void setStartTimeExam(double time) {
        this.startTimeExam = time;
    }
    public double getStartTimeExam() {
        return startTimeExam;
    }

    public void setArrivalTimeQueueTreatment(double time) {
        this.arrivalTimeQueueTreatment = time;
    }
    public double getArrivalTimeQueueTreatment() {
        return arrivalTimeQueueTreatment;
    }

    public void setStartTimeTreatment(double time) {
        this.startTimeTreatment = time;
    }

    public double getStartTimeTreatment() {
        return startTimeTreatment;
    }

    public void setEndTimeTreatment(double time) {
        this.endTimeTreatment = time;
    }
    public double getEndTimeTreatment() {
        return endTimeTreatment;
    }
    public static void resetIdCounter() {
        idCounter = 0;
    }

    public String getStav() {
        return stav;
    }

    public void setStav(String stav) {
        this.stav = stav;
    }
}