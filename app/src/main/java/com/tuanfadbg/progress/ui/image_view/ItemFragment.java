package com.tuanfadbg.progress.ui.image_view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.ortiz.touchview.TouchImageView;
import com.tuanfadbg.progress.R;
import com.tuanfadbg.progress.database.item.Item;

public class ItemFragment extends Fragment {
    private Item item;

    public ItemFragment(Item item) {
        this.item = item;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_item_view_image, container);
//        Glide.with(container.getContext()).load(item.file).into((TouchImageView) view.findViewById(R.id.img));
        return view;
    }
}
