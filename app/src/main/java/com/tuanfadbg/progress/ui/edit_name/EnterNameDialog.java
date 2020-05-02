package com.tuanfadbg.progress.ui.edit_name;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.utils.SharePreferentUtils;

public class EnterNameDialog extends DialogFragment {
    private OnEnterNameListener onEnterNameListener;

    public EnterNameDialog(OnEnterNameListener onEditNameListener) {
        this.onEnterNameListener = onEditNameListener;
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
        View view = inflater.inflate(R.layout.dialog_enter_name, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText edtName = view.findViewById(R.id.edt_name);

        view.findViewById(R.id.txt_save).setOnClickListener(v -> {
            String newName = edtName.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                edtName.setError("");
                return;
            }
            SharePreferentUtils.setName(newName);
            save(newName);
        });

        edtName.requestFocus();
    }

    private void save(String newName) {
        dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        onEnterNameListener.onNewName();
    }

    public interface OnEnterNameListener {
        void onNewName();
    }
}