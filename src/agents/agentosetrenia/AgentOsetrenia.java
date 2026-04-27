package agents.agentosetrenia;

import OSPABA.*;
import agents.agentosetrenia.continualassistants.*;
import simulation.*;
import Distributions.ContinuousEmpiricDist;
import Distributions.ContinuousUniformDist;

//meta! id="37"
public class AgentOsetrenia extends OSPABA.Agent
{

	private ContinuousEmpiricDist walkInExamDurationGen;
	private ContinuousUniformDist ambulanceExamDurationGen;


	public AgentOsetrenia(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
		MySimulation sim = (MySimulation) mySim;
		walkInExamDurationGen = new ContinuousEmpiricDist(
				new double[]{0.1, 0.6, 0.3},
				new double[]{12.0, 14.0, 18.0},
				new double[]{10.0, 12.0, 14.0},
				sim.getGenSeed()
		);
		ambulanceExamDurationGen = new ContinuousUniformDist(15,30, sim.getGenSeed());
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
		new ManagerOsetrenia(Id.managerOsetrenia, mySim(), this);
		new ProcesOsetrovanie(Id.procesOsetrovanie, mySim(), this);
		addOwnMessage(Mc.presunNaOsetrenie);
		addOwnMessage(Mc.reqZdrojeOsetrenie);
		addOwnMessage(Mc.koniecZdrzania);
	}
	//meta! tag="end"


	public ContinuousEmpiricDist getWalkInExamDurationGen() {
		return walkInExamDurationGen;
	}

	public ContinuousUniformDist getAmbulanceExamDurationGen() {
		return ambulanceExamDurationGen;
	}
}