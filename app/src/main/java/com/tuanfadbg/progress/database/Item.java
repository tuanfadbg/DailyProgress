package com.tuanfadbg.progress.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "Item")
public class Item {

    public Item(String title, String file, Integer tag, boolean isPrivate) {
        this.title = title;
        this.des = "";
        this.file = file;
        this.tag = tag;
        this.isPrivate = isPrivate;
        this.createAt = new Date().getTime();
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "des")
    public String des;

    @ColumnInfo(name = "file")
    public String file;

    @ColumnInfo(name = "tag")
    public Integer tag;

    @ColumnInfo(name = "is_private")
    public Boolean isPrivate;

    @ColumnInfo(name = "create_at")
    public long createAt;
}