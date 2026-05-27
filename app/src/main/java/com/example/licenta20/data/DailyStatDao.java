package com.example.licenta20.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DailyStatDao {

    // Inserăm o zi nouă sau o suprascriem dacă există deja
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(DailyStat dailyStat);

    // Luăm datele pentru o zi anume (pentru matematica de pe Home)
    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    DailyStat getStatForDate(String date);

    // Luăm tot istoricul (pentru graficul din Profil)
    @Query("SELECT * FROM daily_stats ORDER BY date ASC")
    List<DailyStat> getAllStats();
}