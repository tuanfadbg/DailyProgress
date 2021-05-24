package com.tuanfadbg.trackprogress.ui.edit_name;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.utils.SharePreferentUtils;

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
        String name = SharePreferentUtils.getName(false);
        EditText edtName = view.findViewById(R.id.edt_name);
        edtName.setText(name);

        view.findViewById(R.id.txt_save).setOnClickListener(v -> {
            String newName = edtName.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                edtName.setError("");
                return;
            }
            SharePreferentUtils.setName(newName);
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