package simulation;

import Distributions.TriangularDist;
import OSPABA.*;
import OSPAnimator.AnimQueue;
import OSPAnimator.AnimShape;
import OSPAnimator.AnimShapeItem;
import OSPAnimator.AnimTextItem;
import agents.agentosetrenia.*;
import agents.agentokolia.*;
import agents.agentzdrojov.*;
import agents.agentboss.*;
import agents.agenturgentu.*;
import agents.agentvstupnehovystrenia.*;
import entities.Patient;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

	public java.util.Map<OSPAnimator.AnimShapeItem, java.awt.Point> domovSestier = new java.util.HashMap<>();
	public java.util.Map<OSPAnimator.AnimShapeItem, java.awt.Point> domovLekarov = new java.util.HashMap<>();

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

	public MySimulation()
	{
		this.numDoctors = 10;
		this.numNurses = 10;
		init();
	}

	@Override
	public void prepareSimulation()
	{
		super.prepareSimulation();
		// Create global statistcis
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
	}

	@Override
	public void simulationFinished()
	{
		// Display simulation results
		super.simulationFinished();
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
			sestra.setColor(Color.GREEN);
			java.awt.Point home = domovSestier.get(sestra);
			if (home != null) {
				// Návrat na základňu k vchodu sanitiek
				sestra.moveTo(currentTime(), 0.5, home.x, home.y);
			}
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
			lekar.setColor(Color.CYAN);
			java.awt.Point home = domovLekarov.get(lekar);
			if (home != null) {
				// Návrat na základňu k vchodu sanitiek
				lekar.moveTo(currentTime(), 0.5, home.x, home.y);
			}
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
			new java.awt.Point(650, 100), new java.awt.Point(710, 100),
			new java.awt.Point(770, 100), new java.awt.Point(830, 100),
			new java.awt.Point(890, 100)
	};
	public boolean[] obsadeneAmbA = new boolean[5];

	// 7x Ambulancia B (teraz na ĽAVEJ strane)
	public java.awt.Point[] bodyAmbB = {
			new java.awt.Point(130, 100), new java.awt.Point(190, 100),
			new java.awt.Point(250, 100), new java.awt.Point(310, 100),
			new java.awt.Point(370, 100), new java.awt.Point(430, 100),
			new java.awt.Point(490, 100)
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

}
