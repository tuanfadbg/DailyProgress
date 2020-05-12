package com.tuanfadbg.trackprogress.database.item;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.tuanfadbg.trackprogress.database.AppDatabase;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemSelectAsyncTask extends AsyncTask<ItemSelectAsyncTask.Data, Void, List<Item>> {
    private WeakReference<Context> contextWeakReference;

    public ItemSelectAsyncTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    private OnItemSelectedListener onItemSelectedListener;
    private boolean isGetAll = true;
    private Integer tagId;

    @Override
    protected List<Item> doInBackground(Data... params) {
        AppDatabase db = Room.databaseBuilder(contextWeakReference.get(),
                AppDatabase.class, AppDatabase.ROOM_NAME)
                .fallbackToDestructiveMigration()
                .build();
        isGetAll = params[0].isGetAll;
        tagId = params[0].tagId;
        onItemSelectedListener = params[0].onItemSelectedListener;

        if (isGetAll) {
            if (tagId == null)
                return db.itemDao().getAll();
            else {
                return db.itemDao().getAllByTag(tagId);
            }
        } else {
            return db.itemDao().getNewest();
        }
    }

    @Override
    protected void onPostExecute(List<Item> datas) {
        onItemSelectedListener.onSelected(datas);
    }

    public static class Data {
        boolean isGetAll;
        Integer tagId;
        OnItemSelectedListener onItemSelectedListener;

        public Data(boolean isGetAll, Integer tagId, OnItemSelectedListener onItemSelectedListener) {
            this.isGetAll = isGetAll;
            this.tagId = tagId;
            this.onItemSelectedListener = onItemSelectedListener;
        }
    }
    public interface OnItemSelectedListener {
        void onSelected(List<Item> datas);
    }
}

