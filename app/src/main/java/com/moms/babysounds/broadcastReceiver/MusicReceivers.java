package com.moms.babysounds.broadcastReceiver;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.moms.babysounds.MyApplication;
import com.moms.babysounds.activity.AlarmActivity;
import com.moms.babysounds.common.Constants;
import com.moms.babysounds.common.TinyDB;
import com.moms.babysounds.event.PlayMusicEvent;
import com.moms.babysounds.model.DayCheckListModel;
import com.moms.babysounds.service.WakeUpService;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
        Log.d(TAG, "onReceive: "+action);
        if (Constants.PLAY_MUSIC_ACTION.equals(action)) {

            EventBus.getDefault().post(new PlayMusicEvent());
        } else if (Constants.WAKE_UP_ACTION.equals(action)) {
            long mTime = setTriggerTime(-Constants.MINUTE * 30);
            SimpleDateFormat format2 = new SimpleDateFormat("MM월 dd일 HH시 mm분 ss");
            String format_time2 = format2.format(mTime);

            setOut30Alarm(context, true, mTime);
            SimpleDateFormat format1 = new SimpleDateFormat("EEE", Locale.KOREA);

            String format_time1 = format1.format(System.currentTimeMillis());

            DayCheckListModel dayCheckListModel = mTinyDB.getObject(Constants.ALARM_DATE_LIST, DayCheckListModel.class);

            for (int i = 0; i < dayCheckListModel.getCheckModels().size(); i++) {
                if (dayCheckListModel.getCheckModels().get(i).getDay().equals(format_time1)) {
                    if (dayCheckListModel.getCheckModels().get(i).isChecked()) {
                        startMusicService(context);

                    }
                }
            }
        } else if (Constants.IN_30_MIN_AWAKENING_ACTION.equals(action)) {
            Intent wakeUpIntent = new Intent(context, WakeUpService.class);
            wakeUpIntent.setAction(Constants.IN_30_MIN_AWAKENING_ACTION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(wakeUpIntent);
            } else {
                context.startService(wakeUpIntent);
            }

        } else if (Constants.AFTER_5_AWAKENING_ACTION.equals(action)) {
            Intent wakeUpIntent = new Intent(context, WakeUpService.class);
            wakeUpIntent.setAction(Constants.AFTER_5_AWAKENING_ACTION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(wakeUpIntent);
            } else {
                context.startService(wakeUpIntent);
            }

        } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {

            Log.d(TAG, "onReceive: ");

            mBefore30TriggerTime = setBootTriggerTime(-Constants.MINUTE * 30);
            mTriggerTime = setBootTriggerTime(0);

            Log.d(TAG, "onReceive: "+mBefore30TriggerTime);
            Log.d(TAG, "onReceive: "+mTriggerTime);

            if (System.currentTimeMillis() < mTriggerTime - Constants.MINUTE * 31) {

                        Log.d(TAG, "onClick: 30분 뒤");
                setBootOut30Alarm(context,true, mBefore30TriggerTime);
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
                SimpleDateFormat format1 = new SimpleDateFormat("HH시 mm분");
                String format_time2 = format1.format(mBefore30TriggerTime);
                String format_time3 = format1.format(mTriggerTime);
                Log.d(TAG, "onClick: " + format_time2);
                Log.d(TAG, "onClick: " + format_time3);

            } else {
                        Log.d(TAG, "onClick: 30분 안");
                setBootIn30Alarm(context, true, mTriggerTime);
                setBootOut30Alarm(context, true, mBefore30TriggerTime);
                SimpleDateFormat format1 = new SimpleDateFormat ( "HH시 mm분");
                String format_time2 = format1.format (mBefore30TriggerTime);
                String format_time3 = format1.format (mTriggerTime);
                Log.d(TAG, "onClick: "+format_time2);
                Log.d(TAG, "onClick: "+format_time3);
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
//        mTinyDB.putLong("before30TriggerTime",mBefore30TriggerTime);
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
//                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, mBefore30TriggerTime,intervalTime, pendingIntent);
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }

    private long setBootTriggerTime(long beforeTime)
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


    private long setTriggerTime(long beforeTime) {
        // current Time
//        long atime = System.currentTimeMillis();

        // timepicker
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
//                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, mBefore30TriggerTime,intervalTime, pendingIntent);
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }

    public void startMusicService(Context context) {

        Intent wakeUpIntent = new Intent(context, WakeUpService.class);
        wakeUpIntent.setAction(Constants.AWAKENING_ACTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(wakeUpIntent);
        } else {
            context.startService(wakeUpIntent);
        }
        Log.d(TAG, "onReceive: ddddddd");
    }
}