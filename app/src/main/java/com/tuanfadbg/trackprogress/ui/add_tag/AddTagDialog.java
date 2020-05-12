package com.tuanfadbg.trackprogress.ui.add_tag;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.Data;
import com.tuanfadbg.trackprogress.database.OnUpdateDatabase;
import com.tuanfadbg.trackprogress.database.tag.Tag;
import com.tuanfadbg.trackprogress.database.tag.TagInsertAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.TagSelectAllAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.TagSelectNewestAsyncTask;

import java.util.ArrayList;
import java.util.List;

public class AddTagDialog extends DialogFragment {

    public static final String TAG = AddTagDialog.class.getSimpleName();

    public AddTagDialog() {

    }

    public AddTagDialog(boolean showSelectTag) {
        this.showSelectTag = showSelectTag;
    }

    private EditText editText;
    private ChipGroup chipGroup;
    private OnAddTagListener onAddTagListener;
    private boolean showSelectTag = true;

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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog_Animation_Up);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_add_tag, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editText = view.findViewById(R.id.editText);
        chipGroup = view.findViewById(R.id.rcv_tag);

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < tags.size(); i++) {
                if (checkedId == tags.get(i).uid) {
                    onAddTagListener.onTagSelected(tags.get(i));
                    dismiss();
                    return;
                }
            }
        });

        TagSelectAllAsyncTask tagSelectAsyncTask = new TagSelectAllAsyncTask(getContext());
        tagSelectAsyncTask.execute(tags -> {
            this.tags = tags;
            if (tags == null)
                tags = new ArrayList<>();

            if (showSelectTag) {
                if (tags.size() == 0) {
                    view.findViewById(R.id.txt_all_tag).setVisibility(View.GONE);
                    chipGroup.setVisibility(View.GONE);
                } else {
                    int size = tags.size();
                    if (size > 15)
                        size = 15;
                    for (int i = 0; i < size; i++) {
                        Chip chip = new Chip(getContext(), null, R.attr.CustomChipChoiceStyle);
                        chip.setId(tags.get(i).uid);
                        chip.setText(tags.get(i).name);
                        chipGroup.addView(chip, 0);
                    }
                }
            }
        });

        if (!showSelectTag) {
            view.findViewById(R.id.txt_all_tag).setVisibility(View.GONE);
            view.findViewById(R.id.rcv_tag).setVisibility(View.GONE);
        }

        view.findViewById(R.id.txt_save).setOnClickListener(v -> save());
        view.findViewById(R.id.txt_delete).setOnClickListener(v -> dismiss());

        view.setOnClickListener(v -> dismiss());
        view.findViewById(R.id.constraintLayout).setOnClickListener(v -> {

        });
    }

    private List<Tag> tags;

    private void save() {
        String newTagName = editText.getText().toString().trim();
        if (TextUtils.isEmpty(newTagName)) {
            editText.setError(getString(R.string.error_tag_name_empty));
        } else {
            for (int i = 0; i < tags.size(); i++) {
                if (newTagName.toLowerCase().equals(tags.get(i).name.toLowerCase())) {
                    editText.setError(getString(R.string.error_tag_name_duplicate));
                    return;
                }
            }

            TagInsertAsyncTask tagInsertAsyncTask = new TagInsertAsyncTask(getContext());
            Tag tag = new Tag(newTagName);
            tagInsertAsyncTask.execute(new Data(tag, new OnUpdateDatabase() {
                @Override
                public void onSuccess() {
                    if (onAddTagListener != null) {
                        TagSelectNewestAsyncTask tagSelectNewestAsyncTask = new TagSelectNewestAsyncTask(getContext());
                        tagSelectNewestAsyncTask.execute(tags -> onAddTagListener.onTagAdded(tags.get(0)));
                    }
                    dismiss();
                }

                @Override
                public void onFail() {
                    Toast.makeText(getContext(), getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                }
            }));
        }
    }

    public void setOnAddTagListener(OnAddTagListener onAddTagListener) {
        this.onAddTagListener = onAddTagListener;
    }

    public interface OnAddTagListener {
        void onTagAdded(Tag tag);

        void onTagSelected(Tag tag);
    }
}