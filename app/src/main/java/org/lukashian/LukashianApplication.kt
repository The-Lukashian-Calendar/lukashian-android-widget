package org.lukashian

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.Intent.ACTION_SCREEN_ON
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import org.lukashian.rendering.startRenderLoop
import org.lukashian.rendering.stopRenderLoop

class LukashianApplication : Application() {

    //TODO: Simulate enabled, deleted, disabled cycle and see full logging of it
    //TODO: Simulate periodic updates with screen on, screen off, reboot, 60 second auto update

    //TODO: Calendar logic to get the actual numbers, then also test http loading
    //TODO: Example image + launcher icons
    //TODO: Put Android URL instead of Garmin URL in README and repo URL

    override fun onCreate() {
        super.onCreate()
        println("Application Created")

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
                println("Screen On received")
                startRenderLoop(context)
            }
            ACTION_SCREEN_OFF -> {
                println("Screen Off received")
                stopRenderLoop()
            }
        }
    }
}
