package simulation;

import OSPABA.*;
import entities.Patient;

public class MyMessage extends OSPABA.MessageForm
{

	private Patient patient;
	private String ambulanceType;


	public MyMessage(Simulation mySim)
	{
		super(mySim);
		this.ambulanceType = "";
	}

	public MyMessage(MyMessage original)
	{
		super(original);
		this.patient = original.getPatient();
		this.ambulanceType = original.getAmbulanceType();
	}

	@Override
	public MessageForm createCopy()
	{
		return new MyMessage(this);
	}

	@Override
	protected void copy(MessageForm message)
	{
		super.copy(message);
		MyMessage original = (MyMessage)message;
		this.patient = original.getPatient();
		this.ambulanceType = original.getAmbulanceType();
	}

	public Patient getPatient() {
		return patient;
	}
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public String getAmbulanceType() {
		return ambulanceType;
	}
	public void setAmbulanceType(String ambulanceType) {
		this.ambulanceType = ambulanceType;
	}



}