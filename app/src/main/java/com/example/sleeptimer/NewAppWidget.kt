package com.example.sleeptimer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sleeptimer.NewAppWidget.Companion.ACTION_BTN

/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_BTN = "TIMER_START"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val action = intent?.action
        if (action == ACTION_BTN) {
            context?.let {
                val sharedPreferences = it.getSharedPreferences("sTimer", AppCompatActivity.MODE_PRIVATE)
                val editor: SharedPreferences.Editor = sharedPreferences.edit()

                val run: Boolean = sharedPreferences.getBoolean("timerRun", false)
                val stop: Boolean = sharedPreferences.getBoolean("stop", true)
                val mute: Boolean = sharedPreferences.getBoolean("mute", false)
                val blue: Boolean = sharedPreferences.getBoolean("blue", false)
                var baseTime: Long = sharedPreferences.getLong("baseTime", 0)
                val setTime: Long = (30 * 1000 * 60).toLong()

                if (run) {
                    if (baseTime < System.currentTimeMillis()) {
                        return
                    }
                }

                baseTime = System.currentTimeMillis() + setTime + 1000
                editor.putBoolean("timerRun", true)
                editor.putLong("baseTime", baseTime)
                editor.putLong("setTime", setTime)
                editor.apply()

                val serviceIntent = Intent(context.applicationContext, TimerService::class.java)
                serviceIntent.putExtra("baseTime", baseTime)
                serviceIntent.putExtra("stop", stop)
                serviceIntent.putExtra("mute", mute)
                serviceIntent.putExtra("blue", blue)
                ContextCompat.startForegroundService(context.applicationContext, serviceIntent)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

@SuppressLint("UnspecifiedImmutableFlag")
internal fun activityIntent(context: Context) : PendingIntent {
    val intent = Intent(context, MainActivity::class.java)
    return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

@SuppressLint("UnspecifiedImmutableFlag")
internal fun timerIntent(context: Context) : PendingIntent {
    val intent = Intent(context, NewAppWidget::class.java).setAction(ACTION_BTN)
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.new_app_widget)

    views.setOnClickPendingIntent(R.id.appwidget_button_app, activityIntent(context))
    views.setOnClickPendingIntent(R.id.appwidget_button_timer, timerIntent(context))

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}