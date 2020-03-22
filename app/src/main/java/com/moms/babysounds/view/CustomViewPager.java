package com.moms.babysounds.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.asksira.loopingviewpager.LoopingViewPager;

public class CustomViewPager extends LoopingViewPager {

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
            return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
            return false;
    }
}
