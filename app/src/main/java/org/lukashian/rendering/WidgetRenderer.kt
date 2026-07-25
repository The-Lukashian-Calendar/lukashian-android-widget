package org.lukashian.rendering

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import org.lukashian.R
import org.lukashian.data.loadCalendarInfo
import org.lukashian.data.loadIndicator
import org.lukashian.data.loadInstance
import org.lukashian.data.scheduleCalendarInfoUpdate

@SuppressLint("DefaultLocale")
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Log.d("WidgetRenderer", "Updating Widget $appWidgetId")

    val instance = context.loadInstance(appWidgetId)
    val indicator = context.loadIndicator(appWidgetId)
    val info = context.loadCalendarInfo(instance)

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.lukashian_widget)

    if (info == null) {
        showLoading(views, context, false)
    } else {
        val currentTime = System.currentTimeMillis() / 1000

//        Log.d("WidgetRenderer", "localEpoch: ${info.localEpoch}")
//        Log.d("WidgetRenderer", "firstDayNumber: ${info.firstDayNumber}")
//        Log.d("WidgetRenderer", "firstYearOfDayNumber: ${info.firstYearOfDayNumber}")
//        Log.d("WidgetRenderer", "nextYearStartIndex: ${info.nextYearStartIndex}")
//        Log.d("WidgetRenderer", "offsets: ${info.offsets}")
//        Log.d("WidgetRenderer", "currentTime: $currentTime")

        var index = -1
        var endOfPreviousDay = 0
        var startOfDay = 0
        var endOfDay = 0

        for (i in info.offsets.indices) {
            if (currentTime <= info.localEpoch + info.offsets[i]) { //Offset itself marks end of day, and is still included in day itself
                index = i

                endOfPreviousDay = if (i == 0) {
                    info.localEpoch - 1
                } else {
                    info.localEpoch + info.offsets[i-1]
                }
                startOfDay = endOfPreviousDay + 1
                endOfDay = info.localEpoch + info.offsets[i]

                break
            }
        }
        if (index == -1) {
            showLoading(views, context, true)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }
//        Log.d("WidgetRenderer", "index: $index")
//        Log.d("WidgetRenderer", "endOfPreviousDay: $endOfPreviousDay")
//        Log.d("WidgetRenderer", "startOfDay: $startOfDay")
//        Log.d("WidgetRenderer", "endOfDay: $endOfDay")

        val totalSecondsOfDay = endOfDay - endOfPreviousDay
        val passedSecondsOfDay = currentTime - startOfDay //Use startOfDay, in order not to count current second itself as having passed, thereby achieving [0000-9999]
//        Log.d("WidgetRenderer", "totalSecondsOfDay: $totalSecondsOfDay")
//        Log.d("WidgetRenderer", "passedSecondsOfDay: $passedSecondsOfDay")

        val proportionPassed = ((passedSecondsOfDay.toDouble() / totalSecondsOfDay.toDouble()) * 10000.toDouble())
//        Log.d("WidgetRenderer", "proportionPassed: $proportionPassed")

        val day = if (index < info.nextYearStartIndex) (info.firstDayNumber + index) else (index - info.nextYearStartIndex + 1)
        val year = if (index < info.nextYearStartIndex) info.firstYearOfDayNumber else (info.firstYearOfDayNumber + 1)
//        Log.d("WidgetRenderer", "day: $day")
//        Log.d("WidgetRenderer", "year: $year")

        val beepsString = String.format("%.4g", proportionPassed)
        val dayYearString = "$day - $year"
        val instanceString = if (indicator) "  (${instance.name.lowercase().replaceFirstChar { it.titlecase() }})" else ""

        views.setTextViewText(R.id.beeps_text, beepsString)
        views.setTextViewText(R.id.day_year_text, dayYearString + instanceString)
    }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun showLoading(views: RemoteViews, context: Context, scheduleCalendarInfoUpdate: Boolean) {
    if (scheduleCalendarInfoUpdate) {
        scheduleCalendarInfoUpdate(context)
    }
    views.setTextViewText(R.id.beeps_text, context.getString(R.string.loading))
    views.setTextViewText(R.id.day_year_text, "")
}
