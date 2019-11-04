package com.example.motor.banner;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.example.motor.banner.convenientbanner.holder.Holder;


/**
 *
 * Author : yanftch
 * Date   : 2017/5/26
 * Time   : 09:18
 * Desc   : 只放图片的Banner图
 */

public class LocalImageHolderView implements Holder<Integer> {
    private ImageView imageView;

    @Override
    public View createView(Context context) {
        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }

    @Override
    public void UpdateUI(Context context, final int position, Integer data) {
        imageView.setImageResource(data);
    }
}