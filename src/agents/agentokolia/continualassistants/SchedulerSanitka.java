package agents.agentokolia.continualassistants;

import OSPABA.*;
import OSPRNG.GammaRNG;
import agents.agentokolia.*;
import entities.Patient;
import simulation.*;

//meta! id="9"
public class SchedulerSanitka extends OSPABA.Scheduler
{

	//GAMMA
	GammaRNG ambulanceArrivalsRNG = new GammaRNG(0.0, 49.86, 7.04);
	public SchedulerSanitka(int id, Simulation mySim, CommonAgent myAgent)
	{
		super(id, mySim, myAgent);
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! sender="AgentOkolia", id="10", type="Start"
	public void processStart(MessageForm message)
	{
		message.setCode(Mc.novyPacient);

		hold(1800.0, message);
	}

	//meta! userInfo="Process messages defined in code", id="0"
	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		// Tu padne tvoja správa, ktorá sa vrátila z budúcnosti (z metódy hold)

		// 1. Vytvoríme entitu pacienta (true = je to sanitka)
		Patient newPatient = new Patient(true, mySim().currentTime());

		// 2. Vytvoríme novú obálku (MyMessage), vložíme pacienta a pošleme šéfovi
		MyMessage noticeMsg = new MyMessage(mySim());
		noticeMsg.setPatient(newPatient);
		noticeMsg.setCode(Mc.novyPacient);
		noticeMsg.setAddressee(myAgent());
		notice(noticeMsg); // Odošli

		// 3. Pôvodnú obálku pošleme ZNOVA do budúcnosti (napr. o 1800 sekúnd neskôr)
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
