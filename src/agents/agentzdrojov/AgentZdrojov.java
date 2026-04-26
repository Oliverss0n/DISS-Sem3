package agents.agentzdrojov;

import OSPABA.*;
import simulation.*;
import OSPDataStruct.SimQueue;
import java.util.LinkedList;
import java.util.Queue;

//meta! id="41"
public class AgentZdrojov extends OSPABA.Agent
{

	private int freeNurses;
	private int freeDoctors;
	private int freeAmbulancesA;
	private int freeAmbulancesB;

	private Queue<MessageForm> queueEntrance;
	private Queue<MessageForm> queueExaminationA;
	private Queue<MessageForm> queueExaminationB;

	public AgentZdrojov(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
		queueEntrance = new LinkedList<>();
		queueExaminationA = new LinkedList<>();
		queueExaminationB = new LinkedList<>();
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

	public Queue<MessageForm> getQueueEntrance() {
		return queueEntrance;
	}

	public void setQueueEntrance(Queue<MessageForm> queueEntrance) {
		this.queueEntrance = queueEntrance;
	}

	public Queue<MessageForm> getQueueExaminationA() {
		return queueExaminationA;
	}

	public void setQueueExaminationA(Queue<MessageForm> queueExaminationA) {
		this.queueExaminationA = queueExaminationA;
	}

	public Queue<MessageForm> getQueueExaminationB() {
		return queueExaminationB;
	}

	public void setQueueExaminationB(Queue<MessageForm> queueExaminationB) {
		this.queueExaminationB = queueExaminationB;
	}
}