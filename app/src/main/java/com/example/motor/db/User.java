package com.example.motor.db;

/**
 * Author : yanftch
 * Date : 2018/3/10
 * Time : 20:31
 * Desc : 登录用户的信息实体类
 */

public class User {

    /**
     * UserID : 1000004
     * UserName : 1000004
     * UserPassWord : CD5IcveA4Ss=
     * IsAdmin : null
     * IsLocked : null
     * AdminLevel : null
     * DepartMent : 1
     * Role : 11
     * UserFullName : 1000004
     * UserEmail : null
     * CreatedTime : null
     * InfoLevel : null
     * IMEI : 867323020957815
     * WeChatID : null
     * Phone : null
     * Describe : null
     * OpenID : null
     */

    private int UserID;
    private String UserName;
    private String UserPassWord;
    private Object IsAdmin;
    private Object IsLocked;
    private Object AdminLevel;
    private int DepartMent;
    private int Role;
    private String UserFullName;
    private Object UserEmail;
    private Object CreatedTime;
    private Object InfoLevel;
    private String IMEI;
    private Object WeChatID;
    private Object Phone;
    private Object Describe;
    private Object OpenID;

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int UserID) {
        this.UserID = UserID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public String getUserPassWord() {
        return UserPassWord;
    }

    public void setUserPassWord(String UserPassWord) {
        this.UserPassWord = UserPassWord;
    }

    public Object getIsAdmin() {
        return IsAdmin;
    }

    public void setIsAdmin(Object IsAdmin) {
        this.IsAdmin = IsAdmin;
    }

    public Object getIsLocked() {
        return IsLocked;
    }

    public void setIsLocked(Object IsLocked) {
        this.IsLocked = IsLocked;
    }

    public Object getAdminLevel() {
        return AdminLevel;
    }

    public void setAdminLevel(Object AdminLevel) {
        this.AdminLevel = AdminLevel;
    }

    public int getDepartMent() {
        return DepartMent;
    }

    public void setDepartMent(int DepartMent) {
        this.DepartMent = DepartMent;
    }

    public int getRole() {
        return Role;
    }

    public void setRole(int Role) {
        this.Role = Role;
    }

    public String getUserFullName() {
        return UserFullName;
    }

    public void setUserFullName(String UserFullName) {
        this.UserFullName = UserFullName;
    }

    public Object getUserEmail() {
        return UserEmail;
    }

    public void setUserEmail(Object UserEmail) {
        this.UserEmail = UserEmail;
    }

    public Object getCreatedTime() {
        return CreatedTime;
    }

    public void setCreatedTime(Object CreatedTime) {
        this.CreatedTime = CreatedTime;
    }

    public Object getInfoLevel() {
        return InfoLevel;
    }

    public void setInfoLevel(Object InfoLevel) {
        this.InfoLevel = InfoLevel;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public Object getWeChatID() {
        return WeChatID;
    }

    public void setWeChatID(Object WeChatID) {
        this.WeChatID = WeChatID;
    }

    public Object getPhone() {
        return Phone;
    }

    public void setPhone(Object Phone) {
        this.Phone = Phone;
    }

    public Object getDescribe() {
        return Describe;
    }

    public void setDescribe(Object Describe) {
        this.Describe = Describe;
    }

    public Object getOpenID() {
        return OpenID;
    }

    public void setOpenID(Object OpenID) {
        this.OpenID = OpenID;
    }

    @Override
    public String toString() {
        return "User{" +
                "UserID=" + UserID +
                ", UserName='" + UserName + '\'' +
                ", UserPassWord='" + UserPassWord + '\'' +
                ", IsAdmin=" + IsAdmin +
                ", IsLocked=" + IsLocked +
                ", AdminLevel=" + AdminLevel +
                ", DepartMent=" + DepartMent +
                ", Role=" + Role +
                ", UserFullName='" + UserFullName + '\'' +
                ", UserEmail=" + UserEmail +
                ", CreatedTime=" + CreatedTime +
                ", InfoLevel=" + InfoLevel +
                ", IMEI='" + IMEI + '\'' +
                ", WeChatID=" + WeChatID +
                ", Phone=" + Phone +
                ", Describe=" + Describe +
                ", OpenID=" + OpenID +
                '}';
    }
}
