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
        Intent serviceIntent = new Intent(context, TimerService.class);
        serviceIntent.putExtra("end", true);
        ContextCompat.startForegroundService(context, serviceIntent);
//        Intent endIntent = new Intent(context, MainActivity.class);
//        context.startActivity(endIntent);
    }
}
