package com.example.sleeptimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TimerService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    CountDownTimer countDownTimer;
    long baseTime;
    long outTime;
    String msg;
    boolean che_stop, che_media, che_blue;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean end = intent.getBooleanExtra("end", false);

        che_stop = intent.getBooleanExtra("che_stop", false);
        che_media = intent.getBooleanExtra("che_media", false);
        che_blue = intent.getBooleanExtra("che_blue", false);

        Log.i("로그", "stop : " + che_stop + "\nche_media : " + che_media + "\nche_blue : " + che_blue);

        if (end) {
            if (che_stop) {
                Log.i("로그", "audioStop()");
                audioStop();
            }
            if (che_media) {
                Log.i("로그", "mute()");
                mute();
            }
            if (che_blue) {
                Log.i("로그", "setBlue()");
                setBlue();
            }
            Intent serviceIntent = new Intent(this, TimerService.class);
            stopService(serviceIntent);
        } else {
            baseTime = intent.getLongExtra("baseTime", System.currentTimeMillis());

            outTime = baseTime - System.currentTimeMillis();

            countDownTimer = new CountDownTimer((baseTime - System.currentTimeMillis()) * 1000, 1000 * 60) {
                @Override
                public void onTick(long millisUntilFinished) {
                    createNotificationChannel();
                    Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                    if (outTime / 1000 / 60 <= 0) {
                        msg = "타이머가 종료되었습니다.";
                    } else {
                        msg = "타이머가 " + (outTime / 1000 / 60) + "분 남았습니다.";
                    }

                    Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                            .setContentText(msg)
                            .setSmallIcon(R.drawable.ic_brightness_3_black_24dp)
                            .setContentIntent(pendingIntent)
                            .build();
                    startForeground(1, notification);

                    outTime -= 1000 * 60;
                }

                @Override
                public void onFinish() {
                    System.runFinalization();
                    System.exit(0);
                }
            };
            countDownTimer.start();
        }
//        createNotificationChannel();
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentText(notiMsg)
//                .setSmallIcon(R.drawable.ic_brightness_3_black_24dp)
//                .setContentIntent(pendingIntent)
//                .build();
//        startForeground(1, notification);

        return START_NOT_STICKY;
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
            serviceChannel.setSound(null,null);

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

        if (mAudioManager != null) {
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
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
