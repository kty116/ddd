package com.brainict.smartwave.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.brainict.smartwave.R;
import com.brainict.smartwave.common.Constants;
import com.brainict.smartwave.common.TinyDB;
import com.brainict.smartwave.databinding.FragmentNotAutoSleep2Binding;
import com.brainict.smartwave.databinding.FragmentNotAutoSleepBinding;

public class NotAutoSleepFragment2 extends DefaultFragment implements View.OnClickListener {
    private FragmentNotAutoSleep2Binding mBinding;
    public static final String TAG = NotAutoSleepFragment2.class.getSimpleName();
    private TinyDB mTinyDB;
    private double mValueHz;
    private int mValueTime;
    private int mVolume;

    public NotAutoSleepFragment2() {
    }

    public static NotAutoSleepFragment2 newInstance() {
        NotAutoSleepFragment2 fragment = new NotAutoSleepFragment2();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_not_auto_sleep2, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTinyDB = new TinyDB(getContext());

        mValueHz = mTinyDB.getDouble(Constants.SETTING_HZ, 0);
        mValueTime = mTinyDB.getInt(Constants.SETTING_TIME);
        mVolume = mTinyDB.getInt(Constants.SETTING_VOLUME);

        mBinding.volume1Button.volumeText.setText("1");
        mBinding.volume2Button.volumeText.setText("2");
        mBinding.volume3Button.volumeText.setText("3");

        if (mValueHz == 0) {
            mValueHz = 5.0;
            mValueTime = 60;
            mBinding.seekBarHz.setProgress(50);
            mBinding.seekBarMin.setProgress(60);
//
            mBinding.hzText.setText("수면파 ( " + mValueHz + "hz )");
            mBinding.timeText.setText("시간 ( " + mValueTime + "분 )");
            mTinyDB.putDouble(Constants.SETTING_HZ, mValueHz);
            mTinyDB.putInt(Constants.SETTING_TIME, mValueTime);
        } else {
            mBinding.seekBarHz.setProgress((int) (mValueHz * 10));
            mBinding.seekBarMin.setProgress(mValueTime);
            mBinding.hzText.setText("수면파 ( " + mValueHz + "hz )");
            mBinding.timeText.setText("시간 ( " + mValueTime + "분 )");
        }

        if (mVolume == -1) {
            mVolume = 2;
        }

        for (int i = 0; i < mBinding.volumeLayout.getChildCount(); i++) {
            View soundSizeButton = mBinding.volumeLayout.getChildAt(i);
            final CheckBox soundSizeCheck = soundSizeButton.findViewById(R.id.volumeCheck);
            final TextView soundSizeText = soundSizeButton.findViewById(R.id.volumeText);

            if (soundSizeText.getText().equals("" + mVolume)) {
                soundSizeCheck.setChecked(true);
                soundSizeText.setTextColor(getResources().getColor(R.color.white));
            } else {
                soundSizeCheck.setChecked(false);
                soundSizeText.setTextColor(getResources().getColor(R.color.navy));
            }

            soundSizeCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setVolume(-2);
                    mVolume = Integer.parseInt(soundSizeText.getText().toString());

                    if (((CheckBox) v).isChecked()) {
                        soundSizeCheck.setChecked(true);
                        soundSizeText.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        if (soundSizeText.getText().equals("" + mVolume)) {  //현재 누른거랑 같은거 누르면
                            soundSizeCheck.setChecked(true);
                            soundSizeText.setTextColor(getResources().getColor(R.color.white));
                        }
                    }
                }
            });
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

    public void setVolume(int volume) {
        for (int i = 0; i < mBinding.volumeLayout.getChildCount(); i++) {
            View soundSizeButton = mBinding.volumeLayout.getChildAt(i);
            final CheckBox soundSizeCheck = soundSizeButton.findViewById(R.id.volumeCheck);
            TextView soundSizeText = soundSizeButton.findViewById(R.id.volumeText);

            if (soundSizeText.getText().equals("" + volume)) {
                soundSizeCheck.setChecked(true);
                soundSizeText.setTextColor(getResources().getColor(R.color.white));
            } else {
                soundSizeCheck.setChecked(false);
                soundSizeText.setTextColor(getResources().getColor(R.color.navy));
            }
        }
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
                mTinyDB.putInt(Constants.SETTING_VOLUME, mVolume);
//                // Log.d(TAG, "onClick: "+mVolume);
                getActivity().getSupportFragmentManager().popBackStack();
//                ArrayList<AudioSetModel> audioSetModelList = new ArrayList<>();
//                audioSetModelList.add(new AudioSetModel(MusicService.Audio.SINE, mValueHz, Constants.MINUTE * mValueTime, false));
//                ((MainActivity) getActivity()).setFragment(InduceSleepFragment.newInstance(audioSetModelList, ""));
                break;
        }
    }
}
