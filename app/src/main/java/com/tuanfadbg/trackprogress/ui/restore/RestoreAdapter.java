package com.tuanfadbg.trackprogress.ui.restore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.tuanfadbg.trackprogress.beforeafterimage.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RestoreAdapter extends RecyclerView.Adapter<RestoreAdapter.ViewHolder> {

    private Context context;
    private List<String> datas;
    private List<String> itemSelected;

    private OnItemClickListener onItemClickListener;
    private static final int SELECTED = 1;
    private static final int NONE = 0;

    public RestoreAdapter(Context context, List<String> datas, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.datas = datas;
        itemSelected = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restore, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (itemSelected.contains(datas.get(position)))
            return SELECTED;
        return NONE;

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void setData(List<String> datas) {
        if (datas == null)
            return;
        this.datas.clear();
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

    public boolean select(String item) {
        if (itemSelected.contains(item)) {
            itemSelected.remove(item);
        } else
            itemSelected.add(item);
        notifyDataSetChanged();
        return itemSelected.size() == datas.size();
    }

    public void selectAll() {
        itemSelected.clear();
        itemSelected.addAll(datas);
        notifyDataSetChanged();
    }

    public List<String> getItemSelected() {
        List<String> items = new ArrayList<>();
        for (String item : datas) {
            if (itemSelected.contains(item)) {
                items.add(item);
            }
        }
        return items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        View viewSelected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            viewSelected = itemView.findViewById(R.id.view_selected);
            viewSelected.setVisibility(View.GONE);
        }

        public void setData(int position) {
            String item = datas.get(position);
            if (getItemViewType() == SELECTED) {
                viewSelected.setVisibility(View.VISIBLE);
            } else {
                viewSelected.setVisibility(View.GONE);
            }

            Glide.with(imageView).load(new File(item)).into(imageView);
            itemView.setOnClickListener(v -> onItemClickListener.onClick(item));
        }
    }


    private void loadImageFromStorage(String path, ImageView img) {
        try {
            File f = new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    interface OnItemClickListener {
        void onClick(String item);
    }
}
