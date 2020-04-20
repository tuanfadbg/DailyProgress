package com.tuanfadbg.progress.ui.main_list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.item.Item;
import com.tuanfadbg.progress.ui.main_grid.OnItemClickListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {

    private Context context;
    private List<Item> datas;
    private OnItemClickListener onItemClickListener;

    public ItemListAdapter(Context context, List<Item> datas, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.datas = datas;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_item_timeline_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void setData(List<Item> datas) {
        this.datas.clear();
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

    public void setNewestData(List<Item> datas) {
        this.datas.addAll(0, datas);
        notifyDataSetChanged();
    }

    public void removeItem(Item item) {
        this.datas.remove(item);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            txtTime = itemView.findViewById(R.id.textView);
        }

        public void setData(int position) {
            Item item = datas.get(position);
            if (TextUtils.isEmpty(item.title))
                txtTime.setVisibility(View.GONE);
            else {
                txtTime.setVisibility(View.VISIBLE);
            }
            txtTime.setText(item.title);
            Glide.with(context).load(new File(item.file)).into(imageView);
            itemView.setOnClickListener(v -> onItemClickListener.onClick(item));
        }
    }
}
