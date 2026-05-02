package simulation;

import OSPABA.*;
import OSPAnimator.AnimQueue;
import OSPAnimator.AnimShapeItem;
import Statistics.Stat;
import Statistics.TimeStat;
import agents.agentosetrenia.*;
import agents.agentokolia.*;
import agents.agentzdrojov.*;
import agents.agentboss.*;
import agents.agenturgentu.*;
import agents.agentvstupnehovystrenia.*;
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

	//private List<Patient> activePatients = new LinkedList<>();
	//private List<Patient> activePatients = new ArrayList<>();
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

	private Stat globalWaitAmbStat;
	private Stat globalWaitWalkInStat;
	private Stat globalUtilNursesStat;
	private Stat globalUtilDoctorsStat;

	public MySimulation()
	{
		this.numDoctors = 10;
		this.numNurses = 10;
		globalWaitAmbStat = new Stat();
		globalWaitWalkInStat = new Stat();
		globalUtilNursesStat = new Stat();
		globalUtilDoctorsStat = new Stat();
		//this.setWarmUpTime(86400.0);
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
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		activePatients.clear();

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
	}

	@Override
	public void simulationFinished()
	{
		// Display simulation results
		super.simulationFinished();
		// --- VÝPIS DO KLASICKEJ KONZOLY ---
		System.out.println("\n======================================================");
		System.out.println("         SIMULÁCIA UKONČENÁ - GLOBÁLNE VÝSLEDKY       ");
		System.out.println("======================================================");
		System.out.println("Počet vykonaných replikácií: " + currentReplication());
		System.out.println("------------------------------------------------------");

		// 1. Čakanie - Sanitky
		double waitAmbMean = globalWaitAmbStat.getMean();
		double waitAmbLow = globalWaitAmbStat.getConfidenceIntervalLower();
		double waitAmbHigh = globalWaitAmbStat.getConfidenceIntervalUpper();
		System.out.printf("Priemerný čas čakania (Sanitky): %.2f s  IS: <%.2f, %.2f>\n", waitAmbMean, waitAmbLow, waitAmbHigh);

		// 2. Čakanie - Peší
		double waitWalkInMean = globalWaitWalkInStat.getMean();
		double waitWalkInLow = globalWaitWalkInStat.getConfidenceIntervalLower();
		double waitWalkInHigh = globalWaitWalkInStat.getConfidenceIntervalUpper();
		System.out.printf("Priemerný čas čakania (Peší):    %.2f s  IS: <%.2f, %.2f>\n", waitWalkInMean, waitWalkInLow, waitWalkInHigh);

		// 3. Vyťaženie - Sestry
		double utilNursesMean = globalUtilNursesStat.getMean();
		double utilNursesPct = (numNurses > 0) ? (utilNursesMean / numNurses) * 100 : 0;
		System.out.printf("Priemerné vyťaženie (Sestry):    %.2f %%  (%.2f / %d) IS: <%.2f, %.2f>\n",
				utilNursesPct, utilNursesMean, numNurses,
				globalUtilNursesStat.getConfidenceIntervalLower(), globalUtilNursesStat.getConfidenceIntervalUpper());

		// 4. Vyťaženie - Lekári
		double utilDoctorsMean = globalUtilDoctorsStat.getMean();
		double utilDoctorsPct = (numDoctors > 0) ? (utilDoctorsMean / numDoctors) * 100 : 0;
		System.out.printf("Priemerné vyťaženie (Lekári):    %.2f %%  (%.2f / %d) IS: <%.2f, %.2f>\n",
				utilDoctorsPct, utilDoctorsMean, numDoctors,
				globalUtilDoctorsStat.getConfidenceIntervalLower(), globalUtilDoctorsStat.getConfidenceIntervalUpper());

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

	//logy - asi niekde inde dat?
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
}
