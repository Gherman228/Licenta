package com.example.licenta20.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BlockSetupDao {

    @Insert
    void insert(BlockSetup blockSetup);

    @Update
    void update(BlockSetup blockSetup);

    @Delete
    void delete(BlockSetup blockSetup);

    // Asta e metoda magică: ne dă doar blocurile pentru Setups sau doar pentru Sleep
    @Query("SELECT * FROM block_setups WHERE category = :category")
    LiveData<List<BlockSetup>> getBlocksByCategory(String category);

    // În caz că vrem să le vedem pe toate la un loc
    @Query("SELECT * FROM block_setups")
    LiveData<List<BlockSetup>> getAllBlocks();

    @Query("DELETE FROM block_setups")
    void deleteAll();
}