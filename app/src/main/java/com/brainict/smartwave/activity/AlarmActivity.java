package com.brainict.smartwave.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.brainict.smartwave.R;
import com.brainict.smartwave.broadcastReceiver.MusicReceivers;
import com.brainict.smartwave.common.Constants;
import com.brainict.smartwave.common.TinyDB;
import com.brainict.smartwave.databinding.ActivityAlarmBinding;
import com.brainict.smartwave.event.WakeUpServiceEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.brainict.smartwave.common.Constants.FIVE;
import static com.brainict.smartwave.common.Constants.MINUTE;

public class AlarmActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityAlarmBinding mBinding;
    private TinyDB mTinyDB;
    private long mTriggerTime;
    public static final String TAG = AlarmActivity.class.getSimpleName();
    private int mAfterTime;
    private BroadcastReceiver _tickReceiver;
    private MediaPlayer mPlayer;
    private Vibrator vibrator;

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
        setTime();
        if (mTinyDB.getBoolean(Constants.ALARM_AFTER_5_ALARM)) {
            mBinding.after5Button.setVisibility(View.VISIBLE);
            mBinding.after5Button.setOnClickListener(this);
        }

        mBinding.endButton.setOnClickListener(this);

        mAfterTime = 0;

        _tickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(Intent.ACTION_TIME_TICK)) {
                    setTime();
                }
            }
        };

        IntentFilter _intentFilter = new IntentFilter();
        _intentFilter.addAction(Intent.ACTION_TIME_TICK);

        registerReceiver(_tickReceiver, _intentFilter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        setRingTon();
        EventBus.getDefault().post(new WakeUpServiceEvent());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.after5Button:

                mTriggerTime = setTriggerTime();
                initAfterTriggerTime();

                setPlayMusicAlarm(true, Constants.MINUTE * mAfterTime);
                finish();
                break;

            case R.id.endButton:
                finish();
                break;
        }
    }

    public void initAfterTriggerTime() {
        if (System.currentTimeMillis() >= mTriggerTime + mAfterTime) {
            mAfterTime += FIVE * MINUTE;
            initAfterTriggerTime();
        }
    }

    public void setTime() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");
        SimpleDateFormat format2 = new SimpleDateFormat("MM월 dd일 EEEE");
        SimpleDateFormat format3 = new SimpleDateFormat("a");
        String time = format1.format(currentTime);
        String date = format2.format(currentTime);
        String ampm = format3.format(currentTime);

        mBinding.timeText.setText(time);
        mBinding.dateText.setText(date);
        mBinding.amPmText.setText(ampm);
    }

    public void setRingTon() {
        float sound = mTinyDB.getFloat(Constants.ALARM_SOUND_SIZE);
        boolean vib = mTinyDB.getBoolean(Constants.ALARM_VIB);
        if (vib) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {2000, 100, 2000, 100}; // 1초 진동, 0.05초 대기, 1초 진동, 0.05초 대기
            vibrator.vibrate(pattern, 0);
        }

        AudioManager audioManager =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        audioManager.getStreamMaxVolume(10);

        mPlayer = new MediaPlayer();

        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        try {
            mPlayer.setDataSource(this, alert);
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mPlayer.setLooping(true);  // 반복여부 지정
            mPlayer.prepare();    // 실행전 준비

        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();   // 실행 시작
    }

    private long setTriggerTime() {
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

    public void setPlayMusicAlarm(boolean start, long time) {

        Intent alarmIntent = new Intent(this, MusicReceivers.class);
        alarmIntent.setAction(Constants.AFTER_5_AWAKENING_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long delay = time;

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mTriggerTime + delay, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, mTriggerTime + delay, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, mTriggerTime + delay, pendingIntent);
            }
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (_tickReceiver != null)
            unregisterReceiver(_tickReceiver);

        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.release();

        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
