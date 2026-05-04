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

	//meta! sender="AgentBoss", id="50", type="Notice"
	public void processNovyPacient(MessageForm message) {
		message.setAddressee(myAgent().findAssistant(Id.processPresunVstup));
		startContinualAssistant(message);
	}

	//meta! sender="AgentVstupnehoVystrenia", id="56", type="Notice"
	public void processPresunNaOsetrenie(MessageForm message) {
		message.setAddressee(myAgent().findAssistant(Id.processPresunOsetrenie));
		startContinualAssistant(message);
	}

	//meta! sender="AgentOsetrenia", id="59", type="Notice"
	public void processOdchodPacienta(MessageForm message) {
		message.setAddressee(myAgent().findAssistant(Id.processPresunOdchod));
		startContinualAssistant(message);
	}

	//meta! sender="ProcessPresunVstup", id="135", type="Finish"
	public void processFinishProcessPresunVstup(MessageForm message) {
		MyMessage msg = (MyMessage) message;
		MySimulation sim = (MySimulation) mySim();

		sim.log("CHODBA KONIEC: Pacient #" + msg.getPatient().getId() + " dorazil k recepcii.");
		msg.setCode(Mc.novyPacient);
		msg.setAddressee(mySim().findAgent(Id.agentVstupnehoVystrenia));
		notice(msg);
	}

	//meta! sender="ProcessPresunOsetrenie", id="139", type="Finish"
	public void processFinishProcessPresunOsetrenie(MessageForm message) {
		MyMessage msg = (MyMessage) message;
		MySimulation sim = (MySimulation) mySim();

		sim.log("CHODBA KONIEC: Pacient #" + msg.getPatient().getId() + " dorazil k ambulanciám.");
		msg.setCode(Mc.presunNaOsetrenie);
		msg.setAddressee(mySim().findAgent(Id.agentOsetrenia));
		notice(msg);
	}

	//meta! sender="ProcessPresunOdchod", id="137", type="Finish"
	public void processFinishProcessPresunOdchod(MessageForm message) {
		MyMessage msg = (MyMessage) message;
		MySimulation sim = (MySimulation) mySim();
		double totalTime = sim.currentTime() - msg.getPatient().getArrivalTime();
		if (msg.getPatient().isAmbulance()) {
			sim.agentZdrojov().getTimeInSystemAmbStat().add(totalTime);
		} else {
			sim.agentZdrojov().getTimeInSystemWalkInStat().add(totalTime);
		}

		sim.incrementPatientCount(msg.getPatient().isAmbulance());

		sim.removePatient(msg.getPatient());
		sim.log("ODCHOD: Pacient #" + msg.getPatient().getId() + " úspešne opúšťa nemocnicu!");

		msg.setCode(Mc.odchodPacienta);
		msg.setAddressee(mySim().findAgent(Id.agentBoss));
		notice(msg);
	}



	//meta! sender="AgentVstupnehoVystrenia", id="63", type="Request"
	public void processReqZdrojeVstupAgentVstupnehoVystrenia(MessageForm message) {
		message.setAddressee(mySim().findAgent(Id.agentZdrojov));
		request(message);
	}

	//meta! sender="AgentZdrojov", id="64", type="Response"
	public void processReqZdrojeVstupAgentZdrojov(MessageForm message) {
		message.setAddressee(mySim().findAgent(Id.agentVstupnehoVystrenia));
		response(message);
	}

	//meta! sender="AgentVstupnehoVystrenia", id="108", type="Notice"
	public void processUvolniZdrojeVstup(MessageForm message) {
		message.setAddressee(mySim().findAgent(Id.agentZdrojov));
		notice(message);
	}

	//meta! sender="AgentOsetrenia", id="127", type="Request"
	public void processReqZdrojeOsetrenieAgentOsetrenia(MessageForm message) {
		message.setAddressee(mySim().findAgent(Id.agentZdrojov));
		request(message);
	}

	//meta! sender="AgentZdrojov", id="110", type="Response"
	public void processReqZdrojeOsetrenieAgentZdrojov(MessageForm message) {
		message.setAddressee(mySim().findAgent(Id.agentOsetrenia));
		response(message);
	}

	//meta! sender="AgentOsetrenia", id="109", type="Notice"
	public void processUvolniZdrojeOsetrenie(MessageForm message) {
		message.setAddressee(mySim().findAgent(Id.agentZdrojov));
		notice(message);
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
			case Mc.finish:
				switch (message.sender().id()) {
					case Id.processPresunVstup:
						processFinishProcessPresunVstup(message);
						break;
					case Id.processPresunOsetrenie:
						processFinishProcessPresunOsetrenie(message);
						break;
					case Id.processPresunOdchod:
						processFinishProcessPresunOdchod(message);
						break;
				}
				break;

			case Mc.reqZdrojeVstup:
				switch (message.sender().id()) {
					case Id.agentVstupnehoVystrenia:
						processReqZdrojeVstupAgentVstupnehoVystrenia(message);
						break;

					case Id.agentZdrojov:
						processReqZdrojeVstupAgentZdrojov(message);
						break;
				}
				break;

			case Mc.reqZdrojeOsetrenie:
				switch (message.sender().id()) {
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
	//meta! tag="end"


	@Override
	public AgentUrgentu myAgent() {
		return (AgentUrgentu)super.myAgent();
	}
}