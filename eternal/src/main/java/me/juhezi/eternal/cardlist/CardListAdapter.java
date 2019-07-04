package me.juhezi.eternal.cardlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import me.juhezi.eternal.cardlist.model.CardInfo;

import java.util.ArrayList;
import java.util.List;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardHolder> {

    private List<CardInfo> mData = new ArrayList<>();

    public void setCardList(List<CardInfo> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void appendCardList(List<CardInfo> data) {
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        BaseCardView cardView = CardFactory.getInstance().getCardView(viewGroup.getContext(), viewType);
        return new CardHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull CardHolder holder, int position) {
        holder.bindData(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).card_type;
    }

    class CardHolder extends RecyclerView.ViewHolder {

        CardHolder(@NonNull BaseCardView itemView) {
            super(itemView);
        }

        void bindData(CardInfo cardInfo, int position) {
            if (itemView instanceof BaseCardView) {
                ((BaseCardView) itemView).update(cardInfo, position);
            }
        }

    }

}
