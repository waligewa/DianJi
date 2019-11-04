package com.example.motor.db;

/**
 * 设备实体类
 * 
 * @author WRJ
 * 
 */
public class RtuInfo {

	private String DeviceID;
	private String DeviceName;
	private String ComAddress;
	private String PumpNum;

	public String getDeviceID() {
		return DeviceID;
	}

	public void setDeviceID(String deviceID) {
		DeviceID = deviceID;
	}

	public String getDeviceName() {
		return DeviceName;
	}

	public void setDeviceName(String deviceName) {
		DeviceName = deviceName;
	}

	public String getComAddress() {
		return ComAddress;
	}

	public void setComAddress(String comAddress) {
		ComAddress = comAddress;
	}

	public String getPumpNum() {
		return PumpNum;
	}

	public void setPumpNum(String pumpNum) {
		PumpNum = pumpNum;
	}

}
