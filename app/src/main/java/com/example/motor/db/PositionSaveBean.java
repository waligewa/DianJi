package com.example.motor.db;

/**
 * Created by admin on 2019/3/21.
 */

public class PositionSaveBean {

    private String equipmentId;                         // 设备id
    private String longitude;                           // 经度
    private String latitude;                            // 纬度

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
