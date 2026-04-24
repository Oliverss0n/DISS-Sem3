package agents.agentvstupnehovystrenia;

import OSPABA.*;
import simulation.*;

//meta! id="34"
public class ManagerVstupnehoVystrenia extends OSPABA.Manager
{
	public ManagerVstupnehoVystrenia(int id, Simulation mySim, Agent myAgent)
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

	//meta! sender="AgentUrgentu", id="63", type="Response"
	public void processReqZdrojeVstup(MessageForm message)
	{
	}

	//meta! sender="ProcesVstup", id="84", type="Finish"
	public void processFinish(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="51", type="Notice"
	public void processNovyPacient(MessageForm message)
	{
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
		case Mc.reqZdrojeVstup:
			processReqZdrojeVstup(message);
		break;

		case Mc.finish:
			processFinish(message);
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
	public AgentVstupnehoVystrenia myAgent()
	{
		return (AgentVstupnehoVystrenia)super.myAgent();
	}

}
