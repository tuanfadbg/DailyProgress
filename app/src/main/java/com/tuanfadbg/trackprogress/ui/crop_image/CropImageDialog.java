package com.tuanfadbg.trackprogress.ui.crop_image;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.tuanfadbg.trackprogress.beforeafterimage.R;

import java.io.File;

public class CropImageDialog extends DialogFragment {

    private static final String TAG = CropImageDialog.class.getSimpleName();
    private CropImageView cropImageView;
    private File source, dest;
    private OnCropListener onCropListener;

    public static void show(FragmentManager fragmentManager, File source, File dest, OnCropListener onCropListener) {
        CropImageDialog dialog = new CropImageDialog();
        dialog.source = source;
        dialog.dest = dest;
        dialog.onCropListener = onCropListener;
        dialog.show(fragmentManager, CropImageDialog.class.getSimpleName());
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
        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_crop_image, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cropImageView = view.findViewById(R.id.crop_image_view);
        RecyclerView rcvRatio = view.findViewById(R.id.rcv_ratio);

        RatioAdapter ratioAdapter = new RatioAdapter(new RatioAdapter.OnRatioSelected() {
            @Override
            public void onSelected(RatioData ratioData, int position) {
                cropImageView.setCropMode(ratioData.ratioValue);
            }
        });
        rcvRatio.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        rcvRatio.setAdapter(ratioAdapter);

        cropImageView.setCropMode(CropImageView.CropMode.FREE);
        cropImageView.load(Uri.fromFile(source)).execute(new LoadCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Throwable e) {

            }
        });
        setListener(view);
    }

    private void setListener(View view) {
        view.findViewById(R.id.img_rotate_left).setOnClickListener(v -> rotateLeft());
        view.findViewById(R.id.img_rotate_right).setOnClickListener(v -> rotateRight());
        view.findViewById(R.id.txt_crop).setOnClickListener(v -> crop());
        view.findViewById(R.id.img_back).setOnClickListener(v -> back());
    }

    private void rotateLeft() {
        cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
    }

    private void rotateRight() {
        cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
    }

    private void crop() {
        cropImageView.crop(Uri.fromFile(source))
                .execute(new CropCallback() {
                    @Override
                    public void onSuccess(Bitmap cropped) {
                        cropImageView.save(cropped)
                                .execute(Uri.fromFile(dest), new SaveCallback() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        onCropListener.onCrop(dest);
                                        dismiss();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                                        dismiss();

                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void back() {
        onCropListener.cancel();
        dismiss();
    }

    public interface OnCropListener {

        public void onCrop(File dest);

        public void cancel();
    }
}