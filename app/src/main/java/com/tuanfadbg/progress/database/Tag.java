package com.tuanfadbg.progress.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "Tag")
public class Tag {

    public Tag(int uid, String name, long createAt) {
        this.uid = uid;
        this.name = name;
        this.createAt = createAt;
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "create_at")
    public long createAt;
}