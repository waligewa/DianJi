package com.example.motor.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.motor.R;


/**
 * Author : yanftch
 * Date : 2018/3/24
 * Time : 21:23
 * Desc :
 */

public class AudioRecordButton extends androidx.appcompat.widget.AppCompatButton {

    private static final String TAG = "zbb_AudioRecordButton";
    private static final int STATUS_NORMAL = 1;// 正常状态
    private static final int STATUS_RECORDING = 2;// 录制状态
    private static final int STATUS_MEAN_TO_CANCEL = 3;// 取消状态
    private static final int MESSAGE_AUDIO_PREPARED = 1;
    private static final int MESSAGE_VOICE_CHANGE = 2;
    private static final int MESSAGE_DISMISS = 3;

    private static final int Y_DISTANCE = 30;

    private int mCurrentStatus = STATUS_NORMAL;// 当前状态
    private boolean isRecording;// 是否正在录音

    private DialogManager mDialogManager;
    private AudioManager mAudioManager;
    private float mTime;
    // 是否触发onLongClick
    private boolean mReady;
    public onAudioFinishRecoredListener mListener;
    public onAudioRecordingListener mOnAudioRecordingListener;

    public AudioRecordButton(Context context) {
        super(context);
        initThis();
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initThis();
    }

    public AudioRecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initThis();
    }

    // audio 音频
    public void setOnAudioFinishListener(onAudioFinishRecoredListener l) {
        mListener = l;
    }

    // recorder 录音机
    public interface onAudioFinishRecoredListener {
        void onRecoredFinish(float seconds, String path, String fileName);
    }

    public interface onAudioRecordingListener {
        void onRecording(float seconds);
    }

    public void setOnAudioRecordingListener(onAudioRecordingListener listener) {
        mOnAudioRecordingListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {// motion  运动

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                changeStatus(STATUS_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    if (meanToCancle(x, y)) {
                        changeStatus(STATUS_MEAN_TO_CANCEL);
                    } else {
                        changeStatus(STATUS_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onTouchEvent: mReady===" + mReady + "      isRecording==" +
                        isRecording + "  mTime=" + mTime);
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                if (!isRecording || mTime < 0.6f) {
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    mHandler.sendEmptyMessageDelayed(MESSAGE_DISMISS, 1500);  // delay 延迟
                } else if (mCurrentStatus == STATUS_RECORDING) {
                    // 正常录制结束
                    mDialogManager.dimiss();
                    Log.e(TAG, "onTouchEvent: mTime=" + mTime + "  filePath=" + mAudioManager.getFilePath());
                    if (null != mListener) {
                        mListener.onRecoredFinish(mTime, mAudioManager.getFilePath(),
                                mAudioManager.getFileName());
                    }
                    mAudioManager.release();
                } else if (mCurrentStatus == STATUS_MEAN_TO_CANCEL) {
                    //取消
                    mDialogManager.dimiss();
                    mAudioManager.cancel();
                }
                reset();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void reset() {
        isRecording = false;
        mReady = false;
        changeStatus(STATUS_NORMAL);
        mTime = 0f;
    }

    /**
     * 根据坐标来判断是否要取消
     *
     * @param x
     * @param y
     * @return
     */
    private boolean meanToCancle(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        if (y < -Y_DISTANCE || y > getHeight() + Y_DISTANCE) {
            return true;
        }
        return false;
    }

    /**
     * 改变录制状态
     */
    private void changeStatus(int status) {
        if (mCurrentStatus != status) {
            mCurrentStatus = status;
            switch (status) {
                case STATUS_NORMAL:
                    setBackgroundResource(R.drawable.selector_record_normal);
                    setText(R.string.record_status_normal_);
                    break;
                case STATUS_RECORDING:
                    setBackgroundResource(R.drawable.selector_record_recording);
                    setText(R.string.record_status_recroding);
                    if (isRecording) {
                        // TODO: 2018/3/25 更新Dialog
                        mDialogManager.recording();
                    }
                    break;
                case STATUS_MEAN_TO_CANCEL:
                    setBackgroundResource(R.drawable.selector_record_recording);
                    setText(R.string.record_status_mean_to_cancel);
                    mDialogManager.meanToCancel();
                    break;
            }
        }
    }

    private void initThis() {
        mDialogManager = new DialogManager(getContext());

        mAudioManager = AudioManager.getInstance();
        mAudioManager.setOnPrepareListener(new AudioManager.onPrepareListener() {
            @Override
            public void onPrepared() {
                mHandler.sendEmptyMessage(MESSAGE_AUDIO_PREPARED);
            }
        });
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mReady = true;
                mAudioManager.prepareAudio();
                if (mOnAudioRecordingListener != null) {
                    mOnAudioRecordingListener.onRecording(mTime);
                }
                return false;
            }
        });
    }

    /**
     * 获取音量大小
     */
    private Runnable getVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;
                    mHandler.sendEmptyMessage(MESSAGE_VOICE_CHANGE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    @SuppressLint("HandlerLeak")

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_AUDIO_PREPARED:
                    //  必须audio 准备完毕之后
                    mDialogManager.showDialog();
                    isRecording = true;
                    new Thread(getVoiceLevelRunnable).start();
                    break;
                    //  这个地方就是那个跳动的7个小格子
                case MESSAGE_VOICE_CHANGE:
                    mDialogManager.updateLevel(mAudioManager.getVoiceLevel(7));
                    if (mOnAudioRecordingListener != null) {
                        mOnAudioRecordingListener.onRecording(mTime);
                    }
                    break;
                case MESSAGE_DISMISS:
                    mDialogManager.dimiss();
                    break;
            }
        }
    };

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density; //  density  密度
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
