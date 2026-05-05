package agents.agentzdrojov;

import OSPABA.*;
import OSPStat.Stat;
import entities.Doctor;
import entities.Nurse;
import simulation.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

//meta! id="41"
public class ManagerZdrojov extends OSPABA.Manager
{

	public ManagerZdrojov(int id, Simulation mySim, Agent myAgent)
	{
		super(id, mySim, myAgent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		if (petriNet() != null)
		{
			petriNet().clear();
		}
		MySimulation sim = (MySimulation) mySim();
		double warmUp = sim.getWarmUpTime();

		if (warmUp > 0.0) {
			MyMessage msg = new MyMessage(sim);
			msg.setCode(Mc.start);
			msg.setAddressee(myAgent().findAssistant(Id.schedulerZahrievania));
			notice(msg);
		}
	}

	//meta! sender="AgentUrgentu", id="110", type="Request"
	public void processReqZdrojeOsetrenie(MessageForm message)
	{
		MyMessage msg = (MyMessage) message;
		int priorita = msg.getPatient().getPriority();

		msg.getPatient().setArrivalTimeQueueTreatment(mySim().currentTime());
		msg.getPatient().setStav("Čaká v rade na Ošetrenie");

		if (priorita == 1 || priorita == 2) {
			myAgent().getQueueExaminationA().add(msg);
		}
		else if (priorita == 5) {
			myAgent().getQueueExaminationB().add(msg);
		}
		else if (priorita == 3 || priorita == 4) {
			myAgent().getQueueExaminationA().add(msg);
			myAgent().getQueueExaminationB().add(msg);
		}

		checkQueues();
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
	}

	//meta! sender="AgentUrgentu", id="64", type="Request"
	public void processReqZdrojeVstup(MessageForm message)
	{
		MyMessage msg = (MyMessage) message;
		msg.getPatient().setArrivalTimeQueueExam(mySim().currentTime());

		msg.getPatient().setStav("Čaká v rade na Vstup");

		myAgent().getQueueEntrance().add(msg);
		updateResourceUtilization();
		checkQueues();
	}

	//meta! sender="AgentUrgentu", id="112", type="Notice"
	public void processUvolniZdrojeVstup(MessageForm message)
	{
		MyMessage msg = (MyMessage) message;
		MySimulation sim = (MySimulation) mySim();

		Nurse sestra = msg.getPatient().getAssignedNurse();

		myAgent().getFreeNurses().add(sestra);
		sim.uvolniSestru(sestra.getAnimItem());

		myAgent().setFreeAmbulancesB(myAgent().getFreeAmbulancesB() + 1);
		sim.odomkniVizualnuAmbulanciu("B", msg.getPatient().getVisualAmbPosition());

		checkQueues();
		updateResourceUtilization();
	}

	//meta! sender="AgentUrgentu", id="113", type="Notice"
	public void processUvolniZdrojeOsetrenie(MessageForm message)
	{
		MyMessage msg = (MyMessage) message;
		MySimulation sim = (MySimulation) mySim();


		Nurse sestra = msg.getPatient().getAssignedNurse();
		Doctor lekar = msg.getPatient().getAssignedDoctor();

		myAgent().getFreeNurses().add(sestra);
		myAgent().getFreeDoctors().add(lekar);

		sim.uvolniSestru(sestra.getAnimItem());
		sim.uvolniLekara(lekar.getAnimItem());

		if (msg.getAmbulanceType().equals("A")) {
			myAgent().setFreeAmbulancesA(myAgent().getFreeAmbulancesA() + 1);
		} else {
			myAgent().setFreeAmbulancesB(myAgent().getFreeAmbulancesB() + 1);
		}

		sim.odomkniVizualnuAmbulanciu(msg.getAmbulanceType(), msg.getPatient().getVisualAmbPosition());


		checkQueues();
		updateResourceUtilization();
	}

	//vygenerovane pomocou AI a zrefaktorovane pre lepsiu citatelnost a zapuzdrenie
	private void checkQueues() {
		boolean changed = true;
		MySimulation sim = (MySimulation) mySim();
		boolean isVariant2 = (sim.getVariant() == 2);

		while (changed) {
			changed = false;

			boolean mozeOsetrovat = myAgent().hasFreeNurses() &&
					myAgent().hasFreeDoctors() &&
					(!isVariant2 || myAgent().getFreeNursesCount() > 1);

			// --- 1. RAD NA OŠETRENIE: AMBULANCIA A ---
			if (mozeOsetrovat && myAgent().getFreeAmbulancesA() > 0 && !myAgent().isQueueExaminationAEmpty()) {
				MyMessage peekMsg = myAgent().peekQueueExaminationA();
				boolean blokovanyLekar = (sim.getVariant() == 1 && myAgent().getFreeDoctorsCount() == 1 && peekMsg.getPatient().getPriority() > 2);

				if (!blokovanyLekar) {
					MyMessage msg = myAgent().pollQueueExaminationA();
					pridelZdrojeOsetrenie(msg, "A");
					changed = true;
					continue;
				}
			}

			// --- 2. RAD NA OŠETRENIE: AMBULANCIA B ---
			if (mozeOsetrovat && myAgent().getFreeAmbulancesB() > 0 && !myAgent().isQueueExaminationBEmpty()) {
				MyMessage peekMsg = myAgent().peekQueueExaminationB();
				boolean blokovanyLekar = (sim.getVariant() == 1 && myAgent().getFreeDoctorsCount() == 1 && peekMsg.getPatient().getPriority() > 2);

				if (!blokovanyLekar) {
					MyMessage msg = myAgent().pollQueueExaminationB();
					pridelZdrojeOsetrenie(msg, "B");
					changed = true;
					continue;
				}
			}

			// --- 3. RAD NA VSTUP: AMBULANCIA B ---
			if (myAgent().hasFreeNurses() && myAgent().getFreeAmbulancesB() > 0 && !myAgent().isQueueEntranceEmpty()) {
				MyMessage msg = myAgent().pollQueueEntrance();
				pridelZdrojeVstup(msg);
				changed = true;
			}
		}
	}

	// --- POMOCNÁ METÓDA PRE OŠETRENIE ---
	private void pridelZdrojeOsetrenie(MyMessage msg, String typAmbulancie) {
		MySimulation sim = (MySimulation) mySim();

		myAgent().removeFromQueueExaminationA(msg);
		myAgent().removeFromQueueExaminationB(msg);

		double waitOsetrenie = mySim().currentTime() - msg.getPatient().getArrivalTimeQueueTreatment();
		msg.getPatient().addWaitingTime(waitOsetrenie);

		if (msg.getPatient().isAmbulance()) {
			myAgent().recordTreatmentWaitAmb(waitOsetrenie);
		} else {
			myAgent().recordTreatmentWaitWalkIn(waitOsetrenie);
		}

		double cakanie = mySim().currentTime() - msg.getPatient().getArrivalTime();
		if (msg.getPatient().isAmbulance()) {
			myAgent().recordWaitingTimeAmbulance(cakanie);
		} else {
			myAgent().recordWaitingTimeWalkIn(cakanie);
		}

		Nurse sestra = myAgent().pollFreeNurse();
		Doctor lekar = myAgent().pollFreeDoctor();

		if (typAmbulancie.equals("A")) {
			myAgent().decrementFreeAmbulancesA();
		} else {
			myAgent().decrementFreeAmbulancesB();
		}

		msg.setAmbulanceType(typAmbulancie);
		msg.getPatient().setStartTimeTreatment(mySim().currentTime());
		msg.getPatient().setStav("Ošetruje sa v Amb. " + typAmbulancie);

		java.awt.Point pos = sim.zamkniVizualnuAmbulanciu(typAmbulancie);
		msg.getPatient().setVisualAmbPosition(pos);
		msg.getPatient().setAssignedNurse(sestra);
		msg.getPatient().setAssignedDoctor(lekar);

		sim.obsadSestru(pos, sestra.getAnimItem());
		sim.obsadLekara(pos, lekar.getAnimItem());

		sim.log("ZDROJE: Pacient #" + msg.getPatient().getId() + " (Priorita: " + msg.getPatient().getPriority() + ") dostal Ambulanciu typu " + typAmbulancie + ".");

		updateResourceUtilization();
		response(msg);
	}

	// POMOCNÁ METÓDA PRE VSTUP
	private void pridelZdrojeVstup(MyMessage msg) {
		MySimulation sim = (MySimulation) mySim();

		double waitVstup = mySim().currentTime() - msg.getPatient().getArrivalTimeQueueExam();
		msg.getPatient().addWaitingTime(waitVstup);

		if (msg.getPatient().isAmbulance()) {
			myAgent().recordEntryWaitAmb(waitVstup);
		} else {
			myAgent().recordEntryWaitWalkIn(waitVstup);
		}

		Nurse sestra = myAgent().pollFreeNurse();
		myAgent().decrementFreeAmbulancesB();

		msg.getPatient().setStartTimeExam(mySim().currentTime());
		msg.getPatient().setStav("Vyšetruje sa na Vstup. (Amb. B)");

		java.awt.Point pos = sim.zamkniVizualnuAmbulanciu("B");
		msg.getPatient().setVisualAmbPosition(pos);
		msg.getPatient().setAssignedNurse(sestra);

		sim.obsadSestru(pos, sestra.getAnimItem());

		sim.log("ZDROJE: Pacient #" + msg.getPatient().getId() + " dostal Sestru a Amb. B na Vstupné vyšetrenie.");

		updateResourceUtilization();
		response(msg);
	}

	//meta! sender="SchedulerZahrievania", id="152", type="Finish"
	public void processFinish(MessageForm message)
	{
		myAgent().getWaitingTimeAmbulanceStat().clear();
		myAgent().getWaitingTimeWalkInStat().clear();
		myAgent().getEntryWaitAmbStat().clear();
		myAgent().getEntryWaitWalkInStat().clear();
		myAgent().getTreatmentWaitAmbStat().clear();
		myAgent().getTreatmentWaitWalkInStat().clear();

		double now = mySim().currentTime();
		myAgent().getNurseUtilizationStat().clear(now);
		myAgent().getDoctorUtilizationStat().clear(now);
		myAgent().getRoomAUtilizationStat().clear(now);
		myAgent().getRoomBUtilizationStat().clear(now);
		myAgent().getEntryQueueLengthStat().clear(now);
		myAgent().getTreatmentQueueLengthStat().clear(now);
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	public void init()
	{
	}

	@Override
	public void processMessage(MessageForm message)
	{
		switch (message.code())
		{
		case Mc.uvolniZdrojeOsetrenie:
			processUvolniZdrojeOsetrenie(message);
		break;

		case Mc.reqZdrojeVstup:
			processReqZdrojeVstup(message);
		break;

		case Mc.reqZdrojeOsetrenie:
			processReqZdrojeOsetrenie(message);
		break;

		case Mc.finish:
			processFinish(message);
		break;

		case Mc.uvolniZdrojeVstup:
			processUvolniZdrojeVstup(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentZdrojov myAgent()
	{
		return (AgentZdrojov)super.myAgent();
	}

	private void updateResourceUtilization() {
		MySimulation sim = (MySimulation) mySim();

		int busyNurses = sim.getNumNurses() - myAgent().getFreeNursesCount();
		int busyDoctors = sim.getNumDoctors() - myAgent().getFreeDoctorsCount();
		myAgent().getNurseUtilizationStat().add(busyNurses);
		myAgent().getDoctorUtilizationStat().add(busyDoctors);

		int busyAmbA = 5 - myAgent().getFreeAmbulancesA();
		int busyAmbB = 7 - myAgent().getFreeAmbulancesB();
		myAgent().getRoomAUtilizationStat().add(busyAmbA);
		myAgent().getRoomBUtilizationStat().add(busyAmbB);

		myAgent().getEntryQueueLengthStat().add(myAgent().getQueueEntranceCount());

		myAgent().getTreatmentQueueLengthStat().add(myAgent().getUniquePatientsInTreatmentQueues());
	}


}
