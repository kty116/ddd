package com.moms.babysounds.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.moms.babysounds.R;
import com.moms.babysounds.activity.MainActivity;
import com.moms.babysounds.common.Constants;
import com.moms.babysounds.common.TinyDB;
import com.moms.babysounds.databinding.FragmentNotAutoSleepBinding;
import com.moms.babysounds.model.AudioSetModel;
import com.moms.babysounds.service.MusicService;

import java.util.ArrayList;

public class NotAutoSleepFragment extends DefaultFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private FragmentNotAutoSleepBinding mBinding;
    public static final String TAG = NotAutoSleepFragment.class.getSimpleName();
    private TinyDB mTinyDB;
    private double mValueHz;
    private int mValueTime;

    public NotAutoSleepFragment() {
    }

    public static NotAutoSleepFragment newInstance(String param1, String param2) {
        NotAutoSleepFragment fragment = new NotAutoSleepFragment();
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_not_auto_sleep, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTinyDB = new TinyDB(getContext());

        mValueHz = 5.0;
        mValueTime = 60;
        mBinding.confirmButton.setOnClickListener(this);
        mBinding.hzMinButton.setOnClickListener(this);
        mBinding.hzPlusButton.setOnClickListener(this);
        mBinding.timeMinButton.setOnClickListener(this);
        mBinding.timePlusButton.setOnClickListener(this);

        mBinding.topLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mBinding.seekBarHz.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValueHz = ((float) progress / 10.0);
                mBinding.hzText.setText("수면파 ( " + mValueHz + "hz )");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBinding.seekBarMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValueTime = progress;
                mBinding.timeText.setText("시간 ( " + mValueTime + "분 )");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.hzMinButton:
                int progress = mBinding.seekBarHz.getProgress();
                if (progress < 31){
                }else {
                    mBinding.seekBarHz.setProgress(mBinding.seekBarHz.getProgress() - 1);
                }
                break;

            case R.id.hzPlusButton:
                int progress2 = mBinding.seekBarHz.getProgress();
                if (progress2 > 69){
                }else {
                    mBinding.seekBarHz.setProgress(mBinding.seekBarHz.getProgress() + 1);
                }
                break;

            case R.id.timeMinButton:
                int progress3 = mBinding.seekBarMin.getProgress();
                if (progress3 < 31){
                }else {
                    mBinding.seekBarMin.setProgress(mBinding.seekBarMin.getProgress() - 1);
                }
                break;

            case R.id.timePlusButton:
                int progress4 = mBinding.seekBarMin.getProgress();
                if (progress4 > 69){
                }else {
                    mBinding.seekBarMin.setProgress(mBinding.seekBarMin.getProgress() + 1);
                }
                break;
            case R.id.confirmButton:
                ArrayList<AudioSetModel> audioSetModelList = new ArrayList<>();
                audioSetModelList.add(new AudioSetModel(MusicService.Audio.SINE, mValueHz, Constants.MINUTE * mValueTime, false));
                ((MainActivity) getActivity()).setFragment(InduceSleepFragment.newInstance(audioSetModelList, ""));
                break;
        }
    }
}
