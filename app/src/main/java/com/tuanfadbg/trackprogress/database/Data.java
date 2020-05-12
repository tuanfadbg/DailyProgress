package com.tuanfadbg.trackprogress.database;

public class Data<C> {
    private C item;
    private C[] items;
    private OnUpdateDatabase onUpdateDatabase;

    public Data(OnUpdateDatabase onUpdateDatabase) {
        this.onUpdateDatabase = onUpdateDatabase;
    }

    public Data(C item, OnUpdateDatabase onUpdateDatabase) {
        this.item = item;
        this.onUpdateDatabase = onUpdateDatabase;
    }

    public Data(C[] items, OnUpdateDatabase onUpdateDatabase) {
        this.items = items;
        this.onUpdateDatabase = onUpdateDatabase;
    }

    public C[] getItems() {
        return items;
    }

    public C getItem() {
        return item;
    }

    public OnUpdateDatabase getOnUpdateDatabase() {
        return onUpdateDatabase;
    }
}