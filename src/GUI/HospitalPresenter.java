/*package GUI;

import OSPABA.ISimDelegate; // Dôležité: Musí to byť tento import z OSPABA
import OSPABA.SimState;
import OSPABA.Simulation;
import entities.Patient;
import simulation.MySimulation;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public class HospitalPresenter implements ISimDelegate {

    private IHospitalView view;
    private MySimulation core;

    public HospitalPresenter(IHospitalView view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    public void startSimulation() {
        // 1. Prečítame parametre z GUI
        int docs = view.getDoctorsCount();
        int nurses = view.getNursesCount();
        int reps = view.getReplicationsCount();

        // 2. Vytvoríme inštanciu simulácie a nastavíme parametre
        core = new MySimulation();
        core.setNumDoctors(docs);
        core.setNumNurses(nurses);

        // 3. Zaregistrujeme tento Presenter ako delegáta pre OSPABA
        core.registerDelegate(this);

        // 4. Nastavenie rýchlosti pre vizuálny režim
        if (view.isVisualMode()) {
            // Nastavíme simulačný krok a reálnu pauzu v milisekundách podľa slidera
            long pause = calculatePauseDuration(view.getSpeedSliderValue());
            core.setSimSpeed(1.0, pause); // Každú 1.0 sim. sekundu sa zavolá refresh()
        }

        // 5. Aktualizujeme GUI tlačidlá a spustíme asynchrónne
        view.setControlsRunning(true);
        //core.simulateAsync(reps);
        core.simulateAsync(reps, 86400.0); //zmenit
    }

    public void stopSimulation() {
        if (core != null) {
            core.stopSimulation();
            view.setControlsRunning(false);
        }
    }

    public void pauseSimulation() {
        if (core != null) {
            if (core.isPaused()) {
                core.resumeSimulation();
                view.updatePauseButton(false);
            } else {
                core.pauseSimulation();
                view.updatePauseButton(true);
            }
        }
    }

    // ==========================================================
    // METÓDY Z ROZHRANIA ISimDelegate (Volá ich samotné OSPABA)
    // ==========================================================

    @Override
    public void simStateChanged(Simulation simulation, SimState simState) {

    }

    @Override
    public void refresh(Simulation sim) {
        MySimulation mySim = (MySimulation) sim;

        // 1. Vytiahneme dáta pre tabuľku (Ešte pred invokeLater, v sim. vlákne)
        List<Patient> patients = mySim.getActivePatients();
        List<String[]> tableData = new ArrayList<>();

        for (Patient p : patients) {
            tableData.add(new String[]{
                    "#" + p.getId(),
                    p.isAmbulance() ? "Sanitka" : "Pešo",
                    p.getPriority() == -1 ? "Čaká" : String.valueOf(p.getPriority()),
                    String.format("%.2f", p.getArrivalTimeBuilding())
            });
        }

        // 2. Prečítame všetky čísla zo simulácie
        double time = mySim.currentTime();
        int qEnt = mySim.getQueueEntranceSize();
        int qExam = mySim.getQueueExamSize();
        int fDoc = mySim.getFreeDoctors();
        int fNur = mySim.getFreeNurses();
        int fAmbA = mySim.getFreeAmbA();
        int fAmbB = mySim.getFreeAmbB();

        // 3. Bezpečne pošleme hotové dáta do GUI vlákna
        SwingUtilities.invokeLater(() -> {
            view.updateVisualState(time, qEnt, qExam, fDoc, fNur, fAmbA, fAmbB);
            view.updatePatientTable(tableData);
        });
    }




    // ==========================================================
    // POMOCNÉ METÓDY
    // ==========================================================

    private long calculatePauseDuration(int sliderValue) {
        // Jednoduchý prepočet: vyššia hodnota slidera = menšia pauza (rýchlejší beh)
        // Ak má slider rozsah napr. 0 až 10, uprav logiku podľa seba
        if (sliderValue == 10) return 0; // Najrýchlejšie (bez umelej pauzy)
        return (long) (1000 / (sliderValue + 1));
    }
}*/