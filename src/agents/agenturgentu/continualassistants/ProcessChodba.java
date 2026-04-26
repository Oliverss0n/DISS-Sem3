package agents.agenturgentu.continualassistants;

import OSPABA.*;
import entities.Patient;
import simulation.*;
import agents.agenturgentu.*;
import OSPABA.Process;

//meta! id="93"
public class ProcessChodba extends OSPABA.Process
{
	public ProcessChodba(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! sender="AgentUrgentu", id="94", type="Start"
	public void processStart(MessageForm message)
	{
		MyMessage msg = (MyMessage) message;

		// STOPOVACÍ LOG 1: Prišiel na chodbu
		((MySimulation)mySim()).log("1. Chodba START | Reason (dôvod): " + msg.getReason());

		Patient patient = msg.getPatient();
		double travelTime = 0;

		// TUTO MUSÍ BYŤ getReason(), nie message.code()!
		if (msg.getReason() == Mc.novyPacient) {
			if (patient.isAmbulance()) {
				travelTime = myAgent().getMoveEntranceAmbulanceGen().sample();
			} else {
				travelTime = myAgent().getMoveEntranceWalkInGen().sample();
			}
		}
		else if (msg.getReason() == Mc.presunNaOsetrenie) {
			travelTime = myAgent().getMoveBetweenAmbulancesGen().sample();
		}
		else if (msg.getReason() == Mc.odchodPacienta) {
			travelTime = myAgent().getMoveExitGen().sample();
		}

		// DEBUG VÝPIS S ID PACIENTA
		// TENTO KÓD DAJ TESNE PRED: message.setCode(Mc.koniecZdrzania);
		String typ = patient.isAmbulance() ? "SANITKA" : "PEŠÍ";
		String prio = patient.getPriority() == -1 ? "Neurčená" : String.valueOf(patient.getPriority());

		((MySimulation)mySim()).log("CHODBA: Pacient #" + patient.getId() + " (" + typ + ", Priorita: " + prio + ") vstúpil na chodbu. Čas presunu: " + String.format("%.1f", travelTime) + " s.");
		// ČISTÉ RIEŠENIE:
		message.setCode(Mc.koniecZdrzania);
		hold(travelTime, message);
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	@Override
	public void processMessage(MessageForm message)
	{
		switch (message.code())
		{
		case Mc.start:
			processStart(message);
		break;
			// ZACHYTENIE ČISTÉHO KÓDU:
			case Mc.koniecZdrzania:
				assistantFinished(message);
				break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentUrgentu myAgent()
	{
		return (AgentUrgentu)super.myAgent();
	}

}