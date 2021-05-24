package com.tuanfadbg.trackprogress.ui.select_image_no_tag;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.AppDatabase;
import com.tuanfadbg.trackprogress.database.Data;
import com.tuanfadbg.trackprogress.database.OnUpdateDatabase;
import com.tuanfadbg.trackprogress.database.item.Item;
import com.tuanfadbg.trackprogress.database.item.ItemSelectAsyncTask;
import com.tuanfadbg.trackprogress.database.item.ItemUpdateAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.Tag;
import com.tuanfadbg.trackprogress.ui.add_tag.AddTagDialog;
import com.tuanfadbg.trackprogress.ui.main_grid.OnItemClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelectImageNoTagDialog extends DialogFragment {

    private RecyclerView rcvData;
    private SelectImageNoTagAdapter dataGridAdapter;
    private TextView txtAll, txtCancel, txtMove, txtDelete;
    private OnUpdateItemListener onUpdateItemListener;

    public SelectImageNoTagDialog(OnUpdateItemListener onUpdateItemListener) {
        this.onUpdateItemListener = onUpdateItemListener;
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
        View view = inflater.inflate(R.layout.dialog_select_image_no_tag, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rcvData = view.findViewById(R.id.rcv_data);
        txtAll = view.findViewById(R.id.txt_select_all);
        txtCancel = view.findViewById(R.id.txt_cancel);
        txtMove = view.findViewById(R.id.txt_move);
        txtDelete = view.findViewById(R.id.txt_delete);

        dataGridAdapter = new SelectImageNoTagAdapter(getContext(), new ArrayList<>(), new OnItemClickListener() {
            @Override
            public void onClick(Item item) {
                boolean isAllSelected = dataGridAdapter.select(item);
                txtAll.setSelected(isAllSelected);
            }
        });
        rcvData.setAdapter(dataGridAdapter);
        rcvData.setLayoutManager(new GridLayoutManager(getContext(), 3));

        getData();

        txtCancel.setOnClickListener(v -> dismiss());
        txtAll.setOnClickListener(v -> selectAll());
        txtDelete.setOnClickListener(v -> delete());
        txtMove.setOnClickListener(v -> move());
    }

    private void getData() {
        ItemSelectAsyncTask itemSelectAsyncTask = new ItemSelectAsyncTask(getContext());
        itemSelectAsyncTask.execute(new ItemSelectAsyncTask.Data(true, View.NO_ID, datas -> {
            if (datas.size() == 0) {
                dismiss();
                return;
            }
            dataGridAdapter.setData(datas);
        }));
    }

    private void selectAll() {
        dataGridAdapter.selectAll();
        txtAll.setSelected(true);
    }

    private void delete() {
        List<Item> items = dataGridAdapter.getItemSelected();
        if (items.size() == 0) {
            Toast.makeText(getContext(), getString(R.string.no_images_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        Item[] itemsArray = new Item[items.size()];
        for (int i = 0; i < items.size(); i++) {
            itemsArray[i] = items.get(i);
        }

        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle(getString(R.string.are_you_sure));
        sweetAlertDialog.setConfirmText(getString(R.string.delete));
        sweetAlertDialog.setCancelText(getString(R.string.cancel));
        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> AsyncTask.execute(() -> {
            AppDatabase db = Room.databaseBuilder(getContext(),
                    AppDatabase.class, AppDatabase.ROOM_NAME)
                    .fallbackToDestructiveMigration()
                    .build();

            db.itemDao().delete(itemsArray);

            for (int i = 0; i < items.size(); i++) {
                File file = new File(items.get(i).file);
                if (file.exists()) {
                    file.delete();
                }
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(sweetAlertDialog::dismissWithAnimation);
                getData();
            }
        }));
        sweetAlertDialog.show();
    }

    private void move() {
        List<Item> items = dataGridAdapter.getItemSelected();
        if (items.size() == 0) {
            Toast.makeText(getContext(), getString(R.string.no_images_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        AddTagDialog addTagDialog = new AddTagDialog();
        addTagDialog.setOnAddTagListener(new AddTagDialog.OnAddTagListener() {
            @Override
            public void onTagAdded(Tag tag) {
                addTagDialog.dismiss();
                askMoveToTag(tag);
            }

            @Override
            public void onTagSelected(Tag tag) {
                addTagDialog.dismiss();
                askMoveToTag(tag);
            }
        });
        addTagDialog.show(getChildFragmentManager(), AddTagDialog.class.getSimpleName());
    }

    private void askMoveToTag(Tag tag) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle(tag.name);
        sweetAlertDialog.setContentText(getString(R.string.image_move_to_tag));
        sweetAlertDialog.setConfirmButton(getString(R.string.move), new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                List<Item> items = dataGridAdapter.getItemSelected();
                Item[] itemsArray = new Item[items.size()];

                for (int i = 0; i < items.size(); i++) {
                    items.get(i).tag = tag.uid;
                    itemsArray[i] = items.get(i);
                }

                ItemUpdateAsyncTask itemUpdateAsyncTask = new ItemUpdateAsyncTask(getContext());
                itemUpdateAsyncTask.execute(new Data(itemsArray, new OnUpdateDatabase() {
                    @Override
                    public void onSuccess() {
                        sweetAlertDialog.dismissWithAnimation();
                        getData();
                    }

                    @Override
                    public void onFail() {

                    }
                }));

            }
        });
        sweetAlertDialog.setCancelText(getString(R.string.cancel));
        sweetAlertDialog.show();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        onUpdateItemListener.onUpdated();
    }

    public interface OnUpdateItemListener {
        void onUpdated();
    }
}