package com.tuanfadbg.trackprogress.database.tag;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "Tag")
public class Tag {

    public Tag(String name) {
        this.name = name;
        createAt = new Date().getTime();
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "create_at")
    public long createAt;
}