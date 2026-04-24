package agents.agentzdrojov;

import OSPABA.*;
import OSPStat.Stat;
import simulation.*;

import java.util.LinkedList;
import java.util.Queue;

//meta! id="41"
public class ManagerZdrojov extends OSPABA.Manager
{

	private int volneSestryVstup;
	private int volneAmbulancieVstup;
	private Queue<MessageForm> radVstup;
	//public Stat statCakanieVstup;

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

		this.volneSestryVstup = 1;
		this.volneAmbulancieVstup = 1;
		this.radVstup = new LinkedList<>();


	}

	//meta! sender="AgentUrgentu", id="110", type="Request"
	public void processReqZdrojeOsetrenie(MessageForm message)
	{
	}

	//meta! userInfo="Removed from model"
	public void processRequestResponse(MessageForm message)
	{
		this.volneSestryVstup++;
		this.volneAmbulancieVstup++;

		if (!this.radVstup.isEmpty()) {

			MessageForm nextMsg = this.radVstup.poll();

			this.volneSestryVstup--;
			this.volneAmbulancieVstup--;
			response(nextMsg);
		}

	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
	}

	//meta! sender="AgentUrgentu", id="64", type="Request"
	public void processReqZdrojeVstup(MessageForm message)
	{
		if (this.volneSestryVstup > 0 && this.volneAmbulancieVstup > 0) {

			this.volneSestryVstup--;
			this.volneAmbulancieVstup--;
			response(message);
		} else {
			this.radVstup.add(message);
		}

	}

	//meta! userInfo="Removed from model"
	public void processReqZdrojeOsetrenieAgentUrgentu(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="112", type="Notice"
	public void processUvolniZdrojeVstup(MessageForm message)
	{
	}

	//meta! sender="AgentUrgentu", id="113", type="Notice"
	public void processUvolniZdrojeOsetrenie(MessageForm message)
	{
	}

	//meta! userInfo="Removed from model"
	public void processReqZdrojeVstupAgentUrgentu(MessageForm message)
	{
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
		case Mc.uvolniZdrojeVstup:
			processUvolniZdrojeVstup(message);
		break;

		case Mc.uvolniZdrojeOsetrenie:
			processUvolniZdrojeOsetrenie(message);
		break;

		case Mc.reqZdrojeOsetrenie:
			processReqZdrojeOsetrenie(message);
		break;

		case Mc.reqZdrojeVstup:
			processReqZdrojeVstup(message);
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