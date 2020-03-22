package com.moms.babysounds.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.asksira.loopingviewpager.LoopingViewPager;
import com.moms.babysounds.R;

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
//        convertView.findViewById(R.id.image).setBackgroundColor(context.getResources().getColor(getBackgroundColor(listPosition)));
        ImageView imageView = convertView.findViewById(R.id.imageView);
        imageView.setBackgroundResource(mImageList.get(listPosition));
//        mListPosition = listPosition;

//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (mListPosition == 0){
//                    mViewPager.setCurrentItem(0, false);
//                    mViewPager.setCurrentItem(1);
//                }else {
//                    mViewPager.setCurrentItem(mListPosition+1);
//                }
//
//            }
//        });
    }
}
