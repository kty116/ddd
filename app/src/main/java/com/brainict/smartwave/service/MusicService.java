package com.brainict.smartwave.service;

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
import com.brainict.smartwave.R;
import com.brainict.smartwave.activity.MainActivity;
import com.brainict.smartwave.broadcastReceiver.MusicReceivers;
import com.brainict.smartwave.common.Constants;
import com.brainict.smartwave.common.TinyDB;
import com.brainict.smartwave.event.CallEvent;
import com.brainict.smartwave.event.MessageEvent;
import com.brainict.smartwave.event.MusicServiceEvent;
import com.brainict.smartwave.event.PlayMusicEvent;
import com.brainict.smartwave.model.AudioSetModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.brainict.smartwave.common.Constants.AFTER_SLEEP_MODE_ACTION;
import static com.brainict.smartwave.common.Constants.AWAKENING_ACTION;
import static com.brainict.smartwave.common.Constants.MINUTE;

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
    private boolean mChangeAudio = false;
    private ArrayList<AudioSetModel> mAudioSetModelList;
    private TinyDB mTinyDB;
    private long mCheckTime = 0;
    private String mSleepModeName = "";
    private AudioManager mAudioManager;
    private double mDivideVolume;
    private int mTimeTick = 0;
    private Audio audio2;
    private String mAction;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNoti("");
        EventBus.getDefault().register(this);
        mTinyDB = new TinyDB(getApplicationContext());

        audio = new Audio();
        audio.start();

        if (audio != null)
            audio.waveform = Audio.SINE;

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
                        setVolume();
                        setPlayMusicAlarm(false, 0);
                        Bundle bundle = intent.getExtras();
                        mAudioSetModelList = (ArrayList<AudioSetModel>) bundle.getSerializable(Constants.AUDIO_LIST);

                        if (mAudioSetModelList.size() == 3) {
                            mSleepModeName = "자동";
                            mCheckTime = MINUTE * 7 * 60 + 30;
                        } else {
                            mSleepModeName = "수동";
                            mCheckTime = MINUTE * 8 * 60;
                        }

                        Log.d(TAG, "onStartCommand: " + mCheckTime);

                        if (audio != null) {
                            setAudio(mAudioSetModelList, 0);
                        }

                        setSleepAlarm(true);

                        break;

                    case Constants.DEEP_SLEEP_ACTION: //서비스 시작
                        createNoti("깊은수면");
                        setVolume();
                        setPlayMusicAlarm(false, 0);

                        mAudioSetModelList = new ArrayList<>();
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 2 * Constants.SETTING_HZ_UP, MINUTE * 10, false));
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 3 * Constants.SETTING_HZ_UP, MINUTE * 10, false));
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 4 * Constants.SETTING_HZ_UP, MINUTE * 10, false));
                        if (audio != null) {
                            setAudio(mAudioSetModelList, 0);
                        }

                        break;

                    case Constants.REST_ACTION: //서비스 시작
                        createNoti("휴식");
                        setVolume();
                        setPlayMusicAlarm(false, 0);

                        mAudioSetModelList = new ArrayList<>();
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 8 * Constants.SETTING_HZ_UP, MINUTE * 10, false));
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 10 * Constants.SETTING_HZ_UP, MINUTE * 10, false));
                        mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 12 * Constants.SETTING_HZ_UP, MINUTE * 10, false));
                        if (audio != null) {
                            setAudio(mAudioSetModelList, 0);
                        }

                        break;

                    case Constants.STOP_SERVICE_ACTION:

                        EventBus.getDefault().post(new MusicServiceEvent(false));
                        stopSelf();

                        break;

                    case Constants.AFTER_SLEEP_MODE_ACTION:
                        setSleepAlarm(false);
                        mAction = AFTER_SLEEP_MODE_ACTION;
                        if (mSleepModeName.equals("자동")) {
                            createNoti("기상유도");
                            initAudioManager();
                            volumeControlThread();

                            if (audio!= null){
                                audio.stop();
                            }

                            audio = new Audio();
                            audio2 = new Audio();

                            mAudioSetModelList = new ArrayList<>();
                            mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 10, MINUTE * 10, false));
                            mAudioSetModelList.add(new AudioSetModel(Audio.SQUARE, 10, MINUTE * 10, false));
                            mAudioSetModelList.add(new AudioSetModel(Audio.SINE, 13, MINUTE * 10, false));
                            if (audio != null) {
                                audio.start();
                                setWakeUpAudio(mAudioSetModelList, 0);
                            }

                        } else {
//                            Log.d(TAG, "onStartCommand: 수동");
                            EventBus.getDefault().post(new MusicServiceEvent(false));
                            stopSelf();
                        }

                }
            }
        }
        return START_NOT_STICKY;
    }

    public void initAudioManager() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        double streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mDivideVolume = streamMaxVolume / 25;
    }

    private void volumeControlThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 25; i++) {
                    try {
                        mTimeTick++;
                        Thread.sleep(Constants.MINUTE);
                    } catch (InterruptedException e) {

                    } finally {
//                        Log.d(TAG, "volumeControlThread: " + mTimeTick + "분 - " + (int) Math.round(mDivideVolume * mTimeTick));
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) Math.round(mDivideVolume * mTimeTick), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                }
            }
        }).start();
    }

    public void setWakeUpAudio(ArrayList<AudioSetModel> audioSetModelList, int index) {
        if (index == 2) {
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

    public void setVolume() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int streamMaxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        int volume = mTinyDB.getInt(Constants.SETTING_VOLUME);
        switch (volume) {
            case 1:
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(streamMaxVolume / 4), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case 2:
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(streamMaxVolume / 2), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case 3:
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(streamMaxVolume), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
        }

    }

    public void setAudio(ArrayList<AudioSetModel> audioSetModelList, int index) {
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

    public void setSleepAlarm(boolean start) {

        Intent alarmIntent = new Intent(this, this.getClass());
        alarmIntent.setAction(Constants.AFTER_SLEEP_MODE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long delay = mCheckTime;

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

        if (event instanceof MusicServiceEvent) {
            MusicServiceEvent musicService = (MusicServiceEvent) event;

        } else if (event instanceof PlayMusicEvent) {

            for (int i = 0; i < mAudioSetModelList.size(); i++) {
                if (mAudioSetModelList.get(i).isAlreadyPlayed()) {
                    if (i == mAudioSetModelList.size() - 1) {  //마지막 이면 끄기
                        if (mAction != null) {
                            EventBus.getDefault().post(new MusicServiceEvent(false));
                            stopSelf();
                            return;
                        }
                        for (int k = 0; k < mAudioSetModelList.size(); k++) {
                            mAudioSetModelList.get(k).setAlreadyPlayed(false);
                        }
                        setAudio(mAudioSetModelList, 0);

//                        stopForeground(true);
//                        EventBus.getDefault().post(new MusicServiceEvent(false));
//                        stopSelf();
                    }
                } else {
                    if (mAction != null) {
                        setWakeUpAudio(mAudioSetModelList, i);
                    } else {
                        setAudio(mAudioSetModelList, i);
                    }

                    break;
                }
            }
        } else if (event instanceof CallEvent) {
            audio.thread.start();

        }
    }

    // Audio
    protected class Audio implements Runnable {
        protected static final int SINE = 0;
        protected static final int SQUARE = 1;
        protected static final int SAWTOOTH = 2;

        protected int waveform;
        protected boolean mute;

        protected double frequency;
        protected double level;

        public Thread thread;

        private AudioTrack audioTrack;

        protected Audio() {
            frequency = 5.0;
            level = 1.0;
        }

        // Start
        protected void start() {
            thread = new Thread(this, "Audio");
            thread.start();
        }

        // Stop
        protected void stop() {
//            Thread t = thread;
            if (thread != null) {
                thread.interrupt();
            }
            thread = null;

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
                } catch (Exception e) {
                }
            }

            audioTrack.stop();
            audioTrack.release();
        }
    }

    public void createNoti(String text) {

        String channelId = "smart_wave_music_channel_id";
        String channelName = "smart_wave_music_channel_name";

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);
        }

        mStateNoti = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(R.drawable.ic_clear_black, "종료", stopScanPendingIntent());

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

    private PendingIntent stopScanPendingIntent() {
        Intent stopScanIntent = new Intent(this, this.getClass());
        stopScanIntent.setAction(Constants.STOP_SERVICE_ACTION);
        PendingIntent pending = PendingIntent.getService(this, 0, stopScanIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }

    public void startWakeUpService(Context context) {
        Intent wakeUpIntent = new Intent(context, WakeUpService.class);
        wakeUpIntent.setAction(AWAKENING_ACTION);
        wakeUpIntent.putExtra("sleep_mode", true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(wakeUpIntent);
        } else {
            context.startService(wakeUpIntent);
        }
    }
}

