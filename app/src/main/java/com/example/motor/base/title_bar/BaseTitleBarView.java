package com.example.motor.base.title_bar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.motor.R;


/**
 * Author : yanftch
 * Date : 2018/1/11
 * Time : 13:24
 * Desc :
 */

public class BaseTitleBarView extends RelativeLayout {

    private static final String TAG = "dah_BaseTitleBarView";
    private Context mContext;
    // 返回箭头图标
    private LinearLayout titleLeftImage;
    // 标题
    private TextView title_title;
    // 右侧容器
    private LinearLayout rightContainer;
    // 右侧图标按钮
    private ImageView titleRightImage;
    // 右侧文案
    private TextView titleRightText;

    private static float density;

    public BaseTitleBarView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BaseTitleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public BaseTitleBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(getLayoutResId(), this);//添加自定义Layout
        titleLeftImage = (LinearLayout) findViewById(R.id.titleLeftImage);
        title_title = (TextView) findViewById(R.id.titleTitle);
        rightContainer = (LinearLayout) findViewById(R.id.titleRightContainer);
        titleRightImage = (ImageView) findViewById(R.id.titleRightImg);
        titleRightText = (TextView) findViewById(R.id.titleRightText);
        // metric 度量
        DisplayMetrics dm = mContext.getApplicationContext().getResources().getDisplayMetrics();
        density = dm.density;
        if (null != titleLeftImage) {
            if (null != mTitleBarClick) {
                mTitleBarClick.onTitleLeftPressed();
            }
        }
    }

    /**
     * 隐藏TitleBar
     */
    public void setBaseTitleBarViewGone() {
        this.setVisibility(GONE);
    }

    /**
     * 默认设置左边返回显示
     */
    public void setLeftContainerVisible() {
        titleLeftImage.setVisibility(VISIBLE);
    }

    /**-------------------------------右边处理-----begin-------------------------------*/

    /**
     * 设置右边文本
     */
    public void setRightTitleText(String rightTitleText) {
        titleRightText.setText(rightTitleText);
        titleRightText.setVisibility(VISIBLE);
        rightContainer.setVisibility(VISIBLE);
        titleRightText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mTitleBarClick) { //  容错处理
                    mTitleBarClick.onTitleRightTextPressed();
                }
            }
        });
        visible();
    }

    /**
     * 设置右边文本的字体颜色
     */
    public void setRightTitleTextColor(int color) {
        rightContainer.setVisibility(VISIBLE);
        titleRightText.setVisibility(VISIBLE);
        titleRightText.setTextColor(color);
        visible();
    }

    /**
     * 隐藏右边的文本
     */
    public void setRightTitleGone() {
        titleRightText.setVisibility(GONE);
        visible();
    }

    /**
     * 隐藏整个右边
     */
    public void setRightGone() {
        rightContainer.setVisibility(INVISIBLE);
        visible();
    }

    /**-------------------------------右边处理-----end-------------------------------*/

    /**-------------------------------------------左边处理-----begin-------------------------------------------------------*/

    /**
     * 设置左边的点击事件
     */
    public void setOnLeftTitleClickListener(OnClickListener onClickListener) {
        titleLeftImage.setVisibility(VISIBLE);
        titleLeftImage.setOnClickListener(onClickListener);
        visible();
    }

    /**
     * 设置左边图标
     *
     * @param drawable
     */
    public void setLeftDrawable(int drawable) {
        if (drawable != -1) {
            titleLeftImage.setBackgroundResource(drawable);
        }
        titleLeftImage.setVisibility(View.VISIBLE);
        titleLeftImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTitleBarClick != null) {
                    mTitleBarClick.onTitleLeftPressed();
                }
            }
        });
        this.setVisibility(View.VISIBLE);
    }
    /**-------------------------------左边处理-----end-------------------------------*/

    /**
     * 设置右边图标
     *
     * @param drawable
     */
    public void setRightDrawable(int drawable) {
        if (drawable != -1) {
            titleRightImage.setBackgroundResource(drawable);
        }
        rightContainer.setVisibility(VISIBLE);
        titleRightText.setVisibility(GONE);
        titleRightImage.setVisibility(View.VISIBLE);
        titleRightImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTitleBarClick != null) {
                    mTitleBarClick.onTitleRightImagePressed();
                }
            }
        });
        visible();
    }
    /**-------------------------------Title处理-----begin-------------------------------*/

    /**
     * 设置居中标题Title
     */
    public void setTitleText(String title) {
        title_title.setText(title);
        title_title.setVisibility(VISIBLE);
        title_title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mTitleBarClick) {
                    mTitleBarClick.onTitlePressed();
                }
            }
        });
        visible();
    }

    /**
     * 设置标题字体颜色
     */
    public void setTitleTextColor(int color) {
        title_title.setVisibility(VISIBLE);
        title_title.setTextColor(color);
        visible();
    }

    /**
     * Title点击监听
     */
    public void addOnTitleClickListener(OnClickListener clickListener) {
        title_title.setOnClickListener(clickListener);
    }

    /*-------------------------------Title处理-----end-------------------------------*/


    protected int getLayoutResId() {
        return R.layout.base_common_title_bar_layout;
    }


    /**
     * 点击监听
     */
    public interface TitleBarClick {
        /**
         * Title 点击
         */
        void onTitlePressed();

        /**
         * 左边点击(返回键的容器)
         */
        void onTitleLeftPressed();

        /**
         * 右边文本点击
         */
        void onTitleRightTextPressed();

        /**
         * 右边图片点击
         */
        void onTitleRightImagePressed();
    }

    private TitleBarClick mTitleBarClick;

    public void setTitleBarClick(TitleBarClick titleBarClick) {
        mTitleBarClick = titleBarClick;
    }

    private void visible() {
        this.setVisibility(VISIBLE);
    }

    private void gone() {
        this.setVisibility(GONE);
    }

    private void invisible() {
        this.setVisibility(INVISIBLE);
    }

    public static int dip2px(float dipValue) {
        return (int) (dipValue * density + 0.5f);
    }
}
