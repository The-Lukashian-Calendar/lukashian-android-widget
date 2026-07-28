package org.lukashian

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager
import org.lukashian.data.deleteCalendarInfo
import org.lukashian.data.deleteWidgetData
import org.lukashian.data.scheduleCalendarInfoUpdate
import org.lukashian.model.Instance
import org.lukashian.rendering.updateAppWidget
import java.time.ZonedDateTime

class LukashianWidget : AppWidgetProvider() {

    //When first widget is added
    override fun onEnabled(context: Context) {
        Log.d("LukashianWidget", "Widget enabled")
        scheduleCalendarInfoUpdate(context)
    }

    //When one or more instances of a widget need updating
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d("LukashianWidget", "Widgets ${appWidgetIds.contentToString()} updating")

        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        refreshUpdateSchedule(context, runImmediately = false)
    }

    //When user deletes one or more instances of a widget
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            Log.d("LukashianWidget", "Widget $appWidgetId deleted")
            context.deleteWidgetData(appWidgetId)
        }
        refreshUpdateSchedule(context, runImmediately = true)
    }

    //When last widget is deleted
    override fun onDisabled(context: Context) {
        Log.d("LukashianWidget", "Widget disabled")

        context.deleteCalendarInfo(Instance.EARTH)
        context.deleteCalendarInfo(Instance.MARS)

        WorkManager.getInstance(context).cancelAllWorkByTag("periodicUpdate")
    }
}

private const val UPDATE_INTERVAL_SECONDS = 4L

internal fun refreshUpdateSchedule(context: Context, runImmediately: Boolean) {
    Log.d("LukashianWidget", "Refreshing Update Schedule")

    if (getActiveWidgetIds(context).isNotEmpty()) {
        val nextUpdate = if (runImmediately) {
            ZonedDateTime.now().plusSeconds(UPDATE_INTERVAL_SECONDS)
        } else {
            ZonedDateTime.now()
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC,
            nextUpdate.toInstant().toEpochMilli(),
            getUpdatePendingIntent(context)
        )
    }
}

private fun getUpdatePendingIntent(context: Context): PendingIntent {
    val updateIntent = Intent(context, LukashianWidget::class.java)
        .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getActiveWidgetIds(context))

    val requestCode = LukashianWidget::class.java.name.hashCode()
    val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

    return PendingIntent.getBroadcast(context, requestCode, updateIntent, flags)
}

private fun getActiveWidgetIds(context: Context): IntArray {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val componentName = ComponentName(context, LukashianWidget::class.java)
    return appWidgetManager.getAppWidgetIds(componentName)
}
