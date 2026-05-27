package com.example.licenta20.ui.profile;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.licenta20.R;
import com.example.licenta20.data.AppDatabase;
import com.example.licenta20.data.DailyStat;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ProfileFragment extends Fragment {

    private BarChart barChart;
    private AppDatabase db;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        barChart = view.findViewById(R.id.barChart);
        db = AppDatabase.getInstance(requireContext());

        setupChartAppearance();
        loadChartData();

        return view;
    }

    private void setupChartAppearance() {
        // Ascundem descrierea și legenda care ocupă spațiu degeaba
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        // Eliminăm marginile și grid-ul pe fundal pentru un look curat
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);

        // Nu permitem zoom-ul dublu ca să nu stricăm vizualizarea
        barChart.setScaleEnabled(false);

        // Configurăm Axa X (Cea de jos, cu zilele)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE); // Text alb
        xAxis.setDrawGridLines(false); // Fără linii verticale
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f); // Distanță de 1 unitate între etichete

        // Configurăm Axa Y (Cea din stânga, cu minutele)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true); // Păstrăm liniile orizontale pentru ghidaj
        leftAxis.setGridColor(Color.parseColor("#333333")); // Linii subțiri și gri închis
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMinimum(0f); // Graficul începe de la 0 minute

        // Ascundem axa din dreapta complet
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void loadChartData() {
        new Thread(() -> {
            // Extragem istoricul ordonat după dată
            List<DailyStat> statsList = db.dailyStatDao().getAllStats();

            List<BarEntry> entries = new ArrayList<>();
            List<String> dateLabels = new ArrayList<>();

            // Formatatoare pentru a schimba "2026-05-27" în "27 Mai" pentru spațiu
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat shortFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

            for (int i = 0; i < statsList.size(); i++) {
                DailyStat stat = statsList.get(i);

                // Transformăm milisecundele în minute
                float focusMinutes = stat.getTotalFocusTime() / (1000f * 60f);

                // Adăugăm bara în grafic la poziția i
                entries.add(new BarEntry(i, focusMinutes));

                // Scurtăm numele datei
                try {
                    Date dateObj = originalFormat.parse(stat.getDate());
                    if (dateObj != null) {
                        dateLabels.add(shortFormat.format(dateObj));
                    } else {
                        dateLabels.add(stat.getDate());
                    }
                } catch (ParseException e) {
                    dateLabels.add(stat.getDate());
                }
            }

            // Dacă avem date, populăm graficul pe Main Thread
            if (!entries.isEmpty() && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    BarDataSet dataSet = new BarDataSet(entries, "Focus Time");
                    dataSet.setColor(Color.WHITE); // Culoarea barelor
                    dataSet.setValueTextColor(Color.WHITE); // Culoarea textului de deasupra barei
                    dataSet.setValueTextSize(12f);

                    // 1. Formatăm valoarea (ex: din 1.07 face "1m")
                    dataSet.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            if (value == 0) return ""; // Nu afisam nimic pt 0
                            return (int) value + "m";
                        }
                    });

                    BarData barData = new BarData(dataSet);
                    // 2. Facem bara mai subțire și elegantă
                    barData.setBarWidth(0.3f);

                    barChart.setData(barData);
                    barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dateLabels));

                    // 3. FIX PENTRU BARA LATĂ: Forțăm graficul să aibă loc pentru 7 zile
                    if (entries.size() < 7) {
                        barChart.getXAxis().setAxisMinimum(-0.5f);
                        barChart.getXAxis().setAxisMaximum(6.5f);
                    } else {
                        // Dacă ai peste 7 zile, îl lăsăm să scroleze normal
                        barChart.getXAxis().resetAxisMinimum();
                        barChart.getXAxis().resetAxisMaximum();
                    }

                    // Desenăm graficul
                    barChart.invalidate();
                });
            }
        }).start();
    }
}