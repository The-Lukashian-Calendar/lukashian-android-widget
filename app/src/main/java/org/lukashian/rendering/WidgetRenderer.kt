package org.lukashian.rendering

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import org.lukashian.R
import org.lukashian.data.loadCalendarInfo
import org.lukashian.data.loadIndicator
import org.lukashian.data.loadInstance
import org.lukashian.model.Instance

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    println("Updating Widget $appWidgetId")
    
    val instance = context.loadInstance(appWidgetId)
    val indicator = context.loadIndicator(appWidgetId)
    val calendarInfo = context.loadCalendarInfo(instance) //TODO take into account null

    val year = if (instance == Instance.EARTH) 5926 else 42
    val day = if (instance == Instance.EARTH) 250 else 550
    val beeps = if (instance == Instance.EARTH) 2500 else 5500
    val instanceString = if (indicator) "  (${instance.name.lowercase().replaceFirstChar { it.titlecase() }})" else ""

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.lukashian_widget)

    views.setTextViewText(R.id.beeps_text, "$beeps")
    views.setTextViewText(R.id.day_year_text, "$day - $year$instanceString")

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
