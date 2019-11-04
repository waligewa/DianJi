package com.example.motor.db;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnalogDicInfo {

	private String ID;//id
	private String EquipmentID;// 设备id
	private String PressureSet;// 设定压力
	private String PressureIN;// 进水压力
	private String PressureOut;// 出水压力
	private String Frequency;// 变频频率
	private String InstantFlow1;// 瞬时流量
	private String TotalFlow1;// 累计流量
    private String SetFlow; // 设定流量
	private String TotalPower;// 累计电量
    private int PumpNum;// 水泵数量
    private String UpdateTime;//更新时间
    private String DeviceState;//设备状态
    private boolean IsOnline;//通讯状态

    public String getSetFlow() {
        return SetFlow;
    }

    public void setSetFlow(String setFlow) {
        SetFlow = setFlow;
    }

    public String getTotalPower() {
		return TotalPower;
	}

	public void setTotalPower(String totalPower) {
		TotalPower = totalPower;
	}

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getEquipmentID() {
		return EquipmentID;
	}

	public void setEquipmentID(String equipmentID) {
		EquipmentID = equipmentID;
	}

	public String getUpdateTime() {
		return UpdateTime;
	}

	public void setUpdateTime(String updateTime) {
		UpdateTime = updateTime;
	}

	public int getPumpNum() {
		return PumpNum;
	}

	public void setPumpNum(int pumpNum) {
		PumpNum = pumpNum;
	}

	public String getDeviceState() {
		return DeviceState;
	}

	public void setDeviceState(String deviceState) {
		DeviceState = deviceState;
	}

	public boolean getIsOnline() {
		return IsOnline;
	}

	public void setIsOnline(boolean isOnline) {
		IsOnline = isOnline;
	}

	public String getPressureSet() {
		return PressureSet;
	}

	public void setPressureSet(String pressureSet) {
		PressureSet = pressureSet;
	}

	public String getPressureIN() {
		return PressureIN;
	}

	public void setPressureIN(String pressureIN) {
		PressureIN = pressureIN;
	}

	public String getPressureOut() {
		return PressureOut;
	}

	public void setPressureOut(String pressureOut) {
		PressureOut = pressureOut;
	}

	public String getFrequency() {
		return Frequency;
	}

	public void setFrequency(String frequency) {
		Frequency = frequency;
	}

	public String getInstantFlow1() {
		return InstantFlow1;
	}

	public void setInstantFlow1(String instantFlow1) {
		InstantFlow1 = instantFlow1;
	}

	public String getTotalFlow1() {
		return TotalFlow1;
	}

	public void setTotalFlow1(String totalFlow1) {
		TotalFlow1 = totalFlow1;
	}

	public List<AnalogDicInfo> getList(JSONArray jsonArray) {
		List<AnalogDicInfo> list = new ArrayList<AnalogDicInfo>();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject Object = jsonArray.getJSONObject(i);
				AnalogDicInfo info = new AnalogDicInfo();
				info.setID(Object.getString("ID"));
				info.setEquipmentID(Object.getString("EquipmentID"));
				info.setUpdateTime(Object.getString("UpdateTime"));
				info.setPressureIN(Object.getString("PressureIN"));
				info.setPressureOut(Object.getString("PressureOut"));
				info.setPressureSet(Object.getString("PressureSet"));
				info.setFrequency(Object.getString("Frequency"));
				info.setInstantFlow1(Object.getString("InstantFlow1"));
				info.setTotalFlow1(Object.getString("TotalFlow1"));
				info.setPumpNum(Object.getInt("PumpNum"));
				list.add(info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
