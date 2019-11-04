package com.example.motor.db;

import java.io.Serializable;

/**
 * 无网状态下提交数据的更新设备状态的子项
 *
 */

public class InspectionOffineStateItem implements Serializable {

    private String workorder;
    private String detail;
    private String guid;

    public String getWorkorder() {
        return workorder;
    }

    public void setWorkorder(String workorder) {
        this.workorder = workorder;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
