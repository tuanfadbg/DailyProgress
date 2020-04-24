package com.tuanfadbg.progress.utils.takephoto;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public interface TakePhotoCallback {

    void onMultipleSuccess(List<String> imagesEncodedList, ArrayList<Uri> mArrayUri, List<Long> lastModifieds);

    void onSuccess(Bitmap bitmap, int width, int height, Uri sourceUri, long lastModified);

    void onFail();
}