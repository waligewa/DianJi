package com.example.motor.db;

import java.io.Serializable;

// 用于权限分配使用
public class AuthorityRole implements Serializable {

    private int Num;
    private String ActionName;
    private int PNum;
    private String Name;
    private String ControllerName;
    private String Description;
    private String ImageUrl;

    public int getNum() {
        return Num;
    }

    public void setNum(int num) {
        Num = num;
    }

    public String getActionName() {
        return ActionName;
    }

    public void setActionName(String actionName) {
        ActionName = actionName;
    }

    public int getPNum() {
        return PNum;
    }

    public void setPNum(int PNum) {
        this.PNum = PNum;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getControllerName() {
        return ControllerName;
    }

    public void setControllerName(String controllerName) {
        ControllerName = controllerName;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }
}
