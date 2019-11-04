package com.example.motor.audio;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.motor.R;


/**
 * Author : yanftch
 * Date : 2018/3/25
 * Time : 09:27
 * Desc :
 */

public class DialogManager {

    int a = R.layout.record_dialog_layout;
    private Dialog mDialog;
    private ImageView mIcon;
    private ImageView mVoice;
    private TextView mTextView;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public DialogManager(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    private void init() {
        mDialog = new Dialog(mContext, R.style.dialog_style);
        View inflate = mLayoutInflater.inflate(R.layout.record_dialog_layout, null);
        mDialog.setContentView(inflate);
        mIcon = (ImageView) inflate.findViewById(R.id.ivRecordDialogIcon);
        mVoice = (ImageView) inflate.findViewById(R.id.ivRecordDialogVoice);
        mTextView = (TextView) inflate.findViewById(R.id.tvRecordDialogLevel);
    }

    public void showDialog() {
        init();
        mDialog.show();
    }

    public void recording() {
        if (null != mDialog && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.recorder);
            mTextView.setText("手指上滑 取消录制");
        }
    }

    public void meanToCancel() {
        if (null != mDialog && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
            mIcon.setImageResource(R.drawable.cancel2);
            mTextView.setText("松开手指 取消录制");
        }
    }

    public void dimiss() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void tooShort() {
        if (null != mDialog && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.voice_to_short);
            mTextView.setText("录音时间过短");
        }
    }

    /**
     *
     * @param level  1-7
     */
    public void updateLevel(int level) {
        if (null != mDialog && mDialog.isShowing()) {
            //  mIcon.setVisibility(View.VISIBLE);
            //  mVoice.setVisibility(View.VISIBLE);
            //  mTextView.setVisibility(View.VISIBLE);
            //  identifier  标识符
            int resId = mContext.getResources().getIdentifier("v" + level, "drawable",
                    mContext.getPackageName());
            mVoice.setImageResource(resId);
        }
    }
}
