package agents.agentosetrenia.continualassistants;

import OSPABA.*;
import agents.agentosetrenia.*;
import entities.Patient;
import simulation.*;
import OSPABA.Process;

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
		MyMessage msg = (MyMessage) message;
		Patient pacient = msg.getPatient();

		double duration = 0;

		if (pacient.isAmbulance()) {
			duration = myAgent().getAmbulanceExamDurationGen().sample();
		} else {
			duration = myAgent().getWalkInExamDurationGen().sample();
		}

		// Tesne pred: message.setCode(Mc.koniecZdrzania);
		((MySimulation)mySim()).log("OŠETRENIE: Pacient #" + pacient.getId() + " sa začal ošetrovať v ambulancii " + msg.getAmbulanceType() + ". Odhadovaný čas: " + String.format("%.1f", duration * 60) + " s.");
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