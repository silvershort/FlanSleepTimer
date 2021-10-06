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
import android.os.Looper
import android.os.Message
import com.example.sleeptimer.TimerService
import androidx.core.content.ContextCompat
import com.example.sleeptimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // To save time SharedPreferences
    val sharedPreferences: SharedPreferences by lazy { getSharedPreferences("sTimer", MODE_PRIVATE) }
    val editor: Editor by lazy { sharedPreferences.edit() }
    val awake = 0
    val sleepy = 1
    val sleep = 2
    var flan = 0

    var baseTime: Long = 0
    var setTime: Long = 0
    var timer_run = false

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        Log.i("메인로그", "onResume() 호출")

        // Recall stored values on sharedPreferences
        // (If, Do not save when timer expires)
        Log.i("로그", "실행상태 : " + sharedPreferences.getBoolean("timer_run", false))
        if (sharedPreferences.getBoolean("timer_run", false)) {
            baseTime = sharedPreferences.getLong("baseTime", System.currentTimeMillis())
            setTime = sharedPreferences.getLong("setTime", 0)
            if (baseTime > System.currentTimeMillis()) {
                timer_run = true
                binding.btnStart.text = getString(R.string.btn_run)
                if (baseTime - System.currentTimeMillis() <= setTime * 1000 * 60 / 2) {
                    Log.i("로그", "setTime : $setTime")
                    if (flan == awake) {
                        binding.ivChar.setImageResource(R.drawable.image2)
                        flan = sleepy
                    }
                }
                myTimer.sendEmptyMessage(0)
            } else {
                baseTime = 0
                binding.ivChar.setImageResource(R.drawable.image3)
                flan = sleep
                timer_run = false
            }
        }
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 각종 요소들을 받아옴
        if (flan == awake) {
            binding.ivChar.setImageResource(R.drawable.image1)
        }

        // 체크박스 설정을 불러옴
        binding.cheStop.isChecked = sharedPreferences.getBoolean("che_stop", true)
        binding.cheMute.isChecked = sharedPreferences.getBoolean("che_mute", false)
        binding.cheBlue.isChecked = sharedPreferences.getBoolean("che_blue", false)
        binding.btn1hour.setOnClickListener(View.OnClickListener {
            if (setTime >= 720) return@OnClickListener
            if (!timer_run) {
                setTime += 60
                renewTimer()
            }
        })
        binding.btn30min.setOnClickListener(View.OnClickListener {
            if (setTime >= 720) return@OnClickListener
            if (!timer_run) {
                setTime += 30
                renewTimer()
            }
        })
        binding.btn5min.setOnClickListener(View.OnClickListener {
            if (setTime >= 720) return@OnClickListener
            if (!timer_run) {
                setTime += 5
                renewTimer()
            }
        })
        binding.btnReset.setOnClickListener(View.OnClickListener {
            if (!timer_run) {
                setTime = 0
                renewTimer()
            }
        })
        binding.btnStart.setOnClickListener(View.OnClickListener {
            if (!binding.cheStop.isChecked && !binding.cheMute.isChecked && !binding.cheBlue.isChecked) {
                Toast.makeText(applicationContext, getString(R.string.nothing), Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }
            if (timer_run) {
                stopTimer()
                stopService()
                flan = awake
                binding.ivChar.setImageResource(R.drawable.image1)
                binding.btnStart.setText(getString(R.string.btn_init))
            } else {
                startTimer()
                startService()
                binding.btnStart.setText(getString(R.string.btn_run))
            }
        })
        binding.ivChar.setOnClickListener(View.OnClickListener {
            if (flan == sleep) {
                binding.ivChar.setImageResource(R.drawable.image2)
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    binding.ivChar.setImageResource(R.drawable.image1)
                    editor.putBoolean("timer_run", false).commit()
                    flan = awake
                }, 2000)
            }
        })
    }

    fun stopTimer() {
//        editor.putBoolean("timer_run", false).commit();
        editor.remove("baseTime").commit()
        editor.remove("setTime").commit()
        myTimer.removeMessages(0)
        setTime = 0
        renewTimer()
        timer_run = false
    }

    fun startTimer() {
        baseTime = System.currentTimeMillis() + setTime * 1000 * 60 + 1000
        myTimer.sendEmptyMessage(0)
        timer_run = true
        binding.ivChar.setImageResource(R.drawable.image1)
        flan = awake
        editor!!.putBoolean("timer_run", true)
        editor!!.putLong("baseTime", baseTime)
        editor!!.putLong("setTime", setTime)
        editor!!.commit()
    }

    fun startService() {
        val serviceIntent = Intent(this, TimerService::class.java)
        serviceIntent.putExtra("baseTime", baseTime)
        serviceIntent.putExtra("stop", binding.cheStop.isChecked)
        serviceIntent.putExtra("mute", binding.cheMute.isChecked)
        serviceIntent.putExtra("blue", binding.cheBlue.isChecked)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    fun stopService() {
        val serviceIntent = Intent(this, TimerService::class.java)
        stopService(serviceIntent)
    }

    fun renewTimer() { // 타이머의 시간을 갱신 시켜주는 리소스
        val str_setTime = String.format("%02d:%02d:00", setTime / 60, setTime % 60)
        binding.tvTimerTime.text = str_setTime
    }

    var myTimer: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            binding.tvTimerTime.text = timeOut
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
                binding.btnStart.text = getString(R.string.btn_init)
                timer_run = false
                binding.ivChar.setImageResource(R.drawable.image3)
                //            editor.putBoolean("timer_run", false).commit();
                flan = sleep
                return "00:00:00"
            }
            if (outTime <= setTime * 1000 * 60 / 2) {
                if (flan == awake) {
                    binding.ivChar.setImageResource(R.drawable.image2)
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
        editor.putBoolean("che_stop", binding.cheStop.isChecked)
        editor.putBoolean("che_mute", binding.cheMute.isChecked)
        editor.putBoolean("che_blue", binding.cheBlue.isChecked)
        editor.commit()
        super.onStop()
    }
}