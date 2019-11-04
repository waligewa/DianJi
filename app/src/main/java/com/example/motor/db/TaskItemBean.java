package com.example.motor.db;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * Author : yanftch
 * Date : 2018/3/21
 * Time : 10:23
 * Desc :
 */

public class TaskItemBean extends LitePalSupport implements Serializable {

    /**
     * 多选模式下，是否选中
     */
    private boolean isMultipleChoiceSelected;

    public boolean isMultipleChoiceSelected() {
        return isMultipleChoiceSelected;
    }

    public TaskItemBean setMultipleChoiceSelected(boolean multipleChoiceSelected) {
        isMultipleChoiceSelected = multipleChoiceSelected;
        return this;
    }

    private boolean showPanel;  // 面板

    public boolean isShowPanel() {
        return showPanel;
    }

    public TaskItemBean setShowPanel(boolean showPanel) {
        this.showPanel = showPanel;
        return this;
    }

    // 以下17条数据是待办任务列表里面的子项的字段
    private String WOID;   // json字符串里面的WOID
    private String WOTitle;  // 标题  WOTitle
    private String WOContent;   // 基本描述信息  WOContent
    private String Voice; // 语音信息
    private String WOState; // WOState
    private String WOIssuedDate;  // WOIssuedDate
    private String WOIssuedUser;  // WOIssuedUser
    private String WOReceiveDate;  // WOReceiveDate
    private String WOReceiveUser;  // WOReceiveUser
    private String WOItemsNum;  // WOItemsNum
    private String WOPerformNum;  // WOPerformNum
    private String WOBeginDate;  // WOBeginDate
    private String WOEndDate;  // WOEndDate
    private String WOCreateDate;  // WOCreateDate
    private String WOType;  // WOType
    private String WOExpectedTime;  // WOExpectedTime
    private String UserName;  // UserName
    private String ReceiveUser;  // ReceiveUser
    private String FBID;
    private String PFID; // 计划流
    private String EquipmentID;
    private String DeviceName;
    private String IsIssue;
    private String IssueName;
    private String ParentIssueCount;
    private String IssueCount;
    private String IssueDetail;
    private String WOFeedback;
    private String WOProState;
    private String WODetail;
    private String EquipmentNo;
    private String DevCheckID;

    public String getFBID() {
        return FBID;
    }

    public void setFBID(String FBID) {
        this.FBID = FBID;
    }

    public String getParentIssueCount() {
        return ParentIssueCount;
    }

    public void setParentIssueCount(String parentIssueCount) {
        ParentIssueCount = parentIssueCount;
    }

    public String getIssueCount() {
        return IssueCount;
    }

    public void setIssueCount(String issueCount) {
        IssueCount = issueCount;
    }

    public String getIssueDetail() {
        return IssueDetail;
    }

    public void setIssueDetail(String issueDetail) {
        IssueDetail = issueDetail;
    }

    public String getWOFeedback() {
        return WOFeedback;
    }

    public void setWOFeedback(String WOFeedback) {
        this.WOFeedback = WOFeedback;
    }

    public String getWOProState() {
        return WOProState;
    }

    public void setWOProState(String WOProState) {
        this.WOProState = WOProState;
    }

    public String getWODetail() {
        return WODetail;
    }

    public void setWODetail(String WODetail) {
        this.WODetail = WODetail;
    }

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
