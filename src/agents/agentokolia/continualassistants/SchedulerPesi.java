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
		// 1. Vygenerujeme čas, kedy príde ďalší peší pacient
		double arrivalTime = myAgent().getWalkArrivals().sample(); // [cite: 308, 411]

		// 2. Vytvoríme entitu pacienta (false = prišiel pešo)
		Patient newPatient = new Patient(false, mySim().currentTime());

		((MySimulation)mySim()).addPatient(newPatient);

		//System.out.println(">>> DEBUG: Vygenerovaný peší pacient ID: " + newPatient.getId() + " v čase " + mySim().currentTime());
		// 3. Oznámime manažérovi (AgentOkolia), že niekto prišiel
		MyMessage noticeMsg = new MyMessage(mySim());
		noticeMsg.setPatient(newPatient);
		noticeMsg.setCode(Mc.novyPacient);
		noticeMsg.setAddressee(myAgent());
		notice(noticeMsg); //

		// 4. Urobíme kópiu štartovacej správy a pošleme ju do "budúcnosti"
		// Keďže jej nemeníme kód, vráti sa ako Mc.start a znova spustí túto metódu
		MessageForm copy = message.createCopy();
		hold(arrivalTime, copy); // [cite: 320]
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