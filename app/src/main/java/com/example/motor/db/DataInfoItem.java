package com.example.motor.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/11/27.
 */

public class DataInfoItem {

    /**此数据格式已废弃
     * DataName : PressureIN
     * DisplayName : 进水压力
     * ID : 2
     * DataValue : 2.1
     * OneShow : true
     * Until : Hz
     */

    /**
     * "ID": 3,
     * "DataID": 2000,
     * "CNName": "进水压力",
     * "ENName": "PressureIN",
     * "Unit": "Mpa",
     * "DataType": 1,
     * "DataRatio": 1.0,
     * "EquipmentType": 1,
     * "Isshow": true
     */
    private int ID;
    private long DataID;
    private String CNName;
    private String ENName;
    private String Unit;
    private int DataType;
    private double DataRatio;
    private int EquipmentType;
    private boolean Isshow;

    private boolean OnShow;
    private String DataValue;

    public static DataInfoItem objectFromData(String str) {

        return new Gson().fromJson(str, DataInfoItem.class);
    }

    public static DataInfoItem objectFromData(String str, String key) {

        try {
            JSONObject jsonObject = new JSONObject(str);

            return new Gson().fromJson(jsonObject.getString(str), DataInfoItem.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<DataInfoItem> arrayDataInfoItemFromData(String str) {

        Type listType = new TypeToken<ArrayList<DataInfoItem>>() {}.getType();

        return new Gson().fromJson(str, listType);
    }

    public String getDataValue() {
        return DataValue;
    }

    public void setDataValue(String dataValue) {
        DataValue = dataValue;
    }

    public long getDataID() {
        return DataID;
    }

    public void setDataID(long dataID) {
        DataID = dataID;
    }

    public String getCNName() {
        return CNName;
    }

    public void setCNName(String CNName) {
        this.CNName = CNName;
    }

    public String getENName() {
        return ENName;
    }

    public void setENName(String ENName) {
        this.ENName = ENName;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }

    public int getDataType() {
        return DataType;
    }

    public void setDataType(int dataType) {
        DataType = dataType;
    }

    public double getDataRatio() {
        return DataRatio;
    }

    public void setDataRatio(double dataRatio) {
        DataRatio = dataRatio;
    }

    public int getEquipmentType() {
        return EquipmentType;
    }

    public void setEquipmentType(int equipmentType) {
        EquipmentType = equipmentType;
    }

    public boolean isIsshow() {
        return Isshow;
    }

    public void setIsshow(boolean isshow) {
        Isshow = isshow;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUntil(String Until) {
        this.Unit = Until;
    }

    public boolean isOnShow() {
        return OnShow;
    }

    public void setOnShow(boolean onShow) {
        OnShow = onShow;
    }
}
