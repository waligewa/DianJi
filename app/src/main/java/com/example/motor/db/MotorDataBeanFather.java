package com.example.motor.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2019/3/21.
 */

public class MotorDataBeanFather {


    /**
     * "PumpNum": 2,
     * "EquipmentID": 1554273551926,
     * "UpdateTime": "2019-06-27T09:35:38.285Z",
     * "ComState": "True",
     * "IsOnLine": "True",
     * "WD1": "36.73",
     * "WD2": "4904",
     * "JD1": "120.01",
     * "JD2": "4941",
     * "Pumplist": [{
     * 			"Vesion1": "0",
     * 			"Frequency1": "0",
     * 			"Electric1": "0",
     * 			"Voltage1": "0",
     * 			"Temperature1": "0",
     * 			"Error1": "0",
     * 			"State1": "0",
     * 			"AI1_ADC1": "0",
     * 			"AI2_ADC1": "0",
     * 			"Timer1": "0"
     *                }, {
     * 			"Vesion2": "1.57",
     * 			"Frequency2": "0",
     * 			"Electric2": "0",
     * 			"Voltage2": "0",
     * 			"Temperature2": "15.9",
     * 			"Error2": "0",
     * 			"State2": "0",
     * 			"AI1_ADC2": "0",
     * 			"AI2_ADC2": "0",
     * 			"Timer2": "40"
     *        }]
     */
    

    private String PumpNum;
    private String EquipmentID;
    private String UpdateTime;
    private String ComState;
    private String IsOnLine;
    private String WD1;
    private String WD2;
    private String JD1;
    private String JD2;
    private String Pumplist;

    public static MotorDataBeanFather objectFromData(String str) {
        return new Gson().fromJson(str, MotorDataBeanFather.class);
    }

    public static MotorDataBeanFather objectFromData(String str, String key) {
        try {
            JSONObject jsonObject = new JSONObject(str);
            return new Gson().fromJson(jsonObject.getString(str), MotorDataBeanFather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<MotorDataBeanFather> arrayMotorDataBeanFromData(String str) {
        Type listType = new TypeToken<ArrayList<MotorDataBeanFather>>() {}.getType();
        return new Gson().fromJson(str, listType);
    }

    public static List<MotorDataBeanFather> arrayMotorDataBeanFromData(String str, String key) {

        try {
            JSONObject jsonObject = new JSONObject(str);
            Type listType = new TypeToken<ArrayList<MotorDataBeanFather>>() {
            }.getType();
            return new Gson().fromJson(jsonObject.getString(str), listType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

    public String getPumpNum() {
        return PumpNum;
    }

    public void setPumpNum(String pumpNum) {
        PumpNum = pumpNum;
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

    public String getComState() {
        return ComState;
    }

    public void setComState(String comState) {
        ComState = comState;
    }

    public String getIsOnLine() {
        return IsOnLine;
    }

    public void setIsOnLine(String isOnLine) {
        IsOnLine = isOnLine;
    }

    public String getWD1() {
        return WD1;
    }

    public void setWD1(String WD1) {
        this.WD1 = WD1;
    }

    public String getWD2() {
        return WD2;
    }

    public void setWD2(String WD2) {
        this.WD2 = WD2;
    }

    public String getJD1() {
        return JD1;
    }

    public void setJD1(String JD1) {
        this.JD1 = JD1;
    }

    public String getJD2() {
        return JD2;
    }

    public void setJD2(String JD2) {
        this.JD2 = JD2;
    }

    public String getPumplist() {
        return Pumplist;
    }

    public void setPumplist(String pumplist) {
        Pumplist = pumplist;
    }
}
