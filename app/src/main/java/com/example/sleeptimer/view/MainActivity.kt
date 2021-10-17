package com.example.sleeptimer.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.sleeptimer.R
import com.example.sleeptimer.databinding.ActivityMainBinding
import com.example.sleeptimer.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding.apply {
            lifecycleOwner = this@MainActivity
            viewModel = mainViewModel
        }
        setContentView(binding.root)
    }

    // 위젯으로 타이머 실행했을 경우 앱 실행시 데이터를 최신화해준다
    override fun onResume() {
        super.onResume()
        mainViewModel.viewModelDataSet()
    }

    // 앱이 Pause 상태가 됐을때 자원을 계속 소모하지 않도록 핸들러를 멈춰준다
    override fun onPause() {
        mainViewModel.timerPause()
        super.onPause()
    }
}