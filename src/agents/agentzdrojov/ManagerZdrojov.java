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
		MySimulation sim = (MySimulation) mySim();
		double warmUp = sim.getWarmUpTime();

		if (warmUp > 0.0) {
			MyMessage msg = new MyMessage(sim);
			msg.setCode(Mc.start); // Asistent sa VŽDY spúšťa kódom Mc.start
			msg.setAddressee(myAgent().findAssistant(Id.schedulerZahrievania));
			notice(msg); // Pošleme mu správu, nech začne odpočítavať
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

		// Zistíme, či je zapnutý Variant 2
		boolean isVariant2 = (sim.getVariant() == 2);

		while (changed) {
			changed = false;

			// 1. NAJPRV OŠETRENIE A
			// --- VARIANT 2 LOGIKA: Ak je zapnutý Variant 2, musíme mať voľné aspoň 2 sestry, inak Ošetrenie preskočíme ---
			if (!myAgent().getFreeNurses().isEmpty() && !myAgent().getFreeDoctors().isEmpty() && myAgent().getFreeAmbulancesA() > 0 && !myAgent().getQueueExaminationA().isEmpty() && (!isVariant2 || myAgent().getFreeNurses().size() > 1)) {

				MyMessage peekMsg = (MyMessage) myAgent().getQueueExaminationA().peek();

				// --- VARIANT 1 LOGIKA ---
				if (sim.getVariant() == 1 && myAgent().getFreeDoctors().size() == 1 && peekMsg.getPatient().getPriority() > 2) {
					// Ochrana lekára - preskakujeme
				} else {
					MyMessage msg = (MyMessage) myAgent().getQueueExaminationA().poll();
					myAgent().getQueueExaminationB().remove(msg);

					double waitOsetrenie = mySim().currentTime() - msg.getPatient().getArrivalTimeQueueTreatment();
					msg.getPatient().addWaitingTime(waitOsetrenie);


					if (msg.getPatient().isAmbulance()) {
						myAgent().getTreatmentWaitAmbStat().add(waitOsetrenie);
					} else {
						myAgent().getTreatmentWaitWalkInStat().add(waitOsetrenie);
					}

					double cakanie = mySim().currentTime() - msg.getPatient().getArrivalTime();
					if (msg.getPatient().isAmbulance()) {
						myAgent().getWaitingTimeAmbulanceStat().add(cakanie);
					} else {
						myAgent().getWaitingTimeWalkInStat().add(cakanie);
					}

					Nurse sestra = myAgent().getFreeNurses().poll();
					Doctor lekar = myAgent().getFreeDoctors().poll();
					myAgent().setFreeAmbulancesA(myAgent().getFreeAmbulancesA() - 1);

					msg.setAmbulanceType("A");
					msg.getPatient().setStartTimeTreatment(mySim().currentTime());
					msg.getPatient().setStav("Ošetruje sa v Amb. A");

					java.awt.Point pos = sim.zamkniVizualnuAmbulanciu("A");
					msg.getPatient().setVisualAmbPosition(pos);
					msg.getPatient().setAssignedNurse(sestra);
					msg.getPatient().setAssignedDoctor(lekar);

					sim.obsadSestru(pos, sestra.getAnimItem());
					sim.obsadLekara(pos, lekar.getAnimItem());

					sim.log("ZDROJE: Pacient #" + msg.getPatient().getId() + " (Priorita: " + msg.getPatient().getPriority() + ") dostal Ambulanciu typu A.");
					response(msg);

					updateResourceUtilization();
					changed = true;
					continue;
				}
			}

			// 2. POTOM OŠETRENIE B
			// --- VARIANT 2 LOGIKA: Rovnaká ochrana 1 sestry pre Ošetrenie B ---
			if (!myAgent().getFreeNurses().isEmpty() && !myAgent().getFreeDoctors().isEmpty() && myAgent().getFreeAmbulancesB() > 0 && !myAgent().getQueueExaminationB().isEmpty() && (!isVariant2 || myAgent().getFreeNurses().size() > 1)) {

				MyMessage peekMsg = (MyMessage) myAgent().getQueueExaminationB().peek();

				// --- VARIANT 1 LOGIKA ---
				if (sim.getVariant() == 1 && myAgent().getFreeDoctors().size() == 1 && peekMsg.getPatient().getPriority() > 2) {
					// Ochrana lekára - preskakujeme
				} else {
					MyMessage msg = (MyMessage) myAgent().getQueueExaminationB().poll();
					myAgent().getQueueExaminationA().remove(msg);

					double waitOsetrenie = mySim().currentTime() - msg.getPatient().getArrivalTimeQueueTreatment();
					msg.getPatient().addWaitingTime(waitOsetrenie);

					if (msg.getPatient().isAmbulance()) {
						myAgent().getTreatmentWaitAmbStat().add(waitOsetrenie);
					} else {
						myAgent().getTreatmentWaitWalkInStat().add(waitOsetrenie);
					}

					double cakanie = mySim().currentTime() - msg.getPatient().getArrivalTime();
					if (msg.getPatient().isAmbulance()) {
						myAgent().getWaitingTimeAmbulanceStat().add(cakanie);
					} else {
						myAgent().getWaitingTimeWalkInStat().add(cakanie);
					}

					Nurse sestra = myAgent().getFreeNurses().poll();
					Doctor lekar = myAgent().getFreeDoctors().poll();
					myAgent().setFreeAmbulancesB(myAgent().getFreeAmbulancesB() - 1);

					msg.setAmbulanceType("B");
					msg.getPatient().setStartTimeTreatment(mySim().currentTime());
					msg.getPatient().setStav("Ošetruje sa v Amb. B");

					java.awt.Point pos = sim.zamkniVizualnuAmbulanciu("B");
					msg.getPatient().setVisualAmbPosition(pos);
					msg.getPatient().setAssignedNurse(sestra);
					msg.getPatient().setAssignedDoctor(lekar);

					sim.obsadSestru(pos, sestra.getAnimItem());
					sim.obsadLekara(pos, lekar.getAnimItem());

					sim.log("ZDROJE: Pacient #" + msg.getPatient().getId() + " (Priorita: " + msg.getPatient().getPriority() + ") dostal Ambulanciu typu B.");
					response(msg);

					updateResourceUtilization();
					changed = true;
					continue;
				}
			}

			// 3. AŽ NAKONIEC VSTUP (Ak zostali voľné sestry a voľná Ambulancia B)
			// Vstup si kedykoľvek vezme sestru, ak nejaká voľná ostala
			if (!myAgent().getFreeNurses().isEmpty() && myAgent().getFreeAmbulancesB() > 0 && !myAgent().getQueueEntrance().isEmpty()) {
				MyMessage msg = (MyMessage) myAgent().getQueueEntrance().poll();

				double waitVstup = mySim().currentTime() - msg.getPatient().getArrivalTimeQueueExam();
				msg.getPatient().addWaitingTime(waitVstup);

				if (msg.getPatient().isAmbulance()) {
					myAgent().getEntryWaitAmbStat().add(waitVstup);
				} else {
					myAgent().getEntryWaitWalkInStat().add(waitVstup);
				}

				Nurse sestra = myAgent().getFreeNurses().poll();
				myAgent().setFreeAmbulancesB(myAgent().getFreeAmbulancesB() - 1);

				msg.getPatient().setStartTimeExam(mySim().currentTime());
				msg.getPatient().setStav("Vyšetruje sa na Vstupe (Amb. B)");

				java.awt.Point pos = sim.zamkniVizualnuAmbulanciu("B");
				msg.getPatient().setVisualAmbPosition(pos);
				msg.getPatient().setAssignedNurse(sestra);

				sim.obsadSestru(pos, sestra.getAnimItem());

				sim.log("ZDROJE: Pacient #" + msg.getPatient().getId() + " dostal Sestru a Amb. B na Vstupné vyšetrenie.");
				response(msg);

				updateResourceUtilization();
				changed = true;
			}
		}
	}
	//meta! sender="SchedulerZahrievania", id="152", type="Finish"
	public void processFinish(MessageForm message)
	{
		processKoniecZahrievania(message);
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

		// Vyťaženie personálu
		int busyNurses = sim.getNumNurses() - myAgent().getFreeNurses().size();
		int busyDoctors = sim.getNumDoctors() - myAgent().getFreeDoctors().size();
		myAgent().getNurseUtilizationStat().add(busyNurses);
		myAgent().getDoctorUtilizationStat().add(busyDoctors);

		// Vyťaženie ambulancií
		int busyAmbA = 5 - myAgent().getFreeAmbulancesA();
		int busyAmbB = 7 - myAgent().getFreeAmbulancesB();
		myAgent().getRoomAUtilizationStat().add(busyAmbA);
		myAgent().getRoomBUtilizationStat().add(busyAmbB);

		// Rad na Vstup
		myAgent().getEntryQueueLengthStat().add(myAgent().getQueueEntrance().size());

		// Rad na Ošetrenie (Unikátni pacienti)
		//vygenerovanie pomocou AI - sluzi na unikatne zaratanie pacienta, kedze je v 2 radoch naraz
		java.util.HashSet<Integer> uniqueWaitingPatients = new java.util.HashSet<>();
		for (Object obj : myAgent().getQueueExaminationA()) {
			uniqueWaitingPatients.add(((MyMessage) obj).getPatient().getId());
		}
		for (Object obj : myAgent().getQueueExaminationB()) {
			uniqueWaitingPatients.add(((MyMessage) obj).getPatient().getId());
		}

		myAgent().getTreatmentQueueLengthStat().add(uniqueWaitingPatients.size());
		// TU už nič ďalšie netreba, tú duplicitu vymaž.
	}

	private void processKoniecZahrievania(MessageForm message) {
		myAgent().getWaitingTimeAmbulanceStat().clear();
		myAgent().getWaitingTimeWalkInStat().clear();
		myAgent().getEntryWaitAmbStat().clear();        // NOVÉ
		myAgent().getEntryWaitWalkInStat().clear();     // NOVÉ
		myAgent().getTreatmentWaitAmbStat().clear();    // NOVÉ
		myAgent().getTreatmentWaitWalkInStat().clear(); // NOVÉ

		// 2. Časovo-vážené štatistiky (TimeStat) - vyžadujú currentTime()
		double now = mySim().currentTime();
		myAgent().getNurseUtilizationStat().clear(now);
		myAgent().getDoctorUtilizationStat().clear(now);
		myAgent().getRoomAUtilizationStat().clear(now);
		myAgent().getRoomBUtilizationStat().clear(now);
		myAgent().getEntryQueueLengthStat().clear(now);     // NOVÉ
		myAgent().getTreatmentQueueLengthStat().clear(now); // NOVÉ
	}

}
