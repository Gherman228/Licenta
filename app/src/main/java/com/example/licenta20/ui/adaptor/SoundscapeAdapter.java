package com.example.licenta20.ui.adaptor; // Ajustează pachetul dacă e necesar

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;

import java.util.List;

public class SoundscapeAdapter extends RecyclerView.Adapter<SoundscapeAdapter.SoundViewHolder> {

    private List<String> soundList;

    public SoundscapeAdapter(List<String> soundList) {
        this.soundList = soundList;
    }

    @NonNull
    @Override
    public SoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_soundscape, parent, false);
        return new SoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundViewHolder holder, int position) {
        String soundTitle = soundList.get(position);
        holder.tvTitle.setText(soundTitle);
    }

    @Override
    public int getItemCount() {
        return soundList.size();
    }

    static class SoundViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public SoundViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSoundTitle);
        }
    }
}