package com.example.licenta20.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// AICI AM MODIFICAT: am adăugat DailyStat.class și version = 3
@Database(entities = {AppConfig.class, DailyStat.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract AppDao appDao();
    public abstract DailyStatDao dailyStatDao(); // Am adăugat DAO-ul nou

    // Singleton pattern
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "kairos_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Păstrat așa cum îl aveai tu pentru testare
                    .build();
        }
        return instance;
    }
}