package com.silver.sleeptimer.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.silver.sleeptimer.AlarmReceiver
import com.example.sleeptimer.R
import com.silver.sleeptimer.util.TimerFunction
import com.silver.sleeptimer.view.MainActivity

class TimerService : Service() {
    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }
    private var timeThread: Thread? = null
    private var baseTime: Long = 0
    private var outTime: Long = 0
    private var msg: String? = null
    private var cheStop = false
    private var cheMute = false
    private var cheBlue = false
    private var alive = true
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: Intent
    private lateinit var alarmPendingIntent: PendingIntent

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val run = intent.getBooleanExtra("run", false)
        if (!run) {
            cheStop = intent.getBooleanExtra("stop", false)
            cheMute = intent.getBooleanExtra("mute", false)
            cheBlue = intent.getBooleanExtra("blue", false)
        }
        baseTime = intent.getLongExtra("baseTime", System.currentTimeMillis())
        outTime = baseTime - System.currentTimeMillis()
        setAlarmManager(baseTime + 1000)

        // 포그라운드 서비스에서 쓰레드를 돌리면서 노티피케이션을 업데이트 해주고 최종적으로 타이머 동작을 하게 한다.
        timeThread = Thread {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    createNotificationChannel()
                    val notificationIntent = Intent(applicationContext, MainActivity::class.java)
                    notificationIntent.putExtra("run", true)
                    val pendingIntent =
                        PendingIntent.getActivity(
                            applicationContext,
                            0,
                            notificationIntent,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            } else PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    msg = if (outTime / 1000 / 60 <= 0) {
                        getString(R.string.timeout)
                    } else {
                        (outTime / 1000 / 60).toString() + getString(R.string.leftTime)
                    }
                    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                        .setContentText(msg)
                        .setSmallIcon(R.drawable.ic_brightness_3_black_24dp)
                        .setContentIntent(pendingIntent)
                        .build()
                    startForeground(1, notification)
                    if (outTime / 1000 / 60 <= 0) {
                        Thread.currentThread().interrupt()
                    } else {
                        outTime -= (1000 * 60).toLong()
                        SystemClock.sleep((1000 * 60).toLong())
                    }
                }
            } catch (e: Exception) {
            } finally {
                if (alive) {
                    if (cheStop) {
                        TimerFunction.audioStop(applicationContext)
                    }
                    if (cheBlue) {
                        TimerFunction.offBlue(applicationContext)
                    }
                    if (cheMute) {
                        TimerFunction.audioMute(applicationContext)
                    }
                    timeout()
                }
            }
        }
        timeThread!!.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timeThread!!.interrupt()
        alive = false
        super.onDestroy()
    }

    private fun timeout() {
        val mainIntent = Intent(applicationContext, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        alarmManager.cancel(alarmPendingIntent)
        startActivity(mainIntent)
        stopSelf()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun setAlarmManager(triggerTime: Long) {
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(applicationContext, AlarmReceiver::class.java)
        alarmPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                applicationContext, 1001, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(
                applicationContext, 1001, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            alarmPendingIntent
        )
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        // 진동과 소리를 제거한다
        serviceChannel.vibrationPattern = longArrayOf(0)
        serviceChannel.enableVibration(true)
        serviceChannel.setSound(null, null)
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}