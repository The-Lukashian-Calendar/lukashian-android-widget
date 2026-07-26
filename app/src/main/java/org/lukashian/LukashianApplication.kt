package org.lukashian

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.Intent.ACTION_SCREEN_ON
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat

class LukashianApplication : Application() {

    //TODO: Test 5, 10, sideload
    //TODO: Put Android URL instead of Garmin URL in README
    //TODO: Put Android URL as repo URL

    override fun onCreate() {
        super.onCreate()
        Log.d("LukashianConfigureActivity", "Application Created")

        val filter = IntentFilter().apply {
            addAction(ACTION_SCREEN_ON)
            addAction(ACTION_SCREEN_OFF)
        }

        ContextCompat.registerReceiver(
            applicationContext,
            ScreenReceiver(),
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }
}

internal class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SCREEN_ON -> {
                Log.d("LukashianConfigureActivity", "Screen On received")
                refreshUpdateSchedule(context, runImmediately = true)
            }
            ACTION_SCREEN_OFF -> {
                Log.d("LukashianConfigureActivity", "Screen Off received")
                refreshUpdateSchedule(context, runImmediately = true)
            }
        }
    }
}
