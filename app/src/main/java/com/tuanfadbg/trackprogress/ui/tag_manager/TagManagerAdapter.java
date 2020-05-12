package com.tuanfadbg.trackprogress.ui.tag_manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tuanfadbg.trackprogress.beforeafterimage.R;
import com.tuanfadbg.trackprogress.database.tag.Tag;
import com.tuanfadbg.trackprogress.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TagManagerAdapter extends RecyclerView.Adapter<TagManagerAdapter.ViewHolder> {

    private Context context;
    private List<Tag> datas;
    private OnTagActionListener onTagActionListener;
    private HashMap<Integer, Integer> tagAndSize;

    public TagManagerAdapter(Context context, List<Tag> datas, OnTagActionListener onTagActionListener) {
        this.context = context;
        this.datas = datas;
        this.onTagActionListener = onTagActionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_manager, parent, false);
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

    public void setData(List<Tag> datas, HashMap<Integer, Integer> tagAndSize) {
        this.tagAndSize = tagAndSize;
        if (datas == null)
            return;
        this.datas.clear();
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

    public void setData(List<Tag> datas) {
        if (datas == null)
            return;
        this.datas.clear();
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

    public void removeItem(Tag tag) {
        this.datas.remove(tag);
        notifyDataSetChanged();
    }

    public void update(Tag tag) {
        if (tag == null)
            return;
        for (int i = 0; i < datas.size(); i++) {
            if (datas.get(i).uid == tag.uid) {
                datas.set(i, tag);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View viewDot;
        TextView txtTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewDot = itemView.findViewById(R.id.view_dot);
            txtTitle = itemView.findViewById(R.id.txt_title);
        }

        public void setData(int position) {
            Tag tag = datas.get(position);
            Integer imageOfTag = tagAndSize != null ? tagAndSize.get(tag.uid) : 0;
            if (imageOfTag == null)
                imageOfTag = 0;
            txtTitle.setText(String.format(Locale.US, "%s (%d)", tag.name, imageOfTag));

            viewDot.setBackground(Utils.getCircleDrawableByColor(Utils.getRandomColor(position)));
            itemView.findViewById(R.id.ic_edit).setOnClickListener(v -> onTagActionListener.onEdit(tag));
            itemView.findViewById(R.id.ic_delete).setOnClickListener(v -> onTagActionListener.onDelete(tag));
        }
    }

}
