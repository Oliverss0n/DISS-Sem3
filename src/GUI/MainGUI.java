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
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Zoznam aktívnych pacientov"));

        String[] columns = {"ID Pacienta", "Príchod", "Priorita", "Čas v systéme"};
        tmPatients = new DefaultTableModel(columns, 0);
        tblPatients = new JTable(tmPatients);

        p.add(new JScrollPane(tblPatients), BorderLayout.CENTER);
        return p;
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
        lblSimTime.setText(String.format(" Čas: %.2f s", sim.currentTime()));

        lblCurQueueEntrance.setText("Rad na vstup: " + sim.getQueueEntranceSize());
        lblCurQueueExam.setText("Rad na ošetrenie: " + sim.getQueueExamSize());
        lblCurFreeDoctors.setText("Voľní lekári: " + sim.getFreeDoctors());
        lblCurFreeNurses.setText("Voľné sestry: " + sim.getFreeNurses());

        // Aktualizácia tabuľky
        List<Patient> patients = sim.getActivePatients();
        tmPatients.setRowCount(0); // Vymaže staré riadky
        for (Patient p : patients) {
            String priorita = p.getPriority() == -1 ? "Čaká" : String.valueOf(p.getPriority());
            String prichod = p.isAmbulance() ? "Sanitka" : "Pešo";
            double stravenyCas = sim.currentTime() - p.getArrivalTimeBuilding();

            tmPatients.addRow(new Object[]{
                    "#" + p.getId(),
                    prichod,
                    priorita,
                    String.format("%.1f s", stravenyCas)
            });
        }
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