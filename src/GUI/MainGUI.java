package GUI;

import OSPABA.ISimDelegate;
import OSPABA.SimState;
import OSPABA.Simulation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
    // --- Štatistiky ---
    private JLabel lblCurQueueEntrance, lblCurQueueExam, lblCurFreeDoctors, lblCurFreeNurses;
    // ZMENENÉ NÁZVY A PRIDANÉ NOVÉ LABELy PRE VYŤAŽENIE:
    private JLabel lblGlobReplications, lblGlobAvgWaitAmbulance, lblGlobAvgWaitWalkIn, lblGlobUtilNurses, lblGlobUtilDoctors;
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

    //zahrievanie:
    private XYSeries eyeballingSeries;
    private JTextField tfEyeballingStep, tfEyeballingReps;

    // --- Štatistiky ---
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

        // --- PRIDANIE ZÁLOŽIEK (TABS) ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Hlavná Simulácia", mainPanel);
        tabbedPane.addTab("Graf Zahrievania (Eyeballing)", buildWarmUpPanel());

        add(tabbedPane);
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

    private JPanel buildWarmUpPanel() {
        JPanel warmUpPanel = new JPanel(new BorderLayout());

        // Horný ovládací panel
        JPanel warmUpControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        warmUpControls.setBorder(BorderFactory.createTitledBorder("Vizuálny odhad (Eyeballing)"));

        tfEyeballingStep = new JTextField("3600", 5); // Zber dát každú hodinu
        tfEyeballingReps = new JTextField("10", 4);   // Viac replikácií pre hladší priemer

        warmUpControls.add(new JLabel("Krok zberu (s):"));
        warmUpControls.add(tfEyeballingStep);
        warmUpControls.add(new JLabel("Replikácie:"));
        warmUpControls.add(tfEyeballingReps);

        JButton btnRunEyeballing = new JButton("Vykresliť graf zahrievania");
        btnRunEyeballing.addActionListener(e -> runEyeballingObservation());
        warmUpControls.add(btnRunEyeballing);

        warmUpPanel.add(warmUpControls, BorderLayout.NORTH);

        // Graf
        eyeballingSeries = new XYSeries("Priemerný počet pacientov v systéme");
        XYSeriesCollection eyeballingDataset = new XYSeriesCollection(eyeballingSeries);
        JFreeChart eyeballingChart = ChartFactory.createXYLineChart(
                "Sledovanie ustáleného stavu", "Simulačný čas (hodiny)", "Počet pacientov (ks)",
                eyeballingDataset, PlotOrientation.VERTICAL, true, true, false
        );

        ChartPanel chartPanel = new ChartPanel(eyeballingChart);
        warmUpPanel.add(chartPanel, BorderLayout.CENTER);

        return warmUpPanel;
    }

    private void runEyeballingObservation() {
        btnStart.setEnabled(false);
        eyeballingSeries.clear();

        int step = Integer.parseInt(tfEyeballingStep.getText());
        int numReps = Integer.parseInt(tfEyeballingReps.getText());

        // Simulujeme 4 týždne (28 dní = 2 419 200 sekúnd)
        double maxTime = 2419200.0;
        int numSteps = (int) (maxTime / step) + 1;

        // Pole na sčítavanie počtu pacientov pre daný časový krok naprieč replikáciami
        double[] eyeballingSums = new double[numSteps];

        // Vytvoríme inštanciu jadra čisto pre zber dát
        MySimulation eyeballingSim = new MySimulation();
        eyeballingSim.setNumDoctors(Integer.parseInt(tfDoctors.getText()));
        eyeballingSim.setNumNurses(Integer.parseInt(tfNurses.getText()));

        // TOTO JE TEN TRIK:
        // Povieme OSPABE: Každých 'step' (3600) sekúnd zavolaj refresh() a zastav sa na smiešnu 0.001 sekundy.
        // Takto jadro beží takmer rýchlosťou turbomódu, ale GUI vie pravidelne a presne zbierať dáta.
        eyeballingSim.setSimSpeed(step, 0.001);

        final int[] lastUpdatedIndex = {-1};

        eyeballingSim.registerDelegate(new ISimDelegate() {
            @Override
            public void refresh(Simulation simulation) {
                MySimulation mySim = (MySimulation) simulation;
                int index = (int) (mySim.currentTime() / step);

                // Zaznamenáme stav LEN AK sme sa posunuli do novej hodiny
                if (index < numSteps && index > lastUpdatedIndex[0]) {
                    eyeballingSums[index] += mySim.getActivePatients().size();
                    lastUpdatedIndex[0] = index; // Zapamätáme si, že túto hodinu sme už vybavili
                }
            }

            @Override
            public void simStateChanged(Simulation simulation, SimState simState) {
                // Na konci replikácie resetujeme počítadlo pre ďalšiu replikáciu
                if (simState == SimState.replicationStopped) {
                    lastUpdatedIndex[0] = -1;
                }

                if (simState == SimState.stopped) {
                    processAndDrawEyeballing(eyeballingSums, numSteps, numReps, step);
                }
            }
        });

        // Spustíme experiment v novom vlákne
        new Thread(() -> {
            eyeballingSim.simulate(numReps, maxTime);
        }).start();
    }

    private void processAndDrawEyeballing(double[] sums, int numSteps, int numReps, int step) {
        int windowSize = 50; // Kĺzavý priemer pre vyhladenie zubov v grafe

        for (int i = 0; i < numSteps; i++) {
            // Najprv urobíme priemer cez všetky zbehnuté replikácie
            sums[i] = sums[i] / numReps;

            // Výpočet kĺzavého priemeru (prevzaté z tvojho starého kódu)
            double smoothedValue;
            if (i >= windowSize - 1) {
                double windowSum = 0;
                for (int j = 0; j < windowSize; j++) {
                    windowSum += sums[i - j];
                }
                smoothedValue = windowSum / windowSize;
            } else {
                double tempSum = 0;
                for (int j = 0; j <= i; j++) {
                    tempSum += sums[j];
                }
                smoothedValue = tempSum / (i + 1);
            }

            // X-os = čas v hodinách, Y-os = vyhladený počet pacientov
            final double finalTime = (i * step) / 3600.0;
            final double finalAvg = smoothedValue;

            SwingUtilities.invokeLater(() -> eyeballingSeries.add(finalTime, finalAvg));
        }

        SwingUtilities.invokeLater(() -> {
            btnStart.setEnabled(true);
            JOptionPane.showMessageDialog(this, "Graf zahrievania bol úspešne vygenerovaný!");
        });
    }

    private void updateGlobalStatsGUI() {
        // Tu ťaháme údaje z GLOBÁLNYCH štatistík z MySimulation
        double waitAmbMean = sim.getGlobalWaitAmbStat().getMean();
        double waitAmbLow = sim.getGlobalWaitAmbStat().getConfidenceIntervalLower();
        double waitAmbHigh = sim.getGlobalWaitAmbStat().getConfidenceIntervalUpper();
        lblGlobAvgWaitAmbulance.setText(String.format("Wait (Amb): %.1fs <%.1f, %.1f>", waitAmbMean, waitAmbLow, waitAmbHigh));

        double waitWalkInMean = sim.getGlobalWaitWalkInStat().getMean();
        double waitWalkInLow = sim.getGlobalWaitWalkInStat().getConfidenceIntervalLower();
        double waitWalkInHigh = sim.getGlobalWaitWalkInStat().getConfidenceIntervalUpper();
        lblGlobAvgWaitWalkIn.setText(String.format("Wait (Walk-in): %.1fs <%.1f, %.1f>", waitWalkInMean, waitWalkInLow, waitWalkInHigh));

        double utilNursesMean = sim.getGlobalUtilNursesStat().getMean();
        double utilNursesPct = (sim.getNumNurses() > 0) ? (utilNursesMean / sim.getNumNurses()) * 100 : 0;
        lblGlobUtilNurses.setText(String.format("Nurses Util.: %.2f%% (%.1f/%d)", utilNursesPct, utilNursesMean, sim.getNumNurses()));

        double utilDoctorsMean = sim.getGlobalUtilDoctorsStat().getMean();
        double utilDoctorsPct = (sim.getNumDoctors() > 0) ? (utilDoctorsMean / sim.getNumDoctors()) * 100 : 0;
        lblGlobUtilDoctors.setText(String.format("Doctors Util.: %.2f%% (%.1f/%d)", utilDoctorsPct, utilDoctorsMean, sim.getNumDoctors()));
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

        JPanel globPanel = new JPanel(new GridLayout(5, 1)); // Zmena na 5 riadkov
        globPanel.setBorder(BorderFactory.createTitledBorder("Averages (Statistics)"));

        lblGlobReplications = new JLabel("Replications: 0");
        lblGlobAvgWaitAmbulance = new JLabel("Wait (Ambulance): 0.0s");
        lblGlobAvgWaitWalkIn = new JLabel("Wait (Walk-in): 0.0s");
        lblGlobUtilNurses = new JLabel("Nurses Util.: 0.0%");
        lblGlobUtilDoctors = new JLabel("Doctors Util.: 0.0%");

        globPanel.add(lblGlobReplications);
        globPanel.add(lblGlobAvgWaitAmbulance);
        globPanel.add(lblGlobAvgWaitWalkIn);
        globPanel.add(lblGlobUtilNurses);
        globPanel.add(lblGlobUtilDoctors);

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
                    updateGlobalStatsGUI(); // Aktualizácia v turbomóde po každej replikácii
                    break;
                case stopped:
                    btnStart.setText("Spustiť");
                    updateGlobalStatsGUI(); // Finálna aktualizácia po skončení všetkých replikácií
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

            MySimulation mySim = (MySimulation) smltn;

            // --- OPRAVENÉ VOLANIA ŠTATISTÍK (cez agentZdrojov) ---

            // 1. Čakanie - Sanitky (Aktuálna replikácia)
            double waitAmbMean = mySim.agentZdrojov().getWaitingTimeAmbulanceStat().getMean();
            double waitAmbLow = mySim.agentZdrojov().getWaitingTimeAmbulanceStat().getConfidenceIntervalLower();
            double waitAmbHigh = mySim.agentZdrojov().getWaitingTimeAmbulanceStat().getConfidenceIntervalUpper();
            lblGlobAvgWaitAmbulance.setText(String.format("Wait (Amb): %.1fs <%.1f, %.1f>", waitAmbMean, waitAmbLow, waitAmbHigh));

            // 2. Čakanie - Peší (Aktuálna replikácia)
            double waitWalkInMean = mySim.agentZdrojov().getWaitingTimeWalkInStat().getMean();
            double waitWalkInLow = mySim.agentZdrojov().getWaitingTimeWalkInStat().getConfidenceIntervalLower();
            double waitWalkInHigh = mySim.agentZdrojov().getWaitingTimeWalkInStat().getConfidenceIntervalUpper();
            lblGlobAvgWaitWalkIn.setText(String.format("Wait (Walk-in): %.1fs <%.1f, %.1f>", waitWalkInMean, waitWalkInLow, waitWalkInHigh));

            // 3. Vyťaženie - Sestry (Aktuálna replikácia)
            double totalNurses = mySim.getNumNurses();
            double utilNursesMean = mySim.agentZdrojov().getNurseUtilizationStat().getMean();
            double utilNursesPct = (totalNurses > 0) ? (utilNursesMean / totalNurses) * 100 : 0;
            lblGlobUtilNurses.setText(String.format("Nurses Util.: %.2f%% (%.1f/%d)", utilNursesPct, utilNursesMean, (int)totalNurses));

            // 4. Vyťaženie - Lekári (Aktuálna replikácia)
            double totalDoctors = mySim.getNumDoctors();
            double utilDoctorsMean = mySim.agentZdrojov().getDoctorUtilizationStat().getMean();
            double utilDoctorsPct = (totalDoctors > 0) ? (utilDoctorsMean / totalDoctors) * 100 : 0;
            lblGlobUtilDoctors.setText(String.format("Doctors Util.: %.2f%% (%.1f/%d)", utilDoctorsPct, utilDoctorsMean, (int)totalDoctors));

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