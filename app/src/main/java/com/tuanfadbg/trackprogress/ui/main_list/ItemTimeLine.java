package com.tuanfadbg.trackprogress.ui.main_list;

import com.tuanfadbg.trackprogress.database.item.Item;

import java.util.List;

public class ItemTimeLine {
    private int time;
    private List<Item> items;

    public ItemTimeLine(int time, List<Item> items) {
        this.time = time;
        this.items = items;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
