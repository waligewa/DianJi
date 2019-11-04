package com.example.motor.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.common.CommonAdapter;
import com.example.motor.adapter.common.ViewHolder;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.db.EquipInsBean2;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.Loadding;
import com.example.motor.util.NetWorkUtil;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Author : yanftch
 * Date   : 2018/4/19
 * Time   : 18:30
 * Desc   : 设备维修列表
 */

public class EquipmentMaintenanceActivity extends BaseActivity {

    private static final String TAG = "dah_EquipInsActivity";
    private ListView mListView;
    private List<EquipInsBean2> datas = new ArrayList<>();
    private List<EquipInsBean2> datas2 = new ArrayList<>();
    private CommonAdapter<EquipInsBean2> mAdapter;
    private SharedPreferences prefs1;
    private Loadding loadding;
    private String address, guid, userId;
    private int number = 0;
    private Intent intent;
    private EditText etInput;

    @Override
    public int setLayout() {
        return R.layout.activity_equip_ins;
    }

    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("设备维修列表");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        // 创建SQLite数据库
        Connector.getDatabase();
        prefs1 = getSharedPreferences("UserInfo", 0); //  创建一个偏好文件
        intent = new Intent();
        loadding = new Loadding(this);
        address = prefs1.getString("add", "");
        guid = prefs1.getString("guid", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        etInput = (EditText) findViewById(R.id.et_input);
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setDivider(new ColorDrawable(Color.rgb(245, 245, 245)));
        mListView.setDividerHeight(20);
        mAdapter = new CommonAdapter<EquipInsBean2>(this, datas, R.layout.layout_item_equip_repair) {
            @Override
            public void convert(ViewHolder holder, EquipInsBean2 item) {
                holder.setText(R.id.tvNumber, item.getNumber() + "");
                holder.setText(R.id.tvName, "名称：" + item.getWOTitle());
                holder.setText(R.id.tvEquipCode, "时间：" + item.getWOIssuedDate().split("T")[0] + " " +
                        item.getWOIssuedDate().split("T")[1]);
                holder.setText(R.id.tvTitle, "设备编号：" + item.getWOID());
                holder.setText(R.id.tvTime, "姓名：" + item.getUserName());
                holder.setText(R.id.tvEquipTask, "工作类型：" + "电话维修");
                holder.setText(R.id.tvDesc, "故障现象：" + item.getWOContent());
            }
        };
        // 如果无网络就走SQLite
        if(!NetWorkUtil.isNetworkConnected(this)){
            datas.clear();
            List<EquipInsBean2> epList = LitePal.findAll(EquipInsBean2.class);
            datas.addAll(epList);
            mAdapter.notifyDataSetChanged();
        } else {
            // 执行中
            http_getList();
        }
        mListView.setAdapter(mAdapter);
        // 列表里面的bean的getWOID传给DeviceRepairActivity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EquipInsBean2 bean = datas.get(position);
                intent.setClass(EquipmentMaintenanceActivity.this, DeviceRepairActivity.class);
                intent.putExtra("ID", bean.getWOID());
                intent.putExtra("equipinsbean2", bean);
                startActivity(intent);
                finish();
            }
        });
        Drawable drawable = getResources().getDrawable(R.drawable.search);
//      第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        drawable.setBounds(0,0,70,70);
        etInput.setCompoundDrawables(drawable,null,null,null); //只放左边
    }

    @Override
    public void widgetClick(View v) {}

    //返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    private void http_getList() {
        loadding.show("正在加载中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("guid", guid);
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-04-05");
        params.addBodyParameter("endDate", "2100-08-08");
        params.addBodyParameter("pageIndex", "1");
        x.http().post(params, new org.xutils.common.Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    if (result != null) {
                        CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(result);
                        if (commonResponseBean.getCode() == 1) {
                            // 清空数据的操作
                            LitePal.deleteAll(EquipInsBean2.class);
                            Type listType = new TypeToken<List<EquipInsBean2>>() {}.getType();
                            Gson gson = new Gson();
                            datas2 = gson.fromJson(commonResponseBean.getData(), listType);
                            for(int i = 0; i < datas2.size(); i++){
                                if(datas2.get(i).getWOType().equals("1") &&
                                        datas2.get(i).getWOState().equals("false") &&
                                        !datas2.get(i).getWOIssuedUser().equals(userId)){
                                    // 每次进入这个判断就number赋值为number++，然后将其放在EquipInsBean
                                    // 里面，每一个合格的子项都赋一个值
                                    number++;
                                    datas2.get(i).setNumber(number);
                                    datas.add(datas2.get(i));
                                }
                            }
                            // 将datas里面的数据存储到SQLite里面去
                            for(int i = 0; i < datas.size();i++){
                                EquipInsBean2 bean = datas.get(i);
                                EquipInsBean2 t = new EquipInsBean2();
                                t.setWOID(bean.getWOID());
                                t.setWOTitle(bean.getWOTitle());
                                t.setWOContent(bean.getWOContent());
                                t.setVoice(bean.getVoice());
                                t.setWOState(bean.getWOState());
                                t.setWOIssuedDate(bean.getWOIssuedDate());
                                t.setWOIssuedUser(bean.getWOIssuedUser());
                                t.setWOReceiveDate(bean.getWOReceiveDate());
                                t.setWOReceiveUser(bean.getWOReceiveUser());
                                t.setWOItemsNum(bean.getWOItemsNum());
                                t.setWOPerformNum(bean.getWOPerformNum());
                                t.setWOBeginDate(bean.getWOBeginDate());
                                t.setWOEndDate(bean.getWOEndDate());
                                t.setWOCreateDate(bean.getWOCreateDate());
                                t.setWOType(bean.getWOType());
                                t.setWOExpectedTime(bean.getWOExpectedTime());
                                t.setUserName(bean.getUserName());
                                t.setReceiveUser(bean.getReceiveUser());
                                t.setPFID(bean.getPFID());
                                t.setEquipmentID(bean.getEquipmentID());
                                t.setDeviceName(bean.getDeviceName());
                                t.setIsIssue(bean.getIsIssue());
                                t.setIssueName(bean.getIssueName());
                                t.setNumber(bean.getNumber());
                                t.setEquipmentNo(bean.getEquipmentNo());
                                t.setDevCheckID(bean.getDevCheckID());
                                t.save();
                            }
                            mAdapter.notifyDataSetChanged();
                        } else if(commonResponseBean.getCode() == 0){
                            intent.setClass(EquipmentMaintenanceActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            toast(commonResponseBean.getMessage());
                        } else {
                            toast(commonResponseBean.getMessage());
                        }
                    } else {
                        toast("服务器异常");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                toast(ex.getMessage());
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {}

            @Override
            public void onFinished() {
                if (loadding.isShow()) {
                    loadding.close();
                }
            }
        });
    }

    private void toast(String text){
        Toast.makeText(EquipmentMaintenanceActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
