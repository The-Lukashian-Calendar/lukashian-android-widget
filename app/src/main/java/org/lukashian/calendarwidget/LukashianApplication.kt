package org.lukashian.calendarwidget

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_ON
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import org.lukashian.calendarwidget.rendering.updateAllWidgets

class LukashianApplication : Application() {
    private val logger = Logger(LukashianApplication::class)

    override fun onCreate() {
        super.onCreate()
        logger.info("Application Created")

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
    private val logger = Logger(ScreenReceiver::class)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SCREEN_ON -> {
                logger.info("Screen On received")
                updateAllWidgets(context)
            }
        }
    }
}
