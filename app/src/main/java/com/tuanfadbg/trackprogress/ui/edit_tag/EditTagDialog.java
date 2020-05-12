package com.tuanfadbg.trackprogress.ui.edit_tag;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.Data;
import com.tuanfadbg.trackprogress.database.OnUpdateDatabase;
import com.tuanfadbg.trackprogress.database.tag.Tag;
import com.tuanfadbg.trackprogress.database.tag.TagSelectAllAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.TagUpdateAsyncTask;

import java.util.ArrayList;
import java.util.List;

public class EditTagDialog extends DialogFragment {

    public static final String TAG = EditTagDialog.class.getSimpleName();

    private EditText editText;
    private OnEditTagListener onEditTagListener;
    private Tag currentTag;
    private List<Tag> tags;

    public EditTagDialog(Tag tag, OnEditTagListener onEditTagListener) {
        this.currentTag = tag;
        this.onEditTagListener = onEditTagListener;
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
        View view = inflater.inflate(R.layout.dialog_edit_tag, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editText = view.findViewById(R.id.editText);
        editText.setText(currentTag.name);

        TagSelectAllAsyncTask tagSelectAsyncTask = new TagSelectAllAsyncTask(getContext());
        tagSelectAsyncTask.execute(tags -> {
            this.tags = tags;
            if (tags == null)
                this.tags = new ArrayList<>();
        });

        view.findViewById(R.id.txt_save).setOnClickListener(v -> save());
        view.findViewById(R.id.txt_delete).setOnClickListener(v -> dismiss());

        view.setOnClickListener(v -> dismiss());
        view.findViewById(R.id.constraintLayout).setOnClickListener(v -> {

        });
    }

    private void save() {
        String newTagName = editText.getText().toString().trim();
        if (TextUtils.isEmpty(newTagName)) {
            editText.setError(getString(R.string.error_tag_name_empty));
        } else {
            for (int i = 0; i < tags.size(); i++) {
                if (newTagName.toLowerCase().equals(tags.get(i).name.toLowerCase()) && tags.get(i).uid != currentTag.uid) {
                    editText.setError(getString(R.string.error_tag_name_duplicate));
                    return;
                }
            }

            currentTag.name = newTagName;
            TagUpdateAsyncTask tagUpdateAsyncTask = new TagUpdateAsyncTask(getContext());
            tagUpdateAsyncTask.execute(new Data(currentTag, new OnUpdateDatabase() {
                        @Override
                        public void onSuccess() {
                            onEditTagListener.onEdited(currentTag);
                            dismiss();
                        }

                        @Override
                        public void onFail() {
                            Toast.makeText(getContext(), getString(R.string.unknown_error), Toast.LENGTH_LONG).show();

                        }
                    }));
        }
    }

    public interface OnEditTagListener {
        void onEdited(Tag tag);
    }
}