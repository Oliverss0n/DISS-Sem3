package agents.agentokolia.continualassistants;

import OSPABA.*;
import OSPRNG.ExponentialRNG;
import OSPRNG.WeibullRNG;
import agents.agentokolia.*;
import entities.Patient;
import simulation.*;

//meta! id="7"
public class SchedulerPesi extends OSPABA.Scheduler
{

	//Exponencialne
	private ExponentialRNG walkInArrivalsRNG = new ExponentialRNG(572.6);
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
		message.setCode(Mc.novyPacient);
		hold(1800.0, message);

	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		// 1. Vytvoríme entitu pacienta (FALSE = prišiel pešo)
		Patient newPatient = new Patient(false, mySim().currentTime());

		// 2. Vytvoríme novú obálku (MyMessage), vložíme pacienta a pošleme šéfovi
		MyMessage noticeMsg = new MyMessage(mySim());
		noticeMsg.setPatient(newPatient);
		noticeMsg.setCode(Mc.novyPacient);
		noticeMsg.setAddressee(myAgent());
		notice(noticeMsg); // Odošli

		// 3. Pôvodnú obálku pošleme ZNOVA do budúcnosti
		hold(1800.0, message);
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
