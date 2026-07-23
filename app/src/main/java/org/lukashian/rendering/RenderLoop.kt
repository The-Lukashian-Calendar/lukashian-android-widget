package org.lukashian.rendering

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import org.lukashian.LukashianWidget

private const val UPDATE_INTERVAL = 4000L

private val handler = Handler(Looper.getMainLooper())
private var renderTick: Runnable? = null

internal fun startRenderLoop(context: Context) {
    if (renderTick != null) return

    renderTick = object : Runnable {
        override fun run() {
            val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
            val thisWidget = ComponentName(context.applicationContext, LukashianWidget::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            if (allWidgetIds.isNotEmpty()) {
                allWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
                handler.postDelayed(this, UPDATE_INTERVAL)
            }
        }
    }
    handler.post(renderTick!!)
}

internal fun stopRenderLoop() {
    renderTick?.let { handler.removeCallbacks(it) }
    renderTick = null
}
