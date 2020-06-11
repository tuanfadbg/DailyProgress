package com.tuanfadbg.trackprogress.ui.restore;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.tuanfadbg.trackprogress.database.item.ItemInsertAsyncTask;
import com.tuanfadbg.trackprogress.database.item.ItemSelectAsyncTask;
import com.tuanfadbg.trackprogress.database.item.ItemUpdateAsyncTask;
import com.tuanfadbg.trackprogress.database.tag.Tag;
import com.tuanfadbg.trackprogress.ui.MainActivity;
import com.tuanfadbg.trackprogress.ui.add_tag.AddTagDialog;
import com.tuanfadbg.trackprogress.utils.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RestoreDialog extends DialogFragment {

    private RecyclerView rcvData;
    private RestoreAdapter dataGridAdapter;
    private TextView txtAll, txtCancel, txtMove, txtDelete;
    private OnUpdateItemListener onUpdateItemListener;
    private ArrayList<String> dataToRestore;

    public RestoreDialog(ArrayList<String> dataToRestore, OnUpdateItemListener onUpdateItemListener) {
        this.dataToRestore = dataToRestore;
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

        dataGridAdapter = new RestoreAdapter(getContext(), new ArrayList<>(), new RestoreAdapter.OnItemClickListener() {
            @Override
            public void onClick(String item) {
                boolean isAllSelected = dataGridAdapter.select(item);
                txtAll.setSelected(isAllSelected);
            }
        });
        rcvData.setAdapter(dataGridAdapter);
        rcvData.setLayoutManager(new GridLayoutManager(getContext(), 3));

        txtCancel.setOnClickListener(v -> dismiss());
        txtAll.setOnClickListener(v -> selectAll());
        txtDelete.setOnClickListener(v -> delete());
        txtMove.setOnClickListener(v -> move());

        dataGridAdapter.setData(dataToRestore);
    }

    private void selectAll() {
        dataGridAdapter.selectAll();
        txtAll.setSelected(true);
    }

    private void delete() {
        List<String> items = dataGridAdapter.getItemSelected();
        if (items.size() == 0) {
            Toast.makeText(getContext(), getString(R.string.no_images_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle(getString(R.string.are_you_sure));
        sweetAlertDialog.setConfirmText(getString(R.string.delete));
        sweetAlertDialog.setCancelText(getString(R.string.cancel));
        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> AsyncTask.execute(() -> {
            for (int i = 0; i < items.size(); i++) {
                File file = new File(items.get(i));
                if (file.exists()) {
                    file.delete();
                }
                dataToRestore.remove(items.get(i));
                dataGridAdapter.setData(dataToRestore);
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(sweetAlertDialog::dismissWithAnimation);
            }
        }));
        sweetAlertDialog.show();
    }

    private void move() {
        List<String> items = dataGridAdapter.getItemSelected();
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
                List<String> items = dataGridAdapter.getItemSelected();
                Observable.fromArray(items)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<String>>() {
                            @Override
                            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onNext(@io.reactivex.rxjava3.annotations.NonNull List<String> files) {
                                for (int i = 0; i < files.size(); i++) {
                                    saveToInternalStorage(files.get(i), tag.uid);
                                }
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                                if (getActivity() != null) {
                                    getActivity().sendBroadcast(MainActivity.getBRItem());
                                    dismiss();
                                }
                            }
                        });
            }
        });
        sweetAlertDialog.setCancelText(getString(R.string.cancel));
        sweetAlertDialog.show();
    }

    private void saveToInternalStorage(String fileName, int tagId) {
        long lastModified = getLastModified(fileName);
        ItemInsertAsyncTask itemInsertAsyncTask = new ItemInsertAsyncTask(getContext());

        Item item = new Item("", fileName, tagId, true, lastModified);

        itemInsertAsyncTask.execute(new Data(item, new OnUpdateDatabase() {
            @Override
            public void onSuccess() {
//                if (onAddNewItemListener != null)
//                    onAddNewItemListener.onNewItem(hasNewTag, finalTagId);
//                dismiss();
            }

            @Override
            public void onFail() {
                Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            }
        }));
    }

    public long getLastModified(String fileName) {
        Uri uri = getImageContentUri(getContext(), new File(fileName));
        String[] filePathColumn = {MediaStore.Images.Media.DATA, DocumentsContract.Document.COLUMN_LAST_MODIFIED};
        Cursor cursor = getContext().getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex1 = cursor.getColumnIndex(filePathColumn[1]);
        String lastModifiedString = cursor.getString(columnIndex1);
        return TextUtils.isEmpty(lastModifiedString) ? 0 : Long.parseLong(lastModifiedString);
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
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