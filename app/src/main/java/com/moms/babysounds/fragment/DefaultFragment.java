package com.moms.babysounds.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.moms.babysounds.R;
import com.moms.babysounds.activity.MainActivity;
import com.moms.babysounds.adapter.DemoInfiniteAdapter;
import com.moms.babysounds.databinding.FragmentInduceSleepBinding;
import com.moms.babysounds.event.FragmentRemoveEvent;
import com.moms.babysounds.service.MusicService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class DefaultFragment extends Fragment implements View.OnClickListener {

    private Fragment mFragment;

    public DefaultFragment() {
    }

    public static DefaultFragment newInstance(String param1, String param2) {
        DefaultFragment fragment = new DefaultFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragment = new Fragment();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_button:
                mFragment = MainFragment.newInstance("","");
                EventBus.getDefault().post(new FragmentRemoveEvent(mFragment));
                break;

            case R.id.setting_button:
                mFragment = NotAutoSleepFragment2.newInstance("","");
                ((MainActivity)getActivity()).setFragment(mFragment);
//                EventBus.getDefault().post(new FragmentRemoveEvent(mFragment));
                break;
        }
    }
}