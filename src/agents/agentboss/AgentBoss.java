package agents.agentboss;

import OSPABA.*;
import simulation.*;

//meta! id="1"
public class AgentBoss extends OSPABA.Agent
{
	public AgentBoss(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerBoss(Id.managerBoss, mySim(), this);
		addOwnMessage(Mc.novyPacient);
		addOwnMessage(Mc.odchodPacienta);
	}
	//meta! tag="end"
}
