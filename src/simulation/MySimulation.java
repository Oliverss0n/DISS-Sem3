package simulation;

import OSPABA.*;
import agents.agentosetrenia.*;
import agents.agentokolia.*;
import agents.agentzdrojov.*;
import agents.agentboss.*;
import agents.agenturgentu.*;
import agents.agentvstupnehovystrenia.*;

import java.util.Random;

public class MySimulation extends OSPABA.Simulation
{
	protected Random genSeed;
	public MySimulation()
	{
		init();
	}

	@Override
	public void prepareSimulation()
	{
		super.prepareSimulation();
		// Create global statistcis
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Reset entities, queues, local statistics, etc...
	}

	@Override
	public void replicationFinished()
	{
		// Collect local statistics into global, update UI, etc...
		super.replicationFinished();
	}

	@Override
	public void simulationFinished()
	{
		// Display simulation results
		super.simulationFinished();
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		this.genSeed = new Random();
		setAgentBoss(new AgentBoss(Id.agentBoss, this, null));
		setAgentOkolia(new AgentOkolia(Id.agentOkolia, this, agentBoss()));
		setAgentUrgentu(new AgentUrgentu(Id.agentUrgentu, this, agentBoss()));
		setAgentVstupnehoVystrenia(new AgentVstupnehoVystrenia(Id.agentVstupnehoVystrenia, this, agentUrgentu()));
		setAgentOsetrenia(new AgentOsetrenia(Id.agentOsetrenia, this, agentUrgentu()));
		setAgentZdrojov(new AgentZdrojov(Id.agentZdrojov, this, agentUrgentu()));
	}

	private AgentBoss _agentBoss;

public AgentBoss agentBoss()
	{ return _agentBoss; }

	public void setAgentBoss(AgentBoss agentBoss)
	{_agentBoss = agentBoss; }

	private AgentOkolia _agentOkolia;

public AgentOkolia agentOkolia()
	{ return _agentOkolia; }

	public void setAgentOkolia(AgentOkolia agentOkolia)
	{_agentOkolia = agentOkolia; }

	private AgentUrgentu _agentUrgentu;

public AgentUrgentu agentUrgentu()
	{ return _agentUrgentu; }

	public void setAgentUrgentu(AgentUrgentu agentUrgentu)
	{_agentUrgentu = agentUrgentu; }

	private AgentVstupnehoVystrenia _agentVstupnehoVystrenia;

public AgentVstupnehoVystrenia agentVstupnehoVystrenia()
	{ return _agentVstupnehoVystrenia; }

	public void setAgentVstupnehoVystrenia(AgentVstupnehoVystrenia agentVstupnehoVystrenia)
	{_agentVstupnehoVystrenia = agentVstupnehoVystrenia; }

	private AgentOsetrenia _agentOsetrenia;

public AgentOsetrenia agentOsetrenia()
	{ return _agentOsetrenia; }

	public void setAgentOsetrenia(AgentOsetrenia agentOsetrenia)
	{_agentOsetrenia = agentOsetrenia; }

	private AgentZdrojov _agentZdrojov;

public AgentZdrojov agentZdrojov()
	{ return _agentZdrojov; }

	public void setAgentZdrojov(AgentZdrojov agentZdrojov)
	{_agentZdrojov = agentZdrojov; }
	//meta! tag="end"


	public Random getGenSeed() {
		return genSeed;
	}
}