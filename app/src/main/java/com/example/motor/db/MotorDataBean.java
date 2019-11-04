package com.example.motor.db;

/**
 * Created by admin on 2019/3/21.
 */

public class MotorDataBean {


    /**
     * "Vesion": "0",
     * "Frequency": "0",
     * "Electric": "0",
     * "Voltage": "0",
     * "Temperature": "0",
     * "Error": "0",
     * "State": "0",
     * "AI1_ADC": "0",
     * "AI2_ADC": "0",
     * "Timer": "0"
     */
    
    private String PumpID;
    private String Vesion;
    private String Frequency;
    private String Electric;
    private String Voltage;
    private String Temperature;
    private String Error;
    private String State;
    private String AI1_ADC;
    private String AI2_ADC;
    private String Timer;
    private String InPDec;
    private String OutPDec;
    private String SetP;
    private String EquipOperateStatus;
    private String EquipAlarmStatus;
    private String ScrEquipPower;

    public String getInPDec() {
        return InPDec;
    }

    public void setInPDec(String inPDec) {
        InPDec = inPDec;
    }

    public String getOutPDec() {
        return OutPDec;
    }

    public void setOutPDec(String outPDec) {
        OutPDec = outPDec;
    }

    public String getSetP() {
        return SetP;
    }

    public void setSetP(String setP) {
        SetP = setP;
    }

    public String getEquipOperateStatus() {
        return EquipOperateStatus;
    }

    public void setEquipOperateStatus(String equipOperateStatus) {
        EquipOperateStatus = equipOperateStatus;
    }

    public String getEquipAlarmStatus() {
        return EquipAlarmStatus;
    }

    public void setEquipAlarmStatus(String equipAlarmStatus) {
        EquipAlarmStatus = equipAlarmStatus;
    }

    public String getScrEquipPower() {
        return ScrEquipPower;
    }

    public void setScrEquipPower(String scrEquipPower) {
        ScrEquipPower = scrEquipPower;
    }

    public String getPumpID() {
        return PumpID;
    }

    public void setPumpID(String pumpID) {
        PumpID = pumpID;
    }

    public String getVesion() {
        return Vesion;
    }

    public void setVesion(String vesion) {
        Vesion = vesion;
    }

    public String getFrequency() {
        return Frequency;
    }

    public void setFrequency(String frequency) {
        Frequency = frequency;
    }

    public String getElectric() {
        return Electric;
    }

    public void setElectric(String electric) {
        Electric = electric;
    }

    public String getVoltage() {
        return Voltage;
    }

    public void setVoltage(String voltage) {
        Voltage = voltage;
    }

    public String getTemperature() {
        return Temperature;
    }

    public void setTemperature(String temperature) {
        Temperature = temperature;
    }

    public String getError() {
        return Error;
    }

    public void setError(String error) {
        Error = error;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getAI1_ADC() {
        return AI1_ADC;
    }

    public void setAI1_ADC(String AI1_ADC) {
        this.AI1_ADC = AI1_ADC;
    }

    public String getAI2_ADC() {
        return AI2_ADC;
    }

    public void setAI2_ADC(String AI2_ADC) {
        this.AI2_ADC = AI2_ADC;
    }

    public String getTimer() {
        return Timer;
    }

    public void setTimer(String timer) {
        Timer = timer;
    }
}
