package com.example.motor.videoSurveillance;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.company.NetSDK.CB_fRealDataCallBackEx;
import com.company.NetSDK.CFG_DSPENCODECAP_INFO;
import com.company.NetSDK.FinalVar;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.SDKDEV_DSP_ENCODECAP_EX;
import com.company.NetSDK.SDK_EXTPTZ_ControlType;
import com.company.NetSDK.SDK_PTZ_ControlType;
import com.company.NetSDK.SDK_RealPlayType;
import com.company.PlaySDK.IPlaySDK;
import com.company.PlaySDK.IPlaySDKCallBack.DEMUX_INFO;
import com.company.PlaySDK.IPlaySDKCallBack.fDemuxCBFun;
import com.example.motor.R;

import java.io.FileOutputStream;
import java.io.IOException;

public class LiveActivity extends Activity {
    private SurfaceView m_PlayView;
    // PTZ
    private LinearLayout back;
    private Button m_btPtz;
    private View m_layoutPtz;
    private PopupWindow m_popPtz;
    private Resources res;
    private byte m_bSpeed = 5;
    private TestRealDataCallBackEx m_callback = new TestRealDataCallBackEx();
    private TestVideoDataCallBack m_VideoCallback = new TestVideoDataCallBack();

    private static int nPort = IPlaySDK.PLAYGetFreePort();   // 要在IPlaySDK.InitSurface之前调用
    private static FileOutputStream m_Fout;

    private static long lRealHandle = 0;

    private boolean bRecordFlag = false;
    private boolean bSound = true;
    private SurfaceHolder holder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liveview);

        // 一般不需要这个函数，偶尔出现过 程序退入后台较长时间，无法找到native方法的情况
        INetSDK.LoadLibrarys();

        res = this.getResources();

        back = (LinearLayout) findViewById(R.id.chronicl_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        m_PlayView = (SurfaceView) findViewById(R.id.view_PlayWindow);
        holder = m_PlayView.getHolder();
        holder.addCallback(new Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("[playsdk]surface", "surfaceCreated");
                IPlaySDK.InitSurface(nPort, m_PlayView);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                Log.d("[playsdk]surface", "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("[playsdk]surface", "surfaceDestroyed");
            }
        });

        m_btPtz = (Button) findViewById(R.id.bt_ptz);
        m_btPtz.setOnClickListener(new LiveButtonsListener());
        m_layoutPtz = View.inflate(LiveActivity.this, R.layout.ptzview, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                if (StartRealPlay(SDK_RealPlayType.SDK_RType_Realplay) == true) {
                    m_callback = new TestRealDataCallBackEx();
                    m_VideoCallback = new TestVideoDataCallBack();

                    if (lRealHandle != 0) {
                        INetSDK.SetRealDataCallBackEx(lRealHandle, m_callback, 1);
                    }
                }
                Looper.loop();
            }
        }).start();

        // stream Type
        int nStreaMask = TestNetSDKActivity.nStreaMask;

        // if nStreaMask from TestNetSDKActivity.nStreaMask is zero, 
        // it means get stream type in TestNetSDKActivity failed, 
        // so, get again here
        if (0 == nStreaMask) {
            SDKDEV_DSP_ENCODECAP_EX stEncodeCapOld = new SDKDEV_DSP_ENCODECAP_EX();
            CFG_DSPENCODECAP_INFO stEncodeCapNew = new CFG_DSPENCODECAP_INFO();
            if (INetSDK.QueryDevState(TestNetSDKActivity.m_loginHandle, FinalVar.SDK_DEVSTATE_DSP_EX, stEncodeCapOld, 6000)) {
                nStreaMask = stEncodeCapOld.dwStreamCap;
            } else if (ToolKits.GetDevConfig(FinalVar.CFG_CMD_HDVR_DSP, stEncodeCapNew, TestNetSDKActivity.m_loginHandle, GlobalSettingActivity.m_nGlobalChn, 1024 * 70)) {
                nStreaMask = stEncodeCapNew.dwStreamCap;
            }
        }
    }

    public boolean StartRealPlay(int nStreamType) {
        lRealHandle = INetSDK.RealPlayEx(TestNetSDKActivity.m_loginHandle, GlobalSettingActivity.m_nGlobalChn, nStreamType);

        if (lRealHandle == 0) {
            ToolKits.showErrorMessage(LiveActivity.this, "RealPlayEx " + res.getString(R.string.info_failed));
            return false;
        }

        boolean bOpenRet = IPlaySDK.PLAYOpenStream(nPort, null, 0, 1024 * 1024 * 2) == 0 ? false : true;
        if (bOpenRet) {
            boolean bPlayRet = IPlaySDK.PLAYPlay(nPort, m_PlayView) == 0 ? false : true;
            if (bPlayRet) {
                boolean bSuccess = IPlaySDK.PLAYPlaySoundShare(nPort) == 0 ? false : true;
                if (!bSuccess) {
                    IPlaySDK.PLAYStop(nPort);
                    IPlaySDK.PLAYCloseStream(nPort);
                    return false;
                }
            } else {
                IPlaySDK.PLAYCloseStream(nPort);
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    public void StopRealPlay() {
        try {
            IPlaySDK.PLAYStop(nPort);
            IPlaySDK.PLAYStopSoundShare(nPort);
            IPlaySDK.PLAYCloseStream(nPort);

            if (bRecordFlag) {
                INetSDK.StopSaveRealData(lRealHandle);
                bRecordFlag = false;
            }

            INetSDK.StopRealPlayEx(lRealHandle);

            if (null != m_Fout) {
                m_Fout.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        lRealHandle = 0;
    }


    private void ptzProcess() {

        if (null == m_popPtz) {
            m_popPtz = new PopupWindow(m_layoutPtz, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        if (m_popPtz.isShowing()) {
            m_popPtz.dismiss();
            return;
        } else {
            m_popPtz.showAtLocation(findViewById(R.id.live_view), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 150);
        }

        Button m_btUp;
        Button m_btDown;
        Button m_btRight;
        Button m_btLeft;
        Button m_btLUp;
        Button m_btRUp;
        Button m_btLDown;
        Button m_btRDown;
        Button m_btMore;
        Button m_btZoomA;
        Button m_btZoomD;
        Button m_btFocusA;
        Button m_btFocusD;
        Button m_btApertA;
        Button m_btApertD;


        m_btUp = (Button) m_layoutPtz.findViewById(R.id.btn_up);
        m_btUp.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_PTZ_ControlType.SDK_PTZ_UP_CONTROL, (byte) 0, m_bSpeed);
            }
        });

        m_btDown = (Button) m_layoutPtz.findViewById(R.id.btn_down);
        m_btDown.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_PTZ_ControlType.SDK_PTZ_DOWN_CONTROL, (byte) 0, m_bSpeed);
            }
        });

        m_btLeft = (Button) m_layoutPtz.findViewById(R.id.btn_left);
        m_btLeft.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_PTZ_ControlType.SDK_PTZ_LEFT_CONTROL, (byte) 0, m_bSpeed);
            }
        });

        m_btRight = (Button) m_layoutPtz.findViewById(R.id.btn_right);
        m_btRight.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_PTZ_ControlType.SDK_PTZ_RIGHT_CONTROL, (byte) 0, m_bSpeed);
            }
        });

        m_btLUp = (Button) m_layoutPtz.findViewById(R.id.btn_lup);
        m_btLUp.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_EXTPTZ_ControlType.SDK_EXTPTZ_LEFTTOP, m_bSpeed, m_bSpeed);
            }
        });

        m_btRUp = (Button) m_layoutPtz.findViewById(R.id.btn_rup);
        m_btRUp.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_EXTPTZ_ControlType.SDK_EXTPTZ_RIGHTTOP, m_bSpeed, m_bSpeed);
            }
        });

        m_btLDown = (Button) m_layoutPtz.findViewById(R.id.btn_ldown);
        m_btLDown.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_EXTPTZ_ControlType.SDK_EXTPTZ_LEFTDOWN, m_bSpeed, m_bSpeed);
            }
        });

        m_btRDown = (Button) m_layoutPtz.findViewById(R.id.btn_rdown);
        m_btRDown.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_EXTPTZ_ControlType.SDK_EXTPTZ_RIGHTDOWN, m_bSpeed, m_bSpeed);
            }
        });

        m_btMore = (Button) m_layoutPtz.findViewById(R.id.btn_more);
        m_btMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToolKits.showMessage(v.getContext(), "Hit More");
                //Jump to another Activity to more ptz operation
            }
        });

        m_btZoomA = (Button) m_layoutPtz.findViewById(R.id.btn_z_add);
        m_btZoomA.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_PTZ_ControlType.SDK_PTZ_ZOOM_ADD_CONTROL, (byte) 0, m_bSpeed);
            }
        });

        m_btZoomD = (Button) m_layoutPtz.findViewById(R.id.btn_z_dec);
        m_btZoomD.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_PTZ_ControlType.SDK_PTZ_ZOOM_DEC_CONTROL, (byte) 0, m_bSpeed);
            }
        });

        m_btFocusA = (Button) m_layoutPtz.findViewById(R.id.btn_f_add);
        m_btFocusA.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_PTZ_ControlType.SDK_PTZ_FOCUS_ADD_CONTROL, (byte) 0, m_bSpeed);
            }
        });

        m_btFocusD = (Button) m_layoutPtz.findViewById(R.id.btn_f_dec);
        m_btFocusD.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_PTZ_ControlType.SDK_PTZ_FOCUS_DEC_CONTROL, (byte) 0, m_bSpeed);
            }
        });

        m_btApertA = (Button) m_layoutPtz.findViewById(R.id.btn_a_add);
        m_btApertA.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_PTZ_ControlType.SDK_PTZ_APERTURE_ADD_CONTROL, (byte) 0, m_bSpeed);
            }
        });

        m_btApertD = (Button) m_layoutPtz.findViewById(R.id.btn_a_dec);
        m_btApertD.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, GlobalSettingActivity.m_nGlobalChn, SDK_PTZ_ControlType.SDK_PTZ_APERTURE_DEC_CONTROL, (byte) 0, m_bSpeed);
            }
        });
    }

    public class TestVideoDataCallBack implements fDemuxCBFun {
        @Override
        public void invoke(int nPort, byte[] pOrgBuffer, int nOrgLen,
                           byte[] pBuffer, int nLen, DEMUX_INFO stInfo, long dwUser) {
            try {
                if (null != m_Fout) {
                    m_Fout.write(pBuffer);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public class TestRealDataCallBackEx implements CB_fRealDataCallBackEx {
        @Override
        public void invoke(long lRealHandle, int dwDataType, byte[] pBuffer,
                           int dwBufSize, int param) {
            if (0 == dwDataType) {
                IPlaySDK.PLAYInputData(nPort, pBuffer, pBuffer.length);
            }
        }
    }

    public boolean PTZControl(MotionEvent event, int nChn, int nControl, byte param1, byte param2) {
        int nAction = event.getAction();
        if ((nAction != MotionEvent.ACTION_DOWN) && (nAction != MotionEvent.ACTION_UP)) {
            return false;
        }

        boolean zRet = INetSDK.SDKPTZControl(TestNetSDKActivity.m_loginHandle, nChn, nControl,
                param1, param2, (byte) 0, nAction == MotionEvent.ACTION_UP);

        return false;
    }

    public class LiveButtonsListener implements OnClickListener {
        @Override
        public void onClick(View btClick) {
            if (btClick == m_btPtz) {
                ptzProcess();
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (null != m_popPtz && m_popPtz.isShowing()) {
            m_popPtz.dismiss();
        }

        if (lRealHandle != 0) {
            StopRealPlay();
        }

        super.onDestroy();
    }
}