package com.brainict.smartwave.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.brainict.smartwave.R;
import com.brainict.smartwave.activity.MainActivity;
import com.brainict.smartwave.common.Constants;
import com.brainict.smartwave.common.TinyDB;
import com.brainict.smartwave.databinding.FragmentNotAutoSleepBinding;
import com.brainict.smartwave.model.AudioSetModel;

import java.util.ArrayList;

public class NotAutoSleepFragment extends DefaultFragment implements View.OnClickListener {
    private FragmentNotAutoSleepBinding mBinding;
    public static final String TAG = NotAutoSleepFragment.class.getSimpleName();
    private TinyDB mTinyDB;
    private double mValueHz;
    private int mValueTime;

    public NotAutoSleepFragment() {
    }

    public static NotAutoSleepFragment newInstance() {
        NotAutoSleepFragment fragment = new NotAutoSleepFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        mValueHz = mTinyDB.getDouble(Constants.SETTING_HZ, 0);
        mValueTime = mTinyDB.getInt(Constants.SETTING_TIME);

        if (mValueHz == 0) {
            mValueHz = 5.0;
            mValueTime = 60;
            mBinding.seekBarHz.setProgress(50);
            mBinding.seekBarMin.setProgress(60);
            mBinding.hzText.setText("수면파 ( " + mValueHz + "hz )");
            mBinding.timeText.setText("시간 ( " + mValueTime + "분 )");
            mTinyDB.putDouble(Constants.SETTING_HZ, mValueHz);
            mTinyDB.putInt(Constants.SETTING_TIME, mValueTime);
        } else {
            mBinding.hzText.setText("수면파 ( " + mValueHz + "hz )");
            mBinding.timeText.setText("시간 ( " + mValueTime + "분 )");
            mBinding.seekBarHz.setProgress((int) (mValueHz * 10));
            mBinding.seekBarMin.setProgress(mValueTime);
        }

        mBinding.confirmButton.setOnClickListener(this);
        mBinding.hzMinButton.setOnClickListener(this);
        mBinding.hzPlusButton.setOnClickListener(this);
        mBinding.timeMinButton.setOnClickListener(this);
        mBinding.timePlusButton.setOnClickListener(this);
        mBinding.layoutDefault.homeButton.setOnClickListener(this);
        mBinding.layoutDefault.settingButton.setVisibility(View.GONE);

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
                if (progress < 31) {
                } else {
                    mBinding.seekBarHz.setProgress(mBinding.seekBarHz.getProgress() - 1);
                }
                break;

            case R.id.hzPlusButton:
                int progress2 = mBinding.seekBarHz.getProgress();
                if (progress2 > 69) {
                } else {
                    mBinding.seekBarHz.setProgress(mBinding.seekBarHz.getProgress() + 1);
                }
                break;

            case R.id.timeMinButton:
                int progress3 = mBinding.seekBarMin.getProgress();
                if (progress3 < 31) {
                } else {
                    mBinding.seekBarMin.setProgress(mBinding.seekBarMin.getProgress() - 1);
                }
                break;

            case R.id.timePlusButton:
                int progress4 = mBinding.seekBarMin.getProgress();
                if (progress4 > 69) {
                } else {
                    mBinding.seekBarMin.setProgress(mBinding.seekBarMin.getProgress() + 1);
                }
                break;
            case R.id.confirmButton:
                mTinyDB.putDouble(Constants.SETTING_HZ, mValueHz);
                mTinyDB.putInt(Constants.SETTING_TIME, mValueTime);
                ArrayList<AudioSetModel> audioSetModelList = new ArrayList<>();
                audioSetModelList.add(new AudioSetModel(0, mValueHz * Constants.SETTING_HZ_UP, Constants.MINUTE * mValueTime, false));
                ((MainActivity) getActivity()).setFragment(InduceSleepFragment.newInstance(audioSetModelList));
                break;
        }
    }
}
