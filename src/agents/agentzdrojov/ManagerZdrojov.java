package agents.agentzdrojov;

import OSPABA.*;
import OSPStat.Stat;
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
		// Setup component for the next replication

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

		// TOTO SME PRIDALI PRE GUI:
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

		// TOTO SME PRIDALI PRE GUI:
		msg.getPatient().setStav("Čaká v rade na Vstup");

		myAgent().getQueueEntrance().add(msg);
		checkQueues();
	}


	//meta! sender="AgentUrgentu", id="112", type="Notice"
	public void processUvolniZdrojeVstup(MessageForm message)
	{
		myAgent().setFreeNurses(myAgent().getFreeNurses() + 1);
		myAgent().setFreeAmbulancesB(myAgent().getFreeAmbulancesB() + 1);
		checkQueues();
	}

	//meta! sender="AgentUrgentu", id="113", type="Notice"
	public void processUvolniZdrojeOsetrenie(MessageForm message)
	{
		MyMessage msg = (MyMessage) message;

		// Vrátime sestru a LEKÁRA (tento predtým chýbal)
		myAgent().setFreeNurses(myAgent().getFreeNurses() + 1);
		myAgent().setFreeDoctors(myAgent().getFreeDoctors() + 1);

		// Zistíme, či nám pacient vracia A alebo B
		if (msg.getAmbulanceType().equals("A")) {
			myAgent().setFreeAmbulancesA(myAgent().getFreeAmbulancesA() + 1);
		} else {
			myAgent().setFreeAmbulancesB(myAgent().getFreeAmbulancesB() + 1);
		}

		checkQueues();
	}

	//navrhnute pomocou AI
	// --- 6. SRDCE ROZDEĽOVANIA (TVOJA METÓDA) ---
	private void checkQueues() {
		boolean changed = true;

		while (changed) {
			changed = false;

			// A) SKONTROLUJEME VSTUP
			if (myAgent().getFreeNurses() > 0 && myAgent().getFreeAmbulancesB() > 0 && !myAgent().getQueueEntrance().isEmpty()) {
				MyMessage msg = (MyMessage) myAgent().getQueueEntrance().poll();

				myAgent().setFreeNurses(myAgent().getFreeNurses() - 1);
				myAgent().setFreeAmbulancesB(myAgent().getFreeAmbulancesB() - 1);

				msg.getPatient().setStartTimeExam(mySim().currentTime());

				// TOTO SME PRIDALI PRE GUI:
				msg.getPatient().setStav("Vyšetruje sa na Vstupe");

				((MySimulation)mySim()).log("ZDROJE: Pacient #" + msg.getPatient().getId() + " dostal Sestru na Vstupné vyšetrenie.");
				response(msg);
				changed = true;
				continue;
			}

			// B) SKONTROLUJEME OŠETRENIE A
			if (myAgent().getFreeNurses() > 0 && myAgent().getFreeDoctors() > 0 && myAgent().getFreeAmbulancesA() > 0 && !myAgent().getQueueExaminationA().isEmpty()) {
				MyMessage msg = (MyMessage) myAgent().getQueueExaminationA().poll();

				myAgent().getQueueExaminationB().remove(msg);

				myAgent().setFreeNurses(myAgent().getFreeNurses() - 1);
				myAgent().setFreeDoctors(myAgent().getFreeDoctors() - 1);
				myAgent().setFreeAmbulancesA(myAgent().getFreeAmbulancesA() - 1);

				msg.setAmbulanceType("A");
				msg.getPatient().setStartTimeTreatment(mySim().currentTime());

				// TOTO SME PRIDALI PRE GUI:
				msg.getPatient().setStav("Ošetruje sa v Amb. A");

				((MySimulation)mySim()).log("ZDROJE: Pacient #" + msg.getPatient().getId() + " (Priorita: " + msg.getPatient().getPriority() + ") dostal Ambulanciu typu A.");
				response(msg);
				changed = true;
				continue;
			}

			// C) SKONTROLUJEME OŠETRENIE B
			if (myAgent().getFreeNurses() > 0 && myAgent().getFreeDoctors() > 0 && myAgent().getFreeAmbulancesB() > 0 && !myAgent().getQueueExaminationB().isEmpty()) {
				MyMessage msg = (MyMessage) myAgent().getQueueExaminationB().poll();

				myAgent().getQueueExaminationA().remove(msg);

				myAgent().setFreeNurses(myAgent().getFreeNurses() - 1);
				myAgent().setFreeDoctors(myAgent().getFreeDoctors() - 1);
				myAgent().setFreeAmbulancesB(myAgent().getFreeAmbulancesB() - 1);

				msg.setAmbulanceType("B");
				msg.getPatient().setStartTimeTreatment(mySim().currentTime());

				// TOTO SME PRIDALI PRE GUI:
				msg.getPatient().setStav("Ošetruje sa v Amb. B");

				((MySimulation)mySim()).log("ZDROJE: Pacient #" + msg.getPatient().getId() + " (Priorita: " + msg.getPatient().getPriority() + ") dostal Ambulanciu typu B.");
				response(msg);
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

}