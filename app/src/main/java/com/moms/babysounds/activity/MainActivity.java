package com.moms.babysounds.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.moms.babysounds.R;
import com.moms.babysounds.common.Constants;
import com.moms.babysounds.databinding.ActivityMainBinding;
import com.moms.babysounds.event.FragmentRemoveEvent;
import com.moms.babysounds.event.MessageEvent;
import com.moms.babysounds.event.MusicServiceEvent;
import com.moms.babysounds.event.PlayMusicEvent;
import com.moms.babysounds.fragment.AwakeningFragment;
import com.moms.babysounds.fragment.DeepSleepFragment;
import com.moms.babysounds.fragment.MainFragment;
import com.moms.babysounds.fragment.MethodOfExecutionFragment;
import com.moms.babysounds.fragment.NotAutoSleepFragment;
import com.moms.babysounds.fragment.NotAutoSleepFragment2;
import com.moms.babysounds.fragment.RestFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding mBinding;
    private Intent mMusicIntent;
    private Fragment fragment;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        fragment = MainFragment.newInstance("", "");
        getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, fragment).commit();
//        setFragment(fragment);


    }

    public void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().addToBackStack(fragment.getClass().getSimpleName()).add(R.id.content_layout, fragment).commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        if (event instanceof MusicServiceEvent) {  //서비스 종료시
            MusicServiceEvent musicService = (MusicServiceEvent) event;

        } else if (event instanceof FragmentRemoveEvent) {
            FragmentRemoveEvent fragmentRemoveEvent = (FragmentRemoveEvent) event;
            Fragment fragment = fragmentRemoveEvent.getFragment();

            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments.size() > 0) {
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                if (fragment instanceof MainFragment) {
                    MainFragment mainFragment = (MainFragment) fragment;
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, mainFragment).commit();
                } else if (fragment instanceof NotAutoSleepFragment) {
                    NotAutoSleepFragment2 notAutoSleepFragment2 = (NotAutoSleepFragment2) fragment;
                    setFragment(notAutoSleepFragment2);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, MainFragment.newInstance("", "")).commit();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, fragment).commit();
            }
            Log.d(TAG, "onMessageEvent: " + fragments.size());

        }
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Log.d(TAG, "onBackPressed: " + fragments.size());
        if (fragments.size() > 1) {
            getSupportFragmentManager().popBackStack();
        } else if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
