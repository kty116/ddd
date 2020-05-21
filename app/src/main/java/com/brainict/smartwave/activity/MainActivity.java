package com.brainict.smartwave.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.brainict.smartwave.R;
import com.brainict.smartwave.common.Commonlib;
import com.brainict.smartwave.common.Constants;
import com.brainict.smartwave.common.TinyDB;
import com.brainict.smartwave.databinding.ActivityMainBinding;
import com.brainict.smartwave.event.FragmentRemoveEvent;
import com.brainict.smartwave.event.MessageEvent;
import com.brainict.smartwave.event.MusicServiceEvent;
import com.brainict.smartwave.fragment.MainFragment;
import com.brainict.smartwave.fragment.NotAutoSleepFragment;
import com.brainict.smartwave.fragment.NotAutoSleepFragment2;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static com.brainict.smartwave.common.Constants.FINISH_INTERVAL_TIME;
import static com.brainict.smartwave.common.Constants.FIVE;
import static com.brainict.smartwave.common.Constants.MINUTE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding mBinding;
    private Intent mMusicIntent;
    private Fragment fragment;

    private long backPressedTime = 0;
    private TinyDB mTinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        fragment = MainFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, fragment).commit();
        mTinyDB = new TinyDB(this);

        Log.d(TAG, "onCreate: "+Commonlib.getRunActivity(this));

//        mBefore30TriggerTime = setBootTriggerTime(-Constants.MINUTE * 30);
//        mTriggerTime = setBootTriggerTime(0);
//
//        Log.d(TAG, "onReceive: "+mBefore30TriggerTime);
//        Log.d(TAG, "onReceive: "+mTriggerTime);
//
//        if (System.currentTimeMillis() < mTriggerTime - Constants.MINUTE * 31) {
//            setBootOut30Alarm(context, true, mBefore30TriggerTime);
//        } else {
//            setBootIn30Alarm(context, true, mTriggerTime);
//            setBootOut30Alarm(context, true, mBefore30TriggerTime);
//        }

    }

    private long setBootTriggerTime(long beforeTime) {
        long atime = System.currentTimeMillis();
        Calendar curTime = Calendar.getInstance();
        curTime.set(Calendar.HOUR_OF_DAY, mTinyDB.getInt(Constants.ALARM_HOUR));
        curTime.set(Calendar.MINUTE, mTinyDB.getInt(Constants.ALARM_MIN));
        curTime.set(Calendar.SECOND, 0);
        curTime.set(Calendar.MILLISECOND, 0);
        long btime = curTime.getTimeInMillis() + beforeTime;
        long triggerTime = btime;
        if (atime > btime)
            triggerTime += 1000 * 60 * 60 * 24;

        return triggerTime;
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

        if (event instanceof MusicServiceEvent) {

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
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, MainFragment.newInstance()).commit();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, fragment).commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.size() > 1) {
            getSupportFragmentManager().popBackStack();
        } else if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), getString(R.string.press_the_back_button_again_to_exit), Toast.LENGTH_SHORT).show();
        }
    }
}
