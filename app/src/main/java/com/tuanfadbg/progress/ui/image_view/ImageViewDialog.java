package com.tuanfadbg.progress.ui.image_view;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.ortiz.touchview.TouchImageView;
import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.Data;
import com.tuanfadbg.progress.database.OnUpdateDatabase;
import com.tuanfadbg.progress.database.item.Item;
import com.tuanfadbg.progress.database.item.ItemDeteleAsyncTask;
import com.tuanfadbg.progress.database.item.ItemInsertAsyncTask;
import com.tuanfadbg.progress.database.tag.Tag;
import com.tuanfadbg.progress.database.tag.TagSelectAllAsyncTask;
import com.tuanfadbg.progress.ui.add_tag.AddTagDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ImageViewDialog extends DialogFragment {

    private TouchImageView imageView;
    private Item item;
    private ConstraintLayout ctBottom;
    private OnItemDeletedListener onItemDeletedListener;
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
        View view = inflater.inflate(R.layout.dialog_image_view, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.imageView);
        ctBottom = view.findViewById(R.id.ct_bottom);

        Glide.with(imageView).load(new File(item.file)).into(imageView);

        imageView.setOnClickListener(v -> {
            if (ctBottom.getVisibility() == View.VISIBLE)
                ctBottom.setVisibility(View.GONE);
            else
                ctBottom.setVisibility(View.VISIBLE);
        });

        view.findViewById(R.id.img_back).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.img_love).setOnClickListener(v -> love());
        view.findViewById(R.id.img_delete).setOnClickListener(v -> delete());
    }

    private void love() {

    }

    private void delete() {
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.are_you_sure))
                .setConfirmText(getString(R.string.delete))
                .setCancelText(getString(R.string.cancel))
                .setConfirmClickListener(sDialog -> {
                    onItemDeletedListener.onItemDelete(item);
                    ItemDeteleAsyncTask itemDeteleAsyncTask = new ItemDeteleAsyncTask(getContext(), item);
                    itemDeteleAsyncTask.execute();
                    sDialog.dismissWithAnimation();
                    dismiss();
                })
                .show();
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setOnItemDeletedListener(OnItemDeletedListener onItemDeletedListener) {
        this.onItemDeletedListener = onItemDeletedListener;
    }

    public interface OnItemDeletedListener {
        void onItemDelete(Item item);
    }
}