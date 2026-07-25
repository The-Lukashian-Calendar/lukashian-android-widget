package org.lukashian

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.lukashian.data.CalendarInfoUpdater
import org.lukashian.data.deleteCalendarInfo
import org.lukashian.data.deleteWidgetData
import org.lukashian.model.Instance
import org.lukashian.rendering.startRenderLoop
import org.lukashian.rendering.stopRenderLoop
import java.util.concurrent.TimeUnit

class LukashianWidget : AppWidgetProvider() {

    //When first widget is added
    override fun onEnabled(context: Context) {
        println("Widget enabled")

        val immediateUpdate = OneTimeWorkRequestBuilder<CalendarInfoUpdater>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "immediateUpdate",
            ExistingWorkPolicy.REPLACE,
            immediateUpdate
        )

        val periodicUpdate = PeriodicWorkRequestBuilder<CalendarInfoUpdater>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag("periodicUpdate")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "periodicUpdate",
            ExistingPeriodicWorkPolicy.KEEP, // Keeps the schedule intact without resetting the timer
            periodicUpdate
        )

        startRenderLoop(context)
    }

    //When one or more instances of a widget need updating
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        println("Widgets ${appWidgetIds.contentToString()} updating")
        startRenderLoop(context)
    }

    //When user deletes one or more instances of a widget
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            println("Widget $appWidgetId deleted")
            context.deleteWidgetData(appWidgetId)
        }
    }

    //When last widget is deleted
    override fun onDisabled(context: Context) {
        println("Widget disabled")

        stopRenderLoop()

        context.deleteCalendarInfo(Instance.EARTH)
        context.deleteCalendarInfo(Instance.MARS)

        WorkManager.getInstance(context).cancelAllWorkByTag("periodicUpdate")
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                println("Boot completed received")
                startRenderLoop(context)
            }
        }
    }
}
