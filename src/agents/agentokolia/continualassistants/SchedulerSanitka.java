package agents.agentokolia.continualassistants;

import OSPABA.*;
import OSPRNG.GammaRNG;
import agents.agentokolia.*;
import entities.Patient;
import simulation.*;

//meta! id="9"
public class SchedulerSanitka extends OSPABA.Scheduler
{

	//test
	//GAMMA
	GammaRNG ambulanceArrivals = new GammaRNG(0.0, 49.86, 7.04);

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
		// 1. Vygenerujeme čas do ďalšieho príchodu
		double arrivalTime = ambulanceArrivals.sample();

		// 2. Vytvoríme pacienta (ale pozor, pacient prišiel PRÁVE TERAZ)
		Patient newPatient = new Patient(true, mySim().currentTime());

		// 3. Pošleme pacienta šéfovi cez Notice (správne z hľadiska teórie)
		MyMessage noticeMsg = new MyMessage(mySim());
		noticeMsg.setPatient(newPatient);
		noticeMsg.setCode(Mc.novyPacient);
		noticeMsg.setAddressee(myAgent());
		notice(noticeMsg);

		// 4. Pošleme kópiu štartovacej správy do budúcnosti.
		// Keďže sme jej nezmenili kód, vráti sa ako Mc.start a spustí túto metódu odznova!
		MessageForm copy = message.createCopy();
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