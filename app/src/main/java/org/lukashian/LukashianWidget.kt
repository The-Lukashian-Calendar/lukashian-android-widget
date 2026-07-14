package org.lukashian

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class LukashianWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteCalendarInstance(context, appWidgetId)
            deleteCalendarIndicator(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val earth = context.getString(R.string.earthInstance)
    val mars = context.getString(R.string.marsInstance)

    val calendarInstance = loadCalendarInstance(context, appWidgetId)
    val indicator = loadCalendarIndicator(context, appWidgetId)

    //TODO: GitHub repo
    //TODO: REST call (updateAppWidget will get called every second, so don't do rest call every time)
    //TODO: Example image + launcher icons
    //TODO: Put Android URL instead of Garmin URL in README

    val year = if (calendarInstance == earth) 5926 else 42
    val day = if (calendarInstance == earth) 250 else 550
    val beeps = if (calendarInstance == earth) 2500 else 5500
    val instance = if (indicator) "  ($calendarInstance)" else ""

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.lukashian_widget)

    views.setTextViewText(R.id.beeps_text, "$beeps")
    views.setTextViewText(R.id.day_year_text, "$day - $year$instance")

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
