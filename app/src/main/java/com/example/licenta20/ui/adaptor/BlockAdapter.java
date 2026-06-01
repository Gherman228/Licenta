package com.example.licenta20.ui.adaptor; // Ajustează pachetul dacă îl pui altundeva

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.data.BlockSetup;

import java.util.ArrayList;
import java.util.List;

public class BlockAdapter extends RecyclerView.Adapter<BlockAdapter.BlockViewHolder> {

    private List<BlockSetup> blockList = new ArrayList<>();

    // Această metodă actualizează lista automat când se schimbă datele în baza de date
    public void setBlocks(List<BlockSetup> blocks) {
        this.blockList = blocks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_block, parent, false);
        return new BlockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockViewHolder holder, int position) {
        BlockSetup currentBlock = blockList.get(position);

        holder.tvTitle.setText(currentBlock.getTitle());
        holder.tvDescription.setText(currentBlock.getDescription());
        holder.tvTime.setText(currentBlock.getTimeRange());

        // Aici vom putea adăuga logica de Editare mai târziu
        holder.btnEdit.setOnClickListener(v -> {
            // TODO: Deschide ecranul de editare
        });
    }

    @Override
    public int getItemCount() {
        return blockList.size();
    }

    // Clasa internă care "ține" elementele vizuale din item_block.xml
    class BlockViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvTime;
        private Button btnEdit;

        public BlockViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBlockTitle);
            tvDescription = itemView.findViewById(R.id.tvBlockDescription);
            tvTime = itemView.findViewById(R.id.tvBlockTime);
            btnEdit = itemView.findViewById(R.id.btnEditBlock);
        }
    }
}