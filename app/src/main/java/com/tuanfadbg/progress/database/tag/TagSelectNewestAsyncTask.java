package com.tuanfadbg.progress.database.tag;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.tuanfadbg.progress.database.AppDatabase;

import java.lang.ref.WeakReference;
import java.util.List;

public class TagSelectNewestAsyncTask extends AsyncTask<OnTagSelectedListener, Void, List<Tag>> {
    private WeakReference<Context> contextWeakReference;

    public TagSelectNewestAsyncTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    private OnTagSelectedListener onTagSelectedListener;

    @Override
    protected List<Tag> doInBackground(OnTagSelectedListener... params) {
        AppDatabase db = Room.databaseBuilder(contextWeakReference.get(),
                AppDatabase.class, AppDatabase.ROOM_NAME)
                .fallbackToDestructiveMigration()
                .build();
        onTagSelectedListener = params[0];
        return db.tagDao().getNewestTag();
    }

    @Override
    protected void onPostExecute(List<Tag> tags) {
        onTagSelectedListener.onTags(tags);
    }
}

