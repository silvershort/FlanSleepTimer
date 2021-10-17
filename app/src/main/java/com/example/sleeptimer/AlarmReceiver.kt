package com.example.sleeptimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sleeptimer.service.TimerService
import com.example.sleeptimer.util.SharedPreferenceManager
import com.example.sleeptimer.util.TimerFunction

class AlarmReceiver : BroadcastReceiver() {
    // Foreground 쓰레드 동작만으로는 doze 상태에 들어갔을때 타이머의 기능을 제대로 할 수 없음.
    // 따라서 알람 매니저를 추가로 동작시켜서 알람매니저에 들어왔을때 타이머가 정지하지 않았을경우 알람매니저로 종료시켜준다.
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val preferenceManager = SharedPreferenceManager.getInstance(context)
            val baseTime = preferenceManager.getBaseTime() - 1000
            val now = System.currentTimeMillis()
            if (baseTime < now) {
                Log.d("@@@", "타이머 동작")
                // 타이머 동작 실행
                if (preferenceManager.getStop()) {
                    TimerFunction.audioStop(context)
                }
                if (preferenceManager.getBlue()) {
                    TimerFunction.offBlue(context)
                }
                if (preferenceManager.getMute()) {
                    TimerFunction.audioMute(context)
                }

                // 포그라운드 서비스 종료
                val serviceIntent = Intent(context, TimerService::class.java)
                context.stopService(serviceIntent)
            }
        }
    }
}