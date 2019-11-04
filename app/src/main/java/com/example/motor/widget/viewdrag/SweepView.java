package com.example.motor.widget.viewdrag;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Iven
 * @date 2017/1/16 10:18
 * @Description
 */

public class SweepView extends ViewGroup {

    private View mChildContent;//内容组件
    private View mChildDel;//删除组件
    private ViewDragHelper mDragHelper;

    private boolean isOpen;//是否打开着

    public SweepView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //第一个参数必须为ViewGroup的对象
        mDragHelper = ViewDragHelper.create(this, new DragCallBack());
    }

    /**
     * @return 对外提供的判断此组件是否打开
     */
    public boolean isOpen() {
        return isOpen;
    }

    class DragCallBack extends ViewDragHelper.Callback {

        private int mCriticalX;//临界x坐标

        /*以删除组件为准的打开和关闭的x坐标*/
        private int mOpenX;
        private int mCloseX;

        /*捕捉ViewGroup内的子组件，参数child为子组件对象。
                        * 返回true就会回调clampViewPositionXXX（）方法。
                        *
                        * 也可针对某个child，返回child==xxx【子组件】就行了*/
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        /*处理拖动事件，第二个参数left指的就是当前拖动的child的x坐标，dx就是改变的值。
        *
        * 在这个方法里可对拖动进行边界限制
        * */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int minLeft = 0;
            int maxLeft = 0;

            //限制内容组件的边界,无非就是限制此组件的x坐标，以屏幕左上角为原点
            if (child == mChildContent) {
                maxLeft = 0;//内容组件不能再向右滑动了，最大x坐标只能为0
                minLeft = -mChildDel.getWidth();
            } else if (child == mChildDel) {
                //对删除组件的边界进行限制
                maxLeft = mChildContent.getWidth();
                minLeft = mChildContent.getWidth() - mChildDel.getWidth();
            }

            if (left < minLeft) {
                left = minLeft;
            } else if (left > maxLeft) {
                left = maxLeft;
            }

            return left;
        }

        /*当当前拖动的子组件位置发生改变时
        *
        * 在这个方法里处理两个子组件的联动
        *
        * 就是内容组件动了，带着删除组件也动
        *
        * 此方法第二个参数就是当前拖动的子组件的最新的x坐标
        * */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            //当拖动的是内容组件时，就对删除组件进行layout布局
            //
            if (changedView == mChildContent) {
                //对删除组件进行重新布局，左坐标和右坐标加上内容组件的最新x坐标就行了
                mChildDel.layout(mChildContent.getWidth() + left, 0, mChildContent.getWidth() +
                        mChildDel.getWidth() + left, mChildDel.getHeight());
            } else if (changedView == mChildDel) {
                //当移动的是删除组件的时候，对内容组件进行重新布局。
                mChildContent.layout(left - mChildContent.getWidth(), 0, left, mChildContent.getHeight());
            }
        }

        /*当手指松开的时候
        *
        * 根据当前位置将组件平稳的滑动
        *
        * 后面两个参数指的是组件离开的速度
        * */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //临界x坐标,判断当前x坐标是否越过它
            mCriticalX = mChildContent.getWidth() - mChildDel.getWidth() / 2;

            /*以删除组件为准的，打开坐标和关闭坐标*/
            mOpenX = mChildContent.getWidth() - mChildDel.getWidth();
            mCloseX = mChildContent.getWidth();

            if (releasedChild == mChildContent) {
                //不应该使用getX方法,应该返回内容组件的右边的实时坐标
                float visualX = mChildContent.getRight();

                smoothChild(visualX);

            } else if (releasedChild == mChildDel) {
                //移动删除组件，以左侧x坐标为准
                float visualX = mChildDel.getLeft();

                smoothChild(visualX);
            }

            invalidate();//触发重新绘制，那么就一定会触发那个compute回调方法
        }

        /**
         * 根据组件判断临界值，来实现打开和关闭的平滑移动
         *
         * @param visualX
         */
        private void smoothChild(float visualX) {
            if (visualX <= mCriticalX) {
                //打开
                mDragHelper.smoothSlideViewTo(mChildDel, mOpenX, 0);
                isOpen = true;
            } else {
                //关闭
                mDragHelper.smoothSlideViewTo(mChildDel, mCloseX, 0);
                isOpen = false;
            }
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /*A测量，下一步就是B布局*/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mChildContent = getChildAt(0);
        mChildDel = getChildAt(1);

        LayoutParams contPar = mChildContent.getLayoutParams();
        //父容器给内容组件指定大小
        int heighContentSpec = MeasureSpec.makeMeasureSpec(contPar.height, MeasureSpec.EXACTLY);
        //测量子组件,内容组件宽随父布局，高度自己定
        mChildContent.measure(widthMeasureSpec, heighContentSpec);

        LayoutParams delPar = mChildDel.getLayoutParams();
        //删除组件的宽和高都是自己定的
        int widDelSpec = MeasureSpec.makeMeasureSpec(delPar.width, MeasureSpec.EXACTLY);
        int heightDelSpec = MeasureSpec.makeMeasureSpec(delPar.height, MeasureSpec.EXACTLY);

        mChildDel.measure(widDelSpec, heightDelSpec);

        //父布局设置最终的宽和高，宽沿用自己测量自己的widthMeasureSpec，高度使用内容布局的高度
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), contPar.height);
    }

    /*B布局子组件，上一步是A测量*/
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mChildContent.layout(0, 0, mChildContent.getMeasuredWidth(), mChildContent.getMeasuredHeight());
        //内容组件在layout方法过后，就可以使用getWidth和getHeight了
        mChildDel.layout(mChildContent.getWidth(), 0, mChildContent.getMeasuredWidth() + mChildDel.getMeasuredWidth(), mChildDel.getMeasuredHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将触摸事件传递到dragHelper来处理
        mDragHelper.processTouchEvent(event);
        return true;
    }
}