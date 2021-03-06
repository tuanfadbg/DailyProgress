package com.tuanfadbg.trackprogress.ui.side_by_side;

import android.Manifest;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.ortiz.touchview.TouchImageView;
import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.item.Item;
import com.tuanfadbg.trackprogress.database.item.ItemSelectAsyncTask;
import com.tuanfadbg.trackprogress.ui.MainActivity;
import com.tuanfadbg.trackprogress.ui.notice_image_export_crop.NoticeImageExportedCropDialog;
import com.tuanfadbg.trackprogress.ui.select_image.SelectImageDialog;
import com.tuanfadbg.trackprogress.utils.FileManager;
import com.tuanfadbg.trackprogress.utils.Logger;
import com.tuanfadbg.trackprogress.utils.RotateTransformation;
import com.tuanfadbg.trackprogress.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private ConstraintLayout ctRotateControl, ctGridLeft, ctGridRight;
    private TextView txtRotateValue, txtRotateCancel, txtRotateDone;
    private SeekBar seekBarRotate;

    private int tagId;
    private Item itemLeft;
    private Item itemRight;
    private int viewType = VIEW_TYPE_ZOOM_EACH_IMAGE;

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

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_side_by_side, container, false);
        this.view = view;
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

        imageView.setDoubleTapScale(10f);
        imageView1.setDoubleTapScale(10f);
        imageView2.setDoubleTapScale(10f);

        imgSettings = view.findViewById(R.id.img_settings);
        progressBar = view.findViewById(R.id.progressBar);

        ctRotateControl = view.findViewById(R.id.ct_rotate_control);
        ctGridLeft = view.findViewById(R.id.ct_grid_left);
        ctGridRight = view.findViewById(R.id.ct_grid_right);

        txtRotateValue = view.findViewById(R.id.txt_rotate_value);
        txtRotateCancel = view.findViewById(R.id.txt_rotate_cancel);
        txtRotateDone = view.findViewById(R.id.txt_rotate_done);
        seekBarRotate = view.findViewById(R.id.seekbar_rotate);

        if (items != null) {
            itemLeft = items.get(items.size() - 1);
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

            txtRotateLeft.setVisibility(View.VISIBLE);
            txtRotateRight.setVisibility(View.VISIBLE);
        } else if (viewType == VIEW_TYPE_COMBINE_ONE) {
            imgSettings.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_combine_one_image));
            Glide.with(imageView).load(resultbitmap).signature(new ObjectKey(new Date().getTime())).into(imageView);

            imageView.setVisibility(View.VISIBLE);
            imageView1.setVisibility(View.GONE);
            imageView2.setVisibility(View.GONE);
            txtRotateLeft.setVisibility(View.GONE);
            txtRotateRight.setVisibility(View.GONE);
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
        view.findViewById(R.id.ic_share).setOnClickListener(v -> saveOrShare(false));
        view.findViewById(R.id.ic_save).setOnClickListener(v -> saveOrShare(true));
        view.findViewById(R.id.img_settings).setOnClickListener(v -> setting());

        view.findViewById(R.id.img_select_right).setOnClickListener(v -> selectImageRight());
        view.findViewById(R.id.img_select_left).setOnClickListener(v -> selectImageLeft());

        txtRotateLeft.setOnClickListener(v -> rotateImageOnTheLeft());
        txtRotateRight.setOnClickListener(v -> rotateImageOnTheRight());

        ctRotateControl.setOnClickListener(v -> {
        });

        txtRotateDone.setOnClickListener(v -> {
            resetRotateControl();
        });

        txtRotateCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctGridLeft.getVisibility() == View.VISIBLE) {
                    imgLeftRotate = previousLeftRotationIfCancel;
                    resetRotateControl();
                    imageView1.setRotation(0f);
                    if (viewType == VIEW_TYPE_ZOOM_EACH_IMAGE) {
                        reloadImageLeft();
                    } else {
                        createBitmap();
                    }
                } else {
                    imgRightRotate = previousRightRotationIfCancel;
                    resetRotateControl();
                    imageView2.setRotation(0f);
                    if (viewType == VIEW_TYPE_ZOOM_EACH_IMAGE) {
                        reloadImageRight();
                    } else {
                        createBitmap();
                    }
                }

            }
        });
    }

    private void resetRotateControl() {
        imgSettings.setVisibility(View.VISIBLE);
        ctGridLeft.setVisibility(View.GONE);
        ctGridRight.setVisibility(View.GONE);
        txtRotateLeft.setVisibility(View.VISIBLE);
        txtRotateRight.setVisibility(View.VISIBLE);
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

    float previousLeftRotation;
    float previousLeftRotationIfCancel;

    private void rotateImageOnTheLeft() {
        ctGridLeft.setVisibility(View.VISIBLE);
        setLayoutRotate();
        seekBarRotate.setProgress((int) imgLeftRotate + 180);
        previousLeftRotation = seekBarRotate.getProgress();
        previousLeftRotationIfCancel = seekBarRotate.getProgress() - 180;
        txtRotateValue.setText(String.format(Locale.US, "%d°", (int) imgLeftRotate));
        seekBarRotate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                imgLeftRotate = progress - 180f;
                txtRotateValue.setText(String.format(Locale.US, "%d°", (int) imgLeftRotate));
                imageView1.setRotation(progress - previousLeftRotation);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                imageView1.setRotation(0f);
                previousLeftRotation = seekBarRotate.getProgress();
                if (viewType == VIEW_TYPE_ZOOM_EACH_IMAGE) {
                    reloadImageLeft();
                } else {
                    createBitmap();
                }
            }
        });
    }

    float previousRightRotation;

    float previousRightRotationIfCancel;
    private void rotateImageOnTheRight() {
        ctGridRight.setVisibility(View.VISIBLE);
        setLayoutRotate();
        seekBarRotate.setProgress((int) imgRightRotate + 180);
        previousRightRotation = seekBarRotate.getProgress();
        previousRightRotationIfCancel = seekBarRotate.getProgress() - 180;
        txtRotateValue.setText(String.format(Locale.US, "%d°", (int) imgRightRotate));
        seekBarRotate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                imgRightRotate = progress - 180f;
                txtRotateValue.setText(String.format(Locale.US, "%d°", (int) imgRightRotate));
                imageView2.setRotation(progress - previousRightRotation);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                imageView2.setRotation(0f);
                previousRightRotation = seekBarRotate.getProgress();
                if (viewType == VIEW_TYPE_ZOOM_EACH_IMAGE) {
                    reloadImageRight();
                } else {
                    createBitmap();
                }
            }
        });
    }

    private void setLayoutRotate() {
        imgSettings.setVisibility(View.GONE);
        txtRotateLeft.setVisibility(View.GONE);
        txtRotateRight.setVisibility(View.GONE);
        ctRotateControl.setVisibility(View.VISIBLE);
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

    private void saveOrShare(boolean isSave) {
        if (FileManager.isWriteStoragePermissionGranted(getActivity())) {
            if (viewType == VIEW_TYPE_ZOOM_EACH_IMAGE) {
                SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.blue));
                pDialog.setTitleText(getString(R.string.loading));
                pDialog.setCancelable(false);
                pDialog.show();

                AsyncTask.execute(() -> {
                    Bitmap mergeBitmap = mergeBitmap2(itemLeft, itemRight);
                    if (SideBySideDialog.this.getActivity() != null)
                        SideBySideDialog.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pDialog.dismiss();
                                if (isSave)
                                    NoticeImageExportedCropDialog.showSaveDialog(getFragmentManager(), mergeBitmap);
                                else
                                    NoticeImageExportedCropDialog.showShareDialog(getFragmentManager(), mergeBitmap);
                            }
                        });
                });
            } else {
                if (isSave)
                    NoticeImageExportedCropDialog.showSaveDialog(getFragmentManager(), resultbitmap);
                else
                    NoticeImageExportedCropDialog.showShareDialog(getFragmentManager(), resultbitmap);
            }
        } else {
            ((MainActivity) getActivity()).setOnPermissionGranted(new MainActivity.OnPermissionGranted() {
                @Override
                public void onPermissionGranted() {
                    saveOrShare(isSave);
                }
            });
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_REQUEST_CODE);
        }
    }

    public Bitmap mergeBitmap2(Item itemLeft, Item itemRight) {

        RectF rectLeftZoomRatio = imageView1.getZoomedRect();
        RectF rectRightZoomRatio = imageView2.getZoomedRect();


        Logger.e("save 1: " + rectLeftZoomRatio.toShortString());
        Logger.e("save 2: " + rectRightZoomRatio.toShortString());

        Bitmap left, right;
        left = loadImageFromStorage(itemLeft.file, imgLeftRotate);
        right = loadImageFromStorage(itemRight.file, imgRightRotate);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Bitmap comboBitmap;
        int width, height;

        int leftWidth = (int) ((rectLeftZoomRatio.right - rectLeftZoomRatio.left) * left.getWidth());
        int rightWidth = (int) ((rectRightZoomRatio.right - rectRightZoomRatio.left) * right.getWidth());

        Logger.e(leftWidth + " " + rightWidth);
        if (leftWidth > rightWidth)
            width = leftWidth * 2;
        else {
            width = rightWidth * 2;
        }

        int leftHeight = (int) ((rectLeftZoomRatio.bottom - rectLeftZoomRatio.top) * left.getHeight());
        int rightHeight = (int) ((rectRightZoomRatio.bottom - rectRightZoomRatio.top) * right.getHeight());
//        Logger.e(leftHeight + " " + rightHeight);
//
//        if (leftHeight > rightHeight)
//            height = leftHeight;
//        else {
//            height = rightHeight;
//        }

        int calculateHeight1 = (int) ((float) width / (float) leftWidth * (float) leftHeight / 2f);
        int calculateHeight2 = (int) ((float) width / (float) rightWidth * (float) rightHeight / 2f);

        if (calculateHeight1 > calculateHeight2) {
            height = calculateHeight1;
        } else {
            height = calculateHeight2;
        }


        Rect rect1 = new Rect(0, 0, width / 2, calculateHeight1);
        Rect rect2 = new Rect(width / 2, 0, width, calculateHeight2);

        if (height > calculateHeight1) {
            rect1.top = (height - calculateHeight1) / 2;
            rect1.bottom = rect1.top + calculateHeight1;
        }

        if (height > calculateHeight2) {
            rect2.top = (height - calculateHeight2) / 2;
            rect2.bottom = rect2.top + calculateHeight2;
        }

        comboBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        Canvas canvas = new Canvas(comboBitmap);
        canvas.drawPaint(paint);

        Bitmap leftBitmapZoom = Bitmap.createBitmap(
                left,
                (int) (left.getWidth() * rectLeftZoomRatio.left),
                (int) (left.getHeight() * rectLeftZoomRatio.top),
                (int) (left.getWidth() * (rectLeftZoomRatio.right - rectLeftZoomRatio.left)),
                (int) (left.getHeight() * (rectLeftZoomRatio.bottom - rectLeftZoomRatio.top))
        );

        canvas.drawBitmap(leftBitmapZoom, null, rect1, null);

        Bitmap rightBitmapZoom = Bitmap.createBitmap(
                right,
                (int) (right.getWidth() * rectRightZoomRatio.left),
                (int) (right.getHeight() * rectRightZoomRatio.top),
                (int) (right.getWidth() * (rectRightZoomRatio.right - rectRightZoomRatio.left)),
                (int) (right.getHeight() * (rectRightZoomRatio.bottom - rectRightZoomRatio.top))
        );

        canvas.drawBitmap(rightBitmapZoom, null, rect2, null);

        comboBitmap = Bitmap.createScaledBitmap(comboBitmap, width, height, true);
        return comboBitmap;
    }

//    private void share() {
//        if (FileManager.isWriteStoragePermissionGranted(getActivity())) {
//            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
//            sweetAlertDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.blue));
//            sweetAlertDialog.setTitle(R.string.loading);
//            sweetAlertDialog.show();
//            AsyncTask.execute(() -> {
//                FileManager fileManager = new FileManager(getActivity());
//                String fileName = fileManager.storeImageWithoutBroadcast(resultbitmap);
//                SharePreferentUtils.insertImagePathHaveToRemove(fileName);
//
//                shareImage(new File(fileName));
//                if (SideBySideDialog.this.getActivity() != null)
//                    SideBySideDialog.this.getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            sweetAlertDialog.dismissWithAnimation();
//                        }
//                    });
//            });
//        } else {
//            ((MainActivity) getActivity()).setOnPermissionGranted(new MainActivity.OnPermissionGranted() {
//                @Override
//                public void onPermissionGranted() {
//                    share();
//                }
//            });
//            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_REQUEST_CODE);
//        }
//    }

//    private void shareImage(File file) {
//        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND);
//        shareIntent.putExtra(Intent.EXTRA_STREAM, Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
//                FileProvider.getUriForFile(getContext(), getContext().getPackageName(), file) : Uri.fromFile(file));
//        shareIntent.setType("image/*");
//        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_video)));
//    }

    private SelectImageDialog selectImageDialog;

    private void selectImageRight() {
        selectImageDialog = new SelectImageDialog(tagId, item -> {
            if (selectImageDialog != null)
                selectImageDialog.dismiss();
            imgRightRotate = 0f;
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
            imgLeftRotate = 0f;
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