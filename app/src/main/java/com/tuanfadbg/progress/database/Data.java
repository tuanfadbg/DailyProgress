package com.tuanfadbg.progress.database;

public class Data {
    private Item item;
    private OnUpdateDatabase onUpdateDatabase;

    public Data(OnUpdateDatabase onUpdateDatabase) {
        this.onUpdateDatabase = onUpdateDatabase;
    }

    public Data(Item item, OnUpdateDatabase onUpdateDatabase) {
        this.item = item;
        this.onUpdateDatabase = onUpdateDatabase;
    }

    public Item getItem() {
        return item;
    }

    public OnUpdateDatabase getOnUpdateDatabase() {
        return onUpdateDatabase;
    }
}