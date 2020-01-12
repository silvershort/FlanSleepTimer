package com.example.sleeptimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class TimerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("리시버로그", "알람 수신 완료");
        boolean stop = intent.getBooleanExtra("che_stop", false);
        boolean media = intent.getBooleanExtra("che_media", false);
        boolean blue = intent.getBooleanExtra("che_blue", false);

        Intent serviceIntent = new Intent(context, TimerService.class);
        serviceIntent.putExtra("end", true);
        serviceIntent.putExtra("che_stop", stop);
        serviceIntent.putExtra("che_media", media);
        serviceIntent.putExtra("che_blue", blue);
        ContextCompat.startForegroundService(context, serviceIntent);
//        Intent endIntent = new Intent(context, MainActivity.class);
//        context.startActivity(endIntent);
    }
}
