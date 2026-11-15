package org.lukashian.calendarwidget.widget.rendering

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.lukashian.calendarwidget.Logger
import org.lukashian.calendarwidget.piForBroadcast
import org.lukashian.calendarwidget.widget.LukashianWidget
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private val logger = Logger(WidgetUpdater::class)

internal class WidgetUpdater(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        logger.info("Calling updateAllWidgets")
        updateAllWidgets(applicationContext)
        return Result.success()
    }
}

//This is done as a safety backup in case the alarms get cancelled AND the periodic update from the manifest also doesn't work
internal fun scheduleWidgetUpdate(context: Context) {
    logger.info("Scheduling Widget update")

    val periodicUpdate = PeriodicWorkRequestBuilder<WidgetUpdater>(15, TimeUnit.MINUTES)
        .addTag("periodicWidgetUpdate")
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "periodicWidgetUpdate",
        ExistingPeriodicWorkPolicy.REPLACE,
        periodicUpdate
    )
}

internal fun unscheduleWidgetUpdate(context: Context) {
    logger.info("Unscheduling Widget update")
    WorkManager.getInstance(context).cancelAllWorkByTag("periodicWidgetUpdate")
}

private const val UPDATE_INTERVAL_SECONDS = 4L

internal fun updateAllWidgets(context: Context) {
    val allWidgetIds = getActiveWidgetIds(context)
    logger.info("Updating all Widgets: ${allWidgetIds.contentToString()}")

    if (allWidgetIds.isEmpty()) {
        return
    }

    val appWidgetManager = context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
    allWidgetIds.forEach { appWidgetId ->
        renderAppWidget(context, appWidgetManager, appWidgetId)
    }

    val updatePI = piForBroadcast(LukashianWidget::class.java, context,
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE, //This calls LukashianWidget.onUpdate
        iExtras = listOf(AppWidgetManager.EXTRA_APPWIDGET_IDS to allWidgetIds))

    val nextUpdate = ZonedDateTime.now().plusSeconds(UPDATE_INTERVAL_SECONDS)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.set(
        AlarmManager.RTC,
        nextUpdate.toInstant().toEpochMilli(),
        updatePI
    )
}

private fun getActiveWidgetIds(context: Context): IntArray {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val componentName = ComponentName(context, LukashianWidget::class.java)
    return appWidgetManager.getAppWidgetIds(componentName)
}
