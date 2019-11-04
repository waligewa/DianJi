package com.example.motor.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.common.CommonAdapter;
import com.example.motor.adapter.common.ViewHolder;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.db.EquipInsBean;
import com.example.motor.db.InspectionOffineItem;
import com.example.motor.db.InspectionOffineStateItem;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.motor.MyApplication.ioi;
import static com.example.motor.MyApplication.iosi;

/**
 *
 * Author : 赵彬彬
 * Date   : 2019/6/18
 * Time   : 17:16
 * Desc   : 这是服务界面的设备巡检
 */
public class EquipmentInspectionActivity extends BaseActivity {

    private static final String TAG = "dah_EquipInsActivity";
    private ListView mListView;
    private EditText etInput;
    private TextView search;
    private List<EquipInsBean> datas = new ArrayList<>();
    private List<EquipInsBean> datas2 = new ArrayList<>();

    private CommonAdapter<EquipInsBean> mAdapter;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Loadding loadding;
    private String address, guid, userId;
    private int listNumber = 0; // 定位list的位置
    private Intent intent;

    @Override
    public int setLayout() {
        return R.layout.activity_equip_ins;
    }

    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("设备巡检列表");
        mBaseTitleBarView.setLeftDrawable(-1);
        //mBaseTitleBarView.setRightDrawable(R.mipmap.add);
    }

    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        // 创建SQLite数据库
        Connector.getDatabase();
        prefs = getSharedPreferences("UserInfo", 0); //  创建一个偏好文件
        editor = prefs.edit();
        intent = new Intent();
        loadding = new Loadding(this);
        address = prefs.getString("add", "");
        guid = prefs.getString("guid", "");
        userId = String.valueOf(prefs.getInt("UserID", 0));
        mListView = (ListView) findViewById(R.id.listView);
        etInput = (EditText) findViewById(R.id.et_input);
        search = (TextView) findViewById(R.id.search);
        etInput.setText("");
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    // 隐藏输入法键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                    mListView.setSelection(0);
                    // 通过设备编号或者设备名称来获取数据
                    //getDataFromNumber();
                }
            }
        });
        mListView.setDivider(new ColorDrawable(Color.rgb(245, 245, 245)));
        mListView.setDividerHeight(20);
        mAdapter = new CommonAdapter<EquipInsBean>(this, datas,
                R.layout.layout_item_equip_ins) {
            @Override
            public void convert(ViewHolder holder, EquipInsBean item) {
                holder.setText(R.id.tvNumber, item.getNumber() + "");
                holder.setText(R.id.tvName, "姓名：         " + item.getWOTitle());
                holder.setText(R.id.tvEquipCode, "设备编号：" + item.getEquipmentNo());
                holder.setText(R.id.tvTitle, item.getDeviceName());
                holder.setText(R.id.tvTIme, "时间：         " + (TextUtils.isEmpty(item.getWOIssuedDate()) ? "" :
                        (item.getWOIssuedDate().split("T")[0] + " " + item.getWOIssuedDate().split("T")[1].substring(0, 8))));
                holder.setText(R.id.tvEquipTask, "巡检任务：" + item.getWOContent());
            }
        };
        // 如果无网络就走SQLite
        if(!NetWorkUtil.isNetworkConnected(this)){
            datas.clear();
            List<EquipInsBean> epList = LitePal.findAll(EquipInsBean.class);
            datas.addAll(epList);
            mAdapter.notifyDataSetChanged();
        } else {
            // 执行中
            http_getList();
        }
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 把实体类子项传递过去DeviceInspectionActivity
                intent.setClass(EquipmentInspectionActivity.this, DeviceInspectionActivity.class);
                intent.putExtra("taskitembean1", datas.get(position));
                listNumber = position;
                startActivityForResult(intent, 0);
            }
        });
        init();
    }

    @Override
    public void widgetClick(View v) {}

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    @Override
    public void onTitleRightImagePressed(){
        if(!DoubleClickUtils.isFastDoubleClick()){
            intent.setClass(this, DeviceInspectionActivity3.class);
            startActivity(intent);
            finish();
        }
    }

    private void init(){

        // 不管是有网还是无网，下面的方法都应该得到执行，
        // 在这个activity里面进行的是ioi集合和iosi集合的累计添加
        String map = prefs.getString("map1", ""); // 从sp文件里面取得map1
        String map2 = prefs.getString("map2", ""); // 从sp文件里面取得map2
        Drawable drawable = getResources().getDrawable(R.drawable.search);
//      第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
        drawable.setBounds(0,0,70,70);
        etInput.setCompoundDrawables(drawable,null,null,null); //只放左边
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){// 请求码
            case 0:
                if(resultCode == RESULT_OK){// 结果码
                    // 让mListView到达最上面
                    mListView.setSelection(listNumber);
                    // 刷新一下
                    if("".equals(etInput.getText().toString())){
                        http_getList();
                    } else {
                        // 更加设备编号或者设备名称来获取数据
                        //getDataFromNumber();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void http_getList() {
        Date te = new Date();
        SimpleDateFormat mat = new SimpleDateFormat("yyyy");
        String str = mat.format(te);
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "10000");
        params.addBodyParameter("beginDate", str + "-01-01");
        params.addBodyParameter("endDate", str + "-12-31");
        params.addBodyParameter("guid", "");
        params.addBodyParameter("pageIndex", "1");
        x.http().get(params, new org.xutils.common.Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    if (result != null) {
                        CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(result);
                        if (commonResponseBean.getCode() == 1) {
                            datas.clear();
                            LitePal.deleteAll(EquipInsBean.class);
                            Type listType = new TypeToken<List<EquipInsBean>>() {}.getType();
                            Gson gson = new Gson();
                            datas2 = gson.fromJson(commonResponseBean.getData(), listType);
                            int number = 0;
                            for(int i = 0; i < datas2.size(); i++){
                                // 此处判断有个bug，就是当userId是int的时候（!datas2.get(i)
                                // .getWOIssuedUser().equals(userId)）这句代码是进行int和String的判断，
                                // 然后一点想要的作用达不到，必须要进行String和String的判断才行。
                                if(datas2.get(i).getWOType().equals("2") &&
                                        datas2.get(i).getWOState().equals("false") &&
                                        datas2.get(i).getIsIssue().equals("false") &&
                                        !datas2.get(i).getWOIssuedUser().equals(userId) &&
                                        TextUtils.isEmpty(datas2.get(i).getDevCheckID())){
                                    // 每次进入这个判断就number赋值为number++，然后将其放在EquipInsBean
                                    // 里面，每一个合格的子项都赋一个值
                                    number++;
                                    datas2.get(i).setNumber(number);
                                    datas.add(datas2.get(i));
                                }
                            }
                            // 将datas里面的数据存储到SQLite里面去
                            for(int i = 0; i < datas.size();i++){
                                EquipInsBean bean = datas.get(i);
                                EquipInsBean t = new EquipInsBean();
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
                            mBaseTitleBarView.setTitleText("设备巡检列表" + " (" + datas.size() + ") " + "条");
                            mAdapter.notifyDataSetChanged();
                        } else if(commonResponseBean.getCode() == 0){
                            intent.setClass(EquipmentInspectionActivity.this, LoginActivity.class);
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
            public void onCancelled(CancelledException cex) { }

            @Override
            public void onFinished() { }
        });
    }

    // 根据设备编号或者设备名称来获取数据
    private void getDataFromNumber() {
        loadding.show("正在拼命加载中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/ResearchWorkOrderList");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/ResearchWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2200-12-31");
        params.addBodyParameter("filter", etInput.getText().toString().trim());
        params.addBodyParameter("guid", "");
        params.addBodyParameter("pageIndex", "1");
        x.http().get(params, new org.xutils.common.Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    if (result != null) {
                        CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(result);
                        if (commonResponseBean.getCode() == 1) {
                            datas.clear();
                            Type listType = new TypeToken<List<EquipInsBean>>() {}.getType();
                            Gson gson = new Gson();
                            datas2 = gson.fromJson(commonResponseBean.getData(), listType);
                            int t = 0;
                            for(int i = 0; i < datas2.size(); i++){
                                // 此处判断有个bug，就是当userId是int的时候（!datas2.get(i)
                                // .getWOIssuedUser().equals(userId)）这句代码是进行int和String的判断，
                                // 然后一点想要的作用达不到，必须要进行String和String的判断才行。
                                if(datas2.get(i).getWOType().equals("2") &&
                                        datas2.get(i).getWOState().equals("false") &&
                                        datas2.get(i).getIsIssue().equals("false") &&
                                        !datas2.get(i).getWOIssuedUser().equals(userId) &&
                                        TextUtils.isEmpty(datas2.get(i).getDevCheckID())){
                                    datas2.get(i).setNumber(++t);
                                    datas.add(datas2.get(i));
                                }
                            }
                            mBaseTitleBarView.setTitleText("设备巡检列表" + " (" + datas.size() + ") " + "条");
                            mAdapter.notifyDataSetChanged();
                        } else if(commonResponseBean.getCode() == 0){
                            intent.setClass(EquipmentInspectionActivity.this, LoginActivity.class);
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
            public void onCancelled(CancelledException cex) { }

            @Override
            public void onFinished() { }
        });
    }

    private void toast(String text){
        Toast.makeText(EquipmentInspectionActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }
}
