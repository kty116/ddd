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
import com.moms.babysounds.model.AudioSetModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MusicService extends Service {

    public final String TAG = MusicService.class.getSimpleName();


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
    private Thread mThread;
    private boolean mChangeAudio = false;
    private ArrayList<AudioSetModel> mAudioSetModelList;
    private TinyDB mTinyDB;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
        }

        setPlayMusicAlarm(false, 0);

        EventBus.getDefault().unregister(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case Constants.INDUCE_SLEEP_ACTION: //서비스 시작
                        createNoti("수면유도");
                        Bundle bundle = intent.getExtras();

                        audio = new Audio();

                        mAudioSetModelList = (ArrayList<AudioSetModel>) bundle.getSerializable(Constants.AUDIO_LIST);
                        Log.d(TAG, "onStartCommand: " + mAudioSetModelList.size());

                        for (int i = 0; i < mAudioSetModelList.size(); i++) {
                            Log.d(TAG, "onStartCommand: " + mAudioSetModelList.get(i));
                        }

                        if (audio != null) {
                            audio.start();
                            setAudio(mAudioSetModelList, 0);
                        }
                        break;

                    case Constants.DEEP_SLEEP_ACTION: //서비스 시작
                        createNoti("깊은수면");
                        audio = new Audio();

                        mAudioSetModelList = new ArrayList<>();
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 2, Constants.MINUTE * 10, false));
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 3, Constants.MINUTE * 10, false));
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 4, Constants.MINUTE * 10, false));
                        if (audio != null) {
                            audio.start();
                            setAudio(mAudioSetModelList, 0);
                        }

                        break;

//                    case Constants.AWAKENING_ACTION: //서비스 시작
//
//                        audio = new Audio();
//
//                        if (audio != null) {
//                            audio.start();
//                            setAudio(mAudioSetModelList, 0);
//                        }
//                        break;

                    case Constants.REST_ACTION: //서비스 시작
                        createNoti("휴식");

                        audio = new Audio();

                        mAudioSetModelList = new ArrayList<>();
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 8, Constants.MINUTE * 10, false));
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 10, Constants.MINUTE * 10, false));
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 12, Constants.MINUTE * 10, false));
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

    public void setAudio(ArrayList<AudioSetModel> audioSetModelList, int index) {
        Log.d(TAG, "setAudio: " + index);
        AudioSetModel audioSetModel = audioSetModelList.get(index);
        audio.waveform = audioSetModel.getAudioWaveForm();
        audio.frequency = audioSetModel.getHz();

        audioSetModel.setAlreadyPlayed(true);

        setPlayMusicAlarm(true, audioSetModel.getTime());

    }

    public void setPlayMusicAlarm(boolean start, final long time) {

        Intent alarmIntent = new Intent(this, MusicReceivers.class);
        alarmIntent.setAction(Constants.PLAY_MUSIC_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long delay = time;  //10분

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
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
                        stopForeground(true);
                        EventBus.getDefault().post(new MusicServiceEvent(false));
                        stopSelf();
                    }
                } else {
                    setAudio(mAudioSetModelList, i);
                    break;
                }
            }
        }
    }

    // Audio
    public class Audio implements Runnable {

        public static final int SINE = 0;
        public static final int SQUARE = 1;
        public static final int SAWTOOTH = 2;

        protected int waveform;
        protected boolean mute;

        protected double frequency;
        protected double level;

        protected Thread thread;

        private AudioTrack audioTrack;

        protected Audio() {
            frequency = 4.0;
            level = 1.0;
        }

        // Start
        protected void start() {
            thread = new Thread(this, "Audio");
            thread.start();
        }

        // Stop
        protected void stop() {
            frequency = 4.0;
            level = 0.0;
            Thread t = thread;
            thread = null;

            // Wait for the thread to exit
            while (t != null && t.isAlive())
                Thread.yield();
        }

        public void run() {
            processAudio();
        }

        // Process audio
        protected void processAudio() {
            short buffer[];

            int rate =
                    AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
            int minSize =
                    AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);

            // Find a suitable buffer size
            int sizes[] = {1024, 2048, 4096, 8192, 16384, 32768};
            int size = 0;

            for (int s : sizes) {
                if (s > minSize) {
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

            if (state != AudioTrack.STATE_INITIALIZED) {
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

            while (thread != null) {
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

    public void createNoti(String text) {

        String channelId = "smart_wave_channel_id";
        String channelName = "smart_wave_channel_name";

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_LOW;
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
        PendingIntent pending = PendingIntent.getActivity(this, 1, clickNotiIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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

