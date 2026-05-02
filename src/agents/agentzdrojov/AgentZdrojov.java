package agents.agentzdrojov;

import OSPABA.*;
import Statistics.Stat;
import Statistics.TimeStat;
import entities.Doctor;
import entities.Nurse;
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

	/*private int freeNurses;
	private int freeDoctors;*/
	private int freeAmbulancesA;
	private int freeAmbulancesB;

	private PriorityQueue<MessageForm> queueEntrance;
	private PriorityQueue<MessageForm> queueExaminationA;
	private PriorityQueue<MessageForm> queueExaminationB;

	// Namiesto: private int freeNurses; a private int freeDoctors;
	private Queue<Nurse> freeNurses = new LinkedList<>();
	private Queue<Doctor> freeDoctors = new LinkedList<>();

	//statistiky
	private Stat waitingTimeAmbulanceStat = new Stat();
	private Stat waitingTimeWalkInStat = new Stat();
	private TimeStat nurseUtilizationStat;
	private TimeStat doctorUtilizationStat;


	public AgentZdrojov(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
		queueEntrance = createPatientQueue();
		queueExaminationA = createPatientQueue();
		queueExaminationB = createPatientQueue();
		nurseUtilizationStat = new TimeStat(mySim);
		doctorUtilizationStat = new TimeStat(mySim);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		MySimulation sim = (MySimulation) mySim();

		// 1. Vytvorenie a prepojenie sestier
		freeNurses.clear();
		for (int i = 0; i < sim.getNumNurses(); i++) {
			entities.Nurse n = new entities.Nurse(i + 1);
			// Ak je zapnutá animácia, prepojíme logickú sestru s grafickým trojuholníkom
			if (sim.animatorExists() && sim.grafikaSestier != null && i < sim.grafikaSestier.length) {
				n.setAnimItem(sim.grafikaSestier[i]);
			}
			freeNurses.add(n);
		}

		// 2. Vytvorenie a prepojenie lekárov
		freeDoctors.clear();
		for (int i = 0; i < sim.getNumDoctors(); i++) {
			entities.Doctor d = new entities.Doctor(i + 1);
			// Ak je zapnutá animácia, prepojíme logického lekára s grafickým štvorcom
			if (sim.animatorExists() && sim.grafikaLekarov != null && i < sim.grafikaLekarov.length) {
				d.setAnimItem(sim.grafikaLekarov[i]);
			}
			freeDoctors.add(d);
		}

		// 3. Reset kapacít
		this.freeAmbulancesA = 5;
		this.freeAmbulancesB = 7;

		// 4. Reset vizualizácie ambulancií (aby neostali "visieť" po reštarte)
		for (int i = 0; i < 5; i++){
			sim.obsadeneAmbA[i] = false;
		}
		for (int i = 0; i < 7; i++){
			sim.obsadeneAmbB[i] = false;
		}

		// 5. Vyčistenie radov
		this.queueEntrance.clear();
		this.queueExaminationA.clear();
		this.queueExaminationB.clear();

		waitingTimeAmbulanceStat.clear();
		waitingTimeWalkInStat.clear();

		nurseUtilizationStat.clear(mySim().currentTime());
		doctorUtilizationStat.clear(mySim().currentTime());
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


	public Queue<Nurse> getFreeNurses() {
		return freeNurses;
	}

	public void setFreeNurses(Queue<Nurse> freeNurses) {
		this.freeNurses = freeNurses;
	}

	public Queue<Doctor> getFreeDoctors() {
		return freeDoctors;
	}

	public void setFreeDoctors(Queue<Doctor> freeDoctors) {
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

	public Stat getWaitingTimeAmbulanceStat() {
		return waitingTimeAmbulanceStat;
	}
	public Stat getWaitingTimeWalkInStat() {
		return waitingTimeWalkInStat;
	}
	public TimeStat getNurseUtilizationStat() {
		return nurseUtilizationStat;
	}
	public TimeStat getDoctorUtilizationStat() {
		return doctorUtilizationStat;
	}

}