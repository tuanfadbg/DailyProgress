package com.tuanfadbg.progress.ui;

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

import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.Item;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private Context context;
    private List<Item> datas;

    public DataAdapter(Context context, List<Item> datas) {
        this.context = context;
        this.datas = datas;
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
        this.datas.clear();
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

    public void setNewestData(List<Item> datas) {
        this.datas.addAll(0, datas);
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
            long diffInMillies = Math.abs(new Date().getTime() - item.createAt);
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            if (diff < 1) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.ENGLISH);
                String currentDate = sdf.format(new Date());
                String createAt = sdf.format(new Date(item.createAt));
                if (currentDate.equals(createAt))
                    diff = 0;
                else diff = 1;
            }

            if (diff == 0)
                txtTime.setText(R.string.today);
            else
                txtTime.setText(String.format(Locale.US, "%d days ago", diff));

            loadImageFromStorage(item.file, imageView);
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
