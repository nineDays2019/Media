package me.juhezi.eternal.cardlist.model;

import org.json.JSONObject;

public class CardInfo {

    public int card_type;

    public JSONObject metaData;

    public int getCardType() {
        return card_type;
    }

    public CardInfo setCardType(int type) {
        this.card_type = type;
        return this;
    }

    public static CardInfo createCardInfo(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        int type = jsonObject.optInt("card_type");
        CardInfo cardInfo = new CardInfo();
        return cardInfo.setCardType(type);
    }

}
