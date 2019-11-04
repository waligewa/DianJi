package com.example.motor.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.activity.DeleteDeviceReportActivity;
import com.example.motor.activity.PerfectInformationActivity;
import com.example.motor.activity.EquipmentInspectionActivity;
import com.example.motor.activity.EquipmentMaintenanceActivity;
import com.example.motor.activity.InstallInformationActivity;
import com.example.motor.activity.MaintenanceManagementActivity;
import com.example.motor.activity.SatisfactionSurveyActivity;
import com.example.motor.db.AuthorityRole;
import com.example.motor.util.DoubleClickUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import org.xutils.x;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Author : 赵彬彬
 * Date   : 2019/5/18
 * Time   : 9:57
 * Desc   : Created by chengxi on 17/4/26.5个子项名字改的一塌糊涂，是为了与iOS对齐，然后动态添加，6月13号提出来要动态添加服务界面的这些功能，有的给，有的不给，这样就造成了这5个activity名称很差劲,这个实现方法就是3个保留的下面放了一个ListView，有实体类、大布局、小布局、适配器就成了。
 */
public class ServiceFragment extends Fragment implements View.OnClickListener{

    private ImageView scan;
    private Intent intent;
    private LinearLayout equipmentDetails, equipmentReportRepair, perfectInformation,
            installInformation, equipmentInspection, equipmentRepair;
    private SharedPreferences prefs1, prefs2;
    private String authorityRole;
    private List<AuthorityRole> list1 = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_service_fragment, null);
        x.view().inject(getActivity());
        prefs1 = getActivity().getSharedPreferences("device", Context.MODE_PRIVATE);
        prefs2 = getActivity().getSharedPreferences("UserInfo", 0); // 创建一个sp文件
        scan = (ImageView) view.findViewById(R.id.scan_scan);
        scan.setOnClickListener(this);
        equipmentDetails = (LinearLayout) view.findViewById(R.id.equipment_details);
        equipmentDetails.setOnClickListener(this); // 设备详情
        perfectInformation = (LinearLayout) view.findViewById(R.id.perfect_information);
        perfectInformation.setOnClickListener(this); // 完善信息
        installInformation = (LinearLayout) view.findViewById(R.id.install_information);
        installInformation.setOnClickListener(this); // 安装信息

        equipmentInspection = (LinearLayout) view.findViewById(R.id.equipment_inspection);
        equipmentInspection.setOnClickListener(this); // 设备巡检
        equipmentRepair = (LinearLayout) view.findViewById(R.id.equipment_repair);
        equipmentRepair.setOnClickListener(this); // 设备维修
        equipmentReportRepair = (LinearLayout) view.findViewById(R.id.equipment_report_repair);
        equipmentReportRepair.setOnClickListener(this); // 设备报修
        init();
        return view;
    }

    private void init(){
        intent = new Intent();
        // 得到登录界面登录成功之后添加到SP中的值
        authorityRole = prefs2.getString("authorityrole", "");
        getData();
    }

    private void getData(){
        list1.clear();
        // 将role里面的数据全部提出来,然后全部存储到sp里面去
        Type listType = new TypeToken<List<AuthorityRole>>() {}.getType();
        Gson gson1 = new Gson();
        List<AuthorityRole> list2 = gson1.fromJson(authorityRole, listType);
        if (list2 == null || list2.isEmpty()) return;
        for(int i = 0; i < list2.size(); i++){
            if(list2.get(i).getActionName() != null){
                if(list2.get(i).getActionName().equals("LoadWoRtuByWOID")){
                    // 完善信息
                    perfectInformation.setVisibility(View.VISIBLE);
                } else if(list2.get(i).getActionName().equals("createInspectionView")){
                    // 设备巡检
                    equipmentInspection.setVisibility(View.VISIBLE);
                } else if (list2.get(i).getActionName().equals("createFixTryView")){
                    // 安装信息
                    installInformation.setVisibility(View.VISIBLE);
                } else if (list2.get(i).getActionName().equals("createRepairInformationView")){
                    // 设备维修
                    equipmentRepair.setVisibility(View.VISIBLE);
                }
            }
        }
        list1.addAll(list2);
    }

    public void onClick(View v){
        String deviceidString = prefs1.getString("deviceId", "");
        switch (v.getId()) {
            case R.id.scan_scan:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    getActivity().startActivityForResult(new Intent(getActivity(), CaptureActivity.class),
                            1);
                }
                break;
            // 设备详情
            case R.id.equipment_details:
                if (deviceidString.length() < 2) {
                    toast("请在首页点击左上角，选择相应设备");
                } else if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), MaintenanceManagementActivity.class);
                    startActivity(intent);
                }
                break;
            // 完善信息 就是第二个
            case R.id.perfect_information:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), PerfectInformationActivity.class);
                    startActivity(intent);
                }
                break;
            // 设备巡检 就是第三个
            case R.id.equipment_inspection:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), EquipmentInspectionActivity.class);
                    startActivity(intent);
                }
                break;
            // 安装信息 就是第四个
            case R.id.install_information:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), InstallInformationActivity.class);
                    startActivity(intent);
                }
                break;
            // 设备维修 就是第五个
            case R.id.equipment_repair:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), EquipmentMaintenanceActivity.class);
                    startActivity(intent);
                }
                break;
            // 设备报修
            case R.id.equipment_report_repair:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), DeleteDeviceReportActivity.class);
                    startActivity(intent);
                }
                break;
            // 满意度调查
            case R.id.satisfaction_survey:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), SatisfactionSurveyActivity.class);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    // Toast的封装
    private void toast(String string1){
        Toast.makeText(getActivity(), string1, Toast.LENGTH_SHORT).show();
    }
}
