package com.silver.sleeptimer.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sleeptimer.R
import com.silver.sleeptimer.ui.theme.FlanSleepTimerTheme
import com.silver.sleeptimer.view.MainActivity.Companion.NORMAL
import com.silver.sleeptimer.view.MainActivity.Companion.SLEEP
import com.silver.sleeptimer.view.MainActivity.Companion.SLEEPY

class MainActivity : AppCompatActivity() {

    companion object {
        const val NORMAL = 0
        const val SLEEPY = 1
        const val SLEEP = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlanSleepTimerTheme {
                MyApp()
            }
        }

        /*// 앱 버전 체크
        val appVersionCheck = AppVersionCheck(this@MainActivity)
        appVersionCheck.versionCheck()
        if (!checkWhitelist()) {
            showWhitelistDialog()
        }*/
    }
    /*// 화이트 리스트 추가 됐는지 확인
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
    }*/
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlanSleepTimerTheme {
        MyApp()
    }
}

@Composable
fun MyApp() {
    // checkbox variable
    var cheStop by remember { mutableStateOf(true) }
    var cheMute by remember { mutableStateOf(false) }
    var cheBlue by remember { mutableStateOf(false) }

    // image variable
    var flanState by remember { mutableStateOf(NORMAL) }

    // timer variable
    var time by remember { mutableStateOf(0L) }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                TimerFunctionCheckBox(state = cheStop,
                    onCheckBoxClick = { cheStop = it },
                    label = stringResource(R.string.cb_media_stop))
                TimerFunctionCheckBox(state = cheMute,
                    onCheckBoxClick = { cheMute = it },
                    label = stringResource(R.string.cb_media_mute))
                TimerFunctionCheckBox(state = cheBlue,
                    onCheckBoxClick = { cheBlue = it },
                    label = stringResource(R.string.cb_blue))
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = when(flanState) {
                        NORMAL -> {
                            R.drawable.image1
                        }
                        SLEEPY -> {
                            R.drawable.image2
                        }
                        SLEEP -> {
                            R.drawable.image3
                        }
                        else -> {
                            R.drawable.image1
                        }
                    }),
                    contentDescription = stringResource(R.string.flan)
                )
                Text(
                    text = convertTime(time),
                    fontSize = 80.sp
                )
            }
            Column() {
                Row(
                ) {
                    Button(
                        onClick = {
                              time += 60 * 60 * 1000
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(text = "1H")
                    }
                    Button(
                        onClick = {
                            time += 30 * 60 * 1000
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(text = "30M")
                    }
                    Button(
                        onClick = {
                            time += 5 * 60 * 1000
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(text = "5M")
                    }
                    Button(
                        onClick = {
                            time = 0
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(text = "리셋")
                    }
                }
                Button(
                    onClick = {
                        if (!cheStop && !cheMute && !cheBlue) {
                            return@Button
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(text = "시작")
                }
            }
        }
    }
}

@Composable
fun TimerFunctionCheckBox(state: Boolean, onCheckBoxClick: (Boolean) -> Unit, label: String) {
    Row (
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = state, onCheckedChange = onCheckBoxClick)
        Text(text = label)
    }
}

fun convertTime(time: Long): String = String.format("%02d:%02d:%02d", time / 1000L / 60L / 60L, time / 1000L / 60L % 60L, time / 1000L % 60L)