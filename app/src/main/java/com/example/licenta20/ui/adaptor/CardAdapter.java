package com.example.licenta20.ui.adaptor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.ui.home.CardItem;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<CardItem> items;
    private OnItemClickListener listener;

    // AM MODIFICAT AICI: Acum avem două acțiuni distincte
    public interface OnItemClickListener {
        void onActionClick(CardItem item); // Când apasă pe butonul mic de Start
        void onCardClick(CardItem item);   // Când apasă pe tot restul cardului
    }

    public CardAdapter(List<CardItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_opal_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.tvActionText.setText(item.getActionText());

        holder.ivBackground.setBackgroundColor(holder.itemView.getContext().getResources().getColor(item.getColorResId(), null));

        // 1. Click pe butonul de acțiune ("Start")
        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) listener.onActionClick(item);
        });

        // 2. Click pe restul cardului
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCardClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvActionText;
        ImageView ivBackground;
        MaterialCardView btnAction;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCardTitle);
            tvSubtitle = itemView.findViewById(R.id.tvCardSubtitle);
            tvActionText = itemView.findViewById(R.id.tvCardActionText);
            ivBackground = itemView.findViewById(R.id.ivCardBackground);
            btnAction = itemView.findViewById(R.id.btnCardAction);
        }
    }
}