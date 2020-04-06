package com.example.motor.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.VideoAdapter;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.example.motor.videoSurveillance.MonitorInfo;
import com.example.motor.videoSurveillance.MonitorInfo2;
import com.example.motor.videoSurveillance.TestNetSDKActivity;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * 这是视频监控的那个初始进入的ListView列表
 *
 */
public class VideoSurveillanceActivity extends AppCompatActivity {

    @ViewInject(R.id.back)
    LinearLayout back;
    @ViewInject(R.id.add)
    ImageView addIpAddress;
    @ViewInject(R.id.lv_monitor)
    ListView listView;
    private Intent intent;
    public static List<MonitorInfo2> data1 = new ArrayList<>();
    private VideoAdapter videoAdapter;
    private AlertDialog alertDialog;
    private MonitorInfo monitorInfo = new MonitorInfo();
    private String address;
    private SharedPreferences prefs1;
    private Loadding loadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_surveillance);
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        intent = new Intent();
        prefs1 = getSharedPreferences("UserInfo", 0);
        loadding = new Loadding(this);
        address = prefs1.getString("add", "");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addIpAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // 这个if语句的作用是禁止连续点击
                if(!DoubleClickUtils.isFastDoubleClick()){
                    loadding.show("正在加载中...");
                    MonitorInfo2 monitorInfo2 = data1.get(position);
                    intent.setClass(VideoSurveillanceActivity.this, TestNetSDKActivity.class);
                    intent.putExtra("monitorInfo", monitorInfo2);
                    //intent.putExtra("position", String.valueOf(position));
                    startActivity(intent);
                    loadding.close();
                }
            }
        });
        videoAdapter = new VideoAdapter(this, data1);
        listView.setAdapter(videoAdapter);
        getData();
    }

    // 这个是点击添加按钮之后的方法
    private void showDialog() {
        View ipAddressView = LayoutInflater.from(VideoSurveillanceActivity.this)
                .inflate(R.layout.check_admin_layout2, null);
        final EditText ipAddress = (EditText) ipAddressView.findViewById(R.id.ip_address);
        ipAddress.setText("");
        Button determine = (Button) ipAddressView.findViewById(R.id.determine);
        Button cancel = (Button) ipAddressView.findViewById(R.id.cancel);
        determine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ipAddress.getText().toString().trim().equals("")) {
                    ipAddress.setText("");
                    toast("请输入添加设备的IP地址");
                } else {
                    //data.add(ipAddress.getText().toString().trim());
                    MonitorInfo2 monitorInfo2 = new MonitorInfo2();
                    monitorInfo2.setIP(ipAddress.getText().toString().trim());
                    monitorInfo2.setPort("");
                    monitorInfo2.setEquName("");
                    monitorInfo2.setEquPwd("");
                    monitorInfo2.setIdentification("2");
                    data1.add(monitorInfo2);
                    videoAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        });
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);
        alertDialog.setView(new EditText(this)); //  不加这句代码EditText无法填入东西
        alertDialog.show();
        alertDialog.getWindow().setContentView(ipAddressView); //  这句代码的意思是将布局填入AlertDialog
    }

    // 这个是activity刚开始就进来的时候获取数据的方法
    private void getData() {
        for(int i = 0; i < data1.size(); i++){
            if(data1.get(i).getIdentification() != null){
                if(data1.get(i).getIdentification().equals("1")){
                    data1.remove(data1.get(i));
                    if(i > 0) i--;
                }
            }
        }
        for(int i = 0; i < data1.size() - 1; i++){
            for(int j = data1.size() - 1; j > i; j--)  {
                if (data1.get(j).getIP().equals(data1.get(i).getIP())){
                    data1.remove(j);
                }
            }
        }
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/getCameraSetting");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/getCameraSetting");
        }
        String guidString = getSharedPreferences("UserInfo", 0).getString("guid", "");
        String EquID = getSharedPreferences("device", 0).getString("EquipmentID", "");
        if (!(EquID.isEmpty() && EquID.equals(""))) {
            params.addBodyParameter("EquID", EquID);
            params.addBodyParameter("guid", guidString);
            x.http().get(params, new Callback.CommonCallback<String>() {

                @Override
                public void onCancelled(CancelledException arg0) {}

                @Override
                public void onError(Throwable ex, boolean arg1) {
                    toast(ex.getMessage());
                }

                @Override
                public void onFinished() {}

                @Override
                public void onSuccess(String arg0) {
                    try {
                        JSONObject object1 = new JSONObject(arg0);
                        if (object1.getString("Code").equals("0")) {
                            intent.setClass(VideoSurveillanceActivity.this,
                                    LoginActivity.class);
                            startActivity(intent);
                            finish();
                            toast(object1.getString("Message"));
                        } else if (object1.getString("Code").equals("1")) {
                            // 通过下面的两行代码就生成了MonitorInfo对象，这个对象就是很多字段的那个，
                            // 是具有3条ip数据的那个对象
                            Gson gson = new Gson();
                            monitorInfo = gson.fromJson(object1.getString("Data"), MonitorInfo.class);
                            if (!monitorInfo.getEquName().isEmpty() && !monitorInfo.getEquName().equals("")) {
                                // 下面的这个对象是用于在ListView中显示用的实体对象
                                MonitorInfo2 monitorInfo2 = new MonitorInfo2();
                                monitorInfo2.setIP(monitorInfo.getIP());
                                monitorInfo2.setPort(monitorInfo.getPort());
                                monitorInfo2.setEquName(monitorInfo.getEquName());
                                monitorInfo2.setEquPwd(monitorInfo.getEquPwd());
                                monitorInfo2.setIdentification("1");
                                data1.add(monitorInfo2);
                            } else if (!monitorInfo.getEquName2().isEmpty() && !monitorInfo.getEquName2().equals("")) {
                                MonitorInfo2 monitorInfo2 = new MonitorInfo2();
                                monitorInfo2.setIP(monitorInfo.getIP2());
                                monitorInfo2.setPort(monitorInfo.getPort2());
                                monitorInfo2.setEquName(monitorInfo.getEquName2());
                                monitorInfo2.setEquPwd(monitorInfo.getEquPwd2());
                                monitorInfo2.setIdentification("1");
                                data1.add(monitorInfo2);
                            } else if (!monitorInfo.getEquName3().isEmpty() && !monitorInfo.getEquName3().equals("")) {
                                MonitorInfo2 monitorInfo2 = new MonitorInfo2();
                                monitorInfo2.setIP(monitorInfo.getIP3());
                                monitorInfo2.setPort(monitorInfo.getPort3());
                                monitorInfo2.setEquName(monitorInfo.getEquName3());
                                monitorInfo2.setEquPwd(monitorInfo.getEquPwd3());
                                monitorInfo2.setIdentification("1");
                                data1.add(monitorInfo2);
                            } else {
                                toast("该设备没有对应的监控设备!");
                            }
                            videoAdapter.notifyDataSetChanged();
                        }
                        toast(object1.getString("Message"));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        } else {
            toast("请点击首页左上角按钮，选择相应设备");
        }
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoAdapter.notifyDataSetChanged();
    }
}
