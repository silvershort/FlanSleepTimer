package com.example.sleeptimer

import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.util.Log
import com.example.sleeptimer.R
import android.os.Bundle
import android.view.View
import android.widget.Toast
import java.lang.Runnable
import android.content.Intent
import android.os.Handler
import android.os.Message
import com.example.sleeptimer.TimerService
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    // To save time SharedPreferences
    val sharedPreferences: SharedPreferences by lazy { getSharedPreferences("sTimer", MODE_PRIVATE) }
    var editor: Editor? = null
    val awake = 0
    val sleepy = 1
    val sleep = 2
    var flan = 0

    val che_stop: CheckBox by lazy { findViewById(R.id.che_stop) }
    val che_mute: CheckBox by lazy { findViewById(R.id.che_mute) }
    val che_blue: CheckBox by lazy { findViewById(R.id.che_blue) }
    val tv_timerTime: TextView by lazy { findViewById(R.id.tv_timerTime) }
    val btn_1hour: Button by lazy { findViewById(R.id.btn_1hour) }
    val btn_30min: Button by lazy { findViewById(R.id.btn_30min) }
    val btn_10min: Button by lazy { findViewById(R.id.btn_5min) }
    val btn_reset: Button by lazy { findViewById(R.id.btn_reset) }
    val btn_start: Button by lazy { findViewById(R.id.btn_start) }
    val iv_char: ImageView by lazy { findViewById(R.id.iv_char) }
    var baseTime: Long = 0
    var setTime: Long = 0
    var timer_run = false
    override fun onResume() {
        Log.i("메인로그", "onResume() 호출")

        // Recall stored values on sharedPreferences
        // (If, Do not save when timer expires)
        Log.i("로그", "실행상태 : " + sharedPreferences!!.getBoolean("timer_run", false))
        if (sharedPreferences!!.getBoolean("timer_run", false)) {
            baseTime = sharedPreferences!!.getLong("baseTime", System.currentTimeMillis())
            setTime = sharedPreferences!!.getLong("setTime", 0)
            if (baseTime > System.currentTimeMillis()) {
                timer_run = true
                btn_start!!.text = getString(R.string.btn_run)
                if (baseTime - System.currentTimeMillis() <= setTime * 1000 * 60 / 2) {
                    Log.i("로그", "setTime : $setTime")
                    if (flan == awake) {
                        iv_char!!.setImageResource(R.drawable.image2)
                        flan = sleepy
                    }
                }
                myTimer.sendEmptyMessage(0)
            } else {
                baseTime = 0
                iv_char!!.setImageResource(R.drawable.image3)
                flan = sleep
                timer_run = false
            }
        }
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 각종 요소들을 받아옴
        editor = sharedPreferences.edit()
        if (flan == awake) {
            iv_char.setImageResource(R.drawable.image1)
        }

        // 체크박스 설정을 불러옴
        che_stop.setChecked(sharedPreferences.getBoolean("che_stop", true))
        che_mute.setChecked(sharedPreferences.getBoolean("che_mute", false))
        che_blue.setChecked(sharedPreferences.getBoolean("che_blue", false))
        btn_1hour.setOnClickListener(View.OnClickListener {
            if (setTime >= 720) return@OnClickListener
            if (!timer_run) {
                setTime += 60
                renewTimer()
            }
        })
        btn_30min.setOnClickListener(View.OnClickListener {
            if (setTime >= 720) return@OnClickListener
            if (!timer_run) {
                setTime += 30
                renewTimer()
            }
        })
        btn_10min.setOnClickListener(View.OnClickListener {
            if (setTime >= 720) return@OnClickListener
            if (!timer_run) {
                setTime += 5
                renewTimer()
            }
        })
        btn_reset.setOnClickListener(View.OnClickListener {
            if (!timer_run) {
                setTime = 0
                renewTimer()
            }
        })
        btn_start.setOnClickListener(View.OnClickListener {
            if (!che_stop.isChecked() && !che_mute.isChecked() && !che_blue.isChecked()) {
                Toast.makeText(applicationContext, getString(R.string.nothing), Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }
            if (timer_run) {
                stopTimer()
                stopService()
                flan = awake
                iv_char.setImageResource(R.drawable.image1)
                btn_start.setText(getString(R.string.btn_init))
            } else {
                startTimer()
                startService()
                btn_start.setText(getString(R.string.btn_run))
            }
        })
        iv_char.setOnClickListener(View.OnClickListener {
            if (flan == sleep) {
                iv_char.setImageResource(R.drawable.image2)
                val handler = Handler()
                handler.postDelayed({
                    iv_char.setImageResource(R.drawable.image1)
                    editor?.putBoolean("timer_run", false)?.commit()
                    flan = awake
                }, 2000)
            }
        })
    }

    fun stopTimer() {
//        editor.putBoolean("timer_run", false).commit();
        editor!!.remove("baseTime").commit()
        editor!!.remove("setTime").commit()
        myTimer.removeMessages(0)
        setTime = 0
        renewTimer()
        timer_run = false
    }

    fun startTimer() {
        baseTime = System.currentTimeMillis() + setTime * 1000 * 60 + 1000
        myTimer.sendEmptyMessage(0)
        timer_run = true
        iv_char!!.setImageResource(R.drawable.image1)
        flan = awake
        editor!!.putBoolean("timer_run", true)
        editor!!.putLong("baseTime", baseTime)
        editor!!.putLong("setTime", setTime)
        editor!!.commit()
    }

    fun startService() {
        val serviceIntent = Intent(this, TimerService::class.java)
        serviceIntent.putExtra("baseTime", baseTime)
        serviceIntent.putExtra("stop", che_stop!!.isChecked)
        serviceIntent.putExtra("mute", che_mute!!.isChecked)
        serviceIntent.putExtra("blue", che_blue!!.isChecked)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    fun stopService() {
        val serviceIntent = Intent(this, TimerService::class.java)
        stopService(serviceIntent)
    }

    fun renewTimer() { // 타이머의 시간을 갱신 시켜주는 리소스
        val str_setTime = String.format("%02d:%02d:00", setTime / 60, setTime % 60)
        tv_timerTime!!.text = str_setTime
    }

    var myTimer: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            tv_timerTime!!.text = timeOut
            if (timer_run) {
                sendEmptyMessage(0)
            } else {
                stopTimer()
            }
        }
    }

    //            editor.putBoolean("timer_run", false).commit();
    private val timeOut: String
        private get() {
            val now = System.currentTimeMillis()
            val outTime = baseTime - now
            if (outTime <= 0) {
                btn_start!!.text = getString(R.string.btn_init)
                timer_run = false
                iv_char!!.setImageResource(R.drawable.image3)
                //            editor.putBoolean("timer_run", false).commit();
                flan = sleep
                return "00:00:00"
            }
            if (outTime <= setTime * 1000 * 60 / 2) {
                if (flan == awake) {
                    iv_char!!.setImageResource(R.drawable.image2)
                    flan = sleepy
                }
            }
            return String.format(
                "%02d:%02d:%02d",
                outTime / 1000 / 60 / 60,
                outTime / 1000 / 60 % 60,
                outTime / 1000 % 60
            )
        }

    override fun onStop() {
        Log.i("로그", "stop() 호출")
        // 체크박스 상태 저장
        editor!!.putBoolean("che_stop", che_stop!!.isChecked)
        editor!!.putBoolean("che_mute", che_mute!!.isChecked)
        editor!!.putBoolean("che_blue", che_blue!!.isChecked)
        editor!!.commit()
        super.onStop()
    }
}