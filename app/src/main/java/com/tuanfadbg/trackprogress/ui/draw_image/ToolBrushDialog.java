package com.tuanfadbg.trackprogress.ui.draw_image;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.SeekBar;

import com.tuanfadbg.trackprogress.beforeafterimage.R;

import top.defaults.colorpicker.ColorPickerView;

public class ToolBrushDialog extends Dialog {

    private Activity activity;

    public ToolBrushDialog(Activity a, int initColor, int brushSize) {
        super(a);
        this.activity = a;
        this.initColor = initColor;
        this.brushSize = brushSize;
    }

    private int initColor, brushSize;
    private SeekBar seekBarBrush;
    private ColorPickerView colorPickerView;
    private OnToolBrushListener onToolBrushListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_tool_brush);
        colorPickerView = findViewById(R.id.colorPicker);
        seekBarBrush = findViewById(R.id.seekBar);
        seekBarBrush.setMax(100);
        seekBarBrush.setProgress(brushSize);
        colorPickerView.setInitialColor(initColor);
        colorPickerView.subscribe((color, fromUser, shouldPropagate) -> initColor = color);


        findViewById(R.id.txt_cancel).setOnClickListener(v -> onToolBrushListener.onCancel());

        findViewById(R.id.txt_ok).setOnClickListener(v -> onToolBrushListener.onOK(initColor, seekBarBrush.getProgress()));
    }

    public void setOnToolBrushListener(OnToolBrushListener onToolBrushListener) {
        this.onToolBrushListener = onToolBrushListener;
    }

    public interface OnToolBrushListener {
        void onOK(int color, int brush);

        void onCancel();
    }
}
