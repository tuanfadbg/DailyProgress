package com.tuanfadbg.progress.ui;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tuanfadbg.progress.BuildConfig;
import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.Data;
import com.tuanfadbg.progress.database.Item;
import com.tuanfadbg.progress.database.ItemInsertAsyncTask;
import com.tuanfadbg.progress.database.ItemSelectAsyncTask;
import com.tuanfadbg.progress.database.OnUpdateDatabase;
import com.tuanfadbg.progress.utils.Constants;
import com.tuanfadbg.progress.utils.takephoto.TakePhotoCallback;
import com.tuanfadbg.progress.utils.takephoto.TakePhotoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    TextView txtHello;
    ImageView imgGrid, imgList, imgEmpty;
    RecyclerView rcvData;
    ChipGroup chipGroup;

    com.tuanfadbg.progress.utils.takephoto.TakePhotoUtils takePhotoUtils;

    DataAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtHello = findViewById(R.id.txt_hello);

        imgGrid = findViewById(R.id.img_grid);
        imgList = findViewById(R.id.img_list);
        imgEmpty = findViewById(R.id.img_empty);

        rcvData = findViewById(R.id.rcv_data);
        chipGroup = findViewById(R.id.rcv_tag);

        takePhotoUtils = new TakePhotoUtils(this, BuildConfig.APPLICATION_ID);

        setListener();
        setData();
    }

    private void setListener() {
        imgGrid.setOnClickListener(v -> {
            imgGrid.setSelected(true);
            imgList.setSelected(false);
        });

        imgList.setOnClickListener(v -> {
            imgGrid.setSelected(false);
            imgList.setSelected(true);
        });

        findViewById(R.id.img_camera).setOnClickListener(v -> takePhotoUtils.takePhoto().setListener(new TakePhotoCallback() {
            @Override
            public void onSuccess(Bitmap bitmap, int width, int height) {
                saveImage(bitmap);
            }

            @Override
            public void onFail() {

            }
        }));
    }

    private void saveImage(Bitmap bitmap) {
        saveToInternalStorage(bitmap);
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("data", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "image_" + new Date().getTime() + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ItemInsertAsyncTask itemInsertAsyncTask = new ItemInsertAsyncTask(this);
        Item item = new Item("hello", mypath.getAbsolutePath(), Constants.TAG_DEFAULT, true);
        itemInsertAsyncTask.execute(new Data(item, new OnUpdateDatabase() {
            @Override
            public void onSuccess() {
                ItemSelectAsyncTask itemSelectAsyncTask
                        = new ItemSelectAsyncTask(MainActivity.this, dataAdapter, imgEmpty);
                itemSelectAsyncTask.execute(false);
            }

            @Override
            public void onFail() {

            }
        }));
    }

    private void loadImageFromStorage(String path) {
        try {
            File f = new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//            ImageView img = (ImageView) findViewById(R.id.imgPicker);
//            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setData() {
        imgGrid.performClick();

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, @IdRes int checkedId) {
                // Handle the checked chip change.
            }
        });



        Chip chip = new Chip(this, null, R.attr.CustomChipChoiceStyle);
        chip.setText("hello");
        chipGroup.addView(chip, 0);

//        chipGroup.addCh

        dataAdapter = new DataAdapter(this, new ArrayList<>());
        rcvData.setAdapter(dataAdapter);
        rcvData.setLayoutManager(new GridLayoutManager(this, 3));
        rcvData.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int space = 16;
                outRect.left = space;
                outRect.right = space;
                outRect.bottom = space;
                outRect.top = space;

//                // Add top margin only for the first item to avoid double space between items
//                if (parent.getChildLayoutPosition(view) == 0) {
//                    outRect.top = space;
//                } else {
//                    outRect.top = 0;
//                }
            }
        });
        ItemSelectAsyncTask itemSelectAsyncTask
                = new ItemSelectAsyncTask(this, dataAdapter, imgEmpty);
        itemSelectAsyncTask.execute(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        takePhotoUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        takePhotoUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
