package com.example.motor.db;

public class DeviceStateInfo {

	private String idString;
	private String runStateString;
	private String controlStateString;
	private String electricString;
	private String nameString;
	private String frequencyString;

	public String getFrequencyString() {
		return frequencyString;
	}

	public void setFrequencyString(String frequencyString) {
		this.frequencyString = frequencyString;
	}

	public String getIdString() {
		return idString;
	}

	public void setIdString(String idString) {
		this.idString = idString;
	}

	public String getRunStateString() {
		return runStateString;
	}

	public void setRunStateString(String runStateString) {
		this.runStateString = runStateString;
	}

	public String getControlStateString() {
		return controlStateString;
	}

	public void setControlStateString(String controlStateString) {
		this.controlStateString = controlStateString;
	}

	public String getElectricString() {
		return electricString;
	}

	public void setElectricString(String contentString) {
		this.electricString = contentString;
	}

	public String getNameString() {
		return nameString;
	}

	public void setNameString(String nameString) {
		this.nameString = nameString;
	}
}
