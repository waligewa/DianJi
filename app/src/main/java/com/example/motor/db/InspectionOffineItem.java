package com.example.motor.db;

import java.io.Serializable;

/**
 * 无网状态下提交数据的提交数据的子项
 *
 */

public class InspectionOffineItem implements Serializable {

    private String guid;
    private String data;
    private String userId;
    private String WOID;
    private String EquipmentID;
    private String EquipmentNo;
    private String Worker;
    private String GISLocation;
    private String EID;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWOID() {
        return WOID;
    }

    public void setWOID(String WOID) {
        this.WOID = WOID;
    }

    public String getEquipmentID() {
        return EquipmentID;
    }

    public void setEquipmentID(String equipmentID) {
        EquipmentID = equipmentID;
    }

    public String getEquipmentNo() {
        return EquipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        EquipmentNo = equipmentNo;
    }

    public String getWorker() {
        return Worker;
    }

    public void setWorker(String worker) {
        Worker = worker;
    }

    public String getGISLocation() {
        return GISLocation;
    }

    public void setGISLocation(String GISLocation) {
        this.GISLocation = GISLocation;
    }

    public String getEID() {
        return EID;
    }

    public void setEID(String EID) {
        this.EID = EID;
    }
}
