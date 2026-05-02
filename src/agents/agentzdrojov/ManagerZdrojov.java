package agents.agentzdrojov;

import OSPABA.*;
import OSPStat.Stat;
import entities.Doctor;
import entities.Nurse;
import simulation.*;

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
		checkQueues();
	}

	//meta! sender="AgentUrgentu", id="112", type="Notice"
	public void processUvolniZdrojeVstup(MessageForm message)
	{
		MyMessage msg = (MyMessage) message;
		MySimulation sim = (MySimulation) mySim();

		// Získame sestru, ktorá vyšetrovala pacienta
		Nurse sestra = msg.getPatient().getAssignedNurse();

		// Sestra skončila, vraciame ju do frontu voľných
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

		// Získame konkrétny personál od pacienta
		Nurse sestra = msg.getPatient().getAssignedNurse();
		Doctor lekar = msg.getPatient().getAssignedDoctor();

		// Personál vraciame do frontov voľných
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

	//vygenerovane pomocou AI
	//vygenerovane pomocou AI
	private void checkQueues() {
		boolean changed = true;
		MySimulation sim = (MySimulation) mySim();

		while (changed) {
			changed = false;

			// 1. NAJPRV OŠETRENIE A
			if (!myAgent().getFreeNurses().isEmpty() && !myAgent().getFreeDoctors().isEmpty() && myAgent().getFreeAmbulancesA() > 0 && !myAgent().getQueueExaminationA().isEmpty()) {
				MyMessage msg = (MyMessage) myAgent().getQueueExaminationA().poll();
				myAgent().getQueueExaminationB().remove(msg);

				Nurse sestra = myAgent().getFreeNurses().poll();
				Doctor lekar = myAgent().getFreeDoctors().poll();
				myAgent().setFreeAmbulancesA(myAgent().getFreeAmbulancesA() - 1);

				msg.setAmbulanceType("A");
				msg.getPatient().setStartTimeTreatment(mySim().currentTime());
				msg.getPatient().setStav("Ošetruje sa v Amb. A");

				// --- ZBER ŠTATISTIKY ČAKANIA ---
				// --- ZBER ŠTATISTIKY ČAKANIA ---
				double waitingTime = mySim().currentTime() - msg.getPatient().getArrivalTimeBuilding();
				if (msg.getPatient().isAmbulance()) {
					myAgent().getWaitingTimeAmbulanceStat().add(waitingTime);
				} else {
					myAgent().getWaitingTimeWalkInStat().add(waitingTime);
				}
				// -------------------------------
				// -------------------------------

				java.awt.Point pos = sim.zamkniVizualnuAmbulanciu("A");
				msg.getPatient().setVisualAmbPosition(pos);

				msg.getPatient().setAssignedNurse(sestra);
				msg.getPatient().setAssignedDoctor(lekar);

				sim.obsadSestru(pos, sestra.getAnimItem());
				sim.obsadLekara(pos, lekar.getAnimItem());

				sim.log("ZDROJE: Pacient #" + msg.getPatient().getId() + " (Priorita: " + msg.getPatient().getPriority() + ") dostal Ambulanciu typu A. Čakal: " + String.format("%.1f", waitingTime) + "s.");
				response(msg);

				updateResourceUtilization(); // Aktualizácia vyťaženia
				changed = true;
				continue;
			}

			// 2. POTOM OŠETRENIE B
			if (!myAgent().getFreeNurses().isEmpty() && !myAgent().getFreeDoctors().isEmpty() && myAgent().getFreeAmbulancesB() > 0 && !myAgent().getQueueExaminationB().isEmpty()) {
				MyMessage msg = (MyMessage) myAgent().getQueueExaminationB().poll();
				myAgent().getQueueExaminationA().remove(msg);

				Nurse sestra = myAgent().getFreeNurses().poll();
				Doctor lekar = myAgent().getFreeDoctors().poll();
				myAgent().setFreeAmbulancesB(myAgent().getFreeAmbulancesB() - 1);

				msg.setAmbulanceType("B");
				msg.getPatient().setStartTimeTreatment(mySim().currentTime());
				msg.getPatient().setStav("Ošetruje sa v Amb. B");

				// --- ZBER ŠTATISTIKY ČAKANIA ---
				// --- ZBER ŠTATISTIKY ČAKANIA ---
				double waitingTime = mySim().currentTime() - msg.getPatient().getArrivalTimeBuilding();
				if (msg.getPatient().isAmbulance()) {
					myAgent().getWaitingTimeAmbulanceStat().add(waitingTime);
				} else {
					myAgent().getWaitingTimeWalkInStat().add(waitingTime);
				}
				// -------------------------------
				// -------------------------------

				java.awt.Point pos = sim.zamkniVizualnuAmbulanciu("B");
				msg.getPatient().setVisualAmbPosition(pos);

				msg.getPatient().setAssignedNurse(sestra);
				msg.getPatient().setAssignedDoctor(lekar);

				sim.obsadSestru(pos, sestra.getAnimItem());
				sim.obsadLekara(pos, lekar.getAnimItem());

				sim.log("ZDROJE: Pacient #" + msg.getPatient().getId() + " (Priorita: " + msg.getPatient().getPriority() + ") dostal Ambulanciu typu B. Čakal: " + String.format("%.1f", waitingTime) + "s.");
				response(msg);

				updateResourceUtilization(); // Aktualizácia vyťaženia
				changed = true;
				continue;
			}

			// 3. AŽ NAKONIEC VSTUP (Ak zostali voľné sestry)
			if (!myAgent().getFreeNurses().isEmpty() && myAgent().getFreeAmbulancesB() > 0 && !myAgent().getQueueEntrance().isEmpty()) {
				MyMessage msg = (MyMessage) myAgent().getQueueEntrance().poll();

				Nurse sestra = myAgent().getFreeNurses().poll();
				myAgent().setFreeAmbulancesB(myAgent().getFreeAmbulancesB() - 1);

				msg.getPatient().setStartTimeExam(mySim().currentTime());
				msg.getPatient().setStav("Vyšetruje sa na Vstupnej prehliadke");

				java.awt.Point pos = sim.zamkniVizualnuAmbulanciu("B");
				msg.getPatient().setVisualAmbPosition(pos);

				msg.getPatient().setAssignedNurse(sestra);

				sim.obsadSestru(pos, sestra.getAnimItem());

				sim.log("ZDROJE: Pacient #" + msg.getPatient().getId() + " dostal Sestru na Vstupné vyšetrenie.");
				response(msg);

				updateResourceUtilization(); // Aktualizácia vyťaženia
				changed = true;
			}
		}
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
			case Mc.reqZdrojeOsetrenie:
				processReqZdrojeOsetrenie(message);
				break;

			case Mc.uvolniZdrojeVstup:
				processUvolniZdrojeVstup(message);
				break;

			case Mc.reqZdrojeVstup:
				processReqZdrojeVstup(message);
				break;

			case Mc.uvolniZdrojeOsetrenie:
				processUvolniZdrojeOsetrenie(message);
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

		// Výpočet
		int busyNurses = sim.getNumNurses() - myAgent().getFreeNurses().size();
		int busyDoctors = sim.getNumDoctors() - myAgent().getFreeDoctors().size();

		// Zápis do lokálnej TimeStat štatistiky vnútri Agenta
		myAgent().getNurseUtilizationStat().add(busyNurses);
		myAgent().getDoctorUtilizationStat().add(busyDoctors);
	}

}