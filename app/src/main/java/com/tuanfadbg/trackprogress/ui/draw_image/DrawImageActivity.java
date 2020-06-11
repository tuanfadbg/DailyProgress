package com.tuanfadbg.trackprogress.ui.draw_image;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;

import com.tuanfadbg.trackprogress.beforeafterimage.R;

import java.io.File;
import java.io.IOException;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class DrawImageActivity extends AppCompatActivity {

    public static final String SCREEN_SHOT_PATH = "SCREEN_SHOT_PATH";
    public static final int REQUEST_CODE = 121;
    private static final String TAG = DrawImageActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, String filePath) {
        Intent intent = new Intent(context, DrawImageActivity.class);
        intent.putExtra(SCREEN_SHOT_PATH, filePath);
        return intent;
    }

    String filePath;

    ImageView imgBrush, imgErase, imgText, imgSave;
    PhotoEditorView mPhotoEditorView;
    PhotoEditor mPhotoEditor;
    ProgressBar progressBar;
    private int initColor = Color.RED, brushSize = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_image);
        filePath = getIntent().getStringExtra(SCREEN_SHOT_PATH);
        init();
        setListener();
    }

    private void init() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;

        mPhotoEditorView = findViewById(R.id.photoEditorView);
        imgBrush = findViewById(R.id.imageView6);
        imgErase = findViewById(R.id.imageView7);
        imgText = findViewById(R.id.imageView8);
        imgSave = findViewById(R.id.imageView9);
        progressBar = findViewById(R.id.progressBar);

        File imgFile = new File(filePath);
        if (imgFile.exists()) {
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(filePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotationInDegrees = exifToDegrees(orientation);
                Matrix matrix = new Matrix();
                Bitmap myBitmapTemp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (orientation != 0) {
                    matrix.preRotate(rotationInDegrees);
                    myBitmapTemp = Bitmap.createBitmap(myBitmapTemp, 0, 0, myBitmapTemp.getWidth(), myBitmapTemp.getHeight(), matrix, true);
                }

                Handler handler = new Handler();
                Bitmap myBitmap = myBitmapTemp;

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mPhotoEditorView.getLayoutParams();
                        if ((float) screenWidth / (float) screenHeight < (float) myBitmap.getWidth() / (float) myBitmap.getHeight()) {
                            layoutParams.width = screenWidth;
                            layoutParams.height = (int) (screenWidth * (float) myBitmap.getHeight() / (float) myBitmap.getWidth());
                        } else {
                            layoutParams.height = screenHeight;
                            layoutParams.width = (int) (screenHeight * (float) myBitmap.getWidth() / (float) myBitmap.getHeight());
                        }
                        Log.e(TAG, "init: " + layoutParams.width + " " + layoutParams.height);
                        mPhotoEditorView.setLayoutParams(layoutParams);
                        progressBar.setVisibility(View.GONE);
                        mPhotoEditorView.getSource().setImageBitmap(myBitmap);
                    }
                }, 500);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true)
                .build();
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private void setListener() {

        imgBrush.setOnClickListener(v -> {
            ToolBrushDialog toolBrushDialog = new ToolBrushDialog(this, initColor, brushSize);
            toolBrushDialog.setOnToolBrushListener(new ToolBrushDialog.OnToolBrushListener() {
                @Override
                public void onOK(int color, int brush) {
                    resetSelectTools();
                    v.setSelected(true);
                    initColor = color;
                    brushSize = brush;
                    brush();
                    toolBrushDialog.dismiss();
                }

                @Override
                public void onCancel() {
                    toolBrushDialog.dismiss();
                }
            });
            toolBrushDialog.show();
        });

        imgErase.setOnClickListener(v -> {
            resetSelectTools();
            v.setSelected(true);
            mPhotoEditor.brushEraser();
        });

        imgText.setOnClickListener(v -> {
            ToolTextDialog toolTextDialog = new ToolTextDialog(this, initColor);
            toolTextDialog.setOnToolTextListener(new ToolTextDialog.OnToolTextListener() {
                @Override
                public void onOK(String text, int color) {
                    resetSelectTools();
                    v.setSelected(true);
                    initColor = color;
                    mPhotoEditor.addText(text, color);
                    toolTextDialog.dismiss();
                }

                @Override
                public void onCancel() {
                    toolTextDialog.dismiss();
                }
            });
            toolTextDialog.show();
        });

        imgSave.setOnClickListener(v -> {
            resetSelectTools();
            v.setSelected(true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(DrawImageActivity.this, getString(R.string.fail_to_save_your_image), Toast.LENGTH_LONG).show();
                return;
            }
            mPhotoEditor.saveAsFile(filePath, new PhotoEditor.OnSaveListener() {
                @Override
                public void onSuccess(String imagePath) {
                    MediaScannerConnection.scanFile(DrawImageActivity.this,
                            new String[]{filePath},
                            new String[]{"image/png"},
                            null);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filePath))));
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }

                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(DrawImageActivity.this, getString(R.string.fail_to_save_your_image), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void brush() {
        mPhotoEditor.setBrushDrawingMode(true);
        mPhotoEditor.setBrushSize(brushSize);
        mPhotoEditor.setBrushColor(initColor);
    }

    private void resetSelectTools() {
        imgBrush.setSelected(false);
        imgErase.setSelected(false);
        imgText.setSelected(false);
        imgSave.setSelected(false);
    }
}
