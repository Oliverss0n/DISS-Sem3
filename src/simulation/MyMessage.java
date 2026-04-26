package simulation;

import OSPABA.*;
import entities.Patient;

public class MyMessage extends OSPABA.MessageForm
{

	private Patient patient;
	private String ambulanceType;

	private int reason;//????

	public MyMessage(Simulation mySim)
	{
		super(mySim);
		this.ambulanceType = "";
		this.reason = -1;//??
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
		this.reason = original.getReason();
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

	// Pridaj getter a setter:
	public int getReason() { return reason; }
	public void setReason(int reason) { this.reason = reason; }


}