package com.tuanfadbg.progress.database.item;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.tuanfadbg.progress.database.AppDatabase;

import java.lang.ref.WeakReference;

public class ItemDeteleAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private WeakReference<Context> contextWeakReference;
    public Item item;

    public ItemDeteleAsyncTask(Context context, Item item) {
        this.contextWeakReference = new WeakReference<>(context);
        this.item = item;
    }
    @Override
    protected Boolean doInBackground(Void... params) {
        AppDatabase db = Room.databaseBuilder(contextWeakReference.get(),
                AppDatabase.class, AppDatabase.ROOM_NAME)
                .fallbackToDestructiveMigration()
                .build();

        db.itemDao().delete(item);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {

    }
}