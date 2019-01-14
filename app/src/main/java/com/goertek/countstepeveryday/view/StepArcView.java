package com.goertek.countstepeveryday.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.goertek.countstepeveryday.R;

/**
 * Created by clara.tong on 2018/7/25.
 */

public class StepArcView extends View {

    /**
     * 圆弧的宽度
     */
    private float borderWidth = dipToPx(14);
    /**
     * 开始绘制圆弧的角度。顺时针方向，右中间开始为0度
     */
    private float startAngle = 135;
    /**
     * 终点对应的角度和起始点对应的角度的夹角
     */
    private float sweepAngle = 270;
    /**
     * 当前对应的角度和起始点对应的角度的夹角
     */
    private float currentAngle = 0;

    //步数字体大小
    private int textNumberSize = 0;

    //当前步数
    private String stepNumber = "0";

    private int animationTime = 3000;

    public StepArcView(Context context) {
        super(context);
    }

    public StepArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StepArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth()/2;
        RectF rectF = new RectF(0+borderWidth,borderWidth,2*centerX-borderWidth,2*centerX-borderWidth);
        //绘制整体黄色圆弧
        drawArcYellow(canvas,rectF);
        //绘制当前进度红色圆弧
        drawArcRed(canvas,rectF);
        //绘制当前步数
        drawTextNumber(canvas,centerX);
        drawTextStepString(canvas,centerX);
    }

    private void drawArcYellow(Canvas canvas, RectF rectF){
        Paint mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.yellow));
        mPaint.setAntiAlias(true);
        /** 结合处为圆弧*/
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        /** 设置画笔的样式 Paint.Cap.Round ,Cap.SQUARE等分别为圆形、方形*/
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        /** 设置画笔的填充样式 Paint.Style.FILL  :填充内部;Paint.Style.FILL_AND_STROKE  ：填充内部和描边;  Paint.Style.STROKE  ：仅描边*/
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(borderWidth);
        /**绘制圆弧的方法
         * drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint)//画弧，
         参数一是RectF对象，一个矩形区域椭圆形的界限用于定义在形状、大小、电弧，
         参数二是起始角(度)在电弧的开始，圆弧起始角度，单位为度。
         参数三圆弧扫过的角度，顺时针方向，单位为度,从右中间开始为零度。
         参数四是如果这是true(真)的话,在绘制圆弧时将圆心包括在内，通常用来绘制扇形；如果它是false(假)这将是一个弧线,
         参数五是Paint对象；
         */
        canvas.drawArc(rectF,startAngle,sweepAngle,false,mPaint);
    }

    private void drawArcRed(Canvas canvas,RectF rectF){
        Paint currentPaint = new Paint();
        currentPaint.setColor(getResources().getColor(R.color.red));
        currentPaint.setAntiAlias(true);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeWidth(borderWidth);
        currentPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(rectF,startAngle,currentAngle,false,currentPaint);
    }

    private void drawTextNumber(Canvas canvas, float centerX){
        Paint vTextPaint = new Paint();
        vTextPaint.setAntiAlias(true);
        vTextPaint.setTextAlign(Paint.Align.CENTER);
        vTextPaint.setColor(getResources().getColor(R.color.red));
        vTextPaint.setTextSize(textNumberSize);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        vTextPaint.setTypeface(font);//字体风格
        Rect bounds_Number = new Rect();
        vTextPaint.getTextBounds(stepNumber, 0, stepNumber.length(), bounds_Number);
        canvas.drawText(stepNumber, centerX, getHeight() / 2 + bounds_Number.height() / 2, vTextPaint);
    }

    /**
     * 4.圆环中心[步数]的文字
     */
    private void drawTextStepString(Canvas canvas, float centerX) {
        Paint vTextPaint = new Paint();
        vTextPaint.setTextSize(dipToPx(16));
        vTextPaint.setTextAlign(Paint.Align.CENTER);
        vTextPaint.setAntiAlias(true);//抗锯齿功能
        vTextPaint.setColor(getResources().getColor(R.color.grey));
        String stepString = "步数";
        Rect bounds = new Rect();
        vTextPaint.getTextBounds(stepString, 0, stepString.length(), bounds);
        canvas.drawText(stepString, centerX, getHeight() / 2 + bounds.height() + getFontHeight(textNumberSize), vTextPaint);
    }

    /**
     * 获取当前步数的数字的高度
     *
     * @param fontSize 字体大小
     * @return 字体高度
     */
    public int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Rect bounds_Number = new Rect();
        paint.getTextBounds(stepNumber, 0, stepNumber.length(), bounds_Number);
        return bounds_Number.height();
    }

    private int dipToPx(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /**
     * 所走的步数进度
     *
     * @param totalStepNum  设置的步数
     * @param currentCounts 所走步数
     */
    public void setCurrentCount(int totalStepNum, int currentCounts) {
     /**如果当前走的步数超过总步数则圆弧还是270度，不能成为圆*/
        if(currentCounts > totalStepNum){
            currentCounts = totalStepNum;
        }
        /*上次所走步数*/
        float scalePrevious = (float)Integer.valueOf(stepNumber)/totalStepNum;
        /*步数转换为角度*/
        float previousAngle = scalePrevious * sweepAngle;

         /**所走步数占用总共步数的百分比*/
        float scaleCurrent = (float)currentCounts/totalStepNum;
        /*步数转换为角度*/
        float currentLength = scaleCurrent * sweepAngle;
        /**开始执行动画*/
        setAnimation(previousAngle,currentLength,animationTime);

        stepNumber = String.valueOf(currentCounts);
        setTextSize(currentCounts);

    }

    /**
     * 为进度设置动画
     * ValueAnimator是整个属性动画机制当中最核心的一个类，属性动画的运行机制是通过不断地对值进行操作来实现的，
     * 而初始值和结束值之间的动画过渡就是由ValueAnimator这个类来负责计算的。
     * 它的内部使用一种时间循环的机制来计算值与值之间的动画过渡，
     * 我们只需要将初始值和结束值提供给ValueAnimator，并且告诉它动画所需运行的时长，
     * 那么ValueAnimator就会自动帮我们完成从初始值平滑地过渡到结束值这样的效果。
     *
     * @param start   初始值
     * @param current 结束值
     * @param length  动画时长
     */
    private void setAnimation(float start, float current, int length) {
        final ValueAnimator progressAnimator = ValueAnimator.ofFloat(start,current);
        progressAnimator.setDuration(length);
        progressAnimator.setTarget(currentAngle);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentAngle = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        progressAnimator.start();

    }

    /**
     * 设置文本大小,防止步数特别大之后放不下，将字体大小动态设置
     *
     * @param num
     */
    public void setTextSize(int num) {
        String s = String.valueOf(num);
        int length = s.length();
        if (length <= 4) {
            textNumberSize = dipToPx(50);
        } else if (length > 4 && length <= 6) {
            textNumberSize = dipToPx(40);
        } else if (length > 6 && length <= 8) {
            textNumberSize = dipToPx(30);
        } else if (length > 8) {
            textNumberSize = dipToPx(25);
        }
    }
}
