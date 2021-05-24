package com.tuanfadbg.trackprogress.ui.image_view;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.ortiz.touchview.TouchImageView;
import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.Data;
import com.tuanfadbg.trackprogress.database.OnUpdateDatabase;
import com.tuanfadbg.trackprogress.database.item.Item;
import com.tuanfadbg.trackprogress.database.item.ItemDeteleAsyncTask;
import com.tuanfadbg.trackprogress.database.item.ItemUpdateAsyncTask;
import com.tuanfadbg.trackprogress.ui.MainActivity;
import com.tuanfadbg.trackprogress.ui.crop_image.CropImageDialog;
import com.tuanfadbg.trackprogress.ui.draw_image.DrawImageActivity;
import com.tuanfadbg.trackprogress.ui.image_note.ImageNoteDialog;
import com.tuanfadbg.trackprogress.ui.side_by_side.SideBySideDialog;
import com.tuanfadbg.trackprogress.utils.FileManager;
import com.tuanfadbg.trackprogress.utils.RotateTransformation;
import com.tuanfadbg.trackprogress.utils.SharePreferentUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ImageViewDialog extends DialogFragment {

    private static final String TAG = ImageNoteDialog.class.getSimpleName();
    private static final int WRITE_EXTERNAL_REQUEST_CODE = 1222;
    private TouchImageView imageView;
    private Item item;
    private ConstraintLayout ctBottom, ctRotateControl, ctGrid;
    private TextView txtRotateValue, txtRotateCancel, txtRotateDone;
    private SeekBar seekBarRotate;
    private OnItemDeletedListener onItemDeletedListener;
    private TextView txtTitle;

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

    long timeView = new Date().getTime();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.imageView);
        imageView.setMaxZoom(100f);
        imageView.setDoubleTapScale(10f);
        ctBottom = view.findViewById(R.id.ct_bottom);
        txtTitle = view.findViewById(R.id.txt_title);

        ctRotateControl = view.findViewById(R.id.ct_rotate_control);
        ctGrid = view.findViewById(R.id.ct_grid);

        txtRotateValue = view.findViewById(R.id.txt_rotate_value);
        txtRotateCancel = view.findViewById(R.id.txt_rotate_cancel);
        txtRotateDone = view.findViewById(R.id.txt_rotate_done);
        seekBarRotate = view.findViewById(R.id.seekbar_rotate);

        if (item == null)
            return;
        txtTitle.setText((TextUtils.isEmpty(item.title) ? "" : item.title));
        Glide.with(imageView)
                .load(new File(item.file))
                .signature(new ObjectKey(timeView))
                .into(imageView);


        setListener(view);
    }

    private void setListener(View view) {
        imageView.setOnClickListener(v -> {
            if (ctRotateControl.getVisibility() == View.VISIBLE)
                return;
            if (ctBottom.getVisibility() == View.VISIBLE)
                ctBottom.setVisibility(View.GONE);
            else
                ctBottom.setVisibility(View.VISIBLE);
        });

        view.findViewById(R.id.img_rotate).setOnClickListener(v -> rotate());
        view.findViewById(R.id.img_compare).setOnClickListener(v -> compare());
        view.findViewById(R.id.img_back).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.img_comment).setOnClickListener(v -> edit());
        view.findViewById(R.id.img_edit).setOnClickListener(v -> startActivityForResult(DrawImageActivity.getStartIntent(getContext(), item.file), DrawImageActivity.REQUEST_CODE));
        view.findViewById(R.id.img_crop).setOnClickListener(v -> crop());
        view.findViewById(R.id.img_share).setOnClickListener(v -> share());
        view.findViewById(R.id.img_delete).setOnClickListener(v -> delete());
        if (!TextUtils.isEmpty(item.title))
            txtTitle.setOnClickListener(v -> viewMoreTitle());


        ctRotateControl.setOnClickListener(v -> {
        });

        txtRotateDone.setOnClickListener(v -> {
            resetRotateControl();
        });

        txtRotateCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctGrid.getVisibility() == View.VISIBLE) {
                    rotateAngle = previousRotationIfCancel;
                    resetRotateControl();
                    imageView.setRotation(0f);
                    reloadImage();
                }

            }
        });
    }

    private void resetRotateControl() {
        ctGrid.setVisibility(View.GONE);
        ctRotateControl.setVisibility(View.GONE);
        seekBarRotate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void viewMoreTitle() {
        if (txtTitle.getMaxLines() < 10)
            txtTitle.setMaxLines(Integer.MAX_VALUE);
        else txtTitle.setMaxLines(2);
    }

    private float rotateAngle, previousRotationIfCancel, previousRotation = 0f;

    private void rotate() {
        ctRotateControl.setVisibility(View.VISIBLE);
        ctGrid.setVisibility(View.VISIBLE);
        ctBottom.setVisibility(View.GONE);
        seekBarRotate.setProgress((int) rotateAngle + 180);
        previousRotation = seekBarRotate.getProgress();
        previousRotationIfCancel = seekBarRotate.getProgress() - 180;
        txtRotateValue.setText(String.format(Locale.US, "%d°", (int) rotateAngle));
        seekBarRotate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rotateAngle = progress - 180f;
                txtRotateValue.setText(String.format(Locale.US, "%d°", (int) rotateAngle));
                imageView.setRotation(progress - previousRotation);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                imageView.setRotation(0f);
                previousRotation = seekBarRotate.getProgress();
                reloadImage();
            }
        });

    }

    private void reloadImage() {
        Glide.with(imageView)
                .load(new File(item.file))
                .signature(new ObjectKey(new Date().getTime()))
                .transform(new RotateTransformation(getContext(), rotateAngle))
                .into(imageView);
    }

    private void edit() {
        ImageNoteDialog imageNoteDialog = new ImageNoteDialog(item, new ImageNoteDialog.OnItemEditedListener() {
            @Override
            public void onEdited(Item item) {
                if (getActivity() == null)
                    return;
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE);
                sweetAlertDialog.setTitle(getString(R.string.saved));
                sweetAlertDialog.show();
            }
        });

        imageNoteDialog.show(getFragmentManager(), ImageNoteDialog.class.getSimpleName());
    }

    private void crop() {
        File tempFile = new FileManager(getActivity()).getNewFileInPrivateStorage();
        CropImageDialog.show(getFragmentManager(), new File(item.file), tempFile, new CropImageDialog.OnCropListener() {
            @Override
            public void onCrop(File dest) {
                File previousFile = new File(item.file);
                if (previousFile.delete()) {
                    Glide.with(imageView).load(dest).signature(new ObjectKey(new Date().getTime())).into(imageView);
                    item.file = dest.getAbsolutePath();
                    ItemUpdateAsyncTask itemUpdateAsyncTask = new ItemUpdateAsyncTask(getContext());
                    itemUpdateAsyncTask.execute(new Data(item, new OnUpdateDatabase() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFail() {

                        }
                    }));
                } else {
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog.setTitle(R.string.unknown_error);
                    sweetAlertDialog.show();
                }
            }

            @Override
            public void cancel() {
                tempFile.delete();
            }
        });
    }

    private void compare() {
        SideBySideDialog sideBySideDialog = new SideBySideDialog(item);
        sideBySideDialog.show(getActivity().getSupportFragmentManager(), SideBySideDialog.class.getSimpleName());
    }

    private void share() {
        if (FileManager.isWriteStoragePermissionGranted(getActivity())) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
            sweetAlertDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.blue));
            sweetAlertDialog.setTitle(R.string.loading);
            sweetAlertDialog.show();

            FileManager fileManager = new FileManager(getActivity());
            File src = new File(item.file);
            File dest = fileManager.getOutputMediaFile();
            SharePreferentUtils.insertImagePathHaveToRemove(dest.getAbsolutePath());

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        fileManager.copyFileUsingStream(src, dest);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    share(dest);
                    if (ImageViewDialog.this.getActivity() != null)
                        ImageViewDialog.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        });
                }
            });
        } else {
            ((MainActivity) getActivity()).setOnPermissionGranted(new MainActivity.OnPermissionGranted() {
                @Override
                public void onPermissionGranted() {
                    share();
                }
            });
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_REQUEST_CODE);
        }
    }

    private void share(File dest) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                FileProvider.getUriForFile(getContext(), getContext().getPackageName(), dest) : Uri.fromFile(dest));
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_video)));
    }

    private void delete() {
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.are_you_sure))
                .setConfirmText(getString(R.string.delete))
                .setCancelText(getString(R.string.cancel))
                .setConfirmClickListener(sDialog -> {
                    File file = new File(item.file);
                    file.delete();

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