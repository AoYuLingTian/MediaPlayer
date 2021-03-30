package com.example.mymusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * @Author: longyu
 * @CreateDate: 2021/3/29 10:10
 * @Description:
 */
public class SlideLineView extends LinearLayout {

    private Context mContext = null;
    //线的画笔
    private Paint mLineaPaint = null;
    //滑过的部分的线的画笔
    private Paint mAcrossLineaPaint = null;
    //点的画笔
    private Paint mPointPaint = null;
    //线的颜色
    private int mLineaColor = R.color.colorAccent;
    //滑过的部分的线的颜色
    private int mAcrossLineaColor = R.color.colorPrimary;
    //点的颜色
    private int mPointColor = R.color.blue;
    //控件的宽
    private float mViewWidth = 0f;
    //控件的高
    private float mViewHeight = 0f;
    //开始的位置
    private float mLineaStartX = 0f;
    //结束的位置
    private float mLineaEndX = 0f;
    //线的长度。不是控件的宽度
    private float mLineaLength = 0f;
    //线在Y轴方向的中心位置
    private float mCenterY = 0f;

    private float mDrawX = 0f;

    private float mStep = 0f;
    //最大长度
    private float mMaxValue = 0f;

    private float mDownX = 0f;

    private float mDownY = 0f;

    private float mMoveX = 0f;

    private float mMoveY = 0f;

    //是否正在滑动
    private boolean isSlide = false;
    //是否滑动
    private boolean isCanSlide = false;

    private SlideLinePercentageListener mSlideLinePercentageListener;

    public void setSlideLinePercentageListener(SlideLinePercentageListener slideLinePercentageListener) {
        this.mSlideLinePercentageListener = slideLinePercentageListener;
    }

    public SlideLineView(Context context) {
        super(context);
        mContext = context;
        setBackgroundColor(Color.TRANSPARENT);
        init();
    }

    public SlideLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setBackgroundColor(Color.TRANSPARENT);
        init();
    }

    public SlideLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setBackgroundColor(Color.TRANSPARENT);
        init();
    }

    /**
     * 初始化
     */
    @SuppressLint("ResourceAsColor")
    private void init() {
        mLineaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLineaPaint.setColor(mLineaColor);
        //设置画笔样式为填充
        mLineaPaint.setStyle(Paint.Style.FILL);
        //设置画笔宽度
        mLineaPaint.setStrokeWidth(dp2px(mContext, 3f));
        /**
         * Paint.Cap.BUTT：默认类型
         *
         * Paint.Cap.SQUARE：以线条宽度为大小，在开头和结尾分别添加半个正方形
         *
         * Paint.Cap.ROUND：以线条宽度为直径，在开头和结尾分别添加一个半圆
         */
        mLineaPaint.setStrokeCap(Paint.Cap.ROUND);

        //设置 滑过的线的画笔的属性
        mAcrossLineaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAcrossLineaPaint.setColor(mAcrossLineaColor);
        mAcrossLineaPaint.setStyle(Paint.Style.FILL);
        mAcrossLineaPaint.setStrokeWidth(dp2px(mContext, 3f));
        mAcrossLineaPaint.setStrokeCap(Paint.Cap.ROUND);

        //设置 滑过的线的画笔的属性
        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(mPointColor);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setStrokeWidth(dp2px(mContext, 10f));
        mPointPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
            requestDisallowInterceptTouchEvent(true);
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            requestDisallowInterceptTouchEvent(false);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, (int) dp2px(mContext, 30f));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = (float) w;
        mViewHeight = (float) h;
        mLineaStartX = dp2px(mContext, 15f);
        mLineaEndX = mViewWidth - dp2px(mContext, 15f);
        mLineaLength = mLineaEndX - mLineaStartX;
        mDrawX = mLineaStartX;
        mCenterY = mViewHeight / 2;
        if (mMaxValue != 0f) {
            mStep = mLineaLength / mMaxValue;
        } else {
            mStep = 0f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(mLineaStartX, mCenterY, mLineaEndX, mCenterY, mLineaPaint);
        canvas.drawLine(mLineaStartX, mCenterY, mDrawX, mCenterY, mAcrossLineaPaint);
        canvas.drawPoint(mDrawX, mViewHeight / 2, mPointPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanSlide) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                isSlide = true;
                mSlideLinePercentageListener.audioStart();
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = event.getX();
                mMoveY = event.getY();
                isSlide = true;
                if (mMoveX <= mLineaStartX) {
                    mDrawX = mLineaStartX;
                } else if (mMoveX >= mLineaEndX) {
                    mDrawX = mLineaEndX;
                } else {
                    mDrawX = mMoveX;
                }
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mMoveX = event.getX();
                isSlide = false;
                mSlideLinePercentageListener.slidePercentageResult((mDrawX - mLineaStartX) / mLineaLength, 0f, 1f);
                break;
        }
        return true;
    }

    /**
     * 设置最大长度
     *
     * @param maxValue
     */
    public void setMaxValue(float maxValue) {
        mMaxValue = maxValue;
        if (mMaxValue != 0f) {
            mStep = mLineaLength / mMaxValue;
        } else {
            mStep = 0f;
        }
    }

    /**
     * 设置步子添加，即 小圆点正向移动
     * <p>
     * isAllowMove：是否允许移动（添加或减少）
     * isAdd：是否是添加（正向移动）
     * stepNum：一下走几步（几个单位的步子）
     */
    public void setStepMove(boolean isAllowMove, boolean isAdd, int stepNum) {
        if (isAllowMove) {
            if (isAdd) {
                mDrawX += mStep * stepNum;
            } else {
                mDrawX -= mStep * stepNum;
            }
            if (mDrawX <= mLineaStartX) {
                mDrawX = mLineaStartX;
            } else if (mDrawX >= mLineaEndX) {
                mDrawX = mLineaEndX;
            }
            postInvalidate();
            mSlideLinePercentageListener.moveResult((mDrawX - mLineaStartX) / mLineaLength,
                    mDrawX <= mLineaStartX, mDrawX >= mLineaEndX, isAdd, stepNum);
        }
    }

    /**
     * 获取是否正在滑动
     *
     * @return
     */
    public boolean getIsNowSlide() {
        return isSlide;
    }

    /**
     * 设置是否滑动
     *
     * @param b
     */
    public void setCanSlide(boolean b) {
        isCanSlide = b;
    }

    //重置控件状态
    public void resetViewStatus() {
        mDrawX = mLineaStartX;
        postInvalidate();
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpValue
     * @return
     */
    private float dp2px(Context context, float dpValue) {
        if (context != null) {
            float scale = context.getResources().getDisplayMetrics().density;
            return dpValue * scale + 0.5f;
        }
        return 0;
    }

    public interface SlideLinePercentageListener {
        void slidePercentageResult(float nowPercentage, float min, float max);

        void moveResult(float nowPercentage, boolean isStart, boolean isEnd, boolean isAdd, int stepNum);

        void audioStart();

        void audioPause();
    }

}
