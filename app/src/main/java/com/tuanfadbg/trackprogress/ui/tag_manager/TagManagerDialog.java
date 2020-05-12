package com.tuanfadbg.trackprogress.ui.tag_manager;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.Data;
import com.tuanfadbg.trackprogress.database.OnUpdateDatabase;
import com.tuanfadbg.trackprogress.database.item.Item;
import com.tuanfadbg.trackprogress.database.item.ItemSelectAsyncTask;
import com.tuanfadbg.trackprogress.database.item.ItemUpdateAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.Tag;
import com.tuanfadbg.trackprogress.database.tag.TagDeleteAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.TagSelectAllAsyncTask;
import com.tuanfadbg.trackprogress.ui.MainActivity;
import com.tuanfadbg.trackprogress.ui.add_tag.AddTagDialog;
import com.tuanfadbg.trackprogress.ui.edit_tag.EditTagDialog;
import com.tuanfadbg.trackprogress.ui.select_image_no_tag.SelectImageNoTagDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TagManagerDialog extends DialogFragment {

    public static final String TAG = TagManagerDialog.class.getSimpleName();
    private RecyclerView rcvData;
    private ViewGroup ctWarning;
    private List<Tag> tags = new ArrayList<>();
    private TagManagerAdapter tagManagerAdapter;
    private HashMap<Integer, Integer> tagAndSize;

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
        ctWarning = view.findViewById(R.id.ct_warning);
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
        ctWarning.setOnClickListener(v -> viewWarningImage());
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
                    if (tagAndSize != null && tagAndSize.get(tag.uid) != null && tagAndSize.get(tag.uid) > 0)
                        setTagToItemNoId(tag.uid);
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

    private void setTagToItemNoId(int tagId) {
        ItemSelectAsyncTask itemSelectAsyncTask = new ItemSelectAsyncTask(getContext());
        itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, tagId, datas -> {
            Item[] items = new Item[datas.size()];
            for (int i = 0; i < datas.size(); i++) {
                datas.get(i).tag = View.NO_ID;
                items[i] = datas.get(i);
            }
            ItemUpdateAsyncTask itemUpdateAsyncTask = new ItemUpdateAsyncTask(getContext());
            itemUpdateAsyncTask.execute(new Data(items, new OnUpdateDatabase() {
                @Override
                public void onSuccess() {
                    ctWarning.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFail() {

                }
            }));
        }));
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
        itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, null, new ItemSelectAsyncTask.OnItemSelectedListener() {
            @Override
            public void onSelected(List<Item> datas) {
                prepareDataAndGetAllTag(datas);
            }
        }));
    }

    private void prepareDataAndGetAllTag(List<Item> datas) {
        tagAndSize = new HashMap<>();
        for (Item data : datas) {
            Integer value = tagAndSize.get(data.tag);
            if (data.tag != null && data.tag.equals(-1)) {
                ctWarning.setVisibility(View.VISIBLE);
            }
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
                if (getActivity() != null) {
                    tagManagerAdapter.setData(tags);
                    getActivity().sendBroadcast(MainActivity.getBRTag());
                }
            }

            @Override
            public void onTagSelected(Tag tag) {

            }
        });
    }

    private void viewWarningImage() {
        SelectImageNoTagDialog selectImageNoTagDialog = new SelectImageNoTagDialog(new SelectImageNoTagDialog.OnUpdateItemListener() {
            @Override
            public void onUpdated() {
                if (getActivity() != null) {
                    getAllItemAndPrepareData();
                    ctWarning.setVisibility(View.GONE);
                    getActivity().sendBroadcast(MainActivity.getBRTag());
                }
            }
        });
        selectImageNoTagDialog.show(getChildFragmentManager(), SelectImageNoTagDialog.class.getSimpleName());
    }

}