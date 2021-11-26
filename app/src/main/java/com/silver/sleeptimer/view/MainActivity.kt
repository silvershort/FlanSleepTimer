package com.silver.sleeptimer.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.sleeptimer.R
import com.example.sleeptimer.databinding.ActivityMainBinding
import com.silver.sleeptimer.firebase.AppVersionCheck
import com.silver.sleeptimer.viewmodel.MainViewModel

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

        // 앱 버전 체크
        val appVersionCheck = AppVersionCheck(this@MainActivity)
        appVersionCheck.versionCheck()
        if (!checkWhitelist()) {
            showWhitelistDialog()
        }
    }

    // 화이트 리스트 추가 됐는지 확인
    private fun checkWhitelist(): Boolean {
        val powerManager: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
         return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    // 화이트 리스트 추가할껀지 묻고 설청창으로 이동
    @SuppressLint("BatteryLife")
    private fun showWhitelistDialog() {
        var dialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.whitelist_add))
            .setPositiveButton(getString(R.string.common_ok)) { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.common_cancel)) { _, _ ->
                dialog?.dismiss()
            }
        dialog = builder.create()
        dialog.show()
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