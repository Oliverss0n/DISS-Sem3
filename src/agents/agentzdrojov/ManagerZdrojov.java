package agents.agentzdrojov;

import OSPABA.*;
import simulation.*;

//meta! id="41"
public class ManagerZdrojov extends OSPABA.Manager
{
	public ManagerZdrojov(int id, Simulation mySim, Agent myAgent)
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

	//meta! sender="AgentUrgentu", id="76", type="Response"
	public void processReqZdrojeOsetrenie(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="64", type="Request"
	public void processReqZdrojeVstupAgentUrgentu(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="69", type="Response"
	public void processReqZdrojeVstupAgentUrgentu(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="77", type="Response"
	public void processRequestResponse(MessageForm message)
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
			switch (message.sender().id())
			{
			case Id.agentUrgentu:
				processReqZdrojeVstupAgentUrgentu(message);
			break;

			case Id.agentUrgentu:
				processReqZdrojeVstupAgentUrgentu(message);
			break;
			}
		break;

		case Mc.reqZdrojeOsetrenie:
			processReqZdrojeOsetrenie(message);
		break;

		case Mc.requestResponse:
			processRequestResponse(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentZdrojov myAgent()
	{
		return (AgentZdrojov)super.myAgent();
	}

}
