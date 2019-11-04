package com.example.motor.activity;

/**
 *  Bitmap在Android中指的是一张图片，可以是png，也可以是jpg等其他图片格式
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.util.Bimp;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.FileUtils;
import com.example.motor.util.ImageItem;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.x;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QMActivity extends Activity {

    private ImageView img1, img2, img3, img4;
    private Bitmap bm1, bm2, bm3, bm4;

    private Intent intent;
    // 图片地址  维保签名、客户签名、机器合照、客户合照
    private String wbqmPath = "", khqmPath = "", jqhzPath = "", khhzPath = "";
    private JSONObject pics = new JSONObject();
    private Loadding loadding;
    private int picNum = 0;
    private SharedPreferences prefs1;
    private String gatewayAddress, guid;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qm);
        x.view().inject(this);
        loadding = new Loadding(this);
        prefs1 = getSharedPreferences("UserInfo", 0);
        gatewayAddress = prefs1.getString("add", "");
        guid = prefs1.getString("guid", "");
        userId = prefs1.getInt("UserID", 0);
    }

    @Event(value = {R.id.img1, R.id.img2, R.id.img3, R.id.img4, R.id.btn3}, type = View
            .OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.img1:
                intent = new Intent(QMActivity.this, HandWriteActivity.class);
                intent.putExtra("Tag", 100);
                startActivityForResult(intent, 100);
                break;
            case R.id.img2:
                intent = new Intent(QMActivity.this, HandWriteActivity.class);
                intent.putExtra("Tag", 101);
                startActivityForResult(intent, 101);
                break;
            case R.id.img3:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 102);
                break;
            case R.id.img4:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 103);
                break;
            case R.id.btn3:
                if (wbqmPath.equals("")) {
                    toast("维保人员还未签字\n请维保人员确认签字");
                } else if (khqmPath.equals("")) {
                    toast("客户还未签字\n请维客户确认签字");
                } else if (jqhzPath.equals("")) {
                    toast("还未上传机器合照\n请上传机器合照");
                } else if (khhzPath.equals("")) {
                    toast("还未上传客户合照\n请上传客户合照");
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("pics", pics.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                img1 = (ImageView) findViewById(R.id.img1);
                if (resultCode == RESULT_OK) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    wbqmPath = data.getStringExtra("path");
                    Log.e("===维保人员签名===", wbqmPath);
                    bm1 = BitmapFactory.decodeFile(wbqmPath, options);
                    File file = new File(wbqmPath);
                    Bitmap bitmap = FileUtils.resizeImage(BitmapFactory.decodeFile(wbqmPath), 600);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    //  进行Base64编码
                    String uploadBuffer = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                    Log.e("===维保人员签名===", uploadBuffer);
                    try {
                        pics.put("wbqm", (file.getName().split("\\."))[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    upData(file.getName(), uploadBuffer);
                }
                break;
            case 101:
                img2 = (ImageView) findViewById(R.id.img2);
                if (resultCode == RESULT_OK) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    khqmPath = data.getStringExtra("path");
                    Log.e("===确认签名===", khqmPath);
                    bm2 = BitmapFactory.decodeFile(khqmPath, options);
                    File file = new File(khqmPath);
                    Bitmap bitmap = FileUtils.resizeImage(BitmapFactory.decodeFile(khqmPath), 600);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    String uploadBuffer = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  //进行Base64编码
                    Log.e("===确认签名===", uploadBuffer);
                    try {
                        pics.put("khqm", (file.getName().split("\\."))[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    upData(file.getName(), uploadBuffer);
                }
                break;
            case 102:
                Bimp.tempSelectBitmap.clear();
                img3 = (ImageView) findViewById(R.id.img3);
                if (Bimp.tempSelectBitmap.size() < 3 && resultCode == RESULT_OK) {
                    try {
                        // 时间戳：new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date())
                        String fileName = new SimpleDateFormat("yyyyMMddHHmmssSSSS")
                                .format(new Date());
                        Bitmap bm = (Bitmap) data.getExtras().get("data");
                        FileUtils.saveBitmap(bm, fileName);  // 保存压缩图片
                        ImageItem takePhoto = new ImageItem();
                        takePhoto.setBitmap(bm);
                        FileUtils.createSDDir(fileName + ".JPEG");  // 创建新目录
                        takePhoto.setImagePath(FileUtils.SDPATH + fileName + ".JPEG");
                        Bimp.tempSelectBitmap.add(takePhoto);
                        bm3 = takePhoto.getBitmap();
                        jqhzPath = takePhoto.getImagePath();
                        File file = new File(takePhoto.getImagePath());
                        Bitmap bitmap = FileUtils.resizeImage(BitmapFactory.decodeFile(takePhoto
                                .getImagePath()), 600);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
                        // 进行Base64编码
                        String uploadBuffer = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                        try {
                            // 因为图片名称为“时间.jpeg”，因此通过split将点之前的名称取出来
                            pics.put("jqhz", (file.getName().split("\\."))[0]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        upData(file.getName(), uploadBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 103:
                Bimp.tempSelectBitmap.clear();
                img4 = (ImageView) findViewById(R.id.img4);
                if (Bimp.tempSelectBitmap.size() < 3 && resultCode == RESULT_OK) {
                    try {
                        String fileName = new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date());
                        Bitmap bm = (Bitmap) data.getExtras().get("data");
                        FileUtils.saveBitmap(bm, fileName);  //  保存压缩图片
                        ImageItem takePhoto = new ImageItem();
                        takePhoto.setBitmap(bm);
                        FileUtils.createSDDir(fileName + ".JPEG");  //  创建新目录
                        takePhoto.setImagePath(FileUtils.SDPATH + fileName + ".JPEG");
                        Bimp.tempSelectBitmap.add(takePhoto);
                        bm4 = takePhoto.getBitmap();
                        khhzPath = takePhoto.getImagePath();
                        File file = new File(takePhoto.getImagePath());
                        Bitmap bitmap = FileUtils.resizeImage(BitmapFactory.decodeFile(takePhoto
                                .getImagePath()), 600);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        String uploadBuffer = new String(Base64.encode(baos.toByteArray(),
                                Base64.DEFAULT));  //进行Base64编码
                        try {
                            pics.put("khhz", (file.getName().split("\\."))[0]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        upData(file.getName(), uploadBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        if (!(bm1 == null)) {
            img1.setImageBitmap(bm1);
        }
        if (!(bm2 == null)) {
            img2.setImageBitmap(bm2);
        }
        if (!(bm3 == null)) {
            img3.setImageBitmap(bm3);
        }
        if (!(bm4 == null)) {
            img4.setImageBitmap(bm4);
        }
    }

    private void upData(String picName, String uploadBuffer) {

        loadding.show("上传数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/Base64ToImg");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/Base64ToImg");
        }
        String pic[] = picName.split("\\.");
        try {
            params.addBodyParameter("base64Str", URLDecoder
                    .decode(uploadBuffer.toString(), "UTF-8").replace(" ", "+"));//图片
            params.addBodyParameter("fileName", pic[0]);  //   图片名称
            params.addBodyParameter("equNo", getIntent().getStringExtra("EquipmentNo"));  //  产品no
            params.addBodyParameter("userId", String.valueOf(userId));//产品no
            params.addBodyParameter("guid", guid);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {
            }

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
                    toast(commonResponseBean.getMessage());
                } else if (commonResponseBean.getCode() == 0) {
                    Intent intent = new Intent(QMActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    toast(commonResponseBean.getMessage());
                }
                if (loadding.isShow()) {
                    loadding.close();
                }
            }
        });
    }

    private void toast(String text){
        Toast.makeText(QMActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
