package com.tuanfadbg.progress.ui.edit_name;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.Data;
import com.tuanfadbg.progress.database.OnUpdateDatabase;
import com.tuanfadbg.progress.database.item.Item;
import com.tuanfadbg.progress.database.item.ItemInsertAsyncTask;
import com.tuanfadbg.progress.database.tag.Tag;
import com.tuanfadbg.progress.database.tag.TagSelectAllAsyncTask;
import com.tuanfadbg.progress.ui.add_tag.AddTagDialog;
import com.tuanfadbg.progress.utils.Constants;
import com.tuanfadbg.progress.utils.SharePreferentUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class EditNameDialog extends DialogFragment {
    private OnEditNameListener onEditNameListener;

    public EditNameDialog(OnEditNameListener onEditNameListener) {
        this.onEditNameListener = onEditNameListener;
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
        View view = inflater.inflate(R.layout.dialog_edit_name, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String name = (String) SharePreferentUtils.getSharedPreference(Constants.NAME, "");
        EditText edtName = view.findViewById(R.id.edt_name);
        edtName.setText(name);

        view.findViewById(R.id.txt_save).setOnClickListener(v -> {
            String newName = edtName.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                edtName.setError("");
                return;
            }
            SharePreferentUtils.setSharedPreference(Constants.NAME, newName);
            save(newName);
        });

        view.findViewById(R.id.txt_delete).setOnClickListener(v -> dismiss());
    }

    private void save(String newName) {
        dismiss();
        onEditNameListener.onNewName(newName);
    }

    public interface OnEditNameListener {
        void onNewName(String newName);
    }
}