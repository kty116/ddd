package com.brainict.smartwave.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.brainict.smartwave.R;
import com.brainict.smartwave.activity.MainActivity;
import com.brainict.smartwave.adapter.DemoInfiniteAdapter;
import com.brainict.smartwave.common.Constants;
import com.brainict.smartwave.databinding.FragmentInduceSleepBinding;
import com.brainict.smartwave.event.FragmentRemoveEvent;
import com.brainict.smartwave.event.MessageEvent;
import com.brainict.smartwave.event.MusicServiceEvent;
import com.brainict.smartwave.model.AudioSetModel;
import com.brainict.smartwave.service.MusicService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class InduceSleepFragment extends DefaultFragment implements View.OnClickListener {
    private static final String AUDIO_LIST = "audio_list";
    private ArrayList<AudioSetModel> mAudioSetModelList;
    private String mParam2;
    private FragmentInduceSleepBinding mBinding;
    private DemoInfiniteAdapter mAdapter;
    public static final String TAG = InduceSleepFragment.class.getSimpleName();
    private int mPositionImage;
    private ArrayList<Integer> mImageList;
    private Handler mHandler;
    private Thread mImageThread;
    private int mImgRes;
    private Intent mMusicIntent;

    public InduceSleepFragment() {
    }

    public static InduceSleepFragment newInstance(ArrayList<AudioSetModel> audioSetModelList) {
        InduceSleepFragment fragment = new InduceSleepFragment();
        Bundle args = new Bundle();
        args.putSerializable(AUDIO_LIST, audioSetModelList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAudioSetModelList = (ArrayList<AudioSetModel>) getArguments().getSerializable(AUDIO_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_induce_sleep, container, false);
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
        mBinding.layoutDefault.settingButton.setOnClickListener(this);
        mBinding.topLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_button:
                Fragment fragment1 = MainFragment.newInstance();
                EventBus.getDefault().post(new FragmentRemoveEvent(fragment1));
                break;

            case R.id.setting_button:
                Fragment fragment2 = AwakeningFragment.newInstance();
                ((MainActivity)getActivity()).setFragment(fragment2);
                break;
        }
    }

    public void startMusicService() {

        mMusicIntent = new Intent(getContext(), MusicService.class);
        mMusicIntent.setAction(Constants.INDUCE_SLEEP_ACTION);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.AUDIO_LIST, mAudioSetModelList);
        mMusicIntent.putExtras(bundle);

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

        if (event instanceof MusicServiceEvent) {
            MusicServiceEvent musicServiceEvent = (MusicServiceEvent) event;
            if (musicServiceEvent.isLive()) {
                startChangeBackground();
            } else {
                stopChangeBackground();
                EventBus.getDefault().post(new FragmentRemoveEvent(MainFragment.newInstance()));
            }
        }
    }

    public void startChangeBackground() {
        if (mImageThread.getState() == Thread.State.NEW) {
            mImageThread.start();
        }
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
