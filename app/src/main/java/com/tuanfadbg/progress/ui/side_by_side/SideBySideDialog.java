package com.tuanfadbg.progress.ui.side_by_side;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.Data;
import com.tuanfadbg.progress.database.OnUpdateDatabase;
import com.tuanfadbg.progress.database.item.Item;
import com.tuanfadbg.progress.database.item.ItemSelectAsyncTask;
import com.tuanfadbg.progress.database.tag.Tag;
import com.tuanfadbg.progress.database.tag.TagInsertAsyncTask;
import com.tuanfadbg.progress.database.tag.TagSelectAllAsyncTask;
import com.tuanfadbg.progress.database.tag.TagSelectNewestAsyncTask;
import com.tuanfadbg.progress.utils.FileManager;
import com.tuanfadbg.progress.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SideBySideDialog extends DialogFragment {

    public static final String TAG = SideBySideDialog.class.getSimpleName();

    private ImageView imageView;
    private Item item;
    private List<Item> items;
    private String title, time1, time2;
    private int style;
    private Bitmap resultbitmap;

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
        View view = inflater.inflate(R.layout.dialog_side_by_side, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.imageView);
        if (items != null) {
            initData();
            createBitmap();
        } else {

        }
//        ItemSelectAsyncTask itemSelectAsyncTask
//                = new ItemSelectAsyncTask(getContext());
//        itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, tagId, datas -> {
//            this.datas = datas;
//            if (imgGrid.isSelected()) {
//                dataGridAdapter.setData(datas);
//            } else {
//                timelineListAdapter.setData(datas);
//            }
//            if (datas.size() > 0) {
//                imgEmpty.setVisibility(View.GONE);
//            } else {
//                imgEmpty.setVisibility(View.VISIBLE);
//            }
//        }));

//        Bitmap bitmap1 = loadImageFromStorage();
//        Glide.with(imageView).load(mergeBitmap())

        setListener(view);
    }

    private void initData() {
        title = "";
        time1 = Utils.getTimeFromLong(items.get(0).createAt);
        time2 = Utils.getTimeFromLong(items.get(items.size() - 1).createAt);
        style = 3;
    }

    private void createBitmap() {
        resultbitmap = mergeBitmap(items.get(0), items.get(items.size() - 1));
        Glide.with(imageView).load(resultbitmap).into(imageView);
    }

    public Bitmap mergeBitmap(Item item1, Item item2) {
        Bitmap fr, sc;
        fr = loadImageFromStorage(items.get(0).file);
        sc = loadImageFromStorage(items.get(items.size() - 1).file);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Bitmap comboBitmap;
        int width, height;

        width = fr.getWidth() + sc.getWidth();
        height = fr.getHeight();

        int heightScreen = displayMetrics.heightPixels;
        int widthScreen = displayMetrics.widthPixels;
        int textsize = (int) Utils.convertDpToPixel(13, getContext());
        textsize = textsize * width / widthScreen;
        float imagePaddingSide = 0f;
        float imagePaddingTop = 0f;
        float imagePaddingBottom = 0f;
        float divider = 0f;
        switch (style) {
            /*
            <item>No text</item>
            <item>Date blow</item>
            <item>White outline</item>
            <item>Instagram</item>
            **/
            case 0: {
                break;
            }
            case 1: {
//                if (TextUtils.isEmpty(title)) {
                imagePaddingTop = 0f;
//                } else {
//                    imagePaddingTop = textsize * 2;
//                }
                imagePaddingBottom = (float) (textsize * 1.5);
                break;
            }
            case 2: {
                imagePaddingSide = textsize;
                imagePaddingTop = textsize * 2;
                divider = (float) (textsize * 0.5);
                imagePaddingBottom = (float) (textsize * 1.5);
                break;
            }

            case 3: {
                imagePaddingSide = textsize;
                imagePaddingTop = textsize * 2;
                imagePaddingBottom = (float) (textsize * 1.5);
                break;
            }
        }
        height += imagePaddingTop + imagePaddingBottom;
        width += imagePaddingSide * 2 + divider;
        comboBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        Canvas canvas = new Canvas(comboBitmap);
        canvas.drawPaint(paint);
        canvas.drawBitmap(fr, imagePaddingSide, imagePaddingTop, null);
        canvas.drawBitmap(sc, fr.getWidth() + imagePaddingSide + divider, imagePaddingTop, null);

        Typeface tf = ResourcesCompat.getFont(getContext(), R.font.lato_regular);

//        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(textsize);
        paint.setTypeface(tf);

        if (!TextUtils.isEmpty(title) && (style == 2 || style == 3)) {
            Rect boundTitle = new Rect();
            paint.getTextBounds(title, 0, title.length(), boundTitle);
            canvas.drawText(title, (canvas.getWidth() - boundTitle.right) / 2, imagePaddingTop - (imagePaddingTop - textsize) / 2, paint);
        }
        if (style != 0) {
            Rect bound1 = new Rect();
            paint.getTextBounds(time1, 0, time1.length(), bound1);
            Rect bound2 = new Rect();
            paint.getTextBounds(time2, 0, time2.length(), bound2);

            canvas.drawText(time1, (fr.getWidth() - bound1.right) / 2 + imagePaddingSide, (float) (height - textsize * 0.3), paint);
            canvas.drawText(time2, (sc.getWidth() - bound2.right) / 2 + fr.getWidth() + imagePaddingSide * 2, (float) (height - textsize * 0.3), paint);
        }

        int newImageHeight;
        int newImageWidth;
        int maxSide = 2000;
        if (height > width) {
            float scale = (float) maxSide / height;
            newImageHeight = maxSide;
            newImageWidth = (int) (scale * (float) width);
        } else {
            float scale = (float) maxSide / width;
            newImageWidth = maxSide;
            newImageHeight = (int) (scale * (float) height);
        }
        comboBitmap = Bitmap.createScaledBitmap(comboBitmap, newImageWidth, newImageHeight, true);
        return comboBitmap;
    }

    private Bitmap loadImageFromStorage(String path) {
        try {
            File f = new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setListener(View view) {
        view.findViewById(R.id.ic_edit).setOnClickListener(v -> {
            EditSideBySideDialog editSideBySideDialog
                    = new EditSideBySideDialog(title, time1, time2, style,
                    (title, time1, time2, style) -> {
                        SideBySideDialog.this.title = title;
                        SideBySideDialog.this.time1 = time1;
                        SideBySideDialog.this.time2 = time2;
                        SideBySideDialog.this.style = style;
                        createBitmap();
                    });
            editSideBySideDialog.show(getFragmentManager(), EditSideBySideDialog.class.getSimpleName());
        });

        view.findViewById(R.id.img_back).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.ic_share).setOnClickListener(v -> share());
        view.findViewById(R.id.ic_save).setOnClickListener(v -> save());
        view.findViewById(R.id.img_settings).setOnClickListener(v -> setting());
    }

    private void setting() {

    }

    private void save() {
        SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(getString(R.string.loading));
        pDialog.setCancelable(false);
        pDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            pDialog
                    .setTitleText(getString(R.string.saved))
                    .setContentText(getString(R.string.image_saved))
                    .setConfirmText(getString(R.string.dialog_ok))
                    .setConfirmClickListener(null)
                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        }, 500);

        AsyncTask.execute(() -> {
            FileManager qrFileManager = new FileManager(getActivity());
            String imagePath = qrFileManager.storeImage(resultbitmap);
        });

    }


    private void share() {

    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}