package agents.agenturgentu.continualassistants;

import OSPABA.*;
import entities.Patient;
import simulation.*;
import agents.agenturgentu.*;

//meta! id="138"
public class ProcessPresunOsetrenie extends OSPABA.Process
{
	public ProcessPresunOsetrenie(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! sender="AgentUrgentu", id="139", type="Start"
	public void processStart(MessageForm message)
	{
		MySimulation sim = (MySimulation) mySim();
		MyMessage msg = (MyMessage) message;
		Patient patient = msg.getPatient();
		patient.setStav("Kráča k ambulanciám");
		double travelTime = myAgent().getMoveBetweenAmbulancesGen().sample();

		sim.log("CHODBA OŠETRENIE: Pacient #" + patient.getId() + " sa presúva k ambulancii. Čas: " + String.format("%.1f", travelTime) + " s.");
		if (patient.getAnimItem() != null) {
			// Vygenerovanie náhodnej pozície niekde v strednej chodbe
			double randX = 300 + sim.getGenSeed().nextDouble() * 400;
			double randY = 200 + sim.getGenSeed().nextDouble() * 100;

			patient.getAnimItem().moveTo(sim.currentTime(), travelTime, randX, randY);
		}
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
