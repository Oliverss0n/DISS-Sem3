package agents.agentvstupnehovystrenia;

import Distributions.*;
import OSPABA.*;
import agents.agentvstupnehovystrenia.continualassistants.*;
import simulation.*;

//meta! id="34"
public class AgentVstupnehoVystrenia extends OSPABA.Agent
{
	private ContinuousEmpiricDist walkInEntranceExamDurationGen;
	private DiscreteUniformDist ambualanceEntranceExamDurationGen;

	private DiscreteEmpiricDist priorityWalkInGen;
	private DiscreteEmpiricDist priorityAmbulanceGen;

	public AgentVstupnehoVystrenia(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		MySimulation sim = (MySimulation) mySim;
		this.walkInEntranceExamDurationGen = new ContinuousEmpiricDist(new double[]{0.6, 0.4}, new double[]{5.0, 9.0}, new double[]{3.0, 5.0}, sim.getGenSeed());
		this.ambualanceEntranceExamDurationGen = new DiscreteUniformDist(4,8, sim.getGenSeed());

		this.priorityWalkInGen = new DiscreteEmpiricDist(
				new double[]{0.1, 0.2, 0.15, 0.25, 0.3},
				new int[]{2, 3, 4, 5, 6},
				new int[]{1, 2, 3, 4, 5},
				sim.getGenSeed()
		);

		this.priorityAmbulanceGen = new DiscreteEmpiricDist(
				new double[]{0.3, 0.25, 0.2, 0.15, 0.1},
				new int[]{2, 3, 4, 5, 6},
				new int[]{1, 2, 3, 4, 5},
				sim.getGenSeed()
		);

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
		addOwnMessage(Mc.koniecZdrzania);

	}
	//meta! tag="end"

	public ContinuousEmpiricDist getWalkInEntranceExamDurationGen() {
		return walkInEntranceExamDurationGen;
	}

	public DiscreteUniformDist getAmbualanceEntranceExamDurationGen() {
		return ambualanceEntranceExamDurationGen;
	}

	public DiscreteEmpiricDist getPriorityWalkInGen() {
		return priorityWalkInGen;
	}

	public DiscreteEmpiricDist getPriorityAmbulanceGen() {
		return priorityAmbulanceGen;
	}

}