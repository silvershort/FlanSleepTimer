package com.example.sleeptimer.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sleeptimer.R
import com.example.sleeptimer.service.TimerService
import com.example.sleeptimer.util.Flan
import com.example.sleeptimer.util.SharedPreferenceManager

class MainViewModel(application: Application) : AndroidViewModel(application)  {

    // service를 실행하기 위해서 AndroidViewModel을 이용하여 applicationContext를 받아준다.
    @SuppressLint("StaticFieldLeak")
    private val applicationContext = getApplication<Application>().applicationContext

    // 라이프사이클

    // 체크박스 설정 및 타이머 시간을 저장하기 위한 sharedPreferences
    private val preferenceManager = SharedPreferenceManager.getInstance(applicationContext)

    // 타이머 텍스트와 연결된 라이브데이터
    private var _time = MutableLiveData<Long>()
    val time: LiveData<Long>
        get() = _time
    // 캐릭터 상태와 연결된 라이브데이터
    private var _state = MutableLiveData<Int>()
    val state: LiveData<Int>
        get() = _state

    // 체크박스와 연결된 양방향 라이브데이터
    val stop = MutableLiveData<Boolean>()
    val mute = MutableLiveData<Boolean>()
    val blue = MutableLiveData<Boolean>()

    // 타이머가 꺼지는 시각
    private var baseTime: Long = 0
    // 현재 타이머가 켜져 있는지 확인하는 메서드
    var run = MutableLiveData<Boolean>()

    // 타이머를 제어하는 핸들러
    private val myTimer: Handler

    init {
        // 핸들러 세팅
        myTimer = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                _time.value = timeOut()
                when (run.value) {
                    true -> {
                        sendEmptyMessage(0)
                    }
                    null, false -> {
                        stopTimer()
                    }
                }
            }
        }

        stop.value = preferenceManager.getStop()
        mute.value = preferenceManager.getMute()
        blue.value = preferenceManager.getBlue()
    }

    fun viewModelDataSet() {
        _time.value = preferenceManager.getSetTime()
        baseTime = preferenceManager.getBaseTime()
        run.value = preferenceManager.getTimerRun()
        Log.d("@@@", "run : ${run.value}")

        if (run.value!!) {
            if (baseTime > System.currentTimeMillis()) {
                myTimer.sendEmptyMessage(0)
            } else {
                stopTimer()
                run.value = false
//                _state.value = Flan.SLEEP.state
            }
        }
    }

    fun timerPause() {
        myTimer.removeMessages(0)
    }

    /* 뷰 클릭 함수 영역 시작 */
    fun timeAdd(addTime: Int) {
        if (_time.value!! < 360 * 1000 * 60) { // 최대 6시간까지 설정 가능
            // 분단위로 들어온 값을 밀리세컨트값으로 변환
            _time.value = _time.value?.plus(addTime * 1000 * 60)
        }
    }

    fun timeReset() {
        _time.value = 0
    }

    fun timerOnOff() {
        // 체크박스를 하나도 선택하지 않았을경우 기능을 실행하지 않음
        if (!stop.value!! && !mute.value!! && !blue.value!!) {
            Toast.makeText(applicationContext, applicationContext.getString(R.string.nothing), Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (run.value!!) {
            run.value = false
            stopTimer()
            stopService()
            _time.value = 0
            _state.value = Flan.AWAKE.state
        } else {
            run.value = true
            baseTime = System.currentTimeMillis() + _time.value!! + 1000
            startTimer()
            startService()
        }
    }

    fun flanTouch() {
        // 타이머가 끝나고 플랑이 자고있을때 터치 애니메이션
        when(_state.value) {
            Flan.SLEEP.state -> {
                _state.value = Flan.SLEEPY.state
                Handler(Looper.getMainLooper()).postDelayed({
                    _state.value = Flan.AWAKE.state
                }, 2000)
            }
        }
    }
    /* 뷰 클릭 함수 영역 끝 */

    fun stopTimer() {
        preferenceManager.putTimerRun(false)
        preferenceManager.removeBaseTime()
        preferenceManager.removeSetTime()
        preferenceManager.editorApply()
        myTimer.removeMessages(0)
        _time.value = 0
    }

    private fun startTimer() {
        myTimer.sendEmptyMessage(0)
        _state.value = Flan.AWAKE.state
        preferenceManager.putTimerRun(true)
        preferenceManager.putSetTime(_time.value!!)
        // 마지막 시간 설정을 저장해서 위젯에서 빠른시작을 했을때 시간을 자동으로 설정해준다.
        preferenceManager.putLastTime(_time.value!!)
        preferenceManager.putBaseTime(baseTime)
        preferenceManager.editorApply()
    }

    private fun startService() {
        val serviceIntent = Intent(applicationContext, TimerService::class.java)
        serviceIntent.putExtra("baseTime", baseTime)
        serviceIntent.putExtra("stop", stop.value)
        serviceIntent.putExtra("mute", mute.value)
        serviceIntent.putExtra("blue", blue.value)
        ContextCompat.startForegroundService(applicationContext, serviceIntent)
    }

    private fun stopService() {
        val serviceIntent = Intent(applicationContext, TimerService::class.java)
        applicationContext.stopService(serviceIntent)
    }

    private fun timeOut(): Long {
        val now = System.currentTimeMillis()
        val outTime = baseTime - now
        val setTime = preferenceManager.getSetTime()
        if (outTime <= 0) {
            run.value = false
            _state.value = Flan.SLEEP.state
            return 0
        }
        if (outTime <= setTime / 2) {
            if (_state.value == Flan.AWAKE.state) {
                _state.value = Flan.SLEEPY.state
            }
        }
        return outTime
    }

    override fun onCleared() {
        // 체크박스 상태 저장
        preferenceManager.putStop(stop.value!!)
        preferenceManager.putMute(mute.value!!)
        preferenceManager.puttBlue(blue.value!!)
        preferenceManager.editorApply()
        super.onCleared()
    }
}