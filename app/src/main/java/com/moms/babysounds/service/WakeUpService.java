package com.moms.babysounds.service;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.RenderScript;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.karlotoy.perfectune.instance.PerfectTune;
import com.moms.babysounds.R;
import com.moms.babysounds.activity.AlarmActivity;
import com.moms.babysounds.activity.MainActivity;
import com.moms.babysounds.broadcastReceiver.MusicReceivers;
import com.moms.babysounds.common.Constants;
import com.moms.babysounds.common.TinyDB;
import com.moms.babysounds.event.MessageEvent;
import com.moms.babysounds.event.MusicServiceEvent;
import com.moms.babysounds.event.PlayMusicEvent;
import com.moms.babysounds.event.WakeUpServiceEvent;
import com.moms.babysounds.model.AudioSetModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class WakeUpService extends Service {

    public final String TAG = WakeUpService.class.getSimpleName();


    private NotificationCompat.Builder mStateNoti;
    //    private Audio audio;
    private double mFrequency;
    private PerfectTune mPerfectTune;
    //    private Audio audio;
    private final int duration = 60; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 440; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];
    private Handler handler;
    private AudioTrack mAudioTrack;
    private Audio audio;
    private boolean mChangeAudio = false;
    private ArrayList<AudioSetModel> mAudioSetModelList;
    private TinyDB mTinyDB;
    private Audio audio2;
    private long mTriggerTime;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        Log.d(TAG, "onCreate: dddddddd");
        createNoti("");
        EventBus.getDefault().register(this);
        mTinyDB = new TinyDB(getApplicationContext());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (audio != null) {
            audio.stop();
        }

        if (audio2 != null) {
            audio2.stop();
        }

        NotificationManager nManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.cancel(10);

        setPlayMusicAlarm(false, 0);

        EventBus.getDefault().unregister(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {

                    case Constants.AFTER_5_AWAKENING_ACTION:
                        createActiNoti("");
//                        NotificationManager nManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
//                        nManager.cancel(10);
                        break;

                    case Constants.IN_30_MIN_AWAKENING_ACTION:
                        createActiNoti("");
//                        NotificationManager nManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
//                        nManager.cancel(10);
                        break;

                    case Constants.AWAKENING_ACTION: //서비스 시작
                        createNoti("기상유도");
//                        setPlayMusicAlarm(false, 0);
//                        if (audio != null) {
//                            audio.stop();
//                        }
//
//                        if (audio2 != null) {
//                            audio2.stop();
//                        }
                        audio = new Audio();
                        audio2 = new Audio();
                        mTriggerTime = setTriggerTime();

                        SimpleDateFormat format1 = new SimpleDateFormat ( "MM월 dd일 HH시 mm분 ss");

                        String format_time1 = format1.format (mTriggerTime - Constants.MINUTE * 20);
                        String format_time2 = format1.format (mTriggerTime - Constants.MINUTE * 10);
                        String format_time3 = format1.format (mTriggerTime);

                        Log.d(TAG, "onClick: "+format_time1);
                        Log.d(TAG, "onClick: "+format_time2);
                        Log.d(TAG, "onClick: "+format_time3);

                        mAudioSetModelList = new ArrayList<>();
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 10, mTriggerTime - Constants.MINUTE * 20, false));
                        mAudioSetModelList.add(new AudioSetModel(Audio.SQUARE, 10, mTriggerTime - Constants.MINUTE * 10, false));
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 13, mTriggerTime, false));
                        if (audio != null) {
                            audio.start();
                            setAudio(mAudioSetModelList, 0);
                        }

                        break;

                    case Constants.STOP_SERVICE_ACTION:
                        EventBus.getDefault().post(new MusicServiceEvent(false));
                        stopSelf();

                        break;
                }
            }
        }
        return START_NOT_STICKY;
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

    public void setAudio(ArrayList<AudioSetModel> audioSetModelList, int index) {
       if (index == 2){
           Log.d(TAG, "setAudio: "+index);
           audio2.start();

           audio2.waveform = Audio.SQUARE;
           audio2.frequency = 13;

       }
        AudioSetModel audioSetModel = audioSetModelList.get(index);
        audio.waveform = audioSetModel.getAudioWaveForm();
        audio.frequency = audioSetModel.getHz();

        audioSetModel.setAlreadyPlayed(true);

        setPlayMusicAlarm(true, audioSetModel.getTime());

    }

//    private long setTriggerTime()
//    {
//        // current Time
//        long atime = System.currentTimeMillis();
//        // timepicker
//        Calendar curTime = Calendar.getInstance();
//        curTime.set(Calendar.HOUR_OF_DAY, mTinyDB.getInt(Constants.ALARM_HOUR));
//        curTime.set(Calendar.MINUTE, mTinyDB.getInt(Constants.ALARM_MIN));
//        curTime.set(Calendar.SECOND, 0);
//        curTime.set(Calendar.MILLISECOND, 0);
//        long btime = curTime.getTimeInMillis();
//        long triggerTime = btime;
//        if (atime > btime)
//            triggerTime += 1000 * 60 * 60 * 24;
//
//        return triggerTime;
//    }

    public void setPlayMusicAlarm(boolean start, final long time) {

        Intent alarmIntent = new Intent(this, MusicReceivers.class);
        alarmIntent.setAction(Constants.PLAY_MUSIC_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long delay = time;  //10분

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delay, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, delay, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, delay, pendingIntent);
            }
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        if (event instanceof MusicServiceEvent) {  //서비스 종료시
            MusicServiceEvent musicService = (MusicServiceEvent) event;

        } else if (event instanceof PlayMusicEvent) {
            for (int i = 0; i < mAudioSetModelList.size(); i++) {

                if (mAudioSetModelList.get(i).isAlreadyPlayed()) {
                    if (i == mAudioSetModelList.size() - 1) {  //마지막 이면 끄기
//                        Log.d(TAG, "onMessageEvent: ddddddd");
                        createActiNoti("");
//                        Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);

//                        stopForeground(true);
//                        EventBus.getDefault().post(new MusicServiceEvent(false));
//                        stopSelf();
                    }
                } else {
                    setAudio(mAudioSetModelList, i);
                    break;
                }
            }
        }else if (event instanceof WakeUpServiceEvent){
           stopSelf();
        }
    }

    // Audio
    protected class Audio implements Runnable
    {
        protected static final int SINE = 0;
        protected static final int SQUARE = 1;
        protected static final int SAWTOOTH = 2;

        protected int waveform ;
        protected boolean mute;

        protected double frequency;
        protected double level;

        protected Thread thread;

        private AudioTrack audioTrack;

        protected Audio()
        {
            frequency = 5.0;
            level = 1.0;
        }

        // Start
        protected void start()
        {
            thread = new Thread(this, "Audio");
            thread.start();
        }

        // Stop
        protected void stop()
        {
            Thread t = thread;
            thread.interrupt();
            thread = null;

            // Wait for the thread to exit
//            while (t != null && t.isAlive())
//                Thread.yield();
        }

        public void run()
        {
            processAudio();
        }

        // Process audio
        protected void processAudio()
        {
            short buffer[];

            int rate =
                    AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
            int minSize =
                    AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);

            // Find a suitable buffer size
            int sizes[] = {1024, 2048, 4096, 8192, 16384, 32768};
            int size = 0;

            for (int s : sizes)
            {
                if (s > minSize)
                {
                    size = s;
                    break;
                }
            }

            final double K = 2.0 * Math.PI / rate;

            // Create the audio track
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    size, AudioTrack.MODE_STREAM);
            // Check audiotrack
            if (audioTrack == null)
                return;

            // Check state
            int state = audioTrack.getState();

            if (state != AudioTrack.STATE_INITIALIZED)
            {
                audioTrack.release();
                return;
            }

            audioTrack.play();

            // Create the buffer
            buffer = new short[size];

            // Initialise the generator variables
            double f = frequency;
            double l = 0.0;
            double q = 0.0;

            while (thread != null)
            {
                try {
                    // Fill the current buffer
                    for (int i = 0; i < buffer.length; i++) {
                        f += (frequency - f) / 4096.0;
                        l += ((mute ? 0.0 : level) * 16384.0 - l) / 4096.0;
                        q += (q < Math.PI) ? f * K : (f * K) - (2.0 * Math.PI);

                        switch (waveform) {
                            case SINE:
                                buffer[i] = (short) Math.round(Math.sin(q) * l);
                                break;

                            case SQUARE:
                                buffer[i] = (short) ((q > 0.0) ? l : -l);
                                break;

                            case SAWTOOTH:
                                buffer[i] = (short) Math.round((q / Math.PI) * l);
                                break;
                        }
                    }

                    audioTrack.write(buffer, 0, buffer.length);
                }catch (Exception e){
                }
            }

            audioTrack.stop();
            audioTrack.release();
        }
    }

//    public void alramNoti(String text) {
//
//        String channelId = "smart_wave_channel_id";
//        String channelName = "smart_wave_channel_name";
//
//        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//
//            int importance = NotificationManager.IMPORTANCE_LOW;
//            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
//            notifManager.createNotificationChannel(mChannel);
//        }
//
//        mStateNoti = new NotificationCompat.Builder(this, channelId)
//                .setContentTitle(getString(R.string.app_name))
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentIntent(clickNotiPendingIntent());
//
//        mStateNoti.addAction(R.mipmap.ic_launcher_round, "종료", stopScanPendingIntent());
//
//        if (!text.isEmpty()) {
//            mStateNoti.setContentText(text);
//        }
//
//        startForeground(1, mStateNoti.build());
//    }

    public void createActiNoti(String text) {

        String channelId = "smart_wave_channel_i";
        String channelName = "smart_wave_channel_nam";

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);
        }

        mStateNoti = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(alarmActivityPendingIntent(),true);

        if (!text.isEmpty()) {
            mStateNoti.setContentText(text);
        }
//        startForeground(10, mStateNoti.build());
        notifManager.notify(10, mStateNoti.build());
    }

    public void createNoti(String text) {

        String channelId = "smart_wave_channel_id";
        String channelName = "smart_wave_channel_name";

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);
        }

        mStateNoti = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(R.drawable.ic_clear_black,"종료",stopScanPendingIntent());

        if (!text.isEmpty()) {
            mStateNoti.setContentText(text);
        }

        startForeground(1, mStateNoti.build());
    }

    private PendingIntent clickNotiPendingIntent() {

        Intent clickNotiIntent = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 1, clickNotiIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }

    private PendingIntent alarmActivityPendingIntent() {

        Intent clickNotiIntent = new Intent(this, AlarmActivity.class);
        clickNotiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent pending = PendingIntent.getActivity(this, 0, clickNotiIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }

//    private PendingIntent startScanPendingIntent() {
//
//        Intent startScanIntent = new Intent(this, this.getClass());
//        startScanIntent.setAction(ACTION_START_BLUETOOTH_CONNECT);
//        PendingIntent pending = PendingIntent.getService(this, 0, startScanIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        return pending;
//    }

    private PendingIntent stopScanPendingIntent() {
        Intent stopScanIntent = new Intent(this, this.getClass());
        stopScanIntent.setAction(Constants.STOP_SERVICE_ACTION);
        PendingIntent pending = PendingIntent.getService(this, 0, stopScanIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }
}

