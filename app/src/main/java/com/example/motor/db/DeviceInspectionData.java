package com.example.motor.db;

/**
 * Created by admin on 2017/6/19.
 */
public class DeviceInspectionData {

    private String SBBH;
    private String GKMC;
    private String SheBXH;
    private String XJY;
    private String XJRQ;
    private String SRDY1;
    private String SRDY2;
    private String HJQK;
    private String ShuiBXH;
    private String SBZX;
    private String SDYL;
    private String SJYL;
    private String CYBH;
    private String JKFS;
    private String YSKJ;
    private String WSTJ;
    private String XLL;
    private String SBYX;
    private String DJDL1;
    private String DJDL2;
    private String BPQDL;
    private String RE11;
    private String RE21;
    private String RE31;
    private String RE41;
    private String RE12;
    private String RE22;
    private String RE32;
    private String RE42;
    private String QJKG;   // 前级开关压线情况
    private String DJXY;   // 电机压线情况
    private String JKLJ;   // 监控是否连接正常
    private String ZXLCD;  // 主线路触点压线情况
    private String LSFW;   // 水泵填料密封是否漏水在规定范围内
    private String LSXX;   // 水泵机械密封是否有漏水现象
    private String JCQ;    // 接触器是否灵活复位
    private String KZGLS;  // 控制柜内螺丝松动情况
    private String WJKZ;   // 微机控制系统接线是否良好
    private String BSD;    // 各水泵手动是否正常工作
    private String ZDZC;   // 自动能否正常启动
    private String KTJ;    // 开停机是否正常
    private String ZHF;    // 止回阀是否正常
    private String ZDJH;   // 自动交换是否正常
    private String YWJ;    // 液位计工作是否正常
    private String ZKBCQ;  // 真空补偿器各指示灯是否正常
    private String BPQGZ;  // 变频器工作是否正常
    private String SBFY;   // 设备是否产生负压
    private String GLQQX;  // 过滤器是否进行清洗
    private String XFC;    // 消防水池是否有水
    private String XFZX;   // 消防控制中心所处状态
    private String WYXJ;   // 无压巡检是否正常
    private String GWLS;   // 管网所有螺丝是否全面紧固一遍
    private String FQF;    // 浮球阀是否检查保养正常
    private String BENG1;
    private String BENG2;
    private String BENG3;
    private String BENG4;
    private String KZGBY;  // 控制柜保养记录
    private String SBGDBY; // 水泵及管道等保养记录
    private String ZJYXJL; // 整机运行记录
    private String YZGN;   // 巡检人员是否按以上试验功能
    private String ZQCZ;   // 用户能否正确操作
    private String YHDZ;   // 用户地址
    private String YHDH;   // 用户电话
    private String YHQZ;   // 用户签字

    public DeviceInspectionData(String GKMC, String SBBH, String SheBXH,
                                String XJRY, String XJRQ, String SRDY1, String SRDY2, String HJQK,
                                String pumpSBXH, String pumpSBZX, String SDYL, String SJYL, String CYBH,
                                String JKFS, String YSKJYL, String WSTJBH, String XLLBY, String SBYX,
                                String DJDL1, String DJDL2, String BPQDL, String RE11, String RE21, String RE31,
                                String RE41, String RE12, String RE22, String RE32, String RE42,
                                String radioButton1, String radioButton2, String radioButton3,
                                String radioButton4, String radioButton5, String radioButton6,
                                String radioButton7, String radioButton8, String radioButton9,
                                String radioButton10, String radioButton11, String radioButton12,
                                String radioButton13, String radioButton14, String radioButton15,
                                String radioButton16, String radioButton17, String radioButton18,
                                String radioButton19, String radioButton20, String radioButton21,
                                String radioButton22, String radioButton23, String radioButton24,
                                String BENG1, String BENG2, String BENG3, String BENG4, String KZGBY,
                                String SBGDBY, String ZJYXJL, String radioButton25, String radioButton26,
                                String YHDZ, String YHDH, String YHQZ) {
        this.GKMC = GKMC;
        this.SBBH = SBBH;
        this.SheBXH = SheBXH;
        this.XJY = XJRY;
        this.XJRQ = XJRQ;
        this.SRDY1 = SRDY1;
        this.SRDY2 = SRDY2;
        this.HJQK = HJQK;
        this.ShuiBXH = pumpSBXH;
        this.SBZX = pumpSBZX;
        this.SDYL = SDYL;
        this.SJYL = SJYL;
        this.CYBH = CYBH;
        this.JKFS = JKFS;
        this.YSKJ = YSKJYL;
        this.WSTJ = WSTJBH;
        this.XLL = XLLBY;
        this.SBYX = SBYX;
        this.DJDL1 = DJDL1;
        this.DJDL2 = DJDL2;
        this.BPQDL = BPQDL;
        this.RE11 = RE11;
        this.RE21 = RE21;
        this.RE31 = RE31;
        this.RE41 = RE41;
        this.RE12 = RE12;
        this.RE22 = RE22;
        this.RE32 = RE32;
        this.RE42 = RE42;
        this.QJKG = radioButton1;
        this.DJXY = radioButton2;
        this.JKLJ = radioButton3;
        this.ZXLCD = radioButton4;
        this.LSFW = radioButton5;
        this.LSXX = radioButton6;
        this.JCQ = radioButton7;
        this.KZGLS = radioButton8;
        this.WJKZ = radioButton9;
        this.BSD = radioButton10;
        this.ZDZC = radioButton11;
        this.KTJ = radioButton12;
        this.ZHF = radioButton13;
        this.ZDJH = radioButton14;
        this.YWJ = radioButton15;
        this.ZKBCQ = radioButton16;
        this.BPQGZ = radioButton17;
        this.SBFY = radioButton18;
        this.GLQQX = radioButton19;
        this.XFC = radioButton20;
        this.XFZX = radioButton21;
        this.WYXJ = radioButton22;
        this.GWLS = radioButton23;
        this.FQF = radioButton24;
        this.BENG1 = BENG1;
        this.BENG2 = BENG2;
        this.BENG3 = BENG3;
        this.BENG4 = BENG4;
        this.KZGBY = KZGBY;
        this.SBGDBY = SBGDBY;
        this.ZJYXJL = ZJYXJL;
        this.YZGN = radioButton25;
        this.ZQCZ = radioButton26;
        this.YHDZ = YHDZ;
        this.YHDH = YHDH;
        this.YHQZ = YHQZ;
    }
}
