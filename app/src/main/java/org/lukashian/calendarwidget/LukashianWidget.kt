package org.lukashian.calendarwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import org.lukashian.calendarwidget.data.deleteCalendarInfo
import org.lukashian.calendarwidget.data.deleteWidgetData
import org.lukashian.calendarwidget.data.scheduleCalendarInfoUpdate
import org.lukashian.calendarwidget.data.unscheduleCalendarInfoUpdate
import org.lukashian.calendarwidget.model.Instance
import org.lukashian.calendarwidget.rendering.scheduleWidgetUpdate
import org.lukashian.calendarwidget.rendering.unscheduleWidgetUpdate
import org.lukashian.calendarwidget.rendering.updateAllWidgets

private val logger = Logger(LukashianWidget::class)

class LukashianWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_GLOBAL_TAP = "org.lukashian.calendarwidget.ACTION_WIDGET_GLOBAL_TAP"
    }

    //When first widget is added
    override fun onEnabled(context: Context) {
        logger.info("Widget enabled")
        scheduleCalendarInfoUpdate(context)
        scheduleWidgetUpdate(context)
    }

    //When one or more instances of a widget need rendering
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        logger.info("Widgets ${appWidgetIds.contentToString()} updated")
        updateAllWidgets(context)
    }

    //When user deletes one or more instances of a widget
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            logger.info("Widget $appWidgetId deleted")
            context.deleteWidgetData(appWidgetId)
        }
        updateAllWidgets(context)
    }

    //When last widget is deleted
    override fun onDisabled(context: Context) {
        logger.info("Widget disabled")

        context.deleteCalendarInfo(Instance.EARTH)
        context.deleteCalendarInfo(Instance.MARS)

        unscheduleCalendarInfoUpdate(context)
        unscheduleWidgetUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_WIDGET_GLOBAL_TAP) {
            logger.info("Widget tapped")
            updateAllWidgets(context)
        }
    }
}
