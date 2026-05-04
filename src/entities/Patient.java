package entities;

import OSPAnimator.AnimItem;


public class Patient implements Comparable<Patient> {
    private static int idCounter = 0;

    //navrhla AI ku gui
    private String stav = "Kráča k recepcii";
    //
    private AnimItem animItem;
    private final int id;
    private final boolean isAmbulance;
    private int priority;

    private final double arrivalTimeBuilding;
    private double arrivalTimeQueueExam;
    private double startTimeExam;
    private double arrivalTimeQueueTreatment;
    private double startTimeTreatment;
    private double endTimeTreatment;

    //animacia
    private java.awt.Point visualAmbPosition;

    private Nurse assignedNurse;
    private Doctor assignedDoctor;

    private final double arrivalTime;
    private double totalWaitingTime = 0.0;


    public Patient(boolean isAmbulance, double currentTime) {
        this.id = ++idCounter;
        this.isAmbulance = isAmbulance;
        this.arrivalTimeBuilding = currentTime;
        this.priority = -1;
        this.arrivalTime = currentTime;
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

    public double getArrivalTime() {
        return arrivalTime;
    }

    //navrhla AI
    @Override
    public int compareTo(Patient iny) {

        //vstupny rad pred priradenim priority
        if (this.priority == -1 && iny.priority == -1) {
            if (this.isAmbulance && !iny.isAmbulance){
                return -1;
            }
            if (!this.isAmbulance && iny.isAmbulance) {
                return 1;
            }

            return Double.compare(this.arrivalTimeQueueExam, iny.arrivalTimeQueueExam);
        }

        int prioCompare = Integer.compare(this.priority, iny.priority);

        if (prioCompare != 0) {
            return prioCompare;
        }

        return Double.compare(this.arrivalTimeQueueTreatment, iny.arrivalTimeQueueTreatment);
    }

    //ANIMAICA
    public AnimItem getAnimItem() {
        return animItem;
    }

    public void setAnimItem(AnimItem animItem) {
        this.animItem = animItem;
    }

    public java.awt.Point getVisualAmbPosition() {
        return visualAmbPosition;
    }

    public void setVisualAmbPosition(java.awt.Point visualAmbPosition) {
        this.visualAmbPosition = visualAmbPosition;
    }

    public Nurse getAssignedNurse() {
        return assignedNurse;
    }

    public void setAssignedNurse(Nurse assignedNurse) {
        this.assignedNurse = assignedNurse;
    }

    public Doctor getAssignedDoctor() {
        return assignedDoctor;
    }

    public void setAssignedDoctor(Doctor assignedDoctor) {
        this.assignedDoctor = assignedDoctor;
    }

    public void addWaitingTime(double time) {
        this.totalWaitingTime += time;
    }

    public double getTotalWaitingTime() {
        return totalWaitingTime;
    }
}