package com.brainict.smartwave.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.brainict.smartwave.R;

import java.util.ArrayList;


public class DemoInfiniteAdapter extends LoopingPagerAdapter<Integer> {

    public static final String TAG = DemoInfiniteAdapter.class.getSimpleName();
    private final ArrayList<Integer> mImageList;
    private int mListPosition;

    public DemoInfiniteAdapter(Context context, ArrayList<Integer> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
        mImageList = itemList;
    }

    @Override
    protected View inflateView(int viewType, ViewGroup container, int listPosition) {
        return LayoutInflater.from(context).inflate(R.layout.item_image, container, false);
    }

    @Override
    protected void bindView(View convertView, int listPosition, int viewType) {
        ImageView imageView = convertView.findViewById(R.id.imageView);
        imageView.setBackgroundResource(mImageList.get(listPosition));
    }
}
