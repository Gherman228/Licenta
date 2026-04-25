package com.example.licenta20.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
    public interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(AppConfig config);

    @Query("SELECT * FROM app_configs")
    List<AppConfig> getAllConfigs();

    @Query("SELECT * FROM app_configs WHERE packageName = :pkgName")
    AppConfig getConfigForApp(String pkgName);
}