package com.example.licenta20.ui.setups;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.data.AppDatabase;
import com.example.licenta20.data.BlockSetup;
import com.example.licenta20.ui.adaptor.CardAdapter;
import com.example.licenta20.ui.home.AppInfo;
import com.example.licenta20.ui.home.CardItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SetupsFragment extends Fragment {

    private AppDatabase db;
    private List<CardItem> cardItems = new ArrayList<>();

    public SetupsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setups, container, false);
        db = AppDatabase.getInstance(requireContext());

        RecyclerView recyclerView = view.findViewById(R.id.rvSetups);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        cardItems = new ArrayList<>();

        CardAdapter adapter = new CardAdapter(cardItems, new CardAdapter.OnItemClickListener() {
            @Override
            public void onActionClick(CardItem item) {
                if (item.getId() == -1) {
                    openSetupConfigBottomSheet(item);
                    return;
                }

                SharedPreferences prefs = requireContext().getSharedPreferences("KairosPrefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("autoStartTimer", true)
                        .putInt("autoStartMinutes", 60)
                        .putString("activeSetupBlockedApps", item.getBlockedApps())
                        .apply();

                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                }
            }

            @Override
            public void onCardClick(CardItem item) {
                openSetupConfigBottomSheet(item);
            }
        });
        recyclerView.setAdapter(adapter);

        db.blockSetupDao().getBlocksByCategory("FOCUS").observe(getViewLifecycleOwner(), blocks -> {
            cardItems.clear();
            int[] colors = {android.R.color.holo_blue_dark, android.R.color.holo_purple, android.R.color.holo_orange_dark, android.R.color.holo_green_dark};
            int colorIndex = 0;

            if (blocks != null && !blocks.isEmpty()) {
                for (BlockSetup block : blocks) {
                    int cardColor = colors[colorIndex % colors.length];
                    colorIndex++;

                    String subtitle = block.getDaysActive() + "\n" + block.getTimeRange();

                    // Adăugăm un mic text dacă e pe pauză
                    if (block.isPaused()) {
                        subtitle = "⏸ PAUSED\n" + subtitle;
                    }

                    CardItem existingCard = new CardItem(block.getTitle(), subtitle, "▶ Start", cardColor);
                    existingCard.setId(block.getId());
                    existingCard.setBlockedApps(block.getBlockedApps());
                    existingCard.setPaused(block.isPaused()); // Preluăm pauza
                    cardItems.add(existingCard);
                }
            }

            CardItem createCard = new CardItem("Nou Setup", "Apasă pentru a crea", "▶ Creează", android.R.color.darker_gray);
            createCard.setId(-1);
            cardItems.add(createCard);

            adapter.notifyDataSetChanged();
        });

        return view;
    }

    private void openSetupConfigBottomSheet(CardItem item) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_setup_config, null);
        bottomSheetDialog.setContentView(sheetView);
        ((View) sheetView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent, null));

        List<String> tempBlockedApps = new ArrayList<>();
        if (item.getBlockedApps() != null && !item.getBlockedApps().isEmpty()) {
            tempBlockedApps.addAll(Arrays.asList(item.getBlockedApps().split(",")));
        }

        TextView tvSetupName = sheetView.findViewById(R.id.tvSetupName);
        if (tvSetupName != null) {
            tvSetupName.setText(item.getTitle());
            tvSetupName.setOnClickListener(v -> {
                android.widget.EditText input = new android.widget.EditText(requireContext());
                input.setText(tvSetupName.getText().toString());
                input.setSelectAllOnFocus(true);

                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Redenumește Rutina")
                        .setView(input)
                        .setPositiveButton("Salvează", (dialog, which) -> tvSetupName.setText(input.getText().toString().trim()))
                        .setNegativeButton("Anulează", null)
                        .show();
            });
        }

        View containerSchedule = sheetView.findViewById(R.id.containerSchedule);
        View containerTimer = sheetView.findViewById(R.id.containerTimer);
        if (containerSchedule != null) containerSchedule.setVisibility(View.VISIBLE);
        if (containerTimer != null) containerTimer.setVisibility(View.GONE);

        TextView btnTabSchedule = sheetView.findViewById(R.id.btnTabSchedule);
        TextView btnTabTimer = sheetView.findViewById(R.id.btnTabTimer);

        if (btnTabSchedule != null && btnTabTimer != null) {
            btnTabSchedule.setOnClickListener(v -> {
                btnTabSchedule.setBackgroundColor(android.graphics.Color.parseColor("#4DFFFFFF"));
                btnTabSchedule.setTextColor(android.graphics.Color.WHITE);
                btnTabTimer.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                btnTabTimer.setTextColor(android.graphics.Color.parseColor("#8A8FA8"));
                if (containerSchedule != null) containerSchedule.setVisibility(View.VISIBLE);
                if (containerTimer != null) containerTimer.setVisibility(View.GONE);
            });

            btnTabTimer.setOnClickListener(v -> {
                btnTabTimer.setBackgroundColor(android.graphics.Color.parseColor("#4DFFFFFF"));
                btnTabTimer.setTextColor(android.graphics.Color.WHITE);
                btnTabSchedule.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                btnTabSchedule.setTextColor(android.graphics.Color.parseColor("#8A8FA8"));
                if (containerSchedule != null) containerSchedule.setVisibility(View.GONE);
                if (containerTimer != null) containerTimer.setVisibility(View.VISIBLE);
            });
        }

        TextView tvTimerDuration = sheetView.findViewById(R.id.tvTimerDuration);
        if (tvTimerDuration != null) {
            tvTimerDuration.setOnClickListener(v -> {
                String[] options = {"15m", "30m", "45m", "60m", "90m", "120m"};
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Alege Durata Timer-ului")
                        .setItems(options, (dialog, which) -> tvTimerDuration.setText(options[which] + " >"))
                        .show();
            });
        }

        View btnOpenBlockList = sheetView.findViewById(R.id.btnOpenBlockList);
        if (btnOpenBlockList != null) {
            btnOpenBlockList.setOnClickListener(v -> {
                openAppsBottomSheetForSetup(tempBlockedApps);
            });
        }

        // Citim butonul de PAUZĂ
        SwitchCompat switchVacation = sheetView.findViewById(R.id.switchVacation);
        if (switchVacation != null) {
            switchVacation.setChecked(item.isPaused());
        }

        View layoutTimeFrom = sheetView.findViewById(R.id.layoutTimeFrom);
        View layoutTimeTo = sheetView.findViewById(R.id.layoutTimeTo);
        TextView tvTimeFrom = sheetView.findViewById(R.id.tvTimeFrom);
        TextView tvTimeTo = sheetView.findViewById(R.id.tvTimeTo);

        String currentFromTime = "9:00 AM";
        String currentToTime = "5:00 PM";
        List<String> activeDays = new ArrayList<>();

        if (item.getId() != -1 && item.getSubtitle() != null) {
            // Curățăm textul dacă are mesajul de pauză pe el
            String cleanSubtitle = item.getSubtitle().replace("⏸ PAUSED\n", "");
            String[] parts = cleanSubtitle.split("\n");
            if (parts.length == 2) {
                activeDays.addAll(Arrays.asList(parts[0].split(" ")));
                String[] timeArray = parts[1].split(" - ");
                if (timeArray.length == 2) { currentFromTime = timeArray[0]; currentToTime = timeArray[1]; }
            }
        } else {
            activeDays.addAll(Arrays.asList("M", "T", "W", "Th", "F"));
        }

        if (tvTimeFrom != null) tvTimeFrom.setText(currentFromTime);
        if (tvTimeTo != null) tvTimeTo.setText(currentToTime);

        if (layoutTimeFrom != null && tvTimeFrom != null) {
            layoutTimeFrom.setOnClickListener(v -> {
                new android.app.TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> tvTimeFrom.setText(formatTime(hourOfDay, minute)), 9, 0, false).show();
            });
        }

        if (layoutTimeTo != null && tvTimeTo != null) {
            layoutTimeTo.setOnClickListener(v -> {
                new android.app.TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> tvTimeTo.setText(formatTime(hourOfDay, minute)), 17, 0, false).show();
            });
        }

        int[] dayIds = {R.id.dayM, R.id.dayT, R.id.dayW, R.id.dayTh, R.id.dayF, R.id.dayS, R.id.daySu};
        String[] dayLetters = {"M", "T", "W", "Th", "F", "S", "Su"};

        for (int i = 0; i < dayIds.length; i++) {
            TextView dayView = sheetView.findViewById(dayIds[i]);
            if (dayView != null) {
                boolean isSelectedInitial = activeDays.contains(dayLetters[i]);
                dayView.setTag(isSelectedInitial);
                if (isSelectedInitial) {
                    dayView.setBackgroundResource(R.drawable.circle_day);
                    dayView.setTextColor(android.graphics.Color.WHITE);
                } else {
                    dayView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    dayView.setTextColor(android.graphics.Color.parseColor("#8A8FA8"));
                }
                dayView.setOnClickListener(v -> {
                    boolean currentlySelected = (boolean) v.getTag();
                    if (currentlySelected) {
                        v.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                        ((TextView) v).setTextColor(android.graphics.Color.parseColor("#8A8FA8"));
                        v.setTag(false);
                    } else {
                        v.setBackgroundResource(R.drawable.circle_day);
                        ((TextView) v).setTextColor(android.graphics.Color.WHITE);
                        v.setTag(true);
                    }
                });
            }
        }

        TextView btnDeleteSetup = sheetView.findViewById(R.id.btnDeleteSetup);
        if (btnDeleteSetup != null) {
            if (item.getId() == -1) {
                btnDeleteSetup.setVisibility(View.GONE);
            } else {
                btnDeleteSetup.setVisibility(View.VISIBLE);
                btnDeleteSetup.setOnClickListener(v -> {
                    new Thread(() -> {
                        BlockSetup toDelete = new BlockSetup(item.getTitle(), item.getSubtitle(), "", "", "FOCUS", 0);
                        toDelete.setId(item.getId());
                        db.blockSetupDao().delete(toDelete);
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Rutina a fost ștearsă!", Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            });
                        }
                    }).start();
                });
            }
        }

        View btnSaveSetup = sheetView.findViewById(R.id.btnSaveSetup);
        if (btnSaveSetup != null) {
            btnSaveSetup.setOnClickListener(v -> {
                String finalInterval = tvTimeFrom.getText().toString() + " - " + tvTimeTo.getText().toString();
                StringBuilder selectedDays = new StringBuilder();
                if (sheetView.findViewById(R.id.dayM).getTag() != null && (boolean) sheetView.findViewById(R.id.dayM).getTag()) selectedDays.append("M ");
                if (sheetView.findViewById(R.id.dayT).getTag() != null && (boolean) sheetView.findViewById(R.id.dayT).getTag()) selectedDays.append("T ");
                if (sheetView.findViewById(R.id.dayW).getTag() != null && (boolean) sheetView.findViewById(R.id.dayW).getTag()) selectedDays.append("W ");
                if (sheetView.findViewById(R.id.dayTh).getTag() != null && (boolean) sheetView.findViewById(R.id.dayTh).getTag()) selectedDays.append("Th ");
                if (sheetView.findViewById(R.id.dayF).getTag() != null && (boolean) sheetView.findViewById(R.id.dayF).getTag()) selectedDays.append("F ");
                if (sheetView.findViewById(R.id.dayS).getTag() != null && (boolean) sheetView.findViewById(R.id.dayS).getTag()) selectedDays.append("S ");
                if (sheetView.findViewById(R.id.daySu).getTag() != null && (boolean) sheetView.findViewById(R.id.daySu).getTag()) selectedDays.append("Su ");

                String tempDays = selectedDays.toString().trim();
                final String finalDaysString = tempDays.isEmpty() ? "Only Once" : tempDays;

                String blockedAppsString = TextUtils.join(",", tempBlockedApps);

                new Thread(() -> {
                    String numeFinal = tvSetupName.getText().toString().trim();
                    boolean pauzaActiva = switchVacation != null && switchVacation.isChecked();

                    if (item.getId() == -1) {
                        BlockSetup newSetup = new BlockSetup(numeFinal, "Setare personalizata", finalInterval, finalDaysString, "FOCUS", 0);
                        newSetup.setBlockedApps(blockedAppsString);
                        newSetup.setPaused(pauzaActiva);
                        db.blockSetupDao().insert(newSetup);
                    } else {
                        BlockSetup updatedSetup = new BlockSetup(numeFinal, item.getSubtitle().replace("⏸ PAUSED\n", ""), finalInterval, finalDaysString, "FOCUS", 0);
                        updatedSetup.setId(item.getId());
                        updatedSetup.setBlockedApps(blockedAppsString);
                        updatedSetup.setPaused(pauzaActiva);
                        db.blockSetupDao().update(updatedSetup);
                    }

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Orar salvat cu succes!", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        });
                    }
                }).start();
            });
        }
        bottomSheetDialog.show();
    }

    // AICI E REPARATĂ ORA PENTRU ROMÂNIA (Locale.US)
    private String formatTime(int hour, int minute) {
        String amPm = (hour >= 12) ? "PM" : "AM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm);
    }

    private void openAppsBottomSheetForSetup(List<String> currentSetupBlockedApps) {
        BottomSheetDialog appSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_apps, null);
        appSheetDialog.setContentView(sheetView);
        RecyclerView rvBottomSheetApps = sheetView.findViewById(R.id.rvBottomSheetApps);
        rvBottomSheetApps.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));

        new Thread(() -> {
            android.content.pm.PackageManager pm = requireContext().getPackageManager();
            android.content.Intent mainIntent = new android.content.Intent(android.content.Intent.ACTION_MAIN, null);
            mainIntent.addCategory(android.content.Intent.CATEGORY_LAUNCHER);
            List<android.content.pm.ResolveInfo> resolvedInfos = pm.queryIntentActivities(mainIntent, 0);

            List<AppInfo> appList = new ArrayList<>();
            for (android.content.pm.ResolveInfo resolveInfo : resolvedInfos) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (packageName.equals(requireContext().getPackageName())) continue;

                boolean alreadyAdded = false;
                for (AppInfo addedApp : appList) {
                    if (addedApp.getPackageName().equals(packageName)) { alreadyAdded = true; break; }
                }
                if (alreadyAdded) continue;

                String name = resolveInfo.loadLabel(pm).toString();
                android.graphics.drawable.Drawable icon = resolveInfo.loadIcon(pm);
                AppInfo appInfo = new AppInfo(name, packageName, icon);
                appList.add(appInfo);
            }
            java.util.Collections.sort(appList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    SetupInnerAppAdapter adapter = new SetupInnerAppAdapter(appList, currentSetupBlockedApps);
                    rvBottomSheetApps.setAdapter(adapter);
                });
            }
        }).start();

        appSheetDialog.show();
    }

    private class SetupInnerAppAdapter extends RecyclerView.Adapter<SetupInnerAppAdapter.ViewHolder> {
        private List<AppInfo> apps;
        private List<String> temporarySelection;

        public SetupInnerAppAdapter(List<AppInfo> apps, List<String> temporarySelection) {
            this.apps = apps;
            this.temporarySelection = temporarySelection;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppInfo app = apps.get(position);

            TextView tvName = holder.itemView.findViewById(R.id.tvAppName);
            ImageView imgIcon = holder.itemView.findViewById(R.id.ivAppIcon);
            android.widget.CheckBox cbSelect = holder.itemView.findViewById(R.id.cbSelect);

            if (tvName != null) tvName.setText(app.getName());
            if (imgIcon != null) imgIcon.setImageDrawable(app.getIcon());

            if (cbSelect != null) {
                cbSelect.setOnCheckedChangeListener(null);
                cbSelect.setChecked(temporarySelection.contains(app.getPackageName()));

                cbSelect.setOnCheckedChangeListener((btn, isChecked) -> {
                    if (isChecked) {
                        if (!temporarySelection.contains(app.getPackageName())) {
                            temporarySelection.add(app.getPackageName());
                        }
                    } else {
                        temporarySelection.remove(app.getPackageName());
                    }
                });
            }
        }

        @Override
        public int getItemCount() { return apps.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View itemView) { super(itemView); }
        }
    }
}