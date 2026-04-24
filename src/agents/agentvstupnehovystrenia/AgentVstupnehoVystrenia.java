package agents.agentvstupnehovystrenia;

import OSPABA.*;
import agents.agentvstupnehovystrenia.continualassistants.*;
import simulation.*;

//meta! id="34"
public class AgentVstupnehoVystrenia extends OSPABA.Agent
{
	public AgentVstupnehoVystrenia(int id, Simulation mySim, Agent parent)
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
		new ManagerVstupnehoVystrenia(Id.managerVstupnehoVystrenia, mySim(), this);
		new ProcesVstup(Id.procesVstup, mySim(), this);
		addOwnMessage(Mc.reqZdrojeVstup);
		addOwnMessage(Mc.novyPacient);
	}
	//meta! tag="end"
}
