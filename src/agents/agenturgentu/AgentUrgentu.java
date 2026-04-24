package agents.agenturgentu;

import OSPABA.*;
import simulation.*;

//meta! id="30"
public class AgentUrgentu extends OSPABA.Agent
{
	public AgentUrgentu(int id, Simulation mySim, Agent parent)
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
		new ManagerUrgentu(Id.managerUrgentu, mySim(), this);
		addOwnMessage(Mc.presunNaOsetrenie);
		addOwnMessage(Mc.reqZdrojeOsetrenie);
		addOwnMessage(Mc.reqZdrojeVstup);
		addOwnMessage(Mc.requestResponse);
		addOwnMessage(Mc.odchodPacienta);
		addOwnMessage(Mc.novyPacient);
		addOwnMessage(Mc.resZdrojeVstup);
	}
	//meta! tag="end"
}
