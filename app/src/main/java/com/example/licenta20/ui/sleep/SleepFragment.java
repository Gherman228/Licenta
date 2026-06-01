package com.example.licenta20.ui.sleep;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.ui.adaptor.CardAdapter;
import com.example.licenta20.ui.home.CardItem;

import java.util.ArrayList;
import java.util.List;

public class SleepFragment extends Fragment {

    public SleepFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleep, container, false);

        RecyclerView rvSoundscapes = view.findViewById(R.id.rvSoundscapes);

        // Grid cu 2 coloane pentru consistență vizuală în tab-uri
        rvSoundscapes.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Creăm lista de carduri
        List<CardItem> sleepList = new ArrayList<>();
        sleepList.add(new CardItem("City Rain", "Ambient Noise", "▶ Play", android.R.color.darker_gray));
        sleepList.add(new CardItem("Thunderstorm", "Heavy Rain", "▶ Play", android.R.color.holo_blue_dark));
        sleepList.add(new CardItem("Pastures", "Nature Sounds", "▶ Play", android.R.color.holo_green_light));
        sleepList.add(new CardItem("Ocean Waves", "Relaxing Water", "▶ Play", android.R.color.holo_blue_light));
        sleepList.add(new CardItem("Forest Night", "Crickets & Owls", "▶ Play", android.R.color.holo_green_dark));

        // Aplicăm Noul Adaptor Universal cu ambele tipuri de click-uri
        CardAdapter adapter = new CardAdapter(sleepList, new CardAdapter.OnItemClickListener() {
            @Override
            public void onActionClick(CardItem item) {
                // Când apasă pe butonul mic de "▶ Play"
                Toast.makeText(getContext(), "Se redă: " + item.getTitle() + " 🎵", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCardClick(CardItem item) {
                // Când apasă pe corpul cardului în tab-ul Sleep
                Toast.makeText(getContext(), "Opțiuni pentru sunetul: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        rvSoundscapes.setAdapter(adapter);

        return view;
    }
}