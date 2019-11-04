package com.example.motor.audio;

import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author : yanftch
 * Date : 2018/3/24
 * Time : 18:14
 * Desc : 录音管理类
 */

public class AudioManager {
    private MediaRecorder mMediaRecorder; // recorder 录音机
    private String mDir; // 录音存储路径
    private String mCurrentFilePath; // 当前的详细路径
    // 是否已经初始化完毕
    private boolean isPrepared = false;

    private static AudioManager instance;
    private String fileName;

    public static AudioManager getInstance() {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (null == instance) {
                    instance = new AudioManager();
                }
            }
        }
        return instance;
    }

    /**
     * 预备状态
     */
    public void prepareAudio() {
        mDir = Environment.getExternalStorageDirectory() + "/mine_" + "audio";
        try {
            isPrepared = false;
            //  创建文件件路径
            File dir = new File(mDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fileName = generateFileName();
            File file = new File(dir, fileName);
            mCurrentFilePath = file.getAbsolutePath(); // absolute 绝对
            mMediaRecorder = new MediaRecorder();
            /**
             * 如下代码是有顺序的！
             * https://blog.csdn.net/huiguixian/article/details/29852661
             */
            // 设置语音输入源  audio   音频
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 麦克风输入
            // 设置输出音频格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            // 设置输出文件
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            // 设置编码格式
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // 编码器
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            isPrepared = true;

            if (null != mListener) {
                mListener.onPrepared();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        release();
        if (mCurrentFilePath != null) {
            File file = new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath = null;
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            //mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 获取音量
     */
    public int getVoiceLevel(int maxLevel) {
        if (isPrepared) {
            try {
                //mMediaRecorder.getMaxAmplitude()的取值是1-32767之间  amplitude 振幅
                return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
            } catch (Exception e) {
            }
        }
        return 1;
    }

    public String getFilePath() {
        return mCurrentFilePath;
    }

    /**
     * 获取录音文件的名字
     *
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    public interface onPrepareListener {
        void onPrepared();
    }

    public onPrepareListener mListener;

    public void setOnPrepareListener(onPrepareListener listener) {
        this.mListener = listener;
    }

    /**
     * 生成文件名
     *
     * @return
     */
    private String generateFileName() {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSSS");
        Date date = new Date(time);
        String timeString = format.format(date);
        return timeString + ".mp3";
    }
}
