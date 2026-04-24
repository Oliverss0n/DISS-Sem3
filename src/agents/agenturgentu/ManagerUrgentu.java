package agents.agenturgentu;

import OSPABA.*;
import simulation.*;

//meta! id="30"
public class ManagerUrgentu extends OSPABA.Manager
{
	public ManagerUrgentu(int id, Simulation mySim, Agent myAgent)
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

	//meta! sender="AgentVstupnehoVystrenia", id="56", type="Notice"
	public void processPresunNaOsetrenie(MessageForm message)
	{
	}

	//meta! sender="AgentOsetrenia", id="75", type="Request"
	public void processReqZdrojeOsetrenieAgentOsetrenia(MessageForm message)
	{
	}

	//meta! sender="AgentZdrojov", id="76", type="Request"
	public void processReqZdrojeOsetrenieAgentZdrojov(MessageForm message)
	{
	}

	//meta! sender="AgentVstupnehoVystrenia", id="63", type="Request"
	public void processReqZdrojeVstupAgentVstupnehoVystrenia(MessageForm message)
	{
	}

	//meta! sender="AgentZdrojov", id="64", type="Response"
	public void processReqZdrojeVstupAgentZdrojov(MessageForm message)
	{
	}


	//meta! sender="AgentZdrojov", id="77", type="Request"
	public void processRequestResponse(MessageForm message)
	{
	}

	//meta! sender="AgentOsetrenia", id="59", type="Notice"
	public void processOdchodPacienta(MessageForm message)
	{
	}

	//meta! sender="AgentBoss", id="50", type="Notice"
	public void processNovyPacient(MessageForm message)
	{
	}

	//meta! sender="AgentOsetrenia", id="73", type="Response"
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
		case Mc.odchodPacienta:
			processOdchodPacienta(message);
		break;

		case Mc.reqZdrojeVstup:
			switch (message.sender().id())
			{
			case Id.agentVstupnehoVystrenia:
				processReqZdrojeVstupAgentVstupnehoVystrenia(message);
			break;

			case Id.agentZdrojov:
				processReqZdrojeVstupAgentZdrojov(message);
			break;

			}
		break;

		case Mc.novyPacient:
			processNovyPacient(message);
		break;

		case Mc.reqZdrojeOsetrenie:
			switch (message.sender().id())
			{
			case Id.agentOsetrenia:
				processReqZdrojeOsetrenieAgentOsetrenia(message);
			break;

			case Id.agentZdrojov:
				processReqZdrojeOsetrenieAgentZdrojov(message);
			break;

			}
		break;

		case Mc.requestResponse:
			processRequestResponse(message);
		break;

		case Mc.presunNaOsetrenie:
			processPresunNaOsetrenie(message);
		break;

		case Mc.resZdrojeVstup:
			processResZdrojeVstup(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentUrgentu myAgent()
	{
		return (AgentUrgentu)super.myAgent();
	}

}
