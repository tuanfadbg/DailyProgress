package com.tuanfadbg.trackprogress.database.item;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.tuanfadbg.trackprogress.database.AppDatabase;
import com.tuanfadbg.trackprogress.database.Data;
import com.tuanfadbg.trackprogress.database.OnUpdateDatabase;

import java.lang.ref.WeakReference;

public class ItemInsertAsyncTask extends AsyncTask<Data, Void, Boolean> {
    private WeakReference<Context> contextWeakReference;
    private OnUpdateDatabase onUpdateDatabase;

    public ItemInsertAsyncTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected Boolean doInBackground(Data... params) {
        onUpdateDatabase = params[0].getOnUpdateDatabase();
        AppDatabase db = Room.databaseBuilder(contextWeakReference.get(),
                AppDatabase.class, AppDatabase.ROOM_NAME)
                .fallbackToDestructiveMigration()
                .build();

        db.itemDao().insertAll((Item) params[0].getItem());
        return true;
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        if (isSuccess) {
            onUpdateDatabase.onSuccess();
        } else {
            onUpdateDatabase.onFail();
        }
    }
}