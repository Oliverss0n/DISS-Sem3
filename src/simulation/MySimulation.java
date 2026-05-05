package simulation;

import OSPABA.*;
import agents.agentosetrenia.*;
import agents.agentokolia.*;
import agents.agentzdrojov.*;
import agents.agentboss.*;
import agents.agenturgentu.*;
import agents.agentvstupnehovystrenia.*;
import OSPAnimator.AnimQueue;
import OSPAnimator.AnimShapeItem;
import Statistics.Stat;
import Statistics.TimeStat;
import entities.Patient;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class MySimulation extends OSPABA.Simulation
{
	protected Random genSeed = new Random();
	private int numDoctors;
	private int numNurses;

	private double simTime = 2419200.0;


	private List<Patient> activePatients = Collections.synchronizedList(new ArrayList<>());
	private boolean logEnabled = false;
	private Consumer<String> logger;

	public AnimShapeItem[] grafikaSestier;
	public AnimShapeItem[] grafikaLekarov;

	private AnimQueue radVstup;
	private AnimQueue radOsetrenie;

	//AI
	public Map<AnimShapeItem, Point> domovSestier = new HashMap<>();
	public Map<AnimShapeItem,Point> domovLekarov = new HashMap<>();

    //statistiky
	private Stat globalWaitAmbStat;
	private Stat globalWaitWalkInStat;
	private Stat globalUtilNursesStat;
	private Stat globalUtilDoctorsStat;

	private Stat globalTimeInSystemAmbStat;
	private Stat globalTimeInSystemWalkInStat;
    private Stat globalRoomAUtilStat = new Stat();
    private Stat globalRoomBUtilStat = new Stat();

    // Globálne štatistiky pre počty vybavených pacientov
    private Stat globalTotalPatientsStat = new Stat();
    private Stat globalTotalWalkInStat = new Stat();
    private Stat globalTotalAmbStat = new Stat();

    private Stat globalEntryWaitAmbStat = new Stat();
    private Stat globalEntryWaitWalkInStat = new Stat();
    private Stat globalEntryQueueLengthStat = new Stat();

    private Stat globalTreatmentWaitAmbStat = new Stat();
    private Stat globalTreatmentWaitWalkInStat = new Stat();
    private Stat globalTreatmentQueueLengthStat = new Stat();

    // Počítadlá pre aktuálnu replikáciu
    private int repTotalPatients;
    private int repWalkInPatients;
    private int repAmbPatients;


	//zahrievanie
	private double warmUpTime;

    //varianty
    private int simVariant = 0;

	public MySimulation()
	{
		this.numDoctors = 6;
		this.numNurses = 8;
		globalWaitAmbStat = new Stat();
		globalWaitWalkInStat = new Stat();
		globalUtilNursesStat = new Stat();
		globalUtilDoctorsStat = new Stat();
		globalTimeInSystemAmbStat = new Stat();
		globalTimeInSystemWalkInStat = new Stat();
		this.warmUpTime = 86400.0; //86400.0
		init();
	}

	@Override
	public void prepareSimulation()
	{
		super.prepareSimulation();
		globalWaitAmbStat.clear();
		globalWaitWalkInStat.clear();
		globalUtilNursesStat.clear();
		globalUtilDoctorsStat.clear();
		globalTimeInSystemAmbStat.clear();
		globalTimeInSystemWalkInStat.clear();
        globalEntryWaitAmbStat.clear();
        globalEntryWaitWalkInStat.clear();
        globalEntryQueueLengthStat.clear();
        globalTreatmentWaitAmbStat.clear();
        globalTreatmentWaitWalkInStat.clear();
        globalTreatmentQueueLengthStat.clear();

	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		activePatients.clear();
        repTotalPatients = 0;
        repWalkInPatients = 0;
        repAmbPatients = 0;

		Patient.resetIdCounter();
		// Reset entities, queues, local statistics, etc...
	}

	@Override
	public void replicationFinished()
	{
		// Collect local statistics into global, update UI, etc...
		super.replicationFinished();
		globalWaitAmbStat.add(agentZdrojov().getWaitingTimeAmbulanceStat().getMean());
		globalWaitWalkInStat.add(agentZdrojov().getWaitingTimeWalkInStat().getMean());
		globalUtilNursesStat.add(agentZdrojov().getNurseUtilizationStat().getMean());
		globalUtilDoctorsStat.add(agentZdrojov().getDoctorUtilizationStat().getMean());
		globalTimeInSystemAmbStat.add(agentZdrojov().getTimeInSystemAmbStat().getMean());
		globalTimeInSystemWalkInStat.add(agentZdrojov().getTimeInSystemWalkInStat().getMean());

        globalRoomAUtilStat.add(agentZdrojov().getRoomAUtilizationStat().getMean());
        globalRoomBUtilStat.add(agentZdrojov().getRoomBUtilizationStat().getMean());

        globalTotalPatientsStat.add(repTotalPatients);
        globalTotalWalkInStat.add(repWalkInPatients);
        globalTotalAmbStat.add(repAmbPatients);

        globalEntryWaitAmbStat.add(agentZdrojov().getEntryWaitAmbStat().getMean());
        globalEntryWaitWalkInStat.add(agentZdrojov().getEntryWaitWalkInStat().getMean());
        globalEntryQueueLengthStat.add(agentZdrojov().getEntryQueueLengthStat().getMean());

        globalTreatmentWaitAmbStat.add(agentZdrojov().getTreatmentWaitAmbStat().getMean());
        globalTreatmentWaitWalkInStat.add(agentZdrojov().getTreatmentWaitWalkInStat().getMean());
        globalTreatmentQueueLengthStat.add(agentZdrojov().getTreatmentQueueLengthStat().getMean());

	}

	//vypisy vygenerovane AI
    @Override
    public void simulationFinished()
    {
        super.simulationFinished();
        System.out.println("\n======================================================");
        System.out.println("         SIMULACIA UKONCENA - GLOBALNE VYSLEDKY       ");
        System.out.println("======================================================");
        System.out.println("Pocet vykonanych replikacii: " + currentReplication());
        System.out.println("------------------------------------------------------");

        System.out.println("\n--- 1. CASY (v minutach) ---");
        // Celkový čas v systéme
        System.out.printf("Cas v systeme celkovo (Sanitky): %.2f min  IS: <%.2f, %.2f>\n", globalTimeInSystemAmbStat.getMean() / 60.0, globalTimeInSystemAmbStat.getConfidenceIntervalLower() / 60.0, globalTimeInSystemAmbStat.getConfidenceIntervalUpper() / 60.0);
        System.out.printf("Cas v systeme celkovo (Pesi):    %.2f min  IS: <%.2f, %.2f>\n", globalTimeInSystemWalkInStat.getMean() / 60.0, globalTimeInSystemWalkInStat.getConfidenceIntervalLower() / 60.0, globalTimeInSystemWalkInStat.getConfidenceIntervalUpper() / 60.0);
        // Celkové čakanie
        System.out.printf("Celkove cakanie (Sanitky):       %.2f min  IS: <%.2f, %.2f>\n", globalWaitAmbStat.getMean() / 60.0, globalWaitAmbStat.getConfidenceIntervalLower() / 60.0, globalWaitAmbStat.getConfidenceIntervalUpper() / 60.0);
        System.out.printf("Celkove cakanie (Pesi):          %.2f min  IS: <%.2f, %.2f>\n", globalWaitWalkInStat.getMean() / 60.0, globalWaitWalkInStat.getConfidenceIntervalLower() / 60.0, globalWaitWalkInStat.getConfidenceIntervalUpper() / 60.0);
        // Čakanie na Vstup
        System.out.printf("Cakanie na VSTUP (Sanitky):      %.2f min  IS: <%.2f, %.2f>\n", globalEntryWaitAmbStat.getMean() / 60.0, globalEntryWaitAmbStat.getConfidenceIntervalLower() / 60.0, globalEntryWaitAmbStat.getConfidenceIntervalUpper() / 60.0);
        System.out.printf("Cakanie na VSTUP (Pesi):         %.2f min  IS: <%.2f, %.2f>\n", globalEntryWaitWalkInStat.getMean() / 60.0, globalEntryWaitWalkInStat.getConfidenceIntervalLower() / 60.0, globalEntryWaitWalkInStat.getConfidenceIntervalUpper() / 60.0);
        // Čakanie na Ošetrenie (Lekára)
        System.out.printf("Cakanie na OSETRENIE (Sanitky):  %.2f min  IS: <%.2f, %.2f>\n", globalTreatmentWaitAmbStat.getMean() / 60.0, globalTreatmentWaitAmbStat.getConfidenceIntervalLower() / 60.0, globalTreatmentWaitAmbStat.getConfidenceIntervalUpper() / 60.0);
        System.out.printf("Cakanie na OSETRENIE (Pesi):     %.2f min  IS: <%.2f, %.2f>\n", globalTreatmentWaitWalkInStat.getMean() / 60.0, globalTreatmentWaitWalkInStat.getConfidenceIntervalLower() / 60.0, globalTreatmentWaitWalkInStat.getConfidenceIntervalUpper() / 60.0);

        System.out.println("\n--- 2. RADY (v osobach) ---");
        System.out.printf("Dlzka radu na VSTUP:             %.2f os.  IS: <%.2f, %.2f>\n", globalEntryQueueLengthStat.getMean(), globalEntryQueueLengthStat.getConfidenceIntervalLower(), globalEntryQueueLengthStat.getConfidenceIntervalUpper());
        System.out.printf("Dlzka radu na OSETRENIE:         %.2f os.  IS: <%.2f, %.2f>\n", globalTreatmentQueueLengthStat.getMean(), globalTreatmentQueueLengthStat.getConfidenceIntervalLower(), globalTreatmentQueueLengthStat.getConfidenceIntervalUpper());

        System.out.println("\n--- 3. VYTAZENOST ZDROJOV (%) ---");
        double utilNursesMean = globalUtilNursesStat.getMean();
        System.out.printf("Sestry:                          %.2f %%  (%.2f / %d) IS: <%.2f, %.2f>\n", (numNurses > 0) ? (utilNursesMean / numNurses) * 100 : 0, utilNursesMean, numNurses, globalUtilNursesStat.getConfidenceIntervalLower(), globalUtilNursesStat.getConfidenceIntervalUpper());
        double utilDocsMean = globalUtilDoctorsStat.getMean();
        System.out.printf("Lekari:                          %.2f %%  (%.2f / %d) IS: <%.2f, %.2f>\n", (numDoctors > 0) ? (utilDocsMean / numDoctors) * 100 : 0, utilDocsMean, numDoctors, globalUtilDoctorsStat.getConfidenceIntervalLower(), globalUtilDoctorsStat.getConfidenceIntervalUpper());
        double utilRoomAMean = globalRoomAUtilStat.getMean();
        System.out.printf("Ambulancia A (Osetrenie):        %.2f %%  (%.2f / 5) IS: <%.2f, %.2f>\n", (utilRoomAMean / 5.0) * 100, utilRoomAMean, globalRoomAUtilStat.getConfidenceIntervalLower(), globalRoomAUtilStat.getConfidenceIntervalUpper());
        double utilRoomBMean = globalRoomBUtilStat.getMean();
        System.out.printf("Ambulancia B (Osetrenie+Vstup):  %.2f %%  (%.2f / 7) IS: <%.2f, %.2f>\n", (utilRoomBMean / 7.0) * 100, utilRoomBMean, globalRoomBUtilStat.getConfidenceIntervalLower(), globalRoomBUtilStat.getConfidenceIntervalUpper());

        System.out.println("\n--- 4. PRIECHODNOST (pocet pacientov) ---");
        System.out.printf("Vybaveni celkovo:                %.2f  IS: <%.2f, %.2f>\n", globalTotalPatientsStat.getMean(), globalTotalPatientsStat.getConfidenceIntervalLower(), globalTotalPatientsStat.getConfidenceIntervalUpper());
        System.out.printf(" - Z toho Pesi:                  %.2f  IS: <%.2f, %.2f>\n", globalTotalWalkInStat.getMean(), globalTotalWalkInStat.getConfidenceIntervalLower(), globalTotalWalkInStat.getConfidenceIntervalUpper());
        System.out.printf(" - Z toho Sanitky:               %.2f  IS: <%.2f, %.2f>\n", globalTotalAmbStat.getMean(), globalTotalAmbStat.getConfidenceIntervalLower(), globalTotalAmbStat.getConfidenceIntervalUpper());

        System.out.println("======================================================\n");
    }
	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		setAgentBoss(new AgentBoss(Id.agentBoss, this, null));
		setAgentOkolia(new AgentOkolia(Id.agentOkolia, this, agentBoss()));
		setAgentUrgentu(new AgentUrgentu(Id.agentUrgentu, this, agentBoss()));
		setAgentVstupnehoVystrenia(new AgentVstupnehoVystrenia(Id.agentVstupnehoVystrenia, this, agentUrgentu()));
		setAgentOsetrenia(new AgentOsetrenia(Id.agentOsetrenia, this, agentUrgentu()));
		setAgentZdrojov(new AgentZdrojov(Id.agentZdrojov, this, agentUrgentu()));
	}

	private AgentBoss _agentBoss;

public AgentBoss agentBoss()
	{ return _agentBoss; }

	public void setAgentBoss(AgentBoss agentBoss)
	{_agentBoss = agentBoss; }

	private AgentOkolia _agentOkolia;

public AgentOkolia agentOkolia()
	{ return _agentOkolia; }

	public void setAgentOkolia(AgentOkolia agentOkolia)
	{_agentOkolia = agentOkolia; }

	private AgentUrgentu _agentUrgentu;

public AgentUrgentu agentUrgentu()
	{ return _agentUrgentu; }

	public void setAgentUrgentu(AgentUrgentu agentUrgentu)
	{_agentUrgentu = agentUrgentu; }

	private AgentVstupnehoVystrenia _agentVstupnehoVystrenia;

public AgentVstupnehoVystrenia agentVstupnehoVystrenia()
	{ return _agentVstupnehoVystrenia; }

	public void setAgentVstupnehoVystrenia(AgentVstupnehoVystrenia agentVstupnehoVystrenia)
	{_agentVstupnehoVystrenia = agentVstupnehoVystrenia; }

	private AgentOsetrenia _agentOsetrenia;

public AgentOsetrenia agentOsetrenia()
	{ return _agentOsetrenia; }

	public void setAgentOsetrenia(AgentOsetrenia agentOsetrenia)
	{_agentOsetrenia = agentOsetrenia; }

	private AgentZdrojov _agentZdrojov;

public AgentZdrojov agentZdrojov()
	{ return _agentZdrojov; }

	public void setAgentZdrojov(AgentZdrojov agentZdrojov)
	{_agentZdrojov = agentZdrojov; }
	//meta! tag="end"

//-----------------------------------------MOJE METODY--------------------------------------------------//

	public void addPatient(Patient p) {
		activePatients.add(p);
	}

	public void removePatient(Patient p) {
		activePatients.remove(p);
	}

	public List<Patient> getActivePatients() {
		return activePatients;
	}

	public int getNumDoctors() {
		return numDoctors;
	}

	public void setNumDoctors(int numDoctors) {
		this.numDoctors = numDoctors;
	}

	public int getNumNurses() {
		return numNurses;
	}

	public void setNumNurses(int numNurses) {
		this.numNurses = numNurses;
	}


	public int getQueueEntranceSize() {
		return agentZdrojov().getQueueEntrance().size();
	}

	public int getQueueExamSize() {
		return agentZdrojov().getQueueExaminationA().size() + agentZdrojov().getQueueExaminationB().size();
	}


	public int getFreeAmbulancesA() {
		return agentZdrojov().getFreeAmbulancesA();
	}

	public int getFreeAmbulancesB() {
		return agentZdrojov().getFreeAmbulancesB();
	}

	public Random getGenSeed() {
		return genSeed;
	}

	public void setLogEnabled(boolean enabled) {
		this.logEnabled = enabled;
	}

	public void setLogger(Consumer<String> logger) {
		this.logger = logger;
	}

	public void log(String message) {
		if (logEnabled && logger != null) {
			logger.accept(String.format("[%.2f] %s", currentTime(), message));
		}
	}





	//------------------------------pokus o animaciu-----------------------------------------
	public void obsadSestru(java.awt.Point ciel, OSPAnimator.AnimShapeItem sestra) {
		if (animatorExists() && sestra != null) {
			sestra.setColor(Color.RED);
			// Presunie sestru do ambulancie (s jemným posunom +15, aby nestála presne na pacientovi)
			sestra.moveTo(currentTime(), 0.5, ciel.x + 15, ciel.y);
		}
	}

	public void uvolniSestru(OSPAnimator.AnimShapeItem sestra) {
		if (animatorExists() && sestra != null) {
			// Iba zmení farbu na voľnú (Zelenú)
			sestra.setColor(Color.GREEN);

			// ZMAZANÝ PRESUN DOMOV:
			// Sestra ostáva fyzicky stáť tam, kde práve je (v poslednej ambulancii).
		}
	}

	public void obsadLekara(java.awt.Point ciel, OSPAnimator.AnimShapeItem lekar) {
		if (animatorExists() && lekar != null) {
			lekar.setColor(Color.RED);
			// Presunie lekára do ambulancie (s jemným posunom -15)
			lekar.moveTo(currentTime(), 0.5, ciel.x - 15, ciel.y);
		}
	}

	public void uvolniLekara(OSPAnimator.AnimShapeItem lekar) {
		if (animatorExists() && lekar != null) {
			// Iba zmení farbu na voľnú (Tyrkysovú)
			lekar.setColor(Color.CYAN);

			// ZMAZANÝ PRESUN DOMOV:
			// Lekár ostáva fyzicky stáť tam, kde práve je.
		}
	}

	public AnimQueue getRadVstup() { return radVstup; }
	public void setRadVstup(AnimQueue radVstup) { this.radVstup = radVstup; }

	public AnimQueue getRadOsetrenie() { return radOsetrenie; }
	public void setRadOsetrenie(AnimQueue radOsetrenie) { this.radOsetrenie = radOsetrenie; }



	//ANIMACIA - TESTOVANIE

	// 5x Ambulancia A (ľavá strana)
	// 5x Ambulancia A (teraz na PRAVEJ strane)
	public java.awt.Point[] bodyAmbA = {
			new java.awt.Point(640, 100), new java.awt.Point(700, 100),
			new java.awt.Point(760, 100), new java.awt.Point(820, 100),
			new java.awt.Point(880, 100)
	};
	public boolean[] obsadeneAmbA = new boolean[5];

	// 7x Ambulancia B (teraz na ĽAVEJ strane)
	public java.awt.Point[] bodyAmbB = {
			new java.awt.Point(120, 100), new java.awt.Point(180, 100),
			new java.awt.Point(240, 100), new java.awt.Point(300, 100),
			new java.awt.Point(360, 100), new java.awt.Point(420, 100),
			new java.awt.Point(480, 100)
	};
	public boolean[] obsadeneAmbB = new boolean[7];

	// Metóda nájde prvý voľný vizuálny obdĺžnik a vráti jeho súradnicu
	public java.awt.Point zamkniVizualnuAmbulanciu(String typ) {
		if ("A".equals(typ)) {
			for (int i = 0; i < 5; i++) {
				if (!obsadeneAmbA[i]) {
					obsadeneAmbA[i] = true;  // <--- TOTO je ten kritický bod, ktorý musí na GUI ukázať [■]
					return bodyAmbA[i];
				}
			}
		} else {
			for (int i = 0; i < 7; i++) {
				if (!obsadeneAmbB[i]) {
					obsadeneAmbB[i] = true;
					return bodyAmbB[i];
				}
			}
		}
		return new java.awt.Point(500, 500);
	}

	// Uvoľnenie vizuálnej pozície, keď pacient odchádza
	public void odomkniVizualnuAmbulanciu(String typ, java.awt.Point p) {
		if (p == null) return;
		if ("A".equals(typ)) {
			for (int i = 0; i < 5; i++) {
				if (bodyAmbA[i].equals(p)) obsadeneAmbA[i] = false;
			}
		} else {
			for (int i = 0; i < 7; i++) {
				if (bodyAmbB[i].equals(p)) obsadeneAmbB[i] = false;
			}
		}
	}

	//*******************************STATISTIKY**************************************


	public Stat getGlobalWaitAmbStat() {
		return globalWaitAmbStat;
	}

	public Stat getGlobalWaitWalkInStat() {
		return globalWaitWalkInStat;
	}

	public Stat getGlobalUtilNursesStat() {
		return globalUtilNursesStat;
	}

	public Stat getGlobalUtilDoctorsStat() {
		return globalUtilDoctorsStat;
	}

	public Stat getGlobalTimeInSystemAmbStat() { return globalTimeInSystemAmbStat; }
	public Stat getGlobalTimeInSystemWalkInStat() { return globalTimeInSystemWalkInStat; }

    public Stat getGlobalRoomAUtilStat() {
        return globalRoomAUtilStat;
    }

    public Stat getGlobalRoomBUtilStat() {
        return globalRoomBUtilStat;
    }

    public Stat getGlobalTotalPatientsStat() {
        return globalTotalPatientsStat;
    }

    public Stat getGlobalTotalWalkInStat() {
        return globalTotalWalkInStat;
    }

    public Stat getGlobalTotalAmbStat() {
        return globalTotalAmbStat;
    }

    // ----------------- GETTERY PRE ČAKANIE NA VSTUP -----------------

    public Stat getGlobalEntryWaitAmbStat() {
        return globalEntryWaitAmbStat;
    }

    public Stat getGlobalEntryWaitWalkInStat() {
        return globalEntryWaitWalkInStat;
    }

    public Stat getGlobalEntryQueueLengthStat() {
        return globalEntryQueueLengthStat;
    }

    public Stat getGlobalTreatmentWaitAmbStat() { return globalTreatmentWaitAmbStat; }
    public Stat getGlobalTreatmentWaitWalkInStat() { return globalTreatmentWaitWalkInStat; }
    public Stat getGlobalTreatmentQueueLengthStat() { return globalTreatmentQueueLengthStat; }


	public double getWarmUpTime() {
		return warmUpTime;
	}

	public void setWarmUpTime(double warmUpTime) {
		this.warmUpTime = warmUpTime;
	}

	public void incrementPatientCount(boolean isAmbulance) {
        repTotalPatients++;
        if (isAmbulance) repAmbPatients++;
        else repWalkInPatients++;
    }

    public int getVariant() {
        return simVariant;
    }

    public void setVariant(int simVariant) {
        this.simVariant = simVariant;
    }

	public double getSimTime() {
		return simTime;
	}

	public void setSimTime(double simTime) {
		this.simTime = simTime;
	}
}