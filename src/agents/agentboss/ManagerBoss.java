package agents.agentboss;

import OSPABA.*;
import simulation.*;

//meta! id="1"
public class ManagerBoss extends OSPABA.Manager
{
	public ManagerBoss(int id, Simulation mySim, Agent myAgent)
	{
		super(id, mySim, myAgent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication

		if (petriNet() != null)
		{
			petriNet().clear();
		}
	}

	//meta! sender="AgentOkolia", id="48", type="Notice"
	public void processNovyPacient(MessageForm message)
	{
		message.setAddressee(mySim().findAgent(Id.agentUrgentu));
		notice(message);
	}

	//meta! sender="AgentUrgentu", id="61", type="Notice"
	public void processOdchodPacienta(MessageForm message)
	{
		MyMessage msg = (MyMessage) message;
		MySimulation sim = (MySimulation) mySim();

		sim.removePatient(msg.getPatient());
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	public void init()
	{
	}

	@Override
	public void processMessage(MessageForm message)
	{
		switch (message.code())
		{
			case Mc.odchodPacienta:
				processOdchodPacienta(message);
				break;

			case Mc.novyPacient:
				processNovyPacient(message);
				break;

			default:
				processDefault(message);
				break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentBoss myAgent()
	{
		return (AgentBoss)super.myAgent();
	}

}