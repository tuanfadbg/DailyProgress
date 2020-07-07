package com.tuanfadbg.trackprogress.ui.side_by_side;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.ortiz.touchview.TouchImageView;
import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.item.Item;
import com.tuanfadbg.trackprogress.database.item.ItemSelectAsyncTask;
import com.tuanfadbg.trackprogress.ui.MainActivity;
import com.tuanfadbg.trackprogress.ui.select_image.SelectImageDialog;
import com.tuanfadbg.trackprogress.utils.FileManager;
import com.tuanfadbg.trackprogress.utils.RotateTransformation;
import com.tuanfadbg.trackprogress.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SideBySideDialog extends DialogFragment {

    public static final String TAG = SideBySideDialog.class.getSimpleName();
    private static final int WRITE_EXTERNAL_REQUEST_CODE = 1222;

    private static final int VIEW_TYPE_ZOOM_EACH_IMAGE = 0;
    private static final int VIEW_TYPE_COMBINE_ONE = 1;

    private TouchImageView imageView, imageView1, imageView2;
    TextView txtRotateLeft, txtRotateRight;
    private ImageView imgSettings;
    private Item item;
    private List<Item> items;
    private String title, time1, time2;
    private int style;
    private Bitmap resultbitmap;
    private ProgressBar progressBar;
    private int tagId;
    private Item itemLeft;
    private Item itemRight;
    private int viewType = VIEW_TYPE_COMBINE_ONE;

    private float imgLeftRotate = 0f;
    private float imgRightRotate = 0f;

    public SideBySideDialog(Item item) {
        this.item = item;
    }

    public SideBySideDialog(List<Item> items) {
        this.items = items;
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
        View view = inflater.inflate(R.layout.dialog_side_by_side, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.imageView);
        imageView1 = view.findViewById(R.id.imageView_1);
        imageView2 = view.findViewById(R.id.imageView_2);
        txtRotateLeft = view.findViewById(R.id.txt_rotate_left);
        txtRotateRight = view.findViewById(R.id.txt_rotate_right);

        imageView2 = view.findViewById(R.id.imageView_2);
        imageView.setMaxZoom(100f);
        imageView1.setMaxZoom(100f);
        imageView2.setMaxZoom(100f);
        imgSettings = view.findViewById(R.id.img_settings);
        progressBar = view.findViewById(R.id.progressBar);

        if (items != null) {
            itemLeft = items.get(items.size() - 1);;
            itemRight = items.get(0);
            tagId = itemLeft.tag;
            initData();
            createBitmap();
        } else if (item != null) {
            tagId = item.tag;
            ItemSelectAsyncTask itemSelectAsyncTask = new ItemSelectAsyncTask(getContext());
            itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, tagId, datas -> {
                this.items = datas;
                if (item.uid == items.get(0).uid) { // neu item la latest
                    itemRight = item;
                    itemLeft = items.get(items.size() - 1);
                } else {
                    itemLeft = item;
                    itemRight = items.get(0);
                }
                initData();
                createBitmap();
            }));
        }

        setListener(view);
    }

    private void initData() {
        title = "";
        style = 1;
        time1 = Utils.getTimeFromLong(itemLeft.createAt);
        time2 = Utils.getTimeFromLong(itemRight.createAt);
    }

    private void createBitmap() {
        AsyncTask.execute(() -> {
            SideBySideDialog.this.getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
            try {
                resultbitmap = mergeBitmap(itemLeft, itemRight);
                SideBySideDialog.this.getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    setImageByType();
                });
            } catch (Exception e) {

            }
        });
    }

    private void setImageByType() {
        if (viewType == VIEW_TYPE_ZOOM_EACH_IMAGE) {
            imgSettings.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_zoom_each_image));
            reloadImageLeft();
            reloadImageRight();

            imageView.setVisibility(View.GONE);
            imageView1.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.VISIBLE);
//
//            txtRotateLeft.setVisibility(View.VISIBLE);
//            txtRotateRight.setVisibility(View.VISIBLE);
        } else if (viewType == VIEW_TYPE_COMBINE_ONE) {
            imgSettings.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_combine_one_image));
            Glide.with(imageView).load(resultbitmap).signature(new ObjectKey(new Date().getTime())).into(imageView);

            imageView.setVisibility(View.VISIBLE);
            imageView1.setVisibility(View.GONE);
            imageView2.setVisibility(View.GONE);
//
//            txtRotateLeft.setVisibility(View.GONE);
//            txtRotateRight.setVisibility(View.GONE);
        }
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
        view.findViewById(R.id.img_select_right).setOnClickListener(v -> selectImageRight());
//        view.findViewById(R.id.img_right).setOnClickListener(v -> selectImageRight());

        view.findViewById(R.id.img_select_left).setOnClickListener(v -> selectImageLeft());
//        view.findViewById(R.id.img_left).setOnClickListener(v -> selectImageLeft());

        txtRotateLeft.setOnClickListener(v -> rotateImageOnTheLeft());
        txtRotateRight.setOnClickListener(v -> rotateImageOnTheRight());

    }

    private void rotateImageOnTheLeft() {
        imgLeftRotate = (imgLeftRotate + 90f) % 360f;
        if (viewType == VIEW_TYPE_ZOOM_EACH_IMAGE) {
            reloadImageLeft();
        } else {
            createBitmap();
        }
    }

    private void rotateImageOnTheRight() {
        imgRightRotate = (imgRightRotate + 90f) % 360f;
        if (viewType == VIEW_TYPE_ZOOM_EACH_IMAGE) {
            reloadImageRight();
        } else {
            createBitmap();
        }
    }

    private void reloadImageLeft() {
        Glide.with(getContext())
                .load(itemLeft.file)
                .signature(new ObjectKey(new Date().getTime()))
                .transform(new RotateTransformation(getContext(), imgLeftRotate))
                .into(imageView1);
    }

    private void reloadImageRight() {
        Glide.with(getContext())
                .load(itemRight.file)
                .signature(new ObjectKey(new Date().getTime()))
                .transform(new RotateTransformation(getContext(), imgRightRotate))
                .into(imageView2);
    }

    private void setting() {
        viewType = (viewType + 1) % 2;
        setImageByType();
    }

    private void save() {
        if (FileManager.isWriteStoragePermissionGranted(getActivity())) {
            SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.blue));
            pDialog.setTitleText(getString(R.string.loading));
            pDialog.setCancelable(false);
            pDialog.show();

            AsyncTask.execute(() -> {
                FileManager qrFileManager = new FileManager(getActivity());
                String imagePath = qrFileManager.storeImage(resultbitmap);
                if (SideBySideDialog.this.getActivity() != null)
                    SideBySideDialog.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pDialog
                                    .setTitleText(getString(R.string.saved))
                                    .setContentText(getString(R.string.image_saved))
                                    .setConfirmText(getString(R.string.str_ok))
                                    .setConfirmClickListener(null)
                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        }
                    });
            });
        } else {
            ((MainActivity) getActivity()).setOnPermissionGranted(new MainActivity.OnPermissionGranted() {
                @Override
                public void onPermissionGranted() {
                    save();
                }
            });
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_REQUEST_CODE);
        }
    }

    private void share() {
        if (FileManager.isWriteStoragePermissionGranted(getActivity())) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
            sweetAlertDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.blue));
            sweetAlertDialog.setTitle(R.string.loading);
            sweetAlertDialog.show();
            AsyncTask.execute(() -> {
                FileManager fileManager = new FileManager(getActivity());
                String fileName = fileManager.storeImageWithoutBroadcast(resultbitmap);
                shareImage(new File(fileName));
                if (SideBySideDialog.this.getActivity() != null)
                    SideBySideDialog.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
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

    private void shareImage(File file) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                FileProvider.getUriForFile(getContext(), getContext().getPackageName(), file) : Uri.fromFile(file));
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_video)));
    }

    private SelectImageDialog selectImageDialog;

    private void selectImageRight() {
        selectImageDialog = new SelectImageDialog(tagId, item -> {
            if (selectImageDialog != null)
                selectImageDialog.dismiss();
            itemRight = item;
            time2 = Utils.getTimeFromLong(itemRight.createAt);
            createBitmap();
        });
        selectImageDialog.show(getFragmentManager(), SelectImageDialog.class.getSimpleName());
    }

    private void selectImageLeft() {
        selectImageDialog = new SelectImageDialog(tagId, item -> {
            if (selectImageDialog != null)
                selectImageDialog.dismiss();
            itemLeft = item;
            time1 = Utils.getTimeFromLong(itemLeft.createAt);
            createBitmap();
        });
        selectImageDialog.show(getFragmentManager(), SelectImageDialog.class.getSimpleName());
    }

    public Bitmap mergeBitmap(Item itemLeft, Item itemRight) {
        Bitmap fr, sc;
        fr = loadImageFromStorage(itemLeft.file, imgLeftRotate);
        sc = loadImageFromStorage(itemRight.file, imgRightRotate);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Bitmap comboBitmap;
        int width, height;

        if (fr.getWidth() > sc.getWidth())
            width = fr.getWidth() * 2;
        else {
            width = sc.getWidth() * 2;
        }

        int caculateHeight1 = (int) ((float) width / (float) fr.getWidth() * (float) fr.getHeight() / 2f);
        int caculateHeight2 = (int) ((float) width / (float) sc.getWidth() * (float) sc.getHeight() / 2f);

        if (caculateHeight1 > caculateHeight2) {
            height = caculateHeight1;
        } else {
            height = caculateHeight2;
        }

        Rect rect1 = new Rect(0, 0, width / 2, caculateHeight1);
        Rect rect2 = new Rect(width / 2, 0, width, caculateHeight2);


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
                imagePaddingTop = 0f;
                imagePaddingBottom = (float) (textsize * 1.5);
                break;
            }
            case 2: {
                imagePaddingSide = textsize;
                imagePaddingTop = textsize * 2;
                imagePaddingBottom = (float) (textsize * 1.5);
                divider = (float) (textsize * 0.5);
                break;
            }

            case 3: {
                imagePaddingSide = textsize;
                imagePaddingTop = textsize * 2;
                imagePaddingBottom = (float) (textsize * 1.5);
                divider = 0;
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

        rect1.top += imagePaddingTop;
        rect1.bottom += imagePaddingTop;
        rect1.left += imagePaddingSide;
        rect1.right += imagePaddingSide;
        canvas.drawBitmap(fr, null, rect1, null);

        rect2.top += imagePaddingTop;
        rect2.bottom += imagePaddingTop;
        rect2.left += imagePaddingSide + divider;
        rect2.right += imagePaddingSide + divider;
        canvas.drawBitmap(sc, null, rect2, null);

        Typeface tf = ResourcesCompat.getFont(getContext(), R.font.lato_regular);

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

            canvas.drawText(time1, (width / 2 - bound1.right) / 2 + imagePaddingSide, (float) (height - textsize * 0.3), paint);
            canvas.drawText(time2, (width / 2 - bound2.right) / 2 + width / 2 + imagePaddingSide + divider, (float) (height - textsize * 0.3), paint);
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

    private Bitmap loadImageFromStorage(String path, float userRotate) {
        try {
            File f = new File(path);

            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            float rotation = getImageRotation(path) + userRotate;
            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.preRotate(rotation);
                return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
            } else {
                return b;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getImageRotation(String filePath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(orientation);

            return rotationInDegrees;
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }
}