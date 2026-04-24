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
		message.setCode(Mc.reqZdrojeOsetrenie);
		message.setAddressee(myAgent().parent());
		request(message);
	}



	//meta! sender="ProcesOsetrovanie", id="88", type="Finish"
	public void processFinish(MessageForm message)
	{
		MessageForm vratenieZdrojov = message.createCopy();
		vratenieZdrojov.setCode(Mc.uvolniZdrojeOsetrenie);
		vratenieZdrojov.setAddressee(myAgent().parent());
		notice(vratenieZdrojov);

		message.setCode(Mc.odchodPacienta);
		message.setAddressee(myAgent().parent());
		notice(message);
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
	}

	//meta! sender="AgentUrgentu", id="127", type="Response"
	public void processReqZdrojeOsetrenie(MessageForm message)
	{
		message.setAddressee(myAgent().findAssistant(Id.procesOsetrovanie));
		startContinualAssistant(message);
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
		case Mc.finish:
			processFinish(message);
		break;

		case Mc.presunNaOsetrenie:
			processPresunNaOsetrenie(message);
		break;

		case Mc.reqZdrojeOsetrenie:
			processReqZdrojeOsetrenie(message);
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