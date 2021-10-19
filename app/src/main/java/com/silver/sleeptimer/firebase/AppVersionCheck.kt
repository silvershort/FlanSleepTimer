package com.silver.sleeptimer.firebase

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.sleeptimer.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class AppVersionCheck(private val context: Context) {
    companion object {
        const val VERSION_NAME_KEY = "version_name"
    }

    private val applicationContext = context.applicationContext
    private val config = FirebaseRemoteConfig.getInstance()

    init {
        initRemoteConfig()
    }

    private fun initRemoteConfig() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0).build()
        config.setConfigSettingsAsync(configSettings)
    }

    fun versionCheck() {
        config.fetchAndActivate().addOnCompleteListener {
            val versionName = config.getString(VERSION_NAME_KEY)
            val currentName = currentVersionName()
            compareVersion(versionName, currentName)
        }
    }

    private fun compareVersion(newVersion: String, oldVersion: String) {
        val newArr = newVersion.split(".")
        val oldArr = oldVersion.split(".")
        for ((i, n) in newArr.withIndex()) {
            if (n.toInt() > oldArr[i].toInt()) {
                updateDialog()
            }
        }
    }

    private fun updateDialog() {
        val builder = AlertDialog.Builder(context)
        var updateDialog: AlertDialog? = null
        builder.setMessage(context.getString(R.string.app_update))
            .setPositiveButton(context.getString(R.string.common_update)) { _, _ ->
                goToPlayStore()
                updateDialog!!.dismiss()
            }
            .setNegativeButton(context.getString(R.string.common_cancel)) { _, _ ->
                updateDialog!!.dismiss()
            }
        updateDialog = builder.create()
        updateDialog!!.show()
    }

    private fun goToPlayStore() {
        try {
            val appStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${applicationContext.packageName}"))
            appStoreIntent.setPackage("com.silver.sleeptimer")
            context.startActivity(appStoreIntent)
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${applicationContext.packageName}")))
        }
    }

    private fun currentVersionName() = applicationContext.packageManager
        .getPackageInfo(applicationContext.packageName, 0).versionName
}