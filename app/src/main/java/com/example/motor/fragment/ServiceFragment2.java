package com.example.motor.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.activity.DeleteDeviceReportActivity;
import com.example.motor.activity.EquipmentInspectionActivity;
import com.example.motor.activity.EquipmentMaintenanceActivity;
import com.example.motor.activity.InstallInformationActivity;
import com.example.motor.activity.MaintenanceManagementActivity;
import com.example.motor.activity.PerfectInformationActivity;
import com.example.motor.activity.SatisfactionSurveyActivity;
import com.example.motor.adapter.common.CommonAdapter;
import com.example.motor.adapter.common.ViewHolder;
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
 *此fragment是2018年10月22日修改服务界面布局使用的一个fragment，因为沈工提出要将几个功能块进行分类，
 * 因此对此fragment进行重新布局，就是要改变ServiceFragment，这个ServiceFragment2是用来备用，以备不时之需
 *
 */

public class ServiceFragment2 extends Fragment implements View.OnClickListener{

    private ImageView scan;
    private Intent intent;
    private LinearLayout equipmentDetails, equipmentReportRepair, satisfactionSurvey;
    private SharedPreferences prefs1, prefs2;
    private String authorityRole;
    private List<AuthorityRole> list1 = new ArrayList<>();
    private ListView listView;
    private CommonAdapter<AuthorityRole> mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_service_fragment2, null);
        x.view().inject(getActivity());
        prefs1 = getActivity().getSharedPreferences("device", Context.MODE_PRIVATE);
        prefs2 = getActivity().getSharedPreferences("UserInfo", 0); // 创建一个偏好文件
        scan = (ImageView) view.findViewById(R.id.scan_scan);
        scan.setOnClickListener(this);
        equipmentDetails = (LinearLayout) view.findViewById(R.id.equipment_details);
        equipmentDetails.setOnClickListener(this);
        equipmentReportRepair = (LinearLayout) view.findViewById(R.id.equipment_report_repair);
        equipmentReportRepair.setOnClickListener(this);
        satisfactionSurvey = (LinearLayout) view.findViewById(R.id.satisfaction_survey);
        satisfactionSurvey.setOnClickListener(this);
        listView = (ListView) view.findViewById(R.id.listview1);
        intent = new Intent();
        init();
        return view;
    }

    private void init(){
        // 得到登录界面登录成功之后添加到SP中的值
        authorityRole = prefs2.getString("authorityrole", "");
        // 适配器的赋值
        mAdapter = new CommonAdapter<AuthorityRole>(getActivity(), list1, R.layout.authority_item) {
            @Override
            public void convert(ViewHolder holder, AuthorityRole item) {
                // 这两个字段，标题和描述信息通过后台给与
                if(!TextUtils.isEmpty(item.getName())){
                    holder.setText(R.id.title1, item.getName());
                }
                if(!TextUtils.isEmpty(item.getDescription())){
                    holder.setText(R.id.description1, item.getDescription());
                }
                // 完善信息
                if(item.getActionName().equals("LoadWoRtuByWOID")){
                    holder.setImageResource(R.id.imageView, R.mipmap.information);
                }
                // 设备巡检
                if(item.getActionName().equals("createInspectionView")){
                    holder.setImageResource(R.id.imageView, R.mipmap.inspection);
                }
                // 安装信息
                if(item.getActionName().equals("createFixTryView")){
                    holder.setImageResource(R.id.imageView, R.mipmap.fixmessage);
                }
                // 设备维修
                if(item.getActionName().equals("createRepairInformationView")){
                    holder.setImageResource(R.id.imageView, R.mipmap.maintain);
                }
            }
        };
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AuthorityRole bean = list1.get(position);
                if(bean.getActionName() != null){
                    if(bean.getActionName().equals("LoadWoRtuByWOID")){
                        if (!DoubleClickUtils.isFastDoubleClick()) {
                            // 完善信息
                            intent.setClass(getActivity(), PerfectInformationActivity.class);
                            startActivity(intent);
                        }
                    } else if(bean.getActionName().equals("createInspectionView")){
                        if (!DoubleClickUtils.isFastDoubleClick()) {
                            // 设备巡检
                            intent.setClass(getActivity(), EquipmentInspectionActivity.class);
                            startActivity(intent);
                        }
                    } else if (bean.getActionName().equals("createFixTryView")){
                        if (!DoubleClickUtils.isFastDoubleClick()) {
                            // 安装信息
                            intent.setClass(getActivity(), InstallInformationActivity.class);
                            startActivity(intent);
                        }
                    } else if (bean.getActionName().equals("createRepairInformationView")){
                        if (!DoubleClickUtils.isFastDoubleClick()) {
                            // 设备维修
                            intent.setClass(getActivity(), EquipmentMaintenanceActivity.class);
                            startActivity(intent);
                        }
                    }
                } else {
                    return;
                }
            }
        });
        getData();
    }

    private void getData(){
        list1.clear();
        // 将role里面的数据全部提出来,然后全部存储到sp里面去
        Type listType = new TypeToken<List<AuthorityRole>>() {}.getType();
        Gson gson1 = new Gson();
        List<AuthorityRole> list2 = gson1.fromJson(authorityRole, listType);
        // 这样写可以解决问题，后来查阅资料发现了迭代器Iterator的存在，可以更好的解决此问题。就是移除
        // 一个元素之后，就要将i减去1，因为下次循环的时候，i就成i+1了，但是后面的元素占据了移除元素的位置，
        // 这样的结果是移除元素与后来的元素的下标是一样的，那就得用同样的i去判断，既然下次循环i要加1，
        // 那就得在移除元素之后先让i减去1。而且i < list2.size()是正确的，如果不理解，就去用笔头画图
        if (list2 == null || list2.isEmpty()) return;
        for(int i = 0; i < list2.size(); i++){
            // 要是判断getActionName的话还得进行非null的判断，设备报修有getActionName的值，
            // 剩下两个为null，如果前一个和后一个判断getName，中间那个判断getActionName的话就有==null的风险
            if(list2.get(i).getName().equals("待办任务")){
                list2.remove(list2.get(i));
                if(i > 0) i--;
            } else if (list2.get(i).getName().equals("设备报修")){
                list2.remove(list2.get(i));
                if(i > 0) i--;
            } else if (list2.get(i).getName().equals("远程控制")){
                list2.remove(list2.get(i));
                if(i > 0) i--;
            }
        }
        list1.addAll(list2);
        mAdapter.notifyDataSetChanged();
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
                if (deviceidString.length() < 2 /*|| SpinWindowAreaUntil.mfistCustemInfors.isEmpty()
                        || SpinWindowAreaUntil.mfistCustemInfors.size() < 1*/) {
                    toast("请在首页点击左上角，选择相应设备");
                } else if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), MaintenanceManagementActivity.class);
                    startActivity(intent);
                }
                break;
            // 完善信息 就是第二个
            /*case R.id.perfect_information:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), PerfectInformationActivity.class);
                    startActivity(intent);
                }
                break;*/
            // 设备巡检 就是第三个
            /*case R.id.equipment_inspection:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), EquipmentInspectionActivity.class);
                    startActivity(intent);
                }
                break;*/
            // 安装信息 就是第四个
            /*case R.id.install_information:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), InstallInformationActivity.class);
                    startActivity(intent);
                }
                break;*/
            // 设备维修 就是第五个
            /*case R.id.equipment_repair:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), EquipmentMaintenanceActivity.class);
                    startActivity(intent);
                }
                break;*/
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
