package com.tuanfadbg.progress.ui.image_view;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.ortiz.touchview.TouchImageView;
import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.item.Item;
import com.tuanfadbg.progress.database.item.ItemDeteleAsyncTask;
import com.tuanfadbg.progress.ui.image_note.ImageNoteDialog;
import com.tuanfadbg.progress.ui.side_by_side.SideBySideDialog;
import com.tuanfadbg.progress.utils.FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT;

public class ImageViewDialog extends DialogFragment {

    private static final String TAG = ImageNoteDialog.class.getSimpleName();
    private TouchImageView imageView;
    private Item item;
    private ConstraintLayout ctBottom;
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.imageView);
        ctBottom = view.findViewById(R.id.ct_bottom);
        txtTitle = view.findViewById(R.id.txt_title);

        txtTitle.setText(item.title);
        Glide.with(imageView).load(new File(item.file)).into(imageView);

        imageView.setOnClickListener(v -> {
            if (ctBottom.getVisibility() == View.VISIBLE)
                ctBottom.setVisibility(View.GONE);
            else
                ctBottom.setVisibility(View.VISIBLE);
        });

        view.findViewById(R.id.img_compare).setOnClickListener(v -> compare());
        view.findViewById(R.id.img_back).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.img_edit).setOnClickListener(v -> edit());
        view.findViewById(R.id.img_share).setOnClickListener(v -> share());
        view.findViewById(R.id.img_delete).setOnClickListener(v -> delete());
        if (!TextUtils.isEmpty(item.title))
            txtTitle.setOnClickListener(v -> viewMoreTitle());
    }

//    private List<MotionEvent> motionEvents = new ArrayList<>();
//    View.OnTouchListener onTouchListener = (v, event) -> {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN: {
//                motionEvents.add(event);
//                break;
//            }
//
//            case MotionEvent.ACTION_MOVE: {
//                motionEvents.add(event);
//                break;
//            }
//
//            case MotionEvent.ACTION_UP: {
//                motionEvents.add(event);
//
//                int lastest = motionEvents.size() - 1;
//                float deltaX = motionEvents.get(lastest).getX() - motionEvents.get(0).getX();
//                float deltaY = motionEvents.get(lastest).getY() - motionEvents.get(0).getY();
//                long deltaTime = motionEvents.get(lastest).getEventTime() - motionEvents.get(0).getEventTime();
//
//                if (deltaTime < 300) {
//                    if (Math.abs(deltaY) < Math.abs(deltaX)) {
//                        if (deltaX < 0) {
//                            swipeLeft();
//                        } else
//                            swipeRight();
//                    }
//                }
//                motionEvents.clear();
//                break;
//            }
//
//        }
//        return false;
//    };

//    private void swipeLeft() {
//        Log.e(TAG, "swipeLeft: " );
//    }
//
//    private void swipeRight() {
//        Log.e(TAG, "swipeRight: ");
//    }

    private void viewMoreTitle() {
        if (txtTitle.getMaxLines() < 10)
            txtTitle.setMaxLines(Integer.MAX_VALUE);
        else txtTitle.setMaxLines(2);
    }

    private void edit() {
        ImageNoteDialog imageNoteDialog = new ImageNoteDialog(item, new ImageNoteDialog.OnItemEditedListener() {
            @Override
            public void onEdited(Item item) {
                if (getActivity() == null)
                    return;
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE);
                sweetAlertDialog.setTitle(getString(R.string.saved));
                sweetAlertDialog.setContentText(getString(R.string.dialog_ok));
                sweetAlertDialog.show();
            }
        });

        imageNoteDialog.show(getFragmentManager(), ImageNoteDialog.class.getSimpleName());
    }

    private void compare() {
        SideBySideDialog sideBySideDialog = new SideBySideDialog(item);
        sideBySideDialog.show(getActivity().getSupportFragmentManager(), SideBySideDialog.class.getSimpleName());
    }

    private void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
        }
    }

    private void share() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.blue));
        sweetAlertDialog.setTitle(R.string.loading);
        sweetAlertDialog.show();

        FileManager fileManager = new FileManager(getActivity());
        File src = new File(item.file);
        fileManager.createFolder();
        File dest = fileManager.getOutputMediaFile();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    copyFileUsingStream(src, dest);
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