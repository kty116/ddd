package com.moms.babysounds.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.moms.babysounds.R;
import com.moms.babysounds.common.Constants;
import com.moms.babysounds.databinding.FragmentDeepSleepBinding;
import com.moms.babysounds.event.FragmentRemoveEvent;
import com.moms.babysounds.event.MessageEvent;
import com.moms.babysounds.event.MusicServiceEvent;
import com.moms.babysounds.service.MusicService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class DeepSleepFragment extends DefaultFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public static final String TAG = InduceSleepFragment.class.getSimpleName();
    private int mPositionImage;
    private ArrayList<Integer> mImageList;
    private Handler mHandler;
    private Thread mImageThread;
    private int mImgRes;
    private Intent mMusicIntent;
    private FragmentDeepSleepBinding mBinding;

    public DeepSleepFragment() {
    }

    public static DeepSleepFragment newInstance(String param1, String param2) {
        DeepSleepFragment fragment = new DeepSleepFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_deep_sleep, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageList = new ArrayList<>();

        for (int i = 1; i < 12; i++) {
            int imgRes = getResources().getIdentifier("sleep" + i, "drawable", getActivity().getPackageName());
            if (imgRes == 0) {
                break;
            }
            mImageList.add(imgRes);
        }

        mPositionImage = 0;
        mBinding.layoutDefault.defaultParent.setBackgroundResource(mImageList.get(mPositionImage));
        initChangeBackgroundThread();

        startMusicService();

        mBinding.layoutDefault.homeButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
        }
    }

    public void startMusicService() {

        mMusicIntent = new Intent(getContext(), MusicService.class);
        mMusicIntent.setAction(Constants.DEEP_SLEEP_ACTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(mMusicIntent);
        } else {
            getContext().startService(mMusicIntent);
        }
    }

    public void stopMusicService() {

        mMusicIntent = new Intent(getContext(), MusicService.class);
        mMusicIntent.setAction(Constants.STOP_SERVICE_ACTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(mMusicIntent);
        } else {
            getContext().stopService(mMusicIntent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        startChangeBackground();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (mImageThread != null && mImageThread.isAlive()) {
            mImageThread.interrupt();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageThread = null;
        stopMusicService();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        if (event instanceof MusicServiceEvent) {  //서비스 종료시
            MusicServiceEvent musicServiceEvent = (MusicServiceEvent) event;
            if (musicServiceEvent.isLive()) {
                startChangeBackground();
            } else {
                stopChangeBackground();
                EventBus.getDefault().post(new FragmentRemoveEvent());
            }
        }
    }

    public void startChangeBackground() {
        mImageThread.start();
    }

    public void stopChangeBackground() {
        if (mImageThread != null && mImageThread.isAlive()) {
            mImageThread.interrupt();
        }
    }

    public void initChangeBackgroundThread(){
        mHandler = new Handler();
        mImageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mPositionImage++;
                                try {
                                    mBinding.layoutDefault.defaultParent.setBackgroundResource(mImageList.get(mPositionImage));
                                } catch (IndexOutOfBoundsException e) {
                                    mPositionImage = 0;
                                    mBinding.layoutDefault.defaultParent.setBackgroundResource(mImageList.get(mPositionImage));
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
    }
}
