package com.example.sleeptimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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


public class MainActivity extends AppCompatActivity {

    // 시간을 저장하기 위한 SharedPreferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    Intent intent;
    final int REQUEST_CODE = 101;
    final int 일반 = 0;
    final int 졸음 = 1;
    final int 수면 = 2;
    int flan = 0;

    CheckBox che_stop, che_media, che_blue;
    TextView tv_timerTime;
    Button btn_1hour, btn_30min, btn_10min, btn_reset, btn_start;
    ImageView iv_char;

    long baseTime = 0;
    long setTime = 0;
    boolean timer_run;

    @Override
    protected void onResume() {
        Log.i("메인로그", "onResume() 호출");
        // sharedPreferences에 저장된 값을 불러옴
        // 단, 타이머가 이미 종료됬을 경우 (저장된 시간값이 현재 시간보다 작을 경우) 저장하지 않음
        Log.i("로그", "실행상태 : " + sharedPreferences.getBoolean("timer_run", false));
        if (sharedPreferences.getBoolean("timer_run", false)) {
            baseTime = sharedPreferences.getLong("baseTime", System.currentTimeMillis());
            setTime = sharedPreferences.getLong("setTime", 0);
            if (baseTime > System.currentTimeMillis()) {
                timer_run = true;
                btn_start.setText("중지");
                if (baseTime - System.currentTimeMillis() <= setTime * 1000 * 60 / 2) {
                    Log.i("로그", "setTime : " + setTime);
                    if (flan == 일반) {
                        iv_char.setImageResource(R.drawable.image2);
                        flan = 졸음;
                    }
                }
                myTimer.sendEmptyMessage(0);
            } else {
                baseTime = 0;
                iv_char.setImageResource(R.drawable.image3);
                flan = 수면;
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
        che_media = findViewById(R.id.che_media);
        che_blue = findViewById(R.id.che_blue);
        tv_timerTime = findViewById(R.id.tv_timerTime);
        btn_1hour = findViewById(R.id.btn_1hour);
        btn_30min = findViewById(R.id.btn_30min);
        btn_10min = findViewById(R.id.btn_10min);
        btn_reset = findViewById(R.id.btn_reset);
        btn_start = findViewById(R.id.btn_start);
        iv_char = findViewById(R.id.iv_char);

        sharedPreferences = getSharedPreferences("sTimer", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (flan == 일반) {
            iv_char.setImageResource(R.drawable.image1);
        }

        // 체크박스 설정을 불러옴
        che_stop.setChecked(sharedPreferences.getBoolean("che_stop", true));
        che_media.setChecked(sharedPreferences.getBoolean("che_media", false));
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
                    setTime += 10;
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
                if (!che_stop.isChecked() && !che_media.isChecked() && !che_blue.isChecked()) {
                    Toast.makeText(getApplicationContext(), "타이머가 수행할 기능을 체크해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (timer_run) {
                    stopTimer();
                    stopService();
                    flan = 일반;
                    iv_char.setImageResource(R.drawable.image1);
                    btn_start.setText("시작");
                } else {
                    startTimer();
                    startService();
                    btn_start.setText("중지");
                }
            }
        });

        iv_char.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flan == 수면) {
                    iv_char.setImageResource(R.drawable.image2);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            iv_char.setImageResource(R.drawable.image1);
                            editor.putBoolean("timer_run", false).commit();
                            flan = 일반;
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

        intent = new Intent(getApplicationContext(), TimerReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public void startTimer() {
        baseTime = System.currentTimeMillis() + setTime * 1000 * 60 + (1000);
        myTimer.sendEmptyMessage(0);
        timer_run = true;

        iv_char.setImageResource(R.drawable.image1);

        editor.putBoolean("timer_run", true);
        editor.putLong("baseTime", baseTime);
        editor.putLong("setTime", setTime);
        editor.commit();

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        intent = new Intent(getApplicationContext(), TimerReceiver.class);

        if (che_stop.isChecked())
            intent.putExtra("che_stop", true);
        if (che_media.isChecked())
            intent.putExtra("che_media", true);
        if (che_blue.isChecked())
            intent.putExtra("che_blue", true);

        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //API 19 이상 API 23미만
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, baseTime, pendingIntent);
            } else {
                //API 19미만
                alarmManager.set(AlarmManager.RTC_WAKEUP, baseTime, pendingIntent);
            }
        } else {
            //API 23 이상
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, baseTime, pendingIntent);
        }
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.putExtra("baseTime", baseTime);
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
            btn_start.setText("시작");
            timer_run = false;
            iv_char.setImageResource(R.drawable.image3);
//            editor.putBoolean("timer_run", false).commit();
            flan = 수면;
            return "00:00:00";
        }
        if (outTime <= setTime * 1000 * 60 / 2) {
            if (flan == 일반) {
                iv_char.setImageResource(R.drawable.image2);
                flan = 졸음;
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
        editor.putBoolean("che_media", che_media.isChecked());
        editor.putBoolean("che_blue", che_blue.isChecked());
        editor.commit();
        super.onStop();
    }
}