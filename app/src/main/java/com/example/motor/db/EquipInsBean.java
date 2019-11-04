package com.example.motor.db;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Author : yanftch
 * Date : 2018/4/19
 * Time : 18:31
 * Desc :
 */

public class EquipInsBean extends LitePalSupport implements Serializable {

    private String WOID;
    private String WOTitle;
    private String WOContent;
    private String WOState;
    private String WOIssuedDate;
    private String WOIssuedUser;
    private String WOReceiveDate;
    private String WOReceiveUser;
    private String WOItemsNum;
    private String WOPerformNum;
    private String WOBeginDate;
    private String WOEndDate;
    private String WOCreateDate;
    private String WOType;
    private String WOExpectedTime;
    private String UserName;
    private String ReceiveUser;
    private String Voice; // 语音信息
    private String PFID; // 计划流
    private String EquipmentID;
    private String DeviceName;
    private String IsIssue;
    private String IssueName;
    private int number;
    private String EquipmentNo;
    private String DevCheckID;

    public String getDevCheckID() {
        return DevCheckID;
    }

    public void setDevCheckID(String devCheckID) {
        DevCheckID = devCheckID;
    }

    public String getEquipmentNo() {
        return EquipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        EquipmentNo = equipmentNo;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getVoice() {
        return Voice;
    }

    public void setVoice(String voice) {
        Voice = voice;
    }

    public String getPFID() {
        return PFID;
    }

    public void setPFID(String PFID) {
        this.PFID = PFID;
    }

    public String getEquipmentID() {
        return EquipmentID;
    }

    public void setEquipmentID(String equipmentID) {
        EquipmentID = equipmentID;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public String getIsIssue() {
        return IsIssue;
    }

    public void setIsIssue(String isIssue) {
        IsIssue = isIssue;
    }

    public String getIssueName() {
        return IssueName;
    }

    public void setIssueName(String issueName) {
        IssueName = issueName;
    }

    public String getWOID() {
        return WOID;
    }

    public void setWOID(String WOID) {
        this.WOID = WOID;
    }

    public String getWOTitle() {
        return WOTitle;
    }

    public void setWOTitle(String WOTitle) {
        this.WOTitle = WOTitle;
    }

    public String getWOContent() {
        return WOContent;
    }

    public void setWOContent(String WOContent) {
        this.WOContent = WOContent;
    }

    public String getWOState() {
        return WOState;
    }

    public void setWOState(String WOState) {
        this.WOState = WOState;
    }

    public String getWOIssuedDate() {
        return WOIssuedDate;
    }

    public void setWOIssuedDate(String WOIssuedDate) {
        this.WOIssuedDate = WOIssuedDate;
    }

    public String getWOIssuedUser() {
        return WOIssuedUser;
    }

    public void setWOIssuedUser(String WOIssuedUser) {
        this.WOIssuedUser = WOIssuedUser;
    }

    public String getWOReceiveDate() {
        return WOReceiveDate;
    }

    public void setWOReceiveDate(String WOReceiveDate) {
        this.WOReceiveDate = WOReceiveDate;
    }

    public String getWOReceiveUser() {
        return WOReceiveUser;
    }

    public void setWOReceiveUser(String WOReceiveUser) {
        this.WOReceiveUser = WOReceiveUser;
    }

    public String getWOItemsNum() {
        return WOItemsNum;
    }

    public void setWOItemsNum(String WOItemsNum) {
        this.WOItemsNum = WOItemsNum;
    }

    public String getWOPerformNum() {
        return WOPerformNum;
    }

    public void setWOPerformNum(String WOPerformNum) {
        this.WOPerformNum = WOPerformNum;
    }

    public String getWOBeginDate() {
        return WOBeginDate;
    }

    public void setWOBeginDate(String WOBeginDate) {
        this.WOBeginDate = WOBeginDate;
    }

    public String getWOEndDate() {
        return WOEndDate;
    }

    public void setWOEndDate(String WOEndDate) {
        this.WOEndDate = WOEndDate;
    }

    public String getWOCreateDate() {
        return WOCreateDate;
    }

    public void setWOCreateDate(String WOCreateDate) {
        this.WOCreateDate = WOCreateDate;
    }

    public String getWOType() {
        return WOType;
    }

    public void setWOType(String WOType) {
        this.WOType = WOType;
    }

    public String getWOExpectedTime() {
        return WOExpectedTime;
    }

    public void setWOExpectedTime(String WOExpectedTime) {
        this.WOExpectedTime = WOExpectedTime;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getReceiveUser() {
        return ReceiveUser;
    }

    public void setReceiveUser(String receiveUser) {
        ReceiveUser = receiveUser;
    }
}
