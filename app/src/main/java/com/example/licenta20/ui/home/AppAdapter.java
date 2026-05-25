package com.example.licenta20.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
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

        // --- PARTEA NOUĂ PENTRU AFIȘARE ---
        if (app.getInterceptCount() > 0) {
            holder.tvInterceptCount.setVisibility(View.VISIBLE);
            holder.tvInterceptCount.setText("Blocat de: " + app.getInterceptCount() + " ori");
        } else {
            holder.tvInterceptCount.setVisibility(View.GONE);
        }
        // ----------------------------------

        holder.itemView.setOnClickListener(v -> {
            app.setSelected(!app.isSelected());
            notifyItemChanged(position); // Actualizăm rândul ca să se bifeze/debifeze

            new Thread(() -> {
                // Aici trebuie să păstrăm și numărul de interceptări când salvăm o bifă nouă
                AppConfig config = new AppConfig(app.getPackageName(), app.isSelected());
                config.setInterceptCount(app.getInterceptCount()); // Salvăm numărul ca să nu se piardă
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
        TextView tvInterceptCount;
        CheckBox cbSelect;
    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
        tvAppName = itemView.findViewById(R.id.tvAppName);
        cbSelect = itemView.findViewById(R.id.cbSelect);
        tvInterceptCount = itemView.findViewById(R.id.tvInterceptCount);

      }
   }
}

