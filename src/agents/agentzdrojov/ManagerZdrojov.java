package agents.agentzdrojov;

import OSPABA.*;
import OSPStat.Stat;
import simulation.*;

import java.util.LinkedList;
import java.util.Queue;

//meta! id="41"
public class ManagerZdrojov extends OSPABA.Manager
{

	private int freeNurses;
	private int freeDoctors;
	private int freeAmbulancesA;
	private int freeAmbulancesB;

	private Queue<MessageForm> queueEntrance;
	private Queue<MessageForm> queueExaminationA;
	private Queue<MessageForm> queueExaminationB;

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

		freeAmbulancesA = 5;
		freeAmbulancesB = 7;

		this.freeNurses = 5;
		this.freeDoctors = 5;
		this.queueEntrance = new LinkedList<>();
		this.queueExaminationA = new LinkedList<>();
		this.queueExaminationB = new LinkedList<>();
	}

	//meta! sender="AgentUrgentu", id="110", type="Request"
	public void processReqZdrojeOsetrenie(MessageForm message)
	{
		// TODO: Tu si neskôr prečítaš prioritu z pacienta vo vnútri správy
		// MyMessage msg = (MyMessage) message;
		// int priorita = msg.getPacient().getPriorita();

		int priorita = 3; // Zatiaľ na tvrdo pre testovanie, kým nemáme MyMessage

		if (priorita == 1 || priorita == 2) {
			this.queueExaminationA.add(message);
		}
		else if (priorita == 5) {
			this.queueExaminationB.add(message);
		}
		else if (priorita == 3 || priorita == 4) {
			// Ide do oboch radov!
			this.queueExaminationA.add(message);
			this.queueExaminationB.add(message);
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
		this.queueEntrance.add(message);
		checkQueues();
	}


	//meta! sender="AgentUrgentu", id="112", type="Notice"
	public void processUvolniZdrojeVstup(MessageForm message)
	{
		this.freeNurses++;
		this.freeAmbulancesB++;
		checkQueues();
	}

	//meta! sender="AgentUrgentu", id="113", type="Notice"
	public void processUvolniZdrojeOsetrenie(MessageForm message)
	{
		freeNurses++;
		this.freeNurses++;

		// TODO: Tu musíme zistiť, či nám pacient vracia A alebo B
		// MyMessage msg = (MyMessage) message;
		// if (msg.getDruhAmbulancie().equals("A")) { this.freeAmbulancesA++; }
		// else { this.freeAmbulancesB++; }

		checkQueues();
	}

	//navrhnute pomocou AI
	// --- 6. SRDCE ROZDEĽOVANIA (TVOJA METÓDA) ---
	private void checkQueues() {
		boolean changed = true;

		// Cyklus beží, kým sa niekto úspešne nepridelí k zdroju
		while (changed) {
			changed = false;

			// A) SKONTROLUJEME VSTUP (Potrebuje: Sestra + Ambulancia B)
			if (this.freeNurses > 0 && this.freeAmbulancesB > 0 && !this.queueEntrance.isEmpty()) {
				MessageForm msg = this.queueEntrance.poll();

				this.freeNurses--;
				this.freeAmbulancesB--;

				response(msg);
				changed = true;
				continue; // Začneme cyklus odznova, lebo sa zmenil stav zdrojov
			}

			// B) SKONTROLUJEME OŠETRENIE A (Potrebuje: Sestra + Lekár + Ambulancia A)
			if (this.freeNurses > 0 && this.freeDoctors > 0 && this.freeAmbulancesA > 0 && !this.queueExaminationA.isEmpty()) {
				MessageForm msg = this.queueExaminationA.poll();

				// BEZPEČNOSTNÁ POISTKA: Ak bol aj v B-čku, vymaž ho odtiaľ!
				this.queueExaminationB.remove(msg);

				this.freeNurses--;
				this.freeDoctors--;
				this.freeAmbulancesA--;

				// TODO: Zapísať do obálky, že dostal A-čko
				// ((MyMessage)msg).setDruhAmbulancie("A");

				response(msg);
				changed = true;
				continue;
			}

			// C) SKONTROLUJEME OŠETRENIE B (Potrebuje: Sestra + Lekár + Ambulancia B)
			if (this.freeNurses > 0 && this.freeDoctors > 0 && this.freeAmbulancesB > 0 && !this.queueExaminationB.isEmpty()) {
				MessageForm msg = this.queueExaminationB.poll();

				// BEZPEČNOSTNÁ POISTKA: Ak bol aj v A-čku, vymaž ho odtiaľ!
				this.queueExaminationA.remove(msg);

				this.freeNurses--;
				this.freeDoctors--;
				this.freeAmbulancesB--;

				// TODO: Zapísať do obálky, že dostal B-čko
				// ((MyMessage)msg).setDruhAmbulancie("B");

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
		case Mc.uvolniZdrojeVstup:
			processUvolniZdrojeVstup(message);
		break;

		case Mc.uvolniZdrojeOsetrenie:
			processUvolniZdrojeOsetrenie(message);
		break;

		case Mc.reqZdrojeOsetrenie:
			processReqZdrojeOsetrenie(message);
		break;

		case Mc.reqZdrojeVstup:
			processReqZdrojeVstup(message);
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