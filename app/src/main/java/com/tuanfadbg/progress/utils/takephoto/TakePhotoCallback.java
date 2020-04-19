package com.tuanfadbg.progress.utils.takephoto;

import android.graphics.Bitmap;

public interface TakePhotoCallback {
    void onSuccess(Bitmap bitmap, int width, int height);

    void onFail();
}