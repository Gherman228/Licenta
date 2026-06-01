package com.example.licenta20;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.licenta20.data.AppDatabase;
import com.example.licenta20.ui.home.HomeFragment;
import com.example.licenta20.ui.setups.SetupsFragment;
import com.example.licenta20.ui.sleep.SleepFragment;
import com.example.licenta20.ui.ai.AIFragment;
import com.example.licenta20.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        // ȘTERGE această linie DUPĂ ce rulezi aplicația o dată!
        AppDatabase.getInstance(this).blockSetupDao().deleteAll();

        // 1. Setăm ecranul de pornire să fie HomeFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        // 2. Logica pentru schimbarea fragmentelor la click pe meniu
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_setups) {
                selectedFragment = new SetupsFragment();
            } else if (id == R.id.nav_sleep) {
                selectedFragment = new SleepFragment();
            } else if (id == R.id.nav_ai) {
                selectedFragment = new AIFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });
    }
}