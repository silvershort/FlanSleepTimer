package com.silver.sleeptimer.util

import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager

object TimerFunction {
    // 블루투스 끄기
    fun offBlue(context: Context) {
        val bluetoothManger: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManger.adapter.disable()
    }

    // 오디오 정지
    fun audioStop(context: Context) {
        val mAudioManager = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager
        val audioFocusRequest: AudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
            .setWillPauseWhenDucked(true)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { }
            .build()
        mAudioManager.requestAudioFocus(audioFocusRequest)
    }

    // 뮤트
    fun audioMute(context: Context) {
        val mAudioManager = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
    }
}