package com.tuanfadbg.trackprogress.ui.select_image_no_tag;

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
import com.tuanfadbg.trackprogress.ui.main_grid.OnItemClickListener;
import com.tuanfadbg.trackprogress.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SelectImageNoTagAdapter extends RecyclerView.Adapter<SelectImageNoTagAdapter.ViewHolder> {

    private Context context;
    private List<Item> datas;
    private List<String> itemSelected;

    private OnItemClickListener onItemClickListener;
    private static final int SELECTED = 1;
    private static final int NONE = 0;

    public SelectImageNoTagAdapter(Context context, List<Item> datas, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.datas = datas;
        itemSelected = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_image_no_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (itemSelected.contains(String.valueOf(datas.get(position).uid)))
            return SELECTED;
        return NONE;

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

    public boolean select(Item item) {
        if (itemSelected.contains(String.valueOf(item.uid))) {
            itemSelected.remove(String.valueOf(item.uid));
        } else
            itemSelected.add(String.valueOf(item.uid));
        notifyDataSetChanged();
        return itemSelected.size() == datas.size();
    }

    public void selectAll() {
        itemSelected.clear();
        for (Item item : datas) {
            itemSelected.add(String.valueOf(item.uid));
        }
        notifyDataSetChanged();
    }

    public List<Item> getItemSelected() {
        List<Item> items = new ArrayList<>();
        for (Item item : datas) {
            if (itemSelected.contains(String.valueOf(item.uid))) {
                items.add(item);
            }
        }
        return items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView txtTime;
        View viewSelected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            txtTime = itemView.findViewById(R.id.textView);
            viewSelected = itemView.findViewById(R.id.view_selected);
            viewSelected.setVisibility(View.GONE);
        }

        public void setData(int position) {
            Item item = datas.get(position);

            long diff = Utils.diffDay(new Date().getTime(), item.createAt);
            if (diff == 0)
                txtTime.setText(R.string.today);
            else
                txtTime.setText(String.format(Locale.US, context.getString(R.string.days_ago), diff));

            if (getItemViewType() == SELECTED) {
                viewSelected.setVisibility(View.VISIBLE);
            } else {
                viewSelected.setVisibility(View.GONE);
            }

            Glide.with(imageView).load(new File(item.file)).into(imageView);

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
