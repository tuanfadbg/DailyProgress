package com.tuanfadbg.trackprogress.database.item;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM Item ORDER BY create_at DESC")
    List<Item> getAll();

    @Query("SELECT * FROM Item WHERE tag IN (:tagId) ORDER BY create_at DESC")
    List<Item> getAllByTag(int tagId);

    @Query("SELECT * FROM Item ORDER BY uid DESC limit 1")
    List<Item> getNewest();

    @Query("SELECT * FROM Item WHERE uid IN (:userIds)")
    List<Item> loadAllByIds(int[] userIds);

    @Update
    void update(Item... items);

    @Insert
    void insertAll(Item... items);

    @Delete
    void delete(Item... items);
}