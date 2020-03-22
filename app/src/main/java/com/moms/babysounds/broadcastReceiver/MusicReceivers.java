package com.moms.babysounds.broadcastReceiver;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

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

    @Override
    public void onReceive(Context context, Intent intent) {
        mTinyDB = new TinyDB(MyApplication.getAppContext());
        String action = intent.getAction();
        if (Constants.PLAY_MUSIC_ACTION.equals(action)) {

            Log.d(TAG, "onReceive: dddddd");
            EventBus.getDefault().post(new PlayMusicEvent());
        }else if (Constants.WAKE_UP_ACTION.equals(action)){
            SimpleDateFormat format1 = new SimpleDateFormat ( "EEE", Locale.KOREA);

            String format_time1 = format1.format (System.currentTimeMillis());
            Log.d(TAG, "onReceive: "+format_time1);

            DayCheckListModel dayCheckListModel = mTinyDB.getObject(Constants.ALARM_DATE_LIST,DayCheckListModel.class);

            for (int i = 0; i < dayCheckListModel.getCheckModels().size(); i++) {
                if (dayCheckListModel.getCheckModels().get(i).getDay().equals(format_time1)){
                    if (dayCheckListModel.getCheckModels().get(i).isChecked()){
                        Log.d(TAG, "onReceive: 얍");
                        startMusicService();
//                        Intent intent1 = new Intent(MyApplication.getAppContext(), AlarmActivity.class);
//                        MyApplication.get
                    }
                }
            }
        }
    }

    public void startMusicService() {

        Intent wakeUpIntent = new Intent(MyApplication.getAppContext(), WakeUpService.class);
        wakeUpIntent.setAction(Constants.AWAKENING_ACTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MyApplication.getAppContext().startForegroundService(wakeUpIntent);
        } else {
            MyApplication.getAppContext().startService(wakeUpIntent);
        }
        Log.d(TAG, "onReceive: ddddddd");
    }
}