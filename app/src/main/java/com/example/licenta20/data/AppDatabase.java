package com.example.licenta20.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// AICI AM MODIFICAT version = 2
@Database(entities = {AppConfig.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract AppDao appDao();

    // Singleton pattern
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "kairos_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Pentru testare e OK momentan
                    .build();
        }
        return instance;
    }
}