package com.example.motor.videoSurveillance;

import java.io.Serializable;

/**
 * 这个是用于显示网关ip地址的实体类
 */

public class MonitorInfo2 implements Serializable{

    private String IP;
    private String Port;
    private String EquName;
    private String EquPwd;
    // 这是个标识的字符串，用来标识子项是手动添加进去的还是通过接口获得的
    private String Identification;

    public String getIdentification() {
        return Identification;
    }

    public void setIdentification(String identification) {
        Identification = identification;
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
}
