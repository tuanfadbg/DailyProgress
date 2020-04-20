package com.tuanfadbg.progress.database;

public class Data<C> {
    private C item;
    private OnUpdateDatabase onUpdateDatabase;

    public Data(OnUpdateDatabase onUpdateDatabase) {
        this.onUpdateDatabase = onUpdateDatabase;
    }

    public Data(C item, OnUpdateDatabase onUpdateDatabase) {
        this.item = item;
        this.onUpdateDatabase = onUpdateDatabase;
    }

    public C getItem() {
        return item;
    }

    public OnUpdateDatabase getOnUpdateDatabase() {
        return onUpdateDatabase;
    }
}