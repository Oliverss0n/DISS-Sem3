package agents.agentvstupnehovystrenia.continualassistants;

import OSPABA.*;
import entities.Patient;
import simulation.*;
import agents.agentvstupnehovystrenia.*;

//meta! id="83"
public class ProcesVstup extends OSPABA.Process
{
	public ProcesVstup(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! sender="AgentVstupnehoVystrenia", id="84", type="Start"
	public void processStart(MessageForm message)
	{
		MyMessage msg = (MyMessage) message;

		Patient patient = msg.getPatient();
		MySimulation sim = (MySimulation) mySim();
		double duration = 0;


		if (patient.isAmbulance()) {
			duration = myAgent().getAmbualanceEntranceExamDurationGen().sample();
		} else {
			duration = myAgent().getWalkInEntranceExamDurationGen().sample();
		}
		//anim
		if (patient.getAnimItem() != null && patient.getVisualAmbPosition() != null) {
			patient.getAnimItem().moveTo(mySim().currentTime(), duration,
					patient.getVisualAmbPosition().x,
					patient.getVisualAmbPosition().y);
		}
		//
		message.setCode(Mc.koniecZdrzania);
		hold(duration * 60.0, message); //v zadani v minutach
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

			case Mc.koniecZdrzania: assistantFinished(message); break;
			default:
				processDefault(message);
				break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentVstupnehoVystrenia myAgent()
	{
		return (AgentVstupnehoVystrenia)super.myAgent();
	}

}