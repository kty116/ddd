package com.brainict.smartwave.broadcastReceiver;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.brainict.smartwave.R;
import com.brainict.smartwave.activity.AlarmActivity;
import com.brainict.smartwave.common.Commonlib;
import com.brainict.smartwave.common.Constants;
import com.brainict.smartwave.common.TinyDB;
import com.brainict.smartwave.event.PlayMusicEvent;
import com.brainict.smartwave.model.DayCheckListModel;
import com.brainict.smartwave.service.WakeUpService;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.brainict.smartwave.common.Constants.TIME_FORMAT;

public class MusicReceivers extends BroadcastReceiver {

    public static final String TAG = MusicReceivers.class.getSimpleName();
    private TinyDB mTinyDB;
    private Intent alarmIntent;
    private long mBefore30TriggerTime;
    private long mTriggerTime;

    @Override
    public void onReceive(Context context, Intent intent) {
        mTinyDB = new TinyDB(context);
        String action = intent.getAction();
        if (Constants.PLAY_MUSIC_ACTION.equals(action)) {

            EventBus.getDefault().post(new PlayMusicEvent());
        } else if (Constants.WAKE_UP_ACTION.equals(action)) {

            long mTime = setTriggerTime(-Constants.MINUTE * 30);
//            SimpleDateFormat format2 = new SimpleDateFormat(TIME_FORMAT);
//            String format_time2 = format2.format(mTime);

            setOut30Alarm(context, true, mTime);
            SimpleDateFormat format1 = new SimpleDateFormat("EEE", Locale.KOREA);

            String format_time1 = format1.format(System.currentTimeMillis());

            DayCheckListModel dayCheckListModel = mTinyDB.getObject(Constants.ALARM_DATE_LIST, DayCheckListModel.class);
            if (dayCheckListModel != null) {
                for (int i = 0; i < dayCheckListModel.getCheckModels().size(); i++) {
                    if (dayCheckListModel.getCheckModels().get(i).getDay().equals(format_time1)) {
                        if (dayCheckListModel.getCheckModels().get(i).isChecked()) {
                            startWakeUpService(context, Constants.AWAKENING_ACTION);
                        }
                    }
                }
            }
        } else if (Constants.IN_30_MIN_AWAKENING_ACTION.equals(action)) {

            startWakeUpService(context, Constants.IN_30_MIN_AWAKENING_ACTION);

        } else if (Constants.AFTER_5_AWAKENING_ACTION.equals(action)) {

            startWakeUpService(context, Constants.AFTER_5_AWAKENING_ACTION);

        } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {

            mBefore30TriggerTime = setBootTriggerTime(-Constants.MINUTE * 30);
            mTriggerTime = setBootTriggerTime(0);

            if (System.currentTimeMillis() < mTriggerTime - Constants.MINUTE * 31) {
                setBootOut30Alarm(context, true, mBefore30TriggerTime);
            } else {
                setBootIn30Alarm(context, true, mTriggerTime);
                setBootOut30Alarm(context, true, mBefore30TriggerTime);
            }
        }
    }

    public void setBootIn30Alarm(Context context, boolean start, long time) {
        alarmIntent = new Intent(context, MusicReceivers.class);
        alarmIntent.setAction(Constants.IN_30_MIN_AWAKENING_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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

    public void setBootOut30Alarm(Context context, boolean start, long time) {
        long intervalTime = 24 * 60 * 60 * 1000;// 24시간
        alarmIntent = new Intent(context, MusicReceivers.class);
        alarmIntent.setAction(Constants.WAKE_UP_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long delay = time;  //10분

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mBefore30TriggerTime, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, mBefore30TriggerTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, mBefore30TriggerTime, pendingIntent);
            }
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
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


    private long setTriggerTime(long beforeTime) {
        Calendar curTime = Calendar.getInstance();
        curTime.set(Calendar.HOUR_OF_DAY, mTinyDB.getInt(Constants.ALARM_HOUR));
        curTime.set(Calendar.MINUTE, mTinyDB.getInt(Constants.ALARM_MIN));
        curTime.set(Calendar.SECOND, 0);
        curTime.set(Calendar.MILLISECOND, 0);
        long btime = curTime.getTimeInMillis() + beforeTime;
        long triggerTime = btime;
//        if (atime > btime)
        triggerTime += 1000 * 60 * 60 * 24;

        return triggerTime;
    }

    public void setOut30Alarm(Context context, boolean start, long time) {
        long intervalTime = 24 * 60 * 60 * 1000;// 24시간
        long currentTime = System.currentTimeMillis();
        Intent alarmIntent = new Intent(context, MusicReceivers.class);
        alarmIntent.setAction(Constants.WAKE_UP_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long delay = time;  //10분

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }

    public void startWakeUpService(Context context, String action) {
            Intent wakeUpIntent = new Intent(context, WakeUpService.class);
            wakeUpIntent.setAction(action);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(wakeUpIntent);
            } else {
                context.startService(wakeUpIntent);
            }
        }
}