package org.lukashian.calendarwidget.rendering

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import org.lukashian.calendarwidget.Logger
import org.lukashian.calendarwidget.R
import org.lukashian.calendarwidget.data.loadCalendarInfo
import org.lukashian.calendarwidget.data.loadIndicator
import org.lukashian.calendarwidget.data.loadInstance
import org.lukashian.calendarwidget.data.scheduleCalendarInfoUpdate

private val logger = Logger(WidgetRenderer::class)
internal class WidgetRenderer

@SuppressLint("DefaultLocale")
internal fun renderAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    logger.info("Rendering Widget $appWidgetId")

    val instance = context.loadInstance(appWidgetId)
    val indicator = context.loadIndicator(appWidgetId)
    val info = context.loadCalendarInfo(instance)

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.lukashian_widget)

    if (info == null) {
        showLoading(views, context, false)
    } else {
        val currentTime = System.currentTimeMillis() / 1000

        logger.debug("localEpoch: ${info.localEpoch}")
        logger.debug("firstDayNumber: ${info.firstDayNumber}")
        logger.debug("firstYearOfDayNumber: ${info.firstYearOfDayNumber}")
        logger.debug("nextYearStartIndex: ${info.nextYearStartIndex}")
        logger.debug("offsets: ${info.offsets}")
        logger.debug("currentTime: $currentTime")

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
        logger.debug("index: $index")
        logger.debug("endOfPreviousDay: $endOfPreviousDay")
        logger.debug("startOfDay: $startOfDay")
        logger.debug("endOfDay: $endOfDay")

        val totalSecondsOfDay = endOfDay - endOfPreviousDay
        val passedSecondsOfDay = currentTime - startOfDay //Use startOfDay, in order not to count current second itself as having passed, thereby achieving [0000-9999]
        logger.debug("totalSecondsOfDay: $totalSecondsOfDay")
        logger.debug("passedSecondsOfDay: $passedSecondsOfDay")

        val proportionPassed = ((passedSecondsOfDay.toDouble() / totalSecondsOfDay.toDouble()) * 10000.toDouble())
        logger.debug("proportionPassed: $proportionPassed")

        val day = if (index < info.nextYearStartIndex) (info.firstDayNumber + index) else (index - info.nextYearStartIndex + 1)
        val year = if (index < info.nextYearStartIndex) info.firstYearOfDayNumber else (info.firstYearOfDayNumber + 1)
        logger.debug("day: $day")
        logger.debug("year: $year")

        val beepsString = String.format("%04d", proportionPassed.toInt())
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
