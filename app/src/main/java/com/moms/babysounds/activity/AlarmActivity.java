package com.moms.babysounds.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.moms.babysounds.R;
import com.moms.babysounds.broadcastReceiver.MusicReceivers;
import com.moms.babysounds.common.Constants;
import com.moms.babysounds.common.TinyDB;
import com.moms.babysounds.databinding.ActivityAlarmBinding;

import java.util.Calendar;

public class AlarmActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityAlarmBinding mBinding;
    private TinyDB mTinyDB;
    private long mTriggerTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_alarm);

        mTinyDB = new TinyDB(this);

        mBinding.after5Button.setOnClickListener(this);
        mBinding.endButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.after5Button:
                //서비스 켜는 알람? 아니면 여기서 그냥 소리

                if ()
                mTriggerTime = setTriggerTime();
                setPlayMusicAlarm(true, );
                break;

            case R.id.endButton:
                //할거 없음
                finish();
                break;
        }
    }

    private long setTriggerTime()
    {
        // timepicker
        Calendar curTime = Calendar.getInstance();
        curTime.set(Calendar.HOUR_OF_DAY, mTinyDB.getInt(Constants.ALARM_HOUR));
        curTime.set(Calendar.MINUTE, mTinyDB.getInt(Constants.ALARM_MIN));
        curTime.set(Calendar.SECOND, 0);
        curTime.set(Calendar.MILLISECOND, 0);
        long btime = curTime.getTimeInMillis();
        long triggerTime = btime;

        return triggerTime;
    }

    public void setPlayMusicAlarm(boolean start, final long time) {

        Intent alarmIntent = new Intent(this, MusicReceivers.class);
        alarmIntent.setAction(Constants.PLAY_MUSIC_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long delay = time;  //10분

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, setTriggerTime()+delay, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, setTriggerTime()+delay, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, setTriggerTime()+delay, pendingIntent);
            }
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }
}
