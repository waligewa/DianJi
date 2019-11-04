package com.example.motor.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class VideoPlayingActivity extends BaseActivity {

    private Intent intent;
    @ViewInject(R.id.video)
    VideoView videoView;

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_video_playing;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("视频播放");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    // 初始化控件，设置监听
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        init();
    }

    // View的click事件
    @Override
    public void widgetClick(View v) {}

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    private void init() {
        intent = new Intent();
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/mine_video.mp4"); // 将路径转换成uri
        videoView.setVideoURI(uri); // 为视频播放器设置视频路径
        videoView.setMediaController(new MediaController(VideoPlayingActivity.this)); // 显示控制栏
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start(); // 开始播放视频
            }
        });
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
