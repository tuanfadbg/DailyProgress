package com.tuanfadbg.progress.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Item.class, Tag.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final String ROOM_NAME = "text_scanner_database.db";

    public abstract ItemDao itemDao();
}