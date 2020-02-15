package com.example.sleeptimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    // To save time SharedPreferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    final int awake = 0;
    final int sleepy = 1;
    final int sleep = 2;
    int flan = 0;

    CheckBox che_stop, che_mute, che_blue;
    TextView tv_timerTime;
    Button btn_1hour, btn_30min, btn_10min, btn_reset, btn_start;
    ImageView iv_char;

    long baseTime = 0;
    long setTime = 0;
    boolean timer_run;

    @Override
    protected void onResume() {
        Log.i("메인로그", "onResume() 호출");

        // Recall stored values on sharedPreferences
        // (If, Do not save when timer expires)
        Log.i("로그", "실행상태 : " + sharedPreferences.getBoolean("timer_run", false));
        if (sharedPreferences.getBoolean("timer_run", false)) {
            baseTime = sharedPreferences.getLong("baseTime", System.currentTimeMillis());
            setTime = sharedPreferences.getLong("setTime", 0);
            if (baseTime > System.currentTimeMillis()) {
                timer_run = true;
                btn_start.setText(getString(R.string.btn_run));
                if (baseTime - System.currentTimeMillis() <= setTime * 1000 * 60 / 2) {
                    Log.i("로그", "setTime : " + setTime);
                    if (flan == awake) {
                        iv_char.setImageResource(R.drawable.image2);
                        flan = sleepy;
                    }
                }
                myTimer.sendEmptyMessage(0);
            } else {
                baseTime = 0;
                iv_char.setImageResource(R.drawable.image3);
                flan = sleep;
                timer_run = false;
            }
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 각종 요소들을 받아옴
        che_stop = findViewById(R.id.che_stop);
        che_mute = findViewById(R.id.che_mute);
        che_blue = findViewById(R.id.che_blue);
        tv_timerTime = findViewById(R.id.tv_timerTime);
        btn_1hour = findViewById(R.id.btn_1hour);
        btn_30min = findViewById(R.id.btn_30min);
        btn_10min = findViewById(R.id.btn_5min);
        btn_reset = findViewById(R.id.btn_reset);
        btn_start = findViewById(R.id.btn_start);
        iv_char = findViewById(R.id.iv_char);

        sharedPreferences = getSharedPreferences("sTimer", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (flan == awake) {
            iv_char.setImageResource(R.drawable.image1);
        }

        // 체크박스 설정을 불러옴
        che_stop.setChecked(sharedPreferences.getBoolean("che_stop", true));
        che_mute.setChecked(sharedPreferences.getBoolean("che_mute", false));
        che_blue.setChecked(sharedPreferences.getBoolean("che_blue", false));

        btn_1hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setTime >= 720) return;
                if (!timer_run) {
                    setTime += 60;
                    renewTimer();
                }
            }
        });

        btn_30min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setTime >= 720) return;
                if (!timer_run) {
                    setTime += 30;
                    renewTimer();
                }
            }
        });

        btn_10min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setTime >= 720) return;
                if (!timer_run) {
                    setTime += 5;
                    renewTimer();
                }
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timer_run) {
                    setTime = 0;
                    renewTimer();
                }
            }
        });

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!che_stop.isChecked() && !che_mute.isChecked() && !che_blue.isChecked()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.nothing), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (timer_run) {
                    stopTimer();
                    stopService();
                    flan = awake;
                    iv_char.setImageResource(R.drawable.image1);
                    btn_start.setText(getString(R.string.btn_init));
                } else {
                    startTimer();
                    startService();
                    btn_start.setText(getString(R.string.btn_run));
                }
            }
        });

        iv_char.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flan == sleep) {
                    iv_char.setImageResource(R.drawable.image2);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            iv_char.setImageResource(R.drawable.image1);
                            editor.putBoolean("timer_run", false).commit();
                            flan = awake;
                        }
                    }, 2000);
                }
            }
        });
    }

    public void stopTimer() {
//        editor.putBoolean("timer_run", false).commit();
        editor.remove("baseTime").commit();
        editor.remove("setTime").commit();
        myTimer.removeMessages(0);
        setTime = 0;
        renewTimer();
        timer_run = false;
    }

    public void startTimer() {
        baseTime = System.currentTimeMillis() + setTime * 1000 * 60 + (1000);
        myTimer.sendEmptyMessage(0);
        timer_run = true;

        iv_char.setImageResource(R.drawable.image1);
        flan = awake;

        editor.putBoolean("timer_run", true);
        editor.putLong("baseTime", baseTime);
        editor.putLong("setTime", setTime);
        editor.commit();
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.putExtra("baseTime", baseTime);
        serviceIntent.putExtra("stop", che_stop.isChecked());
        serviceIntent.putExtra("mute", che_mute.isChecked());
        serviceIntent.putExtra("blue", che_blue.isChecked());
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        stopService(serviceIntent);
    }

    public void renewTimer() { // 타이머의 시간을 갱신 시켜주는 리소스
        String str_setTime = String.format("%02d:%02d:00", setTime/60, setTime%60);
        tv_timerTime.setText(str_setTime);
    }

    Handler myTimer = new Handler() {
        public void handleMessage(Message msg) {
            tv_timerTime.setText(getTimeOut());
            if (timer_run){
                myTimer.sendEmptyMessage(0);
            } else {
                stopTimer();
            }
        }
    };

    private String getTimeOut() {
        long now = System.currentTimeMillis();
        long outTime = baseTime - now;
        if (outTime <= 0) {
            btn_start.setText(getString(R.string.btn_init));
            timer_run = false;
            iv_char.setImageResource(R.drawable.image3);
//            editor.putBoolean("timer_run", false).commit();
            flan = sleep;
            return "00:00:00";
        }
        if (outTime <= setTime * 1000 * 60 / 2) {
            if (flan == awake) {
                iv_char.setImageResource(R.drawable.image2);
                flan = sleepy;
            }
        }
        String easy_outTime = String.format("%02d:%02d:%02d", outTime / 1000 / 60 / 60, (outTime / 1000 / 60) % 60, (outTime / 1000) % 60);
        return easy_outTime;
    }

    @Override
    protected void onStop() {
        Log.i("로그", "stop() 호출");
        // 체크박스 상태 저장
        editor.putBoolean("che_stop", che_stop.isChecked());
        editor.putBoolean("che_mute", che_mute.isChecked());
        editor.putBoolean("che_blue", che_blue.isChecked());
        editor.commit();
        super.onStop();
    }
}