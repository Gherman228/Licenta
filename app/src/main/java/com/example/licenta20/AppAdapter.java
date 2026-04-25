package com.example.licenta20;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.data.AppConfig;
import com.example.licenta20.data.AppDatabase;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private List<AppInfo> appList;
    private AppDatabase db; // Referință către DB

    // Modificăm constructorul
    public AppAdapter(List<AppInfo> appList, AppDatabase db) {
        this.appList = appList;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = appList.get(position);


        holder.tvAppName.setText(app.getName());
        holder.ivAppIcon.setImageDrawable(app.getIcon());
        holder.cbSelect.setChecked(app.isSelected());

        holder.itemView.setOnClickListener(v -> {
            app.setSelected(!app.isSelected());
            notifyItemChanged(position);

            new Thread(() -> {
                AppConfig config = new AppConfig(app.getPackageName(), app.isSelected());
                db.appDao().insertOrUpdate(config);
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAppIcon;
        TextView tvAppName;
        CheckBox cbSelect;
    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
        tvAppName = itemView.findViewById(R.id.tvAppName);
        cbSelect = itemView.findViewById(R.id.cbSelect);

      }
   }
}

