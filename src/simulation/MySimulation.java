package simulation;

import OSPABA.*;
import agents.agentosetrenia.*;
import agents.agentokolia.*;
import agents.agentzdrojov.*;
import agents.agentboss.*;
import agents.agenturgentu.*;
import agents.agentvstupnehovystrenia.*;
import entities.Patient;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class MySimulation extends OSPABA.Simulation
{
	protected Random genSeed = new Random();
	private int numDoctors;
	private int numNurses;

	//private List<Patient> activePatients = new LinkedList<>();
	private List<Patient> activePatients = Collections.synchronizedList(new ArrayList<>());
	private boolean logEnabled = false; // Príznak vizuálneho režimu
	private Consumer<String> logger;

	public void setLogEnabled(boolean enabled) {
		this.logEnabled = enabled;
	}

	public void setLogger(Consumer<String> logger) {
		this.logger = logger;
	}

	public void log(String message) {
		// Ak nie je zapnutý vizuálny režim, metóda skončí okamžite
		// Tým sa vyhneme réžii so SwingUtilities a prekresľovaním GUI
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

		// Vynulujeme IDčka pacientov od 0
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

	// OPRAVA: Voláme .size() na fronte, ktorú nám vráti AgentZdrojov
	public int getQueueEntranceSize() {
		return agentZdrojov().getQueueEntrance().size();
	}

	// OPRAVA: Keďže teraz máme dva fronty na ošetrenie, pre GUI ich môžeme sčítať (alebo urobiť dve samostatné metódy, ak to chceš v GUI vidieť oddelene)
	public int getQueueExamSize() {
		return agentZdrojov().getQueueExaminationA().size() + agentZdrojov().getQueueExaminationB().size();
	}

	public int getFreeDoctors() {
		return agentZdrojov().getFreeDoctors();
	}

	public int getFreeNurses() {
		return agentZdrojov().getFreeNurses();
	}

	// OPRAVA: Zmenené názvy metód, aby sedeli s AgentZdrojov
	public int getFreeAmbulancesA() {
		return agentZdrojov().getFreeAmbulancesA();
	}

	public int getFreeAmbulancesB() {
		return agentZdrojov().getFreeAmbulancesB();
	}

	public Random getGenSeed() {
		return genSeed;
	}
}