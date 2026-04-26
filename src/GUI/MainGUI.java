package GUI;

import OSPABA.ISimDelegate;
import OSPABA.SimState;
import OSPABA.Simulation;
import simulation.MySimulation;
import entities.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainGUI extends JFrame implements ISimDelegate {

    // --- Simulačné jadro a vlákno ---
    private MySimulation sim;
    private Thread simThread;

    // --- GUI Komponenty ---
    private JSlider speedSlider;
    private JButton btnStart, btnPause;
    private JCheckBox cbVisualMode;
    private JLabel lblSimTime, lblSpeedDisplay;
    private JTextField tfReplications, tfDoctors, tfNurses;

    // --- Štatistiky ---
    private JLabel lblCurQueueEntrance, lblCurQueueExam, lblCurFreeDoctors, lblCurFreeNurses;
    private JLabel lblGlobAvgTime, lblGlobAvgWait, lblGlobReplications;

    // --- Tabuľka ---
    private DefaultTableModel tmPatients;
    private JTable tblPatients;

    // --- Logovanie ---
    private JTextArea taLogs;
    private JButton btnClearLogs;

    private DefaultTableModel modelChodba, modelVstup, modelOsetrenie;
    private JTable tblChodba, tblVstup, tblOsetrenie;

    public MainGUI() {
        // Inicializácia jadra
        sim = new MySimulation();
        java.io.OutputStream out = new java.io.OutputStream() {
            @Override
            public void write(int b) {
                SwingUtilities.invokeLater(() -> {
                    if (taLogs != null) {
                        taLogs.append(String.valueOf((char) b));
                        taLogs.setCaretPosition(taLogs.getDocument().getLength());
                    }
                });
            }
        };
        System.setOut(new java.io.PrintStream(out, true));
        System.setErr(new java.io.PrintStream(out, true));
        sim.registerDelegate(this);

        // Vytvorenie GUI okna
        setTitle("Nemocnica - Urgentný príjem");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 800); // Mierne zväčšené pre lepšiu viditeľnosť logov

        // Nastavenie prepojenia logera hneď na začiatku
        // POZOR: MySimulation už musí mať implementované metódy setLogger a setLogEnabled
        sim.setLogger(msg -> {
            SwingUtilities.invokeLater(() -> {
                taLogs.append(msg + "\n");
                taLogs.setCaretPosition(taLogs.getDocument().getLength()); // Automatický scroll dole
            });
        });

        // --- ZOSTAVENIE HLAVNÉHO ROZLOŽENIA ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(buildControlPanel(), BorderLayout.NORTH);
        topPanel.add(buildStatsPanel(), BorderLayout.CENTER);

        // Rozdelenie obrazovky na tabuľku (hore) a logy (dole) pomocou JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildTablePanel(), buildLogPanel());
        splitPane.setResizeWeight(0.5); // Tabuľka a logy si rozdelia priestor 1:1

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Synchronizácia počiatočného stavu logovania
        sim.setLogEnabled(cbVisualMode.isSelected());

        add(mainPanel);
        setVisible(true);
    }

    // ==========================================
    // 1. ZOSTAVENIE GUI
    // ==========================================

    private JPanel buildControlPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        p.setBorder(BorderFactory.createTitledBorder("Ovládanie"));

        tfReplications = new JTextField("10", 5);
        tfDoctors = new JTextField("5", 3);
        tfNurses = new JTextField("5", 3);

        cbVisualMode = new JCheckBox("Vizuálny režim", true);

        // Slider od 1 (najpomalšie) do 100 (najrýchlejšie)
        speedSlider = new JSlider(1, 100, 50);
        lblSpeedDisplay = new JLabel("Rýchlosť: 50");

        btnStart = new JButton("Spustiť");
        btnPause = new JButton("Pauza");

        // --- LISTENERS ---
        btnStart.addActionListener(e -> runSim());

        btnPause.addActionListener(e -> {
            if (sim.isPaused()) {
                sim.resumeSimulation();
                btnPause.setText("Pauza");
            } else {
                sim.pauseSimulation();
                btnPause.setText("Pokračovať");
            }
        });

        speedSlider.addChangeListener(e -> {
            lblSpeedDisplay.setText("Rýchlosť: " + speedSlider.getValue());
            updateSimSpeed();
        });

        cbVisualMode.addActionListener(e -> {
            speedSlider.setEnabled(cbVisualMode.isSelected());
            updateSimSpeed();
            sim.setLogEnabled(cbVisualMode.isSelected()); // Aktualizuje stav logovania v simulácii
        });

        p.add(new JLabel("Replikácie:")); p.add(tfReplications);
        p.add(new JLabel("Lekári:")); p.add(tfDoctors);
        p.add(new JLabel("Sestry:")); p.add(tfNurses);
        p.add(cbVisualMode);
        p.add(btnStart); p.add(btnPause);
        p.add(speedSlider); p.add(lblSpeedDisplay);

        lblSimTime = new JLabel(" Čas: 0.00");
        lblSimTime.setFont(new Font("Monospaced", Font.BOLD, 14));
        p.add(lblSimTime);

        return p;
    }

    private JPanel buildStatsPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel curPanel = new JPanel(new GridLayout(4, 1));
        curPanel.setBorder(BorderFactory.createTitledBorder("Aktuálny stav"));
        lblCurQueueEntrance = new JLabel("Rad na vstup: 0");
        lblCurQueueExam = new JLabel("Rad na ošetrenie: 0");
        lblCurFreeDoctors = new JLabel("Voľní lekári: 0");
        lblCurFreeNurses = new JLabel("Voľné sestry: 0");
        curPanel.add(lblCurQueueEntrance); curPanel.add(lblCurQueueExam);
        curPanel.add(lblCurFreeDoctors); curPanel.add(lblCurFreeNurses);

        JPanel globPanel = new JPanel(new GridLayout(3, 1));
        globPanel.setBorder(BorderFactory.createTitledBorder("Priemery"));
        lblGlobReplications = new JLabel("Replikácia: 0");
        lblGlobAvgTime = new JLabel("Čas v systéme: 0.0");
        lblGlobAvgWait = new JLabel("Čas čakania: 0.0");
        globPanel.add(lblGlobReplications);
        globPanel.add(lblGlobAvgTime); globPanel.add(lblGlobAvgWait);

        p.add(curPanel);
        p.add(globPanel);
        return p;
    }

    private JPanel buildTablePanel() {
        JPanel mainTablePanel = new JPanel(new GridLayout(1, 3, 5, 5));

        // Spoločné stĺpce pre všetky tabuľky
        String[] columns = {"ID", "Typ", "Prio", "Stav / Čas"};

        // 1. Tabuľka - CHODBA
        modelChodba = new DefaultTableModel(columns, 0);
        tblChodba = new JTable(modelChodba);
        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createTitledBorder("Presuny (Chodba)"));
        p1.add(new JScrollPane(tblChodba), BorderLayout.CENTER);

        // 2. Tabuľka - VSTUP
        modelVstup = new DefaultTableModel(columns, 0);
        tblVstup = new JTable(modelVstup);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Vstupné vyšetrenie"));
        p2.add(new JScrollPane(tblVstup), BorderLayout.CENTER);

        // 3. Tabuľka - OŠETROVANIE
        modelOsetrenie = new DefaultTableModel(columns, 0);
        tblOsetrenie = new JTable(modelOsetrenie);
        JPanel p3 = new JPanel(new BorderLayout());
        p3.setBorder(BorderFactory.createTitledBorder("Ambulancie A / B"));
        p3.add(new JScrollPane(tblOsetrenie), BorderLayout.CENTER);

        mainTablePanel.add(p1);
        mainTablePanel.add(p2);
        mainTablePanel.add(p3);

        return mainTablePanel;
    }

    private JPanel buildLogPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Konzola (Logy zo simulácie)"));

        taLogs = new JTextArea();
        taLogs.setEditable(false);
        taLogs.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(taLogs);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        p.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnClearLogs = new JButton("Vymazať logy");
        btnClearLogs.addActionListener(e -> taLogs.setText(""));
        bottomPanel.add(btnClearLogs);

        p.add(bottomPanel, BorderLayout.SOUTH);
        return p;
    }

    // ==========================================
    // 2. RIADENIE SIMULÁCIE (Podľa vzoru)
    // ==========================================

    private void runSim() {
        if (btnStart.getText().equals("Spustiť")) {

            // --- PRIDAJ TOTO: Úplne čistý štart! ---
            sim = new MySimulation();
            sim.registerDelegate(this);
            sim.setLogger(msg -> {
                SwingUtilities.invokeLater(() -> {
                    taLogs.append(msg + "\n");
                    taLogs.setCaretPosition(taLogs.getDocument().getLength());
                });
            });
            sim.setLogEnabled(cbVisualMode.isSelected());
            // ---------------------------------------

            int docs = Integer.parseInt(tfDoctors.getText());
            int nurses = Integer.parseInt(tfNurses.getText());
            int reps = Integer.parseInt(tfReplications.getText());

            sim.setNumDoctors(docs);
            sim.setNumNurses(nurses);

            taLogs.setText(""); // Vymazanie starej konzoly
            updateSimSpeed();

            simThread = new Thread() {
                public void run() {
                    try {
                        sim.simulate(reps, 2419200.0);
                    } catch (Exception ex) {
                        // Pre istotu si tu necháme tento chyták z minula
                        ex.printStackTrace();
                    }
                }
            };
            simThread.start();
            btnStart.setText("Zastaviť");

        } else {
            sim.stopSimulation();
            btnStart.setText("Spustiť");
        }
    }

    private void updateSimSpeed() {
        if (!cbVisualMode.isSelected()) {
            sim.setMaxSimSpeed(); // Vypnutý vizuálny režim (zbehne to za sekundu)
        } else {
            double sliderValue = speedSlider.getValue(); // Hodnota 1 až 100

            // Krok (koľko simulačných sekúnd zbehne naraz)
            // Používame matematickú mocninu, aby pri stovke simulácia doslova letela
            double interval = sliderValue * sliderValue;

            // Zdržanie v reálnom čase (0.01 sekundy = 10 milisekúnd)
            double duration = 0.01;

            // Ak používateľ stiahne slider úplne na 1 (najpomalšie), ideme po 1 sekunde
            if (sliderValue == 1) {
                interval = 1.0;
                duration = 0.5; // Pol sekundová pauza
            }

            sim.setSimSpeed(interval, duration);
        }
    }

    // ==========================================
    // 3. ISimDelegate METÓDY
    // ==========================================

    @Override
    public void simStateChanged(Simulation simulation, SimState simState) {
        SwingUtilities.invokeLater(() -> {
            switch (simState) {
                case replicationStopped:
                    lblGlobReplications.setText("Replikácia: " + (sim.currentReplication() + 1));
                    break;

                case stopped:
                    btnStart.setText("Spustiť");
                    break;

                case running:
                case replicationRunning:
                    break;
            }
        });
    }

    @Override
    public void refresh(Simulation smltn) {
        // Ak nie je vizuálny režim, šetríme výkon a končíme
        if (!cbVisualMode.isSelected()) return;

        // Výpočet pekného času
        double totalSeconds = smltn.currentTime();
        long days = (long) totalSeconds / 86400;
        long hours = (long) (totalSeconds % 86400) / 3600;
        long minutes = (long) (totalSeconds % 3600) / 60;
        long seconds = (long) totalSeconds % 60;

        // Aktualizácia GUI musí bežať v SwingUtilities vlákne
        SwingUtilities.invokeLater(() -> {
            lblSimTime.setText(String.format(" Čas: Deň %d | %02d:%02d:%02d", days + 1, hours, minutes, seconds));

            lblCurQueueEntrance.setText("Rad na vstup: " + sim.getQueueEntranceSize());
            lblCurQueueExam.setText("Rad na ošetrenie: " + sim.getQueueExamSize());
            lblCurFreeDoctors.setText("Voľní lekári: " + sim.getFreeDoctors());
            lblCurFreeNurses.setText("Voľné sestry: " + sim.getFreeNurses());

            // Vymažeme staré dáta z tabuliek
            modelChodba.setRowCount(0);
            modelVstup.setRowCount(0);
            modelOsetrenie.setRowCount(0);

            // Získame zoznam len RAZ
            List<Patient> patients = ((MySimulation)smltn).getActivePatients();

            // DEBUG výpis (voliteľné)
            // System.out.println("DEBUG: V zozname je " + patients.size() + " pacientov.");

            // KRITICKÁ ČASŤ: Zamkneme zoznam (ArrayList), kým ho prechádzame slučkou
            synchronized (patients) {
                for (Patient p : patients) {
                    String prio = p.getPriority() == -1 ? "-" : String.valueOf(p.getPriority());
                    String typ = p.isAmbulance() ? "Sanitka" : "Pešo";
                    String stav = p.getStav();

                    Object[] rowData = new Object[]{
                            "#" + p.getId(),
                            typ,
                            prio,
                            stav
                    };

                    // Rozdelenie do tabuliek
                    if (stav.contains("Kráča")) {
                        modelChodba.addRow(rowData);
                    }
                    else if (stav.contains("Vstup") || stav.contains("recepcii")) {
                        modelVstup.addRow(rowData);
                    }
                    else if (stav.contains("Amb") || stav.contains("Ošetrenie")) {
                        modelOsetrenie.addRow(rowData);
                    }
                }
            } // Koniec zámku
        });
    }

    // ==========================================
    // SPÚŠŤAČ (MAIN)
    // ==========================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            new MainGUI();
        });
    }
}