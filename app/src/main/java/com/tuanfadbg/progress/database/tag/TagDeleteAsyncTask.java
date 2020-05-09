package com.tuanfadbg.progress.database.tag;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.tuanfadbg.progress.database.AppDatabase;
import com.tuanfadbg.progress.database.Data;
import com.tuanfadbg.progress.database.OnUpdateDatabase;

import java.lang.ref.WeakReference;

public class TagDeleteAsyncTask extends AsyncTask<Data, Void, Boolean> {
    private WeakReference<Context> contextWeakReference;
    private OnUpdateDatabase onUpdateDatabase;

    public TagDeleteAsyncTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected Boolean doInBackground(Data... params) {
        onUpdateDatabase = params[0].getOnUpdateDatabase();
        AppDatabase db = Room.databaseBuilder(contextWeakReference.get(),
                AppDatabase.class, AppDatabase.ROOM_NAME)
                .fallbackToDestructiveMigration()
                .build();

        db.tagDao().delete((Tag) params[0].getItem());
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