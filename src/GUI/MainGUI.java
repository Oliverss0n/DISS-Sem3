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


    //private double WARMUP = 86400.0;
    // --- Simulačné jadro a vlákno ---
    private MySimulation sim;
    private Thread simThread;

    //private final double SIM_TIME = 2419200.0;
    //private final double SIM_TIME = 2419200.0;

    // --- GUI Komponenty ---
    private JSlider speedSlider;
    private JButton btnStart, btnPause;
    private JCheckBox cbVisualMode;
    private JLabel lblSimTime, lblSpeedDisplay;
    private JTextField tfReplications, tfDoctors, tfNurses;

    // --- Štatistiky ---
    private JLabel lblCurQueueEntrance, lblCurQueueExam, lblCurFreeDoctors, lblCurFreeNurses;
    // ZMENENÉ NÁZVY A PRIDANÉ NOVÉ LABELy PRE VYŤAŽENIE:
    private JLabel lblGlobReplications, lblGlobAvgWaitAmbulance, lblGlobAvgWaitWalkIn, lblGlobUtilNurses, lblGlobUtilDoctors;
    private JLabel lblGlobUtilRoomA, lblGlobUtilRoomB;
    private JLabel lblGlobTotalPatients, lblGlobTotalWalkIn, lblGlobTotalAmb;
    private JLabel lblGlobEntryWaitAmb, lblGlobEntryWaitWalk, lblGlobEntryQueue;

    private JLabel lblGlobTreatWaitAmb, lblGlobTreatWaitWalk, lblGlobTreatQueue;

    // --- Tabuľka ---
    private DefaultTableModel tmPatients;
    private JTable tblPatients;
    private JLabel lblGlobTimeInSysAmb, lblGlobTimeInSysWalkIn;



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

    // --- PREMENNÉ PRE ANALÝZU CITLIVOSTI ---
    private XYSeries seriesWaitAmb, seriesWaitWalkIn;

    // Horný panel (Rýchly graf)
    private JTextField tfGraphStart, tfGraphEnd, tfGraphFixed, tfGraphReps;
    private JComboBox<String> cbGraphTyp;
    private JButton btnDrawGraph;

    // Dolný panel (Hromadný CSV export)
    private JTextField tfExpDocStart, tfExpDocEnd, tfExpNursStart, tfExpNursEnd, tfExpReps;
    private JButton btnExperiment;

    private JComboBox<String> cbVariantSelect;

    // --- Graf ustaľovania ---
    private XYSeries seriesUstalovanieAmb;
    private XYSeries seriesUstalovaniePesi;

    private JTextField tfSettlingReps;
    private JButton btnDrawSettling;

    // --- LOKÁLNE ŠTATISTIKY (Nové premenné) ---
    // Časy
    private JLabel lblLocTimeInSysAmb, lblLocTimeInSysWalkIn;
    private JLabel lblLocAvgWaitAmb, lblLocAvgWaitWalkIn;
    private JLabel lblLocEntryWaitAmb, lblLocEntryWaitWalk;
    private JLabel lblLocTreatWaitAmb, lblLocTreatWaitWalk;
    // Rady
    private JLabel lblLocEntryQueue, lblLocTreatQueue;
    // Vyťaženosť
    private JLabel lblLocUtilNurses, lblLocUtilDoctors;
    private JLabel lblLocUtilRoomA, lblLocUtilRoomB;
    // Počty
    private JLabel lblLocTotalPatients, lblLocTotalWalkIn, lblLocTotalAmb;

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
        tabbedPane.addTab("Analýza Citlivosti", buildSensitivityPanel());
        tabbedPane.addTab("Graf Ustaľovania", buildSettlingPanel());

        add(tabbedPane);
        setVisible(true);
    }

    private JPanel buildControlPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        p.setBorder(BorderFactory.createTitledBorder("Ovládanie"));

        tfReplications = new JTextField("10", 5);
        tfDoctors = new JTextField("6", 3);
        tfNurses = new JTextField("8", 3);

        cbVisualMode = new JCheckBox("Vizuálny režim", true);
        cbAnimation = new JCheckBox("Animácia", false);

        String[] varianty = {"0 - Základný model", "1 - Ochrana posl. lekára", "2 - Dedikovaná sestra"};
        cbVariantSelect = new JComboBox<>(varianty);
        p.add(new JLabel("Variant:"));
        p.add(cbVariantSelect);

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
        JPanel warmUpControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        warmUpControls.setBorder(BorderFactory.createTitledBorder("Vizuálny odhad (Eyeballing)"));

        tfEyeballingStep = new JTextField("3600", 5);
        tfEyeballingReps = new JTextField("10", 4);

        warmUpControls.add(new JLabel("Krok zberu (s):"));
        warmUpControls.add(tfEyeballingStep);
        warmUpControls.add(new JLabel("Replikácie:"));
        warmUpControls.add(tfEyeballingReps);

        JButton btnRunEyeballing = new JButton("Vykresliť graf zahrievania");
        btnRunEyeballing.addActionListener(e -> runEyeballingObservation());
        warmUpControls.add(btnRunEyeballing);
        warmUpPanel.add(warmUpControls, BorderLayout.NORTH);

        // ZMENA TU: Názvy série a osí
        eyeballingSeries = new XYSeries("Priemerný počet osôb v radoch");
        XYSeriesCollection eyeballingDataset = new XYSeriesCollection(eyeballingSeries);
        JFreeChart eyeballingChart = ChartFactory.createXYLineChart(
                "Sledovanie ustáleného stavu (Zahrievanie)",
                "Simulačný čas (hodiny)",
                "Priemerný počet osôb v radoch", // <--- Os Y
                eyeballingDataset, PlotOrientation.VERTICAL, true, true, false
        );

        ChartPanel chartPanel = new ChartPanel(eyeballingChart);
        warmUpPanel.add(chartPanel, BorderLayout.CENTER);
        return warmUpPanel;
    }

    //vygenerovane pomocou AI pomocou inspiracie z predoslej semestralky
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
        eyeballingSim.setWarmUpTime(0);
        eyeballingSim.setNumDoctors(Integer.parseInt(tfDoctors.getText()));
        eyeballingSim.setNumNurses(Integer.parseInt(tfNurses.getText()));

        eyeballingSim.setVariant(cbVariantSelect.getSelectedIndex());
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
        int windowSize = 100; // Kĺzavý priemer pre vyhladenie zubov v grafe

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

    private JPanel buildSensitivityPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        String[] moznosti = {"Meniť počet LEKÁROV (Sestry pevné)", "Meniť počet SESTIER (Lekári pevní)"};

        // --- HORNÝ PANEL (Rýchly graf) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Nastavenia grafu (rýchly náhľad)"));

        cbGraphTyp = new JComboBox<>(moznosti);
        tfGraphStart = new JTextField("3", 3);
        tfGraphEnd = new JTextField("12", 3);
        tfGraphFixed = new JTextField("5", 3);
        tfGraphReps = new JTextField("10", 4);
        btnDrawGraph = new JButton("Vykresliť graf");

        topPanel.add(new JLabel("Režim:")); topPanel.add(cbGraphTyp);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new JLabel("Od:")); topPanel.add(tfGraphStart);
        topPanel.add(new JLabel("Do:")); topPanel.add(tfGraphEnd);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new JLabel("Pevná hodnota:")); topPanel.add(tfGraphFixed);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new JLabel("Replikácie na bod:")); topPanel.add(tfGraphReps);
        topPanel.add(Box.createHorizontalStrut(15));
        topPanel.add(btnDrawGraph);

        btnDrawGraph.addActionListener(e -> runGraphExperiment());

        // --- SPODNÝ PANEL (Hromadný export) ---
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        exportPanel.setBorder(BorderFactory.createTitledBorder("Hromadný export (Detailná analýza do CSV)"));

        tfExpDocStart = new JTextField("2", 3);
        tfExpDocEnd = new JTextField("6", 3);
        tfExpNursStart = new JTextField("3", 3);
        tfExpNursEnd = new JTextField("8", 3);
        tfExpReps = new JTextField("50", 4); // Odporúčam začať s 50 rep. pre rýchlejší test
        btnExperiment = new JButton("Spustiť Grid Search (CSV)");
        btnExperiment.setBackground(new Color(230, 240, 255));

        exportPanel.add(new JLabel("Lekári Od:")); exportPanel.add(tfExpDocStart);
        exportPanel.add(new JLabel("Do:")); exportPanel.add(tfExpDocEnd);
        exportPanel.add(Box.createHorizontalStrut(10));
        exportPanel.add(new JLabel("Sestry Od:")); exportPanel.add(tfExpNursStart);
        exportPanel.add(new JLabel("Do:")); exportPanel.add(tfExpNursEnd);
        exportPanel.add(Box.createHorizontalStrut(10));
        exportPanel.add(new JLabel("Replikácie:")); exportPanel.add(tfExpReps);
        exportPanel.add(Box.createHorizontalStrut(15));
        exportPanel.add(btnExperiment);

        btnExperiment.addActionListener(e -> runExportExperiment());

        // --- ZLOŽENIE OVLÁDACÍCH PANELOV ---
        JPanel combinedTop = new JPanel(new GridLayout(2, 1));
        combinedTop.add(topPanel);
        combinedTop.add(exportPanel);
        panel.add(combinedTop, BorderLayout.NORTH);

        // --- GRAF ---
        seriesWaitAmb = new XYSeries("Priemerný čas - Sanitky (min)");
        seriesWaitWalkIn = new XYSeries("Priemerný čas - Peší (min)");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(seriesWaitAmb);
        dataset.addSeries(seriesWaitWalkIn);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Závislosť času čakania od počtu personálu",
                "Počet zamestnancov (Menený parameter)",
                "Priemerný čas čakania (Minúty)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private void runGraphExperiment() {
        btnDrawGraph.setEnabled(false);
        btnExperiment.setEnabled(false);
        btnStart.setEnabled(false);
        seriesWaitAmb.clear();
        seriesWaitWalkIn.clear();

        int startVal = Integer.parseInt(tfGraphStart.getText());
        int endVal = Integer.parseInt(tfGraphEnd.getText());
        int fixedVal = Integer.parseInt(tfGraphFixed.getText());
        int reps = Integer.parseInt(tfGraphReps.getText());
        boolean menimeLekarov = cbGraphTyp.getSelectedIndex() == 0;

        new Thread(() -> {
            for (int i = startVal; i <= endVal; i++) {
                MySimulation expSim = new MySimulation();
                expSim.setVariant(cbVariantSelect.getSelectedIndex());
                if (menimeLekarov) {
                    expSim.setNumDoctors(i);
                    expSim.setNumNurses(fixedVal);
                } else {
                    expSim.setNumDoctors(fixedVal);
                    expSim.setNumNurses(i);
                }
                //expSim.setWarmUpTime(WARM_UP);
                expSim.setSimSpeed(0, 0);
                expSim.setMaxSimSpeed();

                expSim.simulate(reps, sim.getSimTime());

                double avgAmbMins = expSim.getGlobalWaitAmbStat().getMean() / 60.0;
                double avgWalkMins = expSim.getGlobalWaitWalkInStat().getMean() / 60.0;

                final int x = i;
                SwingUtilities.invokeLater(() -> {
                    seriesWaitAmb.add(x, avgAmbMins);
                    seriesWaitWalkIn.add(x, avgWalkMins);
                });
            }
            SwingUtilities.invokeLater(() -> {
                btnDrawGraph.setEnabled(true);
                btnExperiment.setEnabled(true);
                btnStart.setEnabled(true);
            });
        }).start();
    }

    private void runExportExperiment() {
        btnDrawGraph.setEnabled(false);
        btnExperiment.setEnabled(false);
        btnStart.setEnabled(false);
        seriesWaitAmb.clear();
        seriesWaitWalkIn.clear();

        int docStart = Integer.parseInt(tfExpDocStart.getText());
        int docEnd = Integer.parseInt(tfExpDocEnd.getText());
        int nursStart = Integer.parseInt(tfExpNursStart.getText());
        int nursEnd = Integer.parseInt(tfExpNursEnd.getText());
        int reps = Integer.parseInt(tfExpReps.getText());

        new Thread(() -> {
            String filename = "grid_search_vysledky.csv";

            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename))) {

                // Hlavička CSV s novým stĺpcom pre celkový súčet personálu
                writer.println("Lekari;Sestry;Personal Spolu;Cakanie Sanitky (min);Cakanie Pesi (min);Vytazenie Lekari (%);Vytazenie Sestry (%);Splna zadanie?");

                int minSpolu = Integer.MAX_VALUE;
                int bestDocs = -1;
                int bestNurs = -1;

                // 2 CYKLY: Prechádzame všetky kombinácie lekárov a sestier
                for (int d = docStart; d <= docEnd; d++) {
                    for (int s = nursStart; s <= nursEnd; s++) {

                        MySimulation expSim = new MySimulation();
                        expSim.setVariant(cbVariantSelect.getSelectedIndex());
                        expSim.setNumDoctors(d);
                        expSim.setNumNurses(s);
                        //expSim.setWarmUpTime(WARMUP);
                        expSim.setSimSpeed(0, 0);
                        expSim.setMaxSimSpeed();

                        expSim.simulate(reps, sim.getSimTime()); // 28 dní

                        double avgAmbMins = expSim.getGlobalWaitAmbStat().getMean() / 60.0;
                        double avgWalkMins = expSim.getGlobalWaitWalkInStat().getMean() / 60.0;
                        double utilDoc = (expSim.getNumDoctors() > 0) ? (expSim.getGlobalUtilDoctorsStat().getMean() / expSim.getNumDoctors()) * 100 : 0;
                        double utilNur = (expSim.getNumNurses() > 0) ? (expSim.getGlobalUtilNursesStat().getMean() / expSim.getNumNurses()) * 100 : 0;

                        boolean splnaPodmienku = (avgAmbMins <= 15.0) && (avgWalkMins < 30.0);
                        String vysledokPodmienky = splnaPodmienku ? "PRAVDA" : "NEPRAVDA";
                        int personalSpolu = d + s;

                        // Ak splní podmienku a použili sme menej personálu ako doteraz, zapamätáme si to
                        if (splnaPodmienku && personalSpolu < minSpolu) {
                            minSpolu = personalSpolu;
                            bestDocs = d;
                            bestNurs = s;
                        }

                        // Zápis do CSV
                        String csvLine = String.format(java.util.Locale.US, "%d;%d;%d;%.2f;%.2f;%.2f;%.2f;%s",
                                d, s, personalSpolu, avgAmbMins, avgWalkMins, utilDoc, utilNur, vysledokPodmienky).replace('.', ',');

                        writer.println(csvLine);

                        // Nepovinné: Aktualizuj graf čisto vizuálne pre nejaký progres (len súčet personálu)
                        final int sum = personalSpolu;
                        SwingUtilities.invokeLater(() -> {
                            seriesWaitAmb.addOrUpdate((Number) sum, avgAmbMins);
                            seriesWaitWalkIn.addOrUpdate((Number) sum, avgWalkMins);
                        });
                    }
                }

                // Vytvorenie finálnej hlášky podľa toho, či sa našlo riešenie
                String finalMsg;
                if (bestDocs != -1) {
                    finalMsg = "Grid Search bol dokončený!\n\n" +
                            "Najlepšia zistená konfigurácia:\n" +
                            "Lekári: " + bestDocs + "\n" +
                            "Sestry: " + bestNurs + "\n\n" +
                            "Všetky dáta nájdete v súbore: " + filename;
                } else {
                    finalMsg = "Grid Search dokončený!\n\nŽiadna z testovaných kombinácií NESPLNILA podmienky.\nSkúste zvýšiť počty personálu.";
                }

                SwingUtilities.invokeLater(() -> {
                    btnDrawGraph.setEnabled(true);
                    btnExperiment.setEnabled(true);
                    btnStart.setEnabled(true);
                    JOptionPane.showMessageDialog(MainGUI.this, finalMsg);
                });
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateGlobalStatsGUI() {
        if (sim == null) return;

        // Update počítadla replikácií
        lblGlobReplications.setText("Replications: " + sim.currentReplication());

        // --- 1. BLOK: ČASY (Všetko v minútach s IS) ---
        lblGlobTimeInSysAmb.setText(String.format("Cas v systeme (Sanitky): %.2f min <%.2f, %.2f>", sim.getGlobalTimeInSystemAmbStat().getMean() / 60.0, sim.getGlobalTimeInSystemAmbStat().getConfidenceIntervalLower() / 60.0, sim.getGlobalTimeInSystemAmbStat().getConfidenceIntervalUpper() / 60.0));
        lblGlobTimeInSysWalkIn.setText(String.format("Cas v systeme (Peší): %.2f min <%.2f, %.2f>", sim.getGlobalTimeInSystemWalkInStat().getMean() / 60.0, sim.getGlobalTimeInSystemWalkInStat().getConfidenceIntervalLower() / 60.0, sim.getGlobalTimeInSystemWalkInStat().getConfidenceIntervalUpper() / 60.0));

        lblGlobAvgWaitAmbulance.setText(String.format("Celkove cakanie (Sanitky): %.2f min <%.2f, %.2f>", sim.getGlobalWaitAmbStat().getMean() / 60.0, sim.getGlobalWaitAmbStat().getConfidenceIntervalLower() / 60.0, sim.getGlobalWaitAmbStat().getConfidenceIntervalUpper() / 60.0));
        lblGlobAvgWaitWalkIn.setText(String.format("Celkove cakanie (Peší): %.2f min <%.2f, %.2f>", sim.getGlobalWaitWalkInStat().getMean() / 60.0, sim.getGlobalWaitWalkInStat().getConfidenceIntervalLower() / 60.0, sim.getGlobalWaitWalkInStat().getConfidenceIntervalUpper() / 60.0));

        lblGlobEntryWaitAmb.setText(String.format("Cakanie VSTUP. (Sanitky): %.2f min <%.2f, %.2f>", sim.getGlobalEntryWaitAmbStat().getMean() / 60.0, sim.getGlobalEntryWaitAmbStat().getConfidenceIntervalLower() / 60.0, sim.getGlobalEntryWaitAmbStat().getConfidenceIntervalUpper() / 60.0));
        lblGlobEntryWaitWalk.setText(String.format("Cakanie VSTUP. (Peší): %.2f min <%.2f, %.2f>", sim.getGlobalEntryWaitWalkInStat().getMean() / 60.0, sim.getGlobalEntryWaitWalkInStat().getConfidenceIntervalLower() / 60.0, sim.getGlobalEntryWaitWalkInStat().getConfidenceIntervalUpper() / 60.0));

        lblGlobTreatWaitAmb.setText(String.format("Cakanie OŠETRENIE (Sanitky): %.2f min <%.2f, %.2f>", sim.getGlobalTreatmentWaitAmbStat().getMean() / 60.0, sim.getGlobalTreatmentWaitAmbStat().getConfidenceIntervalLower() / 60.0, sim.getGlobalTreatmentWaitAmbStat().getConfidenceIntervalUpper() / 60.0));
        lblGlobTreatWaitWalk.setText(String.format("Cakanie OŠETRENIE (Peší): %.2f min <%.2f, %.2f>", sim.getGlobalTreatmentWaitWalkInStat().getMean() / 60.0, sim.getGlobalTreatmentWaitWalkInStat().getConfidenceIntervalLower() / 60.0, sim.getGlobalTreatmentWaitWalkInStat().getConfidenceIntervalUpper() / 60.0));

        // --- 2. BLOK: RADY (v osobách s IS) ---
        lblGlobEntryQueue.setText(String.format("Rad Vstup (osoby): %.2f <%.2f, %.2f>", sim.getGlobalEntryQueueLengthStat().getMean(), sim.getGlobalEntryQueueLengthStat().getConfidenceIntervalLower(), sim.getGlobalEntryQueueLengthStat().getConfidenceIntervalUpper()));
        lblGlobTreatQueue.setText(String.format("Rad Ošetrenie (osoby): %.2f <%.2f, %.2f>", sim.getGlobalTreatmentQueueLengthStat().getMean(), sim.getGlobalTreatmentQueueLengthStat().getConfidenceIntervalLower(), sim.getGlobalTreatmentQueueLengthStat().getConfidenceIntervalUpper()));

        // --- 3. BLOK: VYŤAŽENOSŤ (% s IS) ---
        // --- 3. BLOK: VYŤAŽENOSŤ (% s IS) ---
        double totalNurses = sim.getNumNurses();
        double uNur = sim.getGlobalUtilNursesStat().getMean();
        double uNurLow = sim.getGlobalUtilNursesStat().getConfidenceIntervalLower();
        double uNurHigh = sim.getGlobalUtilNursesStat().getConfidenceIntervalUpper();
        lblGlobUtilNurses.setText(String.format("Sestry: %.2f%% <%.2f%%, %.2f%%>",
                (totalNurses > 0) ? (uNur / totalNurses) * 100 : 0,
                (totalNurses > 0) ? (uNurLow / totalNurses) * 100 : 0,
                (totalNurses > 0) ? (uNurHigh / totalNurses) * 100 : 0));

        double totalDocs = sim.getNumDoctors();
        double uDoc = sim.getGlobalUtilDoctorsStat().getMean();
        double uDocLow = sim.getGlobalUtilDoctorsStat().getConfidenceIntervalLower();
        double uDocHigh = sim.getGlobalUtilDoctorsStat().getConfidenceIntervalUpper();
        lblGlobUtilDoctors.setText(String.format("Lekári: %.2f%% <%.2f%%, %.2f%%>",
                (totalDocs > 0) ? (uDoc / totalDocs) * 100 : 0,
                (totalDocs > 0) ? (uDocLow / totalDocs) * 100 : 0,
                (totalDocs > 0) ? (uDocHigh / totalDocs) * 100 : 0));

        double uRoomALow = sim.getGlobalRoomAUtilStat().getConfidenceIntervalLower();
        double uRoomAHigh = sim.getGlobalRoomAUtilStat().getConfidenceIntervalUpper();
        lblGlobUtilRoomA.setText(String.format("Amb A: %.2f%% <%.2f%%, %.2f%%>",
                (sim.getGlobalRoomAUtilStat().getMean() / 5.0) * 100,
                (uRoomALow / 5.0) * 100,
                (uRoomAHigh / 5.0) * 100));

        double uRoomBLow = sim.getGlobalRoomBUtilStat().getConfidenceIntervalLower();
        double uRoomBHigh = sim.getGlobalRoomBUtilStat().getConfidenceIntervalUpper();
        lblGlobUtilRoomB.setText(String.format("Amb B: %.2f%% <%.2f%%, %.2f%%>",
                (sim.getGlobalRoomBUtilStat().getMean() / 7.0) * 100,
                (uRoomBLow / 7.0) * 100,
                (uRoomBHigh / 7.0) * 100));
        // --- 4. BLOK: PRIECHODNOSŤ (v osobách s IS) ---
        lblGlobTotalPatients.setText(String.format("Vybavení pacienti: %.1f <%.1f, %.1f>", sim.getGlobalTotalPatientsStat().getMean(), sim.getGlobalTotalPatientsStat().getConfidenceIntervalLower(), sim.getGlobalTotalPatientsStat().getConfidenceIntervalUpper()));
        lblGlobTotalWalkIn.setText(String.format(" - Z toho Peší: %.1f <%.1f, %.1f>", sim.getGlobalTotalWalkInStat().getMean(), sim.getGlobalTotalWalkInStat().getConfidenceIntervalLower(), sim.getGlobalTotalWalkInStat().getConfidenceIntervalUpper()));
        lblGlobTotalAmb.setText(String.format(" - Z toho Sanitky: %.1f <%.1f, %.1f>", sim.getGlobalTotalAmbStat().getMean(), sim.getGlobalTotalAmbStat().getConfidenceIntervalLower(), sim.getGlobalTotalAmbStat().getConfidenceIntervalUpper()));
    }

    private JPanel buildStatsPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 10));

        // ==========================================
        // ĽAVÁ STRANA - OKAMŽITÝ STAV A LOKÁLNE ŠTAT.
        // ==========================================
        JPanel leftSide = new JPanel(new BorderLayout(5, 5));

        // 1. Okamžitý stav (Práve teraz)
        JPanel curStatusPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        curStatusPanel.setBorder(BorderFactory.createTitledBorder("Okamžitý stav (Práve teraz)"));

        lblCurQueueEntrance = new JLabel("Rad na vstup: 0");
        lblCurQueueExam = new JLabel("Rad na ošetrenie: 0");
        lblCurFreeDoctors = new JLabel("Voľní lekári: 0");
        lblCurFreeNurses = new JLabel("Voľné sestry: 0");
        lblAmbAStatus = new JLabel("Amb A: ");
        lblAmbBStatus = new JLabel("Amb B: ");

        curStatusPanel.add(lblCurQueueEntrance); curStatusPanel.add(lblCurQueueExam);
        curStatusPanel.add(lblCurFreeDoctors); curStatusPanel.add(lblCurFreeNurses);
        curStatusPanel.add(lblAmbAStatus); curStatusPanel.add(lblAmbBStatus);

        // 2. Lokálne štatistiky (4 bloky)
        JPanel mainLocPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        mainLocPanel.setBorder(BorderFactory.createTitledBorder("Lokálne štatistiky (Aktuálna replikácia)"));

        // Blok 1: Časy
        JPanel pnlLocTimes = new JPanel(new GridLayout(0, 1));
        pnlLocTimes.setBorder(BorderFactory.createTitledBorder("Časy (minúty)"));
        lblLocTimeInSysAmb = new JLabel("Systém (Sanitky): -");
        lblLocTimeInSysWalkIn = new JLabel("Systém (Peší): -");
        lblLocAvgWaitAmb = new JLabel("Celk. čak. (Sanitky): -");
        lblLocAvgWaitWalkIn = new JLabel("Celk. čak. (Peší): -");
        lblLocEntryWaitAmb = new JLabel("Čak. VSTUP (Sanitky): -");
        lblLocEntryWaitWalk = new JLabel("Čak. VSTUP (Peší): -");
        lblLocTreatWaitAmb = new JLabel("Čak. OŠETRENIE (Sanitky): -");
        lblLocTreatWaitWalk = new JLabel("Čak. OŠETRENIE (Peší): -");
        pnlLocTimes.add(lblLocTimeInSysAmb); pnlLocTimes.add(lblLocTimeInSysWalkIn);
        pnlLocTimes.add(lblLocAvgWaitAmb); pnlLocTimes.add(lblLocAvgWaitWalkIn);
        pnlLocTimes.add(lblLocEntryWaitAmb); pnlLocTimes.add(lblLocEntryWaitWalk);
        pnlLocTimes.add(lblLocTreatWaitAmb); pnlLocTimes.add(lblLocTreatWaitWalk);

        // Blok 2: Rady
        JPanel pnlLocQueues = new JPanel(new GridLayout(0, 1));
        pnlLocQueues.setBorder(BorderFactory.createTitledBorder("Rady (Priemerná dĺžka)"));
        lblLocEntryQueue = new JLabel("Rad na Vstup: -");
        lblLocTreatQueue = new JLabel("Rad na Ošetrenie: -");
        pnlLocQueues.add(lblLocEntryQueue); pnlLocQueues.add(lblLocTreatQueue);

        // Blok 3: Vyťaženosť
        JPanel pnlLocUtils = new JPanel(new GridLayout(0, 1));
        pnlLocUtils.setBorder(BorderFactory.createTitledBorder("Vyťaženosť (%)"));
        lblLocUtilNurses = new JLabel("Sestry: -");
        lblLocUtilDoctors = new JLabel("Lekári: -");
        lblLocUtilRoomA = new JLabel("Amb A (Ošetrenie): -");
        lblLocUtilRoomB = new JLabel("Amb B (Vstup+Ošetr.): -");
        pnlLocUtils.add(lblLocUtilNurses); pnlLocUtils.add(lblLocUtilDoctors);
        pnlLocUtils.add(lblLocUtilRoomA); pnlLocUtils.add(lblLocUtilRoomB);

        // Blok 4: Priechodnosť
        JPanel pnlLocCounts = new JPanel(new GridLayout(0, 1));
        pnlLocCounts.setBorder(BorderFactory.createTitledBorder("Vybavení pacienti"));
        lblLocTotalPatients = new JLabel("Spolu: -");
        lblLocTotalWalkIn = new JLabel(" - Peší: -");
        lblLocTotalAmb = new JLabel(" - Sanitky: -");
        pnlLocCounts.add(lblLocTotalPatients); pnlLocCounts.add(lblLocTotalWalkIn); pnlLocCounts.add(lblLocTotalAmb);

        mainLocPanel.add(pnlLocTimes);
        mainLocPanel.add(pnlLocQueues);
        mainLocPanel.add(pnlLocUtils);
        mainLocPanel.add(pnlLocCounts);

        leftSide.add(curStatusPanel, BorderLayout.NORTH);
        leftSide.add(mainLocPanel, BorderLayout.CENTER);

        // ==========================================
        // PRAVÁ STRANA - GLOBÁLNE ŠTATISTIKY
        // ==========================================
        JPanel mainGlobPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        mainGlobPanel.setBorder(BorderFactory.createTitledBorder("Globálne štatistiky"));

        // BLOK 1: ČASY
        JPanel pnlTimes = new JPanel(new GridLayout(0, 1));
        pnlTimes.setBorder(BorderFactory.createTitledBorder("Časy (minúty)"));
        lblGlobTimeInSysAmb = new JLabel("Systém (Sanitky): -");
        lblGlobTimeInSysWalkIn = new JLabel("Systém (Peší): -");
        lblGlobAvgWaitAmbulance = new JLabel("Celk. čak. (Sanitky): -");
        lblGlobAvgWaitWalkIn = new JLabel("Celk. čak. (Peší): -");
        lblGlobEntryWaitAmb = new JLabel("Čak. VSTUP (Sanitky): -");
        lblGlobEntryWaitWalk = new JLabel("Čak. VSTUP (Peší): -");
        lblGlobTreatWaitAmb = new JLabel("Čak. OŠETRENIE (Sanitky): -");
        lblGlobTreatWaitWalk = new JLabel("Čak. OŠETRENIE (Peší): -");
        pnlTimes.add(lblGlobTimeInSysAmb); pnlTimes.add(lblGlobTimeInSysWalkIn);
        pnlTimes.add(lblGlobAvgWaitAmbulance); pnlTimes.add(lblGlobAvgWaitWalkIn);
        pnlTimes.add(lblGlobEntryWaitAmb); pnlTimes.add(lblGlobEntryWaitWalk);
        pnlTimes.add(lblGlobTreatWaitAmb); pnlTimes.add(lblGlobTreatWaitWalk);

        // BLOK 2: RADY + INFO
        JPanel pnlQueues = new JPanel(new GridLayout(0, 1));
        pnlQueues.setBorder(BorderFactory.createTitledBorder("Rady a Info"));
        lblGlobReplications = new JLabel("Replikácie: 0");
        lblGlobEntryQueue = new JLabel("Rad na Vstup: -");
        lblGlobTreatQueue = new JLabel("Rad na Ošetrenie: -");
        pnlQueues.add(lblGlobReplications);
        pnlQueues.add(lblGlobEntryQueue);
        pnlQueues.add(lblGlobTreatQueue);

        // BLOK 3: VYŤAŽENOSŤ
        JPanel pnlUtils = new JPanel(new GridLayout(0, 1));
        pnlUtils.setBorder(BorderFactory.createTitledBorder("Vyťaženosť (%)"));
        lblGlobUtilNurses = new JLabel("Sestry: -");
        lblGlobUtilDoctors = new JLabel("Lekári: -");
        lblGlobUtilRoomA = new JLabel("Amb A (Ošetrenie): -");
        lblGlobUtilRoomB = new JLabel("Amb B (Vstup+Ošetr.): -");
        pnlUtils.add(lblGlobUtilNurses); pnlUtils.add(lblGlobUtilDoctors);
        pnlUtils.add(lblGlobUtilRoomA); pnlUtils.add(lblGlobUtilRoomB);

        // BLOK 4: PRIECHODNOSŤ
        JPanel pnlCounts = new JPanel(new GridLayout(0, 1));
        pnlCounts.setBorder(BorderFactory.createTitledBorder("Vybavení pacienti"));
        lblGlobTotalPatients = new JLabel("Spolu: -");
        lblGlobTotalWalkIn = new JLabel(" - Peší: -");
        lblGlobTotalAmb = new JLabel(" - Sanitky: -");
        pnlCounts.add(lblGlobTotalPatients); pnlCounts.add(lblGlobTotalWalkIn); pnlCounts.add(lblGlobTotalAmb);

        mainGlobPanel.add(pnlTimes);
        mainGlobPanel.add(pnlQueues);
        mainGlobPanel.add(pnlUtils);
        mainGlobPanel.add(pnlCounts);

        p.add(leftSide);
        p.add(mainGlobPanel);
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

            // Do runSim():
            sim.setVariant(cbVariantSelect.getSelectedIndex());

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
            if (seriesUstalovanieAmb != null) seriesUstalovanieAmb.clear();
            if (seriesUstalovaniePesi != null) seriesUstalovaniePesi.clear();
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
                        sim.simulate(reps, sim.getSimTime());
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

    private JPanel buildSettlingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- HORNÝ OVLÁDACÍ PANEL ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Nastavenie experimentu"));

        tfSettlingReps = new JTextField("500", 5);
        btnDrawSettling = new JButton("Vykresliť graf ustaľovania");

        topPanel.add(new JLabel("Počet replikácií pre graf:"));
        topPanel.add(tfSettlingReps);
        topPanel.add(Box.createHorizontalStrut(15));
        topPanel.add(btnDrawSettling);

        btnDrawSettling.addActionListener(e -> runSettlingExperiment());
        panel.add(topPanel, BorderLayout.NORTH);

        // --- GRAF ---
        seriesUstalovanieAmb = new XYSeries("Priemerné čakanie - Sanitky (min)");
        seriesUstalovaniePesi = new XYSeries("Priemerné čakanie - Peší (min)");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(seriesUstalovanieAmb);
        dataset.addSeries(seriesUstalovaniePesi);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Ustaľovanie priemerných dôb čakania pri rastúcom počte replikácií",
                "Počet vykonaných replikácií",
                "Priemerná doba čakania (Minúty)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        org.jfree.chart.plot.XYPlot plot = chart.getXYPlot();

        // Os X (Replikácie) - adaptívne škálovanie
        org.jfree.chart.axis.NumberAxis xAxis = (org.jfree.chart.axis.NumberAxis) plot.getDomainAxis();
        xAxis.setAutoRange(true);
        xAxis.setAutoRangeIncludesZero(false); // Nenúti os X začínať od nuly

        // Os Y (Čakanie v minútach) - adaptívne škálovanie s okrajmi
        org.jfree.chart.axis.NumberAxis yAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRange(true);
        yAxis.setAutoRangeIncludesZero(false); // Dovolí osi Y priblížiť sa na reálne hodnoty (napr. 14 až 16 minút)

        // Pridanie 10% okraja hore a dole, aby graf nebol nalepený na hranách
        yAxis.setUpperMargin(0.05);
        yAxis.setLowerMargin(0.05);

        ChartPanel chartPanel = new ChartPanel(chart);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private void runSettlingExperiment() {
        btnDrawSettling.setEnabled(false);
        btnStart.setEnabled(false);
        seriesUstalovanieAmb.clear();
        seriesUstalovaniePesi.clear();

        int reps = Integer.parseInt(tfSettlingReps.getText());
        int docs = Integer.parseInt(tfDoctors.getText());
        int nurses = Integer.parseInt(tfNurses.getText());
        int variant = cbVariantSelect.getSelectedIndex();

        new Thread(() -> {
            MySimulation settleSim = new MySimulation();
            settleSim.setVariant(variant);
            settleSim.setNumDoctors(docs);
            settleSim.setNumNurses(nurses);
            //settleSim.setWarmUpTime(WARMUP); // Ak používaš globálnu konštantu
            settleSim.setSimSpeed(0, 0);
            settleSim.setMaxSimSpeed();

            settleSim.registerDelegate(new ISimDelegate() {
                @Override
                public void simStateChanged(Simulation simulation, SimState simState) {
                    if (simState == SimState.replicationStopped) {
                        MySimulation ms = (MySimulation) simulation;
                        int repNum = ms.currentReplication(); // BEZ +1 !

                        // repNum=0 → globalStat je prázdny → preskočíme
                        if (repNum < 1) return;

                        double actMeanAmb = ms.getGlobalWaitAmbStat().getMean() / 60.0;
                        double actMeanWalk = ms.getGlobalWaitWalkInStat().getMean() / 60.0;

                        SwingUtilities.invokeLater(() -> {
                            seriesUstalovanieAmb.add(repNum, actMeanAmb);
                            seriesUstalovaniePesi.add(repNum, actMeanWalk);
                        });
                    }
                }

                @Override
                public void refresh(Simulation simulation) {
                    // Tu nepotrebujeme robiť nič
                }
            });

            // Spustenie na zvolený počet replikácií a čas 28 dní
            settleSim.simulate(reps, 2419200.0);

            SwingUtilities.invokeLater(() -> {
                btnDrawSettling.setEnabled(true);
                btnStart.setEnabled(true);
                JOptionPane.showMessageDialog(MainGUI.this, "Graf ustaľovania bol úspešne vygenerovaný!");
            });
        }).start();
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

            // ========================================================
            // SPRÁVNE VOLANIA LOKÁLNYCH ŠTATISTÍK PRE UI
            // ========================================================

            // --- 1. BLOK: Časy (zobrazujeme priemer bežiacej replikácie) ---
            lblLocTimeInSysAmb.setText(String.format("Systém (Sanitky): %.2f min", mySim.agentZdrojov().getTimeInSystemAmbStat().getMean() / 60.0));
            lblLocTimeInSysWalkIn.setText(String.format("Systém (Peší): %.2f min", mySim.agentZdrojov().getTimeInSystemWalkInStat().getMean() / 60.0));

            lblLocAvgWaitAmb.setText(String.format("Celk. čak. (Sanitky): %.2f min", mySim.agentZdrojov().getWaitingTimeAmbulanceStat().getMean() / 60.0));
            lblLocAvgWaitWalkIn.setText(String.format("Celk. čak. (Peší): %.2f min", mySim.agentZdrojov().getWaitingTimeWalkInStat().getMean() / 60.0));

            lblLocEntryWaitAmb.setText(String.format("Čak. VSTUP (Sanitky): %.2f min", mySim.agentZdrojov().getEntryWaitAmbStat().getMean() / 60.0));
            lblLocEntryWaitWalk.setText(String.format("Čak. VSTUP (Peší): %.2f min", mySim.agentZdrojov().getEntryWaitWalkInStat().getMean() / 60.0));

            lblLocTreatWaitAmb.setText(String.format("Čak. OŠETRENIE (Sanitky): %.2f min", mySim.agentZdrojov().getTreatmentWaitAmbStat().getMean() / 60.0));
            lblLocTreatWaitWalk.setText(String.format("Čak. OŠETRENIE (Peší): %.2f min", mySim.agentZdrojov().getTreatmentWaitWalkInStat().getMean() / 60.0));

            // --- 2. BLOK: Rady (priemerná dĺžka počas bežiacej replikácie) ---
            lblLocEntryQueue.setText(String.format("Rad na Vstup: %.2f", mySim.agentZdrojov().getEntryQueueLengthStat().getMean()));
            lblLocTreatQueue.setText(String.format("Rad na Ošetrenie: %.2f", mySim.agentZdrojov().getTreatmentQueueLengthStat().getMean()));

            // --- 3. BLOK: Vyťaženosť ---
            double totalNurses = mySim.getNumNurses();
            double utilNursesMean = mySim.agentZdrojov().getNurseUtilizationStat().getMean();
            lblLocUtilNurses.setText(String.format("Sestry: %.2f %%", (totalNurses > 0) ? (utilNursesMean / totalNurses) * 100 : 0));

            double totalDoctors = mySim.getNumDoctors();
            double utilDoctorsMean = mySim.agentZdrojov().getDoctorUtilizationStat().getMean();
            lblLocUtilDoctors.setText(String.format("Lekári: %.2f %%", (totalDoctors > 0) ? (utilDoctorsMean / totalDoctors) * 100 : 0));

            lblLocUtilRoomA.setText(String.format("Amb A (Ošetrenie): %.2f %%", (mySim.agentZdrojov().getRoomAUtilizationStat().getMean() / 5.0) * 100));
            lblLocUtilRoomB.setText(String.format("Amb B (Vstup+Ošetr.): %.2f %%", (mySim.agentZdrojov().getRoomBUtilizationStat().getMean() / 7.0) * 100));

            // --- 4. BLOK: Počty (Priechodnosť) ---
            // Počet vybavených pacientov = veľkosť vzorky štatistiky času v systéme
            int ambCount = mySim.agentZdrojov().getTimeInSystemAmbStat().getCount();
            int walkInCount = mySim.agentZdrojov().getTimeInSystemWalkInStat().getCount();

            lblLocTotalPatients.setText(String.format("Spolu: %d", ambCount + walkInCount));
            lblLocTotalWalkIn.setText(String.format(" - Peší: %d", walkInCount));
            lblLocTotalAmb.setText(String.format(" - Sanitky: %d", ambCount));
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