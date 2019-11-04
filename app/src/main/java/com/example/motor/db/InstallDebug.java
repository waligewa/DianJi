package com.example.motor.db;

/**
 * Created by 赵彬彬 on 2018/4/23.
 */

public class InstallDebug {

    private String ID;  // 设备编号111
    private String Area;  // 所在地区
    private String EngineeringPlace; // 工程地点
    private String EquipmentNum;  // 设备编号111
    private String DeviceName;  // 站点名称
    private String VillageName;  // 用户名称
    private String CustomerAddress;  // 用户地址
    private String EquipmentType;  // 设备类型
    private String ArrivalDate;  // 到达日期
    private String MachineUser;  // 机器安装人员
    private String MachineDate;  // 机器安装日期
    private String ElectricalUser;  // 电气安装人员
    private String ElectricalDate;  // 电气安装日期
    private String DebuggDate;  // 设备调试人员
    private String DebuggUser;  // 设备调试日期
    private String LocalData;  // true
    private String Manufacturer;  // 设备生产厂商
    private String Conclusion;  // 安装调试结论
    private String Feedback;  // 安装调试反馈
    private String Mesures;  // 反馈措施

    public InstallDebug(String ID, String Area, String EngineeringPlace, String EquipmentNum,
                        String DeviceName, String VillageName, String CustomerAddress, String EquipmentType,
                        String ArrivalDate, String MachineUser, String MachineDate, String ElectricalUser,
                        String ElectricalDate, String DebuggDate, String DebuggUser, String LocalData,
                        String Manufacturer, String Conclusion, String Feedback, String Mesures){
        this.ID = ID;
        this.Area = Area;
        this.EngineeringPlace = EngineeringPlace;
        this.EquipmentNum = EquipmentNum;
        this.DeviceName = DeviceName;
        this.VillageName = VillageName;
        this.CustomerAddress = CustomerAddress;
        this.EquipmentType = EquipmentType;
        this.ArrivalDate = ArrivalDate;
        this.MachineUser = MachineUser;
        this.MachineDate = MachineDate;
        this.ElectricalUser = ElectricalUser;
        this.ElectricalDate = ElectricalDate;
        this.DebuggDate = DebuggDate;
        this.DebuggUser = DebuggUser;
        this.LocalData = LocalData;
        this.Manufacturer = Manufacturer;
        this.Conclusion = Conclusion;
        this.Feedback = Feedback;
        this.Mesures = Mesures;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getArea() {
        return Area;
    }

    public void setArea(String area) {
        Area = area;
    }

    public String getEngineeringPlace() {
        return EngineeringPlace;
    }

    public void setEngineeringPlace(String engineeringPlace) {
        EngineeringPlace = engineeringPlace;
    }

    public String getEquipmentNum() {
        return EquipmentNum;
    }

    public void setEquipmentNum(String equipmentNum) {
        EquipmentNum = equipmentNum;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public String getVillageName() {
        return VillageName;
    }

    public void setVillageName(String villageName) {
        VillageName = villageName;
    }

    public String getCustomerAddress() {
        return CustomerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        CustomerAddress = customerAddress;
    }

    public String getEquipmentType() {
        return EquipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        EquipmentType = equipmentType;
    }

    public String getArrivalDate() {
        return ArrivalDate;
    }

    public void setArrivalDate(String arrivalDate) {
        ArrivalDate = arrivalDate;
    }

    public String getMachineUser() {
        return MachineUser;
    }

    public void setMachineUser(String machineUser) {
        MachineUser = machineUser;
    }

    public String getMachineDate() {
        return MachineDate;
    }

    public void setMachineDate(String machineDate) {
        MachineDate = machineDate;
    }

    public String getElectricalUser() {
        return ElectricalUser;
    }

    public void setElectricalUser(String electricalUser) {
        ElectricalUser = electricalUser;
    }

    public String getElectricalDate() {
        return ElectricalDate;
    }

    public void setElectricalDate(String electricalDate) {
        ElectricalDate = electricalDate;
    }

    public String getDebuggDate() {
        return DebuggDate;
    }

    public void setDebuggDate(String debuggDate) {
        DebuggDate = debuggDate;
    }

    public String getDebuggUser() {
        return DebuggUser;
    }

    public void setDebuggUser(String debuggUser) {
        DebuggUser = debuggUser;
    }

    public String getLocalData() {
        return LocalData;
    }

    public void setLocalData(String localData) {
        LocalData = localData;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        Manufacturer = manufacturer;
    }

    public String getConclusion() {
        return Conclusion;
    }

    public void setConclusion(String conclusion) {
        Conclusion = conclusion;
    }

    public String getFeedback() {
        return Feedback;
    }

    public void setFeedback(String feedback) {
        Feedback = feedback;
    }

    public String getMesures() {
        return Mesures;
    }

    public void setMesures(String mesures) {
        Mesures = mesures;
    }
}
