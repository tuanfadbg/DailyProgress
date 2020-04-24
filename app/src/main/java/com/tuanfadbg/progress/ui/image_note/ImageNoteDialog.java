package com.tuanfadbg.progress.ui.image_note;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.Data;
import com.tuanfadbg.progress.database.OnUpdateDatabase;
import com.tuanfadbg.progress.database.item.Item;
import com.tuanfadbg.progress.database.item.ItemInsertAsyncTask;
import com.tuanfadbg.progress.database.tag.Tag;
import com.tuanfadbg.progress.database.tag.TagSelectAllAsyncTask;
import com.tuanfadbg.progress.ui.add_tag.AddTagDialog;
import com.tuanfadbg.progress.utils.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ImageNoteDialog extends DialogFragment {

    private static final String TAG = ImageNoteDialog.class.getSimpleName();
    private Bitmap bitmap;
    private ImageView imageView;
    private ChipGroup chipGroup;
    private EditText edtNote;
    private TextView txtAddTag, txtMoreImage;
    private int currentTagId;
    private OnAddNewItemListener onAddNewItemListener;
    private ArrayList<Uri> multiImageSelected;
    private List<Long> lastModifieds;
    private long lastModified = 0;

    public ImageNoteDialog(Bitmap bitmap, int currentTagId, OnAddNewItemListener onAddNewItemListener) {
        this.bitmap = bitmap;
        this.currentTagId = currentTagId;
        this.onAddNewItemListener = onAddNewItemListener;
    }

    public ImageNoteDialog(ArrayList<Uri> multiImageSelected, int currentTagId, OnAddNewItemListener onAddNewItemListener) {
        this.multiImageSelected = multiImageSelected;
        this.currentTagId = currentTagId;
        this.onAddNewItemListener = onAddNewItemListener;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_image_note, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.imageView);
        edtNote = view.findViewById(R.id.edt_note);
        chipGroup = view.findViewById(R.id.chip_group);
        txtAddTag = view.findViewById(R.id.txt_add_tag);
        txtMoreImage = view.findViewById(R.id.txt_more_image);

        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else if (multiImageSelected != null) {
            if (multiImageSelected.size() > 1) {
                txtMoreImage.setVisibility(View.VISIBLE);
                txtMoreImage.setText(String.format(Locale.US, "+%d", multiImageSelected.size() - 1));
            }

            Glide.with(this).load(multiImageSelected.get(0)).into(imageView);
        }

        updateRecommendTag();

        txtAddTag.setOnClickListener(v -> addTag());
        view.findViewById(R.id.txt_save).setOnClickListener(v -> save());
        view.findViewById(R.id.txt_delete).setOnClickListener(v -> dismiss());
    }

    private void updateRecommendTag() {
        TagSelectAllAsyncTask tagSelectAsyncTask = new TagSelectAllAsyncTask(getContext());
        tagSelectAsyncTask.execute(tags -> {
            if (tags == null)
                tags = new ArrayList<>();
            // remove all child excepts the see more item
            while (chipGroup.getChildCount() > 1)
                chipGroup.removeView(chipGroup.getChildAt(0));

            // find current tag and put it on position 0
            for (int i = 0; i < tags.size(); i++) {
                if (tags.get(i).uid == currentTagId) {
                    Chip chip = new Chip(getContext(), null, R.attr.CustomChipChoiceStyle);
                    chip.setId(tags.get(i).uid);
                    chip.setText(tags.get(i).name);
                    chip.setChecked(true);
                    chipGroup.addView(chip, 0);
                    break;
                }
            }

            int size = tags.size();
            if (size > 3)
                size = 3;
            for (int i = 0; i < size; i++) {
                if (tags.get(i).uid == currentTagId)
                    continue;
                Chip chip = new Chip(getContext(), null, R.attr.CustomChipChoiceStyle);
                chip.setId(tags.get(i).uid);
                chip.setText(tags.get(i).name);
                chipGroup.addView(chip, 1);
            }
        });
    }

    private boolean hasNewTag = false;

    private void addTag() {
        AddTagDialog addTagDialog = new AddTagDialog();
        addTagDialog.setOnAddTagListener(new AddTagDialog.OnAddTagListener() {
            @Override
            public void onTagAdded(Tag tag) {
                Chip chip = new Chip(getContext(), null, R.attr.CustomChipChoiceStyle);
                chip.setId(tag.uid);
                chip.setText(tag.name);
                chip.setChecked(true);
                chipGroup.addView(chip, 0);
                currentTagId = tag.uid;
                hasNewTag = true;
            }

            @Override
            public void onTagSelected(Tag tag) {
                currentTagId = tag.uid;
                updateRecommendTag();
            }
        });
        addTagDialog.show(getFragmentManager(), AddTagDialog.class.getSimpleName());
    }

    private void save() {
        if (bitmap != null)
            saveToInternalStorage(bitmap);
        else if (multiImageSelected != null) {

//            Observable.fromIterable(multiImageSelected)
//                    .flatMapCompletable(entity->
//                            Observable.fromIterable(entity)
//                                    .flatMapCompletable(this::uploadImag2)
//                                    .doOnComplete(() ->{
//                                        entity.update(entity.setUploaeded(true));
//                                        repository.store(entity);
//                                    }).subscribeOn(Schedulers.computation()));
            Observable.fromArray(multiImageSelected)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ArrayList<Uri>>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull ArrayList<Uri> uris) {
                            for (int i = 0; i < uris.size(); i++) {
                                saveToInternalStorage(uris.get(i), lastModifieds.get(i));
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            int tagId = chipGroup.getCheckedChipId();
                            if (tagId == View.NO_ID)
                                tagId = chipGroup.getChildAt(0).getId();
                            if (onAddNewItemListener != null)
                                onAddNewItemListener.onNewItem(hasNewTag, tagId);
                            dismiss();
                        }
                    });
        }
    }


    private void saveToInternalStorage(Uri uri, long lastModified) {
        FileManager fileManager = new FileManager(getActivity());
        String mypath = fileManager.saveFileFromInputStreamUri(uri);

        ItemInsertAsyncTask itemInsertAsyncTask = new ItemInsertAsyncTask(getContext());
        int tagId = chipGroup.getCheckedChipId();
        if (tagId == View.NO_ID)
            tagId = chipGroup.getChildAt(0).getId();
        Item item = new Item(edtNote.getText().toString().trim()
                , mypath, tagId, true, lastModified);

        itemInsertAsyncTask.execute(new Data(item, new OnUpdateDatabase() {
            @Override
            public void onSuccess() {
//                if (onAddNewItemListener != null)
//                    onAddNewItemListener.onNewItem(hasNewTag, finalTagId);
//                dismiss();
            }

            @Override
            public void onFail() {
                Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            }
        }));
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {
        FileManager fileManager = new FileManager(getActivity());
        File mypath = fileManager.storeImageOnPrivateStorage(bitmapImage);

        ItemInsertAsyncTask itemInsertAsyncTask = new ItemInsertAsyncTask(getContext());
        int tagId = chipGroup.getCheckedChipId();
        if (tagId == View.NO_ID)
            tagId = chipGroup.getChildAt(0).getId();
        Item item = new Item(edtNote.getText().toString().trim()
                , mypath.getAbsolutePath(), tagId, true, lastModified);
        int finalTagId = tagId;
        itemInsertAsyncTask.execute(new Data(item, new OnUpdateDatabase() {
            @Override
            public void onSuccess() {
                if (onAddNewItemListener != null)
                    onAddNewItemListener.onNewItem(hasNewTag, finalTagId);
                dismiss();
            }

            @Override
            public void onFail() {
                Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            }
        }));
    }


    public void setLastModifieds(List<Long> lastModifieds) {
        this.lastModifieds = lastModifieds;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public interface OnAddNewItemListener {
        void onNewItem(boolean hasNewTag, int tagSelected);
    }
}