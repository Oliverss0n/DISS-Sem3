package agents.agentosetrenia;

import OSPABA.*;
import agents.agentosetrenia.continualassistants.*;
import simulation.*;

//meta! id="37"
public class AgentOsetrenia extends OSPABA.Agent
{
	public AgentOsetrenia(int id, Simulation mySim, Agent parent)
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
		new ManagerOsetrenia(Id.managerOsetrenia, mySim(), this);
		new ProcesOsetrovanie(Id.procesOsetrovanie, mySim(), this);
		addOwnMessage(Mc.presunNaOsetrenie);
		addOwnMessage(Mc.reqZdrojeOsetrenie);
	}
	//meta! tag="end"
}