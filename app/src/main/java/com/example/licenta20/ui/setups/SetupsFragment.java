package com.example.licenta20.ui.setups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.data.AppDatabase;
import com.example.licenta20.data.BlockSetup;
import com.example.licenta20.ui.adaptor.BlockAdapter;

public class SetupsFragment extends Fragment {

    private AppDatabase db;
    private BlockAdapter adapter;

    public SetupsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setups, container, false);

        // 1. Inițializăm baza de date și Adapterul
        db = AppDatabase.getInstance(requireContext());
        adapter = new BlockAdapter();

        // 2. Setăm RecyclerView-ul
        RecyclerView recyclerView = view.findViewById(R.id.rvSetups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 3. (Opțional) Inserăm date de test ca să vedem UI-ul
        insertDummyDataIfNeeded();

        // 4. Observăm baza de date: cerem doar rutinele de tip FOCUS
        db.blockSetupDao().getBlocksByCategory("FOCUS").observe(getViewLifecycleOwner(), blocks -> {
            // Când baza de date se schimbă, lista de pe ecran se actualizează INSTANT!
            adapter.setBlocks(blocks);
        });

        return view;
    }

    // Funcție temporară pentru a vedea cum arată design-ul tău
    private void insertDummyDataIfNeeded() {
        new Thread(() -> {
            // Dacă nu avem niciun block de Focus, le creăm noi acum
            if (db.blockSetupDao().getAllBlocks().getValue() == null || db.blockSetupDao().getAllBlocks().getValue().isEmpty()) {
                BlockSetup b1 = new BlockSetup("Laser Focus", "Your daily focus hour", "2:00 PM - 3:00 PM", "Weekdays", "FOCUS", 0);
                BlockSetup b2 = new BlockSetup("Morning Deep Work", "Start the day strong without distractions", "8:00 AM - 11:00 AM", "Daily", "FOCUS", 0);

                db.blockSetupDao().insert(b1);
                db.blockSetupDao().insert(b2);
            }
        }).start();
    }
}