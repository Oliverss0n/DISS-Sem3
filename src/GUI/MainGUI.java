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

import OSPAnimator.AnimShape;
import OSPAnimator.AnimShapeItem;
import OSPAnimator.AnimTextItem;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import OSPAnimator.AnimQueue;


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

    // --- Animacia ---
    private JCheckBox cbAnimation;
    private JPanel animationPanel;
    private JSplitPane mainSplit;

    private JLabel lblAmbAStatus, lblAmbBStatus;

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
        setSize(1000, 800);

        sim.setLogger(msg -> {
            SwingUtilities.invokeLater(() -> {
                taLogs.append(msg + "\n");
                taLogs.setCaretPosition(taLogs.getDocument().getLength());
            });
        });

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(buildControlPanel(), BorderLayout.NORTH);
        topPanel.add(buildStatsPanel(), BorderLayout.CENTER);

        JSplitPane tableLogSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildTablePanel(), buildLogPanel());
        tableLogSplit.setResizeWeight(0.5);

        animationPanel = new JPanel(new BorderLayout());
        animationPanel.setBorder(BorderFactory.createTitledBorder("Mapa Urgentu"));
        animationPanel.add(new JLabel("Animácia je vypnutá", SwingConstants.CENTER), BorderLayout.CENTER);

        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableLogSplit, animationPanel);
        mainSplit.setResizeWeight(0.5);
        mainSplit.setDividerLocation(500);
        mainSplit.setOneTouchExpandable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(mainSplit, BorderLayout.CENTER);

        sim.setLogEnabled(cbVisualMode.isSelected());

        add(mainPanel);
        setVisible(true);
    }

    private JPanel buildControlPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        p.setBorder(BorderFactory.createTitledBorder("Ovládanie"));

        tfReplications = new JTextField("10", 5);
        tfDoctors = new JTextField("5", 3);
        tfNurses = new JTextField("5", 3);

        cbVisualMode = new JCheckBox("Vizuálny režim", true);
        cbAnimation = new JCheckBox("Animácia", false);

        speedSlider = new JSlider(1, 100, 10);
        lblSpeedDisplay = new JLabel("Rýchlosť: 1.0x");

        btnStart = new JButton("Spustiť");
        btnPause = new JButton("Pauza");

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

        java.awt.event.ActionListener visualModeListener = e -> {
            boolean isAnyVisualOn = cbVisualMode.isSelected() || cbAnimation.isSelected();
            speedSlider.setEnabled(isAnyVisualOn);
            updateSimSpeed();
            sim.setLogEnabled(cbVisualMode.isSelected());

            // Dynamické zapnutie/vypnutie animátora podľa stavu checkboxu
            if (cbAnimation.isSelected()) {
                if (!sim.animatorExists()) {
                    sim.createAnimator();
                    sim.animator().setSynchronizedTime(false);

                    animationPanel.removeAll();
                    animationPanel.add(sim.animator().canvas(), BorderLayout.CENTER);
                    setupAnimationEnvironment(sim);

                    animationPanel.revalidate();
                    animationPanel.repaint();
                }
            } else {
                if (sim.animatorExists()) {
                    sim.removeAnimator(); // Toto natvrdo odpojí animátor od simulačného jadra

                    animationPanel.removeAll();
                    animationPanel.add(new JLabel("Animácia je vypnutá (Turbomód)", SwingConstants.CENTER), BorderLayout.CENTER);
                    animationPanel.revalidate();
                    animationPanel.repaint();
                }
            }
        };

        cbVisualMode.addActionListener(visualModeListener);
        cbAnimation.addActionListener(visualModeListener);

        p.add(new JLabel("Replikácie:")); p.add(tfReplications);
        p.add(new JLabel("Lekári:")); p.add(tfDoctors);
        p.add(new JLabel("Sestry:")); p.add(tfNurses);
        p.add(cbVisualMode);
        p.add(cbAnimation);
        p.add(btnStart); p.add(btnPause);
        p.add(speedSlider); p.add(lblSpeedDisplay);

        lblSimTime = new JLabel(" Čas: 0.00");
        lblSimTime.setFont(new Font("Monospaced", Font.BOLD, 14));
        p.add(lblSimTime);

        return p;
    }

    private JPanel buildStatsPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel curPanel = new JPanel(new GridLayout(6, 1));
        curPanel.setBorder(BorderFactory.createTitledBorder("Aktuálny stav"));
        lblCurQueueEntrance = new JLabel("Rad na vstup: 0");
        lblCurQueueExam = new JLabel("Rad na ošetrenie: 0");
        lblCurFreeDoctors = new JLabel("Voľní lekári: 0");
        lblCurFreeNurses = new JLabel("Voľné sestry: 0");
        lblAmbAStatus = new JLabel("Amb A: ");
        lblAmbBStatus = new JLabel("Amb B: ");
        curPanel.add(lblCurQueueEntrance); curPanel.add(lblCurQueueExam);
        curPanel.add(lblCurFreeDoctors); curPanel.add(lblCurFreeNurses);
        curPanel.add(lblAmbAStatus); curPanel.add(lblAmbBStatus);

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

        String[] columns = {"ID", "Typ", "Prio", "Stav / Čas"};

        modelChodba = new DefaultTableModel(columns, 0);
        tblChodba = new JTable(modelChodba);
        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createTitledBorder("Presuny (Chodba)"));
        p1.add(new JScrollPane(tblChodba), BorderLayout.CENTER);

        modelVstup = new DefaultTableModel(columns, 0);
        tblVstup = new JTable(modelVstup);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Vstupné vyšetrenie"));
        p2.add(new JScrollPane(tblVstup), BorderLayout.CENTER);

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


    private void runSim() {
        if (btnStart.getText().equals("Spustiť")) {

            sim = new MySimulation();
            sim.registerDelegate(this);
            sim.setLogger(msg -> {
                SwingUtilities.invokeLater(() -> {
                    taLogs.append(msg + "\n");
                    taLogs.setCaretPosition(taLogs.getDocument().getLength());
                });
            });
            sim.setLogEnabled(cbVisualMode.isSelected());

            int docs = Integer.parseInt(tfDoctors.getText());
            int nurses = Integer.parseInt(tfNurses.getText());
            int reps = Integer.parseInt(tfReplications.getText());

            sim.setNumDoctors(docs);
            sim.setNumNurses(nurses);

            taLogs.setText("");
            updateSimSpeed();

            animationPanel.removeAll();

            if (cbAnimation.isSelected()) {
                sim.createAnimator();
                //sim.animator().setSynchronizedTime(true); //bolo doteraz
                sim.animator().setSynchronizedTime(false);

                animationPanel.add(sim.animator().canvas(), BorderLayout.CENTER);
                setupAnimationEnvironment(sim);
            } else {
                animationPanel.add(new JLabel("Animácia je vypnutá", SwingConstants.CENTER), BorderLayout.CENTER);
            }
            animationPanel.revalidate();
            animationPanel.repaint();

            simThread = new Thread() {
                public void run() {
                    try {
                        sim.simulate(reps, 2419200.0);
                    } catch (Exception ex) {
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
        if (!cbVisualMode.isSelected() && !cbAnimation.isSelected()) {
            sim.setMaxSimSpeed();
            lblSpeedDisplay.setText("Rýchlosť: MAX");
        } else {
            int sliderValue = speedSlider.getValue();
            double speedMultiplier;

            if (sliderValue < 10) {
                speedMultiplier = sliderValue / 10.0;
            } else if (sliderValue == 10) {
                speedMultiplier = 1.0;
            } else {
                double base = sliderValue - 9;
                speedMultiplier = (base * base) * 0.5;
            }

            double duration = 0.02;
            double interval = duration * speedMultiplier;

            if (speedMultiplier < 10.0) {
                lblSpeedDisplay.setText(String.format("Rýchlosť: %.1fx", speedMultiplier));
            } else {
                lblSpeedDisplay.setText(String.format("Rýchlosť: %.0fx", speedMultiplier));
            }

            sim.setSimSpeed(interval, duration);
        }
    }



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
        if (!cbVisualMode.isSelected() && !cbAnimation.isSelected()) return;

        double totalSeconds = smltn.currentTime();
        long days = (long) totalSeconds / 86400;
        long hours = (long) (totalSeconds % 86400) / 3600;
        long minutes = (long) (totalSeconds % 3600) / 60;
        long seconds = (long) totalSeconds % 60;

        SwingUtilities.invokeLater(() -> {
            lblSimTime.setText(String.format(" Čas: Deň %d | %02d:%02d:%02d", days + 1, hours, minutes, seconds));

            lblCurQueueEntrance.setText("Rad na vstup: " + sim.getQueueEntranceSize());
            lblCurQueueExam.setText("Rad na ošetrenie: " + sim.getQueueExamSize());
            lblCurFreeDoctors.setText("Voľní lekári: " + sim.agentZdrojov().getFreeDoctors().size());
            lblCurFreeNurses.setText("Voľné sestry: " + sim.agentZdrojov().getFreeNurses().size());
            StringBuilder statusA = new StringBuilder("Amb A: ");
            for (boolean obsadena : sim.obsadeneAmbA) {
                statusA.append(obsadena ? "[■] " : "[ ] ");
            }
            lblAmbAStatus.setText(statusA.toString());

            StringBuilder statusB = new StringBuilder("Amb B: ");
            for (boolean obsadena : sim.obsadeneAmbB) {
                statusB.append(obsadena ? "[■] " : "[ ] ");
            }
            lblAmbBStatus.setText(statusB.toString());

            if (cbVisualMode.isSelected()) {
                modelChodba.setRowCount(0);
                modelVstup.setRowCount(0);
                modelOsetrenie.setRowCount(0);

                List<Patient> patients = ((MySimulation)smltn).getActivePatients();

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

                        if (stav != null) {
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
                    }
                }
            }
        });
    }

    //pokus o animaciu - vygenerovane pomocou ai
    //pokus o animaciu - vygenerovane pomocou ai
    private void setupAnimationEnvironment(MySimulation sim) {
        if (!sim.animatorExists()) return;

        try {
            java.awt.image.BufferedImage bgImage = javax.imageio.ImageIO.read(new java.io.File("img/pozadie.png")); // uisti sa, že je to pozadie_2.png
            sim.animator().setBackgroundImage(bgImage);
        } catch (java.io.IOException e) {
            System.err.println("Chyba pozadia: " + e.getMessage());
        }

        // Sestry (Trojuholníky)
        int pocetSestier = sim.getNumNurses();
        sim.grafikaSestier = new OSPAnimator.AnimShapeItem[pocetSestier];
        for (int i = 0; i < pocetSestier; i++) {
            sim.grafikaSestier[i] = new OSPAnimator.AnimShapeItem(OSPAnimator.AnimShape.TRIANGLE, java.awt.Color.GREEN, 15);
            java.awt.Point home = new java.awt.Point(220 + (i * 10), 550);
            sim.grafikaSestier[i].setPosition(home.x, home.y);
            sim.domovSestier.put(sim.grafikaSestier[i], home);
            sim.animator().register(sim.grafikaSestier[i]);
        }

        // Lekári (Štvorce)
        int pocetLekarov = sim.getNumDoctors();
        sim.grafikaLekarov = new OSPAnimator.AnimShapeItem[pocetLekarov];
        for (int i = 0; i < pocetLekarov; i++) {
            sim.grafikaLekarov[i] = new OSPAnimator.AnimShapeItem(OSPAnimator.AnimShape.RECTANGLE, java.awt.Color.CYAN, 15);
            java.awt.Point home = new java.awt.Point(220 + (i * 10), 530);
            sim.grafikaLekarov[i].setPosition(home.x, home.y);
            sim.domovLekarov.put(sim.grafikaLekarov[i], home);
            sim.animator().register(sim.grafikaLekarov[i]);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            new MainGUI();
        });
    }
}