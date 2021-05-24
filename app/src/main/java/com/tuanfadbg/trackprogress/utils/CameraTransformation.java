package com.tuanfadbg.trackprogress.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.io.IOException;
import java.security.MessageDigest;

public class CameraTransformation extends BitmapTransformation {

    private int mOrientation = 0;

    public CameraTransformation(String filePath) {
        setOrientation(filePath);
    }

    public CameraTransformation(Uri uri) {
        setOrientation(uri.getPath());
    }

    public void setOrientation(String path) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
                    mOrientation = 90;
                    break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
                    mOrientation = 180;
                    break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
                    mOrientation = 270;
                    break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL:
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        int exifOrientationDegrees = getExifOrientationDegrees(mOrientation);
        return TransformationUtils.rotateImageExif(pool, toTransform, exifOrientationDegrees);
    }

    private int getExifOrientationDegrees(int orientation) {
        int exifInt;
        switch (orientation) {
            case 90:
                exifInt = ExifInterface.ORIENTATION_ROTATE_90;
                break;
            // other cases
            default:
                exifInt = ExifInterface.ORIENTATION_NORMAL;
                break;
        }
        return exifInt;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(("rotate" + mOrientation).getBytes());
    }
}