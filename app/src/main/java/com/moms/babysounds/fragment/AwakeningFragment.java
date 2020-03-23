package com.moms.babysounds.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.moms.babysounds.R;
import com.moms.babysounds.broadcastReceiver.MusicReceivers;
import com.moms.babysounds.common.Constants;
import com.moms.babysounds.common.TinyDB;
import com.moms.babysounds.databinding.FragmentAwakeningBinding;
import com.moms.babysounds.model.DayCheckListModel;
import com.moms.babysounds.model.DayCheckModel;
import com.moms.babysounds.service.WakeUpService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AwakeningFragment extends DefaultFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private FragmentAwakeningBinding mBinding;
    public static final String TAG = AwakeningFragment.class.getSimpleName();
    private ArrayList<View> mDayGroupChildList;
    private ArrayList<DayCheckModel> mDayCheckModels;
    private TinyDB mTinyDB;
    private Gson mGson;
    private Intent mMusicIntent;
    private long mTriggerTime;
    private Intent alarmIntent;
    private long mBefore30TriggerTime;
    private float mValueHz;

    public AwakeningFragment() {
    }

    public static AwakeningFragment newInstance(String param1, String param2) {
        AwakeningFragment fragment = new AwakeningFragment();
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_awakening, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTinyDB = new TinyDB(getContext());
        mGson = new Gson();

        setData();

        mDayGroupChildList = new ArrayList<>();
        for (int i = 0; i < mBinding.dayGroup.getChildCount(); i++) { //그룹안에 있는 뷰에다 요일 넣기
            View dayGroupChild = mBinding.dayGroup.getChildAt(i);
            final TextView dayText = (TextView) dayGroupChild.findViewById(R.id.dayText);
            CheckBox dayCheck = (CheckBox) dayGroupChild.findViewById(R.id.dayCheck);

            dayText.setText(mDayCheckModels.get(i).getDay());

            if (mDayCheckModels.get(i).isChecked()) {
                dayText.setTextColor(getResources().getColor(R.color.white));
                dayCheck.setChecked(true);
            } else {
                dayText.setTextColor(getResources().getColor(R.color.black));
                dayCheck.setChecked(false);
            }
            mDayCheckModels.get(i).setChecked(dayCheck.isChecked());

            final int finalI = i;
            dayCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDayCheckModels.get(finalI).isChecked()) {
                        dayText.setTextColor(getResources().getColor(R.color.black));
                    } else {
                        dayText.setTextColor(getResources().getColor(R.color.white));
                    }
                    mDayCheckModels.get(finalI).setChecked(((CheckBox) v).isChecked());
                }
            });
            mDayGroupChildList.add(dayGroupChild);
        }
        mBinding.confirmButton.setOnClickListener(this);
        mBinding.minButton.setOnClickListener(this);
        mBinding.plusButton.setOnClickListener(this);
//        mValueHz = 0.5f;
//        mValueTime = 60;
        mBinding.soundSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValueHz = ((float) progress / 10.0f);
                Log.d(TAG, "onProgressChanged: "+mValueHz);
//                mBinding.hzText.setText("수면파 ( " + mValueHz + "hz )");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBinding.topLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }

    public void setData() {
        int hour = mTinyDB.getInt(Constants.ALARM_HOUR);
        int min = mTinyDB.getInt(Constants.ALARM_MIN);
        boolean vab = mTinyDB.getBoolean(Constants.ALARM_VIB);
        boolean after5Alarm = mTinyDB.getBoolean(Constants.ALARM_AFTER_5_ALARM);
        float soundSize = mTinyDB.getFloat(Constants.ALARM_SOUND_SIZE);
        DayCheckListModel dayCheckModels2 = mTinyDB.getObject(Constants.ALARM_DATE_LIST, DayCheckListModel.class);

        if (hour != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBinding.timePicker.setHour(hour);
                mBinding.timePicker.setMinute(min);
            } else {
                mBinding.timePicker.setCurrentHour(hour);
                mBinding.timePicker.setCurrentMinute(min);
            }

            mBinding.vibrationSwitch.setChecked(vab);
            mBinding.after5Switch.setChecked(after5Alarm);
            if (soundSize == -1){
                mTinyDB.putFloat(Constants.ALARM_SOUND_SIZE,0.5f);
                mBinding.soundSize.setProgress(5);
            }else {
                mBinding.soundSize.setProgress((int) (soundSize * 10));
            }
        }

        if (dayCheckModels2 == null) {
            mDayCheckModels = new ArrayList<>();
            mDayCheckModels.add(new DayCheckModel("일"));
            mDayCheckModels.add(new DayCheckModel("월"));
            mDayCheckModels.add(new DayCheckModel("화"));
            mDayCheckModels.add(new DayCheckModel("수"));
            mDayCheckModels.add(new DayCheckModel("목"));
            mDayCheckModels.add(new DayCheckModel("금"));
            mDayCheckModels.add(new DayCheckModel("토"));
        } else {
            mDayCheckModels = dayCheckModels2.getCheckModels();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {

            case R.id.min_button:
                int progress = mBinding.soundSize.getProgress();
                if (progress < 1){
                }else {
                    mBinding.soundSize.setProgress(mBinding.soundSize.getProgress() - 1);
                }
                break;

            case R.id.plus_button:
                int progress2 = mBinding.soundSize.getProgress();
                if (progress2 > 9){
                }else {
                    mBinding.soundSize.setProgress(mBinding.soundSize.getProgress() + 1);
                }
                break;

            case R.id.confirmButton:

                Log.d(TAG, "onClick: "+mBinding.timePicker.getCurrentHour());
                Log.d(TAG, "onClick: "+mBinding.timePicker.getCurrentMinute());

                boolean dateCheck = false;
                for (int i = 0; i < mDayCheckModels.size(); i++) {
                    if (mDayCheckModels.get(i).isChecked()) {
                        dateCheck = true;
                        break;
                    }
                }

                if (dateCheck) {

                    DayCheckListModel dayCheckListModel = new DayCheckListModel(mDayCheckModels);
                    mTinyDB.putObject(Constants.ALARM_DATE_LIST, dayCheckListModel);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        mTinyDB.putInt(Constants.ALARM_HOUR, mBinding.timePicker.getHour());
                        mTinyDB.putInt(Constants.ALARM_MIN, mBinding.timePicker.getMinute());
                    } else {
                        mTinyDB.putInt(Constants.ALARM_HOUR, mBinding.timePicker.getCurrentHour());
                        mTinyDB.putInt(Constants.ALARM_MIN, mBinding.timePicker.getCurrentMinute());
                    }
                    mTinyDB.putBoolean(Constants.ALARM_VIB, mBinding.vibrationSwitch.isChecked());
                    mTinyDB.putBoolean(Constants.ALARM_AFTER_5_ALARM, mBinding.after5Switch.isChecked());
                    mTinyDB.putFloat(Constants.ALARM_SOUND_SIZE, mValueHz);

                    mBefore30TriggerTime = setTriggerTime(-Constants.MINUTE * 30);
                    mTriggerTime = setTriggerTime(0);

//                    SimpleDateFormat format1 = new SimpleDateFormat ( "MM월 dd일 HH시 mm분 ss");
//                    String format_time2 = format1.format (mBefore30TriggerTime);
//                    String format_time3 = format1.format (mTriggerTime);
//
//                    Log.d(TAG, "setTriggerTime2: "+format_time2);
//                    Log.d(TAG, "setTriggerTime2: "+format_time3);

                    setOut30Alarm(false, 0);
                    setIn30Alarm(false, 0);

                    if (System.currentTimeMillis() < mTriggerTime -Constants.MINUTE * 31){

//                        Log.d(TAG, "onClick: 30분 뒤");
                        setOut30Alarm(true,mBefore30TriggerTime);
//                        SimpleDateFormat format1 = new SimpleDateFormat("EEE", Locale.KOREA);
//String day = "";
//                        String format_time1 = format1.format(System.currentTimeMillis());  //오늘 요일
//                        if
//                        for (int i = 0; i < mDayCheckModels.size(); i++) {
//                            if (mDayCheckModels.get(i).isChecked()){
//                                day = mDayCheckModels.get(i).getDay();
//                            }
//                        }
//                        SimpleDateFormat format1 = new SimpleDateFormat ( "MM월 dd일 HH시 mm분");
                        SimpleDateFormat format1 = new SimpleDateFormat ("HH시 mm분");
                        String format_time2 = format1.format (mBefore30TriggerTime);
                        String format_time3 = format1.format (mTriggerTime);
                        Log.d(TAG, "onClick: "+format_time2);
                        Log.d(TAG, "onClick: "+format_time3);
                        Toast.makeText(getContext(), "매주 "+format_time3+"에 알람이 울립니다.", Toast.LENGTH_LONG).show();

                    }else {
//                        Log.d(TAG, "onClick: 30분 안");
                        setIn30Alarm(true, mTriggerTime);
                        setOut30Alarm(true, mBefore30TriggerTime);
                        SimpleDateFormat format1 = new SimpleDateFormat ( "HH시 mm분");
                        String format_time2 = format1.format (mBefore30TriggerTime);
                        String format_time3 = format1.format (mTriggerTime);
                        Log.d(TAG, "onClick: "+format_time2);
                        Log.d(TAG, "onClick: "+format_time3);
                        Toast.makeText(getContext(), "오늘"+format_time3+"에 알람이 울립니다.", Toast.LENGTH_LONG).show();
                    }

//                    SimpleDateFormat format1 = new SimpleDateFormat ( "MM월 dd일 HH시 mm분 ss");
//
//                    String format_time1 = format1.format (mBefore30TriggerTime);
//                    String format_time2 = format1.format (mTriggerTime);
//
//                    Log.d(TAG, "onClick: "+format_time1);
//                    Log.d(TAG, "onClick: "+format_time2);
//
//                    Toast.makeText(getContext(), format_time2+"에 알람이 울립니다.", Toast.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStack();

                } else {
                    Toast.makeText(getContext(), "요일을 선택하세요.", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    public void startMusicService() {

        mMusicIntent = new Intent(getContext(), WakeUpService.class);
        mMusicIntent.setAction(Constants.AWAKENING_ACTION);
        Bundle bundle = new Bundle();
        mMusicIntent.putExtras(bundle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(mMusicIntent);
        } else {
            getContext().startService(mMusicIntent);
        }
    }

    public void stopMusicService() {

        mMusicIntent = new Intent(getContext(), WakeUpService.class);
        mMusicIntent.setAction(Constants.STOP_SERVICE_ACTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(mMusicIntent);
        } else {
            getContext().stopService(mMusicIntent);
        }
    }

    private long setTriggerTime(long beforeTime)
    {
        // current Time
        long atime = System.currentTimeMillis();
        // timepicker
        Calendar curTime = Calendar.getInstance();
        curTime.set(Calendar.HOUR_OF_DAY, mTinyDB.getInt(Constants.ALARM_HOUR));
        curTime.set(Calendar.MINUTE, mTinyDB.getInt(Constants.ALARM_MIN));
        curTime.set(Calendar.SECOND, 0);
        curTime.set(Calendar.MILLISECOND, 0);
        long btime = curTime.getTimeInMillis()+ beforeTime;
        long triggerTime = btime;
        if (atime > btime)
            triggerTime += 1000 * 60 * 60 * 24;

        return triggerTime;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setOut30Alarm(boolean start, long time) {
//        mTinyDB.putLong("before30TriggerTime",mBefore30TriggerTime);
        long intervalTime = 24 * 60 * 60 * 1000;// 24시간
        alarmIntent = new Intent(getContext(), MusicReceivers.class);
        alarmIntent.setAction(Constants.WAKE_UP_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        long delay = time;  //10분

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mBefore30TriggerTime, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, mBefore30TriggerTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, mBefore30TriggerTime, pendingIntent);
            }
//                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, mBefore30TriggerTime,intervalTime, pendingIntent);
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }

    public void setIn30Alarm(boolean start, long time) {
        alarmIntent = new Intent(getContext(), MusicReceivers.class);
        alarmIntent.setAction(Constants.IN_30_MIN_AWAKENING_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        long delay = time;  //10분

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mTriggerTime, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, mTriggerTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, mTriggerTime, pendingIntent);
            }
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }
}
