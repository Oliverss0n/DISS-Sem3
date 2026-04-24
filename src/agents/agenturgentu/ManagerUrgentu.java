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
		if (petriNet() != null)
		{
			petriNet().clear();
		}
	}

	// --- CHODBA ---

	//meta! sender="AgentBoss", id="50", type="Notice"
	public void processNovyPacient(MessageForm message)
	{
		message.setAddressee(myAgent().findAssistant(Id.processChodba));
		startContinualAssistant(message);
	}

	//meta! sender="AgentVstupnehoVystrenia", id="56", type="Notice"
	public void processPresunNaOsetrenie(MessageForm message)
	{
		message.setAddressee(myAgent().findAssistant(Id.processChodba));
		startContinualAssistant(message);
	}

	//meta! sender="AgentOsetrenia", id="59", type="Notice"
	public void processOdchodPacienta(MessageForm message)
	{
		message.setAddressee(myAgent().findAssistant(Id.processChodba));
		startContinualAssistant(message);
	}

	//meta! sender="ProcessChodba", id="94", type="Finish"
	public void processFinish(MessageForm message)
	{
		if (message.code() == Mc.novyPacient) {
			message.setAddressee(mySim().findAgent(Id.agentVstupnehoVystrenia));
			notice(message);
		}
		else if (message.code() == Mc.presunNaOsetrenie) {
			message.setAddressee(mySim().findAgent(Id.agentOsetrenia));
			notice(message);
		}
		else if (message.code() == Mc.odchodPacienta) {
			message.setAddressee(myAgent().parent());
			notice(message);
		}
	}

	// --- PREPOSIELANIE PRE VSTUP (Poštár) ---

	//meta! sender="AgentVstupnehoVystrenia", id="104", type="Request"
	public void processReqZdrojeVstupAgentVstupnehoVystrenia(MessageForm message)
	{
		// Z Vstupu do Zdrojov
		message.setAddressee(mySim().findAgent(Id.agentZdrojov));
		request(message);
	}

	//meta! sender="AgentZdrojov", id="69", type="Response"
	public void processReqZdrojeVstupAgentZdrojov(MessageForm message)
	{
		// Zo Zdrojov späť na Vstup
		response(message);
	}

	//meta! sender="AgentVstupnehoVystrenia", id="108", type="Notice"
	public void processUvolniZdrojeVstup(MessageForm message)
	{
		// Z Vstupu uvoľnenie do Zdrojov
		message.setAddressee(mySim().findAgent(Id.agentZdrojov));
		notice(message);
	}

	// --- PREPOSIELANIE PRE OŠETRENIE (Poštár) ---

	//meta! sender="AgentOsetrenia", id="127", type="Request"
	public void processReqZdrojeOsetrenieAgentOsetrenia(MessageForm message)
	{
		// Z Ošetrenia do Zdrojov
		message.setAddressee(mySim().findAgent(Id.agentZdrojov));
		request(message);
	}

	//meta! sender="AgentZdrojov", id="110", type="Response"
	public void processReqZdrojeOsetrenieAgentZdrojov(MessageForm message)
	{
		// Zo Zdrojov späť na Ošetrenie
		response(message);
	}

	//meta! sender="AgentOsetrenia", id="109", type="Notice"
	public void processUvolniZdrojeOsetrenie(MessageForm message)
	{
		// Z Ošetrenia uvoľnenie do Zdrojov
		message.setAddressee(mySim().findAgent(Id.agentZdrojov));
		notice(message);
	}

	public void processDefault(MessageForm message)
	{
	}

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

			case Mc.reqZdrojeOsetrenie:
				switch (message.sender().id())
				{
					case Id.agentZdrojov:
						processReqZdrojeOsetrenieAgentZdrojov(message);
						break;
					case Id.agentOsetrenia:
						processReqZdrojeOsetrenieAgentOsetrenia(message);
						break;
				}
				break;

			case Mc.uvolniZdrojeOsetrenie:
				processUvolniZdrojeOsetrenie(message);
				break;

			case Mc.novyPacient:
				processNovyPacient(message);
				break;

			case Mc.uvolniZdrojeVstup:
				processUvolniZdrojeVstup(message);
				break;

			case Mc.odchodPacienta:
				processOdchodPacienta(message);
				break;

			case Mc.presunNaOsetrenie:
				processPresunNaOsetrenie(message);
				break;

			default:
				processDefault(message);
				break;
		}
	}

	@Override
	public AgentUrgentu myAgent()
	{
		return (AgentUrgentu)super.myAgent();
	}
}