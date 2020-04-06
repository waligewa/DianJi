package com.example.motor.workorderlist.repair;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.activity.LoginActivity;
import com.example.motor.adapter.TaskRvAdapter3;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.TaskItemBean2;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *维修已处理的fragment
 *
 */
public class HandledlFragment extends Fragment implements View.OnClickListener{

    private LinearLayout linearLayout;
    private RecyclerView recyclerView;
    private List<TaskItemBean2> datas = new ArrayList<>();
    private List<TaskItemBean2> gsonDatas = new ArrayList<>();
    private Loadding loadding;
    private String gatewayAddress, userId;
    private SharedPreferences prefs1;
    private Intent intent;
    private TaskRvAdapter3 mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        linearLayout = (LinearLayout) inflater.inflate(R.layout.handledfragment, container, false);
        recyclerView = (RecyclerView) linearLayout.findViewById(R.id.recyclerView);
        prefs1 = getActivity().getSharedPreferences("UserInfo", 0);
        loadding = new Loadding(getActivity());
        intent = new Intent();

        mAdapter = new TaskRvAdapter3(getActivity(), datas);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);
        gatewayAddress = prefs1.getString("add", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        return linearLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onClick(View v){
        switch (v.getId()){
            default:
                break;
        }
    }

    // 获取已处理的列表数据
    private void getData2() {
        datas.clear();
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2100-12-31");
        params.addBodyParameter("guid", "");
        params.addBodyParameter("pageIndex", "1");
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        Type listType = new TypeToken<List<TaskItemBean2>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个true代表是已处理
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("true") /*&&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId)*/){
                                datas.add(gsonDatas.get(i));
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loadding.isShow()) {
                    loadding.close();
                }
            }
        });
    }

    private void toast(String text){
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 获取已处理的列表数据
        getData2();
    }
}
