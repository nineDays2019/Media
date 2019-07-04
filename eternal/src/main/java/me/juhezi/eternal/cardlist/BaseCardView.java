package me.juhezi.eternal.cardlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import me.juhezi.eternal.R;
import me.juhezi.eternal.cardlist.model.CardInfo;

public abstract class BaseCardView extends FrameLayout {

    protected final int VCARDID = 1;

    protected View vCard;
    protected LayoutParams mCardLp;

    protected CardInfo mCardInfo;   // 显示的card数据
    protected boolean isInit = true;
    private int mCardMarginLeft;
    private int mCardMarginRight;

    public BaseCardView(Context context) {
        this(context, null);
    }

    public BaseCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BaseCardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
    }

    public void update(CardInfo cardInfo, int position) {
        setCardInfo(cardInfo);
        if (isInit) {
            init();
        }
    }

    public void setCardInfo(CardInfo cardInfo) {
        mCardInfo = cardInfo;
    }

    private void init() {
        initMarginValues();
        initCardView();
        isInit = false;
    }

    protected void initCardView() {
        vCard = initLayout();
        vCard.setId(VCARDID);
        mCardLp = initCardLayoutParams();

        mCardLp.leftMargin = mCardMarginLeft;
        mCardLp.rightMargin = mCardMarginRight;
        if (vCard.getLayoutParams() == null) {
            vCard.setLayoutParams(mCardLp);
        }
        addView(vCard);
    }

    protected LayoutParams initCardLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
    }

    protected void initMarginValues() {
        mCardMarginLeft = getResources().getDimensionPixelSize(R.dimen.card_normal_margin_left);
        mCardMarginRight = getResources().getDimensionPixelSize(R.dimen.card_normal_margin_right);
    }

    abstract protected View initLayout();

}
