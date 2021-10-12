package com.example.sleeptimer

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat

class TimerService : Service() {
    private var timeThread: Thread? = null
    private var muteHandler: Handler? = null
    private var baseTime: Long = 0
    private var outTime: Long = 0
    private var msg: String? = null
    private var cheStop = false
    private var cheMute = false
    private var cheBlue = false
    private var alive = true

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
                            PendingIntent.FLAG_UPDATE_CURRENT
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
                    Log.i("로그", "stop : $cheStop\nche_mute : $cheMute\nche_blue : $cheBlue")
                    if (cheStop) {
                        Log.i("로그", "audioStop()")
                        audioStop()
                    }
                    if (cheBlue) {
                        Log.i("로그", "setBlue()")
                        setBlue()
                    }
                    if (cheMute) {
                        muteHandler = Handler(Looper.getMainLooper())
                        muteHandler!!.postDelayed({
                            Log.i("로그", "mute()")
                            mute()
                            timeout()
                        }, 1000)
                    } else {
                        timeout()
                    }
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
        startActivity(mainIntent)
        stopForeground(STOP_FOREGROUND_DETACH)
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

    // 블루투스 끄기
    private fun setBlue() {
        val bluetoothManger: BluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManger.adapter.disable()
    }

    // 오디오 정지
    private fun audioStop() {
        val mAudioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        val audioFocusRequest: AudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
            .setWillPauseWhenDucked(true)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { }
            .build()
        mAudioManager.requestAudioFocus(audioFocusRequest)
    }

    // 뮤트
    private fun mute() {
        val mAudioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }
}