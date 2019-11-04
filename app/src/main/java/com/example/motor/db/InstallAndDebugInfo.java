package com.example.motor.db;

/**
 * Created by admin on 2017/6/27.
 */
public class InstallAndDebugInfo {

    /**
     * ID :
     * Area :              地区；
     * EngineeringPlace :  工程地点；
     * EquipmentNum :      设备数；
     * DeviceName :        设备名称；
     * VillageName :       村名；
     * CustomerAddress :   客户地址；
     * EquipmentType :     设备类型；
     * ArrivalDate :       到达日期；
     * MachineUser :       机器安装人员；
     * MachineDate :       机器安装日期；
     * ElectricalUser :    电力用户；
     * ElectricalDate :    电日期；
     * DebuggDate :        Debugg Date；
     * DebuggUser :        Debugg User；
     * LocalData :         本地数据；
     * ManufacturerName :  制造商名称；
     * Conclusion:         结论；
     * Feedback :          反馈；
     * Mesures :           措施；
     * Manufacturer:       制造商；
     * {"Code":1,"Message":"获取安装调试信息成功",
     * "Data":[
      {
      "ID":39956,
      "Area":"浙江",
      "EngineeringPlace":"浙江",
      "EquipmentNum":"1506016",
      "DeviceName":"温岭市大溪镇人民政府",
      "VillageName":"潘郎泵站",
      "CustomerAddress":"浙江省台州市温岭市大溪镇人民政府",
      "EquipmentType":"WWG75-33-4",
      "ArrivalDate":"2015-08-24T00:00:00",
      "MachineUser":"",
      "MachineDate":"2015-09-24T00:00:00",
      "ElectricalUser":"",
      "ElectricalDate":"2015-11-14T00:00:00",
      "DebuggDate":"2015-12-23T00:00:00",
      "DebuggUser":"",
      "LocalData":null,
      "ManufacturerName":null,
      "Conclusion":null,
      "Feedback":null,
      "Mesures":null,
      "Manufacturer":null}
     * ]}
     */

    private int ID;
    private String Area;
    private String EngineeringPlace;
    private String EquipmentNum;
    private String DeviceName;
    private String VillageName;
    private String CustomerAddress;
    private String EquipmentType;
    private String ArrivalDate;
    private String MachineUser;
    private String MachineDate;
    private String ElectricalUser;
    private String ElectricalDate;
    private String DebuggDate;
    private String DebuggUser;
    private boolean LocalData;
    private String ManufacturerName;
    private String Conclusion;
    private String Feedback;
    private String Mesures;
    private int Manufacturer;

    public InstallAndDebugInfo() {
    }

    public InstallAndDebugInfo(int ID, String area, String engineeringPlace, String equipmentNum, String deviceName, String villageName, String customerAddress, String equipmentType, String arrivalDate, String machineUser, String machineDate, String electricalUser, String electricalDate, String debuggDate, String debuggUser, boolean localData, String manufacturerName, String conclusion, String feedback, String mesures, int manufacturer) {
        this.ID = ID;
        Area = area;
        EngineeringPlace = engineeringPlace;
        EquipmentNum = equipmentNum;
        DeviceName = deviceName;
        VillageName = villageName;
        CustomerAddress = customerAddress;
        EquipmentType = equipmentType;
        ArrivalDate = arrivalDate;
        MachineUser = machineUser;
        MachineDate = machineDate;
        ElectricalUser = electricalUser;
        ElectricalDate = electricalDate;
        DebuggDate = debuggDate;
        DebuggUser = debuggUser;
        LocalData = localData;
        ManufacturerName = manufacturerName;
        Conclusion = conclusion;
        Feedback = feedback;
        Mesures = mesures;
        Manufacturer = manufacturer;
    }


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getArea() {
        return Area;
    }

    public void setArea(String Area) {
        this.Area = Area;
    }

    public String getEngineeringPlace() {
        return EngineeringPlace;
    }

    public void setEngineeringPlace(String EngineeringPlace) {
        this.EngineeringPlace = EngineeringPlace;
    }

    public String getEquipmentNum() {
        return EquipmentNum;
    }

    public void setEquipmentNum(String EquipmentNum) {
        this.EquipmentNum = EquipmentNum;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String DeviceName) {
        this.DeviceName = DeviceName;
    }

    public String getVillageName() {
        return VillageName;
    }

    public void setVillageName(String VillageName) {
        this.VillageName = VillageName;
    }

    public String getCustomerAddress() {
        return CustomerAddress;
    }

    public void setCustomerAddress(String CustomerAddress) {
        this.CustomerAddress = CustomerAddress;
    }

    public String getEquipmentType() {
        return EquipmentType;
    }

    public void setEquipmentType(String EquipmentType) {
        this.EquipmentType = EquipmentType;
    }

    public String getArrivalDate() {
        return ArrivalDate;
    }

    public void setArrivalDate(String ArrivalDate) {
        this.ArrivalDate = ArrivalDate;
    }

    public String getMachineUser() {
        return MachineUser;
    }

    public void setMachineUser(String MachineUser) {
        this.MachineUser = MachineUser;
    }

    public String getMachineDate() {
        return MachineDate;
    }

    public void setMachineDate(String MachineDate) {
        this.MachineDate = MachineDate;
    }

    public String getElectricalUser() {
        return ElectricalUser;
    }

    public void setElectricalUser(String ElectricalUser) {
        this.ElectricalUser = ElectricalUser;
    }

    public String getElectricalDate() {
        return ElectricalDate;
    }

    public void setElectricalDate(String ElectricalDate) {
        this.ElectricalDate = ElectricalDate;
    }

    public String getDebuggDate() {
        return DebuggDate;
    }

    public void setDebuggDate(String DebuggDate) {
        this.DebuggDate = DebuggDate;
    }

    public String getDebuggUser() {
        return DebuggUser;
    }

    public void setDebuggUser(String DebuggUser) {
        this.DebuggUser = DebuggUser;
    }

    public boolean isLocalData() {
        return LocalData;
    }

    public void setLocalData(boolean LocalData) {
        this.LocalData = LocalData;
    }

    public String getManufacturerName() {
        return ManufacturerName;
    }

    public void setManufacturerName(String ManufacturerName) {
        this.ManufacturerName = ManufacturerName;
    }

    public String getConclusion() {
        return Conclusion;
    }

    public void setConclusion(String Conclusion) {
        this.Conclusion = Conclusion;
    }

    public String getFeedback() {
        return Feedback;
    }

    public void setFeedback(String Feedback) {
        this.Feedback = Feedback;
    }

    public String getMesures() {
        return Mesures;
    }

    public void setMesures(String Mesures) {
        this.Mesures = Mesures;
    }

    public int getManufacturer() {
        return Manufacturer;
    }

    public void setManufacturer(int Manufacturer) {
        this.Manufacturer = Manufacturer;
    }
}
