package agents.agentosetrenia;

import OSPABA.*;
import simulation.*;

//meta! id="37"
public class ManagerOsetrenia extends OSPABA.Manager
{
	public ManagerOsetrenia(int id, Simulation mySim, Agent myAgent)
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

	//meta! sender="AgentUrgentu", id="58", type="Notice"
	public void processPresunNaOsetrenie(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="75", type="Response"
	public void processReqZdrojeOsetrenieAgentUrgentu(MessageForm message)
	{
	}


	//meta! sender="ProcesOsetrovanie", id="88", type="Finish"
	public void processFinish(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="73", type="Request"
	public void processResZdrojeVstup(MessageForm message)
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
		case Mc.resZdrojeVstup:
			processResZdrojeVstup(message);
		break;

		case Mc.reqZdrojeOsetrenie:
			switch (message.sender().id())
			{
			case Id.agentUrgentu:
				processReqZdrojeOsetrenieAgentUrgentu(message);
			break;
			}
		break;

		case Mc.presunNaOsetrenie:
			processPresunNaOsetrenie(message);
		break;

		case Mc.finish:
			processFinish(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentOsetrenia myAgent()
	{
		return (AgentOsetrenia)super.myAgent();
	}

}
