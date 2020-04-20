package com.tuanfadbg.progress.ui.side_by_side;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.item.Item;
import com.tuanfadbg.progress.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditSideBySideDialog extends DialogFragment {

    public static final String TAG = EditSideBySideDialog.class.getSimpleName();

    private EditText edtTitle, edtTime1, edtTime2;
    private Spinner spinner;
    private String title, time1, time2;
    private int style;
    private OnSettingChange onSettingChange;
    public EditSideBySideDialog(String title, String time1, String time2, int style, OnSettingChange onSettingChange) {
        this.title = title;
        this.time1 = time1;
        this.time2 = time2;
        this.style = style;
        this.onSettingChange = onSettingChange;
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
        View view = inflater.inflate(R.layout.dialog_edit_side_by_side, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtTitle = view.findViewById(R.id.edt_title);
        edtTime1 = view.findViewById(R.id.edt_time1);
        edtTime2 = view.findViewById(R.id.edt_time2);
        spinner = view.findViewById(R.id.spinner_style);

        edtTitle.setText(title);
        edtTime1.setText(time1);
        edtTime2.setText(time2);

        final List<String> itemList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.style_list)));
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, itemList);

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(style);

        view.findViewById(R.id.txt_save).setOnClickListener(v -> {
            dismiss();
            onSettingChange.onNewSetting(edtTitle.getText().toString().trim(),
                    edtTime1.getText().toString().trim(),
                    edtTime2.getText().toString().trim(),
                    spinner.getSelectedItemPosition());
        });
        view.findViewById(R.id.txt_cancel).setOnClickListener(v -> dismiss());
    }

    public interface OnSettingChange {
        void onNewSetting(String title, String time1, String time2, int style);
    }
}