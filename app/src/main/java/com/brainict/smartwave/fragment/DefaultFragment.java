package com.brainict.smartwave.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.brainict.smartwave.R;
import com.brainict.smartwave.activity.MainActivity;
import com.brainict.smartwave.databinding.FragmentInduceSleepBinding;
import com.brainict.smartwave.event.FragmentRemoveEvent;

import org.greenrobot.eventbus.EventBus;

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
                mFragment = MainFragment.newInstance();
                EventBus.getDefault().post(new FragmentRemoveEvent(mFragment));
                break;

            case R.id.setting_button:
                mFragment = NotAutoSleepFragment2.newInstance();
                ((MainActivity)getActivity()).setFragment(mFragment);
//                EventBus.getDefault().post(new FragmentRemoveEvent(mFragment));
                break;
        }
    }
}