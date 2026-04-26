package agents.agenturgentu.continualassistants;

import OSPABA.*;
import entities.Patient;
import simulation.*;
import agents.agenturgentu.*;
import OSPABA.Process;

//meta! id="134"
public class ProcessPresunVstup extends OSPABA.Process
{
	public ProcessPresunVstup(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! sender="AgentUrgentu", id="135", type="Start"
	public void processStart(MessageForm message) {
		MyMessage msg = (MyMessage) message;
		Patient patient = msg.getPatient();
		patient.setStav("Kráča k recepcii");


		// Žiadny IF, sme vo Vstupe, takže generujeme len čas pre Vstup
		double travelTime = patient.isAmbulance()
				? myAgent().getMoveEntranceAmbulanceGen().sample()
				: myAgent().getMoveEntranceWalkInGen().sample();

		((MySimulation)mySim()).log("CHODBA VSTUP: Pacient #" + patient.getId() + " sa presúva na vstup. Čas: " + String.format("%.1f", travelTime) + " s.");

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
