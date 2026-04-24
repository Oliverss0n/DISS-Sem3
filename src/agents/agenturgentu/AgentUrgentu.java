package agents.agenturgentu;

import OSPABA.*;
import simulation.*;
import agents.agenturgentu.continualassistants.*;

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
		new ProcessChodba(Id.processChodba, mySim(), this);
		addOwnMessage(Mc.presunNaOsetrenie);
		addOwnMessage(Mc.reqZdrojeOsetrenie);
		addOwnMessage(Mc.uvolniZdrojeVstup);
		addOwnMessage(Mc.reqZdrojeVstup);
		addOwnMessage(Mc.uvolniZdrojeOsetrenie);
		addOwnMessage(Mc.novyPacient);
		addOwnMessage(Mc.odchodPacienta);
	}
	//meta! tag="end"
}