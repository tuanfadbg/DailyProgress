package com.tuanfadbg.progress.database.tag;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.tuanfadbg.progress.database.AppDatabase;
import com.tuanfadbg.progress.database.Data;
import com.tuanfadbg.progress.database.OnUpdateDatabase;
import com.tuanfadbg.progress.database.item.Item;

import java.lang.ref.WeakReference;

public class TagInsertAsyncTask extends AsyncTask<Data, Void, Boolean> {
    private WeakReference<Context> contextWeakReference;
    private OnUpdateDatabase onUpdateDatabase;

    public TagInsertAsyncTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected Boolean doInBackground(Data... params) {
        onUpdateDatabase = params[0].getOnUpdateDatabase();
        AppDatabase db = Room.databaseBuilder(contextWeakReference.get(),
                AppDatabase.class, AppDatabase.ROOM_NAME)
                .fallbackToDestructiveMigration()
                .build();

        db.tagDao().insertAll((Tag) params[0].getItem());
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