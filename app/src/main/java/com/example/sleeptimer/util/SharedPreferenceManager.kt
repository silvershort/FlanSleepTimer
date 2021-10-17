package com.example.sleeptimer.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class SharedPreferenceManager(private val context: Context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: SharedPreferenceManager? = null
        @Synchronized
        fun getInstance(context: Context) = instance
            ?: SharedPreferenceManager(context).also { instance = it }
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("sTimer", AppCompatActivity.MODE_PRIVATE) }
    private val editor: SharedPreferences.Editor by lazy { sharedPreferences.edit() }

    fun getSetTime() = sharedPreferences.getLong("setTime", 0)
    fun getBaseTime() = sharedPreferences.getLong("baseTime", 0)
    fun getLastTime() = sharedPreferences.getLong("lastTime", (30 * 1000 * 60).toLong())
    fun getTimerRun() = sharedPreferences.getBoolean("timerRun", false)
    fun getStop() = sharedPreferences.getBoolean("stop", true)
    fun getMute() = sharedPreferences.getBoolean("mute", false)
    fun getBlue() = sharedPreferences.getBoolean("blue", false)

    fun putSetTime(setTime: Long) { editor.putLong("setTime", setTime) }
    fun putBaseTime(baseTime: Long) { editor.putLong("baseTime", baseTime) }
    fun putLastTime(lastTime: Long) { editor.putLong("lastTime", lastTime) }
    fun putTimerRun(timerRun: Boolean) { editor.putBoolean("timerRun", timerRun) }
    fun putStop(stop: Boolean) { editor.putBoolean("stop", stop) }
    fun putMute(mute: Boolean) { editor.putBoolean("mute", mute) }
    fun puttBlue(blue: Boolean) { editor.putBoolean("blue", blue) }

    fun removeSetTime() { editor.remove("setTime") }
    fun removeBaseTime() { editor.remove("baseTime") }
    fun removeTimerRun() { editor.remove("timerRun") }
    fun removeStop() { editor.remove("stop") }
    fun removeMute() { editor.remove("mute") }
    fun removeBlue() { editor.remove("blue") }

    fun editorApply() { editor.apply() }
}