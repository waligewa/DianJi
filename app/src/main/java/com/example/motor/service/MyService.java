package com.example.motor.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.db.InspectionOffineItem;
import com.example.motor.db.InspectionOffineStateItem;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import static com.example.motor.MyApplication.ioi;
import static com.example.motor.MyApplication.iosi;

/**
 * 这个服务用于有网络的时候提交存储在sp文件中的数据
 *
 */
public class MyService extends Service {

    private SharedPreferences prefs1;
    private SharedPreferences.Editor editor;
    private List<InspectionOffineItem> see = new ArrayList<>();
    private List<InspectionOffineStateItem> see2 = new ArrayList<>();
    private String address;

    public MyService() { }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.e("Service", "onCreate");
        prefs1 = getSharedPreferences("UserInfo", 0);
        editor = prefs1.edit();
        address = prefs1.getString("add", "");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        List<InspectionOffineItem> datalist = new ArrayList<>(); // 巡检离线子项
        List<InspectionOffineStateItem> datalist2 = new ArrayList<>(); // 巡检离线状态子项
        String strJson = prefs1.getString("inspectionOffineItem", null);
        String strJson2 = prefs1.getString("inspectionOffineItem2", null);
        if (null != strJson && (null != strJson2)){
            Gson gson = new Gson();
            // 通过json字符串生成集合
            datalist = gson.fromJson(strJson, new TypeToken<List<InspectionOffineItem>>() {}.getType());
            datalist2 = gson.fromJson(strJson2, new TypeToken<List<InspectionOffineStateItem>>() {}.getType());
            see.addAll(datalist);
            see2.addAll(datalist2);
            for(int i = 0; i < see.size(); i++){
                // 提交数据
                submitData(
                        see.get(i).getGuid(),
                        see.get(i).getData(),
                        see.get(i).getUserId(),
                        see.get(i).getWOID(),
                        see.get(i).getEquipmentID(),
                        see.get(i).getEquipmentNo(),
                        see.get(i).getWorker(),
                        see.get(i).getGISLocation(),
                        see.get(i),
                        see2.get(i),
                        see2.get(i).getDetail(),
                        see2.get(i).getWorkorder());
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // 提交数据，这是设备巡检的
    private void submitData(final String s1, String s2, String s3, String s4, String s5,
                            String s6, String s7, String s8, final InspectionOffineItem item,
                            final InspectionOffineStateItem item2, final String s9, final String s10) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/AddDeviceCheckInfo");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/AddDeviceCheckInfo");
        }
        params.addBodyParameter("guid", s1);
        params.addBodyParameter("data", s2);
        params.addBodyParameter("userId", s3);
        params.addBodyParameter("WOID", s4);
        params.addBodyParameter("EquipmentID", s5);
        params.addBodyParameter("EquipmentNo", s6);
        params.addBodyParameter("Worker", s7);
        params.addBodyParameter("GISLocation", s8);
        params.addBodyParameter("EID", "20");

        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("无网状态下保存的数据上传失败1");
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                // 改变工单状态
                getUpdateWorkOrder(s1, item, item2, s9, s10);
            }
        });
    }

    // 发送工作任务的3个接口之一  UpdateWorkOrder接口  更新工单状态
    private void getUpdateWorkOrder(String s1, final InspectionOffineItem item,
                                    final InspectionOffineStateItem item2, String s2, String s3) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/UpdateWorkOrder");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/UpdateWorkOrder");
        }
        params.addBodyParameter("workorder", s3);
        params.addBodyParameter("detail", s2);
        params.addBodyParameter("guid", s1);
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("无网状态下保存的数据上传失败2");
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                if (commonResponseBean.getCode() == 1) {
                    ioi.remove(item);
                    iosi.remove(item2);
                    Gson gson = new Gson();
                    // 将ioi转换成json字符串数据，再保存
                    String strJson = gson.toJson(ioi);
                    editor.putString("inspectionOffineItem", strJson);
                    // 将ioi转换成json字符串数据，再保存
                    String strJson2 = gson.toJson(iosi);
                    editor.putString("inspectionOffineItem2", strJson2);
                    editor.apply();
                }
            }
        });
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
