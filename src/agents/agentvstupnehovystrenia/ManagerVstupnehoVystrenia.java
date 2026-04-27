package agents.agentvstupnehovystrenia;

import OSPABA.*;
import entities.Patient;
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
		message.setAddressee(myAgent().findAssistant(Id.procesVstup));
		startContinualAssistant(message);
	}

	//meta! sender="ProcesVstup", id="84", type="Finish"
	public void processFinish(MessageForm message)
	{
		MyMessage msg = (MyMessage) message;
		Patient patient = msg.getPatient();
		MySimulation sim = (MySimulation) mySim();

		int priority = 0;
		if (patient.isAmbulance()) {
			priority = myAgent().getPriorityAmbulanceGen().sample();
		} else {
			priority = myAgent().getPriorityWalkInGen().sample();
		}
		patient.setPriority(priority);

		sim.log("VSTUP KONIEC: Pacient #" + patient.getId() + " bol vyšetrený na vstupe. Pridelená priorita: " + priority);
		MessageForm returnResources = message.createCopy();
		returnResources.setCode(Mc.uvolniZdrojeVstup);
		returnResources.setAddressee(myAgent().parent());
		notice(returnResources);

		message.setCode(Mc.presunNaOsetrenie);
		message.setAddressee(myAgent().parent());
		notice(message);
	}

	//meta! sender="AgentUrgentu", id="51", type="Notice"
	public void processNovyPacient(MessageForm message)
	{
		MySimulation sim = (MySimulation) mySim();
		sim.log("4. VSTUP PRIJATÝ | Pacient žiada sestry a ambulanciu.");
		message.setCode(Mc.reqZdrojeVstup);
		message.setAddressee(mySim().findAgent(Id.agentUrgentu));

		request(message);
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
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
		case Mc.finish:
			processFinish(message);
		break;

		case Mc.reqZdrojeVstup:
			processReqZdrojeVstup(message);
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