package com.example.motor.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.ActionSheetDialog;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.FileUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.MyAlertDialog;
import com.example.motor.util.SPUtils;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 个人资料activity，我的界面点击【个人资料】按钮过来的activity
 *
 */
public class PersonalDataActivity extends AppCompatActivity {

    @ViewInject(R.id.cb_edit)
    CheckBox cbEdit;
    @ViewInject(R.id.circle_imageview)
    CircleImageView circleImageView;
    @ViewInject(R.id.et_username)
    EditText etUsername;
    @ViewInject(R.id.et_phone_number)
    EditText phoneNumber;
    @ViewInject(R.id.wechat_number)
    EditText wechatNumber;
    private Loadding loading;
    private Intent intent;
    private SharedPreferences prefs1;
    private SharedPreferences.Editor editor;
    private Uri imageUri;
    private String imagePath2 = "";
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);
        x.view().inject(this);
        init();
    }

    private void init(){
        intent = new Intent();
        loading = new Loadding(this);
        prefs1 = getSharedPreferences("UserInfo", 0);
        editor = prefs1.edit();
        mActivity = this;
        etUsername.setText(prefs1.getString("etusername", ""));
        imagePath2 = prefs1.getString("imagepath", "");
        // 先出来一个警告框，用以提示用户
        new MyAlertDialog(this)
                .builder()
                .setMsg("请点击“编辑”按钮后完善个人信息")
                .setCancelable(false)
                .setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 如果imagePath2的值为“”的话就让它直接setImageResource，这个R.drawable.splash15
                        // 是int值，如果imagePath2的值包含“content”这个字符串的话就让它进入displayImage
                        // 方法里面进行判断，如果imagePath2的值既不为“”也不包含“content”的话就让它走
                        // displayImage2方法
                        if(!DoubleClickUtils.isFastDoubleClick()){
                            if(imagePath2.equals("")){
                                circleImageView.setImageResource(R.drawable.splash15);
                            } else if (imagePath2.contains("content")){
                                displayImage(prefs1.getString("imagepath", ""));
                            } else {
                                displayImage2(prefs1.getString("imagepath", ""));
                            }
                            // 加载数据的接口
                            loadData();
                        }
                    }
                })
                .show();
        // 这是推送的内容toast显示的一些代码
        /*if (null != intent) {
            Bundle bundle = getIntent().getExtras();
            String title = null;
            String content = null;
            if(bundle != null){
                title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
                content = bundle.getString(JPushInterface.EXTRA_ALERT);
            }
            if(title != null && content != null){
                toast("Title : " + title + "  " + "Content : " + content);
                finish();
            }
        }*/
    }

    @Event(value = {R.id.iv_back, R.id.cb_edit, R.id.circle_imageview}, type = View.OnClickListener.class)
    private void btnClick(View v){
        switch (v.getId()){
            case R.id.iv_back:
                setResult(RESULT_OK, intent);// 返回的时候给一个RESULT_OK
                finish();
                break;
            case R.id.cb_edit:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if(cbEdit.isChecked()){
                        cbEdit.setText("保存");
                        etUsername.setEnabled(true);
                        phoneNumber.setEnabled(true);
                        wechatNumber.setEnabled(true);
                    } else {
                        cbEdit.setText("编辑");
                        etUsername.setEnabled(false);
                        phoneNumber.setEnabled(false);
                        wechatNumber.setEnabled(false);
                        // 上传数据的接口
                        upData();
                    }
                }
                break;
            case R.id.circle_imageview:
                if(cbEdit.isChecked()){
                    new ActionSheetDialog(this)
                            .builder()
                            .setCancelable(false)
                            .setCanceledOnTouchOutside(false)
                            .setTitle("获取相片")
                            .addSheetItem("拍照", ActionSheetDialog.SheetItemColor.Red,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            File outputImage = new File(getExternalCacheDir(),
                                                    "output_image.jpg");
                                            try {
                                                if(outputImage.exists()){
                                                    outputImage.delete();
                                                }
                                                outputImage.createNewFile();
                                            } catch (IOException e){
                                                e.printStackTrace();
                                            }
                                            if (Build.VERSION.SDK_INT >= 24){ // 大于等于android7.0
                                                imageUri = FileProvider.getUriForFile(
                                                        PersonalDataActivity.this,
                                                        "com.example.motor.activity",
                                                        outputImage);
                                                // 这个imagePaht2打印出来是不一样的，这个字符串是带
                                                // 有“content”的。8.0的手机用下面的imagePath2得不到图片
                                                imagePath2 = imageUri.toString();
                                            } else {
                                                imageUri = Uri.fromFile(outputImage);
                                                // 这个imagePath2打印出来是正常的路径，4.4.4的手机没有
                                                // 问题
                                                imagePath2 = imageUri.getPath();
                                            }
                                            //Log.e("imagePath2", imagePath2);
                                            Intent intent = new Intent(
                                                    "android.media.action.IMAGE_CAPTURE");
                                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                            startActivityForResult(intent, 1);
                                        }
                                    })
                            .addSheetItem("从相册中选择", ActionSheetDialog.SheetItemColor.Red,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            Intent intent = new Intent(
                                                    "android.intent.action.GET_CONTENT");
                                            intent.setType("image/*");
                                            startActivityForResult(intent,2);
                                        }
                                    })
                            .show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){// 请求码
            case 1:
                if(resultCode == RESULT_OK){// 结果码
                    if(imagePath2.equals("")){
                        circleImageView.setImageResource(R.drawable.splash15);
                        editor.putString("imagepath", "");
                        editor.apply();
                    } else if (imagePath2.contains("content")){
                        displayImage(imagePath2);
                    } else {
                        displayImage2(imagePath2);
                    }
                }
                break;
            case 2:
                // KitKat是Google（谷歌公司）Android 4.4（安卓系统）的代号  kitkat是奇巧巧克力
                if(resultCode == RESULT_OK){
                    if(Build.VERSION.SDK_INT >= 19){
                        handleImageOnKitKat(data);
                    }else{
                        handleImageBeforeKitKat(data);
                    }
                }
            break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this, uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            //authority 权力、权威、学术权威
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        imagePath2 = imagePath;
        displayImage2(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        imagePath = getImagePath(uri, null);
        imagePath2 = imagePath;
        displayImage2(imagePath);
    }

    private String getImagePath(Uri uri, String selection){
        String Path = null;
        Cursor cursor = getContentResolver().query(uri,null, selection,
                null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                //column列
                Path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return Path;
    }

    private void displayImage(String Path){
        try{
            if(Build.VERSION.SDK_INT >= 24){
                try {
                    Uri uri = Uri.parse(Path);
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 1, baos);
                    circleImageView.setImageBitmap(bitmap);
                    editor.putString("imagepath", Path);
                    editor.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void displayImage2(String Path){
        try{
            Bitmap bitmap = FileUtils.resizeImage(BitmapFactory.decodeFile(Path), 600);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 1, baos);
            circleImageView.setImageBitmap(bitmap);
            editor.putString("imagepath", Path);
            editor.apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 上传数据的接口
    private void upData() {
        String address = prefs1.getString("add", "");
        String userId = String.valueOf(prefs1.getInt("UserID", 0));
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString +
                    "Service/C_WNMS_API.asmx/EditUserInfo");
        } else {
            params = new RequestParams(address +
                    "Service/C_WNMS_API.asmx/EditUserInfo");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("UserFullName", etUsername.getText().toString().trim());
        params.addBodyParameter("Phone", phoneNumber.getText().toString().trim());
        params.addBodyParameter("WeChatID", wechatNumber.getText().toString().trim());
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
                        toast(object1.getString("Message"));
                        MainActivity.instance2.finish();
                        intent.setClass(mActivity, LoginActivity.class);
                        startActivity(intent);
                    } else if (object1.getString("Code").equals("1")){
                        toast(object1.getString("Message"));
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 加载数据的接口
    private void loadData() {
        String address = prefs1.getString("add", "");
        String userId = String.valueOf(prefs1.getInt("UserID", 0));
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString +
                    "Service/C_WNMS_API.asmx/LoadUserInfo");
        } else {
            params = new RequestParams(address +
                    "Service/C_WNMS_API.asmx/LoadUserInfo");
        }
        params.addBodyParameter("userID", userId);
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
                        toast(object1.getString("Message"));
                        MainActivity.instance2.finish();
                        intent.setClass(mActivity, LoginActivity.class);
                        startActivity(intent);
                    } else if (object1.getString("Code").equals("1")){
                        JSONObject object2 = new JSONObject(object1.getString("Data"));
                        etUsername.setText(object2.getString("UserName"));
                        String s = object2.getString("Phone");
                        phoneNumber.setText(object2.getString("Phone") == "null" ? "" : object2.getString("Phone"));
                        wechatNumber.setText(object2.getString("WeChatID") == "null" ? "" : object2.getString("WeChatID"));
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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

    private void toast(String text){
        Toast.makeText(PersonalDataActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
