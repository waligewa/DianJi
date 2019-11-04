package com.example.motor.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.db.InspectionOffineItem;
import com.example.motor.db.InspectionOffineStateItem;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.workorderlist.inspection.InspectionWorkOrderActivity;
import com.example.motor.workorderlist.repair.RepairWorkOrderActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.xutils.view.annotation.Event;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.List;

import static com.example.motor.MyApplication.ioi;
import static com.example.motor.MyApplication.iosi;

public class PendingTaskActivity extends BaseActivity {

    private Intent intent;
    private SharedPreferences prefs1;
    private SharedPreferences.Editor editor;

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_pending_task;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("选择待办任务");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    // 初始化控件，设置监听
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        init();
    }

    // View的click事件
    @Override
    public void widgetClick(View v) { }

    @Event(value = { R.id.maintenance_task, R.id.inspection_mission, R.id.work_report,
            R.id.more_business, R.id.quality_information_tracking }, type = View.OnClickListener.class)
    private void btnClick(View v){
        switch (v.getId()){
            case R.id.maintenance_task:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    // 维修任务
                    intent.setClass(PendingTaskActivity.this, RepairWorkOrderActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.inspection_mission:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    // 巡检任务
                    intent.setClass(PendingTaskActivity.this, InspectionWorkOrderActivity.class);
                    startActivity(intent);
                }
                break;
            /*case R.id.work_report:
                // 工作汇报
                intent.setClass(this, InspectionWorkOrderActivity.class);
                startActivity(intent);
                break;
            case R.id.quality_information_tracking:
                // 质量信息追踪
                intent.setClass(this, QualityInformationTrackActivity.class);
                startActivity(intent);
                break;
            case R.id.more_business:
                // 未开发的内容...
                toast("开发人员正在努力开发中...");
                break;*/
        }
    }

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        setResult(RESULT_OK, intent);// 返回的时候给一个RESULT_OK
        onBackPressed();
    }

    private void init() {
        intent = new Intent();
        prefs1 = getSharedPreferences("UserInfo", 0);
        editor = prefs1.edit();

        // 不管是有网还是无网，下面的方法都应该得到执行，
        // 在这个activity里面进行的是ioi集合和iosi集合的累计添加
        String map = prefs1.getString("map1", ""); // 从sp文件里面取得map1
        String map2 = prefs1.getString("map2", ""); // 从sp文件里面取得map2
        if("".equals(map) || ("".equals(map2))) return;
        Type listType = new TypeToken<List<InspectionOffineItem>>() {}.getType();
        Type listType2 = new TypeToken<List<InspectionOffineStateItem>>() {}.getType();
        Gson gson = new Gson();
        // 将从sp里面取得的map1字符串转为集合persons，将从sp里面取得的map1字符串转为集合persons
        List<InspectionOffineItem> persons = gson.fromJson(map, listType);
        List<InspectionOffineStateItem> persons2 = gson.fromJson(map2, listType2);
        // 因为ioi是static的，所以ioi和iosi是累加的
        ioi.addAll(persons);
        iosi.addAll(persons2);
        // 将ioi转换成json字符串数据，再保存
        String strJson = gson.toJson(ioi);
        editor.putString("inspectionOffineItem", strJson);
        // 将iosi转换成json字符串数据，再保存
        String strJson2 = gson.toJson(iosi);
        editor.putString("inspectionOffineItem2", strJson2);
        editor.apply();
    }

    // 手机返回按钮返回功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                setResult(RESULT_OK, intent);// 返回的时候给一个RESULT_OK
                finish();
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    // Toast提醒的封装
    private void toast(String text) {
        Toast.makeText(PendingTaskActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }
}
