package agents.agenturgentu.continualassistants;

import OSPABA.*;
import entities.Patient;
import simulation.*;
import agents.agenturgentu.*;
import OSPABA.Process;

//meta! id="136"
public class ProcessPresunOdchod extends OSPABA.Process
{
	public ProcessPresunOdchod(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! sender="AgentUrgentu", id="137", type="Start"
	public void processStart(MessageForm message)
	{
		// 1. Pretypujeme správu na MyMessage
		MyMessage msg = (MyMessage) message;

		// 2. Vytiahneme pacienta zo správy (teraz už premenná 'patient' bude existovať)
		Patient patient = msg.getPatient();
		patient.setStav("Kráča k východu");
		// 3. Získame čas presunu
		double travelTime = myAgent().getMoveExitGen().sample();

		// 4. Teraz už logovanie bude fungovať
		((MySimulation)mySim()).log("🚶 CHODBA ODCHOD: Pacient #" + patient.getId() + " kráča k východu. Čas: " + String.format("%.1f", travelTime) + " s.");

		// 5. Nastavíme kód pre návrat a podržíme správu
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
