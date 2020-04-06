package com.example.motor.activity;

/**
 *  这个DeviceRepairActivity2是给待办任务那里点击进入设备详情然后点击处理任务用的。
 *
 */

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.provider.MediaStore;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.db.TaskItemBean;
import com.example.motor.nfc.ByteArrayChange;
import com.example.motor.nfc.ToStringHex;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.FileUtils;
import com.example.motor.util.ImageItem;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceRepairActivity2 extends BaseActivity {

    @ViewInject(R.id.tv_device_number)
    EditText tvDeviceNumber;
    @ViewInject(R.id.tv_name)
    TextView mName;
    @ViewInject(R.id.fault_type)
    RadioGroup faultType;
    //@ViewInject(R.id.edt_type)
    //EditText edtType;
    @ViewInject(R.id.edt_phenomenon)
    EditText edtPhenomenon;
    @ViewInject(R.id.edt_analysis)
    EditText edtAnalysis;
    @ViewInject(R.id.edt_record)
    EditText edtRecord;
    @ViewInject(R.id.edt_conclusion)
    EditText edtConclusion;
    @ViewInject(R.id.iv)
    ImageView miv;
    @ViewInject(R.id.tv_check)
    TextView check;
    @ViewInject(R.id.tv_submit)
    TextView submit;
    @ViewInject(R.id.search)
    LinearLayout search;
    private String mImagePath = "", gatewayAddress, guid, faultId = "", equipmentNo = "",
            receiveDate, endDate, currentDate, username;  // 图片名
    private Date date = new Date();
    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
    private Loadding loadding;
    private boolean workType, isCheck = false;
    private JSONObject pics, jsonObject1, jsonObject2;
    private JSONArray jsonArray = new JSONArray();
    private SharedPreferences prefs1;
    private int userId;
    private Intent intent;
    private TaskItemBean taskItemBean;

    // nfc相关
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private String info = "";

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_device_repair2;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("设备维修");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    // 初始化控件，设置监听
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        initView();
        faultType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                workType = (checkedId == R.id.wx_jx);
            }
        });
        faultType.check(R.id.wx_jx);
        // 先把NFC功能去掉，因为在MainActivity里面已经对NFC进行了处理
        //initNFC();
    }

    // View的click事件
    @Override
    public void widgetClick(View v) {}

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    private void initView() {
        intent = new Intent();
        prefs1 = getSharedPreferences("UserInfo", 0);
        guid = prefs1.getString("guid", "");
        gatewayAddress = prefs1.getString("add", "");
        userId = prefs1.getInt("UserID", 0);
        username = prefs1.getString("userfullname", "");
        faultId = getIntent().getStringExtra("ID"); // 故障id
        taskItemBean = (TaskItemBean) getIntent().getSerializableExtra("taskitembean1");
        jsonObject1 = new JSONObject();
        jsonObject2 = new JSONObject();
        // 这个地方先让提交隐藏，等签名结束之后再将提交显示出来
        submit.setVisibility(View.GONE);
        submit.invalidate();   // 使无效
        tvDeviceNumber.setText(getIntent().getStringExtra("EquipmentNo")); // 设备编号赋值
        loadding = new Loadding(this);
        receiveDate = format1.format(date); // 开始时间
        currentDate = format2.format(date);// 当前时间（年月日）
        mName.setText(username);
    }

    @Event(value = {R.id.iv_camera, R.id.tv_check, R.id.tv_submit, R.id.scan_scan, R.id.search},
            type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            // 拍照的代码
            case R.id.iv_camera:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // capture 获取
                    startActivityForResult(openCameraIntent, 0);
                }
                break;
            // 验证签名
            case R.id.tv_check:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if (!tvDeviceNumber.getText().toString().equals("")) {
                        Intent intent = new Intent(this, QMActivity.class);
                        intent.putExtra("EquipmentNo", tvDeviceNumber.getText().toString()); //  设备编号传值
                        startActivityForResult(intent, 1);
                    } else {
                        tvDeviceNumber.setText("");
                        toast("请输入设备编号");
                    }
                }
                break;
            // 提交按钮
            case R.id.tv_submit:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    // 跳转一个对话框
                    jumpDialog();
                }
                break;
            // 扫一扫
            case R.id.scan_scan:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    startActivityForResult(new Intent(DeviceRepairActivity2.this,
                                    CaptureActivity.class),
                            MainActivity.SCAN_QR_CODE);
                }
                break;
            // 搜索按钮
            case R.id.search:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    intent.setClass(DeviceRepairActivity2.this,
                            PerfectInformationActivity2.class);
                    intent.putExtra("EquipmentNo", tvDeviceNumber.getText().toString().trim());
                    startActivity(intent);
                }
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    try {
                        //String fileName = String.valueOf(System.currentTimeMillis());
                        String fileName = new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date());
                        Bitmap bm = (Bitmap) data.getExtras().get("data");
                        FileUtils.saveBitmap(bm, fileName);   // 保存压缩图片
                        Log.e("bm", data.getExtras().get("data") + "");
                        ImageItem takePhoto = new ImageItem();
                        takePhoto.setBitmap(bm);
                        FileUtils.createSDDir(fileName + ".JPEG");  //   创建新目录
                        takePhoto.setImagePath(FileUtils.SDPATH + fileName + ".JPEG");
                        Log.e("===现场拍照===", FileUtils.SDPATH + fileName + ".JPEG");
                        File file = new File(takePhoto.getImagePath());
                        Bitmap bitmap = FileUtils.resizeImage(BitmapFactory.decodeFile(takePhoto
                                .getImagePath()), 600);  //    decode解码  resize调整大小
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        String uploadBuffer = new String(Base64.encode(baos.toByteArray(),
                                Base64.DEFAULT));   //   进行Base64编码   encode编码
                        upPicData(file.getName(), uploadBuffer);
                        Log.e("===现场拍照0===", uploadBuffer);
                        miv.setImageBitmap(takePhoto.getBitmap());
                        mImagePath = fileName;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    if (!(data == null)) {
                        isCheck = true;
                        try {
                            pics = new JSONObject(data.getStringExtra("pics"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        submit.setVisibility(View.VISIBLE);
                        submit.invalidate();   //  使无效
                        check.setVisibility(View.GONE);
                        check.invalidate();    //  使无效
                    }
                } else {
                    toast("签名存档失败\n请重新签名");
                }
                break;
        }
    }

    // 跳转一个对话框
    private void jumpDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("您确定提交吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("是的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    // 开始提交数据
                    submit();
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        dialog.show();
    }

    // nfc识别相关
    private void initNFC() {
        // 获取默认的NFC适配器
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            toast("设备不支持NFC！");
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            toast("请在系统设置中先启用NFC功能！");
            return;
        }
        // 一旦截获NFC消息，就会通过PendingIntent调用窗口
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this,
                        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // Setup an intent filter for all MIME based dispatches
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[]{ndef,};
        // 读标签之前先确定标签类型。这里以大多数的NfcA为例
        mTechLists = new String[][]{new String[]{NfcA.class.getName()}};
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO 自动生成的方法存根
        super.onNewIntent(intent);
        String intentActionStr = intent.getAction();// 获取到本次启动的action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentActionStr)// NDEF类型
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentActionStr)// 其他类型
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intentActionStr)) {// 未知类型
            // 在intent中读取Tag id
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG); // parcelable打包的
            byte[] bytesId = tag.getId();// 获取id数组
            info = ByteArrayChange.ByteArrayToHexString(bytesId) + "\n"; // hex十六进制
            String equNo = "";
            equNo = change(tag);
            // 转换为ASCll
            //toast("识别成功设备编号:" + equNo);
            // 如果设备编号为null的话就直接return，如果接着往下走就要闪退了
            if(equNo == null) return;
            if (equNo.length() > 0) {

                //TODO  拿到IC卡里携带的设备编号之后要执行的操作
                //toast("识别结果:" + equNo);
                tvDeviceNumber.setText(equNo);
            } else {
                toast("识别失败请重新扫描卡片！");
            }
        }
    }

    // 将ncf识别出的Hex转换为字符串,一般ic卡里存储的是设备编号
    public String change(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        Log.e("进入了change方法", "是的，进入了");
        boolean auth = false;
        // 读取TAG
        String ChangeInfo = "";
        String Ascll = "";
        // Enable I/O operations to the tag from this TagTechnology object.
        try {
            mfc.connect();
            // authenticate认证 sector扇形
            auth = mfc.authenticateSectorWithKeyA(1, MifareClassic.KEY_DEFAULT); // 非常重要---------------------------
            if (auth) {
                Log.e("change的auth验证成功", "开始读取模块信息");
                byte[] data = mfc.readBlock(4 * 1 + 1);//--------------
                ChangeInfo = ByteArrayChange.ByteArrayToHexString(data);
                //String temp = ToStringHex.decode(ChangeInfo);
                Ascll = ToStringHex.decode(ChangeInfo);
                return Ascll;
            }
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } finally {
            try {
                if(mfc == null) return "彬彬";
                mfc.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    private void submit() {
        if (tvDeviceNumber.getText().toString().trim().equals("")) {
            toast("请输入设备编号");
        } else if (edtPhenomenon.getText().toString().trim().equals("")) {
            toast("请输入故障现象");
        } else if (edtAnalysis.getText().toString().trim().equals("")) {
            toast("请输入故障分析");
        } else if (edtRecord.getText().toString().trim().equals("")) {
            toast("请输入修复方法");
        } else if (edtConclusion.getText().toString().trim().equals("")) {
            toast("请输入修复结果");
        } else if (mImagePath.equals("")) {
            toast("请拍摄现场照片");
        } else if (!isCheck) {
            toast("请签名存档");
        } else {
            upData();// 上传数据，这个成功之后会退出当前的活动，显示（添加维修信息成功）
            getData();// 上传更新故障结果
            // 下面的是updateWorkOrder()
            if(TextUtils.isEmpty(faultId)){
                toast("故障id为空不能提报");
            } else {
                try{
                    Date date2 = new Date();
                    endDate = format1.format(date2);  // 结束时间（年月日时分秒）
                    // workorder 字段    这个不用jsonArray
                    jsonObject1.put("WOID", taskItemBean.getWOID());
                    jsonObject1.put("WOTitle", taskItemBean.getWOTitle());
                    jsonObject1.put("WOContent", taskItemBean.getWOContent());
                    jsonObject1.put("Voice", "无语音");
                    jsonObject1.put("WOState", 1);  // 代表已处理完事
                    jsonObject1.put("WOIssuedDate", endDate);  // 派工时间
                    jsonObject1.put("WOIssuedUser", taskItemBean.getWOIssuedUser());  // 派工人
                    // 这个字段是activity进入的时候就获取的年月日时分秒
                    jsonObject1.put("WOReceiveDate", receiveDate);
                    // 这是接收人字段
                    jsonObject1.put("WOReceiveUser", taskItemBean.getWOReceiveUser());
                    jsonObject1.put("WOItemsNum", "1");
                    jsonObject1.put("WOPerformNum", "1");
                    jsonObject1.put("WOBeginDate", taskItemBean.getWOBeginDate());
                    // 这个字段是点击发送按钮获取的当前的年月日时分秒
                    jsonObject1.put("WOEndDate", endDate);
                    jsonObject1.put("WOCreateDate", taskItemBean.getWOCreateDate());
                    jsonObject1.put("WOType", "1");
                    jsonObject1.put("WOExpectedTime", taskItemBean.getWOExpectedTime());
                    jsonObject1.put("FBID", faultId);
                    jsonObject1.put("WOFeedback", "");
                    // detail  字段      这个用jsonArray
                    jsonObject2.put("WOID", taskItemBean.getWOID());
                    jsonObject2.put("Num", "1");
                    jsonObject2.put("OrderContent", "无语音");
                    jsonObject2.put("OrderState", "1");  // 代表已处理完事
                    jsonObject2.put("UserID", userId);
                    jsonObject2.put("DateTime", currentDate);
                    jsonArray.put(jsonObject2);
                    updateWorkOrder();  // 成功的话会显示添加成功
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void upPicData(String picName, String uploadBuffer) {

        loadding.show("上传数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/Base64ToImg");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/Base64ToImg");
        }
        String pic[] = picName.split("\\.");
        try {
            params.addBodyParameter("base64Str", URLDecoder.decode(uploadBuffer.toString(), "UTF-8")
                    .replace(" ", "+"));//图片
            params.addBodyParameter("fileName", pic[0]);  //  图片名称
            params.addBodyParameter("equNo", tvDeviceNumber.getText().toString());  //  产品no
            params.addBodyParameter("userId", String.valueOf(userId));  //  产品no
            params.addBodyParameter("guid", guid);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
            }

            @Override
            public void onFinished() {
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onSuccess(String arg0) {
                Log.e("json", arg0);
                CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                if (commonResponseBean.getCode() == 1) {
                    toast(commonResponseBean.getMessage());
                } else if (commonResponseBean.getCode() == 0) {
                    Intent intent = new Intent(DeviceRepairActivity2.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    toast(commonResponseBean.getMessage() );
                } else {
                    toast(commonResponseBean.getMessage());
                }
                if (loadding.isShow()) {
                    loadding.close();
                }
            }
        });
    }

    private void upData() {

        loadding.show("上传数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/AddDeviceRepair");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/AddDeviceRepair");
        }
        try {
            params.addBodyParameter("guid", guid);
            params.addBodyParameter("EquipmentNo", tvDeviceNumber.getText().toString().trim());// 产品no
            params.addBodyParameter("Worker", username);// 维保人员
            params.addBodyParameter("WorkType", String.valueOf(workType));// 故障解决方式true是现场维修
            params.addBodyParameter("Symptom", edtPhenomenon.getText().toString().trim());// 故障现象
            params.addBodyParameter("FaultAnalysis", edtAnalysis.getText().toString().trim());// 故障分析
            params.addBodyParameter("RepairMethod", edtRecord.getText().toString().trim());// 修复方法  维修记录
            params.addBodyParameter("Message", String.valueOf(workType));// 故障类型
            params.addBodyParameter("RepairResult", edtConclusion.getText().toString().trim());// 修复结果 维修结论
            params.addBodyParameter("LocalImage", mImagePath);// 现场图片
            params.addBodyParameter("WDevImage", pics.getString("jqhz"));// 机器合照
            params.addBodyParameter("WCusImage", pics.getString("khhz"));// 客户合照
            params.addBodyParameter("CustomerSign", pics.getString("khqm"));// 客户签名
            params.addBodyParameter("WorkerSign", pics.getString("wbqm"));// 维保签名
            //params.addBodyParameter("userId", String.valueOf(userId));// 维保签名
            params.addBodyParameter("WOID", taskItemBean.getWOID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
            }

            @Override
            public void onFinished() {
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onSuccess(String arg0) {
                Log.e("json ", arg0);
                CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                if (commonResponseBean.getCode() == 1) {
                    // 成功的话就是添加维修信息成功
                    toast(commonResponseBean.getMessage());
                    setResult(RESULT_OK, intent); // 返回的时候给一个RESULT_OK
                    finish();
                } else if (commonResponseBean.getCode() == 0) {
                    Intent intent = new Intent(DeviceRepairActivity2.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                } else {
                    toast(commonResponseBean.getMessage());
                }
                if (loadding.isShow()) {
                    loadding.close();
                }
            }
        });
    }

    // 这是用于上传更新故障结果
    private void getData() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/UpdateFB");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/UpdateFB");
        }
        params.addBodyParameter("id", faultId);
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
                toast("内部服务异常");
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                if (commonResponseBean.getCode() == 0) {
                    Intent intent = new Intent(DeviceRepairActivity2.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                // 成功的话就是更新成功
                toast(commonResponseBean.getMessage());
            }
        });
    }

    //  发送工作任务的3个接口之一  UpdateWorkOrder接口
    private void updateWorkOrder() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/UpdateWorkOrder");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/UpdateWorkOrder");
        }
        params.addBodyParameter("workorder", jsonObject1.toString());
        params.addBodyParameter("detail", jsonArray.toString());
        params.addBodyParameter("guid", guid);
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(Callback.CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
                toast("服务器异常");
            }

            @Override
            public void onFinished() {
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onSuccess(String arg0) {
                try{
                    Intent intent = new Intent();
                    CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                    if (commonResponseBean.getCode() == 1) {
                        toast(commonResponseBean.getMessage());
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(DeviceRepairActivity2.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        toast(commonResponseBean.getMessage());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void toast(String text){
        Toast.makeText(DeviceRepairActivity2.this, text, Toast.LENGTH_SHORT).show();
    }

    // 手机返回按钮返回功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 设置处理优于所有其他NFC的处理
        if (nfcAdapter != null && nfcAdapter.isEnabled())
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null,
                    null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 恢复默认状态
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
