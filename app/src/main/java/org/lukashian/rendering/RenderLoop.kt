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
    println("Starting render loop")
    if (renderTick != null) return

    println("Instantiating renderTick")
    renderTick = object : Runnable {
        override fun run() {
            println("Tick")

            val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
            val thisWidget = ComponentName(context.applicationContext, LukashianWidget::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            println("Number of widget ids: ${allWidgetIds.size}")
            if (allWidgetIds.isNotEmpty()) {
                allWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
                val postResult = handler.postDelayed(this, UPDATE_INTERVAL)
                println("Next tick scheduled: $postResult")
            }
        }
    }
    handler.post(renderTick!!)
}

internal fun stopRenderLoop() {
    println("Stopping render loop")
    renderTick?.let { handler.removeCallbacks(it) }
    renderTick = null
}
