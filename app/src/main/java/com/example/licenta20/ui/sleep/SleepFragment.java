package com.example.licenta20.ui.sleep;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.ui.adaptor.SoundscapeAdapter;

import java.util.Arrays;
import java.util.List;

public class SleepFragment extends Fragment {

    public SleepFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleep, container, false);

        // Găsim RecyclerView-ul din noul tău layout
        RecyclerView rvSoundscapes = view.findViewById(R.id.rvSoundscapes);

        // Nu mai e nevoie să setăm LayoutManager aici, pentru că ai fost deștept
        // și l-ai pus deja în XML cu app:layoutManager="..."!

        // Creăm o listă rapidă cu sunete de relaxare
        List<String> dummySounds = Arrays.asList(
                "City Rain",
                "Thunderstorm",
                "Pastures",
                "Ocean Waves",
                "Forest Night"
        );

        // Setăm adapterul pe listă
        SoundscapeAdapter adapter = new SoundscapeAdapter(dummySounds);
        rvSoundscapes.setAdapter(adapter);

        return view;
    }
}