package com.tuanfadbg.progress.ui.main_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.item.Item;
import com.tuanfadbg.progress.ui.main_grid.OnItemClickListener;
import com.tuanfadbg.progress.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimelineListAdapter extends RecyclerView.Adapter<TimelineListAdapter.ViewHolder> {

    private Context context;
    private List<ItemTimeLine> datas;
    private OnItemClickListener onItemClickListener;
    private static final int UNIQUE = 0;
    private static final int TOP = 1;
    private static final int CENTER = 2;
    private static final int BOTTOM = 3;

    public TimelineListAdapter(Context context, List<ItemTimeLine> datas, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.datas = datas;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TOP: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_top, parent, false);
                return new ViewHolder(view);
            }

            case CENTER: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_center, parent, false);
                return new ViewHolder(view);
            }

            case BOTTOM: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_bottom, parent, false);
                return new ViewHolder(view);
            }

            case UNIQUE:
            default: { // unique
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_unique, parent, false);
                return new ViewHolder(view);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (datas.size() == 1)
            return UNIQUE;
        if (position == 0)
            return TOP;
        if (position == datas.size() - 1)
            return BOTTOM;
        return CENTER;
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
        for (int i = 0; i < datas.size(); i++) {
            List<Item> items = new ArrayList<>();
            items.add(datas.get(i));
            int j = i + 1;
            for (; j < datas.size(); j++)
                if (Utils.diffDay(datas.get(i).createAt, datas.get(j).createAt) == 0) {
                    items.add(datas.get(j));
                } else {

                    break;
                }
            i = j - 1; // trừ 1 vì sau vòng for i sẽ +1
            int diffday = Utils.diffDay(new Date().getTime(), items.get(0).createAt);
            this.datas.add(new ItemTimeLine(diffday, items));
        }
//        this.datas.addAll(datas);
        notifyDataSetChanged();
    }

//    11233344456

    public void setNewestData(List<Item> datas) {
//        this.datas.addAll(0, datas);
//        notifyDataSetChanged();
    }

    public void removeItem(Item item) {
//        this.datas.remove(item);
//        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTime;
        RecyclerView rcvData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txt_day);
            rcvData = itemView.findViewById(R.id.rcv_data);
        }

        public void setData(int position) {
            ItemTimeLine item = datas.get(position);

            if (item.getTime() == 0)
                txtTime.setText(R.string.today);
            else
                txtTime.setText(String.format(Locale.US, context.getString(R.string.days_ago), item.getTime()));

            rcvData.setAdapter(new ItemListAdapter(context, datas.get(position).getItems(), onItemClickListener));
            rcvData.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        }
    }
}
