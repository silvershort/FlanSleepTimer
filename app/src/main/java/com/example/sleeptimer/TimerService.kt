package com.example.sleeptimer

import android.app.*
import java.lang.Thread
import android.content.Intent
import java.lang.Runnable
import android.util.Log
import com.example.sleeptimer.MainActivity
import com.example.sleeptimer.R
import androidx.core.app.NotificationCompat
import com.example.sleeptimer.TimerService
import java.lang.Exception
import android.bluetooth.BluetoothAdapter
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.*

class TimerService : Service() {
    var timeThread: Thread? = null
    var muteHandler: Handler? = null
    var baseTime: Long = 0
    var outTime: Long = 0
    var msg: String? = null
    var che_stop = false
    var che_mute = false
    var che_blue = false
    var alive = true

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val run = intent.getBooleanExtra("run", false)
        if (!run) {
            che_stop = intent.getBooleanExtra("stop", false)
            che_mute = intent.getBooleanExtra("mute", false)
            che_blue = intent.getBooleanExtra("blue", false)
        }
        baseTime = intent.getLongExtra("baseTime", System.currentTimeMillis())
        outTime = baseTime - System.currentTimeMillis()
        timeThread = Thread(Runnable {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    Log.i("서비스로그", "쓰레드 작동")
                    createNotificationChannel()
                    val notificationIntent = Intent(applicationContext, MainActivity::class.java)
                    notificationIntent.putExtra("run", true)
                    val pendingIntent =
                        PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)
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
                if (!alive) {
                    return@Runnable
                }
                Log.i("로그", "stop : $che_stop\nche_mute : $che_mute\nche_blue : $che_blue")
                if (che_stop) {
                    Log.i("로그", "audioStop()")
                    audioStop()
                }
                if (che_blue) {
                    Log.i("로그", "setBlue()")
                    setBlue()
                }
                if (che_mute) {
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
        })
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    // 블루투스 끄기
    private fun setBlue() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.disable()
    }

    // 오디오 정지
    private fun audioStop() {
        val mAudioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioFocusRequest: AudioFocusRequest
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
                .setWillPauseWhenDucked(true)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { }
                .build()
            mAudioManager.requestAudioFocus(audioFocusRequest)
        } else {
            mAudioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
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