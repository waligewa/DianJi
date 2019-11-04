package com.example.motor.db;

import org.json.JSONException;
import org.json.JSONObject;

public class CommonResponseBean {

    private String Message;
    private int code;
    private String guid;
    private String data;
    private String role;
    private String soft_key, RegionNo; // RegionNo为区域的id

    public String getRegionNo() {
        return RegionNo;
    }

    public void setRegionNo(String regionNo) {
        RegionNo = regionNo;
    }

    public String getSoft_key() {
        return soft_key;
    }

    public void setSoft_key(String soft_key) {
        this.soft_key = soft_key;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
    // 得到一个实体类
    public CommonResponseBean getcommenHit(String result){
        CommonResponseBean commenHint = new CommonResponseBean();
        try {
            JSONObject jsonObject = new JSONObject(result);
            commenHint.setMessage(jsonObject.getString("Message"));
            commenHint.setCode(jsonObject.getInt("Code"));
            if (jsonObject.has("Data")) {
                commenHint.setData(jsonObject.getString("Data"));
            }
            if (jsonObject.has("guid")) {
                commenHint.setGuid(jsonObject.getString("guid"));
            }
            if (jsonObject.has("Role")) {
                commenHint.setRole(jsonObject.getString("Role"));
            }
            if (jsonObject.has("soft_key")){
                commenHint.setSoft_key(jsonObject.getString("soft_key"));
            }
            if (jsonObject.has("RegionNo")){
                commenHint.setRegionNo(jsonObject.getString("RegionNo"));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return commenHint;
    }
}
