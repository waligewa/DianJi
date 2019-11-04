package com.example.motor.videoSurveillance;

import java.io.Serializable;

/**
 * Created by admin on 2017/9/7.
 */

public class MonitorInfo implements Serializable{

    /**
     * CameraID : 12
     * EquipmentID : 1472215084
     * IP : 222.173.103.228
     * Port : 37777
     * EquName : admin
     * EquPwd : 123123
     * IP2 : 222.173.103.228
     * Port2 : 10065
     * EquName2 : admin
     * EquPwd2 : admin
     * IP3 : 222.173.103.228
     * Port3 : 10063
     * EquName3 : admin
     * EquPwd3 : admin
     */

    private int CameraID;
    private long EquipmentID;
    private String IP;
    private String Port;
    private String EquName;
    private String EquPwd;
    private String IP2;
    private String Port2;
    private String EquName2;
    private String EquPwd2;
    private String IP3;
    private String Port3;
    private String EquName3;
    private String EquPwd3;

    public int getCameraID() {
        return CameraID;
    }

    public void setCameraID(int CameraID) {
        this.CameraID = CameraID;
    }

    public long getEquipmentID() {
        return EquipmentID;
    }

    public void setEquipmentID(long EquipmentID) {
        this.EquipmentID = EquipmentID;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getPort() {
        return Port;
    }

    public void setPort(String Port) {
        this.Port = Port;
    }

    public String getEquName() {
        return EquName;
    }

    public void setEquName(String EquName) {
        this.EquName = EquName;
    }

    public String getEquPwd() {
        return EquPwd;
    }

    public void setEquPwd(String EquPwd) {
        this.EquPwd = EquPwd;
    }

    public String getIP2() {
        return IP2;
    }

    public void setIP2(String IP2) {
        this.IP2 = IP2;
    }

    public String getPort2() {
        return Port2;
    }

    public void setPort2(String Port2) {
        this.Port2 = Port2;
    }

    public String getEquName2() {
        return EquName2;
    }

    public void setEquName2(String EquName2) {
        this.EquName2 = EquName2;
    }

    public String getEquPwd2() {
        return EquPwd2;
    }

    public void setEquPwd2(String EquPwd2) {
        this.EquPwd2 = EquPwd2;
    }

    public String getIP3() {
        return IP3;
    }

    public void setIP3(String IP3) {
        this.IP3 = IP3;
    }

    public String getPort3() {
        return Port3;
    }

    public void setPort3(String Port3) {
        this.Port3 = Port3;
    }

    public String getEquName3() {
        return EquName3;
    }

    public void setEquName3(String EquName3) {
        this.EquName3 = EquName3;
    }

    public String getEquPwd3() {
        return EquPwd3;
    }

    public void setEquPwd3(String EquPwd3) {
        this.EquPwd3 = EquPwd3;
    }
}
