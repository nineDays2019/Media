package me.juhezi.eternal.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import me.juhezi.eternal.extension.ContextExtensionKt;

/**
 * 跑马灯 TextView
 */
public class MarqueeTextView extends View {

    private static final String TAG = "MarqueeTextView";

    // 间隔字符
    private String mMarginString = "     ";
    private int mTextSize;
    private int mTextColor;
    private TextPaint mTextPaint;
    private String mText;
    private String mShowText;   //最终显示的字符串
    private int mTextWidth = 0, mTextHeight = 0;
    private int mCurrentX = 0;  //当前位置

    private float mBottomFont;

    private int mWidgetWidth;   // 控件宽度

    private int mDistance = 0;    //左位移
    private int mDx = 5;    // 每次向左位移的距离

    private boolean isScrolling = true;

    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mTextSize = ContextExtensionKt.dip2px(getContext(), 12);    // 默认为 12dp
        mTextPaint = new TextPaint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);     // 抗锯齿
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStrokeWidth(0.5f);
        mTextPaint.setFakeBoldText(true);   // 设置为粗体
        mTextPaint.setShadowLayer(4.0f, 0f, 0f, Color.parseColor("#66000000")); //添加阴影
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidgetWidth = MeasureSpec.getSize(widthMeasureSpec);
        expandText();   // 对默认文字进行扩展
        measureText();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        // 可显示内容的宽高
        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        if (TextUtils.isEmpty(mText)) { // 字符串为空，不进行控制
            return;
        }

        mDistance -= mDx;

        // TODO: 2018/7/30 未完成，需要重新规划，以及滚动的时候会有卡顿，原因是什么？

        // 这里需要计算左移的距离
        canvas.drawText(mShowText, paddingLeft + mDistance, contentHeight - mBottomFont, mTextPaint);
        if (isScrolling) {
            postInvalidateDelayed(16);   // 根据速度调整刷新的间隔时间
        }
    }

    // 测量当前 Text
    private void measureText() {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mBottomFont = fontMetrics.bottom;
    }

    // 根据文本的宽度和控件的宽度扩展文本
    private void expandText() {
        String tempString = mText;
        mTextWidth = (int) mTextPaint.measureText(tempString);  // 文本的宽度
        while (mTextWidth <= mWidgetWidth) {  // 如果文本宽度小于控件宽度，那么就进行扩展
            tempString = tempString + (mMarginString + mText);     // 将两个文本拼接成一个新的字符串
            mTextWidth = (int) mTextPaint.measureText(tempString); // 对拼接后的字符串进行测量宽度
        }
        mShowText = tempString;     // 要显示的字符串
    }

    public void setText(String text) {
        this.mText = text;
        // 要进行重新绘制
        expandText();   // 对文字重新进行扩展
        measureText();
        invalidate();
    }

    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
        configTextPaint();
    }

    private void configTextPaint() {
        mTextPaint.setTextSize(mTextSize);
    }

    public void reset() {
        mCurrentX = 0;
    }

    public void startScroll() {
        isScrolling = true;
    }

    public void endScroll() {
        isScrolling = false;
    }
}
