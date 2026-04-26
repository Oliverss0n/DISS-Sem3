/*package GUI;

import java.util.List;

public interface IHospitalView {
    void setPresenter(HospitalPresenter presenter);

    // --- OVLÁDANIE ---
    boolean isVisualMode();
    int getSpeedSliderValue();
    void setControlsRunning(boolean isRunning);
    void updatePauseButton(boolean isPaused);

    // --- VSTUPNÉ PARAMETRE MODELU ---
    int getDoctorsCount();
    int getNursesCount();
    int getReplicationsCount();

    // --- PRIEBEŽNÉ ŠTATISTIKY (Pre 1 replikáciu - Vizuálny mód) ---
    // Aktualizuje texty počas behu simulácie (napr. Rad Vstup: 5, Voľní lekári: 2)
    void updateVisualState(double currentTime, int queueEntranceSize, int queueExamSize,
                           int freeDoctors, int freeNurses, int freeAmbA, int freeAmbB);

    // --- GLOBÁLNE ŠTATISTIKY (Po skončení replikácií) ---
    void updateGlobalStats(int completedReps, double avgTimeInSystem, double avgWaitTime,
                           double avgQueueEntrance, double avgQueueExam);
    void updatePatientTable(List<String[]> tableData);

    // --- GRAFY A EXPERIMENTY ---
    void clearGraph();
    void addGraphPoint(int numDoctors, double avgWaitTime); // Príklad pre graf citlivosti
}*/
