package com.tuanfadbg.trackprogress.ui.notice_image_export_crop;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.utils.FileManager;
import com.tuanfadbg.trackprogress.utils.SharePreferentUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NoticeImageExportedCropDialog extends DialogFragment {

    private static final String TAG = NoticeImageExportedCropDialog.class.getSimpleName();
    private Bitmap resultBitmap;
    private ImageView imageView;
    private CropImageView cropImageView;
    private TextView txtSave, txtCrop, txtCancel;
//    private ProgressBar progressBar;
    private File tempFile;
    private boolean loadFromTempFile = false;
    private boolean isSave = false;

    public static void showSaveDialog(FragmentManager fragmentManager, Bitmap bitmap) {
        NoticeImageExportedCropDialog dialog = new NoticeImageExportedCropDialog();
        dialog.resultBitmap = bitmap;
        dialog.isSave = true;
        dialog.show(fragmentManager, NoticeImageExportedCropDialog.class.getSimpleName());
    }

    public static void showShareDialog(FragmentManager fragmentManager, Bitmap bitmap) {
        NoticeImageExportedCropDialog dialog = new NoticeImageExportedCropDialog();
        dialog.resultBitmap = bitmap;
        dialog.isSave = false;
        dialog.show(fragmentManager, NoticeImageExportedCropDialog.class.getSimpleName());
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
        View view = inflater.inflate(R.layout.dialog_notice_image_exported_crop, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.imageView);

        cropImageView = view.findViewById(R.id.crop_image_view);
        cropImageView.setVisibility(View.GONE);

        if (resultBitmap != null)
            Glide.with(imageView)
                    .load(resultBitmap)
                    .into(imageView);
        txtSave = view.findViewById(R.id.txt_save);
        txtCrop = view.findViewById(R.id.txt_crop);
        txtCancel = view.findViewById(R.id.txt_delete);
//        progressBar = view.findViewById(R.id.progressBar);

        if (isSave)
            txtSave.setText(R.string.save);
        else
            txtSave.setText(R.string.share);

        resetButtonListener();
    }

    private void saveOrShare() {
        SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.blue));
        pDialog.setTitleText(getString(R.string.loading));
        pDialog.setCancelable(false);
        pDialog.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FileManager fileManager = new FileManager(getActivity());
                if (loadFromTempFile && tempFile != null) {
                    File dest = fileManager.getOutputMediaFile();
                    try {
                        fileManager.copyFileUsingStream(tempFile, dest);
                        if (isSave) {
                            fileManager.sendBroadcastScanFile(dest);
                        } else {
                            if (NoticeImageExportedCropDialog.this.getActivity() != null)
                                NoticeImageExportedCropDialog.this.getActivity().runOnUiThread(pDialog::dismiss);
                            shareImage(dest);
                            SharePreferentUtils.insertImagePathHaveToRemove(dest.getPath());
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String fileName;
                    if (isSave)
                        fileManager.storeImage(resultBitmap);
                    else {
                        fileName = fileManager.storeImageWithoutBroadcast(resultBitmap);
                        if (NoticeImageExportedCropDialog.this.getActivity() != null)
                            NoticeImageExportedCropDialog.this.getActivity().runOnUiThread(pDialog::dismiss);
                        shareImage(new File(fileName));
                        return;
                    }
                }
                if (NoticeImageExportedCropDialog.this.getActivity() != null)
                    NoticeImageExportedCropDialog.this.getActivity().runOnUiThread(() -> pDialog
                            .setTitleText(getString(R.string.saved))
                            .setContentText(getString(R.string.image_saved))
                            .setConfirmText(getString(R.string.str_ok))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    pDialog.dismiss();
                                    NoticeImageExportedCropDialog.this.dismiss();
                                }
                            })
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE));
            }
        });
    }

    private void shareImage(File file) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                FileProvider.getUriForFile(getContext(), getContext().getPackageName(), file) : Uri.fromFile(file));
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_video)));
    }

    private void crop() {
        if (tempFile == null) {
            tempFile = new FileManager(getActivity()).storeImageOnPrivateStorage(resultBitmap);
        }

//        progressBar.setVisibility(View.VISIBLE);
        cropImageView.setVisibility(View.VISIBLE);
        cropImageView.load(Uri.fromFile(tempFile)).execute(new LoadCallback() {
            @Override
            public void onSuccess() {
                imageView.setVisibility(View.GONE);
//                progressBar.setVisibility(View.GONE);
                setListenerCrop();
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    private void setListenerCrop() {
        cropImageView.setCropMode(CropImageView.CropMode.FREE);
        txtSave.setVisibility(View.GONE);
        txtCancel.setOnClickListener(v -> {
            cropImageView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            txtSave.setVisibility(View.VISIBLE);
            resetButtonListener();
        });
        txtCrop.setOnClickListener(v -> {
//            progressBar.setVisibility(View.VISIBLE);
            cropImageView.crop(Uri.fromFile(tempFile))
                    .execute(new CropCallback() {
                        @Override
                        public void onSuccess(Bitmap cropped) {
                            cropImageView.save(cropped)
                                    .execute(Uri.fromFile(tempFile), new SaveCallback() {
                                        @Override
                                        public void onSuccess(Uri uri) {
//                                            progressBar.setVisibility(View.GONE);
                                            cropImageView.setVisibility(View.GONE);

                                            imageView.setVisibility(View.VISIBLE);
                                            loadFromTempFile = true;
                                            Glide.with(imageView)
                                                    .load(tempFile)
                                                    .signature(new ObjectKey(new Date().getTime()))
                                                    .into(imageView);

                                            txtSave.setVisibility(View.VISIBLE);
                                            resetButtonListener();
                                        }

                                        @Override
                                        public void onError(Throwable e) {
//                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        }

                        @Override
                        public void onError(Throwable e) {
//                            progressBar.setVisibility(View.GONE);
                        }
                    });
        });
    }

    private void resetButtonListener() {
        txtSave.setOnClickListener(v -> saveOrShare());
        txtCrop.setOnClickListener(v -> crop());
        txtCancel.setOnClickListener(v -> cancel());
    }

    private void cancel() {
        dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        resultBitmap = null;
        if (tempFile != null)
            SharePreferentUtils.insertImagePathHaveToRemove(tempFile.getPath());
    }
}