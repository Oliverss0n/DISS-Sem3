package agents.agentvstupnehovystrenia;

import Distributions.*;
import OSPABA.*;
import agents.agentvstupnehovystrenia.continualassistants.*;
import simulation.*;

//meta! id="34"
public class AgentVstupnehoVystrenia extends OSPABA.Agent
{
	private ContinuousEmpiricDist walkInDurationGen;
	private DiscreteUniformDist ambualanceDurationGen;

	public AgentVstupnehoVystrenia(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		MySimulation sim = (MySimulation) mySim;
		this.walkInDurationGen = new ContinuousEmpiricDist(new double[]{0.6, 0.4}, new double[]{5.0, 9.0}, new double[]{3.0, 5.0}, sim.getGenSeed());
		this.ambualanceDurationGen = new DiscreteUniformDist(4,8, sim.getGenSeed());
		init();
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
		new ManagerVstupnehoVystrenia(Id.managerVstupnehoVystrenia, mySim(), this);
		new ProcesVstup(Id.procesVstup, mySim(), this);
		addOwnMessage(Mc.reqZdrojeVstup);
		addOwnMessage(Mc.novyPacient);

	}
	//meta! tag="end"

	public ContinuousEmpiricDist getWalkInDurationGen() {
		return walkInDurationGen;
	}

	public DiscreteUniformDist getAmbualanceDurationGen() {
		return ambualanceDurationGen;
	}

}