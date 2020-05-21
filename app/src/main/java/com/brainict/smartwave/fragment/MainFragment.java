package com.brainict.smartwave.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.brainict.smartwave.R;
import com.brainict.smartwave.activity.MainActivity;
import com.brainict.smartwave.common.Constants;
import com.brainict.smartwave.common.TinyDB;
import com.brainict.smartwave.databinding.FragmentAwakeningBinding;
import com.brainict.smartwave.databinding.FragmentMainBinding;
import com.brainict.smartwave.model.DayCheckModel;

import java.util.ArrayList;

public class MainFragment extends DefaultFragment implements View.OnClickListener {
    private FragmentMainBinding mBinding;
    public static final String TAG = MainFragment.class.getSimpleName();
    private ArrayList<View> mDayGroupChildList;
    private DayCheckModel[] mDayCheckModels;
    private TinyDB mTinyDB;
    private Fragment fragment;

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTinyDB = new TinyDB(getContext());
        mBinding.m01.setOnClickListener(this);
        mBinding.m02.setOnClickListener(this);
        mBinding.m03.setOnClickListener(this);
        mBinding.m04.setOnClickListener(this);
        mBinding.m05.setOnClickListener(this);
        mBinding.m06.setOnClickListener(this);

        if (mTinyDB.getInt(Constants.SETTING_VOLUME) == -1) {
            mTinyDB.putInt(Constants.SETTING_VOLUME, 2);
        }

        volumeControlThread();
    }

    private void volumeControlThread(){

        AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        double streamMaxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        double divideVolume = streamMaxVolume / 25;

        for (int i = 1; i <= 25; i++) {
//            // Log.d(TAG, "volumeControlThread: "+i+"분 - "+Math.round(divideVolume * i));
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                audio.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(streamMaxVolume / 4), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//            }
//        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.m01:
                fragment = MethodOfExecutionFragment.newInstance();
                ((MainActivity)getActivity()).setFragment(fragment);

                break;
            case R.id.m02:
                fragment = DeepSleepFragment.newInstance();
                ((MainActivity)getActivity()).setFragment(fragment);
                break;
            case R.id.m03:
                fragment = AwakeningFragment.newInstance();
                ((MainActivity)getActivity()).setFragment(fragment);
                break;
            case R.id.m04:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                builder.setTitle("");
                builder.setMessage("준비중인 기능 입니다.");
                builder.setCancelable(true);
                builder.setPositiveButton("확인", null);

                builder.show();
                break;
            case R.id.m05:
                fragment = RestFragment.newInstance();
                ((MainActivity)getActivity()).setFragment(fragment);
                break;
            case R.id.m06:
                fragment = NotAutoSleepFragment2.newInstance();
                ((MainActivity)getActivity()).setFragment(fragment);
                break;
        }
    }
}
