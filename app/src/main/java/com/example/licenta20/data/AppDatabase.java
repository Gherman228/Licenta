package com.example.licenta20.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 1. Am adăugat BlockSetup.class în lista de entități
// 2. Am crescut versiunea la 4
@Database(entities = {AppConfig.class, DailyStat.class, BlockSetup.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract AppDao appDao();
    public abstract DailyStatDao dailyStatDao();

    // 3. Am adăugat legătura către noul DAO
    public abstract BlockSetupDao blockSetupDao();

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