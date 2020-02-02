package com.example.sleeptimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class TimerService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    Thread timeThread;
    Handler muteHandler;

    long baseTime;
    long outTime;
    String msg;
    boolean che_stop, che_mute, che_blue;
    boolean alive = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean run = intent.getBooleanExtra("run", false);

        if (!run) {
            che_stop = intent.getBooleanExtra("stop", false);
            che_mute = intent.getBooleanExtra("mute", false);
            che_blue = intent.getBooleanExtra("blue", false);
        }

        baseTime = intent.getLongExtra("baseTime", System.currentTimeMillis());

        outTime = baseTime - System.currentTimeMillis();

        timeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            Log.i("서비스로그", "쓰레드 작동");
                            createNotificationChannel();
                            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                            notificationIntent.putExtra("run", true);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                            if (outTime / 1000 / 60 <= 0) {
                                msg = getString(R.string.timeout);
                            } else {
                                msg = (outTime / 1000 / 60) + getString(R.string.leftTime);
                            }

                            Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                    .setContentText(msg)
                                    .setSmallIcon(R.drawable.ic_brightness_3_black_24dp)
                                    .setContentIntent(pendingIntent)
                                    .build();
                            startForeground(1, notification);

                            if (outTime / 1000 / 60 <= 0) {
                                Thread.currentThread().interrupt();
                            } else {
                                outTime -= 1000 * 60;
                                SystemClock.sleep(1000 * 60);
                            }
                        }
                    } catch (Exception e) {
                    } finally {
                        if (!alive) {
                            return;
                        }
                        Log.i("로그", "stop : " + che_stop + "\nche_mute : " + che_mute + "\nche_blue : " + che_blue);

                        if (che_stop) {
                            Log.i("로그", "audioStop()");
                            audioStop();
                        }

                        if (che_blue) {
                            Log.i("로그", "setBlue()");
                            setBlue();
                        }

                        if (che_mute) {
                            muteHandler = new Handler(Looper.getMainLooper());
                            muteHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("로그", "mute()");
                                    mute();
                                    timeout();
                                }
                            }, 1000);
                        } else {
                            timeout();
                        }
                    }
                }
        });

        timeThread.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        timeThread.interrupt();
        alive = false;
        super.onDestroy();
    }

    private void timeout() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);

        stopForeground(Service.STOP_FOREGROUND_DETACH);
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            // 진동과 소리를 제거한다
            serviceChannel.setVibrationPattern(new long[]{0});
            serviceChannel.enableVibration(true);
            serviceChannel.setSound(null, null);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public TimerService() {
    }

    // 블루투스 끄기
    private void setBlue() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.disable();
    }

    // 오디오 정지
    private void audioStop() {
        AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest audioFocusRequest;
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
                    .setWillPauseWhenDucked(true)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int focusChange) {
                        }
                    })
                    .build();
            mAudioManager.requestAudioFocus(audioFocusRequest);
        } else {
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }

    // 뮤트
    private void mute() {
        AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
