package me.juhezi.eternal.widget.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

public class EternalDragFloatActionButton extends FloatingActionButton {

    private int mParentHeight;
    private int mParentWidth;
    private int mLastX;
    private int mLastY;
    private boolean isDrag;

    int marginTop = 0;
    int marginBottom = 0;
    int marginStart = 0;
    int marginEnd = 0;

    public EternalDragFloatActionButton(Context context) {
        this(context, null);
    }

    public EternalDragFloatActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EternalDragFloatActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        if (getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
            marginTop = lp.topMargin;
            marginBottom = lp.bottomMargin;
            marginEnd = lp.rightMargin;
            marginStart = lp.leftMargin;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);   // 设置为按下状态
                isDrag = false;
                mLastX = rawX;
                mLastY = rawY;
                ViewGroup parent;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    parent = (ViewGroup) getParent();
                    mParentHeight = parent.getHeight();
                    mParentWidth = parent.getWidth();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mParentHeight <= 0 || mParentWidth == 0) {
                    isDrag = false;
                    break;
                } else {
                    isDrag = true;
                }
                int dx = rawX - mLastX;
                int dy = rawY - mLastY;
                //这里修复一些华为手机无法触发点击事件
                int distance = (int) Math.sqrt(dx * dx + dy * dy);
                if (distance <= 10) {
                    isDrag = false;
                    break;
                }
                float x = getX() + dx;
                float y = getY() + dy;
                // 边缘检测
                x = x < marginStart ? marginStart :
                        x > mParentWidth - getWidth() - marginEnd ?
                                mParentWidth - getWidth() - marginEnd : x;
                y = getY() < marginTop ? marginTop :
                        getY() + getHeight() > mParentHeight - marginBottom ?
                                mParentHeight - getHeight() - marginBottom : y;
                setX(x);
                setY(y);
                mLastX = rawX;
                mLastY = rawY;
                break;
            case MotionEvent.ACTION_UP:
                if (!notDrag()) {
                    setPressed(false);
                    if (rawX >= mParentWidth / 2) {
                        //靠右吸附
                        ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(), mParentWidth - getWidth() - marginEnd);
                        oa.setInterpolator(new DecelerateInterpolator());
                        oa.setDuration(500);
                        oa.start();
                    } else {
                        //靠左吸附
                        ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(), marginStart);
                        oa.setInterpolator(new DecelerateInterpolator());
                        oa.setDuration(500);
                        oa.start();
                    }
                }
                break;
        }
        // 如果是拖拽，则消耗事件，否则正常传递
        return !notDrag() || super.onTouchEvent(event);
    }

    private boolean notDrag() {
        return !isDrag &&
                (getX() == marginStart ||
                        (getX() == mParentWidth - getWidth() - marginBottom));
    }

}
