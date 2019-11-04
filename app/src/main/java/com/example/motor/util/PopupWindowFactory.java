package com.example.motor.util;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.PopupWindow;

public class PopupWindowFactory {

    private Context mContext;
    private PopupWindow mPop;

    public PopupWindowFactory(Context mContext, View view) {
        this(mContext, view, -2, -2);
    }

    public PopupWindowFactory(Context mContext, View view, int width, int height) {
        this.init(mContext, view, width, height);
    }

    private void init(Context mContext, View view, int width, int height) {
        this.mContext = mContext;
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        this.mPop = new PopupWindow(view, width, height, true);
        this.mPop.setFocusable(true);
        view.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == 4) {
                    PopupWindowFactory.this.mPop.dismiss();
                    return true;
                } else {
                    return false;
                }
            }
        });
        view.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(PopupWindowFactory.this.mPop != null &&
                        PopupWindowFactory.this.mPop.isShowing()) {
                    PopupWindowFactory.this.mPop.dismiss();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public PopupWindow getPopupWindow() {
        return this.mPop;
    }

    public void showAtLocation(View parent, int gravity, int x, int y) {
        if(!this.mPop.isShowing()) {
            this.mPop.showAtLocation(parent, gravity, x, y);
        }
    }

    // anchor 锚
    public void showAsDropDown(View anchor) {
        this.showAsDropDown(anchor, 0, 0);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if(!this.mPop.isShowing()) {
            this.mPop.showAsDropDown(anchor, xoff, yoff);
        }
    }

    public void dismiss() {
        if(this.mPop.isShowing()) {
            this.mPop.dismiss();
        }
    }
}