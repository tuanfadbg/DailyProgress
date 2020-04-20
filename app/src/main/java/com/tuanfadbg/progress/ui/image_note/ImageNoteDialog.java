package com.tuanfadbg.progress.ui.image_note;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class ImageNoteDialog extends DialogFragment {

    public static final String TAG = "example_dialog";

    private Bitmap bitmap;
    private ImageView imageView;
    private ChipGroup chipGroup;
    private EditText edtNote;
    private TextView txtAddTag;
    private int currentTagId;
    private OnAddNewItemListener onAddNewItemListener;
//    public static ImageNoteDialog display(FragmentManager fragmentManager) {
//        ImageNoteDialog exampleDialog = new ImageNoteDialog();
//        exampleDialog.show(fragmentManager, TAG);
//        return exampleDialog;
//    }


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
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);

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
        saveToInternalStorage(bitmap);
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getContext());
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

        ItemInsertAsyncTask itemInsertAsyncTask = new ItemInsertAsyncTask(getContext());
        int tagId = chipGroup.getCheckedChipId();
        if (tagId == View.NO_ID)
            tagId = chipGroup.getChildAt(0).getId();
        Item item = new Item(edtNote.getText().toString().trim()
                , mypath.getAbsolutePath(), tagId, true);
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

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setCurrentTagId(int currentTagId) {
        this.currentTagId = currentTagId;
    }

    public void setOnAddNewItemListener(OnAddNewItemListener onAddNewItemListener) {
        this.onAddNewItemListener = onAddNewItemListener;
    }

    public interface OnAddNewItemListener {
        void onNewItem(boolean hasNewTag, int tagSelected);
    }
}