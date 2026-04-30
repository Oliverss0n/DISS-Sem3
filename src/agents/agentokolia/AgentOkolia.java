package agents.agentokolia;

import OSPABA.*;
import simulation.*;
import agents.agentokolia.continualassistants.*;
import Distributions.ErlangDist;
import Distributions.ExponentialDist;
import OSPRNG.ErlangRNG;

//meta! id="5"
public class AgentOkolia extends OSPABA.Agent
{
	private ExponentialDist walkArrivals;
	private ErlangDist ambulanceArrivals;
	//test
	public AgentOkolia(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();

		MySimulation sim = (MySimulation) mySim;

		walkArrivals = new ExponentialDist(572.6, sim.getGenSeed());
		//walkArrivals = new ExponentialDist(50, sim.getGenSeed());
		ambulanceArrivals = new ErlangDist(8, 364.5, -13.5, sim.getGenSeed());

	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerOkolia(Id.managerOkolia, mySim(), this);
		new SchedulerPesi(Id.schedulerPesi, mySim(), this);
		new SchedulerSanitka(Id.schedulerSanitka, mySim(), this);
		addOwnMessage(Mc.novyPacient);
	}
	//meta! tag="end"


	public ExponentialDist getWalkArrivals() {
		return walkArrivals;
	}

	public ErlangDist getAmbulanceArrivals() {
		return ambulanceArrivals;
	}
}