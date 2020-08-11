package com.tuanfadbg.trackprogress.ui.crop_image;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.isseiaoki.simplecropview.CropImageView;
import com.tuanfadbg.trackprogress.beforeafterimage.R;

import java.util.ArrayList;
import java.util.List;

class RatioAdapter extends RecyclerView.Adapter<RatioAdapter.ViewHolder> {

    private List<RatioData> radioDataList;
    private OnRatioSelected onRatioSelected;
    private int positionSelected = 0;

    public RatioAdapter(OnRatioSelected onRatioSelected) {
        this.radioDataList = new ArrayList<>();
        radioDataList.add(new RatioData("FREE", CropImageView.CropMode.FREE));
        radioDataList.add(new RatioData("FIT IMAGE", CropImageView.CropMode.FIT_IMAGE));
        radioDataList.add(new RatioData("16:9", CropImageView.CropMode.RATIO_16_9));
        radioDataList.add(new RatioData("9:16", CropImageView.CropMode.RATIO_9_16));
        radioDataList.add(new RatioData("3:4", CropImageView.CropMode.RATIO_3_4));
        radioDataList.add(new RatioData("4:3", CropImageView.CropMode.RATIO_4_3));

        this.onRatioSelected = onRatioSelected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_ratio, null);
        if (viewType == 1)
            view.setSelected(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.loadData(radioDataList.get(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        return position == positionSelected ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return radioDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txt_name);
        }

        void loadData(RatioData ratioData, int position) {
            name.setText(ratioData.name);
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    positionSelected = position;
                    onRatioSelected.onSelected(ratioData, position);
                    notifyDataSetChanged();
                }
            });
        }
    }

    public interface OnRatioSelected {
        void onSelected(RatioData ratioData, int position);
    }
}
