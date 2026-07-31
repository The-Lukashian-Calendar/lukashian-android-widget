package org.lukashian.calendarwidget

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_ON
import android.content.IntentFilter
import androidx.core.content.ContextCompat

class LukashianApplication : Application() {

    //TODO: Put Android URL instead of Garmin URL in README
    //TODO: Put Android URL as repo URL

    override fun onCreate() {
        super.onCreate()
//        Log.d("LukashianApplication", "Application Created")

        val filter = IntentFilter().apply {
            addAction(ACTION_SCREEN_ON)
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
//                Log.d("LukashianConfigureActivity", "Screen On received")
                refreshUpdateSchedule(context, runImmediately = true)
            }
        }
    }
}
