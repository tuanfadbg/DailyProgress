package com.tuanfadbg.trackprogress.ui;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tuanfadbg.takephotoutils.TakePhotoCallback;
import com.tuanfadbg.takephotoutils.TakePhotoUtils;
import com.tuanfadbg.trackprogress.beforeafterimage.BuildConfig;
import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.Data;
import com.tuanfadbg.trackprogress.database.OnUpdateDatabase;
import com.tuanfadbg.trackprogress.database.item.Item;
import com.tuanfadbg.trackprogress.database.item.ItemSelectAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.Tag;
import com.tuanfadbg.trackprogress.database.tag.TagInsertAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.TagSelectAllAsyncTask;
import com.tuanfadbg.trackprogress.ui.image_note.ImageNoteDialog;
import com.tuanfadbg.trackprogress.ui.image_view.ImageViewDialog;
import com.tuanfadbg.trackprogress.ui.import_image.ImportImageDialog;
import com.tuanfadbg.trackprogress.ui.main_grid.DataGridAdapter;
import com.tuanfadbg.trackprogress.ui.main_list.TimelineListAdapter;
import com.tuanfadbg.trackprogress.ui.rate.RateDialog;
import com.tuanfadbg.trackprogress.ui.select_image_no_tag.SelectImageNoTagDialog;
import com.tuanfadbg.trackprogress.ui.settings.SettingsDialog;
import com.tuanfadbg.trackprogress.ui.side_by_side.SideBySideDialog;
import com.tuanfadbg.trackprogress.utils.FileManager;
import com.tuanfadbg.trackprogress.utils.SharePreferentUtils;
import com.tuanfadbg.trackprogress.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String UPDATE_NAME = "UPDATE_NAME";
    private static final String UPDATE_TAG = "UPDATE_TAG";
    private static final String UPDATE_ITEM = "UPDATE_ITEM";

    private static final String NAME = "NAME";

//    TextView txtHello;
    ImageView imgGrid, imgList, imgEmpty;
    RecyclerView rcvData;
    ChipGroup chipGroup;
    ConstraintLayout ctStartHere;
    View ctWarning;

    DataGridAdapter dataGridAdapter;
    TimelineListAdapter timelineListAdapter;
    OnPermissionGranted onPermissionGranted;

    TakePhotoUtils takePhotoUtils;
    Integer currentTagSelected = null;
    private List<Item> datas;
    BroadcastReceiver brName;
    BroadcastReceiver brTag;
    BroadcastReceiver brItem;

    @Override
    protected void onResume() {
        super.onResume();
//        Logger.e("resume");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FileManager.removeOldImage();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        txtHello = findViewById(R.id.txt_hello);

        imgGrid = findViewById(R.id.img_grid);
        imgList = findViewById(R.id.img_list);
        imgEmpty = findViewById(R.id.img_empty);

        rcvData = findViewById(R.id.rcv_data);
        chipGroup = findViewById(R.id.rcv_tag);
        ctStartHere = findViewById(R.id.ct_start_here);
        ctWarning = findViewById(R.id.ct_warning);

        takePhotoUtils = new TakePhotoUtils(this, BuildConfig.APPLICATION_ID);

        setListener();
        setData();
        brTag = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getAllTagAndDataInside();
            }
        };
        brItem = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getAllTagAndDataInside();
            }
        };
//        brName = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                updateName(intent.getStringExtra(NAME));
//            }
//        };

//        registerReceiver(brName, new IntentFilter(UPDATE_NAME));
        registerReceiver(brTag, new IntentFilter(UPDATE_TAG));
        registerReceiver(brItem, new IntentFilter(UPDATE_ITEM));
        startAnimationStartHere();
    }

    public static Intent getBRItem() {
        Intent intent = new Intent();
        intent.setAction(UPDATE_ITEM);
        return intent;
    }

    public static Intent getBRTag() {
        Intent intent = new Intent();
        intent.setAction(UPDATE_TAG);
        return intent;
    }

    public static Intent getBRName(String name) {
        Intent intent = new Intent();
        intent.setAction(UPDATE_NAME);
        intent.putExtra(NAME, name);
        return intent;
    }

    private void setListener() {
        imgGrid.setOnClickListener(v -> {
            if (imgGrid.isSelected())
                return;
            imgGrid.setSelected(true);
            imgList.setSelected(false);
            setLayoutGrid();
        });

        imgList.setOnClickListener(v -> {
            if (imgList.isSelected())
                return;
            imgGrid.setSelected(false);
            imgList.setSelected(true);
            setLayoutList();
        });

        findViewById(R.id.img_camera).setOnClickListener(v -> {
            hideViewStartHere();
            ImportImageDialog importImageDialog = new ImportImageDialog();
            importImageDialog.show(getSupportFragmentManager(), ImportImageDialog.class.getSimpleName());
        });

        ctStartHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctStartHere.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.img_compare).setOnClickListener(v -> {
            if (datas == null || datas.size() == 0) {
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
                if (chipGroup.getVisibility() == View.VISIBLE)
                    sweetAlertDialog.setTitle(R.string.error_no_image_on_tag);
                else
                    sweetAlertDialog.setTitle(R.string.error_no_image);
                sweetAlertDialog.setConfirmText(getString(R.string.str_ok));
                sweetAlertDialog.show();
                return;
            }
            SideBySideDialog sideBySideDialog = new SideBySideDialog(datas);
            sideBySideDialog.show(getSupportFragmentManager(), SideBySideDialog.class.getSimpleName());
        });

        findViewById(R.id.img_settings).setOnClickListener(v -> {
            SettingsDialog settingsDialog = new SettingsDialog();
            settingsDialog.show(getSupportFragmentManager(), SettingsDialog.class.getSimpleName());
        });
    }

    public void updateNewItem(boolean hasNewTag, int tagId) {
        if (hasNewTag) {
            TagSelectAllAsyncTask tagSelectAsyncTask = new TagSelectAllAsyncTask(MainActivity.this);
            tagSelectAsyncTask.execute(tags -> {
                fillDataTag(tags);
                ((Chip) findViewById(tagId)).setChecked(true);
            });
        } else {
            selectTag(currentTagSelected);
        }
    }


    private void setData() {
//        String name = SharePreferentUtils.getName(true);
//        name = String.format(getString(R.string.hello_s) + ", %s", name);
//        txtHello.setText(name);

        getAllTagAndDataInside();

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> selectTag(checkedId));

        dataGridAdapter = new DataGridAdapter(this, new ArrayList<>(), this::viewImage);
        timelineListAdapter = new TimelineListAdapter(this, new ArrayList<>(), this::viewImage);

        imgGrid.performClick();
    }

    private void getAllTagAndDataInside() {
        TagSelectAllAsyncTask tagSelectAsyncTask = new TagSelectAllAsyncTask(this);
        tagSelectAsyncTask.execute(tags -> {
            if (tags == null)
                tags = new ArrayList<>();
            if (tags.size() == 0) {
                TagInsertAsyncTask tagInsertAsyncTask = new TagInsertAsyncTask(MainActivity.this);
                Tag tag = new Tag(getString(R.string.default_text));
                tagInsertAsyncTask.execute(new Data(tag, new OnUpdateDatabase() {
                    @Override
                    public void onSuccess() {
                        TagSelectAllAsyncTask tagSelectAsyncTask1 = new TagSelectAllAsyncTask(MainActivity.this);
                        tagSelectAsyncTask1.execute(tags1 -> {
                            fillDataTag(tags1);
                        });
                    }

                    @Override
                    public void onFail() {
                        Toast.makeText(MainActivity.this, getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                    }
                }));
            } else {
                fillDataTag(tags);
            }
        });
    }

    private void setLayoutList() {
        rcvData.setAdapter(timelineListAdapter);
        timelineListAdapter.setData(datas);
        rcvData.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void setLayoutGrid() {
        rcvData.setAdapter(dataGridAdapter);
        dataGridAdapter.setData(datas);
        rcvData.setLayoutManager(new GridLayoutManager(this, 3));
    }

    private void viewImage(Item item) {
        ImageViewDialog imageViewDialog = new ImageViewDialog();
        imageViewDialog.setItem(item);
        imageViewDialog.setOnItemDeletedListener(item1 -> {
            datas.remove(item1);
            if (imgGrid.isSelected())
                dataGridAdapter.removeItem(item1);
            else if (imgList.isSelected()) {
                timelineListAdapter.setData(datas);
            }
        });
        imageViewDialog.show(getSupportFragmentManager(), ImageViewDialog.class.getSimpleName());
    }

    private void fillDataTag(List<Tag> tags) {
        if (tags.size() <= 1) {
            chipGroup.setVisibility(View.GONE);
            selectTag(tags.get(0).uid);
        } else {
            chipGroup.setVisibility(View.VISIBLE);
            chipGroup.removeAllViews();
            for (int i = 0; i < tags.size(); i++) {
                Chip chip = new Chip(MainActivity.this, null, R.attr.CustomChipChoiceStyle);
                chip.setId(tags.get(i).uid);
                chip.setText(tags.get(i).name);
                chipGroup.addView(chip, 0);
            }
            if (currentTagSelected != null && currentTagSelected > 0 && chipGroup.findViewById(currentTagSelected) != null) {
                ((Chip) chipGroup.findViewById(currentTagSelected)).setChecked(true);
            } else {
                ((Chip) chipGroup.getChildAt(0)).setChecked(true);
            }
        }
    }

    private void selectTag(Integer tagId) {
        if (tagId == -1)
            tagId = null;
        flagShowAnimationStartHere = true;

        currentTagSelected = tagId;
        ItemSelectAsyncTask itemSelectAsyncTask
                = new ItemSelectAsyncTask(this);
        Integer finalTagId = tagId;

        itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, null, mDatas -> {
            this.datas = new ArrayList<>();
            for (int i = 0; i < mDatas.size(); i++) {
                Item item = mDatas.get(i);
                if (item.tag != null && item.tag.equals(finalTagId))
                    datas.add(item);

                if (item.tag != null && item.tag.equals(-1)) {
                    showWarningView();
                    flagShowAnimationStartHere = false;
                }
            }

            if (imgGrid.isSelected()) {
                dataGridAdapter.setData(this.datas);
            } else {
                timelineListAdapter.setData(this.datas);
            }

            if (datas.size() > 0) {
                imgEmpty.setVisibility(View.GONE);
                hideViewStartHere();
            } else {
                imgEmpty.setVisibility(View.VISIBLE);
                if (flagShowAnimationStartHere)
                    startAnimationStartHere();
            }
        }));
    }

    boolean flagShowAnimationStartHere = true;

    private void showWarningView() {
        if (ctWarning.getVisibility() == View.VISIBLE)
            return;

        ctWarning.setVisibility(View.VISIBLE);
        ctWarning.setOnClickListener(v -> {
            SelectImageNoTagDialog selectImageNoTagDialog = new SelectImageNoTagDialog(() -> {
                getAllTagAndDataInside();
                ctWarning.setVisibility(View.GONE);
            });
            selectImageNoTagDialog.show(getSupportFragmentManager(), SelectImageNoTagDialog.class.getSimpleName());
        });
    }

    private void startAnimationStartHere() {
        ctStartHere.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(-20, 20);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            ctStartHere.setTranslationY(animatedValue);
        });
        valueAnimator.start();
    }

    private void hideViewStartHere() {
        ctStartHere.setVisibility(View.GONE);
    }

    public void importImage() {
        getTakePhotoUtils()
                .selectMultiple()
                .getImageFromGallery()
                .setListener(new TakePhotoCallback() {
                    @Override
                    public void onMultipleSuccess(List<String> imagesEncodedList, ArrayList<Uri> mArrayUri, List<Long> lastModifieds) {
                        if (isFinishing())
                            return;
                        if (imagesEncodedList != null)
                            for (String imgPath : imagesEncodedList) {
                                if (!Utils.isImage(imgPath)) {
                                    showAlertNotImage();
                                    return;
                                }
                            }
                        ImageNoteDialog imageNoteDialog =
                                new ImageNoteDialog(mArrayUri, getCurrentTagSelected(),
                                        (hasNewTag, tagId) -> {
                                            if (isFinishing())
                                                return;
                                            updateNewItem(hasNewTag, tagId);
                                            showAlertImportSuccess();
                                        });

                        imageNoteDialog.setLastModifieds(lastModifieds);
                        imageNoteDialog.show(getSupportFragmentManager(), ImageNoteDialog.class.getSimpleName());
                    }

                    @Override
                    public void onSuccess(String path, Bitmap bitmap, int width, int height, Uri sourceUri, long lastModified) {
                        if (isFinishing())
                            return;

                        ImageNoteDialog imageNoteDialog =
                                new ImageNoteDialog(bitmap, getCurrentTagSelected(),
                                        (hasNewTag, tagId) -> {
                                            if (isFinishing())
                                                return;
                                            updateNewItem(hasNewTag, tagId);
                                            showAlertImportSuccess();
                                        });
                        imageNoteDialog.setLastModified(lastModified);
                        imageNoteDialog.show(getSupportFragmentManager(), ImageNoteDialog.class.getSimpleName());
                    }

                    @Override
                    public void onFail() {

                    }
                });
    }

    public void takePhoto() {
        takePhotoUtils.takePhoto().toPortrait().setListener(new TakePhotoCallback() {
            @Override
            public void onMultipleSuccess(List<String> imagesEncodedList, ArrayList<Uri> mArrayUri, List<Long> lastModifieds) {

            }

            @Override
            public void onSuccess(String path, Bitmap bitmap, int width, int height, Uri sourceUri, long lastModified) {
//                Log.e(TAG, "onSuccess: path is " + path);
//                Log.e(TAG, "onSuccess: bitmap is" + (bitmap == null));
                ImageNoteDialog imageNoteDialog = null;
                if (path != null)
                    imageNoteDialog = new ImageNoteDialog(
                            path,
                            currentTagSelected,
                            (hasNewTag, tagId) -> {
                                updateNewItem(hasNewTag, tagId);
                            },
                            true);
                else if (bitmap != null)
                    imageNoteDialog = new ImageNoteDialog(
                            bitmap,
                            currentTagSelected,
                            (hasNewTag, tagId) -> {
                                updateNewItem(hasNewTag, tagId);
                            });
                if (imageNoteDialog != null)
                    imageNoteDialog.show(getSupportFragmentManager(), ImageNoteDialog.class.getSimpleName());
            }

            @Override
            public void onFail() {

            }
        });
    }

    private void showAlertNotImage() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle(R.string.import_image);
        sweetAlertDialog.setContentText(getString(R.string.import_not_image));
        sweetAlertDialog.setConfirmText(getString(R.string.str_ok));
        sweetAlertDialog.show();
    }
    SweetAlertDialog alertImportSuccess = null;
    public void showAlertImportSuccess() {
        if (alertImportSuccess != null && alertImportSuccess.isShowing())
            return;
        alertImportSuccess = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        alertImportSuccess.setTitle(R.string.import_image);
        alertImportSuccess.setContentText(getString(R.string.import_success));
        alertImportSuccess.setConfirmText(getString(R.string.str_ok));
        alertImportSuccess.show();
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
        if (onPermissionGranted != null) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted.onPermissionGranted();
            }
            onPermissionGranted = null;
            return;
        }
        takePhotoUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

//    public void updateName(String name) {
//        name = String.format(getString(R.string.hello_s) + ", %s", name);
//        txtHello.setText(name);
//    }

    public TakePhotoUtils getTakePhotoUtils() {
        return takePhotoUtils;
    }

    public int getCurrentTagSelected() {
        return currentTagSelected == null ? 0 : currentTagSelected;
    }

    public void setOnPermissionGranted(OnPermissionGranted onPermissionGranted) {
        this.onPermissionGranted = onPermissionGranted;
    }

    public interface OnPermissionGranted {
        void onPermissionGranted();
    }


    @Override
    public void onBackPressed() {
        if (SharePreferentUtils.shouldShowRate()) {
            RateDialog rateDialog = new RateDialog();
            rateDialog.setOnRateListener(new RateDialog.OnRateListener() {
                @Override
                public void onRate(float star) {
                    if (star == 5)
                        onBackPressed();
                }

                @Override
                public void onCancel() {
                    MainActivity.super.onBackPressed();
                }
            });
            rateDialog.show(getSupportFragmentManager(), RateDialog.class.getSimpleName());
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(brName);
        unregisterReceiver(brTag);
        unregisterReceiver(brItem);
    }
}
