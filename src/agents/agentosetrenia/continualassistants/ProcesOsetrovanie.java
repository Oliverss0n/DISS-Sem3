package agents.agentosetrenia.continualassistants;

import OSPABA.*;
import agents.agentosetrenia.*;
import entities.Patient;
import simulation.*;

//meta! id="87"
public class ProcesOsetrovanie extends OSPABA.Process
{
	public ProcesOsetrovanie(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! sender="AgentOsetrenia", id="88", type="Start"
	public void processStart(MessageForm message)
	{
		MySimulation sim = (MySimulation) mySim();
		MyMessage msg = (MyMessage) message;
		Patient pacient = msg.getPatient();

		double duration = 0;

		if (pacient.isAmbulance()) {
			duration = myAgent().getAmbulanceExamDurationGen().sample();
		} else {
			duration = myAgent().getWalkInExamDurationGen().sample();
		}

		if (pacient.getAnimItem() != null && pacient.getVisualAmbPosition() != null) {
			pacient.getAnimItem().moveTo(sim.currentTime(), 0.5, pacient.getVisualAmbPosition().x , pacient.getVisualAmbPosition().y);
		}

		// --- PRIDAJ TÚTO POISTKU PRE FARBU PERSONÁLU ---
		if (sim.animatorExists()) {
			if (pacient.getAssignedDoctor() != null) pacient.getAssignedDoctor().getAnimItem().setColor(java.awt.Color.RED);
			if (pacient.getAssignedNurse() != null) pacient.getAssignedNurse().getAnimItem().setColor(java.awt.Color.RED);
		}

		sim.log("OŠETRENIE: Pacient #" + pacient.getId() + " sa začal ošetrovať v ambulancii " + msg.getAmbulanceType() + ". Odhadovaný čas: " + String.format("%.1f", duration * 60) + " s.");
		message.setCode(Mc.koniecZdrzania);
		hold(duration * 60.0, message);
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
			/*vygenerovala AI, pretoze mi nemizli*/
			MySimulation sim = (MySimulation) mySim();
			entities.Patient pacient = ((MyMessage) message).getPatient();
			if (pacient.getAnimItem() != null) {
				// Pošleme pacienta preč z mapy
				pacient.getAnimItem().moveTo(sim.currentTime(), 0.5, 1000, 800);
			}
			assistantFinished(message);
		break;
		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentOsetrenia myAgent()
	{
		return (AgentOsetrenia)super.myAgent();
	}

}