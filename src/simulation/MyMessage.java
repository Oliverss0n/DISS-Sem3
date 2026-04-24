package simulation;

import OSPABA.*;
import entities.Patient;

public class MyMessage extends OSPABA.MessageForm
{

	private Patient patient;

	public MyMessage(Simulation mySim)
	{
		super(mySim);
	}

	public MyMessage(MyMessage original)
	{
		super(original);
		this.patient = original.getPatient();
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
		// Copy attributes
	}

	public Patient getPatient() {
		return patient;
	}
	public void setPatient(Patient patient) {
		this.patient = patient;
	}


}