package com.tuanfadbg.trackprogress.ui.crop_image;

import com.isseiaoki.simplecropview.CropImageView;

class RatioData {
    public String name;
    public CropImageView.CropMode ratioValue;

    public RatioData(String name, CropImageView.CropMode ratioValue) {
        this.name = name;
        this.ratioValue = ratioValue;
    }
}
