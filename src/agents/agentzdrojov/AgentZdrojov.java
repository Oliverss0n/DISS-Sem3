package agents.agentzdrojov;

import OSPABA.*;
import simulation.*;

//meta! id="41"
public class AgentZdrojov extends OSPABA.Agent
{
	public AgentZdrojov(int id, Simulation mySim, Agent parent)
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
		new ManagerZdrojov(Id.managerZdrojov, mySim(), this);
		addOwnMessage(Mc.reqZdrojeOsetrenie);
		addOwnMessage(Mc.uvolniZdrojeVstup);
		addOwnMessage(Mc.uvolniZdrojeOsetrenie);
		addOwnMessage(Mc.reqZdrojeVstup);
	}
	//meta! tag="end"
}