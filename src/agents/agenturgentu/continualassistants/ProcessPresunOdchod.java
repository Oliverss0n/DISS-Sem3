package agents.agenturgentu.continualassistants;

import OSPABA.*;
import entities.Patient;
import simulation.*;
import agents.agenturgentu.*;

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
		MySimulation sim = (MySimulation) mySim();
		MyMessage msg = (MyMessage) message;

		Patient patient = msg.getPatient();
		patient.setStav("Kráča k východu");
		double travelTime = myAgent().getMoveExitGen().sample();

		sim.log("CHODBA ODCHOD: Pacient #" + patient.getId() + " kráča k východu. Čas: " + String.format("%.1f", travelTime) + " s.");

		message.setCode(Mc.koniecZdrzania);
		hold(travelTime, message);
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
			case Mc.koniecZdrzania:
				assistantFinished(message);
				break;
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