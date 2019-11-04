package com.example.motor.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.util.FileUtils;
import com.example.motor.util.LinePathView;

import org.xutils.view.annotation.Event;
import org.xutils.x;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HandWriteActivity extends Activity {

    LinePathView mPathView;
    private String fileName;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_write);
        x.view().inject(this);
        mPathView = (LinePathView) findViewById(R.id.view);
        fileName = "";
    }

    @Event(value = {R.id.save1, R.id.clear1}, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.save1:
                if (mPathView.getTouched()) {
                    try {
                        if (getIntent().getIntExtra("Tag", 0) == 100) {
                            fileName = new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date());
                            Log.e("===path100===", FileUtils.SDPATH + fileName + ".png");
                            mPathView.save(FileUtils.SDPATH + fileName + ".png", true, 10);
                            intent = new Intent();
                            intent.putExtra("path", FileUtils.SDPATH + fileName + ".png");
                            setResult(RESULT_OK, intent);
                            finish();
                        } else if (getIntent().getIntExtra("Tag", 0) == 101) {
                            fileName = new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date());
                            Log.e("===path101===", FileUtils.SDPATH + fileName + ".png");
                            mPathView.save(FileUtils.SDPATH + fileName + ".png", true, 10);
                            intent = new Intent();
                            intent.putExtra("path", FileUtils.SDPATH + fileName + ".png");
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(HandWriteActivity.this, "您没有签名~", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.clear1:
                mPathView.clear();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
