package agents.agentokolia;

import OSPABA.*;
import simulation.*;

//meta! id="5"
public class ManagerOkolia extends OSPABA.Manager
{
	public ManagerOkolia(int id, Simulation mySim, Agent myAgent)
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
		MyMessage startWalk = new MyMessage(mySim());
		startWalk.setCode(Mc.start);
		startWalk.setAddressee(myAgent().findAssistant(Id.schedulerPesi));
		startContinualAssistant(startWalk);

		MyMessage startAmbulance = new MyMessage(mySim());
		startAmbulance.setCode(Mc.start);
		startAmbulance.setAddressee(myAgent().findAssistant(Id.schedulerSanitka));
		startContinualAssistant(startAmbulance);
	}

	//meta! sender="SchedulerSanitka", id="10", type="Finish"
	public void processFinishSchedulerSanitka(MessageForm message)
	{
	}

	//meta! sender="SchedulerPesi", id="8", type="Finish"
	public void processFinishSchedulerPesi(MessageForm message)
	{
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		if (message.code() == Mc.novyPacient) {
			message.setAddressee(myAgent().parent());
			notice(message);
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
		case Mc.finish:
			switch (message.sender().id())
			{
			case Id.schedulerSanitka:
				processFinishSchedulerSanitka(message);
			break;

			case Id.schedulerPesi:
				processFinishSchedulerPesi(message);
			break;
			}
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentOkolia myAgent()
	{
		return (AgentOkolia)super.myAgent();
	}

}