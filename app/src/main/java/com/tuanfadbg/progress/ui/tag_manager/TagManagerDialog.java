package com.tuanfadbg.progress.ui.tag_manager;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.Data;
import com.tuanfadbg.progress.database.OnUpdateDatabase;
import com.tuanfadbg.progress.database.item.Item;
import com.tuanfadbg.progress.database.item.ItemSelectAsyncTask;
import com.tuanfadbg.progress.database.tag.Tag;
import com.tuanfadbg.progress.database.tag.TagDeleteAsyncTask;
import com.tuanfadbg.progress.database.tag.TagSelectAllAsyncTask;
import com.tuanfadbg.progress.ui.MainActivity;
import com.tuanfadbg.progress.ui.add_tag.AddTagDialog;
import com.tuanfadbg.progress.ui.edit_tag.EditTagDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TagManagerDialog extends DialogFragment {

    public static final String TAG = TagManagerDialog.class.getSimpleName();
    private RecyclerView rcvData;
    private List<Tag> tags = new ArrayList<>();
    private TagManagerAdapter tagManagerAdapter;

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
        View view = inflater.inflate(R.layout.dialog_tag_manager, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rcvData = view.findViewById(R.id.rcv_data);
        tagManagerAdapter = new TagManagerAdapter(getContext(), tags, new OnTagActionListener() {
            @Override
            public void onDelete(Tag tag) {
                delete(tag);
            }

            @Override
            public void onEdit(Tag tag) {
                edit(tag);
            }
        });

        rcvData.setAdapter(tagManagerAdapter);
        rcvData.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        getAllItemAndPrepareData();

        view.findViewById(R.id.ic_add).setOnClickListener(v -> showAddTag());
        view.findViewById(R.id.img_back).setOnClickListener(v -> dismiss());
    }

    private void delete(Tag tag) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle(getString(R.string.are_you_sure));
        sweetAlertDialog.setConfirmButton(getString(R.string.delete), sweetAlertDialog1 -> {
            TagDeleteAsyncTask tagDeleteAsyncTask = new TagDeleteAsyncTask(getContext());
            tagDeleteAsyncTask.execute(new Data(tag, new OnUpdateDatabase() {
                @Override
                public void onSuccess() {
                    tagManagerAdapter.removeItem(tag);
                    sweetAlertDialog1.dismissWithAnimation();
                    getActivity().sendBroadcast(MainActivity.getBRTag());
                    showAlertDeleteImageBelongToTag(tag);
                }

                @Override
                public void onFail() {
                    Toast.makeText(getContext(), getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                }
            }));
        });
        sweetAlertDialog.setCancelText(getString(R.string.cancel));
        sweetAlertDialog.show();
    }

    private void showAlertDeleteImageBelongToTag(Tag tag) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle(getString(R.string.delete_image_tag));
        sweetAlertDialog.setConfirmButton(getString(R.string.delete), sweetAlertDialog1 -> {
            TagDeleteAsyncTask tagDeleteAsyncTask = new TagDeleteAsyncTask(getContext());
            tagDeleteAsyncTask.execute(new Data(tag, new OnUpdateDatabase() {
                @Override
                public void onSuccess() {

                    sweetAlertDialog1.dismissWithAnimation();
                    getActivity().sendBroadcast(MainActivity.getBRTag());
                    showAlertDeleteImageBelongToTag(tag);
                }

                @Override
                public void onFail() {
                    Toast.makeText(getContext(), getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                }
            }));
        });
        sweetAlertDialog.setCancelText(getString(R.string.cancel));
        sweetAlertDialog.show();

    }

    private void edit(Tag tag) {
        EditTagDialog editTagDialog = new EditTagDialog(tag, new EditTagDialog.OnEditTagListener() {
            @Override
            public void onEdited(Tag tag) {
                tagManagerAdapter.update(tag);
                getActivity().sendBroadcast(MainActivity.getBRTag());
            }
        });

        editTagDialog.show(getChildFragmentManager(), EditTagDialog.class.getSimpleName());
    }

    private void getAllItemAndPrepareData() {
        ItemSelectAsyncTask itemSelectAsyncTask = new ItemSelectAsyncTask(getContext());
        itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, -1, new ItemSelectAsyncTask.OnItemSelectedListener() {
            @Override
            public void onSelected(List<Item> datas) {
                prepareDataAndGetAllTag(datas);
            }
        }));
    }

    private void prepareDataAndGetAllTag(List<Item> datas) {
        HashMap<Integer, Integer> tagAndSize = new HashMap<>();
        for (Item data : datas) {
            Integer value = tagAndSize.get(data.tag);
            if (tagAndSize.get(data.tag) == null) {
                tagAndSize.put(data.tag, 1);
            } else {
                tagAndSize.put(data.tag, value + 1);
            }
        }

        TagSelectAllAsyncTask tagSelectAsyncTask = new TagSelectAllAsyncTask(getContext());
        tagSelectAsyncTask.execute(tags -> {
            this.tags = tags;
            if (tags == null)
                tags = new ArrayList<>();
            Collections.reverse(tags);
            List<Tag> finalTags = tags;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tagManagerAdapter.setData(finalTags, tagAndSize);
                }
            });
        });

    }

    private void showAddTag() {
        AddTagDialog addTagDialog = new AddTagDialog(false);
        addTagDialog.show(getChildFragmentManager(), AddTagDialog.class.getSimpleName());
        addTagDialog.setOnAddTagListener(new AddTagDialog.OnAddTagListener() {
            @Override
            public void onTagAdded(Tag tag) {
                tags.add(tag);
                tagManagerAdapter.setData(tags);
                getActivity().sendBroadcast(MainActivity.getBRTag());
            }

            @Override
            public void onTagSelected(Tag tag) {

            }
        });
    }

}