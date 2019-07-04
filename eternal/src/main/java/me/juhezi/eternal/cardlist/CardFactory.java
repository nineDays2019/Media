package me.juhezi.eternal.cardlist;

import android.content.Context;
import me.juhezi.eternal.cardlist.model.CardInfo;
import org.json.JSONObject;

public class CardFactory {

    private static class Holder {
        private static CardFactory sInstance = new CardFactory();
    }

    public static CardFactory getInstance() {
        return Holder.sInstance;
    }

    public CardInfo getCardInfo(JSONObject data, int type) {
        return null;
    }

    public BaseCardView getCardView(Context context, int type) {

        BaseCardView v = null;

        switch (type) {
            case 1:
//                v = new CardTwoPicView(context);
                break;
            default:
//                v = new CardTestView(context);
        }
        return v;
    }

}
