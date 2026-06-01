package com.example.licenta20.ui.setups;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.ui.adaptor.CardAdapter;
import com.example.licenta20.ui.home.CardItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class SetupsFragment extends Fragment {

    public SetupsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setups, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvSetups);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<CardItem> cardItems = new ArrayList<>();
        cardItems.add(new CardItem("Laser Focus", "Your daily focus hour", "▶ Start", android.R.color.holo_blue_dark));
        cardItems.add(new CardItem("Morning Deep Work", "Start the day strong", "▶ Start", android.R.color.holo_purple));
        cardItems.add(new CardItem("Study Sprint", "No distractions", "▶ Start", android.R.color.holo_orange_dark));
        cardItems.add(new CardItem("Reading Time", "Unplug and read", "▶ Start", android.R.color.holo_green_dark));

        // Folosim interfața actualizată cu cele 2 metode
        CardAdapter adapter = new CardAdapter(cardItems, new CardAdapter.OnItemClickListener() {

            @Override
            public void onActionClick(CardItem item) {
                // LOGICA DE START (Teleportarea spre Home pe care am făcut-o anterior)
                int minutesToSet = 30;
                if (item.getTitle().equals("Laser Focus")) minutesToSet = 60;
                else if (item.getTitle().equals("Morning Deep Work")) minutesToSet = 120;
                else if (item.getTitle().equals("Study Sprint")) minutesToSet = 45;
                else if (item.getTitle().equals("Reading Time")) minutesToSet = 60;

                SharedPreferences prefs = requireContext().getSharedPreferences("KairosPrefs", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("autoStartTimer", true).putInt("autoStartMinutes", minutesToSet).apply();

                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation); // Verifică id-ul navigației
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                }
            }

            @Override
            public void onCardClick(CardItem item) {
                // LOGICA DE EDITARE: Când apasă pe card, deschidem BottomSheet-ul!
                openSetupConfigBottomSheet(item);
            }
        });

        recyclerView.setAdapter(adapter);
        return view;
    }

    // ==========================================
    // METODA CARE AFIȘEAZĂ FEREASTRA DE CONFIGURARE
    // ==========================================
    private void openSetupConfigBottomSheet(CardItem item) {
        // 1. Creăm dialogul
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        // 2. Legăm dialogul de fișierul XML pe care l-ai creat
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_setup_config, null);
        bottomSheetDialog.setContentView(sheetView);

        // Fundal transparent obligatoriu pentru ca acele colțuri rotunde din drawable să se vadă corect
        ((View) sheetView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent, null));

        // 3. Modificăm elementele din fereastră în funcție de cardul apăsat
        TextView tvSetupName = sheetView.findViewById(R.id.tvSetupName);
        if (tvSetupName != null) {
            tvSetupName.setText(item.getTitle()); // Titlul devine "Laser Focus" etc.
        }

        // 4. Afișăm pe ecran
        bottomSheetDialog.show();
    }
}