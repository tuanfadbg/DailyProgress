package com.tuanfadbg.progress.database;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import androidx.room.Room;

import com.tuanfadbg.progress.ui.DataAdapter;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemSelectAsyncTask extends AsyncTask<Boolean, Void, List<Item>> {
    private WeakReference<Context> contextWeakReference;
    private WeakReference<View> emptyViewWeakReference;
    private WeakReference<DataAdapter> reportsAdapterWeakReference;

    public ItemSelectAsyncTask(Context context, DataAdapter reportsAdapter, View emptyView) {
        this.contextWeakReference = new WeakReference<>(context);
        this.reportsAdapterWeakReference = new WeakReference<>(reportsAdapter);
        this.emptyViewWeakReference = new WeakReference<>(emptyView);
    }

    private boolean isGetAll = true;

    @Override
    protected List<Item> doInBackground(Boolean... params) {
        AppDatabase db = Room.databaseBuilder(contextWeakReference.get(),
                AppDatabase.class, AppDatabase.ROOM_NAME)
                .fallbackToDestructiveMigration()
                .build();
        isGetAll = params[0];
        if (isGetAll)
            return db.itemDao().getAll();
        else
            return db.itemDao().getNewestQRCode();
    }

    @Override
    protected void onPostExecute(List<Item> datas) {
        if (isGetAll)
            reportsAdapterWeakReference.get().setData(datas);
        else
            reportsAdapterWeakReference.get().setNewestData(datas);

        if (datas.size() > 0) {
            emptyViewWeakReference.get().setVisibility(View.GONE);
        } else {
            emptyViewWeakReference.get().setVisibility(View.VISIBLE);
        }
    }
}

