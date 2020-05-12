package com.tuanfadbg.trackprogress.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.tuanfadbg.trackprogress.database.item.Item;
import com.tuanfadbg.trackprogress.database.item.ItemDao;
import com.tuanfadbg.trackprogress.database.tag.Tag;
import com.tuanfadbg.trackprogress.database.tag.TagDao;

@Database(entities = {Item.class, Tag.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public static final String ROOM_NAME = "text_scanner_database.db";

    public abstract ItemDao itemDao();
    public abstract TagDao tagDao();
}