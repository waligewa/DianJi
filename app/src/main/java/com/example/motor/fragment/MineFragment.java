package com.example.motor.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.motor.R;
import com.example.motor.activity.AboutActivity;
import com.example.motor.activity.LoginActivity;
import com.example.motor.activity.MainActivity;
import com.example.motor.activity.SettingActivity;
import com.example.motor.activity.PersonalDataActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.SPUtils;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * MineFragment，我的界面
 *
 */
public class MineFragment extends Fragment implements View.OnClickListener{

    private CircleImageView circleImageView;    // 圆形图片
    private ImageView fullPicture;              // fixXY的图片
    private TextView tvUsername, phoneNumber, wechatNumber;
    private LinearLayout personalData, introduction, setting;
    private Intent intent;
    private SharedPreferences prefs1;
    private String imagePath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_mine_fragment, null);
        personalData = (LinearLayout) view.findViewById(R.id.personal_data);
        introduction = (LinearLayout) view.findViewById(R.id.enterprise_introduction);
        setting = (LinearLayout) view.findViewById(R.id.setting);
        personalData.setOnClickListener(this);
        introduction.setOnClickListener(this);
        setting.setOnClickListener(this);
        tvUsername = (TextView) view.findViewById(R.id.tv_username);
        phoneNumber = (TextView) view.findViewById(R.id.tv_phone_number);
        wechatNumber = (TextView) view.findViewById(R.id.tv_wechat_number);
        fullPicture = (ImageView) view.findViewById(R.id.full_picture);
        circleImageView = (CircleImageView) view.findViewById(R.id.circle_imageview);
        init();
        x.view().inject(getActivity());
        return view;
    }

    private void init(){
        intent = new Intent();
        prefs1 = getActivity().getSharedPreferences("UserInfo", 0);
        imagePath = prefs1.getString("imagepath", "");
        if(imagePath.equals("")){
            circleImageView.setImageResource(R.drawable.splash15);
            fullPicture.setImageResource(R.drawable.splash15);
        } else {
            Glide.with(getActivity())
                    .load(imagePath)
                    .skipMemoryCache(true)  // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                    .into(circleImageView);
            // 感觉整个背景图更换成那张图片不好看
            /*Glide.with(this)
                    .load(imagePath)
                    .skipMemoryCache(true)  // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                    .into(fullPicture);*/
        }
        // 加载数据的接口
        loadData();
    }

    public void onClick(View v){
        switch (v.getId()){
            // 个人资料
            case R.id.personal_data:
                intent.setClass(getActivity(), PersonalDataActivity.class);
                startActivityForResult(intent, 1000);// 先加一个requestCode 1000
                break;
            // 设置
            case R.id.setting:
                intent.setClass(getActivity(), SettingActivity.class);
                startActivity(intent);
                break;
            // 企业简介
            case R.id.enterprise_introduction:
                intent.setClass(getActivity(), AboutActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);  // 这个super可不能落下，否则可能回调不了
        switch(requestCode){
            case 1000:
                // 如果既是1000又是RESULT_OK的话就可以执行下面的
                if(resultCode == getActivity().RESULT_OK){
                    imagePath = prefs1.getString("imagepath", "");
                    if(imagePath.equals("")){
                        circleImageView.setImageResource(R.drawable.splash15);
                        fullPicture.setImageResource(R.drawable.splash15);
                    } else {
                        Glide.with(getActivity())
                                .load(imagePath)
                                .skipMemoryCache(true)  // 不使用内存缓存
                                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                                .into(circleImageView);
                        // 感觉整个背景图更换成那张图片不好看
                        /*Glide.with(this)
                                .load(imagePath)
                                .skipMemoryCache(true)  // 不使用内存缓存
                                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                                .into(fullPicture);*/
                    }
                    // 加载数据的接口
                    loadData();
                }
                break;
        }
    }

    // 加载数据的接口
    private void loadData() {
        String address = prefs1.getString("add", "");
        String userId = String.valueOf(prefs1.getInt("UserID", 0));
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
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
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    } else if (object1.getString("Code").equals("1")){
                        JSONObject object2 = new JSONObject(object1.getString("Data"));
                        tvUsername.setText("用户名称：" + object2.getString("UserName"));
                        phoneNumber.setText(object2.getString("Phone") == "null" ? "电话号码：" :
                                "电话号码：" + object2.getString("Phone"));
                        wechatNumber.setText(object2.getString("WeChatID") == "null" ? "微信号码：" :
                                "微信号码：" + object2.getString("WeChatID"));
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void toast(String text){
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
