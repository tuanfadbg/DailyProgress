package com.tuanfadbg.trackprogress.ui.main_grid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.item.Item;
import com.tuanfadbg.trackprogress.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataGridAdapter extends RecyclerView.Adapter<DataGridAdapter.ViewHolder> {

    private Context context;
    private List<Item> datas;
    private OnItemClickListener onItemClickListener;

    public DataGridAdapter(Context context, List<Item> datas, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.datas = datas;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_grid, parent, false);
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
        if (datas == null)
            return;
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

            long diff = Utils.diffDay(new Date().getTime(), item.createAt);
            if (diff == 0)
                txtTime.setText(R.string.today);
            else
                txtTime.setText(String.format(Locale.US, context.getString(R.string.days_ago), diff));


            Glide.with(imageView).load(new File(item.file)).into(imageView);
//            loadImageFromStorage(item.file, imageView);

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
}
