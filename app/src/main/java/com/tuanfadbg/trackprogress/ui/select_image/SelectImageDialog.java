package com.tuanfadbg.trackprogress.ui.select_image;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.item.ItemSelectAsyncTask;
import com.tuanfadbg.trackprogress.ui.main_grid.DataGridAdapter;
import com.tuanfadbg.trackprogress.ui.main_grid.OnItemClickListener;

import java.util.ArrayList;

public class SelectImageDialog extends DialogFragment {

    private RecyclerView rcvData;
    private DataGridAdapter dataGridAdapter;
    private OnItemClickListener onItemClickListener;
    private int tagId;

    public SelectImageDialog(int tagId, OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        this.tagId = tagId;
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
        View view = inflater.inflate(R.layout.dialog_select_image, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rcvData = view.findViewById(R.id.rcv_data);
        dataGridAdapter = new DataGridAdapter(getContext(), new ArrayList<>(), onItemClickListener);
        rcvData.setAdapter(dataGridAdapter);
        rcvData.setLayoutManager(new GridLayoutManager(getContext(), 3));

        ItemSelectAsyncTask itemSelectAsyncTask = new ItemSelectAsyncTask(getContext());
        itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, tagId, datas -> {
            dataGridAdapter.setData(datas);
        }));

        view.findViewById(R.id.txt_cancel).setOnClickListener(v -> dismiss());
        view.setOnClickListener(v -> dismiss());
    }
}