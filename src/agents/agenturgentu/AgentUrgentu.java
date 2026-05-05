package agents.agenturgentu;

import OSPABA.*;
import simulation.*;
import agents.agenturgentu.continualassistants.*;
import Distributions.*;

//meta! id="30"
public class AgentUrgentu extends OSPABA.Agent
{
	private TriangularDist moveEntranceWalkInGen;
	private ContinuousUniformDist moveEntranceAmbulanceGen;
	private TriangularDist moveBetweenAmbulancesGen;
	private ContinuousUniformDist moveExitGen;



	public AgentUrgentu(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);

		MySimulation sim = (MySimulation) mySim;

		this.moveEntranceWalkInGen = new TriangularDist(120.0, 300.0,150.0, sim.getGenSeed());
		this.moveEntranceAmbulanceGen = new ContinuousUniformDist(90.0, 200.0, sim.getGenSeed());
		this.moveBetweenAmbulancesGen = new TriangularDist(15.0,45.0,20.0, sim.getGenSeed());
		this.moveExitGen = new ContinuousUniformDist(150.0, 240.0, sim.getGenSeed());
		init();
		addOwnMessage(Mc.koniecZdrzania);
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
		new ManagerUrgentu(Id.managerUrgentu, mySim(), this);
		new ProcessPresunOdchod(Id.processPresunOdchod, mySim(), this);
		new ProcessPresunVstup(Id.processPresunVstup, mySim(), this);
		new ProcessPresunOsetrenie(Id.processPresunOsetrenie, mySim(), this);
		addOwnMessage(Mc.presunNaOsetrenie);
		addOwnMessage(Mc.reqZdrojeOsetrenie);
		addOwnMessage(Mc.uvolniZdrojeVstup);
		addOwnMessage(Mc.reqZdrojeVstup);
		addOwnMessage(Mc.uvolniZdrojeOsetrenie);
		addOwnMessage(Mc.novyPacient);
		addOwnMessage(Mc.odchodPacienta);

	}
	//meta! tag="end"


	public TriangularDist getMoveEntranceWalkInGen() {
		return moveEntranceWalkInGen;
	}

	public ContinuousUniformDist getMoveEntranceAmbulanceGen() {
		return moveEntranceAmbulanceGen;
	}

	public TriangularDist getMoveBetweenAmbulancesGen() {
		return moveBetweenAmbulancesGen;
	}

	public ContinuousUniformDist getMoveExitGen() {
		return moveExitGen;
	}
}