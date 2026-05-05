package agents.agenturgentu.continualassistants;

import OSPABA.*;
import entities.Patient;
import simulation.*;
import agents.agenturgentu.*;

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
		MySimulation sim = (MySimulation) mySim();

		patient.setStav("Kráča k recepcii");

		double travelTime = patient.isAmbulance()
				? myAgent().getMoveEntranceAmbulanceGen().sample()
				: myAgent().getMoveEntranceWalkInGen().sample();


		sim.log("CHODBA VSTUP: Pacient #" + patient.getId() + " sa presúva na vstup. Čas: " + String.format("%.1f", travelTime) + " s.");

		//anim
		if (patient.getAnimItem() != null) {
			int minX = 100;  int maxX = 900;
			int minY = 155; int maxY = 400;

			double randX = minX + sim.getGenSeed().nextDouble() * (maxX - minX);
			double randY = minY + sim.getGenSeed().nextDouble() * (maxY - minY);

			patient.getAnimItem().moveTo(sim.currentTime(), travelTime, randX, randY);
		}
		//
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