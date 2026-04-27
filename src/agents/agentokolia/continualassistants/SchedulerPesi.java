package agents.agentokolia.continualassistants;

import Distributions.ExponentialDist;
import OSPABA.*;
import OSPRNG.ExponentialRNG;
import OSPRNG.WeibullRNG;
import agents.agentokolia.*;
import entities.Patient;
import simulation.*;

//meta! id="7"
public class SchedulerPesi extends OSPABA.Scheduler
{

	public SchedulerPesi(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! sender="AgentOkolia", id="8", type="Start"
	public void processStart(MessageForm message)
	{
		MessageForm copy = message.createCopy();

		double arrivalTime = myAgent().getWalkArrivals().sample();
		Patient newPatient = new Patient(false, mySim().currentTime());

		MySimulation sim = (MySimulation) mySim();
		sim.addPatient(newPatient);

		MyMessage noticeMsg = new MyMessage(mySim());
		noticeMsg.setPatient(newPatient);
		noticeMsg.setCode(Mc.novyPacient);
		noticeMsg.setAddressee(myAgent());
		notice(noticeMsg);


		hold(arrivalTime, copy);
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

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentOkolia myAgent()
	{
		return (AgentOkolia)super.myAgent();
	}

}