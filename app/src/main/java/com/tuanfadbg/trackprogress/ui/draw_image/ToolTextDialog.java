package com.tuanfadbg.trackprogress.ui.draw_image;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;

import com.tuanfadbg.trackprogress.beforeafterimage.R;

import top.defaults.colorpicker.ColorPickerView;

public class ToolTextDialog extends Dialog {

    private Activity activity;

    public ToolTextDialog(Activity a, int initColor) {
        super(a);
        this.activity = a;
        this.initColor = initColor;
    }


    private String text;
    private int initColor;
    private EditText edt;
    private ColorPickerView colorPickerView;
    private OnToolTextListener onToolTextListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_tool_text);
        colorPickerView = findViewById(R.id.colorPicker);
        edt = findViewById(R.id.edt);
        colorPickerView.setInitialColor(initColor);
        colorPickerView.subscribe((color, fromUser, shouldPropagate) -> initColor = color);


        findViewById(R.id.txt_cancel).setOnClickListener(v -> onToolTextListener.onCancel());

        findViewById(R.id.txt_ok).setOnClickListener(v -> onToolTextListener.onOK(edt.getText().toString().trim(), initColor));
    }

    public void setOnToolTextListener(OnToolTextListener onToolTextListener) {
        this.onToolTextListener = onToolTextListener;
    }

    public interface OnToolTextListener {
        void onOK(String text, int color);

        void onCancel();
    }
}
