package agents.agentzdrojov;

import OSPABA.*;
import entities.Patient;
import simulation.*;
import OSPDataStruct.SimQueue;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

//meta! id="41"
public class AgentZdrojov extends OSPABA.Agent
{

	private int freeNurses;
	private int freeDoctors;
	private int freeAmbulancesA;
	private int freeAmbulancesB;

	private PriorityQueue<MessageForm> queueEntrance;
	private PriorityQueue<MessageForm> queueExaminationA;
	private PriorityQueue<MessageForm> queueExaminationB;

	public AgentZdrojov(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
		queueEntrance = createPatientQueue();
		queueExaminationA = createPatientQueue();
		queueExaminationB = createPatientQueue();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication

		MySimulation sim = (MySimulation) mySim();

		// Inicializácia zdrojov na začiatku každej replikácie podľa nastavení zo simulácie
		this.freeDoctors = sim.getNumDoctors();
		this.freeNurses = sim.getNumNurses();
		this.freeAmbulancesA = 5; // Opravený názov premennej (predtým freeAmbA)
		this.freeAmbulancesB = 7; // Opravený názov premennej (predtým freeAmbB)

		// Správne premazanie všetkých troch frontov pred novou replikáciou
		this.queueEntrance.clear();
		this.queueExaminationA.clear();
		this.queueExaminationB.clear();
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerZdrojov(Id.managerZdrojov, mySim(), this);
		addOwnMessage(Mc.reqZdrojeOsetrenie);
		addOwnMessage(Mc.uvolniZdrojeVstup);
		addOwnMessage(Mc.reqZdrojeVstup);
		addOwnMessage(Mc.uvolniZdrojeOsetrenie);
	}
	//meta! tag="end"


	public int getFreeNurses() {
		return freeNurses;
	}

	public void setFreeNurses(int freeNurses) {
		this.freeNurses = freeNurses;
	}

	public int getFreeDoctors() {
		return freeDoctors;
	}

	public void setFreeDoctors(int freeDoctors) {
		this.freeDoctors = freeDoctors;
	}

	public int getFreeAmbulancesA() {
		return freeAmbulancesA;
	}

	public void setFreeAmbulancesA(int freeAmbulancesA) {
		this.freeAmbulancesA = freeAmbulancesA;
	}

	public int getFreeAmbulancesB() {
		return freeAmbulancesB;
	}

	public void setFreeAmbulancesB(int freeAmbulancesB) {
		this.freeAmbulancesB = freeAmbulancesB;
	}

	public PriorityQueue<MessageForm> getQueueEntrance() {
		return queueEntrance;
	}

	public PriorityQueue<MessageForm> getQueueExaminationA() {
		return queueExaminationA;
	}

	public PriorityQueue<MessageForm> getQueueExaminationB() {
		return queueExaminationB;
	}



	//POMOCNY METODA VYGENEROVANA AI
	private PriorityQueue<MessageForm> createPatientQueue() {
		return new PriorityQueue<>(
				Comparator.comparing(msg -> ((MyMessage) msg).getPatient())
		);
	}

}