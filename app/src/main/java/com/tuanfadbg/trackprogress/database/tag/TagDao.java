package com.tuanfadbg.trackprogress.database.tag;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TagDao {
    @Query("SELECT * FROM Tag ORDER BY uid DESC")
    List<Tag> getAll();

    @Query("SELECT * FROM Tag ORDER BY uid DESC limit 1")
    List<Tag> getNewestTag();

    @Query("SELECT * FROM Tag WHERE uid IN (:userIds)")
    List<Tag> loadAllByIds(int[] userIds);

    @Update
    void update(Tag... items);

    @Insert
    void insertAll(Tag... users);

    @Delete
    void delete(Tag user);
}